---
layout: post
title: "Spring 프로그래밍 (Chapter 15~17)[웹 어플리케이션의 구조, JSON응답과 요청처리, 프로필과 프로퍼티 파일]"
author: "Bys"
category: it_book
date: 2022-04-08 01:00:00
tags: book programming spring framework mvc @restcontroller @jsonignore @jsonformat unixtimestamp @requestbody
---

## 15. 간단한 웹 어플리케이션 구조

### 15.1 웹 어플리케이션의 구성 요소

간단한 웹 엎플리케이션을 개발할 때 사용하는 전형적인 구조는 다음 요소를 포함한다. 

- 프론트 서블릿
- 컨트롤러 + 뷰
- 서비스
- DAO

프론트 서블릿은 웹 브라우저의 모든 요청을 받는 창구 역할을 한다. 프론트 서블릿은 요청을 분석해서 알맞은 컨트롤러에 전달한다. 
스프링 MVC에서는 DispatcherServlet이 프론트 서블릿의 역할을 수행한다.  

```txt
DispatcherServlet -> 컨트롤러 -> 서비스 -> DAO
```

컨트롤러는 어플리케이션이 제공하는 기능과 사용자 요청을 연결하는 매개체로서 기능 제공을 위한 로직을 직접 수행하지는 않는다. 
서비스는 기능의 로직을 구현한다. 서비스는 DB연동이 필요하면 DAO를 사용한다. DAO는 Data Access Object의 약자로서 DB와 웹 어플리케이션 간에 데이터를 이동시켜 주는 역할을 맡는다. 
어플리케이션은 DAO를 통해서 DB에 데이터를 추가하거나 DB에서 데이터를 읽어온다. 

<br>

### 15.2 서비스의 구현
서비스는 핵심이 되는 기능의 로직을 제공한다. 예를 들어 비밀번호 변경 기능은 다음 로직을 서비스에서 수행한다. 
- DB에서 비밀번호를 변경할 회원의 데이터를 구한다.
- 존재하지 않으면 익셉션을 발생시킨다. 
- 회원 데이터의 비밀번호를 변경한다. 
- 변경 내역을 DB에 반영한다. 

웹 어플리케이션을 사용하든 명령해에서 실행하든 비밀번호 변경 기능을 제공하는 서비스는 동일한 로직을 수행한다. 
이런 로직들은 한 번의 과정으로 끝나기보다는 위 예처럼 몇 단계의 과정을 거치곤 한다. 
중간 과정에서 실패가 나면 이전까지 했던 것을 취소해야 하고, 모든 과정을 성공적으로 진행했을 때 완료해야 한다. 이런 이유로 서비스 메서드를 트랜잭션 범위에서 실행한다. 
비밀번호 변경 기능도 다음과 같이 스픠링의 @Transactional을 이용해서 트랜잭션 범위에서 비밀번호 변경 기능을 수행했다. 

```Java
@Transactional
public void changePassword(String email, String oldPwd, String newPwd) {
    Member member = memberDao.selectByEmail(email);
    if (member == null)
        throw new MemberNotFoundException();

    member.changePassword(oldPwd, newPwd);

    memberDao.update(member);
}

public void setMemberDao(MemberDao memberDao) {
    this.memberDao = memberDao;
}
```

서비스를 구현할 때 한 서비스 클래스가 제공할 기능의 개수는 몇 개가 적당할까?
필자는 기능별로 서비스 클래스를 작성하는 것을 선호한다. 그 이유는 한 클래스의 코드 길이를 일정 수준 안에서 유지할 수 있기 때문이다. 


### 15.4 패키지 구성
각 구성 요소의 패키지는 어떻게 구분해 줘야 할까? 
패키지 구성에는 사실 정답이 없다. 패키지를 구성할 때 중요한 점은 팀 구성원 모두가 동일한 규칙에 따라 일관되게 패키지를 구성해야 한다는 것이다.
개발자에 따라 패키지를 구성하는 방식이 서로 다르면 코드를 유지보수할 때 불필요하게 시간을 낭비하게 된다. 
예를 들면 당연히 존재할 거라고 생각한 패키지가 아닌 예상 밖의 패키지에 위치한 클래스를 찾느라 시간을 허비할 수 있다. 

