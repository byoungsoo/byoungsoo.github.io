---
layout: post
title: "Multi Agents 를 이용한 Deep Research -> AI Report 발행 시스템 구현"
author: "Bys"
category: ml
date: 2026-01-20 01:00:00
keywords: "openai, agents, sdk, pydantic"
tags: openai agents pydantic
---

# Multi Agents - Deep Research Agents 구현  
다중 에이전트를 이용하여 심층 조사 레포트를 작성하는 시스템을 구현하려고 한다.  

- **시나리오:** 
  1. Gradio UI 를 통해 사용자 입력을 받음
  2. ResearchManager(오케스트레이터)는 전체프로세스를 관리
     1. 검색 계획 수립(SearchPlanAgent)
     2. 병렬 검색 실행(SearchAgent)
     3. 보고서 작성(WriterAgent)
     4. 메일 발송(EmailAgent)
  3. SearchPlanAgent 는 사용자 입력(Query)를 분석하여 N개의 검색어를 생성  
  4. SearchAgent 는 WebSearchTool 을 사용하여 검색 실행 및 각 검색어당 2-3 문단 요약 생성
  5. WriterAgent 는 검색결과를 통합하여 보고서 작성 (Markdown 형식 출력)
  6. EmailAgent 는 보고서를 HTML 형식으로 변경하여 발송


- **구조:**
deep_research 폴더 하위에 각각의 agent 들을 모듈화하여 구성
```txt
/deep_research/
├── deep_research.py
└── research_manager.py
└── planner_agent.py
└── search_agent.py
└── writer_agent.py
└── email_agent.py
```

## 1. planner_agent.py
```python
from pydantic import BaseModel, Field
from agents import Agent

HOW_MANY_SEARCHES = 5

INSTRUCTIONS = f"You are a helpful research assistant. Given a query, come up with a set of web searches \
to perform to best answer the query. Output {HOW_MANY_SEARCHES} terms to query for."

class WebSearchItem(BaseModel):
    reason: str = Field(description="Your reasoning for why this search is important to the query.")
    query: str = Field(description="The search term to use for the web search.")

class WebSearchPlan(BaseModel):
    searches: list[WebSearchItem] = Field(description="A list of web searches to perform to best answer the query.")

planner_agent = Agent(
  name = "planner_agent",
  instructions = INSTRUCTIONS,
  model = "gpt-4o-mini",
  output_type = WebSearchPlan
)
```
Planner 에이전트는 심층 보고서를 작성하기 전에 사용자 질문에 대해 어떤 아이템을 검색하면 좋을지 리스트를 생성하는 Agent 이다.  

WebSearchItem, WebSearchPlan 두 클래스는 데이터를 구조화하는데 pydantic 에서 BaseModel 을 상속받고, Field 클래스 기능을 사용한다.  
pydantic 은 Python의 유효성 검사 및 설정을 위한 라이브러리다. 이 라이브러리를 통해 데이터 클래스 및 데이터 유효성 검사에 대한 기능을 사용할 수 있다.  

예를 들어 planner_agent 는 output_type 으로 WebSearchPlan 타입을 반환하는데 WebSearchPlan 타입은 WebSearchItem 의 리스트 이며 WebSearchItem 는 reason, query 로 구성된 오브젝트이다. 


## 2. search_agent.py  

```python
from agents import Agent, WebSearchTool, ModelSettings

INSTRUCTIONS = (
    "You are a research assistant. Given a search term, you search the web for that term and "
    "produce a concise summary of the results. The summary must 2-3 paragraphs and less than 300 "
    "words. Capture the main points. Write succintly, no need to have complete sentences or good "
    "grammar. This will be consumed by someone synthesizing a report, so its vital you capture the "
    "essence and ignore any fluff. Do not include any additional commentary other than the summary itself."
)

search_agent = Agent(
    name="Search agent",
    instructions=INSTRUCTIONS,
    tools=[WebSearchTool(search_context_size="low")],
    model="gpt-4o-mini",
    model_settings=ModelSettings(tool_choice="required"),
)
```

