---
layout: post
title: "Middleware의 개념과 분류"
author: "Bys"
category: temp
date: 2019-04-11 11:00:00
tags: middleware
---

>
#### Middleware란 ?
------

분산 컴퓨팅 환경에서 Application이나 데이터베이스, 운영체제, 네트워크 통신 계층 사이에 위치하여 통신을 담당하는 소프트웨어다.  
일반적인 미들웨어는 분산시스템 환경에서 서로 다른 시스템에 올라간 Application간의 어떻게 통신할지에 대한 고민의 결과라고 본다.  

#### Middleware의 분류  

미들웨어는 다양한 방식으로 분류할 수 있다.

- **- Basic Middleware**
<br>

#### 1. Data Management Middleware
SQL, API 등을 통해 원격지 DBMS 또는 파일을 Access  
<br>

#### 2. Communication Middleware
분산된 응용 프로그램들의 각 Component들간에 통신을 담당하는 Middleware로 RPC(Remote Procedure Call), MQ(Message Queue) 등을 통해 원격지의 응용프로그램을 호출하거나 메시지 송/수신 등을 담당.  RPC는 Tightly Coupled, MQ는 Loosely Coupled 방식을 사용  
<br>

#### 3. Platform Middleware
Platform Middleware는 응용프로그램의 실행을 관리하기 위한 Middleware로, Data Source에 대한 Transaction관리, Session관리, 부하분산, Naming Service등을 담당. WAS, TP Monitor가 해당 된다.  
<br>


- **- Integration Middleware**
<br>

#### 1. EAI (Enterprise Application Integration)
특정 환경에 맞추어 구성된 Application, 그리고 Business Application들이 서로 이해할 수 있는 포맷과 내용으로 표현된 Business정보를 교환하는 것을 가능하게 하는 기술과 프로세스의 조합

![middleware2](/assets/it/middleware/middleware/middleware2.jpg)

인사시스템과 M/F 들은 서로 다른 Chracter Encoding 으로 관리되고 있으며, 이들이 주로 사용하는 타 시스템 연계 방식 역시, FTP, Socket, DB Link 등 다양하다고 가정한다면, 인사시스템은 서로 다른 3개의 시스템과 서로 다른 프로토콜, 코드관리 프로그램, Business Logic등을 처리하기 위한 프로그램이 다양하게 존해해야 한다.  

![middleware3](/assets/it/middleware/middleware/middleware3.jpg)


각 시스템들은 자신들이 관리하는 Chracter Encoding Type으로 EAI를 통한 방식으로 통신을 수행하면 되고, EAI에서는 서로 다른 시스템과의 Routing 등을 수행하게 된다. 필요 시에는 송/수신하는 데이터에 추가적인 정보를 포함시키거나 원래의 데이터를 나누어 시스템에 전송해주는 기능을 수행해 줄 수 있다.  

**EAI를 도입하게 되면 일관되게 I/F 관리 할 수 있으며, 시스템간 연계를 보다 단순하게 수행할 수 있는 메커니즘을 제공 해 준다.**
