---
layout: post
title: "Spring5 프로그래밍 (Chapter 7)[AOP 프로그래밍]"
author: "Bys"
category: it_book
date: 2022-03-16 01:00:00
tags: book programming spring framework aop aspect
---

### 7. AOP 프로그래밍  
트랜잭션의 처리 방식을 이해하려면 AOP(Aspect Oriented Programming)를 알야아 한다.  

스프링 프레임워크의 AOP 기능은 spring-aop 모듈이 제공하는데 spring-context 모듈을 의존 대상에 추가하면 spring-aop 모듈도 함께 의존 대상에 포함된다. 
따라서 spring-aop 모듈에 대한 의존을 따로 추가하지 않아도 된다. aspectjweaver 모듈은 AOP를 설정하는데 필요한 어노테이션을 제공하므로 이 의존을 추가해야 한다.  

#### 프록시와 AOP  
`Calculator`  
```Java
package chap07;

public interface Calculator {

	public long factorial(long num);

}
```

`ImpeCalculator`  
```Java
package chap07;

public class ImpeCalculator implements Calculator {

	@Override
	public long factorial(long num) {
		long result = 1;
		for (long i = 1; i <= num; i++) {
			result *= i;
		}
		return result;
	}

}
```

`RecCalculator`  
```Java
package chap07;

public class RecCalculator implements Calculator {

	@Override
	public long factorial(long num) {
        if (num == 0)
            return 1;
        else
            return num * factorial(num - 1);
	}

}
```
<br>

위의 계승 구현 클래스의 실행 시간을 출력하려면 어떻게 해아 할까? 쉬운 방법은 메서드의 시작과 끝에서 시간을 구하고 이 두 시간의 차이를 출력하는 것이다.  

```Java
package chap07;

public class ImpeCalculator implements Calculator {

	@Override
	public long factorial(long num) {
		
		long start = System.currentTimeMillis(); // Start Time
		long result = 1;
		for (long i = 1; i <= num; i++) {
			result *= i;
		}
		long end = System.currentTimeMillis(); // End Time
		System.out.printf("ImpeCalculator.factorial(%d) 실행 시간 = %d\n", num, (end-start)); // End - Start
		
		return result;
	}
}
```
ImpeCalculator 클래스는 위와 같이 수정 하면 된다.  
RecCalculator 클래스는 약간 복잡해진다. factorial() 메서드는 재귀 호출로 구현해서 factorial() 메서드의 시작과 끝에 시간을 구해서 차이를 출력하는 코드를 넣으면 메세지가 여러 번 출력되는 문제가 있다. 

<br>

`Sample`
```Java
ImpeCalculator impeCal = new ImpeCalculator();
long start1 = System.currentTimeMills();
long fourFactorial1 = impeCal.factorial(4);
long end1 = System.currentTimeMills();
System.out.printf("ImpeCalculator.factorial(4) 실행 시간 = %d\n", (end1-start1));

RecCalculator recCal = new RecCalculator();
long start2 = System.currentTimeMills();
long fourFactorial2 = recCal.factorial(4);
long end2 = System.currentTimeMills();
System.out.printf("ImpeCalculator.factorial(4) 실행 시간 = %d\n", (end2-start2));
```
RecCalculator를 고려하면 실행 시간을 출력하기 위해 기존 코드를 변경하는 것보다는 메서드 실행 전후에 값을 구하는게 나을지도 모른다.  

그런데 위 의 방식도 문제가 있다. 실행 시간을 밀리초 단위가 아니라 나노초 단위로 구해야 한다면 어떻게 될까?
위 코드에서 굵게 표시한 시간을 구하고 출력하는 코드가 중복되어 있어 두 곳을 모두 변경해야 한다.  

기존 코드를 수정하지 않고 코드 중복도 피할 수 있는 방법은 없을까? 이때 출현하는 것이 바로 *프록시 객체* 이다. 

<br>

```Java
package chap07;

public class ExeTimeCalculator implements Calculator {

	private Calculator delegate;

	public ExeTimeCalculator(Calculator delegate) {
        this.delegate = delegate;
    }

	@Override
	public long factorial(long num) {
		long start = System.nanoTime();
		long result = delegate.factorial(num);
		long end = System.nanoTime();
		System.out.printf("%s.factorial(%d) 실행 시간 = %d\n",
				delegate.getClass().getSimpleName(),
				num, (end - start));
		return result;
	}

}
```
ExeTimeCalculator 클래스는 Calculator 인터페이스를 구현하고 있다. 
이 클래스는 생성자를 통해 다른 Calculator 객체를 전달받아 delegate 필드에 할당하고 factorial()메서드에서 delegate.factorial() 메서드를 실행한다. 
그리고 delegate.factorial()의 코드를 실행하기 전후에 현재 시간을 구해 차이를 출력한다. 

