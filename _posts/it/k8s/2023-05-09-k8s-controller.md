---
layout: post
title: "Kubernetes Controller"
author: "Bys"
category: k8s
date: 2023-05-09 01:00:00
tags: kubernetes controller
---

# [Kubernetes Controller](https://kubernetes.io/docs/concepts/architecture/controller/)

## [Controller]()
쿠버네티스에서 컨트롤러는 클러스터의 상태를 감지하다가 필요한 경우 생성 또는 변경을 요청하는 control loops이다. 각 컨트롤러는 현재 상태를 Desired 상태로 변경하도록 시도한다.  
> In Kubernetes, controllers are control loops that watch the state of your cluster, then make or request changes where needed. Each controller tries to move the current cluster state closer to the desired state.

<br><br>

## Controller pattern
컨트롤러는 적어도 하나 이상의 리소스를 추적하며 이 객체는 spec 필드에 desired state를 가지고 있다. 컨트롤러는 리소스의 current state를 desired state에 가깝게 하는데 책임이 있다.  
> A controller tracks at least one Kubernetes resource type. These objects have a spec field that represents the desired state. The controller(s) for that resource are responsible for making the current state come closer to that desired state.


- Control via API server 
내장형 Controller들은 API 서버와 상호 작용하며 state를 관리한다.  
> Built-in controllers manage state by interacting with the cluster API server.

The Job controller does not run any Pods or containers itself. Instead, the Job controller tells the API server to create or remove Pods. 
After you create a new Job, the desired state is for that Job to be completed. The Job controller makes the current state for that Job be nearer to your desired state: creating Pods that do the work you wanted for that Job, so that the Job is closer to completion.
Controllers also update the objects that configure them. For example: once the work is done for a Job, the Job controller updates that Job object to mark it Finished.

- Direct control 
일부 컨트롤러들은 클러스터 외부에 있는 것들에 대해서 변경을 만들 필요가 있다.  
> Some controllers need to make changes to things outside of your cluster.

클러스터에 충분한 노드가 있게 하기 위해서 control loop을 사용하는 경우에는 컨트롤러가 필요할 때 새노드를 생성하기 위해 현재 클러스터 외부에서 어떤 것들이 필요할 수 있다. 
> For example, if you use a control loop to make sure there are enough Nodes in your cluster, then that controller needs something outside the current cluster to set up new Nodes when needed.

External state와 상호작용하는 컨트롤러는 API 서버를 통해 desired state를 확인하고 external 시스템과 direct로 통신하여 current state를 desired state에 가깝게 변경한다. Cluster Autoscaler.



## Ways of running controllers 
Kubernetes comes with a set of built-in controllers that run inside the kube-controller-manager. These built-in controllers provide important core behaviors.

You can find controllers that run outside the control plane, to extend Kubernetes. Or, if you want, you can write a new controller yourself. 
You can run your own controller as a set of Pods, or externally to Kubernetes. 

<br><br><br>

- References
1. 