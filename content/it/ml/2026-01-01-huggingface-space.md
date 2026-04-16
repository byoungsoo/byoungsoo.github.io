---
slug: huggingface-space
author: Bys
categories:
- ml
category: ml
date: '2026-01-01 01:00:00'
keywords: tool, ai, llm huggingface
tags:
- tool
- ai
- chatbot
- llm
- huggingface
title: Hugging Face - Space를 통해 커리어 챗봇 만들기
description: "Hugging Face Space를 활용한 커리어 챗봇 구축 - AI 앱 배포 및 공유 방법, Gradio 인터페이스 연동 실습"
---


# [Hugging Face Space](https://huggingface.co/spaces)  
Hugging Face Space 는 AI 앱을 만들어서 배포 및 공유를 할 수 있는 공간이라고 이해하면된다.  

- 간편한 배포: 별도의 복잡한 인프라나 서버 구축 없이, 코드 몇 줄만으로 AI 모델 데모를 웹에 즉시 배포가능
- 오픈소스 공유: GitHub와 유사하게 제작한 앱을 커뮤니티에 공개하여 누구나 실행해 볼 수 있게 하거나, 다른 사용자의 공개된 스페이스를 복제(Duplicate)하여 수정가능
- 다양한 하드웨어 지원: CPU뿐만 아니라 고성능 GPU와 대용량 저장소를 옵션으로 선택할 수 있어, 무거운 대규모 언어 모델(LLM)도 구동 가능하며 CPU 기반의 무료 하드웨어도 지원 

**이번에는 Gradio 라이브러리를 사용하여 간단하게 나의 커리어를 활용(RAG)하여 나의 아바타 같은 채팅 앱을 배포한다.**  


## 1. 작업 순서

1. 허깅 페이스 계정 로그인 
2. 메뉴에서 Access Token 발급 (Write 권한이 필수로 필요하다.)
3. `uv tool install 'huggingface_hub[cli]'` 커맨드를 통해 HuggingFace 툴 설치하고, `hf auth login --token YOUR_TOKEN_HERE` 를 통해 로그인할 수 있다. `hf auth whoami`을 통해 로그인 확인.
4. 코드 구성  
```bash
├── app.py             
├── requirements.txt   
└── me/    
      ├── linkedin.pdf   
      ├── wanted.pdf     
      └── summary.txt    
```
5. `uv run gradio deploy` 커맨드를 통해 HuggingFace Space 에 배포 진행 


## 2. 사전 작업
`me`  
me 폴더 하위에는 linkedin 에서 받은 PDF, wanted 에서 받은 PDF 파일과 나에대한 소개를 적어놓은 summary.txt 파일이 존재한다. 


`requirements.txt`
```
requests
python-dotenv
gradio
pypdf
openai
openai-agents
```


