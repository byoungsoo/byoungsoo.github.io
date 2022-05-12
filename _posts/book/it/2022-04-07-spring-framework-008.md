---
layout: post
title: "Spring 프로그래밍 (Chapter 14)[스프링 MVC 4: 날짜 값 변환, @PathVariable, 익셉션 처리]"
author: "Bys"
category: it_book
date: 2022-04-07 01:00:00
tags: book programming spring framework mvc exception @pathvariable @exceptionhandler @controlleradvice
---

## 14. MVC 4: 날짜 값 변환, @PathVariable, 익셉션 처리

### 14.2 날짜를 이용한 회원 검색 기능 

`MemberDao`  
```Java
public class MemberDao {

	private JdbcTemplate jdbcTemplate;
    //생략 .......
    
    public List<Member> selectByRegdate(LocalDateTime from, LocalDateTime to){
        List<Member> results = jdbcTemplate.query(
                "select * from MEMBER where REGDATE between ? and ? " +
                        "order by REGDATE desc",
                new RowMapper<Member>() {
                    @Override
                    public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Member member = new Member(
                                rs.getString("EMAIL"),
                                rs.getString("PASSWORD"),
                                rs.getString("NAME"),
                                rs.getTimestamp("REGDATE").toLocalDateTime());

                        member.setId(rs.getLong("ID"));
                        return member;
                    }
                },
                from, to);
        return results;
    }
    
    //생략 .......
    
}
```

### 14.3 커맨드 객체 Date 타입 프로퍼티 변환 처리: @DateTimeFormat

`검색을 위한 입력 폼`  
```html
<input type="text" name="from" />
<input type="text" name="to" />
```

여기서 문제는 input 태그에 입력한 문자열을 LocalDateTime 타입으로 변환해야 한다는 것이다. input 태그에 2022년 4월 7일 오후 10시를 표현하기 위해 "2022040720"로 입력해야 한다고 해보자. 
"2022040720" 문자열을 알맞게 LocalDateTime 타입으로 변환해야 한다.  

스프링은 Long이나 int와 같은 기본 데이터 타입으로의 변환은 기본적으로 처리해주지만 LocalDateTime 타입으로의 변환은 추가 설정이 필요하다. 
작성한 ListCommand 클래스의 두 필드에 @DateTimeFormat 어노테이션을 적용하면 된다.  

```Java
public class ListCommand {

	@DateTimeFormat(pattern="yyyyMMddHH")
	private LocalDateTime from;
	@DateTimeFormat(pattern="yyyyMMddHH")
	private LocalDateTime to;

    //생략......
}
```

<br>

#### 14.3.1 변환 에러 처리

폼에서 from이나 to에 '20180301'을 입력해보면 원래 지정한 형식은 "yyyyMMddHH"이기 때문에 "yyyMMdd" 부분만 입력하면 지정한 형식과 일치하지 않게 된다. 
형식에 맞지 않은 값을 폼에 입력한 뒤 '조회'를 실행하면 400에러가 발생한다.  

잘못 입력했을 때 400에러 대신 폼에 알맞은 에러 메시지를 보여주고 싶다면 Errors 타입 파라미터를 요청매핑 어노테이션 적용 메서드에 추가하면 된다. 

```Java
@Controller
public class MemberListController {

	@Autowired
	private MemberDao memberDao;

	@RequestMapping("/members")
	public String list(@ModelAttribute("cmd")ListCommand listCommand, Errors errors, Model model){

		if(errors.hasErrors()){
			return "member/memberList";
		}
		if(listCommand.getFrom() != null && listCommand.getTo() != null) {
			List<Member> members = memberDao.selectByRegdate(listCommand.getFrom(), listCommand.getTo());
			model.addAttribute("members", members);
		}

		return "member/memberList";
	}
}
```

요청 매핑 어노테이션 적용 메서드가 Errors 타입 파라미터를 가질 경우 @DateTimeFormat에 지정한 형식에 맞지 않으면 Errors 객체에 "typeMismatch" 에러 코드를 추가한다. 
에러 코드로 "typeMismatch"를 추가하므로 메세지 프로퍼티 파일에 해당 메시지를 추가하면 에러 메시지를 보여줄 수 있다.  

`label.properties`  
```properties
typeMismatch.java.time.LocalDateTime=잘못된 형식
```

