---
layout: post
title: "Spring5 프로그래밍 (Chapter 1~5)"
author: "Bys"
category: it_book
date: 2022-03-13 01:00:00
tags: programming spring
---

### 2. 스프링 시작하기  

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

<br>

#### Spring은 객체 컨테이너
위 코드에서 핵심은 AnnotationConfigApplicationContext 클래스다. 
스프링의 핵심 기능은 객체를 생성하고 초기화 하는 것이다. 이와 관련된 기능은 ApplicationContext라는 인터페이스에 정의되어 있다. 
AnnotationConfigApplicationContext 클래스는 ApplicationContext 인터페이스를 알맞게 구현한 클래스 중 하나다. 이 클래스는 자바 클래스에서 정보를 읽어와 객체 생성과 초기화를 수행한다. 
XML파일이나 그루비 설정 코드를 이용해서 객체 생성/초기화를 수행하는 클래스도 존재한다.  

어떤 구현 클래스를 사용하든, 각 구현 클래스는 설정 정보로부터 빈(Bean)이라고 불리는 객체를 생성하고 그 객체를 내부에 보관한다. 그리고 getBean() 메서드를 실행하면 해당하는 빈 객체를 제공한다.  

ApplicationContext(또는 BeanFactory)는 빈 객체의 생성, 초기화, 보관, 제거 등을 관리하고 있어서 ApplicationContext를 컨테이너(Container)라고도 부른다. 
스프링 컨테이너(ApplicationContext)는 내부적으로 빈 객체와 빈 이름을 연결하는 정보를 갖는다.  

<br>

#### 싱글톤(Singleton) 객체  

```Java
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx =
				new AnnotationConfigApplicationContext(AppContext.class);
		Greeter g1 = ctx.getBean("greeter", Greeter.class);
		Greeter g2 = ctx.getBean("greeter", Greeter.class);
		System.out.println("(g1 == g2) = " + (g1 == g2));
		ctx.close();
	}
}
```
이름이 "greeter"인 빈 객체를 구해서 각각 g1, g2 변수에 할당한다. 그리고 실제 실행 결과는 g1==g2 true로 출력이 된다.  

별도 설정을 하지 않을 경우 스프링은 한 개의 빈 객체만을 생성하며, 이때 빈 객체는 '싱글톤(Singleton) 범위를 갖는다'고 표현한다. 
싱글톤은 단일 객체를 의미하는 단어로서 스프링은 기본적으로 한 개의 @Bean 어노테이션에 대해 한 개의 빈 객체를 생성한다.  

<br><br>

### 3. 스프링 DI(Dependency Injection)  
DI는 의존하는 객체를 직접 생성하는 대신 의존 객체를 전달받는 방식을 사용한다.  

```Java
public class MemberRegisterService {

   //private MemberDao memberDao = new MemberDao();

	private MemberDao memberDao;
	public MemberRegisterService(MemberDao memberDao) {
		this.memberDao = memberDao;
	}
}
```
직접 의존 객체를 생성했던 코드와 달리 바뀐 코드는 의존 객체를 직ㅈ버 생성하지 않는다. 대신 생성자를 통해서 의존 객체를 전달받는다. 
즉 생성자를 통해 MemberRegisterService가 의존 하고 있는 MemberDao 객체를 주입 받는 것이다.  

DI를 적용한 결과 MemberRegisterService 클래스를 사용하는 코드는 다음과 같이 MemberRegisterService객체를 생성할 때 생성자에 MemberDao 객체를 전달해야 한다.  
```Java
MemberDao dao = new MemberDao();
//의존 객체를 생성자를 통해 주입한다. 
MemberRegisterService svc = new MemberRegisterService(dao);
```

<br>

#### 객체 조립기  
main 메서드에서 의존 대상 객체를 생성하고 주입하는 방법이 나쁘진 않으나 좀 더 나은 방법은 객체를 생성하고 의존 객체를 주입해주는 클래스를 따로 작성하는 것이다. 
의존 객체를 주입한다는 것은 서로 다른 두 객체를 조립한다고 생각할 수 있는데, 이런 의미에서 이 클래스를 조립기(Assembler)라고도 표현한다.  

`Assembler`  
```Java
public class Assembler {

	private MemberDao memberDao;
	private MemberRegisterService regSvc;
	private ChangePasswordService pwdSvc;

	public Assembler() {
		memberDao = new MemberDao();
		regSvc = new MemberRegisterService(memberDao);
		pwdSvc = new ChangePasswordService();
		pwdSvc.setMemberDao(memberDao);
	}

	public MemberDao getMemberDao() {
		return memberDao;
	}

	public MemberRegisterService getMemberRegisterService() {
		return regSvc;
	}

	public ChangePasswordService getChangePasswordService() {
		return pwdSvc;
	}

}
```