ExeTimeCalculator 클래스를 사용하면 다음과 같은 방법으로 ImpeCalculator의 실행 시간을 측정할 수 있다. 
```Java
ImpeCalculator impeCal = new ImpeCalculator();
ExeTimeCalculator calculator = new ExeTimeCalculator(impeCal);
long result = calculator.factorial(4);
```

위 코드에서 calulator.factorial()을 실행하면 그림과 같은 순서로 코드가 실행된다.  

![spring5_7_1](/assets/book/spring5/spring5_7_1.png){: width="60%" height="auto"}  


위 실행 흐름을 보면 ExeTimeCalculator 클래스의 factorial() 메서드는 결과적으로 ImpeCalculator의 factorial() 메서드의 실행 시간을 구해서 콘솔에 출력하게 된다.  

```Java
package main;

public class MainProxy {

	public static void main(String[] args) {
		ExeTimeCalculator ttCal1 = new ExeTimeCalculator(new ImpeCalculator());
		System.out.println(ttCal1.factorial(20));

		ExeTimeCalculator ttCal2 = new ExeTimeCalculator(new RecCalculator());
		System.out.println(ttCal2.factorial(20));
	}
}
```

`Output`
```log
ImpeCalculator.factorial(20) 실행 시간 = 4196
2432902008176640000
RecCalculator.factorial(20) 실행 시간 = 3779
2432902008176640000
```
위 결과에서 다음을 알 수 있다. 
- 기존 코드를 변경하지 않고 시간을 출력할 수 있다. ImpeCalculator 클래스나 RecCalculator 클래스의 코드 변경 없이 이 두 클래스의 factorial() 메서드 실행 시간을 출력할 수 있게 되었다. 
- 실행 시간을 구하는 코드의 중복을 제거했다. 나노초 대신에 밀리초를 사용해서 실행 시간을 구하고 싶다면 ExeTimeCalculator 클래스만 변경하면 된다.  

<br>

이것이 가능한 이유는 ExeTimeCalculator 클래스를 다음과 같이 구현했기 때문이다.  
- factorial() 기능 자체를 직접 구현하기보다는 다른 객체에 factorial()의 실행을 위임한다. (delegate.factorial(num))
- 계산 기능 외에 다른 부가적인 기능을 실행한다. 여기서 부가적인 기능은 실행 시간 측정이다.  

이렇게 핵심 기능의 실행은 다른 객체에 위임하고 부가적인 기능을 제공하는 객체를 프록시(proxy)라고 부른다. 
실제 핵심 기능을 실행하는 객체는 대상 객체라고 부른다. ExeTimeCalculator가 프록시이고 ImpeCalculator 객체가 프록시의 대상 객체가 된다. 
프록시의 특징은 핵심 기능은 구현하지 않는다는 점이다. ImpeCalculator RecCalculator는 팩토리얼 연산이라는 핵심 기능을 구현하고 있다. 
반면에 ExeTimeCalculator 클래스는 팩토리얼 연산 자체를 구현하고 있지 않다.  

프록시는 핵심 기능을 구현하지 않는 대신 여러 객체에 공통으로 적용할 수 있는 기능을 구현한다. 
이 예에서 ExeTimeCalculator 클래스는 ImpeCalculator 객체와 RecCalculator 객체에 공통으로 적용되는 실행 시간 측정 기능을 구현하고 있다.  

`정리`  
ImpeCalculator와 RecCalculator는 팩토리얼을 구한다는 핵심 기능 구현에 집중하고 프록시인 ExeTimeCalculator는 실행 시간 측정이라는 공통 기능 구현에 집중한다.  
**이렇게 공통 기능 구현과 핵심 기능 구현을 분리하는 것이 AOP의 핵심이다.**

<br>

**AOP**  
AOP는 Aspect Oriented Programming의 약자로, 여러 객체에 공통으로 적용할 수 있는 기능을 분리해서 재사용성을 높여주는 프로그래밍 기법이다. 
AOP는 핵심 기능과 공통 기능의 구현을 분리함으로써 핵심 기능을 구현한 코드의 수정 없이 공통 기능을 적용할 수 있게 만들어 준다.  

스프링도 프록시를 이용해서 AOP를 구현하고 있다. AOP의 기본 개념은 핵심 기능에 공통 기능을 삽입하는 것이다. 
즉 핵심 기능의 코드를 수정하지 않으면서 공통 기능의 구현을 추가하는 것이 AOP이다. 핵심 기능에 공통 기능을 삽입하는 방법에는 다음 세가지가 있다.  
1. 컴파일 시점에 코드에 공통 기능을 삽입하는 방법  
2. 클래스 로딩 시점에 바이트 코드에 공통 기능을 삽입하는 방법
3. 런타임에 프록시 객체를 생성해서 공통 기능을 삽입하는 방법

