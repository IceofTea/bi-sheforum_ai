# -*- coding: utf-8 -*-
"""分镜引导式演示录屏：按《项目视频录制指引》9 场景自动操作前端，生成带字幕卡的演示视频底片。

用法：
  python record_demo.py                # 默认约 4分30秒
  python record_demo.py --seconds 280  # 自定义时长
  python record_demo.py --question "图书馆周末几点开门"  # 指定智能问答问题

原理：Playwright 打开演示面板，每个分镜场景自动执行操作并显示字幕卡（后续可配音/剪辑）。
需先启动：python mock_forum.py 和 python bot.py api
"""
import argparse
import json
import time
import sys

OUT = r"D:\2025海之子计算机复试电子资料\福star\学习\人创大赛\提交材料_校园智脑\演示视频_raw.webm"
API = "http://localhost:5000"
CHROMIUM = r"C:\Users\13111\AppData\Local\ms-playwright\chromium-1208\chrome-win64\chrome.exe"


def log(*a):
    print(*a, flush=True)


def subtitle_script_js():
    """注入字幕卡工具：window.__sub(title, lines, kws, ms) 显示并自动淡出"""
    return """
    window.__subTimer = null;
    window.__sub = (title, lines, kws, ms) => {
      const old = document.querySelector('.subtitle');
      if (old) old.remove();
      const d = document.createElement('div');
      d.className = 'subtitle';
      d.innerHTML = `<h1>${title}</h1>` +
        (Array.isArray(lines) ? lines.map(l => `<p>${l}</p>`).join('') : `<p>${lines||''}</p>`) +
        (kws ? `<div class="kws">${kws}</div>` : '');
      document.body.appendChild(d);
      clearTimeout(window.__subTimer);
      if (ms) window.__subTimer = setTimeout(() => { d.classList.add('fade'); setTimeout(()=>d.remove(), 1000); }, ms);
    };
    window.__subHide = () => {
      const d = document.querySelector('.subtitle'); if (d) { d.classList.add('fade'); setTimeout(()=>d.remove(), 1000); }
    };
    true
    """


def smooth_scroll_js():
    """让聊天区自动丝滑滚动到底部，保证回答内容逐条可见"""
    return """
    (function(){
      if (window.__scrollTimer) clearInterval(window.__scrollTimer);
      const el = document.getElementById('chatLog');
      window.__scrollTimer = setInterval(() => {
        el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' });
      }, 600);
      return true;
    })()
    """


def stop_scroll_js():
    return """
    if (window.__scrollTimer) { clearInterval(window.__scrollTimer); window.__scrollTimer = null; }
    true
    """


def highlight(agents):
    """agents: ['Emotion','Planner',...]，逐个点亮并停留"""
    js_agents = json.dumps(agents, ensure_ascii=False)
    return f"""
    (() => {{
      const list = {js_agents};
      let i = 0;
      window.__hlStep = () => {{
        if (i >= list.length) return;
        const name = list[i];
        const box = document.querySelector(`[data-agent="${{name}}"]`);
        if (box) {{
          document.querySelectorAll('.agent-box.hl').forEach(b => b.classList.remove('hl'));
          box.classList.add('hl');
        }}
        i++;
      }};
      window.__hlStep();
      true
    }})()
    """