`MainForAssembler`  
```Java
public class MainForAssembler {

	public static void main(String[] args) throws IOException {
		BufferedReader reader = 
				new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.println("명령어를 입력하세요:");
			String command = reader.readLine();
			if (command.equalsIgnoreCase("exit")) {
				System.out.println("종료합니다.");
				break;
			}
			if (command.startsWith("new ")) {
				processNewCommand(command.split(" "));
				continue;
			} else if (command.startsWith("change ")) {
				processChangeCommand(command.split(" "));
				continue;
			}
			printHelp();
		}
	}

	private static Assembler assembler = new Assembler();

	private static void processNewCommand(String[] arg) {
		MemberRegisterService regSvc = assembler.getMemberRegisterService();
		RegisterRequest req = new RegisterRequest();
		req.setEmail(arg[1]);
		req.setName(arg[2]);
		req.setPassword(arg[3]);
		req.setConfirmPassword(arg[4]);
		
		regSvc.regist(req);
	}

	private static void processChangeCommand(String[] arg) {
		ChangePasswordService changePwdSvc = 
				assembler.getChangePasswordService();
	}

}
```
조립기는 객체를 생성하고 의존 객체를 주입하는 기능을 제공한다. 또한 특정 객체가 필요한 곳에 객체를 제공한다.  
Assembler는 자신이 생성하고 조립한 객체를 리턴하는 메서드를 제공한다. 

<br>

#### 스프링의 DI설정  
의존, DI, 조립기에 대해 먼저 알아본 이유는 스프링이 DI를 지원하는 조립기이기 때문이다.  
스프링은 앞서 구현한 조립기와 유사한 기능을 제공해준다. 스프링은 Assembler 클래스의 생성자 코드처럼 필요한 객체를 생성하고 생성한 객체에 의존을 주입한다. 
스프링은 범용 조립기이다.  

**스프링을 이용한 객체 조립과 사용**  
```Java
package config;

@Configuration
public class AppCtx {

	@Bean
	public MemberDao memberDao() {
		return new MemberDao();
	}
	
	@Bean
	public MemberRegisterService memberRegSvc() {
		return new MemberRegisterService(memberDao());
	}
	
	@Bean
	public ChangePasswordService changePwdSvc() {
		ChangePasswordService pwdSvc = new ChangePasswordService();
		pwdSvc.setMemberDao(memberDao());
		return pwdSvc;
	}
}
```
@Configuration 어노테이션은 스프링 설정 클래스를 의미한다. 이 어노테이션을 붙여야 스프링 설정 클래스로 사용할 수 있다. 
@Bean 어노테이션은 해당 메서드가 생성한 객체를 스프링 빈이라고 설정한다. 각각의 메서드마다 한 개의 빈 객체를 생성한다.  
memberRegSvc()는 MemberRegisterService생성자를 통해 memberDao를 주입한다.  
changePwdSvc()는 ChangePasswordService 세터(setMemberDao)를 통해 memberDao를 주입한다.  

> 생성자 DI 방식과 세터 메서드 DI 방식은 각자 장점이 있다. 각 필요한 시점에 알맞게 사용하면 된다. 
- 생성자 방식: 빈 객체를 생성하는 시점에 모든 의존 객체가 주입된다. 
- 세터 메서드 방식: 세터 메서드 이름을 통해 어떤 의존 객체가 주입되는지 알 수 있다. 



설정 클래스를 만들었다고 해서 끝난 것이 아니다. 객체를 생성하고 의존 객체를 주입하는 것은 스프링 컨테이너이므로 설정 클래스를 이용해서 컨테이너를 생성해야 한다. 
AnnotationConfigApplicationContext 클래스를 이용해서 스프링 컨테이너를 생성할 수 있다.  
```Java
ApplicationContext ctx = new AnnotationConfigApplicationContext(AppCtx.class);
```

컨테이너를 생성하면 getBean() 메서드를 이용해서 사용할 객체를 구할 수 있다.  
```Java
MemberRegisterService regSvc = ctx.getBean("memberRegSvc", MemberRegisterService.class);
```

