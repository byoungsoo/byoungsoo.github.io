---
layout: post
title: "Java 기초 개념"
author: "Byoungsoo Ko"
category: java
tags: java programming
---

# Java

>객체지향 프로그래밍 개념

현실 세계에 존재하는 사물이나 개념을 그대로 프로그램을 옮겨 놓은 것
객체: 실제 사물이 가진 속성과 행동을 분석해 프로그램으로 구현한 것으로 상태와 행동을 가진다.

1. 객체지향의 특성

-	은닉성 (Encapsulation)

클래스의 사용자에게 필요한 최소의 기능만을 노출하고 내부를 숨기는 것
클래스에 선언되어 있는 필드와 메소드 중에 사용자 노출 여부 결정 가능 (Private 선언)  


-	상속성

기존에 있는 클래스의 코드를 이용하여 새로운 코드를 생성하거나 재정의
프로그램의 개발 속도 개선 및 코드 중복 방지
기존에 정의된 클래스의 모든 멤버와 기능을 물려받아 사용하는 것으로 기존에 정의된 클래스는 부모 클래스, 물려 받은 클래스는 자식 클래스라고 한다. 자식 클래스에서 extends 키워드를 사용하여 상속 받는다.

```java
public class Parent {
}
public class Child extends Parent{
}
```

-	다형성

서로 다른 클래스와 객체들이 같은 메시지에 대해 각자의 방법으로 작동할 수 있도록 하는 방법
하나의 인터페이스를 제공하고 여러 개의 클래스가 그 인터페이스를 각자의 방법으로 구현
상위 클래스에서 공통 규약을 만들고 서로 다른 하위 클래스의 객체에 각각 정의한 메소드를 동일한 방식으로 호출하여 사용 (Overriding)
Java에서는 다중 상속이 지원 되지 않는데 Interface를 사용하여 해결 한다. Interface는 추상 메소드만을 가진다.
Interface 키워드로 클래스를 선언하며 abstract으로 메소드를 추상화 한다. Abstract 키워드가 없어도 컴파일 시 자동 추가.

```java
Interface Transport {
	abstract void move();
}
```
Implements 키워드를 통해 구현하며 Transport에 정의된 추상화 메소드인 move를 재정의 해주어야 한다.

```java
public class Car implements Transport{
	@Override
	public void move() {
		System.out.println("Car is moving");
	}
}

public class Train implements Transport{
	@Override
	public void move() {
		System.out.println("Train is moving");
	}
}

public class Test {
	public static void main(String args[]) {
		Car c1 = new Car();
		Train t1 = new Train();
		Transport p1;

		p1 = (Transport)t1;

		if(p1 instanceof Car) {
			p1.move();
		}
		else {
			p1.move();
		}
	}
}
```