Search 에이전트는 Planner 에이전트에게 검색 리스트를 전달받아 실제 검색을 수행하는 에이전트이다. 이 때 Search 에이전트는 도구로서 [WebSearchTool](https://openai.github.io/openai-agents-python/tools/)을 사용한다. WebSearchTool 은 OpenAI 에서 제공하는 Hosted Tool 로 우리는 이것을 가져다가 사용할 수 있다. Web Search 도구에 대해 자세한 설명은 [Web search OpenAI 문서](https://platform.openai.com/docs/guides/tools-web-search)에서 확인할 수 있다.  

## 3. writer_agent.py

```python
from agents import Agent

INSTRUCTIONS = (
    "You are a senior researcher tasked with writing a cohesive report for a research query. "
    "You will be provided with the original query, and some initial research done by a research assistant.\n"
    "You should first come up with an outline for the report that describes the structure and "
    "flow of the report. Then, generate the report and return that as your final output.\n"
    "The final output should be in markdown format, and it should be lengthy and detailed. Aim "
    "for 5-10 pages of content, at least 1000 words."
)

class ReportData(BaseModel):
    short_summary: str = Field(description="A short 2-3 sentence summary of the findings.")

    markdown_report: str = Field(description="The final report")

    follow_up_questions: list[str] = Field(description="Suggested topics to research further")

writer_agent = Agent(
    name="Writer agent",
    instructions=INSTRUCTIONS,
    model="gpt-4o-mini",
    output_type=ReportData
)

Writer 에이전트는 검색결과를 기반으로 보고서를 작성하는 에이전트다. 마찬가지로 ReportData 라는 구조화된 데이터형식을 output_type 으로 사용하게 된다.  

```

## 4. email_agent.py  

```python
from agents import Agent
from typing import Dict
from agents import Agent, function_tool
import requests

@function_tool
def send_email(title: str, body, str):
    domain = "bys.digital"
    api_key = os.getenv('MAILGUN_API_KEY')
    if not api_key:
        print("❌ MAILGUN_API_KEY 환경 변수가 설정되지 않았습니다!")
        return None
    url = f"https://api.mailgun.net/v3/{domain}/messages"
    data = {
        "from": f"Mailgun Sandbox <postmaster@{domain}>",
        "to": "skwltg90@naver.com",  # 이메일만 (이름 제거)
        "subject": title,
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

INSTRUCTIONS = """You are able to send a nicely formatted HTML email based on a detailed report.
You will be provided with a detailed report. You should use your tool to send one email, providing the 
report converted into clean, well presented HTML with an appropriate subject line."""

email_agent=Agent(
  name="Email agent",
  instructions=INSTRUCTIONS,
  tools=[send_email],
  model="gpt-4o-mini"
)
```
이메일을 발송하기 위해 Mailgun 이라는 사이트를 이용했고, Request를 보내기 위해서는 반드시 MAILGUN_API_KEY 가 필요하다. Mailgun 에서 계정을 생성하고 API KEY를 발급받으면 특정 건수까지 무료로 사용이 가능하다.  


## 5. research_manager.py

```python
from agents import Runner, trace, gen_trace_id
from search_agent import search_agent
from planner_agent import planner_agent, WebSearchItem, WebSearchPlan
from writer_agent import writer_agent, ReportData
from email_agent import email_agent
import asyncio

class ResearchManager:

    async def run(self, query: str):
        """ Run the deep research process, yielding the status updates and the final report"""
        trace_id = gen_trace_id()
        with trace("Research Trace", trace_id=trace_id):
            print(f"View trace: https://platform.openai.com/traces/trace?trace_id={trace_id}")
            yield f"View trace: https://platform.openai.com/traces/trace?trace_id={trace_id}"
            
            print("Starting research...")
            search_plan = await self.plan_searches(query)
            
            yield "Searches planned, starting to search..."     
            search_results = await self.perform_searches(search_plan)

            yield "Searches complete, writing report..."
            report = await self.write_report(query, search_results)

            yield "Report written, sending email..."
            await self.send_email(report)

            yield "Email sent, research complete"
            yield report.markdown_report

    async def plan_searches(self, query: str) -> WebSearchPlan:
        """ Plan the searches to perform for the query """
        print("Planning searches...")
        result = await Runner.run(
            planner_agent,
            f"Query: {query}",
        )
        print(f"Will perform {len(result.final_output.searches)} searches")
        return result.final_output_as(WebSearchPlan)

    async def perform_searches(self, search_plan: WebSearchPlan) -> list[str]:
        """ Perform the searches to perform for the query """
        print("Searching...")
        num_completed = 0
        
        # asyncio
        tasks = [asyncio.create_task(self.search(item)) for item in search_plan.searches]
        results = []
        for task in asyncio.as_completed(tasks):
            result = await task
            if result is not None:
                results.append(result)
            num_completed += 1
            print(f"Searching... {num_completed}/{len(tasks)} completed")
        print("Finished searching")
        return results
    
    async def search(self, item: WebSearchItem) -> str | None:
        """ Perform a search for the query """
        input = f"Search term: {item.query}\nReason for searching: {item.reason}"
        try:
            result = await Runner.run(
                search_agent,
                input
            )
            return str(result.final_output)
        except Exception:
            return None
    
    async def write_report(self, query: str, search_results: list[str]) -> ReportData:
        """ Write the report for the query """
        print("Thinking about report...")
        input = f"Original query: {query}\nSummarized search results: {search_results}"
        result = await Runner.run(
            writer_agent,
            input
        )
        print("Finished writing report")
        return result.final_output_as(ReportData)


    async def send_email(self, report: ReportData) -> None:
        print("Writing email...")
        print("Sending email")
        result = await Runner.run(
            email_agent,
            report.markdown_report,
        )
        print("Finished sending email")
        return report
```
위 코드는 ResearchManager가 run 함수를 통해 여러 에이전트를 순차적으로 실행하고, 각 단계의 진행 상황을 yield로 반환하여 실시간 피드백을 제공하는 코드다.


## 6. deep_research.py
```python
import gradio as gr
from dotenv import load_dotenv
from research_manager import ResearchManager

load_dotenv(override=True)


async def run(query: str):
    async for chunk in ResearchManager().run(query):
        yield chunk


with gr.Blocks(theme=gr.themes.Default(primary_hue="sky")) as ui:
    gr.Markdown("# Deep Research")
    query_textbox = gr.Textbox(label="What topic would you like to research?")
    run_button = gr.Button("Run", variant="primary")
    report = gr.Markdown(label="Report")
    
    run_button.click(fn=run, inputs=query_textbox, outputs=report)
    query_textbox.submit(fn=run, inputs=query_textbox, outputs=report)

ui.launch(inbrowser=True)
```
Gradio는 비동기 제너레이터의 yield 값을 실시간으로 UI에 반영


## 7. 실행결과

```bash
$ uv run deep_research.py 
* Running on local URL:  http://127.0.0.1:7860
It looks like you are running Gradio on a hosted Jupyter notebook, which requires `share=True`. Automatically setting `share=True` (you can turn this off by setting `share=False` in `launch()` explicitly).

* Running on public URL: https://8af3c34de77e54e7c4.gradio.live

This share link expires in 1 week. For free permanent hosting and GPU upgrades, run `gradio deploy` from the terminal in the working directory to deploy to Hugging Face Spaces (https://huggingface.co/spaces)
View trace: https://platform.openai.com/traces/trace?trace_id=trace_239c2760fccf44039cfe1d2db521eba9
Starting research...
Planning searches...
Will perform 3 searches
Searching...
Searching... 1/3 completed
Searching... 2/3 completed
Searching... 3/3 completed
Finished searching
Thinking about report...
Finished writing report
Writing email...
Status Code: 200
Response: {"id":"<20260122020451.de5f7dab43b0485f@bys.digital>","message":"Queued. Thank you."}

✅ 이메일 전송 성공!
Email sent
```
deep_research.py 를 수행하면 Gradio 채팅 앱이 실행된다.  


`I'd like to research the US stock market outlook for 2026.`을 입력했을 때 아래와 같은 보고서가 메일로 발송된다.  

```
<html><head><style>body {font-family: Arial, sans-serif; line-height: 1.6;} h1, h2, h3, h4 {color: #2C3E50;} p {margin-bottom: 15px;} .container {padding: 20px; background-color: #F7F9FC; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);}</style></head><body><div class='container'><h1>U.S. Stock Market Outlook for 2026</h1><h2>I. Introduction</h2><p>The U.S. stock market, a barometer of the economy’s health, is often influenced by a myriad of factors that include economic indicators, political landscape, and global events. As we look ahead to 2026, analysts provide varied forecasts based on existing economic conditions and anticipated developments. This report collates insights from multiple sources to evaluate the expected performance of the stock market, focusing on the S&amp;P 500 index and relevant economic indicators.</p><h2>II. Economic Context</h2><h3>A. Economic Growth Projections</h3><p>In 2026, the U.S. economy is projected to experience moderate growth, with real GDP increasing by approximately 2.0%. This growth is anticipated to be significantly driven by robust consumer spending and substantial investments in artificial intelligence (AI), culminating in enhanced productivity and economic activity. The integration of AI is expected to stimulate various sectors, improving operational efficiency and thereby positively influencing the overall economic landscape.</p><p>According to Deloitte, reported growth rates will have a positive effect on the stock market as companies leverage AI to drive innovations and efficiency gains.</p><h3>B. Monetary Policy and Interest Rates</h3><p>The Federal Reserve is projected to begin a gradual easing cycle in 2026, with the federal funds rate expected to drop from 3.50% to approximately 3%. This reduction in interest rates is likely to support direct investment by corporations and spur consumer spending, essential components for achieving earnings growth.</p><h2>III. Stock Market Projections</h2><h3>A. S&amp;P 500 Performance</h3><p>Analysts project the S&amp;P 500 to achieve modest gains, with a year-over-year earnings growth forecast of approximately 14.5%, suggesting a continued bullish trend in equity markets. This could translate into a target for the index to reach around 7,968 by the end of the year 2026, based on historical averages and current market conditions.</p><p>Furthermore, despite a remarkable gain of 92% since the onset of the bull market in October 2022, ongoing high valuations raise the possibility of increased volatility.</p><h3>B. ETF and Market Components</h3><p>The SPDR S&amp;P 500 ETF Trust (SPY), one of the leading ETFs tracking the S&amp;P 500, reflects ongoing market sentiment and trading activity. As of January 2026, the ETF was trading at approximately $685.4, suggesting investor interest and a potential rally as fundamentals suggest a favorable outlook.</p><h2>IV. Influences of AI and Technological Innovations</h2><h3>A. AI Investments and Capital Expenditure</h3><p>As integral components of the future economic landscape, investments in AI are expected to exceed $527 billion by 2026. These investments are anticipated not only to boost productivity but also to pose challenges regarding operational costs.</p><h3>B. Impact on Employment and Inflation</h3><p>Despite positive economic forecasts, challenges such as persistent inflation pressures and fluctuations in the job market prevail. The unemployment rate, forecasted to peak at approximately 4.6% in 2026, may dampen consumer confidence if not managed effectively.</p><h2>V. Geopolitical Risks and Market Volatility</h2><h3>A. Recent Geopolitical Events</h3><p>Political developments, particularly regarding trade relations, have recently influenced market sentiment. Such fluctuations underscore the importance of geopolitical stability in market assurance.</p><h3>B. Long-Term vs Short-Term Outlook</h3><p>Ultimately, while short-term volatility remains a concern, the long-term outlook for the U.S. stock market through 2026 appears promising.</p><h2>VI. Strategic Recommendations for Investors</h2><p>Given the projected economic conditions and inherent market risks, investors are advised to adopt strategies that promote diversification and focus on high-quality stocks. This approach can mitigate risks associated with market volatility.</p><h2>VII. Conclusion</h2><p>In conclusion, the U.S. stock market outlook for 2026 presents a mixed but fundamentally positive scenario.</p><h2>VIII. Follow-Up Research Questions</h2><p>1. How will global trade policies evolve and impact U.S. market dynamics in 2026?<br>2. What sectors are poised for the highest growth due to AI advancements?<br>3. How can investors effectively hedge against inflation and market volatility?<br>4. What specific fiscal policies might influence consumer spending and corporate investment moving forward?<br>5. How will demographic shifts in the workforce affect productivity and economic growth in 2026?</p></div></body></html>
```


Trace 결과를 확인하면 다음과 같다.  

![deep_research_trace1](/assets/it/ml/agents/deep_research_trace1.png)  

Planner 에이전트는 Output으로 구조화된 타입(WebSearchPlan - list[WebSearchItem])으로 출력해준 것을 알 수 있으며 Search 에이전트는 검색을 수행했다.  
```Json
{
  "searches": [
    {
      "reason": "To gather expert predictions and analyses on the US stock market for 2026.",
      "query": "US stock market outlook 2026"
    },
    {
      "reason": "To find historical trends and data that could influence future market conditions in 2026.",
      "query": "US stock market historical trends 2026"
    },
    {
      "reason": "To get insights from financial analysts and economic indicators that could impact the market in 2026.",
      "query": "US economic indicators stock market outlook 2026"
    }
  ]
}
```
이 후, Writer 에이전트는 구조회된 출력(ReportData)로 결과를 Email 에이전트에게 전달하고 HTML 형태로 발송하는 것을 볼 수 있다. 실제 HTML 파일을 열면 아래와 같다.  

![deep_research](/assets/it/ml/agents/deep_research.png)  


## 8. 배운점

이번 데모 환경을 통해 배운점은:
1. 다중 에이전트를 통해 각각의 역할을 정의하고 스텝(검색계획수립 -> 검색 -> 보고서작성 -> 메일발송)별로 프로세스를 진행시킬 수 있다는 점
2. 다중 에이전트를 비동기 호출 하여 Gradio 앱에서 yield를 통해 실시간으로 보여줄 수 있다는 점
3. 에이전트 중에서도 실제로 Search 에서는 asyncio 를 통해 병렬처리를 구현할 수 있다는 점
4. asyncio.gather vs asyncio.as_completed 차이점  
    asyncio.gather - 모두 완료될 때까지 대기, asyncio.as_completed 완료되는 대로 처리
    ```python
    
    # 모든 작업이 끝날 때까지 기다림
    results = await asyncio.gather(
        search(item1),  # 5초
        search(item2),  # 3초
        search(item3),  # 7초
    )
    # 7초 후 한 번에 [result1, result2, result3] 반환
    print(results)  # 순서 보장됨

    asyncio.as_completed - 완료되는 대로 처리

    # 완료되는 순서대로 처리
    tasks = [
        asyncio.create_task(search(item1)),  # 5초
        asyncio.create_task(search(item2)),  # 3초
        asyncio.create_task(search(item3)),  # 7초
    ]

    for task in asyncio.as_completed(tasks):
        result = await task
        print(result)  # 3초 → 5초 → 7초 순으로 출력
    ```

---

## 📚 References

[1] **Udemy - AI Engineer Agentic Track**
- https://www.udemy.com/course/the-complete-agentic-ai-engineering-course