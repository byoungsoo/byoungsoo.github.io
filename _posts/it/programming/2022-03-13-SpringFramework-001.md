---
layout: post
title: "Spring Framework Basic 01"
author: "Bys"
category: programming
date: 2022-03-13 01:00:00
tags: programming spring
---

## 스프링 시작하기

### 1. Spring Framework 시작하기

스프링은 객체를 생성하고 초기화하는 기능을 제공하는데, 아래의 코드가 한 개 객체를 생성하고 초기화하는 설정을 담고 있다.  

`Greeter`
```Java
public class Greeter {
    private String format;

    public String greet(String guest) {
        return String.format(format, guest);
    }

    public void setFormat(String format) {
        this.format = format;
    }

}
```
<br>

`AppContext`
```Java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppContext {

	@Bean
	public Greeter greeter() {
		Greeter g = new Greeter();
		g.setFormat("%s, 안녕하세요!");
		return g;
	}
}
```
스프링이 생성하는 객체를 빈(Bean) 객체라고 부르는데, 이 빈 객체에 대한 정보를 담고 있는 메서드가 greeter() 메서드이다. 
이 메서드에는 @Bean 어노테이션이 붙어 있다. @Bean 어노테이션을 메서드에 붙이면 해당 메서드가 생성한 객체를 스프링이 관리하는 빈 객체로 등록한다.  
@Bean 어노테이션을 붙인 메서드는 객체를 생성하고 알맞게 초기화해야 한다. 위 코드에서는 g.setFormat 에서 Greeter 객체를 초기화하고 있다.  

<br>

`Main`
```Java
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx =
				new AnnotationConfigApplicationContext(AppContext.class);
		Greeter g = ctx.getBean("greeter", Greeter.class);
		String msg = g.greet("스프링");
		System.out.println(msg);
		ctx.close();
	}
}
```
AnnotationConfigApplicationContext 클래스는 자바 설정에서 정보를 읽어와 빈 객체를 생성하고 관리한다.  
AnnotationConfigApplicationContext 객체를 생성할 때 AppContext클래스를 생성자 파라미터로 전달하면, AnnotationConfigApplicationContext는 AppContext에 정의한 @Bean 설정 정보를 읽어와 Greeter 객체를 생성하고 초기화한다.  
getBean() 메서드는 AnnotationConfigApplicationContext가 자바 설정을 읽어와 생성한 빈 객체를 검색할 때 사용된다. 
getBean() 메서드의 첫 번째 파라미터는 @Bean 어노테이션의 메서드 이름인 빈 객체의 이름이며, 두 번째 파라미터는 검색할 빈 객체의 타입이다.  

따라서 g 에는 AppContext 설정에 따라 생성한 Greeter 객체가 들어가게 되며, 메인을 실행하면 "스프링, 안녕하세요" 가 출력되게 된다.

<br><br>

### 2. Spring은 객체 컨테이너
위 코드에서 핵심은 AnnotationConfigApplicationContext 클래스다. 
스프링의 핵심 기능은 객체를 생성하고 초기화 하는 것이다. 이와 관련된 기능은 ApplicationContext라는 인터페이스에 정의되어 있다. 
AnnotationConfigApplicationContext 클래스는 ApplicationContext 인터페이스를 알맞게 구현한 클래스 중 하나다. 이 클래스는 자바 클래스에서 정보를 읽어와 객체 생성과 초기화를 수행한다. 
XML파일이나 그루비 설정 코드를 이용해서 객체 생성/초기화를 수행하는 클래스도 존재한다.  

어떤 구현 클래스를 사용하든, 각 구현 클래스는 설정 정보로부터 빈(Bean)이라고 불리는 객체를 생성하고 그 객체를 내부에 보관한다. 그리고 getBean() 메서드를 실행하면 해당하는 빈 객체를 제공한다.  

ApplicationContext(또는 BeanFactory)는 빈 객체의 생성, 초기화, 보관, 제거 등을 관리하고 있어서 ApplicationContext를 컨테이너(Container)라고도 부른다. 

스프링 컨테이너(ApplicationContext)는 내부적으로 빈 객체와 빈 이름을 연결하는 정보를 갖는다.  



