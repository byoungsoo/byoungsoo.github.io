---
layout: post
title: "Guardrail"
author: "Bys"
category: ml
date: 2026-01-15 01:00:00
keywords: "openai, agents, sdk, guardrail"
tags: openai guardrail sdk
---

# [Guardrail](https://openai.github.io/openai-agents-python/guardrails/)  
Guardrails 는 사용자 입력과, agent 의 출력에 대해서 검증 가능하도록 도와주는 역할을 한다.  
> Guardrails enable you to do checks and validations of user input and agent output.

예를 들어, 부적절한 단어나 용어가 사용되는 경우 혹은 비즈니스적으로 특정 입력/출력을 제약해야하는 경우 우리는 Guardrails 를 활용할 수 있다.  


## 1. Input guardrails  
Input Guardrail 은 사용자 입력에 대해 검증하는 작업을 진행한다.  

1. 입력 받기
사용자가 에이전트에게 보낸 입력이 그대로 guardrail에 전달

2. 검증
Guardrail 함수가 실행되어 입력을 분석 후, 결과로 GuardrailFunctionOutput 생성 → InputGuardrailResult 로 wrapping

3. 예외 처리
.tripwire_triggered 가 true인지 체크하고, True면 → 예외 발생. 이 예외를 catch해서 적절한 응답을 사용자에게 보냄.


```python
class EmojiCheckOutput(BaseModel):
    is_emoji_in_message: bool
    emoji: str

intput_guardrail_agent = Agent( 
    name="Emoji check",
    instructions="Check if the user is including Any 'Emoji icon' in what they want you to do. Examples of emoji: ☀️🌧️❄️⛅🌈🥶🤪😊 etc",
    output_type=EmojiCheckOutput,
    model="gpt-4o-mini"
)
@input_guardrail
async def input_guardrail_against_emoji(ctx, agent, message):
    result = await Runner.run(intput_guardrail_agent, message, context=ctx.context)
    print(f"Input guardrail result: {result}")
    is_emoji_in_message = result.final_output.is_emoji_in_message
    return GuardrailFunctionOutput(output_info={"found_emoji": result.final_output},tripwire_triggered=is_emoji_in_message)
```

```python
manager_instruction = """
You are a senior weather cast manager. Your goal is to find the single best weather cast email using weather_cast_agent tools and send e-mail. 

Follow these steps carefully:
1. Generate Drafts: Use all three weather_cast_agent tools to generate three different email drafts. Do not proceed until all three drafts are ready.
 
2. Evaluate and Select: Review the drafts and choose the single best email using your judgment of which one is most effective.
You can use the tools one time if you're not satisfied with the results, just choose second one.
 
3. Handoff for Sending: Pass ONLY the winning email draft to the 'Email Manager' agent. The Email Manager will take care of sending.
 
Crucial Rules:
- You must use the weather_cast_agent tools to generate the drafts — do not write them yourself.
- You must hand off exactly ONE email to the Email Manager — never more than one.
"""

senior_weather_cast_manager = Agent(
    name="Senior Weather Cast Manager", 
    instructions=manager_instruction, 
    model="gpt-4o-mini", 
    tools=tools, 
    handoffs = handoffs,
    input_guardrails=[input_guardrail_against_emoji])

print("=== 날씨 이메일 생성 시작 ===\n")

message = "☀️ Generate weather cast e-mail drafts for seoul. 2. When you get e-mail drafts, choose best one. Do not call weather cast agent again 3. Pass best one to Email Manager"
with trace("Senior Weather Cast Manager"):
    result = await Runner.run(senior_weather_cast_manager, message)

print(result)
```
위 코드를 실행하면 아래와 같이 오류가 발생하는 것을 알 수 있다.  


```txt
=== 날씨 이메일 생성 시작 ===

Guardrail result: RunResult:
- Last agent: Agent(name="Emoji check", ...)
- Final output (EmojiCheckOutput):
    {
      "is_emoji_in_message": true,
      "emoji": "☀️"
    }
- 1 new item(s)
- 1 raw response(s)
- 0 input guardrail result(s)
- 0 output guardrail result(s)
(See `RunResult` for more details)
```

