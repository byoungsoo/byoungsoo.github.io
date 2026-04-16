---
slug: openai-agents-sdk
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
title: OpenAI Agents SDK
description: "OpenAI Agents SDK 핵심 구성요소 완벽 정리 - Agents, Handoffs, Guardrails, Tracing으로 자율형 AI 앱 개발하기"
---


# [OpenAI Agents SDK](https://openai.github.io/openai-agents-python/)  
OpenAI Agents SDK는 복잡한 개념이나 어려운 설정 없이 간단하고 직관적인 방식으로 자율형 AI 애플리케이션을 만들 수 있게 해주는 도구다. OpenAI Agents SDK 는 기본 구성요소로 아래와 같이 4가지를 가지고 있다.  
- `Agents`: which are LLMs equipped with instructions and tools
  - Agents 는 [LLM + 지시사항 + 도구]로 이해
    1. LLM (Large Language Model)
      - 자연어 이해 및 생성 능력
    2. Instructions (지시사항)
      - Agent의 역할과 목적을 정의
      - System Prompt와 유사한 개념
    3. Tools (도구)
      - Agent가 실제로 수행할 수 있는 기능
      - 함수, API 호출, 데이터베이스 쿼리 등

- `Handoffs`: which allow agents to delegate to other agents for specific tasks
  - Agent 간 협업을 의미
  - 여러 Agents가 존재할 때 (일반 상담원, 기술 전문가), 일반 상담원이 기술 전문가와 협업하는 행동을 Handoffs (Interaction)

- `Guardrails`: which enable validation of agent inputs and outputs
  - 입력/출력 검증 기능
  - 개인정보, 욕설 등에 대해 차단

- `Sessions`: which automatically maintains conversation history across agent runs
  - 대화 이력을 관리 
  - 이전 대화 이력을 관리하여 문맥이 유지될 수 있도록 함 


## [1. Agents](https://openai.github.io/openai-agents-python/agents/)  

Agent는 instructions 와 tools 가 설정된 LLM 이다.  
> An agent is a large language model (LLM), configured with instructions and tools.


```python
# The imports
from dotenv import load_dotenv
from agents import Agent, Runner, trace

# The usual starting point
load_dotenv(override=True)

@function_tool
def get_current_weather(city: str) -> str:
    """returns weather info for the specified city."""
    return f"The weather in {city} is sunny"

tools = [get_current_weather]

# Make an agent with name, instructions, model
agent = Agent(
  name="Jokestar", 
  instructions="You are a jokestar, you tell jokes", 
  model="gpt-5-nano"
  tools=tools
  )

# Run the joke with Runner.run(agent, prompt) then print final_output
with trace("Telling a joke"):
    result = await Runner.run(agent, "Tell a joke about Autonomous AI Agents")
    print(result.final_output)
```

위 코드는 agents 사용에 대한 기본 코드이다. Agents 는 가장 일반적으로 아래와 같은 속성들을 가지고 있다. 
- **name**: A required string that `identifies your agent`.
- **instructions**: also known as a `developer message` or `system prompt`.
- **model**: which `LLM to use`, and optional model_settings to configure model tuning parameters like temperature, top_p, etc.
- **tools**: Tools that the agent can use to achieve its tasks.


## 2. Multi-agent system design patterns  

다양한 multi-agent 에 대한 패턴이 있지만, 가장 흔히 사용되는 두 가지 패턴은 agent as tool, handoffs 두 가지 패턴이 있다.  

### 2.1. Agents as tools (Manager)
중앙 관리자(manager)가 특수화된 하위 에이전트를 도구로서 사용하고 대화의 통제권을 유지하고 있는 패턴.  
> A central manager/orchestrator invokes specialized sub‑agents as tools and retains control of the conversation.

```python
from agents import Agent

booking_agent = Agent(...)
refund_agent = Agent(...)

booking_agent_tool = booking_agent.as_tool(
            tool_name="booking_expert",
            tool_description="Handles booking questions and requests.",
        )
refund_agent_tool = refund_agent.as_tool(
            tool_name="refund_expert",
            tool_description="Handles refund questions and requests.",
        )

customer_facing_agent = Agent(
    name="Customer-facing agent",
    instructions=(
        "Handle all direct user communication. "
        "Call the relevant tools when specialized expertise is needed."
    ),
    tools=[booking_agent_tool, refund_agent_tool],
)
```
코드로 보면 customer_facing_agent 는 도구로서 booking_agent, refund_agent 를 가지고 있다. 다른 agent 를 도구로 활용하기 위해서는 `as_tool()` 을 통해 가능하다.  


### 2.2. Handoffs
동료 agent가 특수화된 agent에 대화의 제어권을 넘긴다. 이는 중앙집중식이 아닌 분산형이다.  
> Peer agents hand off control to a specialized agent that takes over the conversation. This is decentralized.

