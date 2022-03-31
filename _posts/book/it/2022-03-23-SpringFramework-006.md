---
layout: post
title: "Spring 프로그래밍 (Chapter 11)[스프링 MVC 1]"
author: "Bys"
category: it_book
date: 2022-03-23 01:00:00
tags: book programming spring framework mvc @modelattribute @valid
---

## 11 MVC 1: 요청 매핑, 커맨드 객체, 리다이렉트, 폼 태그, 모델  
스프링 MVC를 사용해서 웹 어플리케이션을 개발한다는 것은 결국 컨트롤러와 뷰 코드를 구현한다는 것을 뜻한다. 
대부분 설정은 개발 초기에 완성된다. 개발이 완료될 때까지 개발자가 만들어야 하는 코드는 컨트롤러와 뷰 코드이다. 이 장에서는 기본적인 컨트롤러와 뷰의 구현 방법을 배울 것이다.  

<br>

### 11.2 요청 매핑 어노테이션을 이용한 경로 매핑
웹 어플리케이션을 개발하는 것은 다음 코드를 작성하는 것이다. 

- 특정 요청 URL을 처리할 코드  
- 처리 결과를 HTML과 같은 형식으로 응답하는 코드  

회원가입 과정은 '약관 동의' -> '회원 정보 입력' -> '가입 완료'인데  아래와 같이 URL을 정의할 수 있다. 
- 약관 동의 화면 요청 처리: http://localhost:8080/sp5-chap11/register/step1
- 회원 정보 입력 화면: http://localhost:8080/sp5-chap11/register/step2
- 가입 처리 결과 화면: http://localhost:8080/sp5-chap11/register/step3

이렇게 여러 단계를 거쳐 하나의 기능이 완성되는 경우 관련 요청 경로를 한 개의 컨트롤러 클래스에서 처리하면 코드 관리에 도움이 된다. 

```Java
@Controller
@RequestMapping("/register")
public class RegisterController{
	
	@RequestMapping("step1")
	public String handleStep1(){
	}
	@RequestMapping("step2")
	public String handleStep2(){
	}
	@RequestMapping("step3")
	public String handleStep3(){
	}
}
```
각 요청 매핑 어노테이션의 경로가 "/register"로 시작한다. 
이 경우 위의 코드처럼 공통되는 부분의 경로를 담은 @RequestMapping 어노테이션을 클래스에 적용하고 각 메서드는 나머지 경로를 값으로 갖는 요청 매핑 어노테이션을 적용할 수 있다. 

<br>

### 11.3 GET과 POST구분: @GetMapping, @PostMapping  
스프링 MVC는 별도 설정이 없으면 GET과 POST 방식에 상관없이 @RequestMapping에 지정한 경로와 일치하는 요청을 처리한다. 
만약 POST 방식 요청만 처리하고 싶다면 @PostMapping 어노테이션을 사용해서 제한할 수 있다. 
동일하게 @GetMapping 어노테이션을 사용하면 GET 방식만 처리하도록 제한할 수 있다. 

이 두 어노테이션을 사용하면 다음 코드처럼 같은 경로에 대해 GET과 POST방식을 각각 다른 메서드가 처리하도록 설정할 수 있다. 

```Java
@Controller
public class LoginController {
	
	@GetMapping("/member/login")
	public String form(){
	}

	@PostMapping("/member/login")
	public String login(){
	}
}
```
<br>

### 11.4 요청 파라미터 접근  
약관 동의 화면을 생성하는 코드를 보면 다음 처럼 약관 동의할 경우 true인 'agree' 요청 파라미터의 값을 POST 방식으로 전송한다. 
```html
<form action="step2" method="post">
<label>
	<input type="checkbox" name="agree" value="true"> 약관 동의
</label>
<input type="submit" value="다음 단계" />
</form>
```

