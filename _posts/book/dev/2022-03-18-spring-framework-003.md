---
layout: post
title: "Spring 프로그래밍 (Chapter 7)[AOP 프로그래밍]"
author: "Bys"
category: dev
date: 2022-03-18 01:00:00
tags: book programming spring framework aop aspect around pointcut
---

## 7. AOP 프로그래밍  
트랜잭션의 처리 방식을 이해하려면 AOP(Aspect Oriented Programming)를 알야아 한다.  

스프링 프레임워크의 AOP 기능은 spring-aop 모듈이 제공하는데 spring-context 모듈을 의존 대상에 추가하면 spring-aop 모듈도 함께 의존 대상에 포함된다. 
따라서 spring-aop 모듈에 대한 의존을 따로 추가하지 않아도 된다. aspectjweaver 모듈은 AOP를 설정하는데 필요한 어노테이션을 제공하므로 이 의존을 추가해야 한다.  

### 7.2 프록시와 AOP  
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

#### 7.2.1 AOP
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

#### 7.2.2 Advice의 종류 
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

### 7.3 스프링 AOP 구현  
스프링 AOP를 이용해서 공통 기능을 구현하고 적용하는 방법은 단순하다. 다음과 같은 절차만 따르면 된다. 
- Aspect로 사용할 클래스에 @Aspect 어노테이션을 붙인다.  
- @Pointcut 어노테이션으로 공통 기능을 적용할 Pointcut을 정의한다.  
- 공통 기능을 구현한 메서드에 @Around 어노테이션을 적용한다. 

<br>

#### 7.3.1 @Aspect, @Pointcut, @Around를 이용한 AOP 구현  
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

위의 코드를 보면 ProceedingJoinPoint의 getSignature(), getTarget(), getArgs() 등의 메서드를 사용하고 있다. 
각 메서드는 호출한 메서드의 시그니처, 대상 객체, 인자 목록을 구하는데 사용된다. 이 메서드를 사용해서 대상 객체의 클래스 이름과 메서드 이름을 출력한다. 
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
`Output`
```log
RecCalculator.factorial([5]) 실행 시간 : 27225 ns
cal.factorial(5) = 120
com.sun.proxy.$Proxy17
```
위 와같은 문구가 출력되었다. 첫 번째 줄은 ExeTimeAspect 클래스의 measure() 메서드가 출력한 것이다. 
세 번째 줄은 cal.getClass().getName()에서 출력한 코드다. 이 출력 결과를 보면 Calculator 타입이 RecCalculator 클래스가 아니고 $Proxy17이다. 
이 타입은 스프링이 생성한 프록시 타입이다. 실제 Main에서 cal.factorial(5); 코드를 호출할 때 실행되는 과정은 아래 그림과 같다.  

![spring5_7_3](/assets/book/spring5/spring5_7_3.png){: width="60%" height="auto"}  

AOP를 적용하지 않았으면 ctx.getBean("calculator", Calculator.class) 에서 리턴한 객체는 프록시 객체가 아닌 RecCalculator 타입이었을 것이다. 

<br>

#### 7.3.2 ProceedingJoinPoint의 메서드  
Around Advice에서 사용할 공통 기능 메서드는 대부분 파라미터로 전달받은 ProceedingJoinPoint의 proceed() 메서드만 호출하면 된다. 
ExeTimeAspect클래스도 joinPoint.proceed() 메서드를 호출했다.  

호출 되는 대상 객체에 대한 정보, 실행되는 메서드에 대한 정보, 메서드를 호출할 때 전달된 이자에 대한 정보가 필요할 때가 있다. 
이들 정보에 접근할 수 있또록 ProceedingJoinPoint 인터페이스는 다음 메서드를 제공한다.  
- Signature getSignature(): 호출되는 메서드에 대한 정보를 구한다. 
- Object getTarget(): 대상 객체를 구한다. 
- Object[] getArgs(): 파라미터 목록을 구한다. 

org.aspectj.lang.Signature 인터페이스는 다음 메서드를 제공한다. 각 메서드는 호출되는 메서드의 정보를 제공한다. 
- String getName(): 호출되는 메서드의 이름을 구한다. 
- String toLongString(): 호출되는 메서드를 완전하게 표현한 문장을 구한다(메서드의 리턴 타입, 파라미터 타입이 모두 표시된다).
- String toShortString(): 호출되는 메서드를 축약해서 표현한 문장을 구한다(기본 구현은 메서드의 이름만 구한다).

<br>

### 7.4 프록시 생성 방식  

MainAspect 클래스 코드를 다음과 같이 변경해보자.  