def main(seconds: int, question: str):
    video = None
    from playwright.sync_api import sync_playwright

    with sync_playwright() as p:
        browser = None
        try:
            browser = p.chromium.launch(
                headless=True, executable_path=CHROMIUM,
                args=["--autoplay-policy=no-user-gesture-required"],
            )
            ctx = browser.new_context(
                viewport={"width": 1600, "height": 1000},
                record_video_dir=r"C:\Users\13111\AppData\Local\Temp\opencode\demo_video",
                record_video_size={"width": 1600, "height": 1000},
            )
            page = ctx.new_page()
            video = page.video
            t_start = time.time()

            def timestamp():
                return time.time() - t_start

            def show(title, lines=None, kws=None, ms=0, hold=0.0):
                page.evaluate(subtitle_script_js())
                page.evaluate("window.__sub(%s, %s, %s, %s)" % (
                    ("'%s'" % title), ("%s" % (json.dumps(lines, ensure_ascii=False) if lines else "null")),
                    ("'%s'" % kws if kws else "null"), ms))
                if hold > 0:
                    page.wait_for_timeout(int(hold * 1000))

            # ── 场景1：开场字幕卡（约30秒）──
            log("[1/9] 场景1 开场与背景 ...")
            page.goto(API + "/", wait_until="networkidle", timeout=30000)
            page.wait_for_timeout(1500)
            show("校园智脑", ["面向高校社区的 LLM 多智能体问答助手",
                              "智能问答 · 数据洞察 · 热点识别 · 内容安全守护"],
                 "全本地部署 · 零API成本 · 结果可解释", hold=16)
            page.__timers__ = None

            # ── 场景2：多智能体架构逐个高亮（约40秒）──
            log("[2/9] 场景2 多智能体架构 ...")
            agents = ["Emotion", "Planner", "Retriever", "Clarify", "Persona",
                      "Critic", "Memory", "Analytics", "Moderator"]
            show("多智能体架构", ["CampusAgent 总控编排 · 各司其职 · 推理轨迹可解释"],
                 "Emotion → Planner → Retriever → Clarify → Persona → Critic → Memory → Analytics → Moderator", hold=5)
            page.evaluate(highlight(agents))
            for _ in range(len(agents)):
                page.evaluate("window.__hlStep()")
                page.wait_for_timeout(1600)
            page.wait_for_timeout(1500)

            # ── 场景3：情绪预判与主动引导（约40秒）──
            log("[3/9] 场景3 情绪预判 ...")
            show("零成本情绪预判", ["先感知用户情绪与紧急度，再自动匹配回答语气与行动指引"],
                 "急切求助 · 愤怒投诉 · 正向分享 · 中性咨询", hold=4)
            page.locator('[data-emo="1"]').click()
            page.wait_for_timeout(16000)
            page.wait_for_timeout(6000)

            # ── 场景4：智能问答 + SSE 推理轨迹（约80秒）──
            log("[4/9] 场景4 智能问答 + SSE推理轨迹 ...")
            show("智能问答 · 实时推理轨迹", ["意向识别→查询规划→双通道检索→评估→个性化生成→自查→行动"],
                 "SSE 流式输出 · 回答逐步可见 · 结果可溯源", hold=4)
            page.evaluate(smooth_scroll_js())
            page.locator("#qInput").fill(question)
            page.evaluate(stop_scroll_js())
            page.evaluate(smooth_scroll_js())  # 重新起滚动让输入可见
            page.locator("#sendBtn").click()
            tq = time.time()
            # 等待完整回答
            try:
                page.wait_for_function(
                    """
                    () => {
                      const logs = document.querySelectorAll('#chatLog .msg.bot');
                      const last = logs[logs.length-1];
                      if (!last) return false;
                      const t = last.textContent;
                      return !t.includes('正在思考') && !t.includes('正在生成')
                             && !t.includes('实时推理轨迹') && t.trim().length > 40;
                    }
                    """,
                    timeout=160000,
                )
            except Exception as e:
                log("   [!] 等待回答超时:", type(e).__name__)
            log("   问答经时 %.0fs" % (time.time() - tq))
            # 丝滑滚动展示回答全文
            page.evaluate(smooth_scroll_js())
            # 展开推理轨迹与证据来源
            try:
                btns = page.locator('#chatLog .msg.bot').last.locator('.btn-toggle')
                for i in range(min(2, btns.count())):
                    btns.nth(i).click()
                    page.wait_for_timeout(2600)
                    page.evaluate(smooth_scroll_js())
            except Exception as e:
                log("   [!] 展开区块异常:", type(e).__name__)
            page.evaluate(smooth_scroll_js())
            page.wait_for_timeout(10000)
            page.evaluate(stop_scroll_js())

            # ── 场景5：个性化与反馈（约25秒）──
            log("[5/9] 场景5 个性化与反馈 ...")
            show("个性化与反馈", ["结合用户画像（资料/收藏/发帖/反馈记忆）给出差异化回答",
                                  "对回答点赞后，后续回答会参考你的历史反馈"],
                 "Persona + Memory 双引擎", hold=8)
            page.wait_for_timeout(4000)

            # ── 场景6：数据分析与热点话题（约30秒）──
            log("[6/9] 场景6 数据分析与热点话题 ...")
            page.evaluate(subtitle_script_js())
            page.evaluate("window.__sub('论坛数据洞察', ['自动识别热点话题 · 统计板块热度 · 生成自然语言洞察报告'], '图书馆 · 考研 · 食堂 · 宿舍 · 失物招领 · 社团', 9000)")
            page.locator('#quickQs .q-chip[data-analytics="1"]').click()
            page.wait_for_timeout(16000)

            # ── 场景7：内容安全审核 + 巡检（约25秒）──
            log("[7/9] 场景7 内容安全审核 + 巡检 ...")
            show("内容安全守护", ["规则 + LLM 两级检测广告 / 辱骂 / 诈骗 / 隐私泄露"],
                 "内容审核 · 批量巡检", hold=3)
            page.locator("#modInput").fill("加微信 xxx 低价代刷，包过四六级，转账即可")
            page.locator("#modBtn").click()
            page.wait_for_timeout(7000)
            page.locator('[data-modscan="1"]').click()
            page.wait_for_timeout(7000)

            # ── 场景8：模块化拼接亮点（约20秒）──
            log("[8/9] 场景8 模块化拼接 ...")
            show("引擎与场景完全解耦", ["情感词典 · 引导话术 · 行动指引 集中在 scene_config.py 一个文件",
                                  "拼接电商 / 政务 / 医疗：只改配置文件 + 替换数据源，引擎零改动"],
                 "Engine 零改动 · 场景即插即用（可复用）", hold=8)
            page.wait_for_timeout(3000)

            # ── 场景9：结尾（约20秒）──
            log("[9/9] 场景9 结尾 ...")
            show("校园智脑", ["全本地部署 · 零API成本 · 隐私闭环 · 推理可解释 · 场景可拼接",
                              "感谢观看"], None, hold=12)
            page.wait_for_timeout(2000)

            # 补齐到设定总时长
            spent = time.time() - t_start
            remain = seconds - spent
            if remain > 0:
                log("   其余画面自然停留 %.0fs" % remain)
                page.wait_for_timeout(int(remain * 1000))

            ctx.close()
            try:
                if video is not None:
                    video.save_as(OUT)
                    log("[OK] 视频已保存:", OUT)
                else:
                    log("[!] page.video 为 None")
            except Exception as e:
                log("[!] save_as 异常:", type(e).__name__, e)
        finally:
            if browser:
                browser.close()


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--seconds", type=int, default=270)
    ap.add_argument("--question", default="图书馆周末的开放时间是怎么安排的")
    args = ap.parse_args()
    main(args.seconds, args.question)