"""校园智脑 — Mock 论坛后端

提供与真实 SpringBoot 论坛一致的 API 接口，内嵌丰富校园示例数据，
使本项目无需 Java/MySQL 即可独立演示运行。

用法：
  python mock_forum.py            # 启动在 4477 端口
  python mock_forum.py --port 4477

支持接口：
  GET  /api/threads/all        帖子列表
  GET  /api/threads/one?id=    帖子详情
  GET  /api/threads/byuser    按用户查帖子
  GET  /api/users/one?id=      用户信息
  GET  /api/users/page         用户分页（含 total）
  GET  /api/users/login        用户登录（POST）
  GET  /api/usercollect/byuser 用户收藏
  GET  /api/comments?threadInfoId= 帖子评论
  GET  /api/bot/replied        已回复列表
  POST /api/bot/replied        记录已回复
  POST /api/comments           发表评论
  GET  /vercode               验证码图片（真实论坛接口）
"""
import argparse
import io
import random
import string

from flask import Flask, request, jsonify, make_response
from flask_cors import CORS

from PIL import Image, ImageDraw, ImageFont, ImageFilter

app = Flask(__name__)
CORS(app)

# ═══════════════════════════════════════════
# 内嵌示例数据
# ═══════════════════════════════════════════

SORTS = [
    {"id": 1, "name": "新生指南"},
    {"id": 2, "name": "学习交流"},
    {"id": 3, "name": "校园服务"},
    {"id": 4, "name": "兴趣社群"},
    {"id": 5, "name": "就业发展"},
    {"id": 6, "name": "校园热点"},
]

USERS = [
    {"id": 1, "username": "xiaoming", "nickname": "小明同学", "level": 8, "experience": 1200,
     "head": "avatar1.png"},
    {"id": 2, "username": "xiaohong", "nickname": "小红", "level": 5, "experience": 600,
     "head": "avatar2.png"},
    {"id": 3, "username": "laoshi", "nickname": "张老师", "level": 20, "experience": 5000,
     "head": "avatar3.png"},
    {"id": 1001, "username": "智能助手", "nickname": "校园小助手", "level": 99, "experience": 99999,
     "head": "default_avatar.png"},
]