`memberList.jsp`
```jsp
<form:form modelAttribute="cmd">
<p>
    <label>from: <form:input path="from" /></label>
    <form:errors path="from" />
    ~
    <label>to:<form:input path="to" /></label>
    <form:errors path="to" />
    <input type="submit" value="조회">
</p>
</form:form>
```

<br>

#### 14.4 변환 처리에 대한 이해  
@DataTimeFormat 어노테이션을 사용하면 지정한 형식의 문자열을 LocalDateTime타입으로 변환해준다는 것을 예제를 통해 확인했다. 
여기서 궁금중이 하나 생긴다. 누가 문자열을 LocalDateTime 타입으로 변환하느지에 대한 것이다. 답은 WebDataBinder에 있다. 

스프링 MVC는 요청 매핑 어노테이션 적용 메서드와 DispatcherServlet 사이를 연결하기 위해 RequestMappingHandlerAdapter 객체를 사용한다. 
이 핸들러 어댑터 객체는 요청 파라미터와 커맨드 객체 사이의 변환 처리를 위해 WebDataBinder를 이용한다. 

WebDataBinder는 직접 타입을 변환하지 않고 ConversionService에 그 역할을 위임한다. 
스프링 MVC를 위한 설정인 @EnableWebMvc 어노테이션을 사용하면 DefaultFormattingConversionService를 ConversionService로 사용한다.  

DefaultFormattingConversionService는 int, long과 같은 기본 데이터 타입뿐만 아니라 @DateTimeFormat 어노테이션을 사용한 시간 관련 타입 변환 기능을 제공한다. 
이런 이유로 커맨드로 사용할 클래스에 @DateTimeFormat 어노테이션만 붙이면 지정한 형식의 문자열을 시간 타입 값으로 받을 수 있는 것이다. 

<br>

#### 14.6 @PathVariable을 이용한 경로 변수 처리  

다음은 ID가 10인 회원의 정보를 조회하기 위한 URL이다. 
```bash
http://localhost:8080/members/10
```

이 형식의 URL을 사용하면 각 회원마다 경로의 마지막 부분이 달라진다. 이렇게 경로의 일부가 고정되지 않고 달라질 때 사용할 수 있는 것이 @PathVariable 어노테이션이다. 
@PathVariable 어노테이션을 사용하면 다음과 같은 방법으로 가변 경로를 처리할 수 있다. 

```Java
@Controller
public class MemberDetailController {

	@Autowired
	private MemberDao memberDao;

	@GetMapping("/members/{id}")
	public String detail(@PathVariable("id") Long memberId, Model model){

		Member member = memberDao.selectById(memberId);
		if(member == null){
			throw new MemberNotFoundException();
		}
		model.addAttribute("member", member);

		return "member/memberDetail";
	}
}
```
매핑 경로에 '{경로변수}'와 같이 중괄호로 둘러 쌓인 부분을 경로 변수라고 부른다. "{경로변수}"에 해당하는 값은 같은 경로 변수 이름을 지정한 @PathVariable 파라미터에 전달된다. 
"/members/{id}"에서 {id}에 해당하는 부분의 경로 값을 @PathVariable("id") 어노테이션이 적용된 memberId 파라미터에 전달한다. 
memberId 파라미터의 타입은 Long인데 이 경우 String 타입 값 "0"을 알맞게 Long 타입으로 변환한다.  

<br>

#### 14.7 컨트롤러 익셉션 처리하기
MemberDetailController에 없는 ID를 경로변수로 사용하면 500오류(MemberNotFoundException)가 발생한다. 
MemberDetailController 가 사용하는 경로 변수는 Long 타입인데 실제 요청 경로에 숫자가 아닌 문자를 입력하면 400에러가 발생한다. 

MemberNotFoundException은 try-catch로 잡은 뒤 안내 화면을 보여주는 뷰를 보여주면 될 것 같다. 그런데 타입 변환 실패에 따른 익셉션은 어떻게 해야 에러화면을 보여줄 수 있을까?
이럴 때 유용하게 사용할 수 있는 것이 바로 @ExceptionHandler 어노테이션이다. 같은 컨트롤러에 @ExceptionHandler 어노테이션을 적용한 메서드가 존재하면 그 메서드가 익셉션을 처리한다. 
따라서 컨트롤러에서 발생한 익셉션을 직접 처리하고 싶다면 @ExceptionHandler 어노테이션을 적용한 메서드를 구현하면 된다. 