`app.py`  
```python
from dotenv import load_dotenv
from openai import OpenAI
import json
import os
import requests
from pypdf import PdfReader
import gradio as gr


load_dotenv(override=True)

def push(text):
    requests.post(
        "https://api.pushover.net/1/messages.json",
        data={
            "token": os.getenv("PUSHOVER_TOKEN"),
            "user": os.getenv("PUSHOVER_USER"),
            "message": text,
        }
    )


def record_user_details(email, name="Name not provided", notes="not provided"):
    push(f"Recording {name} with email {email} and notes {notes}")
    return {"recorded": "ok"}

def record_unknown_question(question):
    push(f"Recording {question}")
    return {"recorded": "ok"}

record_user_details_json = {
    "name": "record_user_details",
    "description": "Use this tool to record that a user is interested in being in touch and provided an email address",
    "parameters": {
        "type": "object",
        "properties": {
            "email": {
                "type": "string",
                "description": "The email address of this user"
            },
            "name": {
                "type": "string",
                "description": "The user's name, if they provided it"
            }
            ,
            "notes": {
                "type": "string",
                "description": "Any additional information about the conversation that's worth recording to give context"
            }
        },
        "required": ["email"],
        "additionalProperties": False
    }
}

record_unknown_question_json = {
    "name": "record_unknown_question",
    "description": "Always use this tool to record any question that couldn't be answered as you didn't know the answer",
    "parameters": {
        "type": "object",
        "properties": {
            "question": {
                "type": "string",
                "description": "The question that couldn't be answered"
            },
        },
        "required": ["question"],
        "additionalProperties": False
    }
}

tools = [{"type": "function", "function": record_user_details_json},
        {"type": "function", "function": record_unknown_question_json}]


class Me:

    def __init__(self):
        self.openai = OpenAI()
        self.name = "Byoungsoo Ko"
        reader = PdfReader("me/linkedin.pdf")
        
        self.linkedin = ""
        for page in reader.pages:
            text = page.extract_text()
            if text:
                self.linkedin += text
                
        reader = PdfReader("me/wanted.pdf")
        self.wanted = ""
        for page in reader.pages:
            text = page.extract_text()
            if text:
                self.wanted += text
 
        with open("me/summary.txt", "r", encoding="utf-8") as f:
            self.summary = f.read()


    def handle_tool_call(self, tool_calls):
        results = []
        for tool_call in tool_calls:
            tool_name = tool_call.function.name
            arguments = json.loads(tool_call.function.arguments)
            print(f"Tool called: {tool_name}", flush=True)
            tool = globals().get(tool_name)
            result = tool(**arguments) if tool else {}
            results.append({"role": "tool","content": json.dumps(result),"tool_call_id": tool_call.id})
        return results
    
    def system_prompt(self):
        system_prompt = f"You are acting as {self.name}. You are answering questions on {self.name}'s website, \
particularly questions related to {self.name}'s career, background, skills and experience. \
Your responsibility is to represent {self.name} for interactions on the website as faithfully as possible. \
You are given a summary of {self.name}'s background and LinkedIn profile which you can use to answer questions. \
Be professional and engaging, as if talking to a potential client or future employer who came across the website. \
If you don't know the answer to any question, use your record_unknown_question tool to record the question that you couldn't answer, even if it's about something trivial or unrelated to career. \
If the user is engaging in discussion, try to steer them towards getting in touch via email; ask for their email and record it using your record_user_details tool. "

        system_prompt += f"\n\n## Summary:\n{self.summary}\n\n## LinkedIn Profile:\n{self.linkedin}\n\nnWanted Profile:\n{self.wanted}\n\n"
        system_prompt += f"With this context, please chat with the user, always staying in character as {self.name}."
        return system_prompt
    
    def chat(self, message, history):
        messages = [{"role": "system", "content": self.system_prompt()}] + history + [{"role": "user", "content": message}]
        done = False
        while not done:
            response = self.openai.chat.completions.create(model="gpt-4o-mini", messages=messages, tools=tools)
            if response.choices[0].finish_reason=="tool_calls":
                message = response.choices[0].message
                tool_calls = message.tool_calls
                results = self.handle_tool_call(tool_calls)
                messages.append(message)
                messages.extend(results)
            else:
                done = True
        return response.choices[0].message.content
    

if __name__ == "__main__":
    me = Me()
    gr.ChatInterface(me.chat, type="messages").launch()
```

이 코드는 이전 RAG, Tools 에서 다룬 내용과 비슷한 내용의 코드이다. 나에 대한 정보를 토대로 Gradio UI 인터페이스를 통해 챗을 구현한다.  



## 3. 배포
`uv run gradio deploy` 커맨드를 통해 HuggingFace Space 에 나의 앱을 배포할 수 있다.  

- Name: career_conversation
- AppFile: app.py
- Hardware: cpu-basic (Free)
- Supply Secret:
  - OPENAI_API_KEY 
  - PUSHOVER_USER
  - PUSHOVER_TOKEN
- Github Actions: No