사용자 Input 에서 `☀️` 이모지를 발견했기 때문이다. 이 와 같이 우리는 Input Guardrail 을 통해 사용자 입력에 대한 Validation 을 진행할 수 있다.  


다음은 Output Guardrail 을 만들어서 만약, 이모지가 들어간 이메일 초안(agetn2 번이 작성한 메일 초안)이 작성되면 Validation 이 걸리도록 해보겠다. 

## 2. Output Guardrails  
Output agent 에서 중요한 내용은 final agent output 에 대ㅐ해 의도된 것이므로 마지막 에이전의 output_guardrails 에 추가되어야 한다는 점이다.  

> Output guardrails are intended to run on the final agent output, so an agent's guardrails only run if the agent is the last agent. 

따라서, 아래 예시 코드에서는 'Senior Weather Cast Manager' 가 weather cast 에이전트를 통해 3가지 초안 중 1가지를 선택하고 Email Manager와 Handoff 하므로 흐름이 Email Manager 에게 넘어가게 되고, Email Manager 에이전트의 Output Guardrails 를 설정해야 한다는 점이다.  


```python
class EmojiCheckOutput(BaseModel):
    is_emoji_in_message: bool
    emoji: str

intput_guardrail_agent = Agent( 
    name="Emoji check",
    instructions="Check if the user is including Any 'Emoji icon' in what they want you to do. Examples of emoji: ☀️🌧️❄️⛅🌈🥶🤪😊 etc",
    output_type=EmojiCheckOutput,
    model="gpt-4o-mini"
)


output_guardrail_agent = Agent( 
    name="Emoji check",
    instructions="Check if the agent is including Any 'Emoji icon' in what they want you to do. Examples of emoji: ☀️🌧️❄️⛅🌈🥶🤪😊 etc",
    output_type=EmojiCheckOutput,
    model="gpt-4o-mini"
)


@input_guardrail
async def input_guardrail_against_emoji(ctx, agent, message):
    result = await Runner.run(intput_guardrail_agent, message, context=ctx.context)
    print(f"Input guardrail result: {result}")
    is_emoji_in_message = result.final_output.is_emoji_in_message
    return GuardrailFunctionOutput(output_info={"found_emoji": result.final_output},tripwire_triggered=is_emoji_in_message)

@output_guardrail
async def output_guardrail_against_emoji(ctx, agent, message):
    result = await Runner.run(output_guardrail_agent, message, context=ctx.context)
    print(f"Output guardrail result: {result}")
    is_emoji_in_message = result.final_output.is_emoji_in_message
    return GuardrailFunctionOutput(output_info={"found_emoji": result.final_output},tripwire_triggered=is_emoji_in_message)
```

```python
manager_instruction = """
You are a senior weather cast manager. Your goal is to find the single best weather cast email using weather_cast_agent tools and send e-mail. 

Follow these steps carefully:
1. Generate Drafts: Use all three weather_cast_agent tools to generate three different email drafts. Do not proceed until all three drafts are ready.
 
2. Evaluate and Select: Review the drafts and choose the single best email using your judgment of which one is most effective.
You can use the tools one time if you're not satisfied with the results, just choose second one.
 
1. Handoff for Sending: Pass ONLY the winning email draft to the 'Email Manager' agent. The Email Manager will take care of sending.
 
Crucial Rules:
- You must use the weather_cast_agent tools to generate the drafts — do not write them yourself.
- You must hand off exactly ONE email to the Email Manager — never more than one.
"""

senior_weather_cast_manager = Agent(
    name="Senior Weather Cast Manager", 
    instructions=manager_instruction, 
    model="gpt-4o-mini", 
    tools=tools, 
    handoffs = handoffs,
    input_guardrails=[input_guardrail_against_emoji])

## 여기 Email Mnager 에 Output Guardrail 을 설정해야 함.  
send_email_agent.output_guardrails = [output_guardrail_against_emoji]

print("=== 날씨 이메일 생성 시작 ===\n")

message = "Generate weather cast e-mail drafts for seoul. 2. When you get e-mail drafts, choose best one. Do not call weather cast agent again 3. Pass best one to Email Manager"
with trace("Senior Weather Cast Manager"):
    result = await Runner.run(senior_weather_cast_manager, message)

print(result)
```

