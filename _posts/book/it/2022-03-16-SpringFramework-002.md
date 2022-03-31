---
layout: post
title: "Spring 프로그래밍 (Chapter 6)[빈 라이프사이클과 범위]"
author: "Bys"
category: it_book
date: 2022-03-16 01:00:00
tags: book programming spring framework lifecycle @bean @scope
---

## 6. 빈 라이프사이클과 범위  

### 6.1 컨테이너 초기화와 종료  
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

<br>

### 6.2 스프링 빈 객체의 라이프사이클  
스프링 컨테이너는 빈 객체의 라이프사이클을 관리한다. 컨테이너가 관리하는 빈 객체의 라이프사이클은 아래와 같다.  
- 객체 생성 -> 의존 설정 -> 초기화 -> 소멸

스프링 컨테이너를 초기화 할 때 가장 먼저 빈 객체를 생성하고 의존을 설정한다. 
의존 자동 주입을 통한 의존 설정이 이 시점에 수행된다. 모든 의존 설정이 완료되면 빈 객체의 초기화를 수행한다. 
빈 객체를 초기화하기 위해 스프링은 빈 객체의 지정된 메서드를 호출한다.  

스프링 컨테이너를 종료하면 스프링 컨테이너는 빈 객체의 소멸을 처리한다. 이때에도 지정한 메서드를 호출한다.  

<br>

#### 6.2.1 빈 객체의 초기화와 소멸: 스프링 인터페이스 
스프링 컨테이너는 빈 객체를 초기화하고 소멸하기 위해 빈 객체의 지정한 메서드를 호출한다.
- org.springframework.beans.factory.InitializingBean
- org.springframework.beans.factory.DisposableBean

```Java
public interface InitializingBean {
    void afterPropertiesSet() throws Exception;
}

public interface DisposableBean {
    void destroy() throws Exception;
}
```
빈 객체가 InitializingBean 객체를 구현하면 스프링 컨테이너는 초기화 과정에서 빈 객체의 afterPropertiesSet() 메서드를 실행한다. 
빈 객체를 생성한 뒤에 초기화 과정이 필요하면 InitializingBean 인터페이스를 상속하고 afterPropertiesSet() 메서드를 알맞게 구현하면 된다. 

스프링 컨테이너는 빈 객체가 DisposableBean 인터페이스를 구현한 경우 소멸 과정에서 빈 객체의 destroy() 메서드를 실행한다. 
빈 객체의 소멸 과정이 필요하면 DisposableBean 인터페이스를 상속하고 destroy() 메서드를 알맞게 구현하면 된다.  

초기화와 소멸 과정이 필요한 예가 데이터베이스 커넥션 풀이다. 커넥션 풀을 위한 빈 객체는 초기화 과정에서 데이터베이스 연결을 생성한다. 
컨테이너를 사용하는 동안 연결을 유지하고 빈 객체를 소멸할 때 사용중이 데이터베이스 연결을 끊어야 한다.  

<br>

`Client`  
```Java
package spring;

public class Client implements InitializingBean, DisposableBean {

	private String host;

	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("Client.afterPropertiesSet() 실행");
	}

	public void send() {
		System.out.println("Client.send() to " + host);
	}

	@Override
	public void destroy() throws Exception {
		System.out.println("Client.destroy() 실행");
	}

}
```

`AppCtx`  
```Java
@Configuration
public class AppCtx {

	@Bean
	public Client client() {
		Client client = new Client();
		client.setHost("host");
		return client;
	}
    
	@Bean(initMethod = "connect", destroyMethod = "close")
	public Client2 client2() {
		Client2 client = new Client2();
		client.setHost("host");
		return client;
	}
}
```

`Main`  
```Java
package main;

public class Main {

	public static void main(String[] args) throws IOException {
		AbstractApplicationContext ctx = 
				new AnnotationConfigApplicationContext(AppCtx.class);

		Client client = ctx.getBean(Client.class);
		client.send();

		ctx.close();
	}

}
```

`Output`  
```log
Client.afterPropertiesSet() 실행
Client.send() to host
Client.destroy() 실행
```

