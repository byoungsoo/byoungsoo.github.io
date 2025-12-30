---
layout: post
title: "Agentic Systems - 5 workflow design patterns"
author: "Bys"
category: ml
date: 2025-12-02 01:00:00
keywords: "agentic ai"
tags: agentic ai workflow designpattern
---

# [Agentic System]  

## 1. Agents  
- Workflows are systems where LLMs and tools are orchestrated through predefined code paths. 
- AI Agent가 특정 목표를 달성하기 위해 LLM의 '지능'과 외부 tool 의 '실행 능력'을 결합하되, 이 모든 과정이 개발자에 의해 미리 코드로 설계된 정해진 절차와 규칙에 따라 체계적으로 조정(오케스트레이션)되는 시스템을 Workflow 라고 한다. Workflow는 큰 그림이다. 어떤 복잡한 문제를 해결하기 위한 전체적인 접근 방식, 구조, 그리고 단계들의 연결을 의미한다. AI Agent가 목표를 달성하기 위해 수행하는 일련의 자율적인 '행동 프로세스'로 이해할 수 있다.  



#### - Five workflow design patterns

#### 1. PROMPT CHAINING
![prompt-chaining](/assets/it/ml/ai-agents/prompt-chaining.png)
- Decompose into fixed sub-tasks
- 하나의 복잡한 작업을 여러 개의 작고 관리 가능한 하위 작업으로 분해하고, 각 하위 작업의 결과(출력)를 다음 하위 작업의 입력(프롬프트)으로 연속적으로 연결하여 최종 목표를 달성하는 기법을 의미한다.  

- 단일 프롬프트의 경우 매우 복잡한 질문이나 작업을 한 번에 처리하도록 요구하면 LLM이 혼란스러워하거나 정확도가 떨어질 수 있고, 또는 중간 단계를 건너뛰고 바로 최종 결과를 내놓으려고 시도할 수 있다. 
- 긴 프롬프트의 비용 및 성능 저하. 너무 긴 프롬프트는 토큰 비용을 증가시키고, 모델의 성능을 저하시킬 수 있다.   

Prompt chaining 은 AI Agent가 복잡한 작업을 수행하는 핵심 메커니즘 중 하나이며 Agent는 목표를 달성하기 위해 **계획 (Planning)**을 세우는데, 이 계획의 각 단계는 종종 프롬프트 체이닝으로 구현된다. Agent는 현재 상태를 평가하고 다음 행동을 결정하기 위해 LLM을 호출하고, 그 결과를 바탕으로 다음 프롬프트를 동적으로 생성하며 체인을 이어나간다.  

LangChain, CrewAI, AutoGen과 같은 AI Agent 프레임워크들은 이러한 프롬프트 체이닝을 쉽게 구현할 수 있도록 다양한 모듈과 추상화 계층을 제공함.



#### 2. ROUTING
![routing](/assets/it/ml/ai-agents/routing.png)
- Direct an input into a specialized sub-task, ensuring separation of concerns.
- 들어오는 요청, 작업 또는 정보를 기반으로 다음으로 실행할 LLM 모델, 도구(Tool), 하위 워크플로우 또는 특정 처리 경로를 동적으로 선택하고 연결하는 패턴이다.  
  
라우팅 패턴에서는 특정 문제를 해결하기 위한 special 모델이 있을 수 있다는 아이디어가 있습니다. LLM1, LLM2, LLM3 는 각각 다른 task에 좋은 역할을 할 수 있다. LLM 라우터는 입력을 분석하여 그 의도(intent)를 파악하거나, 내용의 유형(예: 수학 문제, 글쓰기 요청, 웹 검색 필요 여부)을 분류하여 어떤 LLM 모델, 어떤 도구, 어떤 하위 체인 또는 어떤 최종 경로로 이 입력을 보낼지 결정한다.  



#### 3. PARALLELIZATION
![parallelization](/assets/it/ml/ai-agents/parallelization.png)
- Breaking down tasks and running multiple subtasks concurrently
- 전체 목표 달성에 필요한 여러 하위 작업을 동시에(concurrently) 실행하여 전체 처리 시간을 단축하고 효율성을 높이는 기법
- 여기서 Coordinator 는 **어떤 코드** 이며 하나의 작업을 여러 조각으로 나누어 모두 병렬로 실행하는 코드를 작성한다. 그리고 동시에 세 가지 활동을 동시에 수행하기 위해 3 개의 LLM으로 전달된다.  

Parallelization(병렬화)에서는 서로 의존성이 없는(independent) 하위 작업을 식별하고, 이를 동시에 실행하는 방식으로 이루어진다. 
1. 작업식별(Task Identification): 전체 목표를 여러개의 하위 작업으로 나눔.
2. 의존성 분석(Dependency Analysis): 각 하위 작업이 다른 작업 결과를 필요로 하는지(순차적 의존성) 아니면 독립적으로 실행가능한지(병렬 수행 가능성)을 판단.
3. 동시 실행(Concurrent Execution): 독립적인 하위 작업들을 동시 실행 (여러 LLM 모델 호출, 여러 도구 호출 등)
4. 결과 통합(Result Integration): 병렬로 실행된 각 하위 작업의 결과를 모아(Aggregation) 다음 단계의 인풋으로 활용 또는 최종 결과를 생성.


#### 4. ORCHESTRATOR-WORKER
![orchestrator-worker](/assets/it/ml/ai-agents/orchestrator-worker.png)
- Complex tasks are broken down dynamically and combined
- Parallelization(병렬화) 패턴과 유사하지만 오스케스트레이션을 수행하는 것이 더 이상 코드가 아닌 LLM 이라는 점이다. 
- 모델을 사용하여 복잡한 작업을 더 작은 단계로 세분화한 다음 다음 모델을 사용하여 결과를 결합하는 것이다. 


#### 5. EVALUATOR-OPTIMIZER
![evaluator-optimizer](/assets/it/ml/ai-agents/evaluator-optimizer.png)
- LLM output is validated by another
- LLM Generator 는 업무를 수행하는 LLM 이다. LLM Generator 가 어떤 해결책을 제시하면 Evaluator LLM 은 이전 LLM의 작업을 확인하기 위해 존재한다. 
- 어떤 작업을 수행한 결과물(생성된 텍스트, 코드, 계획 등)을 Evaluator LLM을 통해'평가'하고, 이 평가 결과를 바탕으로 다시 원래 작업을 수행하는 LLM 또는 도구(Optimizer)에 '피드백'하여 결과물을 개선(Optimize)하는 반복적인 프로세스
- AI Agent의 자율성과 신뢰성을 높이는 핵심적인 디자인 패턴입니다.

Evaluator LLM은 이전 작업에 대해 평가 후, 수락하면 출력으로 이동하며 거부하는 경우 이유가 있어야 한다. 거부 하는 경우 거절 사유와 함께 이전으로 돌아가게 된다. Evaluator optimizer workflow 를 사용하는 경우 정확도를 높이고 최종 결과물의 품질을 올릴 수 있는 강력한 방법이다.  



---

## 📚 References

[1] **Udemy - AI Engineer Agentic Track**
- https://www.udemy.com/course/the-complete-agentic-ai-engineering-course