위 의 코드에서는 Assembler를 ApplicationContext를 사용하도록 수정 해주면 아래와 같다.  
```Java
//private static Assembler assembler = new Assembler();
private static ApplicationContext ctx = null;

ctx = new AnnotationConfigApplicationContext(AppCtx.class);

//MemberRegisterService regSvc = assembler.getMemberRegisterService();
MemberRegisterService regSvc = ctx.getBean("memberRegSvc", MemberRegisterService.class);
```

<br>

#### 두 개 이상의 설정 파일 사용  
설정하는 빈의 개수가 증가하면 한 개의 클래스 파일에 설정하는 것보다 영역별로 설정 파일을 나누면 관리하기 편해진다.  

```Java
package config;

@Configuration
public class AppConf1 {
	@Bean
	public MemberDao memberDao() {
		return new MemberDao();
	}
	@Bean
	public MemberPrinter memberPrinter() {
		return new MemberPrinter();
	}
}
```

```Java
package config;

@Configuration
public class AppConf2 {

	@Autowired
	private MemberDao memberDao;
	@Autowired
	private MemberPrinter memberPrinter;
	
	@Bean
	public MemberRegisterService memberRegSvc() {
		return new MemberRegisterService(memberDao);
	}
	@Bean
	public ChangePasswordService changePwdSvc() {
		ChangePasswordService pwdSvc = new ChangePasswordService();
		pwdSvc.setMemberDao(memberDao);
		return pwdSvc;
	}
	@Bean
	public MemberListPrinter listPrinter() {
		return new MemberListPrinter(memberDao, memberPrinter);
	}
	@Bean
	public MemberInfoPrinter infoPrinter() {
		MemberInfoPrinter infoPrinter = new MemberInfoPrinter();
		infoPrinter.setMemberDao(memberDao);
		infoPrinter.setPrinter(memberPrinter);
		return infoPrinter;
	}
	@Bean
	public VersionPrinter versionPrinter() {
		VersionPrinter versionPrinter = new VersionPrinter();
		versionPrinter.setMajorVersion(5);
		versionPrinter.setMinorVersion(0);
		return versionPrinter;
	}
}
```

@Autowired 어노테이션은 스프링의 자동 주입 기능을 위한 것이다. 이 설정은 의존 주입과 관련이 있다. 
스프링 설정 클래스의 필드에 @Autowired 어노테이션을 붙이면 해당 타입의 빈을 찾아서 필드에 할당한다. 
위 설정의 경우 스프링 컨테이너는 MemberDao 타입의 빈을 memberDao 필드에 할당한다.  


설정 클래스가 두 개 이상이어도 스프링 컨테이너를 생성하는 코드는 크게 다르지 않다.  
```Java
ctx = new AnnotationConfigApplicationContext(AppConf1.class, AppConf2.class);
```
다음과 같이 파라미터로 설정 클래스를 추가로 전달하면 된다. AnnotationConfigApplicationContext의 생성자의 인자는 가변 인자이기 때문에 설정 클래스 목록을 콤마로 구분해서 전달하면 된다. 

<br>

#### @Import 어노테이션 사용  
두 개 이상의 설정 파일을 사용하는 또 다른 방법은 @Import 어노테이션을 사용하는 것이다. 
@Import 어노테이션은 함께 사용할 설정 클래스를 지정한다.  
```Java
package config;

@Configuration
@Import({AppConf2.class})
public class AppConfImport {

	@Bean
	public MemberDao memberDao() {
		return new MemberDao();
	}
	
	@Bean
	public MemberPrinter memberPrinter() {
		return new MemberPrinter();
	}
}
```
AppConfImport 설정 클래스를 사용하면, @Import 어노테이션으로 지정한 AppConf2 설정 클래스도 함께 사용하기 때문에 스프링 컨테이너를 생성할 때 AppConf2 설정 클래스를 지정할 필요가 없다. 

```Java
public class MainForImport {

	private static ApplicationContext ctx = null;
	
	public static void main(String[] args) throws IOException {
		ctx = new AnnotationConfigApplicationContext(AppConfImport.class);
		......
	}
	......
}
```
위 코드 처럼 AppConfImport 클래스만 사용하면 AppConf2 클래스의 설정도 함께 사용해서 컨테이너를 초기화한다.  


```Java
@Import({AppConf1.class, AppConf2.class})
```
배열을 이용해서 두 개 이상의 설정 클래스도 지정 가능하며, @Import를 사용해서 포함한 설정 클래스가 다시 @Import를 사용할 수도 있다.  

<br>

#### getBean() 메서드 사용  

