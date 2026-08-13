import requests

resp = requests.get('http://localhost:4477/api/comments', params={'threadInfoId': 1})
print(f'Status: {resp.status_code}')
if resp.status_code == 200:
    data = resp.json()
    comments = data.get('data', [])
    print(f'Found {len(comments)} comments')
    for c in comments[-3:]:
        print(f'User: {c.get("userInfoId")}, Comment: {c.get("comment", "")[:80]}')