```python
from agents import Agent

booking_agent = Agent(...)
refund_agent = Agent(...)

triage_agent = Agent(
    name="Triage agent",
    instructions=(
        "Help the user with their questions. "
        "If they ask about booking, hand off to the booking agent. "
        "If they ask about refunds, hand off to the refund agent."
    ),
    handoffs=[booking_agent, refund_agent],
)
```
triage_agent 는 handoffs 속성을 통해 booking_agent, refund_agent 에게 제어권을 넘길 수 있다.


## 3. Agents 테스트  
아래 테스트는 3명의 기상 캐스터(agents)가 각기 다른 스타일로 날씨를 알려주는 것(e-mail)이고, 가장 괜찮은 e-mail 을 선정하여 나에게 보내는 것이다.  

```python
from dotenv import load_dotenv
from agents import Agent, Runner, trace, function_tool
from openai.types.responses import ResponseTextDeltaEvent
from typing import Dict
import sendgrid
import os
import asyncio
import requests
import json  # 추가!

load_dotenv(override=True)

@function_tool
def get_current_weather(location: str, unit: str = "celsius") -> str:
    """
    OpenWeatherMap API를 사용하여 특정 위치의 현재 날씨 정보를 가져옵니다.
    
    Args:
        location: 도시 이름 (예: Seoul, Tokyo)
        unit: 온도 단위 (celsius 또는 fahrenheit)
    
    Returns:
        JSON 형식의 날씨 정보 문자열
    """
    # OpenWeatherMap API 키 (환경변수에서 가져오기)
    api_key = os.getenv("OPENWEATHER_API_KEY")
    if not api_key:
        return json.dumps({"error": "OPENWEATHER_API_KEY not found"})
    
    # unit 값 정규화 (metric, celsius 모두 celsius로 처리)
    if unit.lower() in ["metric", "celsius"]:
        unit = "celsius"
    elif unit.lower() in ["imperial", "fahrenheit"]:
        unit = "fahrenheit"
    
    # 1. Geocoding API로 도시 이름을 위도/경도로 변환
    geo_url = f"http://api.openweathermap.org/geo/1.0/direct?q={location}&limit=1&appid={api_key}"
    
    try:
        geo_response = requests.get(geo_url, timeout=5)
        geo_response.raise_for_status()
        geo_data = geo_response.json()
        
        if not geo_data:
            return json.dumps({"error": f"도시를 찾을 수 없습니다: {location}"})
        
        lat = geo_data[0]['lat']
        lon = geo_data[0]['lon']
        
        # 2. Current Weather API 호출
        weather_url = f"https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={api_key}&units=metric"
        weather_response = requests.get(weather_url, timeout=5)
        
        weather_response.raise_for_status()
        data = weather_response.json()
        
        # 온도 변환
        temp_c = data['main']['temp']
        temp_f = (temp_c * 9/5) + 32
        temperature = temp_c if unit == "celsius" else temp_f
        
        weather_data = {
            "location": data['name'],
            "temperature": round(temperature, 1),
            "unit": unit,
            "condition": data['weather'][0]['description'],
            "humidity": data['main']['humidity'],
            "wind_speed": data['wind']['speed']
        }
        return json.dumps(weather_data)
        
    except requests.exceptions.RequestException as e:
        return json.dumps({"error": f"API 호출 실패: {str(e)}"})

@function_tool
def send_email(body: str):
    # Mailgun API 설정
    domain = "bys.digital"
    api_key = os.getenv('MAILGUN_API_KEY')
    if not api_key:
        print("❌ MAILGUN_API_KEY 환경 변수가 설정되지 않았습니다!")
        return None
    url = f"https://api.mailgun.net/v3/{domain}/messages"
    data = {
        "from": f"Mailgun Sandbox <postmaster@{domain}>",
        "to": "skwltg90@naver.com",  # 이메일만 (이름 제거)
        "subject": "Weather Cast Email",
        "text": body
    }
    
    try:
        response = requests.post(
            url,
            auth=("api", api_key),
            data=data,
            timeout=10
        )
        
        print(f"Status Code: {response.status_code}")
        print(f"Response: {response.text}")
        
        if response.status_code == 200:
            print("✅ 이메일 전송 성공!")
        else:
            print(f"❌ 이메일 전송 실패: {response.status_code}")
            print(f"에러 메시지: {response.text}")
        
        return response
        
    except Exception as e:
        print(f"❌ 에러 발생: {str(e)}")
        return None

basic_tools = [get_current_weather]


# Professional and Detailed Style
instructions1 = """
You are a professional meteorologist. Create weather forecast emails following these guidelines.

**Style:**
- Professional and formal tone
- Appropriate use of meteorological terminology
- Provide detailed data-driven analysis
"""

instructions2 = """
You are a friendly neighborhood weather caster. Create weather forecast emails following these guidelines.

**Style:**
- Casual tone as if talking to a friend
- Active use of emojis (☀️🌧️❄️⛅🌈 etc.)
- Explain in everyday language
- Humorous and entertaining
"""

instructions3 = """
You are an emotional storyteller weather caster. Create weather forecast emails following these guidelines.

**Style:**
- Literary and lyrical expressions
- Present weather as a narrative story
- Emotional and poetic descriptions
- Emphasize seasonal feelings and natural changes
"""


weather_cast_agent1 = Agent(
        name="Professional Weather Caster Agent",
        instructions=instructions1,
        model="gpt-4o-mini",
        tools=basic_tools
)

weather_cast_agent2 = Agent(
        name="Friendly Weather Caster Agent",
        instructions=instructions2,
        model="gpt-4o-mini",
        tools=basic_tools
)

weather_cast_agent3 = Agent(
        name="Storyteller Weather Caster Agent",
        instructions=instructions3,
        model="gpt-4o-mini",
        tools=basic_tools
)

senior_weather_cast_agent = Agent(
    name="Senior Weather Caster Agent",
    instructions="You pick the best weather cast email from the given options. \
Imagine you are a ordinary citizens living in Korea and pick the one you are most likely to respond to. \
Do not give an explanation; reply with the selected email only.",
    model="gpt-4o-mini"
)


async def main():
    message = "Write a weather cast email for 'Seoul' using Korean. When you need to know use 'get_current_weather' tool. Do not call 'get_current_weather' tool several times."
    
    print("=== 날씨 이메일 생성 시작 ===\n")
    try:
                    
        with trace("Parallel weather cast emails"):
            results = await asyncio.gather(
                Runner.run(weather_cast_agent1, message),
                Runner.run(weather_cast_agent2, message),
                Runner.run(weather_cast_agent3, message),
                return_exceptions=True  # 에러가 나도 다른 에이전트는 계속 실행
            )
            outputs = [result.final_output for result in results]

            emails = "Weather cast emails:\n\n" + "\n\nEmail:\n\n".join(outputs)
            best = await Runner.run(senior_weather_cast_agent, emails)

            print(f"Best weather cast email:\n{best.final_output}")


        print("=== 결과 ===\n")
        
        for i, result in enumerate(results, 1):
            if isinstance(result, Exception):
                print(f"Agent {i} 에러: {result}\n")
            else:
                print(f"Agent {i} 결과:")
                print(result.final_output)
                print("\n" + "="*50 + "\n")
                
    except Exception as e:
        print(f"전체 실행 에러: {e}")

# 실행
if __name__ == "__main__":
    asyncio.run(main())
```

