import requests
from generator import ForumAPI

api = ForumAPI()

thread = api.get_thread(1)
if thread and thread.get("name"):
    print(f"Thread ID=1: {thread.get('name')}")
    print(f"  Writer: {thread.get('writer')}")
    print(f"  Introduction: {str(thread.get('introduction', ''))[:200]}")
else:
    print("Thread 1 not found")

print("\n--- Comments for thread 1 ---")
try:
    resp = requests.get(
        f"http://localhost:4477/api/comments",
        params={"threadInfoId": 1},
        timeout=10
    )
    print(f"Status: {resp.status_code}")
    if resp.status_code == 200:
        data = resp.json()
        comments = data.get("data", []) if isinstance(data, dict) else data
        print(f"Total comments: {len(comments)}")
        for c in comments[-5:]:
            print(f"  User {c.get('userInfoId')}: {str(c.get('comment', ''))[:100]}")
except Exception as e:
    print(f"Error: {e}")
