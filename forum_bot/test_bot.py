import requests

# 测试索引API
print("Testing index API...")
try:
    resp = requests.post("http://localhost:5000/api/bot/index", json={"force": True}, timeout=120)
    print(f"Index response: {resp.status_code} - {resp.text}")
except Exception as e:
    print(f"Index error: {e}")

# 测试状态API
print("\nTesting status API...")
try:
    resp = requests.get("http://localhost:5000/api/bot/status", timeout=10)
    print(f"Status: {resp.status_code} - {resp.text}")
except Exception as e:
    print(f"Status error: {e}")

# 测试回复功能
print("\nTesting reply API...")
try:
    resp = requests.post("http://localhost:5000/api/bot/reply", json={"threadId": 1}, timeout=30)
    print(f"Reply response: {resp.status_code} - {resp.text}")
except Exception as e:
    print(f"Reply error: {e}")