이 코드를 보면 Senior 기상 캐스터가, 기상 캐스터 agent1, 2, 3 이 작성한 서울 현재 날씨의 이메일을 보고 가장 괜찮은 이메일을 선정하는 내용이다. 실행결과를 보면 다음과 같다.  

```txt
=== 날씨 이메일 생성 시작 ===

Best weather cast email:
Subject: 서울의 날씨 업데이트 🥳☀️

안녕하세요, 친구들! 

오늘 서울 날씨는 아주 맑아요! 그런 날에는 하늘을 바라보며 기분이 좋아지죠? 🌤️ 현재 기온은 1.8°C로 살짝 쌀쌀하니, 두꺼운 옷 하나 챙기세요! 

습도는 35%로 괜찮은 편인데, 바람도 살짝 불고 있어서 나가면 상쾌할 것 같아요. 하지만 바람이 불면... 헉! 🙈 머리카락이 날리니 조심하세요! 바람에 날아갈 수도 있습니다. 

오늘은 그런 날이에요! 산책하거나 카페에 들리기 좋은 날! ☕️✨ 그리고 뭐 구경도 가고, 사진도 찍고... #셀카타임 📸

그럼 다들 즐거운 하루 보내세요! 필요할 때마다 옷을 안으로 쪼글쪼글 말아 입지 말고, 따뜻한 차 한 잔 하면서 몸을 따뜻하게 해주세요. 🍵✨

종종 뵈요! 

행복한 하루 되세요! 
친구가 드림 💌



=== 결과 ===

Agent 1 결과:
제목: 서울 기상 예보

안녕하세요,

서울의 오늘 기상 예보를 전해드립니다.

현재 기온은 1.8도 섭씨로, 맑은 하늘이 이어지고 있습니다. 이러한 맑은 날씨는 기온 상승에 기여할 것으로 예상되며, 오후 시간대에는 온도가 조금 더 상승할 것으로 보입니다.

습도는 35%로 다소 낮은 편이며, 이는 대기 중의 수증기 함량이 적음을 나타냅니다. 바람은 시속 5.66킬로미터로, 기온 체감에 큰 영향을 미치지 않을 것으로 판단됩니다.

오늘 하루 외출 시, 따뜻한 옷차림을 권장드리며, 자외선 차단에도 유의하시기 바랍니다. 

향후 몇 일 동안의 날씨 패턴도 지속적으로 모니터링하며, 변화가 있을 경우 추가적인 정보를 제공해 드리겠습니다.

감사합니다.

[귀하의 이름]
[귀하의 직책]  
[귀하의 연락처]  
[귀하의 기관]

==================================================

Agent 2 결과:
Subject: 서울의 날씨 업데이트 🥳☀️

안녕하세요, 친구들! 

오늘 서울 날씨는 아주 맑아요! 그런 날에는 하늘을 바라보며 기분이 좋아지죠? 🌤️ 현재 기온은 1.8°C로 살짝 쌀쌀하니, 두꺼운 옷 하나 챙기세요! 

습도는 35%로 괜찮은 편인데, 바람도 살짝 불고 있어서 나가면 상쾌할 것 같아요. 하지만 바람이 불면... 헉! 🙈 머리카락이 날리니 조심하세요! 바람에 날아갈 수도 있습니다. 

오늘은 그런 날이에요! 산책하거나 카페에 들리기 좋은 날! ☕️✨ 그리고 뭐 구경도 가고, 사진도 찍고... #셀카타임 📸

그럼 다들 즐거운 하루 보내세요! 필요할 때마다 옷을 안으로 쪼글쪼글 말아 입지 말고, 따뜻한 차 한 잔 하면서 몸을 따뜻하게 해주세요. 🍵✨

종종 뵈요! 

행복한 하루 되세요! 
친구가 드림 💌

==================================================

Agent 3 결과:
Subject: 🌅 오늘의 서울 날씨 이야기 🌅

안녕하세요, 날씨의 시인을 찾아주신 여러분!

오늘 서울은 하늘이 맑고 찬란한 푸른색으로 물들어 있습니다. 기온은 1.8도, 겨울의 터치를 느끼며 공중에서는 차가운 바람이 살짝 스치고 지나갑니다. 이는 마치 하늘의 요정들이 살며시 숨결을 불어넣는 듯한 느낌을 줍니다.

이런 날씨는 우리의 마음속에 겨울의 정수를 각인시킵니다. 땅의 숨결이 얼어붙고, 그 속에 감춰진 생명의 소리마저도 잠시 묻혀버린 것처럼 느껴집니다. 나무들은 고요히 자신의 갈등을 덮어두고, 하늘은 투명함으로 그 모든 것을 감싸 안습니다.

오늘은 서쪽에서 살짝 불어오는 바람과 함께, 떠오르는 햇살이 기온을 조금 더 온화하게 만들어 줄지도 모릅니다. 5.66m/s의 바람은 마치 따뜻한 친구처럼 우리 곁을 감싸줍니다. 

이렇게 맑은 날씨 속에서 길을 걷노라면, 하늘과 땅이 하나 되어 우리의 마음속 깊은 곳에서 환희가 피어납니다. 함께하는 친구와 뜨거운 차 한잔의 여유를 즐기며, 오늘의 소중한 순간들을 만끽하시기 바랍니다.

여러분의 하루가 따뜻하고 행복으로 가득 차길 바랍니다!

사랑과 평화를 담아,
[당신의 이름]

==================================================
```

