---
layout: post
title: "Spring 프로그래밍 (Chapter 13)[스프링 MVC 3: 세션, 인터셉터, 쿠키]"
author: "Bys"
category: it_book
date: 2022-03-30 01:00:00
tags: book programming spring framework mvc session cookie interceptor
---

## 13 MVC 3: 세션, 인터셉터, 쿠키

### 13.3 컨트롤러에서 HttpSession 사용하기  

로그인 기능을 구현했는데 한 가지 빠진 것이 있다. 그것은 바로 로그인 상태를 유지하는 것이다. 
로그인 상태를 유지하는 방법은 크게 HttpSession을 이용하는 방법과 쿠키를 이용하는 방법이 있다. 
외부 데이터베이스에 세션 데이터를 보관하는 방법도 사용하는데 큰 틀에서 보면 HttpSession과 쿠키의 두 가지 방법으로 나뉜다. 
이 장에서는 HttpSession을 이용해서 로그인 상태를 유지하는 코드를 추가해보자.  

컨트롤러에서 HttpSession을 사용하려면 다음의 두 가지 방법 중 한 가지를 사용하면 된다. 
- 요청 매핑 어노테이션 적용 메서드에 HttpSession 파라미터를 추가한다. 
- 요청 매핑 어노테이션 적용 메서드에 HttpServletRequest 파라미터를 추가하고 HttpServletRequest을 이용해서 HttpSession을 구한다.  

<br>

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


<br>

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






<br><br><br>

---

- 출처  
초보 웹 개발자를 위한 스프링 5 (최범균)

---