THREADS = [
    {"id": 1, "name": "图书馆闭馆时间调整通知：周一至周五22:30闭馆", "introduction": "本学期图书馆闭馆时间调整，周一至周五22:30闭馆，周六日21:30闭馆，请大家合理安排自习时间。",
     "threadsSortId": 3, "writer": "小明同学", "writerId": 1, "status": "0"},
    {"id": 2, "name": "新生报到需要准备哪些材料？", "introduction": "马上开学了，请问新生报到需要准备什么材料？录取通知书、身份证、户口迁移证明都需要吗？",
     "threadsSortId": 1, "writer": "小红", "writerId": 2, "status": "0"},
    {"id": 3, "name": "考研英语一复习攻略，上岸学姐分享", "introduction": "本人英语一83分上岸，分享一下我的复习方法：单词每天100个，真题刷三遍，作文模板要自己整理。有需要的同学可以评论区交流。",
     "threadsSortId": 2, "writer": "小明同学", "writerId": 1, "status": "0"},
    {"id": 4, "name": "一食堂三楼哪个窗口好吃？求推荐", "introduction": "开学三个月了还是没吃明白一食堂，三楼哪家窗口的菜比较好吃？有没有性价比高的推荐？",
     "threadsSortId": 6, "writer": "小红", "writerId": 2, "status": "0"},
    {"id": 5, "name": "宿舍空调不制冷怎么报修？", "introduction": "我们宿舍空调不制冷了，晚上热得睡不着，请问报修流程是什么？大概多久能修好？",
     "threadsSortId": 3, "writer": "小明同学", "writerId": 1, "status": "0"},
    {"id": 6, "name": "四六级考试什么时候报名？流程是什么？", "introduction": "请问今年四六级什么时候开始报名？在哪个系统报？需要提前准备什么？",
     "threadsSortId": 2, "writer": "小红", "writerId": 2, "status": "0"},
    {"id": 7, "name": "失物招领：教学楼捡到学生卡一张", "introduction": "在第三教学楼一楼教室捡到一张学生卡，姓名缩写ZYY，请失主到教务处认领，联系方式见内。",
     "threadsSortId": 3, "writer": "张老师", "writerId": 3, "status": "0"},
    {"id": 8, "name": "校园卡丢失补办流程全攻略", "introduction": "校园卡丢了不要慌，补办流程：挂失→到大学生服务中心→缴费15元→当场拿到新卡。注意挂失后原卡立即失效。",
     "threadsSortId": 3, "writer": "小明同学", "writerId": 1, "status": "0"},
    {"id": 9, "name": "选课系统卡顿怎么办？通识课推荐", "introduction": "选课系统登录不进去，一直转圈怎么办？另外有没有老师给分高的通识课推荐？",
     "threadsSortId": 2, "writer": "小红", "writerId": 2, "status": "0"},
    {"id": 10, "name": "毕业季二手书和闲置物品大甩卖", "introduction": "毕业季处理闲置：考研资料全套、自行车、台灯、收纳箱，价格美丽，学校东门自提，详情私信。",
     "threadsSortId": 6, "writer": "小明同学", "writerId": 1, "status": "0"},
    {"id": 11, "name": "校内实习信息：字节跳动校园大使招募", "introduction": "字节跳动校园大使正在招募，主要负责校园活动推广，可获得实习证明和周边礼品，感兴趣的加群了解。",
     "threadsSortId": 5, "writer": "小红", "writerId": 2, "status": "0"},
    {"id": 12, "name": "图书馆占座问题严重，希望加强管理", "introduction": "最近图书馆占座现象严重，有人用书包占座一整天，建议图书馆加强巡查或者引入座位预约系统。",
     "threadsSortId": 3, "writer": "张老师", "writerId": 3, "status": "0"},
    {"id": 13, "name": "数学建模竞赛组队找队友", "introduction": "九月数学建模国赛，找两名队友，要求：会MATLAB/Python、有责任心，本人在往届省赛获奖，求靠谱队友一起冲国奖。",
     "threadsSortId": 2, "writer": "小明同学", "writerId": 1, "status": "0"},
    {"id": 14, "name": "食堂新增轻食窗口啦", "introduction": "二食堂二楼新开轻食窗口，主打低脂低卡，减脂期的同学可以试试，人均15-20元。",
     "threadsSortId": 6, "writer": "小红", "writerId": 2, "status": "0"},
    {"id": 15, "name": "社团招新季：篮球社/舞蹈社/摄影社", "introduction": "各社团开始招新了，本周五下午操场摆摊，篮球社、舞蹈社、摄影社都有，欢迎大一新生加入。",
     "threadsSortId": 4, "writer": "张老师", "writerId": 3, "status": "0"},
    {"id": 16, "name": "心理咨询预约方式与开放时间", "introduction": "心理中心免费为在校生提供咨询服务，电话预约或线上预约均可，开放时间为周一至周五9:00-17:00。",
     "threadsSortId": 3, "writer": "小红", "writerId": 2, "status": "0"},
    {"id": 17, "name": "期末考试复习资料分享专区", "introduction": "本专区汇总各科往年真题和复习资料，由学长学姐整理，欢迎下载，也欢迎大家补充。",
     "threadsSortId": 2, "writer": "小明同学", "writerId": 1, "status": "0"},
    {"id": 18, "name": "宿舍门禁时间与晚归登记说明", "introduction": "宿舍楼门禁时间为23:30，晚归的同学需要登记，请大家合理安排作息时间。",
     "threadsSortId": 3, "writer": "张老师", "writerId": 3, "status": "0"},
    {"id": 19, "name": "咖啡厅半价券转让", "introduction": "手里有几张图书馆咖啡厅半价券，快过期了，有需要的同学留言，先到先得。",
     "threadsSortId": 6, "writer": "小红", "writerId": 2, "status": "0"},
    {"id": 20, "name": "英语角活动每周三晚不见不散", "introduction": "英语角每周三晚7点在留学生活动中心，有外教参与，欢迎想练口语的同学来玩。",
     "threadsSortId": 4, "writer": "小明同学", "writerId": 1, "status": "0"},
]

COLLECTS = [
    {"id": 1, "userInfoId": 1, "threadInfoId": 3},
    {"id": 2, "userInfoId": 1, "threadInfoId": 8},
    {"id": 3, "userInfoId": 1, "threadInfoId": 13},
    {"id": 4, "userInfoId": 2, "threadInfoId": 5},
    {"id": 5, "userInfoId": 2, "threadInfoId": 9},
]

COMMENTS = {
    1: [{"id": 1, "threadInfoId": 1, "userInfoId": 2, "comment": "太好了，正愁晚上没地方自习"}],
    3: [{"id": 2, "threadInfoId": 3, "userInfoId": 2, "comment": "谢谢学姐分享，已收藏！"},
        {"id": 3, "threadInfoId": 3, "userInfoId": 3, "comment": "补充一点：作文一定要自己动手写模板"}],
    5: [{"id": 4, "threadInfoId": 5, "userInfoId": 2, "comment": "物业电话是8888，打过去就有人来"}],
}

REPLIED = []