```Java
@Controller
public class MemberDetailController {

	@Autowired
	private MemberDao memberDao;

	@GetMapping("/members/{id}")
	public String detail(@PathVariable("id") Long memberId, Model model){

		Member member = memberDao.selectById(memberId);
		if(member == null){
			throw new MemberNotFoundException();
		}
		model.addAttribute("member", member);
		return "member/memberDetail";
	}

	@ExceptionHandler(TypeMismatchException.class)
	public String handleTypeMismatchException(){
		return "member/invalid";
	}

	@ExceptionHandler(MemberNotFoundException.class)
	public String memberNotFoundException(){
		return "member/noMember";
	}
}
```
@ExceptionHandler의 값으로 TypeMismatchException.class를 주었다. 
이 익센셥은 경로 변수값의 타입이 올바르지 않을 때 발생한다. 이 익셉션이 발생하면 에러 응답을 보내는 대신 handleTypeMismatchException() 메서드를 실행한다. 
비슷하게 detail() 메서드를 실행하는 과정에서 MemberNotFoundException이 발생하면 handleNotFoundException() 메서드를 이용해서 익셉션을 처리한다. 

#### 14.7.1 @ControllerAdvice를 이용한 공통 익셉션 처리 
컨트롤러 클래스에 @ExceptionHandler 어노테이션을 적용하면 해당 컨트롤러에서 발생한 익셉션만을 처리한다. 다수의 컨트롤러에서 동일 타입의 익셉션이 발생할 수도 있다. 
각 컨트롤러 클래스마다 익셉션 처리 메서드를 구현하는 것은 불필요한 코드 중복을 발생시킨다.  

여러 컨트롤러에서 동일하게 처리할 익셉션이 발생하면 @ControllerAdvice 어노테이션을 이용해서 중복을 없앨 수 있다. 
```Java
@ControllerAdvice("controller")
public class CommonExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(){
        return "error/commonException";
    }
}
```
@ControllerAdvice 어노테이션이 적용된 클래스는 지정한 범위의 컨트롤러에 공통으로 사용될 설정을 지정할 수 있다. 
위 코드는 "controller" 패키지와 그 하위 패키지에 속한 컨트롤러 클래스를 위한 공통 기능을 정의했다. 
controller 패키지와 그 하위 패키지에 속한 컨트롤러에서 RunTimeException이 발생하면 handleRuntimeException() 메서드를 통해서 익셉션을 처리한다. 

@ControllerAdvice 적용 클래스가 동작하려면 해당 클래스를 스프링에 빈으로 등록해야 한다.  

<br> 

#### 14.7.2 @ExceptionHandler 적용 메서드의 우선 순위 
@ControllerAdvice 클래스에 있는 @ExceptionHandler 메서드와 컨트롤러 클래스에 있는 @ExceptionHandler 메서드 중 컨트롤러 클래스에 적용된 @ExceptionHandler 메서드가 우선한다. 
즉 컨트롤러의 메서드를 실행하는 과정에서 익셉션이 발생하면 다음의 순서로 익셉션을 처리할 @ExceptionHandler 메서드를 찾는다. 
1. 같은 컨트롤러에 위치한 @ExceptionHandler 메서드 중 해당 익셉션을 처리할 수 있는 메서드를 검색
2. 같은 클래스에 위치한 메서드가 익셉션을 처리할 수 없을 경우 @ControllerAdvice 클래스에 위치한 @ExceptionHandler 메서드를 검색 

#### 14.7.3 @ExceptionHandler 어노테이션 적용 메서드의 파라미터와 리턴 타입
@ExceptionHandler 어노테이션을 붙인 메서드는 다음 파라미터를 가질 수 있다.

- HttpServletRequest, HttpServletResponse, HttpSession
- Model
- Exception

리턴 가능한 타입은 다음과 같다. 
- ModelAndView
- String (뷰 이름)
- @ResponseBody 어노테이션을 붙인 경우, 임의 객체 
- ResponseEntity



<br><br><br>

---

**Reference**  
- 초보 웹 개발자를 위한 스프링 5 (최범균)

---