실행 순서를 보면 Main함수의 AnnotationConfigApplicationContext 객체를 생성할 때 AppContext클래스를 생성자 파라미터로 전달한다. 
AnnotationConfigApplicationContext 객체는 AppCtx 클래스에 정의한 @Bean 설정 정보를 읽어와 Client객체를 생성하고 초기화한다. 

초기화 과정에서 afterPropertiesSet() 메서드를 실행했다. 스프링 컨테이너는 빈 객체 생성을 마무리한 뒤에 초기화 메서드를 실행한다. 가장 마지막에 destroy()메서드를 실행했다. 
이 메서드는 스프링 컨테이너를 종료하면 호출이된다. Main함수에서 ctx.close() 코드가 없다면 컨테이너의 종료 과정을 수행하지 않기 때문에 빈 객체의 소멸 과정도 실행되지 않는다.  

<br>

#### 6.2.2 빈 객체의 초기화와 소멸: 커스텀 메서드
InitializingBean, DisposableBean 인터페이스를 사용하고 싶지 않은 경우에는 스프링 설정에서 직접 메서드를 지정할 수 있다. 
@Bean 태그에서 initMethod 속성과 destroyMethod 속성을 사용해서 초기화 메서드와 소멸 메서드의 이름을 지정하면 된다.  

`AppCtx`  
```Java
package config;

@Configuration
public class AppCtx {
    
	@Bean(initMethod = "connect", destroyMethod = "close")
	public Client2 client2() {
		Client2 client = new Client2();
		client.setHost("host");
//		client.connect();  initMethod="connect"
		return client;
	}
}
```

`Client2`  
```Java
package spring;

public class Client2 {

	private String host;

	public void setHost(String host) {
		this.host = host;
	}
	public void connect() {
		System.out.println("Client2.connect() 실행");
	}
	public void send() {
		System.out.println("Client2.send() to " + host);
	}
	public void close() {
		System.out.println("Client2.close() 실행");
	}

}

```
Client2 클래스를 빈으로 사용하려면 초기화 과정에서 connect()메서드를 실행하고 소멸 과정에서 close()메서드를 실행해야 한다면 
위 코드와 같이 @Bean 어노테이션의 initMethod 속성과 destroyMethod 속성에 초기화와 소멸 과정에서 사용할 메서드 이름이 connect와 close를 지정해주기만 하면 된다.  

<br>

### 6.3 빈 객체의 생성과 관리 범위
2장에서 우리는 스프링 컨테이너는 빈 객체를 한 개만 생성한다고 했다. 
```Java
Client client1 = ctx.getBean("client", Client.class);
Client client2 = ctx.getBean("client", Client.class);
// client1 == client2 => true 
```
이렇게 한 식별자에 대해 한 개의 객체만 존재하는 빈은 싱글톤(Singleton) 범위(Scope)를 갖는다. 별도 설정을 하지 않으면 빈은 싱글톤 범위를 갖는다.  
사용 빈도가 낮긴 하지만 프로토타입 범위의 빈을 설정할 수도 있다. 빈의 범위를 프로토타입으로 지정하면 빈 객체를 구할 때마다 매번 새로운 객체를 생성한다. 

```Java
@Configuration
public class AppCtxWithPrototype{
	@Bean
	@Scope("prototype")
	public Client client(){
		Client client = new Client();
		client.setHost("host");
		return client;
	}
}
```
특정 빈을 프로토타입 범위로 지정하려면 다음과 같이 값으로 "prototype"을 갖는 @Scope 어노테이션을 @Bean 어노테이션과 함께 사용하면 된다. 
싱글톤 범위를 명시적으로 지정하고 싶다면 @Scope어노테이션의 값으로 "singleton"을 주면 된다. @Scope("singleton")  

프로토타입 범위를 갖는 빈은 와전한 라이프사이클을 따르지 않는다는 점에 주의해야 한다. 
스프링 컨테이너는 프로토타입 빈 객체를 생성하고 프로퍼티를 설정하고 초기화 작업까지는 수행하지만, 컨테이너를 종료한다고 해서 생성한 프로토타입 빈 객체의 소멸 메서드를 실행하지는 않는다. 
따라서 프로토타입 범위의 빈을 사용할 때에는 빈 객체의 소멸 처리를 코드에서 직접 해야 한다.  



<br><br><br>

---

- 출처  
초보 웹 개발자를 위한 스프링 5 (최범균)

---