# 当前验证码（供登录校验 / vercode 接口使用）
_CURRENT_VERCODE = None


def _gen_vercode(digits=4):
    """生成验证码图片（适配真实论坛 /vercode 接口：GET /vercode 返回图片）"""
    chars = "".join(random.choices(string.ascii_uppercase + string.digits, k=digits))
    global _CURRENT_VERCODE
    _CURRENT_VERCODE = chars
    width, height = 110, 40
    img = Image.new("RGB", (width, height), (255, 255, 255))
    draw = ImageDraw.Draw(img)
    for _ in range(160):
        x = random.randint(0, width)
        y = random.randint(0, height)
        draw.point((x, y), fill=(random.randint(120, 255), random.randint(120, 255), random.randint(120, 255)))
    for i, c in enumerate(chars):
        try:
            font = ImageFont.truetype("arial.ttf", 26)
        except Exception:
            font = ImageFont.load_default()
        draw.text((12 + i * 24, random.randint(2, 8)), c,
                  font=font, fill=(random.randint(0, 100), random.randint(0, 100), random.randint(0, 100)))
    img = img.filter(ImageFilter.SMOOTH)
    buf = io.BytesIO()
    img.save(buf, format="PNG")
    buf.seek(0)
    resp = make_response(buf.read())
    resp.headers["Content-Type"] = "image/png"
    return resp


def _ok(data):
    return jsonify({"code": 200, "msg": "success", "data": data})


@app.route("/api/threads/all", methods=["GET"])
def threads_all():
    return _ok(THREADS)


@app.route("/api/threads/one", methods=["GET"])
def threads_one():
    tid = request.args.get("id", type=int)
    for t in THREADS:
        if t["id"] == tid:
            return _ok(t)
    return _ok(None)


@app.route("/api/threads/byuser", methods=["GET"])
def threads_byuser():
    wid = request.args.get("writerId", type=int)
    return _ok([t for t in THREADS if t.get("writerId") == wid])


@app.route("/api/users/one", methods=["GET"])
def users_one():
    uid = request.args.get("id", type=int)
    for u in USERS:
        if u["id"] == uid:
            return _ok(u)
    return _ok(None)


@app.route("/api/users/page", methods=["GET"])
def users_page():
    return _ok({"total": len(USERS), "list": USERS})


@app.route("/api/users/byusername", methods=["GET"])
def users_byusername():
    name = request.args.get("username", "")
    for u in USERS:
        if u["username"] == name:
            return _ok(u)
    return _ok(None)


@app.route("/api/usercollect/byuser", methods=["GET"])
def collects_byuser():
    uid = request.args.get("userId", type=int)
    return _ok([c for c in COLLECTS if c["userInfoId"] == uid])


@app.route("/api/comments", methods=["GET"])
def comments_get():
    tid = request.args.get("threadInfoId", type=int)
    return _ok(COMMENTS.get(tid, []))


@app.route("/api/comments", methods=["POST"])
def comments_post():
    body = request.json or {}
    return _ok({"id": 999, "comment": body.get("comment", ""), "status": "ok"})


@app.route("/api/bot/replied", methods=["GET"])
def bot_replied():
    return _ok([{"threadInfoId": rid} for rid in REPLIED])


@app.route("/api/bot/replied", methods=["POST"])
def bot_replied_post():
    body = request.json or {}
    tid = body.get("threadInfoId")
    if tid and tid not in REPLIED:
        REPLIED.append(tid)
    return _ok("记录成功")


@app.route("/vercode", methods=["GET"])
def vercode():
    """验证码图片（真实论坛接口，前端 <img src='/vercode'>）"""
    return _gen_vercode(4)


@app.route("/api/users/login", methods=["POST"])
def users_login():
    body = request.json or {}
    username = body.get("username", "")
    password = body.get("password", "")
    for u in USERS:
        if u["username"] == username:
            # Mock：密码任意非空即可登录（演示用），返回非空 token 模拟真实后端 JWT
            fake_token = "mock_token_" + str(u["id"]) + "_" + username
            res = {"id": u["id"], "username": u["username"], "nickname": u["nickname"], "level": u["level"],
                   "token": fake_token, "msg": fake_token}
            return jsonify({"status": 200, **res})
    return jsonify({"status": 404, "msg": "用户不存在"}), 200


@app.route("/api/users/register", methods=["POST"])
def users_register():
    body = request.json or {}
    return _ok({"id": 100, "nickname": body.get("nickname", "")})


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--port", type=int, default=4477)
    args = parser.parse_args()
    print(f"[MockForum] 启动于 http://localhost:{args.port}  (20条示例帖子)")
    app.run(host="0.0.0.0", port=args.port, debug=False)