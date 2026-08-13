from generator import LLMGenerator

llm = LLMGenerator()
print('Testing LLM...')

print('\n--- Intent Test ---')
resp = llm.judge_intent('你好，请问我校园卡丢了怎么办')
print(f'Intent: {resp}')

print('\n--- Generate Test ---')
resp2 = llm.generate('你好，请介绍一下你自己')
print(f'Generate: {resp2}')