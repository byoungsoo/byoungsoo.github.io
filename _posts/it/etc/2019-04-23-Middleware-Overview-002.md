---
layout: post
title: "Middleware 솔루션"
author: "Bys"
category: etc
tags: middleware
---

>
## Spring Boot

Spring을 사용하여 웹 개발을 시작하려고 했을 때가 있다. 나는 여러 웹 사이트에서 스프링 프레임워크를 통해, 빠른 Java 엔터프라이즈 개발을 할 수 있다고 보고 믿었다.  
<br/><br/>
하지만, 기초가 부족했던 내게 웹 개발은 머나먼 이야기였고, 개발을 시작하기도 전에 프레임워크 설정에서 애를 많이 먹었다.  
Framework이라고 하여 어느정도의 형태를 제공해주고 간단한 수정만 하면 될 줄 알았다. 하지만 실제로는 엄청난 양의 xml설정과 라이브러리 등등.. 정말 할 것이 많았다.  
<br/><br/>

Spring Boot는 스프링의 여러 프로젝트 중 하나로, 위에서 언급한 Spring의 단점들을 보완해서 실행 할 수 있는 솔루션이다.  
Spring Boot는 아래와 같은 특징이 존재한다.

- 가장 많이 사용되는 자바 프레임워크
- 자체적으로 Servlet Container를 내장하고 있다.
  + 자바 환경만 갖춰져 있다면 별도 WAS 없이도 웹 애플리케이션을 실행할 수 있다.
  + 애플리케이션 서버에 war 파일을 별도로 배포할 필요가 없다.
- Java 솔루션으로 OS로부터 독립적이다. (Dependency가 없다.)
- Starter POM 파일로 인하여 Maven 설정이 쉽다.
- 최근 트렌드인 MSA 형태의 프로젝트 수행해 유용하다.

살펴보면, 스프링부트는 스프링 프레임워크를 더 빠르고 쉽게 사용할 수 있는 프레임워크라고 볼 수 있겠다.  




> Web Service 종류

## SOAP(Simple Object Access Protocol)
>SOAP은 HTTP, HTTPS, SMTP 등을 사용하여 XML 기반의 메세지를 컴퓨터 네트워크 상에서 교환하는 형태의 프로토콜.

SOA(Service Oriented Architecture)를 구현하기 위한 기술.  
SOAP은 아래와 같이 SOAP Envelope, SOAP Header, SOAP Body로 구성된 하나의 XML 문서로 표현되어 전달된다.

```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
<SOAP-ENV:Body>
<getProductDetails xmlns="http://warehouse.example.com/ws">
<productId>123456</productId>
</getProductDetails>
</SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

- 장점
1. SOAP은 플랫폼과 프로그래밍 언어에 독립적이다.
2. SOAP은 확장성이 좋다.  

- 단점
1. REST보다 어렵고, 무겁다.
2. REST보다 개발이 어렵다.  

== 같이보기 ==  
- WSDL(Web Services Description Language): Web Service 기술언어 또는 기술된 정의 파일의 총칭으로 XML로 기술 된다.  
- UDDI(Universal Description, Discovery, and Intergration): 웹 서비스 관련 정보의 공개와 탐색을 위한 표준이다.  

## REST(Representational State Transfer)
>REST는 WWW와 같은 분산 하이퍼미디어 시스템을 위한 소프트웨어 아키텍처의 한 형식. 웹 상의 자료를 HTTP위에서 SOAP이나 쿠키를 통한 세션 트랙킹 같은 별도의 전송 계층 없이 전송하기 위한 아주 간단한 인터페이스다.

자원(Resource)의 표현 상태(Representational State)를 주고 받는 것. HTTP URI를 통해 자원을 명시하고, HTTP Method(POST, GET, PUT, DELETE)를 통해 해당 자원에 대한 CRUD를 적용한다.  

- 클라이언트/서버 구조: 일관적인 인터페이스로 분리되어야 한다
- 무상태(Stateless): 각 요청 간 클라이언트의 콘텍스트가 서버에 저장되어서는 안 된다
- 캐시 처리 가능(Cacheable): WWW에서와 같이 클라이언트는 응답을 캐싱할 수 있어야 한다.
- 잘 관리되는 캐싱은 클라이언트-서버 간 상호작용을 부분적으로 또는 완전하게 제거하여 scalability와 성능을 향상시킨다.
계층화(Layered System): 클라이언트는 보통 대상 서버에 직접 연결되었는지, 또는 중간 서버를 통해 연결되었는지를 알 수 없다. 중간 서버는 로드 밸런싱 기능이나 공유 캐시 기능을 제공함으로써 시스템 규모 확장성을 향상시키는 데 유용하다.
Code on demand (optional) - 자바 애플릿이나 자바스크립트의 제공을 통해 서버가 클라이언트가 실행시킬 수 있는 로직을 전송하여 기능을 확장시킬 수 있다.
인터페이스 일관성: 아키텍처를 단순화시키고 작은 단위로 분리(decouple)함으로써 클라이언트-서버의 각 파트가 독립적으로 개선될 수 있도록 해준다..
- 장점






1. SOAP, WSDL, UDDI 기반으로 한 Web Service는 최근에는 사용하는 곳이 거의 없어 사장되는 기술이었으나, Open API와 Microservice Architecture 등 서비스 기반의 분산 아키텍처가 활성화되면서 다시 살아나는 추세이다.

2. ESB는 내부 시스템 연계용으로 주로 사용하지만 Front-end 채널 통합용으로 사용하는 경우가 가끔 있다. 그것이 가능한 이유는 대부분의 ESB 솔루션이 전문변환, 오케스트레이션, 개시/마감 전문처리, 결번처리 관리 등 Front-end에 필요한 기능을 가지고 있기 때문이다.

3. EAI는 태생이 서비스 기반 솔루션으로 업무 프로세스를 진행하기 위한 오케스트레이션 기능이 강하고 표준 인터페이스와 프로토콜을 사용하여 호환성이 우수하다.

4. TP-Monitor는 개별 서비스 요청이 프로세스 단위로 실행되어 단일 프로세스 장애 시 타 프로세스에 영향이 없을 뿐 아니라 개별 서비스 단위로 실행 타임아웃, 최대 동시 실행 개수, 비정상 종료 시 최대 재기동 회수 등의 설정이 가능해 WAS에 비해 개별 서비스에 대한 제어 기능이 우수하다.


*참고
1. https://ko.wikipedia.org/wiki/REST
2. https://gmlwjd9405.github.io/2018/09/21/rest-and-restful.html
