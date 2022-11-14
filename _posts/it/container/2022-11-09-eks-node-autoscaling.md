---
layout: post
title: "EKS Worker Node Autoscaling"
author: "Bys"
category: container
date: 2022-11-09 01:00:00
tags: kubernetes eks autoscaling
---

# AutoScaling
여기서 진행하는 Auto Scaling은 Pod의 Auto Scaling과는 다르다. Pod의 경우 HPA 배포를 통해 Auto Scaling을 진행하지만 Pod가 어느정도 이상 늘어나다 가용한 노드가 없는 경우에는 Worker Node의 Auto Scaling이 필요하다.  
EKS에서 Node Group이 Auto Scaling Group에 의해 관리가 되고 있다고 해도 자동으로 Worker Node가 Auto Scaling이 되지는 않는다. 따라서 우리는 Cluster Autoscaler를 통해 Worker Node가 Auto Scaling이 가능하도록 해야 한다.  


## 1. Cluster Autoscaler
Kubernetes Cluster Autoscaler는 pods가 실패하거나 다른 노드로 다시 스케쥴링이 될 때 클러스터의 노드 수를 자동으로 조정한다. Cluster Autoscaler는 일반적으로 클러스터에 Deployment로 설치한다.  

> - Kubernetes Cluster Autoscaler – A core component of the Kubernetes control plane that makes scheduling and scaling decisions. For more information, see Kubernetes Control Plane FAQ on GitHub.
> - AWS Cloud provider implementation – An extension of the Kubernetes Cluster Autoscaler that implements the decisions of the Kubernetes Cluster Autoscaler by communicating with AWS products and services such as Amazon EC2. For more information, see Cluster Autoscaler on AWS on GitHub.
> - Node groups – A Kubernetes abstraction for a group of nodes within a cluster. Node groups aren't a true Kubernetes resource, but they're found as an abstraction in the Cluster Autoscaler, Cluster API, and other components. Nodes that are found within a single node group might share several common properties such as labels and taints. However, they can still consist of more than one Availability Zone or instance type.
> - Amazon EC2 Auto Scaling groups – A feature of AWS that's used by the Cluster Autoscaler. Auto Scaling groups are suitable for a large number of use cases. Amazon EC2 Auto Scaling groups are configured to launch instances that automatically join their Kubernetes cluster. They also apply labels and taints to their corresponding node resource in the Kubernetes API.


## 2. Cluster Autoscaler Install
[Installation Guide](https://docs.aws.amazon.com/eks/latest/userguide/autoscaling.html) 

1. Create an IAM policy and role
2. Deploy the Cluster Autoscaler


## 3. 동작방법
> CA is responsible for ensuring that your cluster has enough nodes to schedule your pods without wasting resources. 
 즉, 파드가 클러스터의 노드에 정상적으로 스케줄링 될 수 있도록 노드의 수를 유지해주는 역할을 한다. 파드가 노드에 스케줄되는 것이 실패하는 것과 노드의 사용량이 낮은 것을 모니터링 하면서 노드를 추가하거나 제거하는 것을 시뮬레이션 하고 적용한다.   
 
 ![autoscaler001](/assets/it/container/eks/autoscaler001.png){: width="50%" height="auto"}

그림을 보면 알다시피 Pending Pod가 생겼을 때 오토스케일링 그룹에서 바로 노드를 늘려주는 것이 아니라 CA가 그것을 인지하고 오토스케일링 그룹으로 인스턴스의 증가/제거를 요청 하는 역할을 하는 것이다.  


<br><br><br>

> Ref: https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/autoscaling.html
> Ref: https://aws.github.io/aws-eks-best-practices/cluster-autoscaling/