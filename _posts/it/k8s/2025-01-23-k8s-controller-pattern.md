---
layout: post
title: "kubernetes Controller Pattern"
author: "Bys"
category: k8s
date: 2025-01-23 01:00:00
tags: kubernetes pattern controller
---

# [Controllers](https://kubernetes.io/docs/concepts/architecture/controller/)
로봇공학 및 자동화에서는 control loop 은 종료되지 않는 loop 으로 시스템 상태를 조절하며 하나의 예시로서 온도계를 설명할 수 있다. 
우리가 온도를 설정하면 이 것은 온도계에게 desired state (원하는 온도)를 이야기한 것 과 같다. 실제 방의 온도는 current state (현재 상태)와 같다. 온도계는 현재 방의 온도(current state)를 원하는 온도(desired state)로 맞추기 위해 장비를 켜거나 끔으로서 원하는 상태를 맞추기 위해 노력한다.  

쿠버네티스에서도 컨트롤러는 클러스터의 상태를 watch 하고 필요한 곳에 생성 또는 변경을 요청하는 control loop 입니다. 각각의 컨트롤러는 현재 클러스터의 상태를 원하는 상태로 맞추려고 한다.  

자원 상태가 변경(예를 들어 Deployment 의 replicas 속성 값 변경) 될 때마다 쿠버네티스는 이벤트를 생성해 이해관계가 있는 모든 리스너에게 브로드캐스트 한다. 
리스너는 새로운 자원을 수정, 삭제, 생성함으로 반을할 수 있고, 이를 통해 파드 생성 이벤트 등의 또 다른 이벤트가 생성된다. 또한, 이러한 이벤트는 여타 컨트롤러에 의해 다시 선택되어, 특정 작업을 수행할 수도 있다. 

이러한 전체 절차를 State Reconciliation 이라고 한다. 여기서 대상 상태가 현재 상태와 다르며 컨트롤러가 다시 요청한 상태가 되도록 Reconcile 하는 작업을 수행한다.


### [Kubernetes Controller Pattern](https://kubernetes.io/docs/concepts/architecture/controller/#controller-pattern)
하나의 컨트롤러는 적어도 하나의 리소스를 추적하며 이러한 오브젝트들은 desired state 를 표현하는 spec 필드 값을 가지고 있다. 해당 리소스의 컨트롤러들은 현재 상태를 의도한 상태에 가깝게 만드는 역할을 한다.  

- Control via API server 
API 서버와 상호작용함으로서 상태를 관리하는 방법. Ex. kubernetes built-in controller(Job controller, Deployment Controller, Replicaset Controller ...)

- Direct control
일부 컨트롤러는 클러스터 외부의 것들에 대해 변경해야 할 필요가 있다. 
> Controllers that interact with external state find their desired state from the API server, then communicate directly with an external system to bring the current state closer in line.
> In the thermostat example, if the room is very cold then a different controller might also turn on a frost protection heater. With Kubernetes clusters, the control plane indirectly works with IP address management tools, storage services, cloud provider APIs, and other services by extending Kubernetes to implement that.




### Design
Control loop 의 셋트가 모놀리식으로 구성되어 서로 연결되어 있는 것 보다 간단한 컨트롤러로 사용하는 것이 유용하다.  



---

## 📚 References

[1] **Controllers Pattern - Design**  
- https://kubernetes.io/docs/concepts/architecture/controller/#design

[2] **쿠버네티스 패턴**  
- O'REILLY