```Java
VersionPrinter versionPrinter = ctx.getBean("versionPrinter", VersionPrinter.class);
```
여기서 getBean() 메서드의 첫 번째 인자는 빈의 이름이고 두 번째 인자는 빈의 타입이다.  
getBean() 메서드를 호출할 때 존재하지 않는 빈 이름을 사용하면 Exception이 발생한다. getBean()메서드에 지정한 타입이 달라도 Exception이 발생한다.  

```Java
VersionPrinter versionPrinter = ctx.getBean(VersionPrinter.class);
```
위 소스와 같이 빈 이름을 지정하지 않고 타입만으로도 빈을 구할 수 있다. 
하지만 해당 타입의 빈 객체가 존재하지 않거나 타입이 VersionPrinter인 Bean이 두 개 이상이되어도 Exception이 발생한다. 

<br><br>

### 4. 의존 자동 주입  

#### @Autowired 어노테이션을 이용한 의존 자동 주입  
스프링에서 의존 자동 주입을 설정하려면 @Autowired 어노테이션이나 @Resource 어노테이션을 사용하면 된다. 이 책에서는 @Autowired 사용 방법을 살펴본다.  

`자동 주입 사용 할 때 @Autowired`  
```Java
package spring;

public class ChangePasswordService {

	@Autowired
	private MemberDao memberDao;

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

}

package config;

@Configuration
public class AppCtx {
	@Bean
	public ChangePasswordService changePwdSvc() {
		return new ChangePasswordService();
	}
```

memberDao 필드에 @Autowired 어노테이션을 붙였다. @Autowired 어노테이션을 붙이면 설정 클래스에서 의존을 주입하지 않아도 된다. 
@Autowired 붙어 있으면 스프링이 해당 타입의 빈 객체를 찾아서 필드에 할당한다.  

`자동 주입을 하지 않을 때`  
```Java
@Configuration
public class AppCtx {
	@Bean
	public ChangePasswordService changePwdSvc() {
		ChangePasswordService pwdSvc = new ChangePasswordService();
		// pwdSvc.setMemberDao(memberDao());
		return pwdSvc;
	}
```
@Autowired 어토네이션을 붙였으므로 위의 소스코드에서 Setter 메서드를 통해 의존을 주입하는 코드를 삭제하면 된다.  


@Autowired 어노테이션은 메서드에도 붙일 수 있다.  
```Java
package spring;

public class MemberInfoPrinter {

	private MemberDao memDao;
	private MemberPrinter printer;

	public void printMemberInfo(String email) {
		Member member = memDao.selectByEmail(email);
		if (member == null) {
			System.out.println("데이터 없음\n");
			return;
		}
		printer.print(member);
		System.out.println();
	}

	@Autowired
	public void setMemberDao(MemberDao memberDao) {
		this.memDao = memberDao;
	}

	@Autowired
	@Qualifier("printer")
	public void setPrinter(MemberPrinter printer) {
		this.printer = printer;
	}
}
```

```Java
@Configuration
public class AppCtx {
	@Bean
	public MemberInfoPrinter infoPrinter() {
		MemberInfoPrinter infoPrinter = new MemberInfoPrinter();
		infoPrinter.setMemberDao(memberDao());
		infoPrinter.setPrinter(memberPrinter());
		return infoPrinter;
	}

	@Bean
	public MemberInfoPrinter infoPrinter() {
		MemberInfoPrinter infoPrinter = new MemberInfoPrinter();
		return infoPrinter;
	}
}
```
MemberInfoPrinter의 세터 메서드에 @Autowired 어노테이션을 붙이면 AppCtx 설정 클래스에서 위 와 같이 수정할 수 있다.  

빈 객체의 메서드에 @Autowired 어노테이션을 붙이면 스프링은 해당 메서드를 호출한다. 이때 메서드 파라미터 타입에 해당하는 빈 객체를 찾아 인자로 주입한다.  
@Autowired 어노테이션을 필드나 세터 메서드에 붙이면 스프링은 타입이 일치하는 빈 객체를 찾아서 주입한다. 

<br>

#### @Qualifier 어노테이션을 이용한 의존 객체 선택  
자동 주입 가능한 빈이 두 개 이상이면 자동 주입할 빈을 지정할 수 있는 방법이 필요하다. 이 때 @Qualifier 어노테이션을 사용한다.  

@Qualifier 어노테이션은 두 위치에서 사용 가능하다. 첫 번째는 @Bean 어노테이션을 붙인 빈 설정 메서드다.  
```Java
package config; 

@Configuration
public class AppCtx {
	@Bean
	@Qualifier("printer")
	public MemberPrinter memberPrinter1() {
		return new MemberPrinter();
	}
	@Bean
	public MemberPrinter memberPrinter2() {
		return new MemberPrinter();
	}
}
```