```bash
$ uv run gradio deploy

Need 'write' access token to create a Spaces repo.

    _|    _|  _|    _|    _|_|_|    _|_|_|  _|_|_|  _|      _|    _|_|_|      _|_|_|_|    _|_|      _|_|_|  _|_|_|_|
    _|    _|  _|    _|  _|        _|          _|    _|_|    _|  _|            _|        _|    _|  _|        _|
    _|_|_|_|  _|    _|  _|  _|_|  _|  _|_|    _|    _|  _|  _|  _|  _|_|      _|_|_|    _|_|_|_|  _|        _|_|_|
    _|    _|  _|    _|  _|    _|  _|    _|    _|    _|    _|_|  _|    _|      _|        _|    _|  _|        _|
    _|    _|    _|_|      _|_|_|    _|_|_|  _|_|_|  _|      _|    _|_|_|      _|        _|    _|    _|_|_|  _|_|_|_|


Enter your token (input will not be visible): 

Add token as git credential? (Y/n) Y 
Creating new Spaces Repo in '/Users/bys/workspace/code_repo/github/agents/1_foundations'. Collecting metadata, press Enter to accept default value.

Enter Spaces app title [1_foundations]: career_conversation

Enter Gradio app file [app_local.py]: app.py

Enter Spaces hardware (cpu-basic, cpu-upgrade, cpu-xl, zero-a10g, t4-small, t4-medium, l4x1, l4x4, l40sx1, l40sx4, l40sx8, a10g-small, a10g-large, a10g-largex2, a10g-largex4, a100-large, h100, h100x8) [cpu-basic]: cpu-basic

Any Spaces secrets (y/n) [n]: y

Enter secret name (leave blank to end): OPENAI_API_KEY
Enter secret value for OPENAI_API_KEY: 

Enter secret name (leave blank to end): PUSHOVER_USER
Enter secret value for PUSHOVER_USER:  

Enter secret name (leave blank to end): PUSHOVER_TOKEN
Enter secret value for PUSHOVER_TOKEN:

Enter secret name (leave blank to end): 

Create Github Action to automatically update Space on 'git push'? [n]: n
It seems you are trying to upload a large folder at once. This might take some time and then fail if the folder is too large. For such cases, it is recommended to upload in smaller batches or to use `HfApi().upload_large_folder(...)`/`hf upload-large-folder` instead. For more details, check out https://huggingface.co/docs/huggingface_hub/main/en/guides/upload#upload-a-large-folder.
Processing Files (1 / 1)      : 100%|██████████████████████████████████████████████████████████████████████████████████████████████████████|  525kB /  525kB,  0.00B/s  
New Data Upload               : |                                                                                                          |  0.00B /  0.00B,  0.00B/s  
  ...foundations/me/wanted.pdf: 100%|██████████████████████████████████████████████████████████████████████████████████████████████████████|  525kB /  525kB            
Space available at https://huggingface.co/spaces/~~
```


## 4. Test 결과

![space-demo1](/assets/it/ml/huggingface/space-demo1.png)

HuggingFace 를 통해 나의 커리어 챗봇이 배포되었으며 인터넷을 통해 접근하여 질의시 상세히 답변해주는 것을 알 수 있다. 또한, 대화중 특정 요청을 할 경우 HuggingFace 에 배포된 앱에서도 tool을 통해 pushover 앱 알람이 오는 것을 확인할 수 있었다.  


## 5. AWS Bedrock 모델호출로 전환

app.py 코드에서 OpenAI 모델 호출 대신 AWS Bedrock 모델을 호출하도록 코드를 변경하였다. 이유는 비용때문이다. 나의 환경에서는 현재 AWS Bedrock 의 경우는 무료로 사용할 수 있기 때문에 OpenAI 쪽으로 API가 지속 날아가면 비용이 문제가 될 수 있기 때문에 AWS Bedrock 모델 호출로 변경한다.

### 5.1 주요 변경점

#### 1) 클라이언트 초기화
```python
# OpenAI
self.openai = OpenAI()

# Bedrock
self.bedrock = boto3.client(
    service_name='bedrock-runtime',
    region_name=os.getenv('AWS_REGION', 'us-east-1'),
)
self.model_id = "global.anthropic.claude-opus-4-5-20251101-v1:0"
```
- OpenAI SDK 대신 boto3 사용
- 모델 ID를 명시적으로 지정 (Claude Opus 4.5)

#### 2) Tool 정의 형식
```python
# OpenAI 형식
{
    "type": "function",
    "function": {
        "name": "record_user_details",
        "parameters": { ... }
    }
}

# Bedrock 형식
{
    "toolSpec": {
        "name": "record_user_details",
        "inputSchema": {
            "json": { ... }
        }
    }
}
```
- Bedrock은 `toolSpec`과 `inputSchema.json` 구조 사용
- `parameters` 대신 `inputSchema` 사용

