---
layout: post
title: "Spring 프로그래밍 (Chapter 9~10)[스프링 MVC]"
author: "Bys"
category: dev
date: 2022-03-20 01:00:00
tags: book programming spring framework mvc
---

## 9. 스프링 MVC 시작하기  
스프링을 사용하는 여러 이유가 있지만 한 가지 이유를 꼽자면 스프링이 지원하는 웹 MVC 프레임워크 때문이다. 
스프링 MVC의 설정 방법만 익혀두면 웹 개발에 필요한 다양한 기능을 구현할 수 있게 된다. 

### 9.1 프로젝트 생성  
- src/main/java
- src/main/webapp
- src/main/webapp/WEB-INF
- src/main/webapp/WEB-INF/view

> 서블릿 스펙에 따르면 WEB-INF 폴더의 하위 폴더로 lib 폴더와 classes 폴더를 생성하고 각각의 폴더에 필요한 jar 파일과 컴파일 된 클래스 파일이 위치해야 한다. 

<br>

### 9.3 스프링 MVC를 위한 설정  

- 스프링 MVC의 주요 설정 (HandlerMapping, ViewResolver 등)
- 스프링의 DispatcherServlet 설정

<br>

#### 9.3.1 스프링 MVC 설정  
이 장에서 사용할 스프링 MVC설정은 아래와 같다. 
```Java
package config;

@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		registry.jsp("/WEB-INF/view/", ".jsp");
	}

}
```
위 설정을 간단하게 설명하면 다음과 같다.  
- @EnableWebMvc 어노테이션은 스프링 MVC설정을 활성화한다. 스프링 MVC를 사용하는데 필요한 다양한 설정을 생성한다. 
- DispatcherServlet의 매핑 경로를 '/'로 주었을 때, JSP/HTML/CSS 등을 올바르게 처리하기 위한 설정을 추가한다. 
- JSP를 이용해서 컨트롤러의 실행 결과를 보여주기 위한 설정을 추가한다.  

@EnableWebMvc 어노테이션을 사용하면 내부적으로 다양한 빈 설정을 추가해준다. 이 설정을 직접하려면 수십 줄에 가까운 코드를 작성해야 한다.  
@EnableWebMvc 어노테이션이 스프링 MVC를 사용하는데 필요한 기본적인 구성을 설정해준다면, WebMvcConfigure 인터페이스는 스프링 MVC의 개별 설정을 조정할 때 사용한다. 

configureDefaultServletHandling() 메서드와 configureViewResolvers() 메서드는 WebMvcConfigure 인터페이스에 정의된 메서드로 각각 default 서블릿과 ViewResolver와 관련된 설정을 조정한다.  

<br>

#### 9.3.2 web.xml 파일에 DispatcherServlet 설정  
스프링 MVC가 웹 요청을 처리하려면 DispatcherServlet을 통해서 웹 요청을 받아야 한다. 이를 위해 web.xml 파일에 DispatcherServlet을 등록한다. 
사용할 web.xml 파일은 다음과 같다. 

```xml
<?xml version="1.0" encoding="UTF-8"?>

<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee 
             http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
	version="3.1">

	<servlet>
		<servlet-name>dispatcher</servlet-name>
		<servlet-class>
			org.springframework.web.servlet.DispatcherServlet
		</servlet-class>
		<init-param>
			<param-name>contextClass</param-name>
			<param-value>
				org.springframework.web.context.support.AnnotationConfigWebApplicationContext
			</param-value>
		</init-param>
		<init-param>
			<param-name>contextConfigLocation</param-name>
			<param-value>
				config.MvcConfig
				config.ControllerConfig
			</param-value>
		</init-param>
		<load-on-startup>1</load-on-startup>
	</servlet>

	<servlet-mapping>
		<servlet-name>dispatcher</servlet-name>
		<url-pattern>/</url-pattern>
	</servlet-mapping>

	<filter>
		<filter-name>encodingFilter</filter-name>
		<filter-class>
			org.springframework.web.filter.CharacterEncodingFilter
		</filter-class>
		<init-param>
			<param-name>encoding</param-name>
			<param-value>UTF-8</param-value>
		</init-param>
	</filter>
	<filter-mapping>
		<filter-name>encodingFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

</web-app>
```
- DispatcherServlet을 dispatcher라는 이름으로 등록한다.
- contextClass 초기화 파라미터를 설정한다. 자바 설정을 사용하는 경우 AnnotationConfigWebApplicationContext클래스를 사용한다. 
  이 클래스는 자바 설정을 이용하는 웹 어플리케이션 용 스프링 컨테이너 클래스이다. 
