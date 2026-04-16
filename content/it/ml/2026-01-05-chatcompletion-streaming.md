---
slug: chatcompletion-streaming
author: Bys
categories:
- ml
category: ml
date: '2026-01-05 01:00:00'
keywords: openai, agents, sdk
tags:
- openai
- agents
- sdk
title: OpenAI API
description: "OpenAI Chat Completions API vs Streaming 비교 분석 - 챗봇 개발 시 응답 방식 선택 가이드와 실제 구현 예제"
---


## Streaming vs Chat Completions API  

이전에는 Chat Completions API 를 호출해 챗봇을 만들었는데, Agent 장에서는 Streaming을 통해 응답을 받아와서 차이점에 대해서 살펴봤다.  


### 1. 코드를 통한 예시  
[Chat Completions](https://platform.openai.com/docs/api-reference/chat)
> The Chat Completions API endpoint will generate a model response from a list of messages comprising a conversation.

```python
# Check the key - if you're not using OpenAI, check whichever key you're using! Ollama doesn't need a key.

import os
from openai import OpenAI

openai_api_key = os.getenv('OPENAI_API_KEY')
openai = OpenAI(
    base_url="http://localhost:11434/v1",
    api_key="ollama"
)

# And now - let's ask for a question:
question = "Please propose a hard, challenging question to assess someone's IQ. Respond only with the question."
messages = [{"role": "user", "content": question}]

response = openai.chat.completions.create(
    model="gpt-oss:20b-cloud",
    messages=messages
)

question = response.choices[0].message.content
print(question)

# form a new messages list
messages = [{"role": "user", "content": question}]
response = openai.chat.completions.create(
    model="gpt-oss:20b-cloud",
    messages=messages
)

answer = response.choices[0].message.content
print(answer)
```

[Streaming](https://openai.github.io/openai-agents-python/streaming/)
> Streaming lets you subscribe to updates of the agent run as it proceeds. This can be useful for showing the end-user progress updates and partial responses.

```python
# The imports
from dotenv import load_dotenv
from agents import Agent, Runner, trace

# The usual starting point
load_dotenv(override=True)


# Make an agent with name, instructions, model
agent = Agent(name="Jokestar", instructions="You are a jokestar, you tell jokes", model="gpt-3.5-turbo")

# Run the joke with Runner.run(agent, prompt) then print final_output
with trace("Telling a joke"):
    result = await Runner.run(agent, "Tell a joke about Autonomous AI Agents")
    print(result.final_output)
```

두 코드 모두 LLM 모델을 호출해서 응답을 받아온다. 하지만, Chat Completion API는 response 에 동기방식으로 모든 답변을 받아와서 처리할 수 있고, Streaming 은 비동기식으로 LLM 에서 생성되는 답변 일부 일부를 즉시 받아와서 보여준다.  
하지만, 문서를 자세히 살펴보니 Chat Completions API 에서도 stream=True 옵션을 통해 스트리밍을 할 수 있었다.  

```python
stream = openai.chat.completions.create(
    model="gpt-oss:20b-cloud",
    messages=messages
    stream=True  # 이 부분을 통해 스트림으로 가능
)
for chunk in stream:  # 반복문 필요
    if chunk.choices[0].delta.content:  # 조건 확인 필요
        print(chunk.choices[0].delta.content)  # 조각 처리
```

## 2. 무엇이 다른가?
Chat Completions API 는 openai 라이브러리에서 호출되는 비교적 단순한 API 이며, Streaming 은 Agents SDK 에서 사용되는 비교적 복잡한 수준의 SDK/프레임워크로 이해할 수 있다. 

| 기능 | Chat Completions API | Agents SDK | 설명 |
|------|---------------------|-----------|------|
| **멀티 에이전트** | ❌ 직접 구현 필요 | ✅ 내장 지원 | 여러 에이전트 간 협업 |
| **도구 호출** | ⚠️ 수동 처리 | ✅ 자동 처리 | 함수/API 호출 자동화 |
| **대화 관리** | ⚠️ 수동 관리 | ✅ 자동 관리 | 메시지 히스토리 관리 |
| **트레이싱** | ❌ 없음 | ✅ `trace()` 제공 | 디버깅 및 모니터링 |
| **핸드오프** | ❌ 없음 | ✅ 에이전트 간 전환 지원 | 작업 위임 기능 |
| **스트리밍** | ✅ `stream=True` | ✅ 자동 스트리밍 | 실시간 응답 |
| **컨텍스트 변수** | ❌ 없음 | ✅ 내장 지원 | 세션 상태 관리 |
| **복잡도** | 🔧 낮음 (직접 제어) | 🎯 높음 (추상화) | 구현 난이도 |



## [3. Streaming 사용법](https://openai.github.io/openai-agents-python/streaming/)
Stream 하기 위해서는 Runner.run_streamed() 호출해서 RunResultStreaming 객체를 받을 수 있고, result.stream_events() 에서 비동기식으로 호출되는 스트림 이벤트 오브젝트의 스트림을 받을 수 있다. 
```python
import asyncio
from openai.types.responses import ResponseTextDeltaEvent
from agents import Agent, Runner

async def main():
    agent = Agent(
        name="Joker",
        instructions="You are a helpful assistant.",
    )

    result = Runner.run_streamed(agent, input="Please tell me 5 jokes.")
    async for event in result.stream_events():
        if event.type == "raw_response_event" and isinstance(event.data, ResponseTextDeltaEvent):
            print(event.data.delta, end="", flush=True)


if __name__ == "__main__":
    asyncio.run(main())
```

위 코드에서 raw_response_event 은 LLM 에서 직접 전달되는 raw 이벤트이다. 위 코드에서는 텍스트 데이터를 처리하기 위한 코드로 이해하면 된다.  


---

## 📚 References

[1] **Chat Completions**
- https://platform.openai.com/docs/api-reference/chat

[2] **Streaming**
- https://openai.github.io/openai-agents-python/streaming/

[3] **Streaming events**
- https://openai.github.io/openai-agents-python/ref/stream_events/