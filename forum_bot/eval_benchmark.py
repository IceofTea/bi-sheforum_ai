"""校园智脑 — 评测脚本

在当前论坛数/用户数（或注入的样本）上运行一组小规模问题，
度量：回答完整性(近似F1)、平均耗时、LLM调用次数/成本、缓存命中率。

用法：
  python eval_benchmark.py            # 直接调用智能体评测（内置10个校园问题）
  python eval_benchmark.py --api      # 通过已启动的 Flask 服务评测
  python eval_benchmark.py --compare  # 对照实验：双通道 vs 单通道 / 自查 vs 无自查

对照实验（--compare）用于量化创新点：
  - 双通道检索（向量+BM25+RRF）相对单向量检索的耗时/质量差异
  - 自查-修复闭环带来的额外 LLM 调用成本
"""
import argparse
import time
import json
import os

# 内置评测集（校园场景真实问题，30 题，覆盖多种意图类别与情绪样本）
# 每题标注 (问题, 意图类别, 是否有明确答案)
BENCHMARK_QUESTIONS = [
    # ── 基础咨询（10题）──
    "图书馆几点关门？",
    "四六级什么时候报名",
    "推荐几门通识课",
    "哪里可以打印论文",
    "新生入学要准备什么",
    "快递驿站周末营业吗",
    "校医院在哪个楼，挂号流程是什么",
    "怎么申请助学贷款",
    "体育馆开放时间",
    "考研自习室哪些可以提前占座",
    # ── 报修 / 后勤（5题）──
    "怎么报修宿舍空调",
    "宿舍热水器不热了找谁修",
    "食堂吃出异物怎么投诉",
    "教室投影仪坏了报修流程",
    "校园网经常断线怎么办",
    # ── 失物 / 求助（5题）──
    "校园卡丢了怎么补办",
    "在图书馆丢了一个黑色书包去哪找",
    "考研英语怎么复习",
    "考试挂科了怎么补考，影响大吗",
    "毕业论文查重怎么避免过高",
    # ── 交易 / 二手（5题）──
    "想买个二手自行车，去哪比较靠谱",
    "二手书在哪卖比较快",
    "宿舍楼里的洗衣机坏了好几天了",
    "校园周边哪里可以打印论文且便宜",
    "搬宿舍东西太多，有人一起拼车吗",
    # ── 情绪化 / 紧急（5题）──
    "我的一卡通丢了，急死了怎么办",
    "食堂饭菜真的太难吃了，我要投诉！",
    "宿舍漏水泡了我的书，很生气",
    "期末复习压力太大，快崩溃了",
    "校园里有人推销刷单，差点被骗，大家小心",
]


def measure(agent, questions):
    """直接调用 CampusAgent，统计指标"""
    summary = {
        "total": len(questions),
        "answered": 0,
        "cache_hit": 0,
        "llm_calls_total": 0,
        "avg_time_ms": 0.0,
        "avg_llm_calls": 0.0,
    }
    times = []
    for q in questions:
        t0 = time.perf_counter()
        result = agent.answer(q)  # 不带 userId：测纯智能体
        dt = (time.perf_counter() - t0) * 1000
        times.append(dt)

        if result.get("answer") and "未能" not in result["answer"] and "失败" not in result["answer"]:
            summary["answered"] += 1
        if result.get("source") == "cache":
            summary["cache_hit"] += 1
        if result.get("stats"):
            summary["llm_calls_total"] += result["stats"].get("llm_calls", 0)

    summary["avg_time_ms"] = round(sum(times) / len(times), 1)
    summary["avg_llm_calls"] = round(summary["llm_calls_total"] / len(questions), 2)
    summary["answer_rate"] = round(summary["answered"] / len(questions), 3)
    return summary


