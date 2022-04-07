---
layout: post
title: "Spring 프로그래밍 (Chapter 13)[스프링 MVC 3: 세션, 인터셉터, 쿠키]"
author: "Bys"
category: it_book
date: 2022-03-30 01:00:00
tags: book programming spring framework mvc session cookie interceptor antpath @cookievalue
---

## 13. MVC 3: 세션, 인터셉터, 쿠키

### 13.3 컨트롤러에서 HttpSession 사용하기  

로그인 기능을 구현했는데 한 가지 빠진 것이 있다. 그것은 바로 로그인 상태를 유지하는 것이다. 
로그인 상태를 유지하는 방법은 크게 HttpSession을 이용하는 방법과 쿠키를 이용하는 방법이 있다. 
외부 데이터베이스에 세션 데이터를 보관하는 방법도 사용하는데 큰 틀에서 보면 HttpSession과 쿠키의 두 가지 방법으로 나뉜다. 
이 장에서는 HttpSession을 이용해서 로그인 상태를 유지하는 코드를 추가해보자.  

컨트롤러에서 HttpSession을 사용하려면 다음의 두 가지 방법 중 한 가지를 사용하면 된다. 
- 요청 매핑 어노테이션 적용 메서드에 HttpSession 파라미터를 추가한다. 
- 요청 매핑 어노테이션 적용 메서드에 HttpServletRequest 파라미터를 추가하고 HttpServletRequest을 이용해서 HttpSession을 구한다.  

1. 첫 번째 방법을 사용한 코드
    ```Java
    @PostMapping
    public String form(LoginCommand loginCommand, Errors errors, HttpSession session){
        // session 코드
    }
    ```
    요청 매핑 어노테이션 적용 메서드에 HttpSession 파라미터가 존재할 경우 스프링 MVC는 컨트롤러 메서드를 호출할 때 HttpSession 객체를 파라미터로 전달한다. 
    HttpSession을 생성하기 전이면 새로운 HttpSession을 생성하고 그렇지 않으면 기존에 존재하는 HttpSession을 전달한다.  

2. 두 번째 방법을 사용한 코드 
    ```Java
    @PostMapping
    public String submit(LoginCommand loginCommand, Errors errors, HttpServletRequest request){
        HttpSession session = request.getSession();
        // session 코드
    }
    ```
    첫 번째 방법은 항상 HttpSession을 생성하지만 두 번째 방법은 필요한 시점에만 HttpSession을 생성할 수 있다.  


로그인에 성공하면 authInfo 속성에 인증 정보 객체를 저장하도록 코드를 추가한다. 
```Java
    @PostMapping
    public String form(LoginCommand loginCommand, Errors errors, HttpSession session){
        new LoginCommandValidator().validate(loginCommand, errors);
        if(errors.hasErrors()){
            return "login/loginForm";
        }

        try {
            AuthInfo authInfo = authService.authenticate(loginCommand.getEmail(), loginCommand.getPassword());

            session.setAttribute("authInfo", authInfo);

            return "login/loginSuccess";

            ...... // 생략
```

로그아웃을 위한 컨트롤러 클래스는 HttpSession을 제거하면 된다.  

```Java
@Controller
public class LogoutController {

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/main";
	}

}
```

### 13.5 인터셉터 사용하기  

로그인을 하지 않은 상태에서 비밀번호 변경 폼을 요청하면 로그인 화면으로 이동시키는 것이 더 좋은 방법이다. 

이를 위해 HttpSession에 authInfo 객체가 존재하는지 검사하고 존재하지 않으면 로그인 경로로 리다이렉트하도록 수정할 수 있다. 
그런데 실제 웹 어플리케이션에서는 비밀번호 변경 기능 외에 더 많은 기능에 로그인 여부를 확인해야 한다. 각 기능을 구현할 컨트롤러 코드마다 세션 확인 코드를 삽입하는 것은 많은 중복을 일으킨다. 
이렇게 다수의 컨트롤러에 대해 동일한 기능을 적용해야 할 때 사용할 수 있는 것이 HandlerInterceptor이다. 

#### 13.5.1 HandlerInterceptor 인터페이스 구현하기  
org.springframework.web.HandlerInterceptor 인터페이스를 사용하면 다음의 세 시점에 공통 기능을 넣을 수 있다. 
- 컨트롤러(핸들러) 실행 전
- 컨트롤러(핸들러) 실행 후, 아직 뷰를 실행하기 전
- 뷰를 실행한 이후 

세 시점을 처리하기 위해 HandlerInterceptor 인터페이스는 다음 메서드를 정의하고 있다.

```Java
boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;
void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception;
void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception;
```