<br><br>

## 16. JSON 응답과 요청 처리
웹 페이지에서 Ajax를 이용해서 서버 API를 호출하는 사이트가 많다. 이들 API는 웹 요청에 대한 응답으로 HTML 대신 JSON이나 XML을 사용한다. 
웹 요청에도 쿼리 문자열 대신에 JSON이나 XML을 데이터로 보내기도 한다. 이 장에서는 스프링 MVC에서 JSON 응답과 요청을 처리하는 방법을 살펴보도록 하자. 

### 16.1 JSON 개요
JSON(Javascript Object Notation)은 간단한 형식을 갖는 문자열로 데이터 교환에 주로 사용한다. 
```Json
{
  "name": "유관순",
  "birthday": "1902-12-16",
  "age": "17",
  "related": ["남동순", "류예도"],
  "edu": [
    {
      "title": "이화학당보통과",
      "year": "1916"
    },
    {
      "title": "이화학당고등과",
      "year": "1916"
    }
  ]
}
```
JSON 규칙은 간단하다. 중괄호를 사용해서 객체를 표헌한다. 객체는 (이름, 값) 쌍을 갖는다. 이때 이름과 값은 콜론(:)으로 구분한다.  
값에는 다음이 올 수 있다.
- 문자열, 숫자, 불리언, null
- 배열
- 다른 객체 

<br>

### 16.2 Jackson 의존 설정 
Jackson은 자바 객체와 JSON 형식 문자열 간 변환을 처리하는 라이브러리이다. 
스프링 MVC에서 Jackson 라이브러리를 이용해서 자바 객체를 JSON으로 변환하라면 클래스패스에 Jackson 라이브러리를 추가하면 된다. 

Jackson은 아래와 같이 자바 객체와 JSON 사이의 변환을 처리한다.

```Java
public class Person{
    private String name;
    private int age;
}
```

```Json
{
    "name": "이름",
    "age": 10
}
```

<br>

### 16.3 @RestController로 JSON 형식 응답 
스프링 MVC에서 JSON 형식으로 데이터를 응답하는 것은 매우 간단하다. @Controller 어노테이션 대신 @RestController 어노테이션을 사용하면 된다. 

```Java
@RestController
public class RestMemberController {
	private MemberDao memberDao;
	private MemberRegisterService registerService;


	@GetMapping("/api/members")
	public List<Member> members() {
		return memberDao.selectAll();
	}
	
	@GetMapping("/api/members2/{id}")
	public Member member2(@PathVariable Long id, HttpServletResponse response) throws IOException {
		Member member = memberDao.selectById(id);
		if (member == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		return member;
	}
    //......생략
}
```
기존 코드와 다른 점은 다음과 같다.
- @Controller 어노테이션 대신 @RestController 어노테이션 사용 
- 요청 매핑 어노테이션 적용 메서드의 리턴 타입으로 일반 객체 사용 

@RestController 어노테이션을 붙인 경우 스프링 MVC는 요청 매핑 어노테이션을 붙인 메서드가 리턴한 객체를 알맞은 형식으로 변환해서 응답 데이터를 전송한다. 
이때 클래스 패스에 Jackson이 존재하면 JSON 형식의 문자열로 변환해서 응답한다. 

![spring5_16_3](/assets/book/spring5/spring5_16_3.png){: width="100%" height="auto"}  

<br>

#### 16.3.1 @JsonIgnore를 이용한 예외 처리
위의 그림을 보면 응답 결과에 password가 포함되어 있다. 보통 암호와 같이 민감한 데이터는 응답 결과에 포함시키면 안되므로 password 데이터를 응답 결과에서 제외시켜야 한다. 
Jackson이 제공하는 @JsonIgnore 어노테이션을 사용하면 이를 간단히 처리할 수 있다. 
다음과 같이 JSON 응답에 포함시키지 않을 대상에 @JsonIgnore 어노테이션을 붙인다. 

```Java
public class Member {

	private Long id;
	private String email;
	@JsonIgnore
	private String password;
	private String name;
	private LocalDateTime registerDateTime;
}
```