이 코드에서 memberPrinter1() 메서드에 "printer" 값을 갖는 @Qualifier 어노테이션을 붙였다. 이 설ㅈ어은 해당 빈의 한정 값으로 "printer"를 지정한다.  
이렇게 지정한 한정 값은 @Autowired 어노테이션에서 자동 주입할 빈을 한정할 때 사용한다. 이곳이 @Qualifier 어노테이션을 사용하는 두 번째 위치다. 

```Java
package spring;

public class MemberListPrinter {
	
	private MemberDao memberDao;
	private MemberPrinter printer; 
	
	@Autowired
	@Qualifier("printer")
	public void setMemberPrinter(MemberPrinter printer){
		this.printer = printer; 
	}
	
}
```
setMemberPrinter()에 @Autowired 어노테이션을 붙였으므로 MemberPrinter타입의 빈을 자동으로 주입한다. 
이 때 @Qualifier 어노테이션 값이 "printer"이므로 한 정 값이 "printer"인 빈을 의존 주입 후보로 사용한다. MemberPrinter 타입의 반 (memberPrinter1)을 자동 주입 대상으로 사용한다.  

<br>

#### 상위/하위 타입 관계와 자동 주입

```Java
package config;

@Configuration
public class AppCtx {
	@Bean
//	@Qualifier("printer")
	public MemberPrinter memberPrinter1() {
		return new MemberPrinter();
	}
	
	@Bean
//	@Qualifier("summaryPrinter")
	public MemberSummaryPrinter memberPrinter2() {
		return new MemberSummaryPrinter();
	}
```

```Java
package spring;

public class MemberSummaryPrinter extends MemberPrinter {

	@Override
	public void print(Member member) {
		System.out.printf(
				"회원 정보: 이메일=%s, 이름=%s\n", 
				member.getEmail(), member.getName());
	}

}

public class MemberInfoPrinter {

	private MemberDao memDao;
	private MemberPrinter printer;

	@Autowired
	public void setMemberDao(MemberDao memberDao) {
		this.memDao = memberDao;
	}

	@Autowired
//	@Qualifier("printer")
	public void setPrinter(MemberPrinter printer) {
		this.printer = printer;
	}

}
```

실제로 위의 코드를 실행 시키면 아래와 같은 오류가 발생한다.  
```log
Caused by: org.springframework.beans.factory.NoUniqueBeanDefinitionException:  
No qualifying bean of type 'spring.MemberPrinter' available: expected single matching bean but found 2: memberPrinter1,memberPrinter2
```

memberPrinter2 빈이 MemberSummaryPrinter 타입으로 변경했음에도 에러가 발생하는 이유는 MemberSummaryPrinter 클래스가 MemberPrinter 클래스를 상속했기 때문이다. 
MemberSummaryPrinter 클래스는 MemberPrinter 타입에도 할당할 수 있으므로, 스프링 컨테이너는 MemberPrinter 타입 빈을 자동 주입해야 하는 @Autowired 어노테이션 태그를 만나면 
memberPrinter1, memberPrinter2 타입 빈 중에서 어떤 빈을 주입해야 하는지 알 수 없다. 그래서 Exception 이 발생한다.  

<br>

#### @Autowired 어노테이션의 필수 여부 
```Java
public class MemberPrinter {
	private DateTimeFormatter dateTimeFormatter;
	
	public MemberPrinter() {
		dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
	}
	
	public void print(Member member) {
		if (dateTimeFormatter == null) {
			System.out.printf(
					"회원 정보: 아이디=%d, 이메일=%s, 이름=%s, 등록일=%tF\n", 
					member.getId(), member.getEmail(),
					member.getName(), member.getRegisterDateTime());
		} else {
			System.out.printf(
					"회원 정보: 아이디=%d, 이메일=%s, 이름=%s, 등록일=%s\n", 
					member.getId(), member.getEmail(),
					member.getName(), 
					dateTimeFormatter.format(member.getRegisterDateTime()));
		}
	}
	
	@Autowired //(required = false)
	public void setDateFormatter(DateTimeFormatter dateTimeFormatter) {
		this.dateTimeFormatter = dateTimeFormatter;

	}
}
```
dateTimeFormatter 필드가 null이면 날짜 형식을 %tF로 출력하고 이 필드가 null이 아니면 dateTimeFormatter를 이용해서 날짜 형식을 맞춰 출력하도록 print()메서드를 수정했다. 
세터 메서드는 @Autowired 어노테이션을 이용해서 자동 주입하도록 했다.  