- contextConfiguration 초기화 파라미터의 값을 지정한다. 이 파라미터에는 스프링 설정 클래스 목록을 지정한다. 각 설정 파일의 경로는 줄바꿈이나 콤바로 구분한다. 
- \<load-on-startup\>1\</load-on-startup\> 톰캣과 같은 컨테이너가 웹 어플리케이션을 구동할 때 이 서블릿을 함께 실행하도록 설정한다.  
- 모든 요청을 DispatcherServlet이 처리하도록 서블릿 매핑을 설정한다. 
- HTTP 요청 파라미터의 인코딩 처리를 위한 서블릿 필터를 등록한다. 스프링은 인코딩 처리를 위한 필터인 CharacterEncodingFilter 클래스를 제공한다.  

<br>

DispatcherServlet은 초기화 과정에서 contextConfiguration 초기화 파라미터에 지정한 설정 파일을 이용해서 스프링 컨테이너를 초기화한다.  
*즉, 위의 설정은 MvcConfig 클래스와 ControllerConfig 클래스를 이용해서 스프링 컨테이너를 생성한다.*  

<br>

### 9.4 코드 구현

#### 9.4.1 컨트롤러 구현  

```Java
package chap09;

@Controller
public class HelloController {

	@GetMapping("/hello")
	public String hello(Model model,
			@RequestParam(value = "name", required = false) String name) {
		model.addAttribute("greeting", "안녕하세요, " + name);
		return "hello";
	}
}
```
- @Controller 어노테이션을 적용한 클래스는 스프링 MVC에서 컨트롤러로 사용한다. 
- @GetMapping 어노테이션은 메서드가 처리할 요청 경로를 지정한다. 
- Model 파라미터는 컨트롤러의 처리 결과를 뷰에 전달할 때 사용한다. 
- @RequestParam 어노테이션은 HTTP 요청 파라미터의 값을 메서드의 파라미터로 전달할 때 사용된다. 

스프링 MVC 프레임워크에서 컨트롤러란 간단히 설명하면 웹 요청을 처리하고 그 결과를 뷰에 전달하는 스프링 빈 객체이다. 
스프링 컨트롤러로 사용될 클래스는 @Controller 어노테이션을 붙여야 하고, @GetMapping 어노테이션이나 @PostMapping 어노테이션과 같은 요청 매핑 어노테이션을 이용해서 처리할 경로를 지정해 주어야 한다.  

@RequestParam 어노테이션은 HTTP 요청 파라미터를 메서드의 파라미터로 전달받을 수 있게 해 준다. 
@RequestParam 어노테이션의 value 속성은 HTTP 요청 파라미터의 이름을 지정하고 required 속성은 필수 여부를 지정한다.  

Model.addAttribute() 메서드의 첫 번째 파라미터는 데이터를 식별하는데 사용되는 속성 이름이고 두 번째 파라미터는 속성 이름에 해당하는 값이다. 
뷰 코드는 이 속성 이름을 사용해서 컨트롤러가 전달한 데이터에 접근하게 된다.  

마지막으로 @GetMapping이 붙은 메서드는 컨트롤러의 실행 결과를 보여줄 뷰 이름을 리턴한다. 


#### 9.4.2 JSP 구현  

