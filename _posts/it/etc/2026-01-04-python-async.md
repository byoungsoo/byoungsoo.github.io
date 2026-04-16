---
layout: post
title: "Python coroutine"
author: "Bys"
category: etc
date: 2026-01-04 01:00:00
keywords: "python coroutine"
tags: python
---


## 1. **코루틴(Coroutine)이란?**

**"일시 중지하고 재개할 수 있는 함수"**

#### Coroutine 객체는 무엇인가?
**"아직 실행되지 않은 함수의 '실행 계획서'"**

```python
# 일반 함수
def normal_function():
    return "완료"

# 코루틴 (async def 사용)
async def my_coroutine():
    return "완료"
```

**차이점:**
```python
# 일반 함수는 호출하면 즉시 실행됨
result = normal_function()  # "완료"가 바로 result에 들어감

# 코루틴은 호출해도 실행되지 않음!
coro_obj = my_coroutine()  # 코루틴 객체만 반환됨 (실행 X)
result = await my_coroutine()  # await를 써야 실행됨
```

**`async` 로 함수를 정의하면, `await` 을 통해 실행할 수 있다.**



### 동기 방식 (일반 코드)
```python
def process_three_requests():
    result1 = call_api_1()  # 3초 대기
    result2 = call_api_2()  # 3초 대기
    result3 = call_api_3()  # 3초 대기
    # 총 9초 소요
```

### await 키워드를 붙인 동기 방식 
```python
def process_three_requests():
    result1 = await call_api_1()  # 3초 대기
    result2 = await call_api_2()  # 3초 대기
    result3 = await call_api_3()  # 3초 대기
    # 총 9초 소요
```

### 비동기 방식
```python
async def process_three_requests():
    results = await asyncio.gather(
        call_api_1(),  # 동시에
        call_api_2(),  # 동시에
        call_api_3()   # 동시에
    )
    # 총 3초 소요 (동시 실행)
```

여기서 헷갈리지 말아야 하는 건(await 키워드를 붙인 동기 방식 사례) await 키워드를 붙여서 call_api_1, 2, 3 을 호출하더라도 call_api_1() 호출 중 다른일을 할 수 있을 때도 다른 coroutine 함수가 asyncio.gather 에 의해 묶이지 않았기 때문에 다른 coroutine 작업을 진행하지 않는다. 따라서, 기존 동기방식과 같이 9초가 걸린다. 따라서, `async def` 함수는 `await`와 함께 사용한다고 이해하면 좋다.  


```python
def watch_movie():
    print("영화 전체 다운로드 중...")
    time.sleep(3600)  # 1시간 대기
    print("다운로드 완료! 이제 재생합니다")
    play_entire_movie()

→ 1시간 기다려야 영화 볼 수 있음 😭
스트리밍 (비동기)

async def watch_movie():
    async for chunk in movie_stream():  # 조각조각 받음
        play(chunk)  # 받는 즉시 재생!
        # 다음 조각 오는 동안 지금 조각 재생

→ 바로 볼 수 있음! 넷플릭스처럼! 🎉

원래 코드가 정확히 이겁니다!

# AI가 이메일을 "스트리밍"으로 생성
async for event in result.stream_events():  # 한 글자씩 받음
    print(event.data.delta, end="")  # 받는 즉시 출력!
```


## 2. 사용 규칙
#### 규칙 1: `async def` 함수는 `await`와 함께
```python
async def my_function():
    return "결과"

# ❌ 잘못된 사용
result = my_function()  # 코루틴 객체만 얻음

# ✅ 올바른 사용
result = await my_function()  # 실제로 실행됨
```

#### 규칙 2: `await`는 `async def` 안에서만
```python
# ❌ 에러 발생
def normal_function():
    result = await some_coroutine()  # 에러!

# ✅ 정상 작동
async def async_function():
    result = await some_coroutine()  # OK
```

#### 규칙 3: 여러 작업 동시 실행
```python
# 순차 실행 (느림)
async def sequential():
    r1 = await task1()  # 끝날 때까지 대기
    r2 = await task2()  # 그 다음 실행
    r3 = await task3()  # 그 다음 실행

# 동시 실행 (빠름)
async def concurrent():
    results = await asyncio.gather(
        task1(),  # 모두
        task2(),  # 동시에
        task3()   # 실행
    )
```