print()메서드는 dateTimeFormatter값이 null인 경우에도 동작한다. 즉, 반드시 setDateFormatter() 메서드를 통해서 의존 객체를 주입할 필요는 없다. 
setDateFormatter()에 주입할 빈이 존재하지 않아도 MemberPrinter가 동작하는데는 문제가 없다.  

그런데 @Autowired 어노테이션은 기본적으로 @Autowired 어노테이션을 붙인 타입에 해당하는 빈이 존재하지 않으면 익셉션을 발생시킨다. 
따라서 스프링 컨테이너는 빈 객체인 MemberPrinter에 @Autowired 어노테이션이 붙은 setDateFormatter() 메서드를 실행 시키고 DateTimeFormatter 타입의 빈을 찾아 자동 주입 하다 Exception이 발생한다.  

자동 주입할 대상이 필수가 아닌 경우에는 @Autowired 어노테이션의 required 속성을 false로 지정하면 된다. 
@Autowired 어노테이션의 required 속성을 false로 지정하면 매칭되는 빈이 없어도 Exception이 발생하지 않으며 자동 주입을 수행하지 않는다.  
*위 에에서는 DateTimeFormatter 타입의 빈이 존재하지 않으면 익셉션을 발생하지 않고 setDateFormatter() 메서드를 실행하지 않는다.*  

<br>

```Java
@Autowired
public void setDateFormatter(Optional<DateTimeFormatter> formatterOpt) {
	if (formatterOpt.isPresent()) {
		this.dateTimeFormatter = formatterOpt.get();
	} else {
		this.dateTimeFormatter = null;
	}
}

@Autowired
public void setDateFormatter(@Nullable DateTimeFormatter dateTimeFormatter) {
	this.dateTimeFormatter = dateTimeFormatter;
}
```
스프링5 버전 부터는 @Autowired 어노테이션의 required 속성을 false로 하는 대신에 자바 8의 Optional을 사용해도 된다.  

필수 여부를 지정하는 세 번째 방법은 @Nullable 어노테이션을 사용하는 것이다. 
스프링 컨테이너는 세터 메서드를 호출할 때 자동 주입할 빈이 존재하면 해당 빈을 인자로 전달하고, 존재하지 않으면 인자로 null을 전달한다. 
*@Autowired 어노테이션의 required 속성을 false로 할 때와 차이점은 @Nullable 어노테이션을 사용하면 자동 주입할 빈이 존재하지 않아도 메서드가 호출된다는 점이다.*  

<br>

#### 자동 주입과 명시적 의준 주입 간의 관계  

설정 클래스에서 의존을 주입했는데 자동 주입 대상이면 어떻게 될까?  

```Java
package config;

@Configuration
public class AppCtx {
@Bean
	@Qualifier("printer")
	public MemberPrinter memberPrinter1() {
		System.out.println("test");
		return new MemberPrinter();
	}
	
	@Bean
	public MemberInfoPrinter infoPrinter() {
		MemberInfoPrinter infoPrinter = new MemberInfoPrinter();
		infoPrinter.setPrinter(memberPrinter2()); //의존 명시 주입
		return infoPrinter;
	}
}
```

```Java
package spring;

public class MemberInfoPrinter {

	private MemberDao memDao;
	private MemberPrinter printer;

	@Autowired
	public void setMemberDao(MemberDao memberDao) {
		this.memDao = memberDao;
	}

	@Autowired
	@Qualifier("printer") //의존 자동 주입
	public void setPrinter(MemberPrinter printer) {
		this.printer = printer;
	}

}
```
1. infoPrinter() 메서드는 MemberInfoPrinter의 setPrinter()메서드를 호출해서 memberPrinter2 빈을 주입하고 있다.  
2. MemberInfoPrinter의 setPrinter() 메서드는 @Autowired 어노테이션, @Qualifier 어노테이션을 통해 memberPrinter1을 이용해 자동 주입하고 있다.  

실행 결과를 확인해보면 memberPrinter1 빈을 사용해서 회원정보를 출력한다.  
즉 설정 클래스에서 세터 메서드를 통해 의존을 주입해도 해당 세터 메서드에 @Autowired 어노테이션이 붙어 있으면 자동 주입을 통해 일치하는 빈을 주입한다. 
따라서 @Autowired 어노테이션을 사용했다면 설정 클래스에서 객체를 주입하기보다는 스프링이 제공하는 자동 주입 기능을 사용하는 편이 낫다.  

<br><br>

