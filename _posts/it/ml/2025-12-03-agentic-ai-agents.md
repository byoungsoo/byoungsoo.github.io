---
layout: post
title: "Agentic Systems - Agents"
author: "Bys"
category: ml
date: 2025-12-03 01:00:00
keywords: "agentic ai"
tags: agentic ai workflow designpattern
---

# [Agentic System]  

## 1. Agents  
- Workflow 패턴과 반대로 Agents 프로세스는:
  1. Open-ended - 개방적
  2. Feedback loops - 여러 번 처리될 수 있음
  3. No fixed path - 일련의 단계까 없고 유동적이고 역동적임

![agents](/assets/it/ml/ai-agents/agents.png)

Agents 다이어그램에서는 더 이상 결과물이 나오지 않는다. 보다시피 사람, LLM, Environment(상호작용 할 수 있는 외부 세계를 반영하는 환경. Ex: 도구, 조명, 장비 등)이 존재한다.  
사람은 LLM 에게 어떤 요청을 할 수 있고, LLM 은 어떤 방식으로든 환경에서 정보를 다시 가져올 수 있으며 반복되는 루프를 수행할 수 있다. LLM은 계속해서 다른 작업을 수행하고 피드백을 받을 수 있으며, 원할 경우 중단을 선택할 수 있다. 


이 자체로 일종의 개방형 디자인 패턴이기 때문에 더 구체적인 디자인 패턴은 존재하지 않는다. 이 패턴을 되돌아보면, 이 패턴은 아마도 영원히 계속될 수도 있다. 고정된 정지가 있는 것은 아니지만 본질적으로 Agent 패턴은 더 유동적이고 유연하기 때문에 훨씬 더 복잡한 문제를 해겨할 수 있다.  

위 다이어그램으로는 단점으로는 작업을 완료하기 까지 시간이 얼마나 걸릴지 알 수 없고, 작업을 완료할지 알 수 없으며, 어떤 품질의 결과물이 나올지. 비용이 얼마나 나올지 등을 예측할 수 없는 새로운 문제들이 있다. 
- Risks of Agent Frameworks
  - Unpredictable path
  - Unpredictable output
  - Unpredictable costs
- To Mitigate risks
  - Monitor
  - Guardrails

Monitoring을 통해 모델과 에이전트의 상호 작용에 대해 가시성을 확보하고, Guardrail을 통해 모델이 해야 할 일을 하고 있는지, 또는 모델이 일부, 일부 제약 조건, 일부, 일부 레일을 남겨두지 않도록 하는 역할을 하게 한다.  







---

## 📚 References

[1] **Udemy - AI Engineer Agentic Track**
- https://www.udemy.com/course/the-complete-agentic-ai-engineering-course