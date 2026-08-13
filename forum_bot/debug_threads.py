from generator import ForumAPI
api = ForumAPI()
threads = api.get_threads(limit=100)
q_threads = [t for t in threads if '?' in str(t.get('name','')) or '？' in str(t.get('name',''))]
print(f'Found {len(q_threads)} threads with question mark')
for t in q_threads[:10]:
    print(f'ID: {t.get("id")}, Title: {t.get("name")}')