### 5. 컴포넌트 스캔  
자동 주입과 함께 사용하는 추가 기능이 컴포넌트 스캔이다. 컴포넌트 스캔은 스프링이 직접 클래스를 검색해서 빈으로 등록해주는 기능이다. 
설정 클래스에 빈으로 등록하지 않아도 원하는 클래스를 빈으로 등록할 수 있으므로 컴포넌트 스캔 기능을 사용하면 설정 코드가 크게 줄어든다.  

#### @Component 어노테이션으로 스캔 대상 지정  
스프링이 검색해서 빈으로 등록할 수 있으려면 클래스에 @Component 어노테이션을 붙여야 한다. 
@Component 어노테이션은 해당 클래스를 스캔 대상으로 표시한다. 

```Java
@Component
public class MemberRegisterService {
}

@Component("listPrinter")
public class MemberListPrinter {
}

@Component("infoPrinter")
public class MemberInfoPrinter {
}
```
@Component 어노테이션에 값을 주었는지에 따라 빈으로 등록할 때 사용할 이름이 결정된다. 
@Component 어노테이션에 값을 주지 않은 경우 클래스 이름의 첫 글자를 소문자로 바꾼 이름을 빈 이름으로 사용한다. 
MemberRegisterService 클래스의 경우 빈 이름으로 memberRegisterService를 사용한다.  

@Component 어노테이션에 값을 주면 그 값을 빈 이름으로 사용한다. MemberListPrinter 클래스의 경우 빈 이름으로 listPrinter를 사용한다. 

<br>

#### @Component 어노테이션으로 스캔 설정  
@Component 어노테이션을 붙인 클래스를 스캔해서 스프링 빈으로 등록하려면 설정 클래스에 @ComponentScan 어노테이션을 적용해야 한다. 
설정 클래스인 AppCtx에 @ComponentScan 어노테이션을 적용하면 아래와 같다.  
```Java
@ComponentScan(basePackages = {"spring"})
@Configuration
public class AppCtx {

}
```
@ComponentScan 어노테이션의 basePackages 속성값은 {"spring"}이다. 이 속성은 스캔 대상 패키지 목록을 지정한다. 
spring 패키지와 그 하위 패키지에 속한 클래스를 스캔 대상으로 설정한다. 스캔 대상에 해당하는 클래스 중에서 @Component 어노테이션이 붙은 클래스의 객체를 생성해서 빈으로 등록한다.  

<br>

#### 스캔 대상에서 제외하거나 포함하기  
excludeFilters 속성을 사용하면 스캔할 때 특정 대상을 자동 등록 대상에서 제외할 수 있다. 

```Java
@ComponentScan(basePackages = {"spring"}, excludeFilters = @Filter(type = FilterType.REGEX, pattern = "spring\\..*Dao"))
@Configuration
public class AppCtxWithExclude {
	@Bean
	public MemberDao memberDao(){
		return new MemberDao();
	}
}
```
위 코드는 @Filter 어노테이션의 type 속성값으로 FilterType.REGEX를 중ㅆ다. 이는 정규표현식을 사용해서 제외 대상을 지정한다는 의미이다. 
pattern속성은 FilterType에 적용할 값을 설정한다. 위 설정에서는 "spring."으로 시작하고 Dao로 끝나는 정규표현식을 지정했으므로 spring.MemberDao 클래스를 컴포넌트 스캔 대상에서 제외한다.  

<br>

```Java
@ComponentScan(basePackages = {"spring"}, excludeFilters = @Filter(type = FilterType.ASPECTJ, pattern = "spring.*Dao"))
@Configuration
public class AppCtxWithExclude {
	@Bean
	public MemberDao memberDao(){
		return new MemberDao();
	}
}
```
FilterType.ASPECTJ를 필터 타입으로 설정할 수도 있다. 이 타입을 사용하면 정규표현식 대신 AspectJ 패턴을 사용해서 대상을 지정한다. 
위 설정을 사용하면 spring 패키지에서 이름이 Dao로 끝나는 타입을 컴포넌트 스캔 대상에서 제외한다.  

<br>

특정 어노테이션을 붙인 타입을 컴포넌트 대상에서 제외할 수도 있다.  
예를 들어 다음의 @NoProduct나 @ManualBean 어노테이션을 붙인 클래스는 컴포넌트 스캔 대상에서 제외하고 싶다고 하자. 
```Java
@Retention(RUNTIME)
@Target(TYPE)
public @interface NoProduct{
}

@Retention(RUNTIME)
@Target(TYPE)
public @interface ManualBean{
}
```