@JsonIgnore 어노테이션을 붙인 대상이 JSON 결과에서 제외된 것을 알 수 있다. 

![spring5_16_4](/assets/book/spring5/spring5_16_4.png){: width="100%" height="auto"}  

<br>

#### 16.3.2 날짜 형식 변환 처리: @JsonFormat 사용
위의 그림에서 registerDateTime의 값은 [2022, 3, 21, 15, 43, 1]이다. 
Member 클래스의 registerDateTime 속성은 LocalDateTime 타입인데 JSON값은 배열로 바뀌었다. 
만약 registerDateTime 속성이 java.util.Date 타입이면 다음과 같이 유닉스 타임스탬프로 날짜 값을 표현한다.  

```Json
{
    "id": 1,
    "email:": "test@test.com",
    "name": "test",
    "registerDateTime": "1519870069000"
}
```

<br>

> **유닉스 타임 스탬프**  
> 유닉스 타임 스탬프는 1970년 1월 1일 이후 흘러간 시간을 말한다. 보통 초 단위로 표현하나 Jackson은 별도 설정이 없으면 밀리초 단위로 값을 변환한다. 
> System.currentTimeMils() 메서드가 리턴하는 정수도 유닉스 타임 스탬프 값이다.  

<br>

보통 날짜나 시간은 배열이나 숫자보다는 "yyyy-MM-dd HH:mm:ss"와 같이 특정 형식을 갖는 문자열로 표현하는 것을 선호한다. 
Jackson에서 날짜나 시간 값을 특정한 형식으로 표현하는 가장 쉬운 방법은 @JsonFormat 어노테이션을 사용하는 것이다. 
예를 들어 ISO-8601 형식으로 변환하고 싶다면 다음과 같이 shape 속성 값으로 Shape.STRING을 갖는 @JsonFormat 어노테이션을 변환 대상에 적용하면 된다. 

```Java
public class Member {

	private Long id;
	private String email;
	@JsonIgnore
	private String password;
	private String name;
	@JsonFormat(shape= Shape.STRING)
	private LocalDateTime registerDateTime;
	//생략......
}
```

어노테이션을 사용했을 때 출력 형식이다. ISO-8601 형식을 사용해서 registerDateTime을 문자열로 표시하고 있다. 
```Json
[
    {
        "id": 1,
        "email": "bys@gmail.com",
        "name": "bys",
        "registerDateTime": "2022-03-21T15:43:01"
    }
]
```

ISO-8601 형식이 아닌 원하는 형식으로 변환해서 출력하고 싶다면 @JsonFormat 어노테이션의 pattern 속성을 사용한다. 
```Java
public class Member {

	private Long id;
	private String email;
	@JsonIgnore
	private String password;
	private String name;
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime registerDateTime;
	//생략......
}
```

```Json
[
    {
        "id": 1,
        "email": "bys@gmail.com",
        "name": "bys",
        "registerDateTime": "2022-03-21 15:43:01"
    }
]
```

<br>

#### 16.3.3 날짜 형식 변환 처리: 기본 적용 설정
날짜 형식을 변환할 모든 대상에 @JsonFormat 어노테이션을 붙여야 한다면 상당히 귀찮다. 이런 귀찮음을 피하려면 날짜 타입에 해당하는 모든 대상에 동일한 변환 규칙을 적용할 수 있어야 한다. 
@JsonFormat 어노테이션을 사용하지 않고 Jackson의 변환 규칙을 모든 날짜 타입에 적용하려면 스프링 MVC설정을 변경해야 한다. 

스프링 MVC는 자바 객체를 HTTP 응답으로 변환할 때 HttpMessageConverter라는 것을 사용한다. 
예를 들어 Jackson을 이용해서 자바 객체를 JSON으로 변환할 때에는 MappingJackson2HttpMessageConverter를 사용하고 Jaxb를 이용해서 XML로 변환할때에는 Jaxb2RootElementHttpMessageConverter를 사용한다. 
따라서 JSON으로 변환할 때 사용하는 MappingJackson2HttpMessageConverter를 새롭게 등록해서 날짜 형식을 원하는 형식으로 변환하도록 설정하면 모든 날짜 형식에 동일한 변환 규칙을 적용할 수 있다. 