```Java
package config;

@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		registry.jsp("/WEB-INF/view/", ".jsp");
	}
}
```
registry.jsp() 코드는 JSP를 뷰 구현으로 사용할 수 있도록 해주는 설정이다. 
jsp() 메서드의 첫 번째 인자는 JSP 파일 경롤를 찾을 때 사용하는 접두어이며 두 번째 인자는 접미사이다. 


<br>


## 10. 스프링 MVC 프레임워크 동작 방식  

### 10.1 스프링 MVC 핵심 구성 요소 

스프링 MVC 핵심 구성 요소와 각 요소 간의 관계는 아래와 같이 정리할 수 있다. 
이 그림은 매우 중요하므로 설명을 읽을 때 수시로 이 그림을 참조하면 내용을 이해하는데 도움이 된다.  

![spring5_10_1](/assets/book/spring5/spring5_10_1.png){: width="60%" height="auto"}  

그림에서 <<spring bean>> 이라고 표시한 것은 스프링 빈으로 등록해야 하는 것을 의미한다. 
회색 배경을 까진 구성 요소(컨트롤러, JSP)는 개발자가 직접 구현해야 하는 요소이다.  

그림의 중앙에 위치한 DispatcherServlet은 모든 연결을 담당한다. 
웹 브라우저로부터 요청이 들어오면 DispatcherServlet은 그 요청을 처리하기 위한 컨트롤러 객체를 검색한다. 
이 때 DispatcherServlet은 직접 컨트롤러를 검색하지 않고 HandlerMapping이라는 빈 객체에게 검색을 요청한다. (2번 과정)

HandlerMapping은 클라이언트의 요청 경로를 이용해서 이를 처리할 컨트롤러 빈 객체를 DispatcherServlet에 전달한다. 
컨트롤러 객체를 DispatcherServlet이 전달받았다고 해서 바로 컨트롤러 객체의 메서드를 실행할 수 있는 것은 아니다. 
DispatcherServlet은 @Controller 어노테이션을 이용해서 구현한 컨트롤러뿐만 아니라 스프링 2.5까지 주로 사용됐던 Controller 인터페이스를 구현한 컨트롤러,
그리고 특수 목적으로 사용되는 HttpRequestHandler 인터페이스를 구현한 클래스를 동일한 방식으로 실행할 수 있도록 만들어졌다. 
@Controller, Controller 인터페이스, HttpRequestHandler 인터페이스를 동일한 방식으로 처리하기 위해 중간에 사용되는 것이 바로 HandlerAdapter 빈이다.  

DispatcherServlet은 HandlerMapping이 찾아준 컨트롤러 객체를 처리할 수 있는 HandlerAdapter 빈에게 요청 처리를 위임한다. (3번 과정)
HandlerAdapter는 컨트롤러의 알맞은 메서드를 호출해서 요청을 처리하고 (4~5번 과정) 그 결과를 DispatcherServlet 리턴한다.(6번 과정)
이 때 HandlerAdapter는 컨트롤러의 처리 결과를 ModelAndView라는 객체로 변환해서 DispatcherServlet에 리턴한다.  

HandlerAdapter로부터 컨트롤러의 요청 처리 결과를 ModelAndView로 받으면 DispatcherServlet은 결과를 보여줄 뷰를 찾기 위해 ViewResolver 빈 객체를 사용한다(7번 과정). 
ModelAndView는 컨트롤러가 리턴한 뷰 이름을 담고 있는데 ViewResolver는 이 뷰 이름에 해당하는 View 객체를 찾거나 생성해서 리턴한다. 
응답을 생성하기 위해 JSP를 사용하는 ViewResolver는 매번 새로운 View 객체를 생성해서 DispatcherServlet에 리턴한다.  

DispatcherServlet은 ViewResolver가 리턴한 View 객체에게 응답 결과 생성을 요청한다(8번 과정). 
JSP를 사용하는 경우 View 객체는 JSP를 실행함으로써 웹 브라우저에 전송할 응답 결과를 생성하고 이로써 모든 과정이 끝이 난다.  

<br>