#### 3) 메시지 형식 변환
```python
# OpenAI: 단순 문자열
{"role": "user", "content": "message"}

# Bedrock: content는 배열 형태
{"role": "user", "content": [{"text": "message"}]}
```
- Bedrock은 content를 배열로 감싸고 text 객체로 구성
- `convert_history_to_bedrock()` 메서드로 Gradio history 변환

#### 4) API 호출 방식
```python
# OpenAI
response = self.openai.chat.completions.create(
    model="gpt-4o-mini",
    messages=messages,
    tools=tools
)

# Bedrock
response = self.bedrock.converse(
    modelId=self.model_id,
    messages=messages,
    system=[{"text": self.system_prompt()}],
    toolConfig={"tools": tools}
)
```
- OpenAI는 system 메시지를 messages 배열에 포함
- Bedrock은 system을 별도 파라미터로 분리
- `toolConfig`로 tools 전달

#### 5) Tool 호출 처리
```python
# OpenAI
if response.choices[0].finish_reason == "tool_calls":
    tool_calls = message.tool_calls
    # tool_call.function.name, tool_call.function.arguments
    results = [{"role": "tool", "content": json.dumps(result), "tool_call_id": tool_call.id}]

# Bedrock
if stop_reason == "tool_use":
    for content_block in response['output']['message']['content']:
        if 'toolUse' in content_block:
            # content_block['toolUse']['name'], ['input'], ['toolUseId']
            result = {"toolUseId": tool_use_id, "content": [{"json": result}]}
    messages.append({"role": "user", "content": tool_results})
```
- OpenAI는 `finish_reason`과 `tool_calls` 사용
- Bedrock은 `stopReason`과 `toolUse` 블록 사용
- Tool 결과를 user 메시지로 추가 (OpenAI는 tool role)

#### 6) 응답 추출
```python
# OpenAI
return response.choices[0].message.content

# Bedrock
for content_block in response['output']['message']['content']:
    if 'text' in content_block:
        return content_block['text']
```
- Bedrock은 content 배열에서 text 블록 찾아야 함

### 5.2 전체 코드

