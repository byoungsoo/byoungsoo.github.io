---
layout: post
title: "Spring5 프로그래밍 (Chapter 9)[스프링 mvc 시작하기]"
author: "Bys"
category: it_book
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
- <load-on-startup>1</load-on-startup> 톰캣과 같은 컨테이너가 웹 어플리케이션을 구동할 때 이 서블릿을 함께 실행하도록 설정한다.  
- 모든 요청을 DispatcherServlet이 처리하도록 서블릿 매핑을 설정한다. 
- HTTP 요청 파라미터의 인코딩 처리를 위한 서블릿 필터를 등록한다. 스프링은 인코딩 처리를 위한 필터인 CharacterEncodingFilter 클래스를 제공한다.  

<br>

DispatcherServlet은 초기화 과정에서 contextConfiguration 초기화 파라미터에 지정한 설정 파일을 이용해서 스프링 컨테이너를 초기화한다.  
*즉, 위의 설정은 MvcConfig 클래스와 ControllerConfig 클래스를 이용해서 스프링 컨테이너를 생성한다.*  























<br><br><br>

---

- 출처  
초보 웹 개발자를 위한 스프링 5 (최범균)

---