#### 10.1.1 컨트롤러와 핸들러  
클라이언트의 요청을 실제로 처리하는 것은 컨트롤러이고 DispatcherServlet은 클라이언트의 요청을 전달받는 창구 역할을 한다.  
DispatcherServlet은 클라이언트의 요청을 처리할 컨트롤러를 찾기 위해 HandlerMapping을 사용한다. 

스프링 MVC는 웹 요청을 처리할 수 있는 범용 프레임워크다. 
이 책에서는 @Controller 어노테이션을 붙인 클래스를 이용해서 클라이언트의 요청을 처리하지만 원한다면 자신이 직접 만든 클래스를 이용해서 클라이언트의 요청을 처리할 수도 있다. 
즉 DispatcherServlet 입장에서는 클라이언트 요청을 처리하는 객체의 타입이 반드시 @Controller를 적용한 클래스일 필요는 없다. 

이런 이유로 스프링 MVC 웹 요청을 실제로 처리하는 객체를 핸들러(Handler)라고 표현하고 있으며 @Controller 적용 개체나 Controller 인터페이스를 구현한 객체는 모두 스프링 MVC 입장에서는 핸들러가 된다. 
따라서 특정 요청 경로를 처리해주는 핸드럴를 찾아주는 객체를 HandlerMapping 이라고 부른다.  

DispatcherServlet은 핸들러 객체의 실제 타입에 상관없이 실행 결과를 ModelAndView라는 타입으로만 받을 수 있으면 된다. 
그런데 핸들러의 실제 구현 타입에 따라 ModelAndView를 리턴하는 객체도(Controller 인터페이스를 구현한 클래스의 객체)있고, 그렇지 않은 객체도 있다. 
따라서 핸들러의 처리 결과를 ModelAndView로 변환해주는 객체가 필요하며 HandlerAdapter가 이 변환을 처리해준다.  

핸들러 객체의 실제 타입마다 그에 알맞은 HandlerMapping과 HandlerAdapter가 존재하기 때문에, 사용할 핸들러의 종류에 따라 해당 HandlerMapping과 HandlerAdapter를 스프링 빈으로 등록해야 한다. 
물론 스프링이 제공하는 설정 기능을 사용하면 이 두 종료의 빈을 직접 등록하지 않아도 된다.  

<br>

### 10.2 DispatcherServlet과 스프링 컨테이너  
9장의 web.xml 파일을 보면 다음과 같이 DispatcherServlet의 contextConfiguration 초기화 파라미터를 이용해서 스프링 설정 클래스 목록을 전달했다.  

```xml
<servlet>
	<servlet-name>dispatcher</servlet-name>
	<servlet-class>
		org.springframework.web.servlet.DispatcherServlet
	</servlet-class>
	<init-param>
		<param-name>contextClass</param-name>
		<param-value>
			org.springframework.web.context.support.AnnotationConfigWebApplicationContext
		</param-value>
	</init-param>
	<init-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>
			config.MvcConfig
			config.ControllerConfig
		</param-value>
	</init-param>
	<load-on-startup>1</load-on-startup>
</servlet>
```
DispatcherServlet은 전달받은 설정 파일을 이용해서 스프링 컨테이너를 생성하는데 앞에서 언급한 
HandlerMapping, HandlerAdapter, 컨트롤러, ViewResolver 등의 빈은 아래 그림처럼 DispatcherServlet이 생성한 스프링 컨테이너에서 구한다. 

![spring5_10_2](/assets/book/spring5/spring5_10_2.png){: width="40%" height="auto"}  

<br>

### 10.3 @Controller를 위한 HandlerMapping과 HandlerAdapter  
@Controller 적용 객체는 DispatcherServlet 입장에서 보면 한 종류의 핸들러 객체이다. 
DispatcherServlet은 웹 브라우저의 요청을 처리할 핸들러 객체를 찾기 위해 HandlerMapping을 사용하고 핸들러를 실행하기 위해 HandlerAdapter를 사용한다. 
DispatcherServlet은 스프링 컨테이너에서 HandlerMapping과 HandlerAdapter타입의 빈을 사용하므로 핸들러에 알맞은 HandlerMapping 빈과 HandlerAdapter타입의 빈이 스프링 설정에 등록되어 있어야 한다. 
그런데 9장에서 작성한 예제를 보면 HandlerMapping이나 HandlerAdapter 클래스를 빈으로 등록하는 코드는 보이지 않는다.  
*단지 @EnableWebMvc 어노테이션만 추가했다.*  