preHandle() 메서드는 컨트롤러 객체를 실행하기 전에 필요한 기능을 구현할 때 사용한다. handler 파라미터는 웹 요청을 처리할 컨트롤러 객체이다. 
이 메서드를 사용하면 다음 작업이 가능하다. 
- 로그인하지 않은 경우 컨트롤러를 실행하지 않음
- 컨트롤러를 실행하기 전에 컨트롤러에서 필요로 하는 정보를 생성

<br>

preHandle() 메서드의 리턴 타입은 boolean 이다.  preHandle() 메서드가 false를 리턴하면 컨트롤러(또는 다음 HandlerInterceptor)를 실행하지 않는다.  

postHandle() 메서드는 컨트롤러가 정상적으로 실행된 이후에 추가 기능을 구현할 때 사용한다. 컨트롤러가 exception을 발생하면 postHandle() 메서드는 실행하지 않는다. 

afterCompletion() 메서드는 뷰가 클라이언트에 응답을 전송한 뒤에 실행된다. 컨트롤러 실행 과정에서 exception이 발생하면 이 메서드의 네 번째 파라미터로 전달된다. 
exception이 발생하지 않으면 네 번째 파라미터는 null이 된다. 따라서 컨트롤러 실행 이후에 예기치 않게 발생한 exception 로그로 남긴다거나 실행 시간을 기록하는 등의 후처리를 하기에 적합한 메서드다. 

![spring5_13_6](/assets/book/spring5/spring5_13_6.png){: width="60%" height="auto"}  

HandlerInterceptor 인터페이스의 각 메서드는 아무 기능도 구현하지 않은 자바 8의 디폴트 메서드이다. 따라서 HandlerInterceptor 인터페이스의 메서드를 모두 구현할 필요가 없다. 
이 인터페이스를 상속받고 필요한 메서드만 재정의하면 된다. 


`AuthCheckInterceptor`  
```Java
public class AuthCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        if(session != null){
            Object authInfo = session.getAttribute("authInfo");
            if(authInfo != null){
                return true;
            }
        }
        response.sendRedirect(request.getContextPath() + "/login");
        return false;
    }
}
```

<br>

#### 13.5.2 HandlerInterceptor 설정하기  
HandlerInterceptor를 구현하면 HandlerInterceptor를 어디에 적용할지 설정해야 한다.  

```Java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

    @Override
	public void addInterceptors(InterceptorRegistry registry){
		registry.addInterceptor(authCheckInterceptor())
				.addPathPatterns("/edit/**");
	}

   	@Bean
	public AuthCheckInterceptor authCheckInterceptor(){
		return new AuthCheckInterceptor();
	}
	...... // 생략
}
```
1. WebMvcConfigurer#addInterceptors() 메서드는 인터셉터를 설정하는 메서드이다.  
2. InterceptorRegistry#addInterceptor() 메서드는 HandlerInterceptor 객체를 설정한다.  
3. InterceptorRegistry#addInterceptor() 메서드는 InterceptorRegistration 객체를 리턴하는데 이 객체의 addPathPatterns() 메서드는 인터셉터를 적용할 경로 패턴을 지정한다. 이 경로는 Ant 경로 패턴을 사용한다. 
    addPathPatterns() 메서드에 지정한 경로 패턴 중 일부를 제외하고 싶다면 excludePathPatterns() 메서드를 사용한다.  
    ```Java
    @Configuration
    @EnableWebMvc
    public class MvcConfig implements WebMvcConfigurer {

        @Override
        public void addInterceptors(InterceptorRegistry registry){
            registry.addInterceptor(authCheckInterceptor())
                    .addPathPatterns("/edit/**")
                    .excludePathPatterns("/edit/changePassword");
        }
        ......//생략
    }
    ```

> **Ant 경로 패턴**  
Ant 패턴은 *, **, ?의 세 가지 특수 문자를 이용해서 경로를 표현한다. 각 문자는 다음의 의미를 갖는다. 
- *: 0개 또는 그 이상의 글자 
- ?: 1개 글자
- **: 0개 또는 그 이상의 폴더 경로  <br><br>
이들 문자를 사용한 경로 표현 예는 다음과 같다.  
- @RequestMapping("/member/?*.info")  
/member/로 시작하고 확장자가 .info로 끝나는 모든 경로  
- @RequestMapping("/faq/f?00.fq")  
/faq/f로 시작하고, 1글자가 사이에 위치하고 00.fq로 끝나는 모든 경로 ex) /fq/fa00.fq 
- @RequestMapping("/folders/**/files")  
/folders/로 시작하고, 중간에 0개 이상의 중간 경로가 존재하고 /files로 끝나는 모든 경로 ex) /folders/files, /folders/1/2/3/files 등 


