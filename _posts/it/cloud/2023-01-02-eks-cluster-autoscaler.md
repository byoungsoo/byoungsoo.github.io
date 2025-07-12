---
layout: post
title: "EKS Cluster Autoscaler를 통한 노드 Autoscaling"
author: "Bys"
category: cloud
date: 2023-01-02 01:00:00
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
3. Edit Configuration


## 3. [동작방법](https://github.com/kubernetes/autoscaler/blob/master/cluster-autoscaler/FAQ.md#table-of-contents)  
> CA is responsible for ensuring that your cluster has enough nodes to schedule your pods without wasting resources. It watches for pods that fail to schedule and for nodes that are underutilized. It then simulates the addition or removal of nodes before applying the change to your cluster. The AWS Cloud Provider implementation within Cluster Autoscaler controls the .DesiredReplicas field of your EC2 Auto Scaling Groups.

 즉, 파드가 클러스터의 노드에 정상적으로 스케줄링 될 수 있도록 노드의 수를 유지해주는 역할을 한다. 파드가 노드에 스케줄되는 것이 실패하는 것과 노드의 사용량이 낮은 것을 모니터링 하면서 노드를 추가하거나 제거하는 것을 시뮬레이션 하고 적용한다.   

### Scale-up

 ![autoscaler001](/assets/it/cloud/eks/autoscaler001.png){: width="80%" height="auto"}

그림을 보면 알다시피 Pending Pod가 생겼을 때 오토스케일링 그룹에서 바로 노드를 늘려주는 것이 아니라 CA가 그것을 인지하고 오토스케일링 그룹으로 인스턴스의 증가/제거를 요청 하는 역할을 하는 것이다.  

### Scale-down
HPA에 의해 노드가 확장한 이 후 다시 파드가 줄어들면서 노드가 scale-down이 될 때 어떻게 동작하는지 살펴보자.  

