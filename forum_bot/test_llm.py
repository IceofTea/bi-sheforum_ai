import sys
sys.path.insert(0, '.')
from generator import LLMGenerator

llm = LLMGenerator()
print("Testing LLM with LongCat API...")
response = llm.generate("你好，请介绍一下你自己")
print(f"Response: {response}")