1번과 2번 두 가지는 스프링 AOP에서는 지원하지 않으며 AspectJ와 같이 AOP 전용 도구를 사용해서 적용할 수 있다.  
스프링이 제공하는 AOP 방식은 프록시를 이용한 세 번째 방식이다. 

![spring5_7_2](/assets/book/spring5/spring5_7_2.png){: width="60%" height="auto"}  

프록시 방식은 앞서 살펴본 것처럼 중간에 프록시 객체를 생성한다. 그리고 그림처럼 실제 객체의 기능을 실행하기 전-후에 공통 기능을 호출한다. 

스프링 AOP는 프록시 객체를 자동으로 만들어준다. 따라서 ExeTimeCalculator 클래스 처럼 상위 타입의 인터페이스를 상속받은 프록시 클래스를 직접 구현할 필요가 없다. 
단지 공통 기능을 구현한 클래스만 알맞게 구현하면 된다.  

AOP에서 공통 기능을 Aspect라고 하는데 Aspect 외에 알아두여야 할 용어이다.  

`AOP 주요 용어`  

| 용어        |  의미 |
| :---       | :--- |
| Advice     | 언제 공통 관심 기능을 핵심 로직에 적용할 지를 정의하고 있다. 예를 들어 '메서드를 호출하기 전'(언제)에 '트랜잭션 시작'(공통 기능) 기능을 적용한다는 것을 정의한다. |
| JointPoint | Advice를 적용 가능한 지점을 의미한다. 메서드 호출, 필드 값 변경 등이 Jointpoint에 해당한다. 스프링은 프록시를 이용해서 AOP를 구현하기 때문에 메서드 호출에 대한 Jointpoint만 지원한다. |
| PointCut   | Jointpoint의 부분 집합으로서 실제 Advice가 적용되는 Jointpoint를 나타낸다. 스프링에서는 정규 표현식이나 AspectJ의 문법을 이요하여 Pointcut을 정의할 수 있다. |
| Weaving    | Advice를 핵심 로직 코드에 적용하는 것을 weaving이라고 한다. |
| Aspect     | 여러 객체에 공통으로 적용되는 기능을 Aspect라고 한다. 트랜잭션이나 보안 등이 Aspect의 좋은 예이다. |

<br>

**Advice의 종류**  
스프링은 프록시를 이용해서 메서드 호출 시점에 Aspect를 적용하기 때문에 구현 가능한 Advice의 종류는 아래와 같다. 

`스프링에서 구현 가능한 Advice 종류`  

| 종류                    |  설명 |
| :---                   | :--- |
| Before Advice          | 대상 객체의 메서드 호출 전에 공통 기능을 실행한다. |
| After Returning Advice | 대상 객체의 메서드가 익셉션 없이 실행된 이후에 공통 기능을 실행한다. |
| After Throwing Advice  | 대상 객체의 메서드를 실행하는 도중 exception이 발생한 경우에 공통 기능을 실행한다. |
| After Advice           | Exception 발생 여부에 상관없이 대상 객체의 메서드 실행 후 공통 기능을 실행한다. (try-catch-finally의 finally 블록과 비슷하다) |
| Around Advice          | 대상 객체의 메서드 실행 전, 후 또는 exception 발생 시점에 공통 기능을 실행하는데 사용된다. |

이 중에서 널리 사용되는 것은 Around Advice이다. 이유는 대상 객체의 메서드를 실행 하기 전/후, exception 발생 시점 등 다양한 시점에 원하는 기능을 삽입할 수 있기 때문이. 
캐시 기능, 성능 모니터링 기능과 같은 Aspect를 구현할 때에는 Around Advice를 주로 이용한다. 이 책에서도 Around Advice의 구현 방법에 대해서만 살펴볼 것이다.  

<br>

#### 스프링 AOP 구현  
스프링 AOP를 이용해서 공통 기능을 구현하고 적용하는 방법은 단순하다. 다음과 같은 절차만 따르면 된다. 
- Aspect로 사용할 클래스에 @Aspect 어노테이션을 붙인다.  
- @Pointcut 어노테이션으로 공통 기능을 적용할 Pointcut을 정의한다.  
- 공통 기능을 구현한 메서드에 @Around 어노테이션을 적용한다. 

<br>

개발자는 공통 기능을 제공하는 Aspect 구현 클래스를 만들고 자바 설정을 이용해서 Aspect를 어디에 적용할지 설정하면 된다. 
Aspect는 @Aspect 어노테이션을 이용해서 구현한다. 프록시는 스프링 프레임워크가 알아서 만들어준다. 