```Java
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"controller", "service", "dao"})
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void extendMessageConverts(List<HttpMessageConverter<?>> converters){
		ObjectMapper objectMapper = Jackson2ObjectMapperBuilder
				.json()
				.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
				.build();
		converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
	}
    //생략......
}
```
extendMessageConverters() 메서드는 WebMvcConfigurer 인터페이스에 정의된 메서드로서 HttpMessageConverter를 추가로 설정할 때 사용한다. 
@EnableWebMvc 어노테이션을 사용하면 스프링 MVC는 여러 형식으로 변환할 수 있는 HttpMessageConverter를 미리 등록한다. 
extendMessageConverters()는 등록된 HttpMessageConverter목록을 파라미터로 받는다. 

미리 등록된 HttpMessageConverter에는 Jackson을 이용하는 것도 포함되어 있기 때문에 새로 생성한 HttpMessageConverter는 목록의 제일 앞에 위치시켜야 한다. 
그래야 가장 먼저 적용된다. 이를 위해 새로운 HttpMessageConverter를 0번 인덱스에 추가했다.  

이 코드는 JSON으로 변환할 때 사용할 ObjectMapper를 생성한다. Jackson2ObjectMapperBuilder는 ObjectMapper를 보다 쉽게 생성할 수 있도록 스프링이 제공하는 클래스이다. 
featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) 코드는 Jackson이 날짜 형식을 출력할 때 유닉스 타임 스탬프로 출력하는 기능을 비활성화한다. 
이 기능을 비활성화하면 ObjectMapper는 날짜 타입의 값을 ISO-8601 형식으로 출력한다. 

<br>

모든 java.util.Date 타입의 값을 원하는 형식으로 출력하도록 설정하고 싶다면 Jackson2ObjectMapperBuilder#simpleDateFormat() 메서드를 이용해서 패턴을 지정한다. 
```Java
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"controller", "service", "dao"})
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void extendMessageConverts(List<HttpMessageConverter<?>> converters){
		ObjectMapper objectMapper = Jackson2ObjectMapperBuilder
				.json()
				.simpleDateFormat("yyyyMMddHHmmss")
				.build();
		converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
	}
    //생략......
}
```

모든 LocalDateTime 타입에 대해 ISO-8601 형식 대신 원하는 패턴을 설정하고 싶다면 다음과 같이 serializerByType() 메서드를 이용해서 LocalDateTime 타입에 대한 JsonSerializer를 직접 설정하면 된다. 
```Java
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters){
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		ObjectMapper objectMapper = Jackson2ObjectMapperBuilder
				.json()
				.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter))
				.build();
		converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
	}
}
```

<br>

#### 16.3.3 응답 데이터의 컨텐츠 형식

그림과 같이 응답 헤더의 Content-Type이 application/json 인 것을 알 수 있다. 

![spring5_16_3](/assets/book/spring5/spring5_16_3.png){: width="100%" height="auto"}  

<br>

### 16.4 @RequestBody로 JSON 요청 처리
POST방식이나 PUT방식을 사용하면 쿼리 문자열 형식이 아니라 다음과 같은 JSON형식의 데이터를 요청 데이터로 전송할 수 있다. 
```Json
{
    "name": "이름",
    "age": 17
}
```
JSON 형식으로 전송된 요청 데이터를 커맨드 객체로 전달받는 방법은 매우 간단하다. 커맨드 객체에 @RequestBody 어노테이션을 붙이기만 하면 된다. 

```Java
@PostMapping("/api/members")
public void newMember(@RequestBody @Valid RegisterRequest regReq, HttpServletResponse response) throws IOException {
    try {
        Long newMemberId = registerService.regist(regReq);
        response.setHeader("Location", "/api/members" + newMemberId);
        response.setStatus(HttpServletResponse.SC_CREATED);

    } catch (DuplicateMemberException dupEx) {
        response.sendError(HttpServletResponse.SC_CONFLICT);
    }
}
```
@RequestBody 어노테이션을 커맨드 객체에 붙이면 JSON 형식의 문자열을 해당 자바 객체로 변환한다. 
스프링 MVC가 JSON 형식으로 전송된 데이터를 올바르게 처리하려면 요청 컨텐츠 application/json이어야 한다. 
보통 POST 방식의 폼 데이터는 쿼리 문자열인 "p1=v1&p2=v2"로 전송되는데 이때 컨텐츠 타입은 application/x-www-form-urlencoded이다. 
쿼리 문자열 대신 JSON 형식을 사용하려면 application/json 타입으로 데이터를 전송할 수 있는 별도 프로그램이 필요하다. 