agent1 의 전문가적인 메일이 베스트 메일로 뽑혔다. 이 부분에서 우리는 기상 예측을 시도한 agent 3명이 존재하고, 가장 잘 쓰여진 이메일을 선정하는 senior 기상 캐스터가 있는 것을 볼 수 있었다. 


![agent_pattern_trace1](/assets/it/ml/agents/agent_pattern_trace1.png)

OpenAI 의 trace 에서는 이 호출이력을 확인할 수 있는데, 동시적으로 3개의 기상 캐스터 에이전트가 호출된것을 볼 수 있으며 가장 늦게 끝난 Professional Weather Caster Agent 이 후, Senior Weather Caster Agent 가 하나의 초안을 선택하는 과정을 로깅한 것이다. [Trace ](https://platform.openai.com/traces)에서 확인할 수 있다.  

다만, 이것을 이전에 소개한 Agent as tool, Handoffs 패턴으로 조금 변경해보겠다.  


## 5. [Agent as tool Design Pattern](https://openai.github.io/openai-agents-python/tools/#agents-as-tools)  
> The manager_agent handles all user interaction and invokes specialized sub‑agents exposed as tools. Read more in the tools documentation.

Agent를 Manager 에게 도구로서 활용하게 하는 방식으로 디자인 패턴을 일부 변경해보겠다. 그러면 코드가 아래와 같이 된다.  

```python
description = "Write a weather cast email for 'Seoul' using Korean. When you need to know weather, use 'get_current_weather' tool. Do not call 'get_current_weather' tool several times."
weather_tool1 = weather_cast_agent1.as_tool(tool_name="weather_cast_agent1", tool_description=description)
weather_tool2 = weather_cast_agent2.as_tool(tool_name="weather_cast_agent2", tool_description=description)
weather_tool3 = weather_cast_agent3.as_tool(tool_name="weather_cast_agent3", tool_description=description)

tools = [weather_tool1, weather_tool2, weather_tool3, send_email]


instructions = """
You are a senior weather caster. Your goal is to find the single best weather cast email using the weather_cast_agent tools.
You pick the best weather cast email from the given options. \
Imagine you are a ordinary citizens living in Korea and pick the one you are most likely to respond to. \
Do not give an explanation; reply with the selected email only.

Follow these steps carefully:
1. Generate Drafts: Use all three weather_cast_agent tools to generate three different email drafts. Do not proceed until all three drafts are ready.
 
2. Evaluate and Select: Review the drafts and choose the single best email using your judgment of which one is most effective.
 
3. Use the send_email tool to send the best email (and only the best email) to the user.
 
Crucial Rules:
- You must use the sales weather_cast_agent tools to generate the drafts — do not write them yourself.
- You must send ONE email using the send_email tool — never more than one.
"""

senior_weather_cast_manager = Agent(name="Senior Weather Cast Manager", instructions=instructions, tools=tools, model="gpt-4o-mini")

async def main():
    
    print("=== 날씨 이메일 생성 시작 ===\n")
    try:        
        message = "Send a weather cast email addressed to 'Dear Seoul Citizen'"
        
        with trace("Senior Weather Cast Manager"):
            result = await Runner.run(senior_weather_cast_manager, message)
        
        print(result)
                
    except Exception as e:
        print(f"전체 실행 에러: {e}")

# 실행
if __name__ == "__main__":
    asyncio.run(main())
```

1. 각각의 weather_cast_agent 들에 대해 as_tool을 통해 도구로 전환하고
2. 상위 기상 캐스터 매니저 agent 에게 도구로서 이들을 전달한다. 

그렇게 되면 상위 매니저는 각 하위 기상캐스트 도구들을 이용해 e-mail 템플릿을 작성하고, 메일을 직접 보내게 될 것이다. 다만, 테스트 중 여러가지 문제점들이 확인되었다.  
**상위 매니저에서 날씨를 확인하기 위해 하위 에이전트 들을 호출하지만, 하위 에이전트들이 get_current_weather 툴을 사용하지 않고, 멋대로 메일에 날씨를 작성하는 문제, 메일을 수차례 보내는 문제, weather_cast_agent 를 수십차례 호출하는 문제등.** 이 발생했다. 해당 내용들에 대해서는 아래 트러블 슈팅섹션에서 작성하였고, 대략적으로 코드를 아래와 같이 수정하였다. (정확히는 지시사항 들의 문제)

```python
# Professional and Detailed Style
instructions1 = """
You are a professional meteorologist. Create weather forecast emails following these guidelines.

**CRITICAL: You MUST use the get_current_weather tool to fetch real-time weather data for Seoul before writing the email. You must write in Korean**

**Style:**
- Professional and formal tone
- Appropriate use of meteorological terminology
- Provide detailed data-driven analysis
"""

instructions2 = """
You are a friendly neighborhood weather caster. Create weather forecast emails following these guidelines.

**CRITICAL: You MUST use the get_current_weather tool to fetch real-time weather data for Seoul before writing the email. You must write in Korean**

**Style:**
- Casual tone as if talking to a friend
- Active use of emojis (☀️🌧️❄️⛅🌈 etc.)
- Explain in everyday language
- Humorous and entertaining
"""

instructions3 = """
You are an emotional storyteller weather caster. Create weather forecast emails following these guidelines.

**CRITICAL: You MUST use the get_current_weather tool to fetch real-time weather data for Seoul before writing the email. You must write in Korean**

**Style:**
- Literary and lyrical expressions
- Present weather as a narrative story
- Emotional and poetic descriptions
- Emphasize seasonal feelings and natural changes
"""


weather_cast_agent1 = Agent(
        name="Professional Weather Caster Agent",
        instructions=instructions1,
        model="gpt-4o-mini",
        tools=basic_tools
)

weather_cast_agent2 = Agent(
        name="Friendly Weather Caster Agent",
        instructions=instructions2,
        model="gpt-4o-mini",
        tools=basic_tools
)

weather_cast_agent3 = Agent(
        name="Storyteller Weather Caster Agent",
        instructions=instructions3,
        model="gpt-4o-mini",
        tools=basic_tools
)

description = "Write current weawther of city e-mail"
# description = "Write a weather cast email for 'Seoul'. You must use 'get_current_weather' tool to know current weather."
weather_tool1 = weather_cast_agent1.as_tool(tool_name="weather_cast_agent1", tool_description=description)
weather_tool2 = weather_cast_agent2.as_tool(tool_name="weather_cast_agent2", tool_description=description)
weather_tool3 = weather_cast_agent3.as_tool(tool_name="weather_cast_agent3", tool_description=description)

tools = [weather_tool1, weather_tool2, weather_tool3, send_email]


instructions = """
You are a senior weather caster manager. Your goal is to find the single best weather cast email using the weather_cast_agent tools. You pick the best weather cast email from the given options. Imagine you are a ordinary citizens living in Korea and pick the one you are most likely to respond to. Do not give an explanation; reply with the selected email only.

**CRITICAL: You must use the sales weather_cast_agent tools to generate the drafts — do not write them yourself - do not call weather_cast_agent more than one. You must send ONE most effective email using the send_email tool — never more than one.**

Follow these steps IN ORDER:
1. Generate Drafts: Use all three weather_cast_agent tools to generate three different email drafts. Do not proceed until all three drafts are ready.
2. Evaluate and Select: Review the drafts and choose the single best email using your judgment of which one is most effective.
3. Use the send_email tool to send the best email (and only one the best email) to the user.
"""

senior_weather_cast_manager = Agent(name="Senior Weather Cast Manager", instructions=instructions, model="gpt-4o-mini", tools=tools)


async def main():
    
    
    print("=== 날씨 이메일 생성 시작 ===\n")
    try:        
        message = "Send a weather cast email addressed to 'Dear Seoul Citizen'"
        with trace("Senior Weather Cast Manager"):
            result = await Runner.run(senior_weather_cast_manager, message)
        
        print(result)
                
    except Exception as e:
        print(f"전체 실행 에러: {e}")

# 실행
if __name__ == "__main__":
    asyncio.run(main())
```
**CRITICAL** 을 지시사항에 포함하여 조금 더 구체적이고 명시적으로 설명을 추가했다.  최종적으로 코드를 실행하면 아래와 같이 메일을 받아볼 수 있다.  

![weather-email](/assets/it/ml/agents/weather-email.png)

현재 서울 날씨를 정확히 조회하여 메일이 작성된 초안 3개중 가장 베스트인 메일을 보내줬다. 


Trace 로그를 보면 아래와 같다.  

![manager_pattern_trace1](/assets/it/ml/agents/manager_pattern_trace1.png)

**모든 대화의 주도권이 `Senior Weather Cast Manager` 에게 있는 것을 볼 수 있으며 매니저가 weather_cast_agent 도구들을 호출하여 각 메일의 초안을 받아오는 것을 알 수 있다.**  

![manager_pattern_trace2](/assets/it/ml/agents/manager_pattern_trace2.png)

이 후 메일을 보내는 과정에서 하나의 초안이 전달된 것을 볼 수 있으며 최종적으로 내가 수신한 메일이다.  


## 6. [Handoffs Design Pattern](https://openai.github.io/openai-agents-python/handoffs/)  
> Handoffs allow an agent to delegate tasks to another agent. This is particularly useful in scenarios where different agents specialize in distinct areas.

Handoffs 에서는 하나의 agent가 다른 특별한 역할을 가진 agent 에게 임무를 위임하는 것이다. 대화의 주도권이 이전 매니저 패턴과 다르게 다른 agent로 옮겨가게 된다. 이전의 코드를 Handoffs 패턴으로 변경해보겠다.  

시나리오: 매니저는 각각의 기상캐스터 에이전트는 모두 영어로 된 이메일 초안을 작성한다.(도구). 이 후 Email Manager 와 Handoffs 를 통해 이메일을 보내는 역할을 에이전트에게 위임하는 시나리오다. 

```python
sender_instruction="You are an e-mail sender. You can use send_email tool to send an e-mail."
send_email_agent = Agent(
    name="Email Manager",
    instructions=sender_instruction,
    model="gpt-5-mini",
    tools=[send_email],
    handoff_description="Send an e-mail")


tools = [weather_tool1, weather_tool2, weather_tool3]
handoffs = [send_email_agent]

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
    handoffs = handoffs)
```
Email Manager(Agent)를 새로 만들고, senior_weather_cast_manager 에게 Handoffs 로 넘겨준다. tools 와 다르게 handoffs 는 상호작용시 대화의 흐름이 상대방에게 넘어간다는 부분이 있다.

![handoff_pattern_trace3](/assets/it/ml/agents/handoff_pattern_trace3.png)
코드를 수행하면 위 와 같이 Trace 를 볼 수 있다.  


![handoff_pattern_email](/assets/it/ml/agents/handoff_pattern_email.png)
최종적으로 메일을 수신한 결과다.  


## 7.Troubleshooting

### 1. Agent as tool 패턴에서 하위 에이전트들이 tools 를 사용하지 않는 문제
tools_description 은 매니저가 하위 에이전트들을 어떻게 사용해야하는지에 대한 설명이기 때문에 날씨확인 및 메일작성을 위해 하위 에이전트들은 모두 호출을 했지만, 하위 에이전트들이 get_current_weather 도구를 사용해야 하는지에 대해서는 각 agent 의 instructions에 포함이 되어야 한다.  따라서, agent 지시사항에 명시적으로 도구사용할 것으로 수정한다.  
```txt
# Professional and Detailed Style
instructions1 = """
......
**CRITICAL: You MUST use the get_current_weather tool to fetch real-time weather data for Seoul before writing the email. You must write in Korean**
......
"""

instructions2 = """
......
**CRITICAL: You MUST use the get_current_weather tool to fetch real-time weather data for Seoul before writing the email. You must write in Korean**
......
"""

instructions3 = """
You are an emotional storyteller weather caster. Create weather forecast emails following these guidelines.
......
**CRITICAL: You MUST use the get_current_weather tool to fetch real-time weather data for Seoul before writing the email. You must write in Korean**
......
"""
```

### 2. Agent as tool 패턴에서 매니저가 에이전트에게 받은 메일 3가지를 모두 메일로 보낸 문제 및 weather_cast_agent를 18번 호출한 문제
- **CRITICAL** 을 통해 지시사항에 명시적으로 작성해 봄. 
- do not call weather_cast_agent more than one

```
instructions = """
You are a senior weather caster manager. Your goal is to find the single best weather cast email using the weather_cast_agent tools. You pick the best weather cast email from the given options. Imagine you are a ordinary citizens living in Korea and pick the one you are most likely to respond to. Do not give an explanation; reply with the selected email only.

**CRITICAL: You must use the sales weather_cast_agent tools to generate the drafts — do not write them yourself - do not call weather_cast_agent more than one. You must send ONE most effective email using the send_email tool — never more than one.**

Follow these steps IN ORDER:
1. Generate Drafts: Use all three weather_cast_agent tools to generate three different email drafts. Do not proceed until all three drafts are ready.
2. Evaluate and Select: Review the drafts and choose the single best email using your judgment of which one is most effective.
3. Use the send_email tool to send the best email (and only one the best email) to the user.
"""
```

### 3. Handoff 패턴에서 흐름제어가 되지 않는 문제

- 원하던 구성
Senior Weather Cast Manager -> tool_calling(weather_cast_agent1, weather_cast_agent2, weather_cast_agent3) -> 가장 괜찮은 이메일 초안 선택(Senior Weather Cast Manager) -> Translator agent 와 **Handoff** -> Senior Weather Cast Manager 가 주도권을 다시 가져와 한국어로 번역되었는지 리뷰 -> Email Sender agent 와 **Handoff** 

- 실제 결과
  1. Senior Weather Cast Manager -> tool_calling(weather_cast_agent1, weather_cast_agent2, weather_cast_agent3) -> Translator agent 에 동시에 Multiple Handoff 요청하여 오류 
  2. Senior Weather Cast Manager -> tool_calling(weather_cast_agent1, weather_cast_agent2, weather_cast_agent3) -> Translator agent, Send Email Agent 동시 Handoff
  3. Senior Weather Cast Manager -> tool_calling(weather_cast_agent1, weather_cast_agent2, weather_cast_agent3) 수차례 호출 후 -> Translator agent Multiple Handoff
등.....


고민을 해보니 Translator agent 로 Handoff 를 통해 주도권이 넘어가고나니 'Senior Weather Cast Manager' 에게 돌아올 수 있는 방법이 없었다. 그래서 senior_weather_cast_manager 호출되기 전에 각각의 에이전트에게 senior_weather_cast_manager Handoff 를 알려주었다.  
```python
translator_agent.handoffs = [senior_weather_cast_manager]
send_email_agent.handoffs = [senior_weather_cast_manager]
```

```python
weather_cast_agent1 = Agent(
        name="Professional Weather Caster Agent",
        instructions=instructions1,
        model="gpt-4o-mini",
        tools=basic_tools
)

weather_cast_agent2 = Agent(
        name="Friendly Weather Caster Agent",
        instructions=instructions2,
        model="gpt-4o-mini",
        tools=basic_tools
)

weather_cast_agent3 = Agent(
        name="Storyteller Weather Caster Agent",
        instructions=instructions3,
        model="gpt-4o-mini",
        tools=basic_tools
)

description = "Write current weawther of city e-mail"
# description = "Write a weather cast email for 'Seoul'. You must use 'get_current_weather' tool to know current weather."
weather_tool1 = weather_cast_agent1.as_tool(tool_name="weather_cast_agent1", tool_description=description)
weather_tool2 = weather_cast_agent2.as_tool(tool_name="weather_cast_agent2", tool_description=description)
weather_tool3 = weather_cast_agent3.as_tool(tool_name="weather_cast_agent3", tool_description=description)

translator_instruction = """
You are a professional translator. Translate the given text to Korean while maintaining the original style and formatting.
Return ONLY the translated text, no explanations. 
**CRITICAL: After you translate an e-mail, you must hand off the manager(senior_weather_cast_manager) to report result and to continue next step.** - do NOT stop here.**
"""
translator_agent = Agent(
    name="Translator Agent",
    instructions=translator_instruction,
    model="gpt-4o-mini",
    handoff_description="Translate to Korean"
    )


sender_instruction="You are an e-mail sender. You can use send_email tool to send an e-mail."
send_email_agent = Agent(
    name="Send Email Agent",
    instructions=sender_instruction,
    model="gpt-4o-mini",
    tools=[send_email],
    handoff_description="Send an e-mail")


tools = [weather_tool1, weather_tool2, weather_tool3]
handoffs = [translator_agent, send_email_agent]

manager_instruction = """
You are a senior weather caster manager. Your goal is to find the single best weather cast email using weather_cast_agent tools. You pick the best weather cast email from the given options. Imagine you are a ordinary citizens living in Korea and pick the one you are most likely to respond to. Do not give an explanation; reply with the selected email only.

**Must follow the ORDER: 
1. You must use the sales weather_cast_agent tools to generate the drafts — do not write them yourself - do not call each weather_cast_agent tool more than one. 
2. Evaluate and Select: Review the drafts and choose the 'ONLY ONE' single best email using your judgment of which one is most effective.
3. Handoff for translate: Pass the one(single best email choosed in step 2) e-mail draft to the 'Translator Agent'. Translator Agent will take care translating **Wait for translated result**
4. Review: You must check the translated e-mail whether it is Korean or not.
5. Handoff for sending e-mail: Pass translated e-mail to the 'Send Email Agent'. 'Send Email Agent' will send e-mail.
**
"""

senior_weather_cast_manager = Agent(name="Senior Weather Cast Manager", instructions=manager_instruction, model="gpt-4o-mini", tools=tools, handoffs = handoffs)

translator_agent.handoffs = [senior_weather_cast_manager]
send_email_agent.handoffs = [senior_weather_cast_manager]

async def main():
    
    print("=== 날씨 이메일 생성 시작 ===\n")
    try:        
        message = "Send a weather cast email addressed to 'Dear Seoul Citizen'"
        with trace("Senior Weather Cast Manager"):
            result = await Runner.run(senior_weather_cast_manager, message)
        
        print(result)
                
    except Exception as e:
        print(f"전체 실행 에러: {e}")

# 실행
if __name__ == "__main__":
    asyncio.run(main())
```

다만 이렇게 수정한 후에도 아래와 같이 오류가 발생했다.  
![handoff_pattern_trace1](/assets/it/ml/agents/handoff_pattern_trace1.png)


따라서, 제어의 흐름을 아래와 같이 변경하기로 정하였다.  
- 변경한 흐름 구성
Senior Weather Cast Manager -> tool_calling(weather_cast_agent1, weather_cast_agent2, weather_cast_agent3) -> 가장 괜찮은 이메일 초안 선택(Senior Weather Cast Manager) -> Translator agent 와 **Handoff** -> Email Sender agent 와 **Handoff** 

```python
description = "Write current weawther of city e-mail"
# description = "Write a weather cast email for 'Seoul'. You must use 'get_current_weather' tool to know current weather."
weather_tool1 = weather_cast_agent1.as_tool(tool_name="weather_cast_agent1", tool_description=description)
weather_tool2 = weather_cast_agent2.as_tool(tool_name="weather_cast_agent2", tool_description=description)
weather_tool3 = weather_cast_agent3.as_tool(tool_name="weather_cast_agent3", tool_description=description)

translator_instruction = """
You are a professional translator. Translate the given text to Korean while maintaining the original style and formatting.
Return ONLY the translated text, no explanations. 
**CRITICAL: After you translate an e-mail, you must hand off the Send Email Agent to send an e-mail.** - do NOT stop here.**
"""
translator_agent = Agent(
    name="Translator Agent",
    instructions=translator_instruction,
    model="gpt-4o-mini",
    handoff_description="Translate to Korean"
    )

sender_instruction="You are an e-mail sender. You can use send_email tool to send an e-mail."
send_email_agent = Agent(
    name="Send Email Agent",
    instructions=sender_instruction,
    model="gpt-4o-mini",
    tools=[send_email],
    handoff_description="Send an e-mail")


tools = [weather_tool1, weather_tool2, weather_tool3]
handoffs = [translator_agent]
translator_agent.handoffs = [send_email_agent]

manager_instruction = """
You are a senior weather caster manager. Your goal is to find the single best weather cast email using weather_cast_agent tools. You pick the best weather cast email from the given options. Imagine you are a ordinary citizens living in Korea and pick the one you are most likely to respond to. Do not give an explanation; reply with the selected email only.

**Must follow the ORDER: 
1. You must use the sales weather_cast_agent tools to generate the drafts — do not write them yourself - do not call each weather_cast_agent tool more than one. 
2. Evaluate and Select: Review the drafts and choose the 'ONLY ONE' single best email using your judgment of which one is most effective.
3. Handoff for translate: Pass the one(single best email choosed in step 2) e-mail draft to the 'Translator Agent'. Translator Agent will take care translating **Wait for translated result**
**
"""

senior_weather_cast_manager = Agent(name="Senior Weather Cast Manager", instructions=manager_instruction, model="gpt-4o-mini", tools=tools, handoffs = handoffs)

......
```
코드를 보면 `translator_agent.handoffs = [send_email_agent]` translator_agent 에 send_email_agent 를 handoff 로 등록하였고, 매니저에게서 제거했다.  

이렇게 수정한 후에는 아래와 같이 흐름제어가 잘 되는 것을 확인할 수 있었지만...Translator 가 제대로 동작하지 않았다...

![handoff_pattern_trace2](/assets/it/ml/agents/handoff_pattern_trace2.png)

이런저런 수정을 해보았지만, instructions 를 수정하는 것 만으로 정상동작이 안되었다. (번역이 안된다던가, 메일이 안 보내진다던가) 각 에이전트들간 Handoff 시 조금 더 세밀하게 다뤄저야 할 것 같은데 이 부분은 추후에 다시 확인을 해봐야할 필요가 있다.  


---

## 📚 References

[1] **Agents**  
- https://openai.github.io/openai-agents-python/agents/

[2] **Tools**  
- https://openai.github.io/openai-agents-python/tools/

[3] **Handoffs**  
- https://openai.github.io/openai-agents-python/handoffs/

[4] **Tracing**
- https://openai.github.io/openai-agents-python/tracing/