from bot import ForumBot
b = ForumBot()
print(f"Bot user ID: {b.bot_user_id}")
print(f"Replied posts: {len(b.replied_posts)}")

# 获取一个新帖子
threads = b.forum_api.get_threads(limit=5)
for t in threads:
    tid = t.get('id')
    if tid not in b.replied_posts:
        print(f"\nTesting with thread ID: {tid}, Title: {t.get('name')}")
        result = b.process_thread(t)
        print(f"Result: {result}")
        break