![spring5_16_6](/assets/book/spring5/spring5_16_6.png){: width="100%" height="auto"}  

응답상태가 201인 것을 알 수 있다. 또한 "Location" 헤더가 응답 결과에 포함되어 있다. 

<br>

#### 16.4.1 JSON 데이터의 날짜 형식 다루기 
별도 설정을 하지 않으면 다음 패턴(시간대가 없는 JSR-8601)의 문자열을 LocalDateTime과 Date로 변환한다.
```txt
yyyy-MM-dd HH:mm:ss
```

특정 패턴을 가진 문자열을 LocalDateTime이나 Date타입으로 변환하고 싶다면 @JsonFormat 어노테이션의 pattern 속성을 사용해서 패턴을 지정한다.
```Java
@JsonFormat(pattern="yyyyMMddHHmmss")
private LocalDateTime birthDateTime;

@JsonFormat(pattern="yyyyMMdd HHmmss")
private LocalDateTime birthDate;
```

특정 속성이 아니라 해당 타입을 갖는 모든 속성에 적용하고 싶다면 스프링 MVC 설정을 추가하면 된다. 
```Java
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"controller", "service", "dao"})
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters){
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		ObjectMapper objectMapper = Jackson2ObjectMapperBuilder
				.json()
				.featuresToEnable(SerializationFeature.INDENT_OUTPUT)
				.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter))
				.simpleDateFormat("yyyyMMdd HHmmss")
				.build();
		converters.add(0, new MappingJackson2HttpMessageConverter(objectMapper));
	}
}
```
deserializerByType()은 JSON 데이터를 LocalDateTime 타입으로 변환할 때 사용할 패턴을 지정하고 simpleDateFormat()은 Date 타입으로 변환할 때 사용할 패턴을 지정한다.
simpleDateFormat()은 Date 타입을 JSON 데이터로 변환할 때에도 사용된다는 점에 유의한다. 

<br>

#### 16.4.2 요청 객체 검증하기
regReq 파라미터에 @Valid 어노테이션이 붙어있다.  

```Java
@RestController
public class RestMemberController {
    @PostMapping("/api/members")
    public void newMember(@RequestBody @Valid RegisterRequest regReq, HttpServletResponse response) throws IOException {
        try {
            Long newMemberId = registerService.regist(regReq);
            response.setHeader("Location", "/api/members" + newMemberId);
            response.setStatus(HttpServletResponse.SC_CREATED);

        } catch (DuplicateMemberException dupEx) {
            response.sendError(HttpServletResponse.SC_CONFLICT);
        }
    }
}
```
JSON 형식으로 전송한 데이터를 변환한 객체도 동일한 방식으로 @Valid 어노테이션이나 Validator를 이용해서 검증할 수 있다. 
@Valid 어노테이션을 사용한 경우 검증에 실패하면 400(Bad Request) 상태 코드를 응답한다. 

Validator를 사용할 경우 다음과 같이 직접 상태 코드를 처리해야 한다. 
```Java
@RestController
public class RestMemberController {
    @PostMapping("/api/members")
    public void newMember(@RequestBody @Valid RegisterRequest regReq, Errors errors, HttpServletResponse response) throws IOException {
        try {
            
            new RegisterRequestValidator().validate(regReq, errors);
            if(errors.hasErrors()){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            //......생략
            
        } catch (DuplicateMemberException dupEx) {
            response.sendError(HttpServletResponse.SC_CONFLICT);
        }
    }
}
```

<br>