더 이상 노드의 확장이 필요하지 않은 경우 10 초마다 (configurable by --scan-interval flag) CA는 어떤 노드가 필요 없는지 확인한다. 그리고 아래의 모든 조건이 충족되면 제거가 고려된다. [문서](https://github.com/kubernetes/autoscaler/blob/master/cluster-autoscaler/FAQ.md#how-does-scale-down-work)  
1. The sum of cpu and memory requests of all pods running on this node (DaemonSet pods and Mirror pods are included by default but this is configurable with --ignore-daemonsets-utilization and --ignore-mirror-pods-utilization flags) is smaller than 50% of the node's allocatable. (Before 1.1.0, node capacity was used instead of allocatable.) Utilization threshold can be configured using --scale-down-utilization-threshold flag.
    - scale-down-utilization-threshold: Node utilization level, defined as sum of requested resources divided by capacity, below which a node can be considered for scale down (Deafult/0.5)

2. All pods running on the node (except these that run on all nodes by default, like manifest-run pods or pods created by daemonsets) can be moved to other nodes. 
    - [다만 파드가 다른 노드로 이동할 수 없는 경우](https://github.com/kubernetes/autoscaler/blob/master/cluster-autoscaler/FAQ.md#what-types-of-pods-can-prevent-ca-from-removing-a-node)에는 scale-down이 불가하다. Ex) PDB, 'cluster-autoscaler.kubernetes.io/safe-to-evict' 관련 annotation 등.

3. [It doesn't have scale-down disabled annotation](https://github.com/kubernetes/autoscaler/blob/master/cluster-autoscaler/FAQ.md#how-can-i-prevent-cluster-autoscaler-from-scaling-down-a-particular-node)  
    - '"cluster-autoscaler.kubernetes.io/scale-down-disabled": "true"'


이러한 조건이 만족되고 10분이 지나면 노드가 제거되는데 아래와 같이 동작한다. Unneeded시간은 (--scale-down-unneeded-time)에 의해 조정된다.  
> If a node is unneeded for more than 10 minutes, it will be terminated. 

1. 노드는 SchedulingDisabled 상태로 변경된다. 
    ```bash
    Mon Jan  2 14:24:50 KST 2023
    NAME                                              STATUS   ROLES    AGE   VERSION
    ip-10-20-10-235.ap-northeast-2.compute.internal   Ready    <none>   10d   v1.21.14-eks-fb459a0
    ip-10-20-10-55.ap-northeast-2.compute.internal    Ready    <none>   13m   v1.21.14-eks-fb459a0
    ip-10-20-11-78.ap-northeast-2.compute.internal    Ready    <none>   21m   v1.21.14-eks-fb459a0
    ip-10-20-11-91.ap-northeast-2.compute.internal    Ready    <none>   13d   v1.21.14-eks-fb459a0

    Mon Jan  2 14:24:55 KST 2023
    NAME                                              STATUS                     ROLES    AGE   VERSION
    ip-10-20-10-235.ap-northeast-2.compute.internal   Ready                      <none>   10d   v1.21.14-eks-fb459a0
    ip-10-20-10-55.ap-northeast-2.compute.internal    Ready                      <none>   13m   v1.21.14-eks-fb459a0
    ip-10-20-11-78.ap-northeast-2.compute.internal    Ready,SchedulingDisabled   <none>   21m   v1.21.14-eks-fb459a0
    ip-10-20-11-91.ap-northeast-2.compute.internal    Ready                      <none>   13d   v1.21.14-eks-fb459a0

    Mon Jan  2 14:25:31 KST 2023
    NAME                                              STATUS                     ROLES    AGE   VERSION
    ip-10-20-10-235.ap-northeast-2.compute.internal   Ready                      <none>   10d   v1.21.14-eks-fb459a0
    ip-10-20-10-55.ap-northeast-2.compute.internal    Ready                      <none>   14m   v1.21.14-eks-fb459a0
    ip-10-20-11-78.ap-northeast-2.compute.internal    Ready,SchedulingDisabled   <none>   22m   v1.21.14-eks-fb459a0
    ip-10-20-11-91.ap-northeast-2.compute.internal    Ready                      <none>   13d   v1.21.14-eks-fb459a0

    Mon Jan  2 14:25:36 KST 2023
    NAME                                              STATUS   ROLES    AGE   VERSION
    ip-10-20-10-235.ap-northeast-2.compute.internal   Ready    <none>   10d   v1.21.14-eks-fb459a0
    ip-10-20-10-55.ap-northeast-2.compute.internal    Ready    <none>   14m   v1.21.14-eks-fb459a0
    ip-10-20-11-91.ap-northeast-2.compute.internal    Ready    <none>   13d   v1.21.14-eks-fb459a0
    ```

2. Removed 대상이 된 노드에 존재하는 파드는 Terminating된다. (Eviction 발생)
    ```bash
    Mon Jan  2 14:24:37 KST 2023
    netutil-6888c55849-2vclr       1/1     Running   0          15m   10.20.11.222   ip-10-20-11-78.ap-northeast-2.compute.internal    <none>           <none>

    Mon Jan  2 14:24:42 KST 2023
    netutil-6888c55849-2vclr       0/1     Terminating         0          15m   10.20.11.222   ip-10-20-11-78.ap-northeast-2.compute.internal    <none>           <none>

    Mon Jan  2 14:24:47 KST 2023
    netutil-6888c55849-2vclr       0/1     Terminating   0          15m   10.20.11.222   ip-10-20-11-78.ap-northeast-2.compute.internal    <none>           <none>
    ```

3. 다른 노드에 Scheduling
    ```bash
    Mon Jan  2 14:24:38 KST 2023
    php-apache1-795b6c9c76-qthxq   1/1     Running   0          14m   10.20.10.192   ip-10-20-10-55.ap-northeast-2.compute.internal    <none>           <none>

    Mon Jan  2 14:24:43 KST 2023
    netutil-6888c55849-fqpnj       0/1     ContainerCreating   0          4s    <none>         ip-10-20-10-55.ap-northeast-2.compute.internal    <none>           <none>
    php-apache1-795b6c9c76-qthxq   1/1     Running             0          14m   10.20.10.192   ip-10-20-10-55.ap-northeast-2.compute.internal    <none>           <none>

    Mon Jan  2 14:24:49 KST 2023
    netutil-6888c55849-fqpnj       1/1     Running   0          9s    10.20.10.15    ip-10-20-10-55.ap-northeast-2.compute.internal    <none>           <none>
    php-apache1-795b6c9c76-qthxq   1/1     Running   0          15m   10.20.10.192   ip-10-20-10-55.ap-northeast-2.compute.internal    <none>           <none>
    ```



---

## 📚 References

[1] **AWS Doc**  
- https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/autoscaling.html

[2] **EKS Best Practices Guides**  
- https://aws.github.io/aws-eks-best-practices/cluster-autoscaling/

[3] **Github - Frequently Asked Questions**  
- https://github.com/kubernetes/autoscaler/blob/master/cluster-autoscaler/FAQ.md

[4] **parameters to CA**  
- https://github.com/kubernetes/autoscaler/blob/master/cluster-autoscaler/FAQ.md#what-are-the-parameters-to-ca
