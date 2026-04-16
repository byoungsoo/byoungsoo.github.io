---
slug: lang-graph
author: Bys
categories:
- ml
category: ml
date: '2026-04-06 01:00:00'
draft: true
keywords: keyword1, keyword2, keyword3
tags:
- ml
- langgraph
- agent
title: LangGraph
---


# [LangGraph]()  
LLM 기반 애플리케이션을 그래프(Graph) 구조로 설계할 수 있게 해주는 프레임워크 

### 용어
- **State**: State represents the current snapshot of the application.
  - State is Immutable
- **Node**: Nodes are python functions that represent agent logic. Receive the **current state** as input, do something, and return an **updated state**.
- **Edge**: Edges are python functions that determine which node to execute next based on the state. They can be conditional or fixed

<img src="/assets/it/ml/agents/lang-graph001.png" alt="lang-graph001" style="width: 30%; height: auto;">

**Nodes** do the work. **Edges** choose what to do next 


## 1. []()  

## 2. [Test]()  

```python
import sqlite3
from langgraph.checkpoint.sqlite import SqliteSaver
from typing import Annotated
from langgraph.graph import StateGraph, START, END
from langgraph.graph.message import add_messages
from dotenv import load_dotenv
from IPython.display import Image, display
import gradio as gr
from langgraph.prebuilt import ToolNode, tools_condition
import requests
import os
from langchain_openai import ChatOpenAI
from langchain_aws import ChatBedrock
from typing import TypedDict
from langchain_community.utilities import GoogleSerperAPIWrapper
from langchain_core.tools import Tool


# Our favorite first step! Crew was doing this for us, by the way.
load_dotenv(override=True)

# Var
pushover_token = os.getenv("PUSHOVER_TOKEN")
pushover_user = os.getenv("PUSHOVER_USER")
pushover_url = "https://api.pushover.net/1/messages.json"


# Tools
serper = GoogleSerperAPIWrapper()
tool_search =Tool(
        name="search",
        func=serper.run,
        description="Useful for when you need more information from an online search"
    )

def push(text: str):
    """Send a push notification to the user"""
    requests.post(pushover_url, data = {"token": pushover_token, "user": pushover_user, "message": text})
tool_push = Tool(
        name="send_push_notification",
        func=push,
        description="useful for when you want to send a push notification"
    )

tools = [tool_search, tool_push]

# Memory
db_path = "memory.db"
conn = sqlite3.connect(db_path, check_same_thread=False)
sql_memory = SqliteSaver(conn)


# Step 1: Define the State object
class State(TypedDict):
    messages: Annotated[list, add_messages]

# Step 2: Start the Graph Builder with this State class
graph_builder = StateGraph(State)

# Step 3: Create a

# llm = ChatOpenAI(model="gpt-4o-mini")
llm = ChatBedrock(model_id="us.anthropic.claude-opus-4-6-v1", region_name="us-east-1")
llm_with_tools = llm.bind_tools(tools)

def chatbot(state: State):
    print(state)
    return {"messages": [llm_with_tools.invoke(state["messages"])]}


graph_builder.add_node("chatbot", chatbot)
graph_builder.add_node("tools", ToolNode(tools=tools))


# Step 4: Create Edges
graph_builder.add_conditional_edges( "chatbot", tools_condition, "tools")
graph_builder.add_edge("tools", "chatbot")
graph_builder.add_edge(START, "chatbot")


# Step 5: Compile the Graph
graph = graph_builder.compile(checkpointer=sql_memory)
display(Image(graph.get_graph().draw_mermaid_png()))


config = {"configurable": {"thread_id": "3"}}

def chat(user_input: str, history):
    result = graph.invoke({"messages": [{"role": "user", "content": user_input}]}, config=config)
    return result["messages"][-1].content


gr.ChatInterface(chat).launch()

```


## 3. []()  

## 4. []()  

## 5. []()  


---

## 📚 References

[1] **제목** - 설명  
- https://example.com