`app_bedrock.py`
```python
from dotenv import load_dotenv
import json
import os
import requests
from pypdf import PdfReader
import gradio as gr
import boto3


load_dotenv(override=True)

def push(text):
    requests.post(
        "https://api.pushover.net/1/messages.json",
        data={
            "token": os.getenv("PUSHOVER_TOKEN"),
            "user": os.getenv("PUSHOVER_USER"),
            "message": text,
        }
    )


def record_user_details(email, name="Name not provided", notes="not provided"):
    push(f"Recording {name} with email {email} and notes {notes}")
    return {"recorded": "ok"}

def record_unknown_question(question):
    push(f"Recording {question}")
    return {"recorded": "ok"}

# Bedrock Tool 형식 (OpenAI와 다름)
record_user_details_json = {
    "toolSpec": {
        "name": "record_user_details",
        "description": "Use this tool to record that a user is interested in being in touch and provided an email address",
        "inputSchema": {
            "json": {
                "type": "object",
                "properties": {
                    "email": {
                        "type": "string",
                        "description": "The email address of this user"
                    },
                    "name": {
                        "type": "string",
                        "description": "The user's name, if they provided it"
                    },
                    "notes": {
                        "type": "string",
                        "description": "Any additional information about the conversation that's worth recording to give context"
                    }
                },
                "required": ["email"]
            }
        }
    }
}

record_unknown_question_json = {
    "toolSpec": {
        "name": "record_unknown_question",
        "description": "Always use this tool to record any question that couldn't be answered as you didn't know the answer",
        "inputSchema": {
            "json": {
                "type": "object",
                "properties": {
                    "question": {
                        "type": "string",
                        "description": "The question that couldn't be answered"
                    }
                },
                "required": ["question"]
            }
        }
    }
}

tools = [record_user_details_json, record_unknown_question_json]


class Me:

    def __init__(self):
        # Bedrock 클라이언트 초기화
        self.bedrock = boto3.client(
            service_name='bedrock-runtime',
            region_name=os.getenv('AWS_REGION', 'us-east-1'),
        )
        
        # 사용할 모델 ID
        self.model_id = "global.anthropic.claude-opus-4-5-20251101-v1:0"
        
        self.name = "Byoungsoo Ko"
        reader = PdfReader("me/linkedin.pdf")
        
        self.linkedin = ""
        for page in reader.pages:
            text = page.extract_text()
            if text:
                self.linkedin += text
                
        reader = PdfReader("me/wanted.pdf")
        self.wanted = ""
        for page in reader.pages:
            text = page.extract_text()
            if text:
                self.wanted += text
 
        with open("me/summary.txt", "r", encoding="utf-8") as f:
            self.summary = f.read()


    def handle_tool_call(self, tool_use_block):
        """Bedrock의 tool_use 블록 처리"""
        tool_name = tool_use_block['name']
        arguments = tool_use_block['input']
        tool_use_id = tool_use_block['toolUseId']
        
        print(f"Tool called: {tool_name}", flush=True)
        
        # 함수 실행
        tool = globals().get(tool_name)
        result = tool(**arguments) if tool else {}
        
        # Bedrock 형식으로 결과 반환
        return {
            "toolUseId": tool_use_id,
            "content": [{"json": result}]
        }
    
    def system_prompt(self):
        system_prompt = f"You are acting as {self.name}. You are answering questions on {self.name}'s website, \
particularly questions related to {self.name}'s career, background, skills and experience. \
Your responsibility is to represent {self.name} for interactions on the website as faithfully as possible. \
You are given a summary of {self.name}'s background and LinkedIn profile which you can use to answer questions. \
Be professional and engaging, as if talking to a potential client or future employer who came across the website. \
If you don't know the answer to any question, use your record_unknown_question tool to record the question that you couldn't answer, even if it's about something trivial or unrelated to career. \
If the user is engaging in discussion, try to steer them towards getting in touch via email; ask for their email and record it using your record_user_details tool. "

        system_prompt += f"\n\n## Summary:\n{self.summary}\n\n## LinkedIn Profile:\n{self.linkedin}\n\nWanted Profile:\n{self.wanted}\n\n"
        system_prompt += f"With this context, please chat with the user, always staying in character as {self.name}."
        return system_prompt
    
    def convert_history_to_bedrock(self, history):
        """Gradio history를 Bedrock 형식으로 변환"""
        bedrock_messages = []
        for msg in history:
            bedrock_messages.append({
                "role": msg["role"],
                "content": [{"text": msg["content"]}]
            })
        return bedrock_messages
    
    def chat(self, message, history):
        # Bedrock 메시지 형식으로 변환
        messages = self.convert_history_to_bedrock(history)
        messages.append({
            "role": "user",
            "content": [{"text": message}]
        })
        
        done = False
        while not done:
            # Bedrock Converse API 호출
            response = self.bedrock.converse(
                modelId=self.model_id,
                messages=messages,
                system=[{"text": self.system_prompt()}],
                toolConfig={"tools": tools}
            )
            
            stop_reason = response['stopReason']
            
            # Tool 호출이 필요한 경우
            if stop_reason == "tool_use":
                # Assistant 메시지 추가
                assistant_message = {
                    "role": "assistant",
                    "content": response['output']['message']['content']
                }
                messages.append(assistant_message)
                
                # Tool 실행 및 결과 추가
                tool_results = []
                for content_block in response['output']['message']['content']:
                    if 'toolUse' in content_block:
                        result = self.handle_tool_call(content_block['toolUse'])
                        tool_results.append({"toolResult": result})
                
                # Tool 결과를 user 메시지로 추가
                messages.append({
                    "role": "user",
                    "content": tool_results
                })
            else:
                done = True
        
        # 최종 텍스트 응답 추출
        for content_block in response['output']['message']['content']:
            if 'text' in content_block:
                return content_block['text']
        
        return "Sorry, I couldn't generate a response."
    

if __name__ == "__main__":
    me = Me()
    gr.ChatInterface(me.chat, type="messages").launch()
```

---

## 📚 References

[1] **Udemy - AI Engineer Agentic Track**
- https://www.udemy.com/course/the-complete-agentic-ai-engineering-course

[2] **AWS Bedrock Converse API**
- https://docs.aws.amazon.com/bedrock/latest/APIReference/API_runtime_Converse.html