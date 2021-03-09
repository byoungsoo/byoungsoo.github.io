---
layout: post
title: "SpringBoot에 APM툴 Scouter 설치하기"
author: "Bys"
category: middleware
date: 2021-03-09 01:00:00
tags: apm scouter opensource oss
---

Scouter는 Open Source로 나와있는 APM툴이다. 


- Scouter Download  
Scouter는 무료 오픈 소스로 아래의 github에서 다운 받을 수 있다.  
https://github.com/scouter-project/scouter/releases/


- Scouter의 동작 방식은 아래와 같이 Agent를 각 WAS에 심고, 각 WAS의 Metric정보를 Host서버가 수집한다. Client는 Clinet툴을 이용하여 Host서버에 붙어 모니터링 정보를 확인 할 수 있다.  
Client ↔︎ Collector(EC2) ↔︎ WAS(VM/Container)  
![Scouter](/assets/it/Scouter.jpg)

- Scouter의 동작을 확인하기 위해서는 즉, 3가지 설치가 필요하다.  

1. Agent (scouter/agent.java)  
scouter/agent.java의 구성은 confg, plugin, scouter.agent.jar로 구성이 되어있다.  
conf파일을 WAS의 적당한 위치에 올리고, agent.jar 파일을 WAS에 심은 후 연동하면 된다.   
<br>

`scouter/agent.java/conf/scouter.conf` 변경
```conf
### scouter java agent configuration sample
obj_name=WAS-01 #WAS_NAME으로 값 변경 후 sed처리 (Container)
net_collector_ip=127.0.0.1 #HOST_IP로 값 변경 후 sed처리 (Container)
net_collector_udp_port=6100
net_collector_tcp_port=6100
#hook_method_patterns=sample.mybiz.*Biz.*,sample.service.*Service.*
#trace_http_client_ip_header_key=X-Forwarded-For
#profile_spring_controller_method_parameter_enabled=false
#hook_exception_class_patterns=my.exception.TypedException
#profile_fullstack_hooked_exception_enabled=true
#hook_exception_handler_method_patterns=my.AbstractAPIController.fallbackHandler,my.ApiExceptionLoggingFilter.handleNotFoundErrorResponse
#hook_exception_hanlder_exclude_class_patterns=exception.BizException
```

`WAS 설정` (WAS와 Agent연동)
```Java
SCOUTER_AGENT_DIR=/usr/local/appServer/scouter/agent.java
JAVA_OPTS=" ${JAVA_OPTS} -javaagent:${SCOUTER_AGENT_DIR}/scouter.agent.jar"
JAVA_OPTS=" ${JAVA_OPTS} -Dscouter.config=${SCOUTER_AGENT_DIR}/conf/scouter.conf"
JAVA_OPTS=" ${JAVA_OPTS} -Dobj_name=spring-api"
```

`Dockerfile` (Container 사용 시)
```Dockerfile
COPY scouter.agent.java /usr/local/appServer/scouter/agent.java
RUN sed -i -e "s#WAS-01#WAS_NAME#g" /usr/local/appServer/scouter/agent.java/conf/scouter.conf 
RUN sed -i -e "s#HOST_IP#HOST_IP#g" /usr/local/appServer/scouter/agent.java/conf/scouter.conf 
```




2. Host

3. Clinet