`MainAspect`  
```Java
package main;

public class MainAspect {
	
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = 
				new AnnotationConfigApplicationContext(AppCtx.class);

		//Calculator cal = ctx.getBean("calculator", Calculator.class);
		Calculator cal = ctx.getBean("calculator", RecCalculator.class);

	}
}

@Configuration
@EnableAspectJAutoProxy
public class AppCtx {

	@Bean
	public Calculator calculator() {
		return new RecCalculator();
	}

}
```
getBean() 메서드에 Calculator 타입 대신에 RecCalculator 타입을 사용하도록 수정했다. 
자바 설정 파일에도 "calculator"빈을 생성할 때 사용한 타입이 RecCalculator 클래스이므로 문제가 없어 보인다. 
하지만 정상 실행될 것이라는 예상과 달리 다음과 같은 exception이 발생한다.  

```log
Exception in thread "main" org.springframework.beans.factory.BeanNotOfRequiredTypeException: 
Bean named 'calculator' is expected to be of type 'chap07.RecCalculator' but was actually of type 'com.sun.proxy.$Proxy17'
```
exception 메세지를 보면 getBean() 메서드에 사용한 타입이 RecCalculator인데 반해 실제 타입은 $Proxy17이라는 메세지가 나온다. 
$Proxy17은 스프링이 런타임에 생성한 프록시 객체의 클래스 이름이다. 이 $Proxy 클래스는 RecCalculator 클래스가 상속받은 Calculator 인터페이스를 상속받게 된다. 
아래 그림과 같은 게층 구조를 갖는다.  

![spring5_7_4](/assets/book/spring5/spring5_7_4.png){: width="45%" height="auto"}  

스프링은 AOP를 위한 프록시 객체를 생성할 때 실제 생성할 빈 객체가 인터페이스를 상속하면 인터페이스를 이용해서 프록시를 생성한다. 
앞서 에에서도 RecCalculator 클래스가 Calculator 인터페이스를 상속하므로 Calculator 인터페이스를 상속받은 프록시 객체를 생성했다. 
따라서 위의 코드처럼 빈의 실제 타입이 RecCalculator라고 하더라도 "calculator" 이름에 해당하는 빈 객체의 타입은 위 그림처럼 Calculator 인터페이스를 상속받은 프록시 타입이 된다.  

빈 객체가 인터페이스를 상속할 때 인터페이스가 아닌 클래스를 이용해서 프록시를 생성하고 싶다면 다음과 같이 설정하면 된다.  
```Java
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AppCtx{
}
```
@EnableAspectJAutoProxy 어노테이션의 proxyTargetClass 속성을 true로 지정하면 인터페이스가 아닌 자바 클래스를 상속받아 프록시를 생성한다. 

<br>

#### 7.4.1 execution 명시자 표현식  
Aspect를 적용할 위치를 지정할 대 사용한 Pointcut설정을 보면 execution 명시자를 사용했다.  

```Java
@Pointcut("execution(public * chap07..*(..))")
private void publicTarget(){
}
```
execution 명시자는 Advice를 적용할 메서드를 지정할 때 사용한다. 기본 형식은 다음과 같다.  

```Java
execution(수식어패턴? 리턴타입패턴 클래스이름패턴?메서드이름패턴(파라미터패턴))
```
'수식어패턴'은 생략 가능하며 public, protected 등이 온다. 스프링 AOP는 public 메서드에만 적용할 수 있기 때문에 사실상 public만 의미있다. 
'리턴타입패턴'은 리턴 타입을 명시한다. '클래스이름패턴'과 '메서드이름패턴'은 클래스 이름 및 메서드 이름을 패턴으로 명시한다. 
'파라미터패턴'은 매칭될 파라미터에 대해서 명시한다.  

각 패턴은 '*'을 이용하여 모든 값을 표현할 수 있다. 또한 '..'(점 두개)을 이용하여 0개 이상이라는 의미를 표현할 수 있다.  

`execution 명시자 예시`  

