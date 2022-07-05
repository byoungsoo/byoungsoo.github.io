---
layout: post
title: "Elastic APM"
author: "Bys"
category: solution
date: 2022-04-21 01:00:00
tags: h2 db
---

## Elastic APM

### 1. Elastic APM 이란  
APM은 Application Performance Monitoring으로 Application에 대한 성능정보 및 호출이력 JVM 리소스 모니터링 등 기본적인 Metric 정보를 확인할 수 있다.  
다양한 APM이 존재하지만 이번에 Elastic APM을 사용한 이유는 구성 환경 중 이미 EFK를 활용하여 로깅시스템을 구축하였고, Kibana와 통합된 화면으로 APM정보를 확인 할 수 있다는 점에서 사용을 해보게 되었다. 

![elastic-apm001.png](/assets/it/solution/elastic-apm/elastic-apm001.png){: width="80%" height="auto"}  

### 2. Elastic-APM의 구성요소  

![elastic-apm002.png](/assets/it/solution/elastic-apm/elastic-apm002.png){: width="80%" height="auto"}  

실제로 Beats, Logstash와 연동하여 elasticsearch로도 데이터를 전달할 수 있는 아키텍처이며 여기서는 elastic-apm-agents와 elastic-apm-server를 사용하여 구성하였다.  
agent,server 방식에서는 아래와 같은 구성 요소를 갖는다. 

1. **Elastic-apm-agent**  
   elastic-apm-agent는 실제로 사용자가 모니터링할 Application에 심어지며 elastic-apm-server로 Application의 메트릭 정보를 수집하여 apm-server로 전송한다.  

2. **Elastic-apm-server**  
   elastic-apm-server는 사용자 Application의 데이터를 수집하여 Elasticsearch로 전송한다.  

3. **Elasticsearch**  
   elasticsearch는 apm-server로 부터 수집된 데이터를 저장하는 공간이다.  

4. **Kibana**  
   kibana는 elasticsearch에 저장된 메트릭 정보를 사용자가 볼 수 있는 화면이다.  

### 3. Elastic-APM 설치  

1. **APM-Server**  
2. [APM Download](https://www.elastic.co/kr/downloads/apm)에 접속 > 알맞은 OS를 선택한 후 다운로드 받는다. (여기서는 Choose platform: Linux x86_64)
   
   APM서버는 기본 8200포트로 기동이되며 

3. 