```Java
package aspect;

@Aspect
public class ExeTimeAspect {

	@Pointcut("execution(public * chap07..*(..))")
	private void publicTarget() {
	}

	@Around("publicTarget()")
	public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
		long start = System.nanoTime();
		try {
			Object result = joinPoint.proceed();
			return result;
		} finally {
			long finish = System.nanoTime();
			Signature sig = joinPoint.getSignature();
			System.out.printf("%s.%s(%s) 실행 시간 : %d ns\n",
					joinPoint.getTarget().getClass().getSimpleName(),
					sig.getName(), Arrays.toString(joinPoint.getArgs()),
					(finish - start));
		}
	}

}
```
각 어노테이션과 메서드에 대해 알아보자.  
먼저 @Aspect 어노테이션을 적용한 클래스는 Advice와 Pointcut을 함께 제공한다.  

@Pointcut은 공통 기능을 적용할 대상을 설정한다. 
@Pointcut 어노테이션의 값으로 사용할 수 있는 execution 명시자에 대해서는 다음에 살펴볼 것이다. 
일단 지금은 chap07 패키지와 그 하위 패키지에 위치한 타입의 public 메서드를 Pointcut으로 설정한다는 정도만 이해하고 넘어가자.  

@Around 어노테이션은 Around Advice를 설정한다. 
@Around 어노테이션의 값이 "publicTarget()"인데 이는 publicTarget() 메서드에 정의한 Pointcut에 공통 기능을 적용한다는 것을 의미한다. 
publicTarget() 메서드는 chap07 패키지와 그 하위 패키지에 위치한 public 메서드를 Pointcut으로 설정하고 있으므로, 
chap07 패키지나 그 하위 패키지에 속한 빈 객체의 public 메서드에 @Around가 붙은 measure() 메서드를 적용한다.  

measure() 메서드의 ProceedingJoinPoint 타입 파라미터는 프록시 대상 객체의 메서드를 호출할 때 사용한다. 
위의 코드에서 proceed() 메서드를 사용해서 실제 대상 객체의 메서드를 호출한다. 
이 메서드를 호출하면 대상 객체의 메서드가 실행되므로 이 코드 이전과 이후에 공통 기능을 위한 코드를 위치시키면 된다. 위 의 코드에서는 실행하기 전과 후에 현재 시간을 구한 뒤 실행 시간을 출력하고 있다.  

위의 코드를 보면 ProceedingJointPoint의 getSignature(), getTarget(), getArgs() 등의 메서드를 사용하고 있다. 
각 메서드는 호출한 메서드의 시그니처, 대상, 객체, 인자 목록을 구하는데 사용된다. 이 메서드를 사용해서 대상 객체의 클래스 이름과 메서드 이름을 출력한다. 
각 메서드에 대한 내용은 뒤에서 다시 살펴보도록 하자.  

> 자바에서 메서드 이름과 파라미터를 합쳐서 메서드 시그니처라고 한다. 메서드 이름이 다르거나 파라미터 타입, 개수가 다르면 시그니처가 다르다고 표현한다. 

<br>

설정 클래스는 다음과 같다.  
`AppCtx`  
```Java
package config;

@Configuration
@EnableAspectJAutoProxy
public class AppCtx {
	@Bean
	public ExeTimeAspect exeTimeAspect() {
		return new ExeTimeAspect();
	}

	@Bean
	public Calculator calculator() {
		return new RecCalculator();
	}

}
```
@Aspect 어노테이션을 붙인 클래스를 공통 기능으로 적용하려면 @EnableAspectJAutoProxy 어노테이션을 설정 클래스에 붙여야 한다. 
이 어노테이션을 추가하면 스프링은 @Aspect 어노테이션이 붙은 빈 객체를 찾아서 빈 객체를 찾아서 빈 객체의 @Pointcut 설정과 @Around 설정을 사용한다. 


```Java
package aspect;

@Aspect
public class ExeTimeAspect {

	@Pointcut("execution(public * chap07..*(..))")
	private void publicTarget() {
	}

	@Around("publicTarget()")
	public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
	}
}
```
@Around 어노테이션은 Pointcut으로 publicTarget() 메서드를 설정했다. 
publicTarget() 메서드의 @Pointcut은 chap07 패키지나 그 하위 패키지에 속한 빈 객체의 public 메서드를 설정한다. 
Calculator 타입이 chap07 패키지에 속하므로 calculator 빈에 ExeTimeAspect 클래스에 정의한 공통 기능인 measure()를 적용한다.  

`MainAspect`  
```Java
package main;

public class MainAspect {
	
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = 
				new AnnotationConfigApplicationContext(AppCtx.class);

		Calculator cal = ctx.getBean("calculator", Calculator.class);
		long fiveFact = cal.factorial(5);
		System.out.println("cal.factorial(5) = " + fiveFact);
		System.out.println(cal.getClass().getName());
		ctx.close();
	}
}
```















<br><br><br>

---

- 출처  
초보 웹 개발자를 위한 스프링 5 (최범균)

---