| 예                                                 | 설명 |
| :---                                              | :--- |
| execution(public void set*(..))                   | 리턴 타입이 void이고, 메서드 이름이 set으로시작하고, 파라미터가 0개 이상인 메서드 호출. 파라미터 부분에 '..'을 사용하여 파라미터가 0개 이상인 것을 표현했다. |
| execution(* chap07.*.*())                         | chap07 패키지의 타입에 속한 파라미터가 없는 모든 메서드 호출 |
| execution(* chap07..*.*(..))                      | chap07 패키지 및 하위 패키지에 있는, 파라미터가 0개 이상인 메서드 호출. 패키지 부분에 '..'을 사용하여 해당 패키지 또는 하위 패키지를 표현했다. |
| execution(Long chap07.Calculator.factorial(..))   | 리턴 타입이 Long인 Calculator 타입의 factorial() 메서드 호출 |
| execution(* get*(*))                              | 이름이 get으로 시작하고 파라미터가 한 개인 메서드 호출 |
| execution(* get*(*, *))                           | 이름이 get으로 시작하고 파라미터가 두 개인 메서드 호출 |
| execution(* read*(Integer, ..))                   | 메서드 이름이 read로 시작하고, 첫 번째 파라미터 타입이 Integer이며, 한 개 이상의 파라미터를 갖는 메서드 호출 |

<br>

#### 7.4.2 Advice 적용 순서  
한 Pointcut에 여러 Advice를 적용할 수도 있다. 

`CacheAspect`  
```Java
package aspect;

@Aspect
public class CacheAspect {

	private Map<Long, Object> cache = new HashMap<>();

	@Pointcut("execution(public * chap07..*(long))")
	public void cacheTarget() {
	}
	
	@Around("cacheTarget()")
	public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
		Long num = (Long) joinPoint.getArgs()[0];
		if (cache.containsKey(num)) {
			System.out.printf("CacheAspect: Cache에서 구함[%d]\n", num);
			return cache.get(num);
		}

		Object result = joinPoint.proceed();
		cache.put(num, result);
		System.out.printf("CacheAspect: Cache에 추가[%d]\n", num);
		return result;
	}
}
```
CacheAspect 클래스는 간단하게 캐시를 구현한 공통 기능이다. 동작 순서는 아래와 같다.  
1. 첫 번째 인자를 Long 타입으로 구한다. ((Long) joinPoint.getArgs()[0];)
2. 위 에서 구한 키 값이 cache에 존재하면 키에 해당하는 값을 구해서 리턴한다. (return cache.get(num);)
3. 위 에서 구한 키 값이 cache에 존재하지 않으면 프록시 대상 객체를 실행한다. (joinPoint.proceed();)
4. 프록시 대상 객체를 실행한 결과를 cache에 추가한다. (cache.put(num, result);)
5. 프록시 대상 객체의 실행 결과를 리턴한다. 

@Around 값으로 cacheTarget() 메서드를 지정했다. @Pointcut 설정은 첫 번째 인자가 long인 메서드를 대상으로 한다. 
따라서 execute() 메서드는 앞서 작성한 Calculator의 factorial(long) 메서드에 적용된다. 

새로운 Aspect를 구현했으므로 스프링 설정 클래스에는 아래와 같이 두 개의 Aspect를 추가할 수 있다. 
ExeTimeAspect는 앞서 구현한 시간 측정 Aspect이다. 두 Aspect에서 설정한 Pointcut은 모두 Calculator 타입의 factorial() 메서드에 적용된다.  

`AppCtxWithCacheAspect`
```Java
package config;

@Configuration
@EnableAspectJAutoProxy
public class AppCtxWithCache {

	@Bean
	public CacheAspect cacheAspect() {
		return new CacheAspect();
	}

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

`MainAspectWithCache`
```Java
package main;

public class MainAspectWithCache {
	