1. 컨트롤러 메서드에서 요청 파라미터를 사용하는 첫 번째 방법은 HttpServletRequest를 직접 이용하는 것이다.  
	예를 들면 다음과 같이 컨트롤러 처리 메서드의 파라미터로 HttpServletRequest 타입을 사용하고 HttpServletRequest의 getParameter() 메서드를 이용해서 파라미터의 값을 구하면 된다.  

	```Java
	@PostMapping("/register/step2")
	public String handleStep2(HttpServletRequest request) {
		String agreeParam = request.getParamter("agree");
	}
	```

2. 요청 파라미터에 접근하는 또 다른 방법은 @RequestParam 어노테이션을 사용하는 것이다. 

	```Java
	@PostMapping("/register/step2")
	public String handleStep2(
			@RequestParam(value = "agree", defaultValue = "false") Boolean agree,
			Model model) {
		if (!agree) {
			return "register/step1";
		}
		model.addAttribute("registerRequest", new RegisterRequest());
		return "register/step2";
	}
	```
	위 코드는 agree 요청 파라미터의 값을 읽어와 agreeVal 파라미터에 할당한다. 요청 파라미터의 값이 없으면 "false" 문자열을 값으로 사용한다.  

	`@RequestParam 어노테이션의 속성`  

	| 속성          | 타입     | 설명  |
	| :---         | :---    | :--- |
	| value        | String  | HTTP 요청 파라미터의 이름을 지정한다. |
	| required     | boolean | 필수 여부를 지정한다. | 
	| defaultValue | String  | 요청 파라미터가 값이 없을 때 사용한 문자열 값을 지정한다. |

<br>

### 11.6 커맨드 객체를 이용해서 요청 파라미터 사용하기  

```Java
@PostMapping("/register/step3")
public String handleStep3(HttpServletRequest request){

	String email = request.getParameter("email");
	String name = request.getParameter("name");
	String password = request.getParameter("password");
	String confirmPassword = request.getParameter("confirmPassword");
	
	RegisterRequest regReq = new RegisterRequest();
	regReq.setEmail(email);
	regReq.setName(name);
	......
	memberRegisterService.regist(regReq);
}
```
폼 전송 요청을 처리하는 컨트롤러 코드는 각 파라미터의 값을 구하기 위해 위와 같은 코드를 사용할 수 있다. 
위 코드가 올바르게 동작하지만, 요청 파라미터 개수가 증가할 때마다 handleStep3() 메서드의 코드 길이도 함께 길어지는 단점이 있다. 

스프링은 이런 불편함을 줄이기 위해 요청 파라미터의 값을 커맨드(command) 객체에 담아주는 기능을 제공한다. 
예를 들어 이름이 name인 요청 파라미터의 값을 커맨드 객체의 setName() 메서드를 사용해서 커맨드 객체에 전달하는 기능을 제공한다. 
커맨드 객체라고 해서 특별한 코드를 작성해야 하는 것은 아니다. *요청 파라미터의 값을 전달받을 수 있는 세터 메서드를 포함하는 객체를 커맨드 객체로 사용하면 된다.*  


```Java
@PostMapping("/register/step3")
public String handleStep3(RegisterRequest regReq){
	memberRegisterService.regist(regReq);
}
```
RegisterRequest 클래스에는 setEmail(), setName() 등의 메서드가 있다. 
스프링은 이들 메서드를 사용해서 email, name, password, confirmPassword 요청 파라미터의 값을 커맨드 객체에 복사한 뒤 regReq 파라미터로 전달한다. 
즉 스프링 MVC가 handleStep3() 메서드에 전달할 RegisterRequest 객체를 생성하고 그 객체의 세터 메서드를 이용해서 일치하는 요청 파라미터의 값을 전달한다.  

<br>

### 11.7 뷰 JSP 코드에서 커맨드 객체 사용하기  
```jsp
<body>
    <p><strong>${registerRequest.name}님</strong> 
        회원 가입을 완료했습니다.</p>
    <p><a href="<c:url value='/main'/>">[첫 화면 이동]</a></p>
</body>
```