#### 규칙 4: 연쇄 규칙 (Chain Rule)
```python
# OpenAI 라이브러리 내부 (가정)
async def chat():  # ← async def
    # 네트워크 요청
    return response

# 우리 코드
async def call_openai_1():  # ← await 쓰려면 async def 필요
    response = await openai.chat()  # ← await 사용
    return response

# 더 상위 코드
async def main():  # ← await 쓰려면 async def 필요
    result = await call_openai_1()  # ← await 사용
    return result
```


## 3. 정리

### 🎓 핵심 요약

1. **`async def`** = 일시 중지 가능한 함수 (코루틴)
2. **`await`** = "이 작업 실행하고 끝날 때까지 대기"
3. **이벤트 루프** = 대기 중인 작업 있으면 다른 작업으로 전환
4. **`asyncio.gather()`** = 여러 작업 동시 실행


## 4. 실제 사용 예시
## 🎬 상세한 실행 과정

```python
async def download_file():
    print("다운로드 시작")
    data = await fetch_chunk_1()  # ← A 지점
    print("첫 번째 청크 완료")
    data += await fetch_chunk_2()  # ← B 지점
    print("두 번째 청크 완료")
    return data

async def fetch_chunk_1():
    print("청크1 요청 보냄")
    await asyncio.sleep(2)  # 네트워크 대기 시뮬레이션
    print("청크1 응답 받음")
    return "데이터1"
```

### 실행 타임라인

```
0.0초: download_file() 시작
       출력: "다운로드 시작"
       
0.0초: fetch_chunk_1() 호출
       출력: "청크1 요청 보냄"
       asyncio.sleep(2) 도달
       
       ┌─────────────────────────────────┐
       │ 여기서 "멈춘다"의 의미:        │
       │                                  │
       │ ❌ 프로그램 전체가 멈춤         │
       │ ✅ 이 코루틴만 대기 상태        │
       │    → CPU는 다른 코루틴 실행 가능│
       └─────────────────────────────────┘

[0.0초 ~ 2.0초: 네트워크 응답 대기]
       이벤트 루프: "이 코루틴은 대기 중이니까
                     다른 코루틴 실행하자!"

2.0초: 네트워크 응답 도착
       출력: "청크1 응답 받음"
       fetch_chunk_1() 완료 → "데이터1" 반환
       
2.0초: download_file() 재개
       data = "데이터1"
       출력: "첫 번째 청크 완료"
       
       (이제 fetch_chunk_2()로 같은 과정 반복...)
```

### 예시: 두 개의 다운로드
```python
async def main():
    await asyncio.gather(
        download_file_A(),
        download_file_B()
    )

async def download_file_A():
    print("A: 시작")
    data = await fetch_data()  # 2초 걸림
    print("A: 완료")
    return data

async def download_file_B():
    print("B: 시작")
    data = await fetch_data()  # 2초 걸림
    print("B: 완료")
    return data
```

### 실행 과정

```
0.0초: download_file_A() 시작
       출력: "A: 시작"
       await fetch_data() 도달
       → A는 네트워크 대기 상태 ⏸️

0.0초: 이벤트 루프: "A가 대기 중이네? B 실행하자!"
       download_file_B() 시작
       출력: "B: 시작"
       await fetch_data() 도달
       → B도 네트워크 대기 상태 ⏸️

0.0초 ~ 2.0초: 
       A도 대기 ⏸️
       B도 대기 ⏸️
       이벤트 루프: "둘 다 대기 중, 나도 쉬자" 💤
       (하지만 CPU는 다른 프로그램 실행 가능!)

2.0초: A의 네트워크 응답 도착
       → A 재개 ▶️
       출력: "A: 완료"
       
2.0초: B의 네트워크 응답 도착
       → B 재개 ▶️
       출력: "B: 완료"
```

**출력 결과:**
```
A: 시작
B: 시작
(2초 후)
A: 완료
B: 완료
```