### 13.6 컨트롤러에서 쿠키 사용하기  
사용자 편의를 위해 아이디를 기억해 두었다가 다음에 로그인할 때 아이디를 자동으로 넣어주는 사이트가 많다. 이 기능을 구현할 때 쿠키를 사용한다. 
이 장의 예제에도 쿠키를 사용해서 이메일 기억하기 기능을 추가해보자.  

1. loginForm.jsp: 이메일 기억하기 선택 항목을 추가한다. 
2. LoginController#form(): 쿠키가 존재할 경우 폼에 전달할 커맨드 객체의 email 프로퍼티를 쿠키의 값으로 설정한다. 
3. LoginController#submit(): 이메일 기억하기 옵션을 선택한 경우 로그인 성공 후에 이메일 담고 있는 쿠키를 생성한다.  
4. label.properties: 메시지를 추가한다.  

loginForm.jsp에는 이메일 기억하기를 선택할 수 있도록 체크박스를 추가한다.  

LoginController의 form() 메서드는 이메일 정보를 기억하고 있는 쿠키가 존재하면 해당 쿠키의 값을 이용해서 LoginCommand 객체의 email 프로퍼티 값을 설정하면 된다. 
스프링 MVC에서 쿠키를 사용하는 방법 중 하나는 @CookieValue 어노테이션을 사용하는 것이다. 
@CookieValue 어노테이션은 요청 매핑 어노테이션 적용 메서드의 Cookie타입 파라미터에 적용한다. 

```Java
@GetMapping
public String form(LoginCommand loginCommand, @CookieValue(value="REMEMBER", required = false) Cookie rCookie, HttpSession session){
    if(session.getAttribute("authInfo") != null){
        System.out.println("session.getAuthInfo(): " + session.getAttribute("authInfo"));
        System.out.println("session.getId(): " + session.getId());
        return "login/loginSuccess";
    }
    if (rCookie != null) {
        loginCommand.setEmail(rCookie.getValue());
        loginCommand.setRememberEmail(true);
    }
    return "login/loginForm";
}
```

@CookieValue 어노테이션의 value 속성은 쿠키의 이름을 지정한다. 이 코드는 이름이 REMEMBER인 쿠키를 Cookie 타입으로 전달받는다. 
지정한 이름을 가진 쿠키가 존재하지 않을 수도 있다면 required 속성값을 false로 지정한다. (이 예제의 경우 이메일 기억하기를 선택하지 않을 수도 있다.)  
REMEMBER 쿠키가 존재하면 쿠키의 값을 읽어와 커맨드 객체의 email 프로퍼티 값을 설정한다. 
커맨드 객체를 사용해서 폼을 출력하므로 REMEMBER 쿠키가 존재하면 입력 폼의 email 프로퍼티에 쿠키값이 채워져서 출력된다.  


실제로 REMEMBER 쿠키를 생성하는 부분은 로그인을 처리하는 submit() 메서드이다.  
쿠키를 생성하려면 HttpServletResponse 객체가 필요하므로 submit() 메서드의 파라미터로 HttpServletResponse 타입을 추가한다.  
```Java
@PostMapping
public String submit(LoginCommand loginCommand, Errors errors, HttpSession session, HttpServletResponse response){
    new LoginCommandValidator().validate(loginCommand, errors);
    if(errors.hasErrors()){
        return "login/loginForm";
    }
    try {
        AuthInfo authInfo = authService.authenticate(loginCommand.getEmail(), loginCommand.getPassword());
        session.setAttribute("authInfo", authInfo);

        Cookie rememberCookie = new Cookie("REMEMBER", loginCommand.getEmail());
        rememberCookie.setPath("/");
        if(loginCommand.isRememberEmail()) {
            rememberCookie.setMaxAge(60 * 60 * 24* 30);
        } else {
            rememberCookie.setMaxAge(0);
        }
        response.addCookie(rememberCookie);

        return "login/loginSuccess";
    } catch(WrongIdPasswordException e) {
        errors.reject("idPasswordNotMatching");
        return "login/loginForm";
    }
}
```
로그인에 성공하면 이메일 기억하기를 선택했는지 여부에 따라 30일동안 유지되는 쿠키를 생성하거나 바로 삭제되는 쿠키를 생성한다.  

<br><br>

## 14. MVC 3: 세션, 인터셉터, 쿠키

### 13.3 컨트롤러에서 HttpSession 사용하기  


<br><br><br>

---

**Reference**  
- 초보 웹 개발자를 위한 스프링 5 (최범균)

---