\${registerRequest.name} 코드가 있다. 여기서 registerRequest가 커맨드 객체에 접근할 때 사용한 속성 이름이다. 
스프링 MVC는 커맨드 객체의 (첫 글자를 소문자로 바꾼) 클래스 이름과 동일한 속성 이름을 사용해서 커맨드 객체를 뷰에 전달한다. 
커맨드 객체의 클래스 이름이 RegisterRequest인 경우 JSP 코드는 registerRequest라는 이름을 사용해서 커맨드 객체에 접근할 수 있다.

### 11.8 @ModelAttribute 어노테이션으로 커맨드 객체 속성 이름 변경  
커맨드 객체에 접근할 때 사용할 속성 이름을 변경하고 싶다면 커맨드 객체로 사용할 파라미터에 @ModelAttribute 어노테이션을 적용하면 된다. 

```Java
@PostMapping("/register/step3")
public String handleStep3(@ModelAttribute("formData") RegisterRequest regReq){
	memberRegisterService.regist(regReq);
}
```
@ModelAttribute 어노테이션은 모델에서 사용할 속성 이름을 값으로 설정한다. 
위 설정을 사용하면 뷰 코드에서 "formData"라는 이름으로 커맨드 객체에 접근할 수 있다.  

<br>

### 11.10 컨트롤러 구현 없는 경로 매핑
회원 가입 완료 후 첫 화면으로 이동할 수 있는 링크 /main 을 보여준다. 이 첫화면은 단순히 환영 문구와 회원 가입으로 이동할 수 있는 링크만 제공한다고 하자. 
이를 위한 컨트롤러 클래스는 특별히 처리할 것이 없기 때문에 다음처럼 단순히 뷰 이름만 리턴하도록 구현할 것이다.
```Java
@Controller
public MainController(){

	@RequestMapping("/main")
	public String main(){
		return "main";
	}
}
```
이 컨트롤러 코드는 요청 경로와 뷰 이름을 연결해주는 것에 불과하다. 단순 연결을 위해 특별한 로직이 없는 컨트롤러 클래스를 만드는 것은 성가신 일이다. 
WebMvcConfigurer 인터페이스의 addViewControllers() 메서드를 사용하면 이런 성가심을 없앨 수 있다. 
```Java
@Override
public void addViewControllers(ViewControllerRegistry registry){
	registry.addViewController("/main").setViewName("main");
}
```
이 태그는 /main 요청 경로에 대해 뷰 이름으로 main을 사용한다고 설정한다.

<br><br>

## 12 MVC 2: 메시지, 커맨드 객체 검증  

### 12.2 <spring:message> 태그로 메시지 출력하기 

src/main/resources에 message 폴더를 생성하고 이 폴더에 label.properties 파일을 생성한다.  

```properties
member.register=회원가입

term=약관
term.agree=약관동의
next.btn=다음단계

member.info=회원정보
email=이메일
name=이름
password=비밀번호
password.confirm=비밀번호 확인
register.btn=가입 완료

register.done=<strong>{0}님 ({1})</strong>, 회원 가입을 완료했습니다.

go.main=메인으로 이동

required=필수항목입니다.
bad.email=이메일이 올바르지 않습니다.
duplicate.email=중복된 이메일입니다.
nomatch.confirmPassword=비밀번호와 확인이 일치하지 않습니다.
```

다음으로 MessageSource 타입의 빈을 추가한다.  
```Java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {
	@Bean
	public MessageSource messageSource() {
		ResourceBundleMessageSource ms = 
				new ResourceBundleMessageSource();
		ms.setBasenames("message.label");
		ms.setDefaultEncoding("UTF-8");
		return ms;
	}
}
```
setBasenames 프로퍼티 값으로 "message.label"을 주었다. 이는 message 패키지에 속한 label 프로퍼티 파일로부터 메시지를 읽어온다고 설정한 것이다. 
src/main/resources 폴더도 클래스 패스에 포함되고 message 폴더는 message 패키지에 대응한다. 
따라서 이 설정은 앞서 작성한 label.properties 파일로부터 메시지를 읽어온다. 앞서 작성한 label.properties 파일은 UTF-8 인코딩을 사용하므로 defaultEncoding 속성의 값으로 "UTF-8"을 사용했따.  