이 두 어노테이션을 붙인 클래스를 컴포넌트 스캔 대상에서 제외하려면 다음과 같이 excludeFilters 속성을 설정한다. 
```Java
@Configuration
@ComponentScan(basePackages = {"spring", "spring2"}, excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = {NoProduct.class, ManualBean.class}))
public class AppCtxWithExclude {
	@Bean
	public MemberDao memberDao(){
		return new MemberDao();
	}
}

@ManualBean
@Component
public class MemberDao{
}
```
<br>

특정 타입이나 그 하위 타입을 컴포넌트 스캔 대상에서 제외하려면 ASSIGNABLE_TYPE을 FilterType으로 사용한다.  
```Java
@Configuration
@ComponentScan(basePackages = {"spring"}, excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MemberDao.class))
public class AppCtxWithExclude {
}

```
classes 속성에는 제외할 타입 모록을 지정한다. 

<br>

설정할 필터가 두 개 이상이면 excludeFilters 속성에 배열을 사용해서 @Filter 목록을 전달하면 된다. 
```Java
@Configuration
@ComponentScan(basePackages = {"spring"},
  excludeFilters = {
	  @Filter(type = FilterType.ANNOTATION, classes = ManualBean.class),
	  @Filter(type = FilterType.REGEX, pattern = "spring2\\..*")
})
public class AppCtxWithExclude {
}
```

#### 기본 스캔 대상  
@Component 어노테이션을 붙인 클래스만 컴포넌트 스캔 대상에 포함되는 것은 아니다. 다음 어노테이션을 붙인 클래스가 컴포넌트 스캔 대상에 포함된다.  
- @Component(org.springframework.stereotype 패키지)
- @Controller(org.springframework.stereotype 패키지)
- @Service(org.springframework.stereotype 패키지)
- @Repository(org.springframework.stereotype 패키지)
- @Aspect(org.aspectj.lang.annotation 패키지)
- @Configuration(org.springframework.context.annotation 패키지)

@Aspect 어노테이션을 제외한 나머지 어노테이션은 실제로는 @Component 어노테이션에 대한 특수 어노테이션이다.  

`@Controller`  
```Java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Controller {
	@AliasFor(annotation = Component.class)
	String value() default "";
}
```
@Component 어노테이션이 붙어 있는데, 스프링은 @Controller 어노테이션을 @Component 어노테이션과 동일하게 컴포넌트 스캔 대상에 포함한다. 
@Controller 어노테이션이나 @Repository 어노테이션 등은 컴포넌트 스캔 대상이 될 뿐만 아니라 스프링 프레임워크에서 특별한 기능과 연관되어 있다. 
@Controller 어노테이션은 웹 MVC와 관련 있고 @Repository 어노테이션은 DB연동과 관련 있다.  

<br>

#### 컴포넌트 스캔에 따른 충돌 처리  

spring 패키지와 spring2 패키지에 MemberRegisterService 클래스가 존재하고 두 클래스 모두 @Component 어노테이션을 붙였다고 하자. 
이 상태에서 다음 @ComponentScan 어노테이션을 사용하면 어떻게 될까?  
```Java
@Configuratino
@ComponentScan(basePackages = {"spring","spring2"})
public class AppCtx{
}
```

위 설정을 이용해서 스프링 컨테이너를 생성하면 Exception이 발생한다.  
```log
Caused by: org.springframework.context.annotation.ConflictingBeanDefinitionException: 
Annotation-specified bean name 'memberRegisterService' for bean class [spring2.MemberRegisterService] conflicts with existing, 
non-compatible bean definition of same name and class [spring.MemberRegisterService]
```
<br>


MmemberDao 클래스는 컴포넌트 스캔 대상이다. 자동 등록된 빈의 이름은 memberDao다. 그런데 다음과 같이 설정 클래스에 직접 MemberDao 클래스를 "memberDao"라는 이름의 빈으로 등록하면 어떻게 될 까?
```Java
@Component
public class MemberDao{
}
```

```Java
@ComponentScan(basePackages = {"spring")
@Configuration
public class AppCtx {
	
	@Bean
	public MemberDao memberDao() {
		MemberDao memberDao = new MemberDao();
		return memberDao;
	}
}
```
스캔할 때 사용하는 빈 이름과 수동 등록한 빈 이름이 같은 경우 수동 등록한 빈이 우선한다. 즉 MemberDao 타입 빈은 AppCtx에서 정의한 한 개만 존재한다.  




<br><br><br>

---

- 출처  
초보 웹 개발자를 위한 스프링 5 (최범균)

---