def run_compare(questions):
    """对照实验：量化双通道检索 / 自查修复 的贡献"""
    from agent import CampusAgent

    configs = [
        ("完整版(双通道+自查)", {}),
        ("仅向量检索(关闭双通道)", {"AGENT_DUAL_RETRIEVAL": "false"}),
        ("关闭自查(无Critic)", {"AGENT_SELF_CRITIQUE": "false"}),
    ]
    results = []
    for label, env in configs:
        old = {}
        for k, v in env.items():
            old[k] = os.environ.get(k)
            os.environ[k] = v
        # 强制重新读取 config 模块（因 config 在 import 时读取 env）
        import importlib
        import config
        importlib.reload(config)
        importlib.reload(importlib.import_module("agent"))

        agent = CampusAgent()
        s = measure(agent, questions)
        s["label"] = label
        results.append(s)
        print(f"  {label}: answer_rate={s['answer_rate']} avg_time={s['avg_time_ms']}ms "
              f"avg_llm={s['avg_llm_calls']}")

        # 还原环境变量
        for k, v in env.items():
            if v is None:
                os.environ.pop(k, None)
            else:
                os.environ[k] = v
        importlib.reload(config)
        importlib.reload(importlib.import_module("agent"))
        for k, v in old.items():
            if v is None:
                os.environ.pop(k, None)
            else:
                os.environ[k] = v

    return results


def measure_via_api(base, questions):
    """通过 Flask 服务调用 /api/bot/ask/agent"""
    import requests
    summary = {"total": len(questions), "answered": 0, "cache_hit": 0, "avg_time_ms": 0.0}
    times = []
    for q in questions:
        t0 = time.perf_counter()
        try:
            resp = requests.post(
                f"{base}/api/bot/ask/agent",
                json={"query": q}, timeout=60
            )
            data = resp.json() if resp.ok else {}
            if data.get("answer") and "未能" not in data["answer"]:
                summary["answered"] += 1
            if data.get("source") == "cache":
                summary["cache_hit"] += 1
        except Exception as e:
            print(f"[API] 请求 {q[:20]} 失败: {e}")
        times.append((time.perf_counter() - t0) * 1000)
    summary["avg_time_ms"] = round(sum(times) / len(times), 1)
    summary["answer_rate"] = round(summary["answered"] / len(questions), 3)
    return summary


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--api", action="store_true", help="测试 Flask API")
    parser.add_argument("--base", default="http://localhost:5000", help="Flask 服务地址")
    parser.add_argument("--count", type=int, default=10, help="评测问题数")
    parser.add_argument("--compare", action="store_true", help="对照实验（量化创新点）")
    parser.add_argument("--out", default="", help="对照实验结果 JSON 输出路径")
    args = parser.parse_args()

    questions = BENCHMARK_QUESTIONS[: args.count]

    if args.compare:
        print(f"▶ 对照实验（{args.count} 题）...")
        results = run_compare(questions)
        print("\n===== 对照实验汇总 =====")
        print(f"{'配置':<22} {'回答率':>8} {'平均耗时ms':>10} {'平均LLM':>8}")
        for r in results:
            print(f"{r['label']:<22} {r['answer_rate']:>8.1%} {r['avg_time_ms']:>10.1f} {r['avg_llm_calls']:>8.2f}")
        if args.out:
            payload = {
                "count": len(questions),
                "results": results,
                "questions": questions,
                "generated_at": time.strftime("%Y-%m-%d %H:%M:%S"),
            }
            with open(args.out, "w", encoding="utf-8") as f:
                json.dump(payload, f, ensure_ascii=False, indent=2)
            print(f"\n结果已保存: {args.out}")
    elif args.api:
        print(f"▶ 通过 API 评测 {args.base}，{len(questions)} 题...")
        summary = measure_via_api(args.base, questions)
    else:
        print(f"▶ 直接调用智能体评测，{len(questions)} 题...")
        from agent import CampusAgent
        agent = CampusAgent()
        summary = measure(agent, questions)

    if not args.compare:
        print(json.dumps(summary, ensure_ascii=False, indent=2))
        print("\n指标说明：")
        print("  answer_rate   - 有效回答比例（近似质量）")
        print("  avg_llm_calls - 每题平均 LLM 调用次数（成本，越低越好）")
        print("  avg_time_ms   - 每题平均耗时")