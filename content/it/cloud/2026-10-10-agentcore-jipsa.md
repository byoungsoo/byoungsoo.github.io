---
slug: agentcore
author: Bys
categories:
- cloud
date: '2026-10-10 01:00:00'
keywords: aws bedrock agentcore agent
tags:
- aws
- bedrock
- agentcore
- agent
title: Amazon Bedrock AgentCore로 부동산 에이전트 운영하기
draft: true
---

여기서는 부동산 에이전트를 개발하여 사용하는 것을 최종 목적으로 한다. 부동산 에이전트는 AWS Bedrock AgentCore 서비스에 올릴 예정이다. AgentCore의 다양한 코어 서비스를 이용하여 부동산 세법, 최근실거래 등을 모두 조사해주는 에이전트를 만들어 보도록 한다. 

# [Amazon Bedrock AgentCore](https://docs.aws.amazon.com/bedrock-agentcore/latest/devguide/what-is-bedrock-agentcore.html)  
> Amazon Bedrock AgentCore는 프레임워크 및 파운데이션 모델을 사용하여 매우 효과적인 에이전트를 대규모로 안전하게 구축, 배포 및 운영하기 위한 에이전트 플랫폼입니다. AgentCore를 사용하면 에이전트가 인프라 관리 없이도 적절한 권한과 거버넌스로 도구 및 데이터 전반에 걸쳐 작업을 수행하고, 에이전트를 대규모로 안전하게 실행하고, 프로덕션 환경에서 에이전트의 성능과 품질을 모니터링할 수 있습니다. AgentCore 서비스는 CrewAI, LangGraph, LlamaIndex, Strands Agents와 같은 오픈 소스 프레임워크 및 파운데이션 모델과 함께 또는 독립적으로 작동하므로 오픈 소스 유연성과 엔터프라이즈급 보안 및 안정성 중에서 선택할 필요가 없습니다.


## 1. [Architecture]()  

## 2. Agent 코드 개발
에이전트 코드와 도구 등을 개발. 여기서 코드는 중요하지 않다. 핵심이 되는 샘플 코드만 여기선 확인해보도록 한다. 
메인 코드(agent)에서는 결국 agent 를 만들어 호출하는 코드가 된다.  

`main`  
```python
from bedrock_agentcore.runtime import BedrockAgentCoreApp
from strands import Agent
from strands.models import BedrockModel

from config import get_settings
from prompts import load_prompt
from tools import get_apartment_trades, search_apartments

settings = get_settings()
app = BedrockAgentCoreApp()

# 모듈 수준에서 한 번만 만든다. Runtime은 세션마다 microVM을 따로 띄우므로
# 같은 세션 ID로 다시 호출하면 이 객체의 대화 기록이 이어진다.
agent = Agent(
    model=BedrockModel(model_id=settings.model_id, region_name=settings.region),
    system_prompt=load_prompt("system"),
    tools=[search_apartments, get_apartment_trades],
)


@app.entrypoint
def invoke(payload):
    prompt = payload.get("prompt", "")
    result = agent(prompt)
    return {"result": str(result)}


if __name__ == "__main__":
    app.run()
```

`tool`  
```python
@tool
def search_apartments(district: str, keyword: str) -> dict:
    """서울 구 안에서 단지명으로 아파트 단지 후보를 찾는다.

    국토부 단지명은 부르는 이름과 다를 수 있다. 예: '이문쌍용'은 '쌍용'(이문동),
    '강동롯데캐슬퍼스트'는 '롯데캐슬퍼스트'(암사동)로 등록되어 있다.
    특정 단지의 시세를 묻는 질문이면 get_apartment_trades 전에 반드시 이 도구로 단지를 확정한다.

    Args:
        district: 서울의 구 이름. 예: "동대문구"
        keyword: 사용자가 말한 단지명. 예: "이문쌍용", "강동롯데캐슬퍼스트"
    """
......

@tool
def get_apartment_trades(district: str, deal_ymd: str, apt_name: str = "", jibun: str = "") -> dict:
    """서울 구 단위 아파트 매매 실거래가를 조회한다.

    특정 단지만 보려면 search_apartments 결과의 apt_name과 jibun을 그대로 넘긴다.
    단지명은 완전히 일치하는 거래만 남기므로, 사용자가 말한 이름을 그대로 넣으면 안 된다.

    Args:
        district: 서울의 구 이름. 예: "강남구", "마포구"
        deal_ymd: 계약년월 6자리. 예: "202409"
        apt_name: search_apartments가 돌려준 국토부 단지명. 비우면 구 전체를 조회한다.
        jibun: search_apartments가 돌려준 지번. 같은 이름의 다른 단지를 구분한다.
    """
......
```