이 태그가 빈으로 추가해주는 클래스 중에는 @Controller 타입의 핸들러 객체를 처리하기 위한 다음의 두 클래스도 포함되어 있다.
- org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
- org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter

RequestMappingHandlerMapping은 @Controller 어노테이션이 적용된 객체의 요청 매핑 어노테이션(@GetMapping) 값을 이용해서 웹 브라우저의 요청을 처리할 컨트롤러 빈을 찾는다.  
RequestMappingHandlerAdapter는 컨트롤러 컨트롤러의 메서드를 알맞게 실행하고 그 결과를 ModelAndView 객체로 변환해서 DispatcherServlet에 리턴한다.  

```Java
@Controller
public class HelloController {

	@RequestMapping("/hello")
	public String hello(Model model,
			@RequestParam(value = "name", required = false) String name) {
		model.addAttribute("greeting", "안녕하세요, " + name);
		return "hello";
	}
}
```
RequestMappingHandlerAdapter 클래스는 "/hello" 요청 경로에 대해 hello() 메서드를 호출한다. 
이때 Model 객체를 생성해서 첫 번째 파라미터로 전달한다. 미슷하게 이름이 "name"인 HTTP 요청 파라미터의 값을 두 번째 파라미터로 전달한다. 
RequestMappingHandlerAdapter는 컨트롤러 메서드 결과 값이 String 타입이면 해당 값을 뷰 이름으로 갖는 ModelAndView 객체를 생성해서 DispatcherServlet에 리턴한다. 
이때 첫 번째 파라미터로 전달한 Model 객체에 보관된 값도 ModelAndView에 함께 전달한다. 

<br>

### 10.4 WebMvcConfigure 인터페이스와 설정  
@EnableWebMvc 어노테이션을 사용하면 @Controller 어노테이션을 붙인 컨트롤러를 위한 설정을 생성한다. 
또한 @EnableWebMvc 어노테이션을 사용하면 WebMvcConfigurer 타입의 빈을 이용해서 MVC 설정을 추가로 생성한다. 

```Java
package config;

@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		registry.jsp("/WEB-INF/view/", ".jsp");
	}

}
```
여기서 설정 클래스는 WebMvcConfigurer 인터페이스를 상속하고 있다. 
@Configuration 어노테이션을 붙인 클래스 역시 컨테이너에 빈으로 등록되므로 MvcConfig 클래스는 WebMvcConfigurer 타입의 빈이 된다.  

@EnableWebMvc 어노테이션을 사용하면 WebMvcConfigurer 타입인 빈 객체의 메서드를 호출해서 MVC 설정을 추가한다. 

스프링 5 버전은 자바 8 버전부터 지원하는 디폴트 메서드를 사용해서 WebMvcConfigurer 인터페이스의 메서드에 기본 구현을 제공하고 있다. 
기본 구현은 모두 빈(비어있는) 구현이다. 이 인터페이스를 상속한 설정 클래스는 재정의가 필요한 메서드만 구현하면 된다.  

<br>

