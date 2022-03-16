---
layout: post
title: "Spring5 프로그래밍 (Chapter 6~8)"
author: "Bys"
category: it_book
date: 2022-03-16 01:00:00
tags: programming spring
---

### 6. 빈 라이프사이클과 범위  

#### 컨테이너 초기화와 종료  
스프링 컨테이너는 초기화와 종료라는 라이프사이클을 갖는다. 

```Java
// 1. 컨테이너 초기화
AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppContext.class);

// 2. 컨테이너에서 빈 객체를 구해서 사용
Greeter g = ctx.getBean("greeter", Greeter.class);
String msg = g.greet("스프링");
System.out.println(msg));

// 3. 컨테이너 종료
ctx.close();
```
위 코드를 보면 AnnotationConfigApplicationContext의 생성자를 이용해서 컨텍스트 객체를 생성하엿는데 이 시점에 스프링 컨테이너를 초기화한다. 
스프링 컨테이너는 설정 클래스에서 정보를 읽어와 알맞은 빈 객체를 생성하고 각 빈을 연결(의존 주입)하는 작업을 수행한다.  

컨테이너 초기화가 완료되면 컨테이너를 사용할 수 있다. 컨테이너를 사용한다는 것은 getBean()과 같은 메서드를 이용해서 컨테이너에 보관된 빈 객체를 구한다는 것을 뜻 한다.  

컨테이너 사용이 끝나면 컨테이너를 종료한다. 컨테이너를 종료할 때 사용하는 메서드가 close() 메서드이다. 
close() 메서드는 AbstractApplicationContext 클래스에 정의되어 있다. 
자바 설정을 사용하는 AnnotationConfigApplicationContext 클래스나 XML설정을 사용하는 GenericXmlApplicationContext 클래스 모두 AbstractApplicationContext클래스를 상속받고 있다. 
따라서 앞서 코드처럼 close() 메서드를 이용해서 컨테이너를 종료할 수 있다.  

컨테이너를 초기화하고 종료할 때에는 다음의 작업도 함께 수행한다. 
- 컨테이너 초기화 -> 빈 객체의 생성, 의존 주입, 초기화
- 컨테이너 종료 -> 빈 객체의 소멸 

스프링 컨테이너의 라이프사이클에 따라 빈 객체도 자연스럽게 생성과 소멸이라는 라이프사이클을 갖는다.  

#### 스프링 빈 객체의 라이프사이클  
스프링 컨테이너는 빈 객체의 라이프사이클을 관리한다. 컨테이너가 관리하는 빈 객체의 라이프사이클은 아래와 같다. 
| :-: |
|- 객체 생성 -> |







<br><br><br>

---

- 출처  
초보 웹 개발자를 위한 스프링 5 (최범균)

---