### 16.5 ResponseEntity로 객체 리턴하고 응답 코드 지정하기 
지금까지 예제 코드는 상태 코드를 지정하기 위해 HttpServletResponse의 setStatus()메서드와 sendError() 메서드를 사용했다. 
문제는 HttpServletResponse를 이용해서 404 응답을 하면 JSON 형식이 아닌 서버가 기본으로 제공하는 HTML을 응답 결과로 제공한다는 점이다. 
API를 호출하는 프로그램 입장에서 404나 500과 같이 처리에 실패한 경우 HTML 응답 데이터 대신에 JSON 형식의 응답 데이터를 전송해야 API 호출 프로그램이 일관된 방법으로 응답을 처리할 수 있을 것이다. 

#### 16.5.1 ResponseEntity를 이용한 응답 데이터 처리 
정상인 경우와 비정상적인 경우 모두 JSON 응답을 전송하는 방법은 ResponseEntity를 사용하는 것이다. 
먼저 에러 상황일 때 응답으로 사용할 ErrorResponse 클래스를 다음과 같이 작성한다. 
```Java
package controller;

public class ErrorResponse {
    private String message;

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}
```
ResponseEntity를 이용하면 member() 메서드를 아래와 같이 구현할 수 있다. 

```Java
@RestController
public class RestMemberController {

    @GetMapping("/api/members/{id}")
    public ResponseEntity<Object> member(@PathVariable Long id) {
        Member member = memberDao.selectById(id);
        if (member == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("no member"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(member);
    }
}
```
스프링 MVC는 리턴 타입이 ResponseEntity이면 ResponseEntity의 body로 지정한 객체를 사용해서 변환을 처리한다. 
위에서는 member를 body로 지정했는데, 이 경우 member 객체를 JSON으로 변환한다. 
동일하게 ErrorResponse 객체를 body로 지정했으므로 member가 null이면 ErrorResponse를 JSON으로 변환한다. 

```Json
// http://localhost:8080/api/members/7
{
    "id": 7,
    "email": "postman1@test.com",
    "name": "postman",
    "registerDateTime": "2022-04-11 11:28:20"
}

// http://localhost:8080/api/members/8
{
    "message": "no member"
}
```

ResponseEntity를 생성하는 기본 방법은 status와 body를 이용해서 상태 코드와 JSON으로 변환할 객체를 지정하는 것이다. 
```Java
ResponseEntity.status(statusCode).body(Object)
```
상태 코드는 HttpStatus 열거 타입에 정의된 값을 이용해서 정의한다.  

만약 몸체 내용이없다면 body를 지정하지 않고 build()로 바로 생성한다. 
```Java
ResponseEntity.status(HttpStatus.NOT_FOUND).build()
```

<br>

#### 16.5.2 @ExceptionHandler 적용 메서드에서 ResponseEntity로 응답하기 

@ExceptionHandler 어노테이션을 적용한 메서드에서 에러 응답을 처리하도록 구현하면 중복을 없앨 수 있다. 
```Java
@RestController
public class RestMemberController {

    @GetMapping("/api/members/{id}")
    public ResponseEntity<Object> member(@PathVariable Long id) {
        Member member = memberDao.selectById(id);
        if (member == null) {
            throw new MemberNotFoundException();
        }
        return ResponseEntity.status(HttpStatus.OK).body(member);
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoData(){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("no member"));
    }
}
```

회원 데이터가 존재하지 않으면 MemberNotFoundException이 발생한다. 이 익셉션이 발생하면 @ExceptionHandler 어노테이션을 사용한 handleNoData() 메서드가 에러를 처리한다. 

@RestControllerAdvice 어노테이션을 이용해서 에러 처리 코드를 별도 클래스로 분리할 수도 있다. 
@RestControllerAdvice 어노테이션은 @ControllerAdvice 어노테이션과 동일하다. 차이는 @RestController 어노테이션과 같이 동일하게 응답을 JSON, XML과 같은 형식으로 변환한다는 것이다. 

```Java
@RestControllerAdvice("controller")
public class ApiExceptionHandler {

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoData(){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("no member"));
    }
}
```

@RestControllerAdvice 어노테이션을 사용하면 에러 처리 코드가 한 곳에 모여 효과적으로 에러 응답을 관리할 수 있다. 




<br><br><br>

---

**Reference**  
- 초보 웹 개발자를 위한 스프링 5 (최범균)

---