```jsp
<body>
    <h2><spring:message code="member.info" /></h2>
    <form:form action="step3" modelAttribute="registerRequest">
    <p>
        <label><spring:message code="email" />:<br>
        <form:input path="email" />
        <form:errors path="email"/>
        </label>
    </p>
    <p>
        <label><spring:message code="name" />:<br>
        <form:input path="name" />
        <form:errors path="name"/>
        </label>
    </p>
	......
    <input type="submit" value="<spring:message code="register.btn" />">
    </form:form>
</body>
```

\<spring:message\> 태그는 MessageSource로부터 코드에 해당하는 메세지를 읽어온다. 
앞서 설정한 MessageSource는 label.properties 파일로부터 메시지를 읽어오므로 <spring:message> 태그의 위치에 label.properties에 설정한 프로퍼티의 값이 출력된다.  

> 다국어 메세지를 지원하려면 각 프로퍼티 파일 이름에 언어에 해당하는 로케일 문자를 추가한다. 
> - label_ko.properties
> - label_en.properties
> 각 언어를 위한 두 글자 구분자가 존재한다. 특정 언어에 해당하는 메세지 파일이 존재하지 않으면 언어 구분이 없는 label.properties 파일의 메시지를 사용한다. 
> 스프링 MVC는 웹 브라우저가 전송한 Accept-Language 헤더를 이용해서 Locale을 구한다. 이 Locale을 MessageSource에서 메세지를 구할 때 사용한다.  

<br>

### 12.3 커맨드 객체의 값 검증과 에러 메시지 처리  

폼 값 검증과 에러 메시지 처리는 어플리케이션을 개발할 때 놓쳐서는 안 된다. 스프링은 이 두가지 문제를 처리하기 위해 다음 방법을 제공하고 있다. 

- 커맨드 객체를 검증하고 결과를 에러 코드로 저장
- JSP에서 에러 코드로부터 메시지를 출력 

<br>

#### 12.3.1 커맨드 객체 검증과 에러 코드 지정하기 
스프링 MVC에서 커맨드 객체의 값이 올바른지 검사하려면 다음의 두 인터페이스를 사용한다. 
- org.springframework.validation.Validator
- org.springframework.validation.Errors

객체를 검증할 때 사용하는 Validator 인터페이스는 다음과 같다.  
```Java
package org.springframework.validation;

public interface Validator{
	boolean supports(Class<?> clazz);
	void validate(Object target, Errors errors);
}
```
위 코드에서 supports() 메서드는 Validator가 검증할 수 있는 타입인지 검사한다. 
validate() 메서드는 첫 번째 파라미터로 전달받은 객체를 검증하고 오류 결과를 Errors에 담는 기능을 정의한다. 

일단 Validator 인터페이스를 구현한 클래스를 먼저 만들어보고, 주요 코드를 보면서 구현 방법을 살펴보자. 
RegisterRequest 객체를 검증하기 위한 Validator 구현 클래스의 작성 예이다.  
```Java
package controller;

public class RegisterRequestValidator implements Validator {
	private static final String emailRegExp = 
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
			"[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private Pattern pattern;

	public RegisterRequestValidator() {
		pattern = Pattern.compile(emailRegExp);
		System.out.println("RegisterRequestValidator#new(): " + this);
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return RegisterRequest.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		System.out.println("RegisterRequestValidator#validate(): " + this);
		RegisterRequest regReq = (RegisterRequest) target;
		if (regReq.getEmail() == null || regReq.getEmail().trim().isEmpty()) {
			errors.rejectValue("email", "required");
		} else {
			Matcher matcher = pattern.matcher(regReq.getEmail());
			if (!matcher.matches()) {
				errors.rejectValue("email", "bad");
			}
		}
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "required");
		ValidationUtils.rejectIfEmpty(errors, "password", "required");
		ValidationUtils.rejectIfEmpty(errors, "confirmPassword", "required");
		if (!regReq.getPassword().isEmpty()) {
			if (!regReq.isPasswordEqualToConfirmPassword()) {
				errors.rejectValue("confirmPassword", "nomatch");
			}
		}
	}

}
```
supports() 메서드는 파라미터로 전달받은 clazz 객체가 RegisterRequest 클래스로 타입 변환이 가능한지 확인한다. 
이 예제에서는 supports() 메서드를 직접 실행하진 않지만 스프링 MVC가 자동으로 검증 기능을 수행하도록 설정하려면 supports() 메서드를 올바르게 구현해야 한다.  