	public static void main(String[] args) {
		AnnotationConfigApplicationContext ctx = 
				new AnnotationConfigApplicationContext(AppCtxWithCache.class);

		Calculator cal = ctx.getBean("calculator", Calculator.class);
		cal.factorial(7);
		cal.factorial(7);
		cal.factorial(5);
		cal.factorial(5);
		ctx.close();
	}

}
```
`Output`
```log
RecCalculator.factorial([7]) 실행 시간 : 16710 ns
CacheAspect: Cache에 추가[7]
CacheAspect: Cache에서 구함[7]
RecCalculator.factorial([5]) 실행 시간 : 3969 ns
CacheAspect: Cache에 추가[5]
CacheAspect: Cache에서 구함[5]
```
결과를 보면 첫 번째 factorial(7)을 실행할 때와 두 번째 factorial(7)을 실행할 때 콘솔에 출력되는 내용이 다르다. 
첫 번째 실행 결과는 ExeTimeAspect와 CacheAspect가 모두 적용되었고 두 번째 실행 결과는 CacheAspect만 적용되었다. 
이렇게 첫 번째와 두 번째 실행 결과가 다른 이유는 Advice를 다음 순서로 적용했기 때문이다.

- CacheAspect 프록시 -> ExeTimeAspect프록시 -> 실제 대상 객체  

메인에서 ctx.getBean("calculator", Calculator.class); 로 구한 calculator 빈은 실제로는 CacheAspect 프록시 객체이다. 
근데 CacheAspect 프록시 객체의 대상 객체는 ExeTimeAspect의 프록시 객체이다. 
그리고 ExeTimeAspect 프록시의 대상 객체가 실제 대상 겍체이다.  

![spring5_7_7](/assets/book/spring5/spring5_7_7.png){: width="55%" height="auto"}  

실제 실행 순서는 그림과 같다.  
factorial(7)을 두 번째 호출하면 cache맵에 담긴 값을 리턴하고 끝난다. 이 경우 joinPoint.proceed()를 실행하지 않으므로 ExeTimeAspect나 실제 객체가 실행되지 않는다.  

어떤 Aspect가 먼저 적용될지는 스프링 프레임워크나 자바 버전에 따라 달라질 수 있기 때문에 적용 순서가 중요하다면 직접 순서를 지정해야 한다. 
이럴 때 사용하는 것이 @Order 어노테이션이다. @Aspect 어노테이션과 함께 @Order 어노테이션을 클래스에 붙이면 @Order 어노테이션에 지정한 값에 따라 적용 순서를 결정한다. 
@Order 어노테이션의 값이 작으면 먼저 적용하고 크면 나중에 적용한다.  

다음과 같이 두 Aspect 클래스에 @Order 어노테이션을 적용했다고 하자. 
```Java
@Aspect
@Order(1)
public class ExeTimeAspect {
}

@Aspect
@Order(2)
public class CacheAspect {
}
```

- ExeTimeAspect 프록시 -> CacheAspect 프록시 -> 실제 대상 객체 

`Output`  
```log
CacheAspect: Cache에 추가[7], Result: 5040
RecCalculator.factorial([7]) 실행 시간 : 230406 ns
CacheAspect: Cache에서 구함[7], Result: 5040
RecCalculator.factorial([7]) 실행 시간 : 91243 ns
```
<br>

#### 7.4.3 @Around의 Pointcut 설정과 @Pointcut 재사용  
@Pointcut 어노테이션이 아닌 @Around 어노테이션에 execution 명시자를 직접 지정할 수도 있다. 
```Java
@Aspect
public class CacheAspect{

	@Around(execution(public * chap07..*(..)))
	public Object execute(ProceedingJointPoint joinPoint) throws Throwable{

	}
}
```

만약 같은 Pointcut을 여러 Advice가 함께 사용한다면 공통 Pointcut을 재사용할 수도 있다.  

사실 이미 Pointcut을 재사용하는 코드를 앞서 작성했다.
```Java
@Aspect
public class ExeTimeAspect {

	@Pointcut("execution(public * chap07..*(long))")
	private void publicTarget() {
	}
	
	@Around("publicTarget()")
	public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
	}
}
```
이 코드에서 @Around는 publicTarget() 메서드에 설정한 Pointcut을 사용한다. 
publicTarget() 메서드는 private인데 이 경우 같은 클래스에 있는 @Around 어노테이션에서만 해당 설정을 사용할 수 있다. 

다른 클래스에 위치한 @Around 어노테이션에서 publicTarget() 메서드의 Pointcut을 사용하고 싶다면 publicTarget() 메서드를 public으로 바꾸면 된다. 
그리고 해당 Pointcut의 완전한 클래스 이름을 포함한 메서드 이름을 @Around 어노테이션에서 사용하면 된다. 
```Java
@Aspect
public class ExeTimeAspect {

	@Pointcut("execution(public * chap07..*(long))")
	public void publicTarget() {
	}
}


@Aspect
public class cacheAspect{

	@Around(aspect.ExeTimeAspect.publicTarget())
	//@Around(ExeTimeAspect.publicTarget()) 같은 패키지에 위치하므로 패키지 이름이 없는 클래스 이름으로 설정 가능 
}
```
<br>

여러 Aspect에서 공통으로 사용하는 Pointcut이 있다면 그림과 같이 별도 클래스에 Pointcut을 정의하고, 각 Aspect 클래스에서 해당 Pointcut을 사용하도록 구성하면 Pointcut 관리가 편해진다. 

![spring5_7_10](/assets/book/spring5/spring5_7_10.png){: width="60%" height="auto"}  

그림에서 @Pointcut을 설정한 CommonPointcut은 빈으로 등록할 필요가 없다. 
@Around 어노테이션에서 해당 클래스에 접근 가능하면 해당 Pointcut을 사용할 수 있다.  



<br><br><br>

---

**Reference**  
- 초보 웹 개발자를 위한 스프링 5 (최범균)

---
