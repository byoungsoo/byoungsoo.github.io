---
layout: post
title: "EKS VPC CNI란"
author: "Bys"
category: incubator
date: 2023-01-01 01:00:00
tags: incubator
---

# [CNI](https://github.com/containernetworking/cni)  
CNI는 CNCF 프로젝트며 Linux container들의 네트워크 인터페이스를 구성하기 위한 libraries와 specification으로 구성되어있다. 
즉, CNI는 컨테이너 간의 네트워킹을 제어할 수 있는 플러그인을 만들기 위한 표준이다.

# [VPC CNI](https://github.com/aws/amazon-vpc-cni-k8s)xrf
AWS에서 제공하며 VPC의 ENI를 사용하여 쿠버네티스의 파드 네트워킹을 위한 네트워크 플러그인이다.   
> Networking plugin for pod networking in Kubernetes using Elastic Network Interfaces on AWS.

- For each Kubernetes node (ec2 instance), create multiple elastic network interfaces (ENIs) and allocate their secondary IP addresses.
- For each pod, pick a free secondary IP address, assign it to the pod, wire host and pod networking stack to allow:
    - Pod to Pod on a single host communication
    - Pod to Pod on different hosts communication
    - Pod to other AWS service communication
    - Pod to on-premises data center communication
    - Pod to internet communication

## 1. [Components]()  
VPC CNI는 두 가지 component로 구성된다. 

1. CNI Plugin  
    CNI Plugin, which will wire up the host's and pod's network stack when called.
    > 쿠버네티스 1.24 이전까지는 cni-bin-dir과 network-plugin 커맨드 라인 파라미터를 사용해 kubelet이 CNI 플러그인을 관리하게 할 수도 있었다. 이 커맨드 라인 파라미터들은 쿠버네티스 1.24에서 제거되었으며, CNI 관리는 더 이상 kubelet 범위에 포함되지 않는다.

2. IPAMD (node-Local IP Address Management (IPAM) daemon)
    - maintaining a warm-pool of available IP addresses
    - assigning an IP address to a Pod.
    - L-IPAM 데몬은 각 호스트에서 warm-pool을 유지하며 kubelet이 파드 추가 요청을 받을 때 마다 즉시 secondary IP를 할당한다. 
    - IP 주소가 설정된 임계치 보다 낮으면 아래의 동작을 통해 warm-pool을 유지한다. (ENI생성 및 EC2에 attach, ENI에 secondary IP할당, IP주소가 할당 가능해지면 IP들을 warm-pool에 추가한다) 
    CreateNetworkInterface -> AttachNetworkInterface -> ModifyNetworkInterfaceAttribute -> AssignPrivateIpAddresses


## 2. [IPAMD](https://github.com/aws/amazon-vpc-cni-k8s/blob/master/docs/eni-and-ip-target.md)  
CNI binary는 gRPC를 통해 ipamd를 호출하며 새로운 파드를 위한 IP를 요청한다. 만약 할당 가능한 IP가 없을 경우 오류를 반환한다. 유지가능한 IP의 숫자는 WARM_ENI_TARGET, WARM_IP_TARGET, MINIMUM_IP_TARGET 값에 의해 조절된다. 
기본 값은 WARM_ENI_TARGET=1 이며 이는 ipamd가 반드시 "a full ENI" 만큼의 가능한 IP를 

## 3. [ECS Task Definition]()  

## 4. [ECS Service]()  

## 5. [ECS Fargate]()  

## 10. [Trouble Shooting]()  

<br><br><br>

- References  
[1] 