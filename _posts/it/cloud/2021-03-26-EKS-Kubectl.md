---
layout: post
title: "AWS EKS Kubectl 설정"
author: "Bys"
category: cloud
date: 2021-03-26 01:00:00
tags: aws eks istio bookinfo
---
<br>

#### - kubectl설정  
kubectl 설치
https://kubernetes.io/ko/docs/tasks/tools/install-kubectl-windows/

aws configure 설정을 통해 access_key, secret_key, region, format을 입력한다.  

이후 다음과 같은 AWS-CLI를 통해 EKS Config 파일을 내려 받는다.  
```cmd
aws eks update-kubeconfig --name <CLUSTER-NAME> --profile <PROFILE_NAME> --region <REGION_NAME>
```

\--profile option 의 경우 multi-account를 사용하여 assume-role이 필요한 경우 사용한다.  
 config 파일에 아래와 같이 profile을 설정한다. profile의 role_arn 의 값은 eks-cluster에 접근 권한이 있는 role이다.  
```config
[default]
region = ap-northeast-2
output = json

[profile PROFILE_NAME]
role_arn = arn:aws:iam::ACCOUNT:role/ROLE_NAME
source_profile = default
```
<br>

이 후 아래와 같이 aws eks update-kubeconfig 명령을 통해 config 파일을 확보한다.  
```cmd
aws configure  
...
aws sts get-caller-identity 
...

aws eks update-kubeconfig --name SMP-DEV-EKS-CLUSTER --region ap-northeast-2
```
```cmd
Added new context arn:aws:eks:ap-northeast-2:718652001716:cluster/SMP-DEV-EKS-CLUSTER to C:\Users\user_name\.kube\config
```

위 의 aws eks update 명령어 수행 결과를 보면 최종적으로 사용자 폴더 밑에 .kube/config 파일이 생성된 것을 확인 할 수 있다.  
여러 환경을 사용한다면 config 파일이 매번 수정 되므로 이번에는 config 파일을 smp-dev-config 파일로 이름을 변경한다.  

이제 smp-dev-config파일을 적용하여 kubectl 명령을 수행 할 것이다.  
```cmd
cd C:\Users\user_name\.kube
set KUBECONFIG="smp-dev-config"
```
<br>

정상적으로 호출이 되는지 확인을 위해 아래와 같이 명령을 수행한다.  
```cmd
kubectl cluster-info
```
아래와 같이 cluster-info가 출력이 되면 완료가 되었다.  
```cmd
Kubernetes control plane is running at https://07DC6DFD63866257FB41C****************.yl4.ap-northeast-2.eks.amazonaws.com
CoreDNS is running at https://07DC6DFD63866257FB***.yl4.ap-northeast-2.eks.amazonaws.com/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
To further debug and diagnose cluster problems, use 'kubectl cluster-info dump'.
```