사용자 실행시 메세지에서는 이모지를 제거했으므로 input_guardrails는 통과하고 output_guardrails 에서 2번 초안이 걸려야 한다.  


#### 첫 번째 실행 결과 
Output guardrail result 를 보면 Emoji 가 없이 성공한 것으로 알 수 있다. 초안에서 이모지가 없는 경우가 선택된 경우다.  
```txt
=== 날씨 이메일 생성 시작 ===

Input guardrail result: RunResult:
- Last agent: Agent(name="Emoji check", ...)
- Final output (EmojiCheckOutput):
    {
      "is_emoji_in_message": false,
      "emoji": ""
    }
- 1 new item(s)
- 1 raw response(s)
- 0 input guardrail result(s)
- 0 output guardrail result(s)
(See `RunResult` for more details)
Status Code: 200
Response: {"id":"<20260116022930.7d3d3a87f91e7c38@bys.digital>","message":"Queued. Thank you."}

✅ 이메일 전송 성공!
Output guardrail result: RunResult:
- Last agent: Agent(name="Emoji check", ...)
- Final output (EmojiCheckOutput):
    {
      "is_emoji_in_message": false,
      "emoji": ""
    }
- 1 new item(s)
- 1 raw response(s)
- 0 input guardrail result(s)
- 0 output guardrail result(s)
(See `RunResult` for more details)
RunResult:
- Last agent: Agent(name="Email Manager", ...)
- Final output (str):
    선택한 최상의 초안(형식적/실무용)을 이메일 매니저로 전달했습니다.
    
    선택된 초안(요약):
    - 제목: 서울의 현재 날씨 및 예보
    - 내용: 현재 기온 2.8도, 안개 및 흐림, 습도 93%, 바람 2.57 m/s, 안전 및 방한 주의 안내, 서명란 포함
    
    필요하면 수신자, 제목(별도), 또는 서명 정보를 바꿔 재전송해 드리겠습니다.
- 12 new item(s)
- 4 raw response(s)
- 1 input guardrail result(s)
- 1 output guardrail result(s)
(See `RunResult` for more details)
```

![output-guardrail1](/assets/it/ml/agents/output-guardrail1.png)


#### 두 번째 실행 결과 
이모지 아이콘이 포함된 두 번째 초안이 선택되었음에도 메일이 발송된 것을 확인했다.  
```txt
✅ 이메일 전송 성공!
Output guardrail result: RunResult:
- Last agent: Agent(name="Emoji check", ...)
- Final output (EmojiCheckOutput):
    {
      "is_emoji_in_message": false,
      "emoji": ""
    }
- 1 new item(s)
- 1 raw response(s)
- 0 input guardrail result(s)
- 0 output guardrail result(s)
(See `RunResult` for more details)
RunResult:
- Last agent: Agent(name="Email Manager", ...)
- Final output (str):
    Done — the chosen weather-cast draft (casual, friendly version) has been sent to the Email Manager.
- 18 new item(s)
- 5 raw response(s)
- 1 input guardrail result(s)
- 1 output guardrail result(s)
(See `RunResult` for more details)
```

![output-guardrail2](/assets/it/ml/agents/output-guardrail2.png)

그 이유는 여기서 찾을 수 있었다.  
```txt
- Final output (str):
    Done — the chosen weather-cast draft (casual, friendly version) has been sent to the Email Manager.
```
Email Manager 에게 전달된 것이 두 번째 초안이고, 그것을 send_email 도구로 처리한 후 나온 `Output: Done — the chosen weather-cast draft (casual, friendly version) has been sent to the Email Manager.` 에 대해서 output_guardrail 이 수행되는 것이다.  

따라서, output_guardrail 로는 이모지가 포함된 메일 발송을 막을 수 없다. Email Manager 의 input_guardrail 로서 수행이되어야 하는 것이다.  
그리고 output_guardrail 은 Email Manager 의 작업 후 출력되는 메세지에 대한 것임을 알게되었다. 

어떤 경우에 사용하는지 알게되었고, 사용방법을 알았으니 테스트를 마무리하는 것으로 한다.  

---

## 📚 References

[1] **Guardrail**  
- https://openai.github.io/openai-agents-python/guardrails/
