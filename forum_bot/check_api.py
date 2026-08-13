import requests

FORUM_API_URL = "http://localhost:4477"

print("Checking forum API...")

resp = requests.get(f"{FORUM_API_URL}/api/users/byusername", params={"username": "智能助手"}, timeout=10)
print(f"byusername: {resp.status_code}")
print(f"Response: {resp.text[:500]}")

resp2 = requests.get(f"{FORUM_API_URL}/api/users/bot", timeout=10)
print(f"\nbot: {resp2.status_code}")
print(f"Response: {resp2.text[:500]}")