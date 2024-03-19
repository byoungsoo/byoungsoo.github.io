---
layout: post
title: "EKS SGP(Security Group for Pod)"
author: "Bys"
category: incubator
date: 2023-07-04 01:00:00
tags: aws cloud eks sgp pod vpc-resource-controller branch-eni trunk-eni
---

# [Security Group for Pods](https://docs.aws.amazon.com/eks/latest/userguide/security-groups-for-pods.html)  

## 1. Install  

## 2. 동작방식  
https://aws.amazon.com/ko/blogs/containers/introducing-security-groups-for-pods/
https://guide.aws.dev/articles/ARVptFLTVMSk29jZDXpkFFjQ/eks-internals-of-security-group-for-pods

-----------------
1. set the ENABLE_POD_ENI variable to true 
  - IPAMD adds label on the node - vpc.amazonaws.com/has-trunk-attached=false
  - ipamd가 자신의 노드에 ENI 빈 슬롯이 있는 경우 Trunk ENI를 할당한다. (1.15 버전에서 부터는 더 이상 Annotation을 달지 않음)
  - cninode 로 대신함 (kubectl get cninode -A)

2. vpc-resource-controller가 vpc.amazonaws.com/has-trunk-attached 어노테이션을 Watch하고 AWS로 Create Trunk ENI 요청
3. vpc-resource-controller가 노드 레이블 업데이트 vpc.amazonaws.com/has-trunk-attached=true

4. Pod 생성 요청시 Mutating Webhook이 동작하여 vpc.amazonaws.com/pod-eni: 1 어노테이션 

5. vpc-resource-controller: SecurityGroupRequested
6. vpc-resource-controller: AWS API Called - CreateNetworkInterface, CreateNetworkInterfacePermission
7. Pod에 Patch 작업을 통한 annotation 설정(vpc.amazonaws.com/pod-eni)
8.  IPAMD가 branch-eni 할당 (이과정에서 1분 까지소요)
-----------------



## 10. Trouble Shooting

<br><br><br>



-----------------
kubectl set env daemonset aws-node -n kube-system ENABLE_POD_ENI=true
설정하고나면

잠시후 어노테이션
kubectl get nodes -o wide -l vpc.amazonaws.com/has-trunk-attached=true

노드에는 trunk-eni가할당됨
-----------------




- References  
[1] https://guide.aws.dev/articles/ARVptFLTVMSk29jZDXpkFFjQ