validate() 메서드는 두 개의 파라미터를 갖는다. 
target 파라미터는 검사 대상 객체이고 errors 파라미터는 검사 결과 에러 코드를 설정하기 위한 객체이다. validate() 메서드는 보통 다음과 같이 구현한다. 
- 검사 대상 객체의 특정 프로퍼티나 상태가 올바른지 검사 
- 올바르지 않다면 Errors의 rejectValue() 메서드를 이용해서 에러 코드 저장 

검사 대상의 값을 구하기 위해 첫 번째 파라미터로 전달 받은 target을 실제 타입으로 변환한 뒤에 값을 검사한다. 
Errors의 rejectValue() 메서드는 첫 번째 파라미터로 프로퍼티의 이름을 전달받고, 두 번째 파라미터로 에러 코드를 전달받는다. 
JSP 코드에서는 여기서 지정한 에러 코드를 이용해서 에러 메시지를 출력한다.  

ValidationUtils 클래스는 객체의 값 검증 코드를 간결하게 작성할 수 있도록 도와준다. 
위 코드는 검사 대상 객체의 "name" 프로퍼티가 null 이거나 공백문자로만 되어 있는 경우 "name" 프로퍼티의 에러 코드로 "required"를 추가한다. 
즉 위의 코드는 다음 코드와 동일하다. 
```Java
String name = reqReg.getName();
if(name == null || name.trim().isEmtpy()){
	errors.rejectValue("name", "required");
}
```

<br>

ValidationUtils.rejectIfEmptyOrWhitespace 메서드를 실행할 때 검사 대상 객체인 target을 파라미터로 전달하지 않았는데 어떻게 target 객체의 "name" 프로퍼티의 값을 검사할까? 
비밀은 Errors 객체에 있다. 스프링 MVC에서 Validator를 사용하는 코드는 요청 매핑 어노테이션 적용 메서드에 Errors 타입 파라미터를 전달받고, 
이 Errors 객체를 Validator의 validate() 메서드에 두 번째 파라미터로 전달한다. 
```Java
@Controller
public class RegisterController {

	private MemberRegisterService memberRegisterService;

	public void setMemberRegisterService(
			MemberRegisterService memberRegisterService) {
		this.memberRegisterService = memberRegisterService;
	}
	@PostMapping("/register/step3")
	public String handleStep3(RegisterRequest regReq, Errors errors) {
		new RegisterRequestValidator().validate(regReq, errors);
		if (errors.hasErrors())
			return "register/step2";

		try {
			memberRegisterService.regist(regReq);
			return "register/step3";
		} catch (DuplicateMemberException ex) {
			errors.rejectValue("email", "duplicate");
			return "register/step2";
		}
	}
}
```
요청 매핑 어노테이션 적용 메서드의 커맨드 객체 파라미터 뒤에 Errors 타입 파라미터가 위치하면, 스프링 MVC는 handleStep3() 메서드를 호출할 때 커맨드 객체와 연결된 Errors 객체를 생성해서 파라미터로 전달한다. 
이 Errors 객체는 커맨드 객체의 특정 프로퍼티 값을 구할 수 있는 getFieldValue() 메서드를 제공한다. 
따라서 ValidationUtils.rejectIfEmptyOrWhitespace() 메서드는 커맨드 객체를 전달받지 않아도 Errors 객체를 이용해서 지정한 값을 구할 수 있다.  

