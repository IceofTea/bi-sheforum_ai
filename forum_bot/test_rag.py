from bot import ForumBot

b = ForumBot()
print(f"\nBot 用户 ID: {b.bot_user_id}")
print(f"已回复帖子数: {len(b.replied_posts)}")

threads = b.forum_api.get_threads(limit=5)
for t in threads:
    tid = t.get("id")
    if tid not in b.replied_posts:
        name = t.get("name", "")
        print(f"\n{'='*50}")
        print(f"测试帖子 [{tid}]: {name[:60]}")

        # 步骤0: 意图判断
        print("\n[步骤0] 意图判断...")
        intent = b.intent.judge(name)
        print(f"  结果: {intent}")
        if not intent.get("is_question", False):
            print("  => 非提问帖，跳过回复 (但标记为已回复以避免重复处理)")
            b._mark_replied(tid)
            continue

        # 步骤1: RAG 问答
        print("\n[步骤1] RAG 问答...")
        result = b.rag.answer_question(name)

        print(f"  来源: {result['source']}")
        print(f"  相似度: {result['similarity']:.4f}")
        print(f"  回答预览: {result['answer'][:150]}...")
        if result.get("recommendations"):
            print(f"  关联推荐: {len(result['recommendations'])} 条")
            for rec in result["recommendations"]:
                print(f"    - {rec['content'][:50]}")

        # 步骤2: 发表评论
        print("\n[步骤2] 发表评论...")
        success = b.forum_api.post_comment(tid, b.bot_user_id, result["answer"])
        print(f"  结果: {'✓ 成功' if success else '✗ 失败'}")

        if success:
            b._mark_replied(tid)
            print("  => 已标记为已回复")

        print(f"\n{'='*50}")
        break