`system 프롬프트`  
```bash
당신은 '집사'라는 이름의 개인 부동산 전문가입니다.

## 시세 조회

- 시세 질문에는 반드시 get_apartment_trades 도구로 국토교통부 실거래가를 조회한 뒤 답합니다.
- 답변에는 조회한 지역, 계약년월, 거래 건수와 함께 근거가 된 거래(단지명, 전용면적, 층, 거래금액)를 보여 줍니다.
- 거래금액 단위는 만원입니다. 사용자에게는 '억 원' 단위로 읽기 쉽게 바꿔 보여 줍니다.
- 해제(취소)된 거래는 도구가 이미 뺐습니다. 해제 거래가 섞였을 수 있다고 안내하지 않습니다.

## 단지 확정

- 특정 단지를 물으면 먼저 search_apartments로 단지를 확정합니다. 사용자가 말한 이름을 get_apartment_trades에 그대로 넣지 않습니다.
- resolved가 true이면 그 후보의 apt_name과 jibun으로 get_apartment_trades를 호출합니다.
- 후보가 여러 개이거나 match_type이 partial이면 추측하지 말고 조회를 멈춥니다. 후보를 단지명, 법정동, 지번, 준공 연도로 보여 주고 사용자에게 고르게 합니다.
- '쌍용아파트', '롯데캐슬'처럼 이름이 흔하면 '이문쌍용', '강동롯데캐슬퍼스트'처럼 동네나 지역명을 붙인 정확한 단지명을 알려 달라고 요청합니다.
- 국토부 등록 단지명이 사용자가 부른 이름과 다르면 둘 다 보여 줍니다. 예: "이문쌍용(국토부 등록명: 쌍용, 이문동 64)"
- 후보가 없으면 검색 기간에 거래가 없었거나 이름이 다를 수 있다고 알리고, 다른 이름이나 법정동을 물어봅니다.

## 공통

- 출처는 '국토교통부 아파트 매매 실거래가 자료(공공데이터포털)'로 밝힙니다.
- 조회 결과가 없거나 지원하지 않는 지역이면 추측하지 말고 그렇다고 말합니다.
- 투자 판단은 참고용이며 확정 판단은 전문가 확인이 필요하다고 안내합니다.
- 한국어로 답합니다.
```

해당 코드들을 package로 만들어 .zip 파일로 묶는다.  

## 3. [AgentCore Runtime](https://docs.aws.amazon.com/bedrock-agentcore/latest/devguide/agents-tools-runtime.html)  
AgentCore 서비스에서 런타임 환경을 만든다. 런타임 환경에서는 컴퓨트 타입(microVMs, Instnaces)를 선택하고, 에이전트 소스를 어디서(S3, ECR) 가져올지 선택한다. 런타임을 만들면 DEFAULT 엔드포인트가 하나 생성되게 되고 엔드포인트로 요청되는 각 사용자 세션은 격리된 microVM에서 실행되게 된다. 실행환경을 EC2 로 선택할경우 지속적인 세션을 제공할 수 있게되고, GPU 워크로드도 실행가능하다.  



## 4. []()  

## 5. []()  


---

## 📚 References

[1] **제목** - 설명  
- https://example.com