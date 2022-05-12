---
layout: post
title: "헤드퍼스트 디자인 패턴"
author: "Bys"
category: it_book
date: 2022-05-10 01:00:00
tags: book programming java design-pattern
---

## 1. 전략 패턴  
알고리즘군을 정의하고 캡슐화해서 각각의 알고리즘군을 수정해서 쓸 수 있게 해 준다. 전략 패턴을 사용하면 클라이언트로부터 알고리즘을 분리해서 독립적으로 변경할 수 있다. 

Duck 클래스에서 quack(), fly() 등과 같이 변화할 수 있는 것들을 따로 뽑아서 캡슐화한다. 
그러면 나중에 바뀌지 않는 부분에는 영향을 미치지 않고 그 부분만 고치거나 확장할 수 있다.

```Java
public abstract class Duck {

    FlyBehavior flyBehavior;

    QuackBehavior quackBehavior;

    public void performFly(){
        flyBehavior.fly();
    }

    public void performQuack(){
        quackBehavior.quack();
    }

    public void swim(){
        System.out.println("Swim");
    };

    public abstract void display();


    public void setFlyBehavior(FlyBehavior flyBehavior){
        this.flyBehavior = flyBehavior;
    }
    public void setQuackBehavior(QuackBehavior quakBehavior){
        this.quackBehavior = quackBehavior;
    }
}
```

```Java
public interface QuackBehavior {

    public void quack();
}

public interface FlyBehavior {

    public void fly();
}

public class FlyWithWings implements FlyBehavior {

    @Override
    public void fly() {
        System.out.println("Fly With Wings");
    }
}

public class Quack implements QuackBehavior {

    @Override
    public void quack() {
        System.out.println("Quack");
    }
}
```

<br>

## 2. 옵저버 패턴  





<br><br><br>

---

**Reference**  
- 헤드퍼스트 디자인패턴 (Eric Freeman, Elisabeth Robson)

---

 