validate()를 실행하는 과정에서 유효하지 않은 값이 존재하면 Errors의 rejectValue()메서드를 실행한다. 
이 메서드가 한 번이라도 불리면 Errors의 hasErrors() 메서드는 true를 리턴한다. 

커맨드 객체의 특정 프로퍼티가 아닌 커맨드 객체 자체가 잘못될 쑤도 있다. 이런 경우에는 rejectValue() 메서드 대신에 reject() 메서드를 사용한다. 
```Java
try {
	...... // 인증 처리 코드
} catch(WrongIdPasswordException ex){
	errors.reject("notMatchingIdPassword");
	return "login/loginForm";
}
```
로그인 아이디와 비밀번호를 잘못 입력한 경우 아이디와 비밀번호가 불일치한다는 메시지를 보여줘야 한다. 
이 경우 특정 프로퍼티에 에러를 추가하기 보다는 커맨드 객체 자체에 에러를 추가해야 하는데, 이 때 reject() 메서드를 사용한다. 
reject() 메서드는 개별 프로퍼티가 아닌 객체 자체에 에러 코드를 추가하므로 이 에러를 글로벌 에러라고 부른다. 

<br>

### 12.4 글로벌 범위 Validator와 컨트롤러 범위 Validator 
스프링 MVC는 모든 컨트롤러에 적용할 수 있는 글로벌 Validator와 단일 컨트롤러에 적용할 수 있는 Validator를 설정하는 방법을 제공한다. 
이를 사용하면 @Valid 어노테이션을 사용해서 커맨드 객체에 검증 기능을 적용할 수 있다.  

#### 12.4.1 글로벌 범위 Validator 설정과 @Valid 어노테이션 
글로벌 범위 Validator는 모든 컨트롤러에 적용할 수 있는 Validator이다. 글로벌 범위 Validator를 적용하려면 다음 두 가지를 설정하면 된다. 

- 설정 클래스에서 WebMvcConfigurer의 getValidator() 메서드가 Validator 구현 객체를 리턴하도록 구현 
- 글로벌 범위 Validator가 검증할 커맨드 객체에 @Valid 어노테이션 적용 

<br>

먼저 글로벌 범위 Validator를 설정하자. 이를 위해 해야 할 작업은 WebMvcConfigurer인터페이스에 정의된 getValidator() 메서드를 구현하는 것이다. 

```Java
@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public Validator getValidator(){
		return new RegisterRequestValidator();
	}
}
```
스프링 MVC는 WebMvcConfigurer 인터페이스의 getValidator() 메서드가 리턴한 객체를 글로벌 범위 Validator로 사용한다. 
글로벌 범위 Validator를 지정하면 @Valid 어노테이션을 사용해서 Validator를 적용할 수 있다. 

```Java
@Controller
public class RegisterController {
	
	@PostMapping("/register/step3")
	public String handleStep3(@Valid RegisterRequest regReq, Errors errors) {
		new RegisterRequestValidator().validate(regReq, errors);
		if (errors.hasErrors())
			return "register/step2";

		try {
			memberRegisterService.regist(regReq);
			return "register/step3";
		} catch (DuplicateMemberException ex) {
			errors.rejectValue("email", "duplicate");
			return "register/step2";
		}
	}
}
```
커ㄴ드 객체에 해ㅇ하는 파미터에 @Valid 어노테이션을 붙이면 글로벌 범위 Validator가 해당 타입을 검증할 수 있는지 확인한다. 
검증 가능하면 실제 검증을 수행하고 그 결과를 Errors에 저장한다. 이는 요청 처리 메서드 실행 전에 적용된다. 

위 예의 경우 handleStep3() 메서드를 실행하기 전에 @Valid 어노테이션이 붙은 regReq 파라미터를 글로벌 범위 Validator로 검증한다. 

<br><br><br>

---

- 출처  
초보 웹 개발자를 위한 스프링 5 (최범균)

---