### 10.6 디폴트 핸들러와 HandlerMapping의 우선순위  
9장의 web.xml 설정을 보면 DispatcherServlet에 대한 매핑 경로를 다음과 같이 '/'로 주었다.  
```xml
<servlet>
	<servlet-name>dispatcher</servlet-name>
	<servlet-class>
		org.springframework.web.servlet.DispatcherServlet
	</servlet-class>
</servlet>

<servlet-mapping>
	<servlet-name>dispatcher</servlet-name>
	<url-pattern>/</url-pattern>
</servlet-mapping>
```
매핑 경로가 '/'인 경우 .jsp로 끝나는 요청을 제외한 모든 요청을 DispatcherServlet이 처리한다. 
그런데 @EnableWebMvc 어노테이션이 등록하는 HandlerMapping은 @Controller 어노테이션을 적용한 빈 객체가 처리할 수 있는 요청 경로만 대응할 수 있다. 
예를 들어 등록된 컨트롤러가 한 개이고 그 컨트롤러가 @GetMapping("/hello") 설정을 사용한다면 /hello 경로만 처리할 수 있게 된다. 
따라서 "/index.html"이나 "/css/bootstrap.css"와 같은 요청을 처리할 수 있는 컨트롤러 객체를 찾지 못해 DispatcherServlet은 404응답을 전송한다.  

"/index.html"이나 "/css/bootstrap.css"와 같은 요청을 처리할 수 있는 컨트롤러 객체를 직접 구현할 수도 있지만, 
그보다는 WebMvcConfigurer의 configureDefaultServletHandling() 메서드를 사용한것이 편리하다. 

```Java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}
}
```
위 설정에서 DefaultServletHandlerConfigurer#enable() 메서드는 다음의 두 빈 객체를 추가한다.  
- DefaultServletHttpRequestHandler
- SimpleUrlHandlerMapping



DefaultServletHttpRequestHandler는 클라이언트의 모든 요청을 WAS가 제공하는 디폴트 서블릿에 전달한다. 
예를 들어 "index.html"에 대한 처리를 DefaultServletHttpRequestHandler에 요청하면 이 요청을 다시 디폴트 서블릿에 전달해서 처리하도록 한다. 
그리고 SimpleUrlHandlerMapping을 이용해서 모든 경로 ("/**")를 DefaultServletHttpRequestHandler를 이용해서 처리하도록 설정한다.  

@EnableWebMvc 어노테이션이 등록하는 RequestMappingHandlerMapping의 적용 우선순위가 DefaultServletHandlerConfigurer#enable() 메서드가 등록하는 SimpleUrlHandlerMapping의 우선순위보다 높다. 
때문에 웹 브라우저의 요청이 들어오면 DispatcherServlet은 다음과 같은 방식으로 요청을 처리한다. 

1. RequestMappingHandlerMapping을 사용해서 요청을 처리할 핸들러를 검색한다.  
    - 존재하면 해당 컨트롤러를 이용해서 요청을 처리한다. 
2. 존재하지 않으면 SimpleUrlHandlerMapping을 사용해서 요청을 처리할 핸드러를 검색한다. 
    - DefaultServletHandlerConfigurer#enable() 메서드가 등록한 SimpleUrlHandlerMapping은 "/**" 경로에 대해 DefaultServletHttpRequestHandler를 리턴한다.
	- DispatcherServlet은 DefaultServletHttpRequestHandler에 처리를 요청한다. 
	- DefaultServletHttpRequestHandler는 디폴트 서블릿에 처리를 위임한다. 

예를 들어 "/index.html" 경로로 요청이 들어오면 1번 과정에서 해당하는 컨트롤러를 찾지 못하므로 2번 과정을 통해 디폴트 서블릿이 /index.html 요청을 처리하게 된다.  

<br>

### 10.8 정리  
DispatcherServlet은 웹 브라우저의 요청을 받기 위한 창구 역할을 하고, 다른 주요 구성 요소들을 이용해서 요청 흐름을 제어하는 역할을 한다. 
HandlerMapping은 클라이언트의 요청을 처리할 핸들러 객체를 찾아준다. 핸들러(커맨드) 객체는 클라이언트의 요청을 실제로 처리한 뒤 뷰 정모와 모델을 설정한다. 
HandlerAdapter는 DispatcherServlet과 핸들러 객체 사이의 변환을 알맞게 처리해 준다. 
ViewResolver는 요청 처리 결과를 생성할 View를 찾아주고 View는 최종적으로 클라이언트에 응답을 생성해서 전달한다.


<br><br><br>

---

**Reference**  
- 초보 웹 개발자를 위한 스프링 5 (최범균)

---

