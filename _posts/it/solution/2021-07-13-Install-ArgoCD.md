---
layout: post
title: "Install Argo CD"
author: "Bys"
category: solution
date: 2021-04-21 01:00:00
tags: cicd gitlab gitlab-runner pipeline docker docker-compose
---


#### - Argo CD  
> Argo CD is a declarative, GitOps continuous delivery tool for Kubernetes  


#### - Kubernets Helm을 통한 설치

`Create Namespace`  
```bash
kubectl create namespace argo
```   

`Donwload Template`  
```bash
helm fetch argo/argo-cd
tar -xvzf argo-cd-3.6.4.tgz
```   

`Modify values.yaml`  
```bash
cd argo-cd
vim values.yaml
```   

`Service Using NLB`  
```yaml
## Server service configuration
  service:
    annotations:
      service.beta.kubernetes.io/aws-load-balancer-type: "nlb"
      service.beta.kubernetes.io/aws-load-balancer-subnets: subnet-1111,subnet-2222
    labels: {}
    type: loadBalancer
```   

`Service Using NLB`  
```bash
helm install argo -n argo argo/argo-cd -f values.yaml
```  

helm을 통해 정상 배포 후에는 



<br>

#### - Install Docker  
```bash
sudo yum install docker
# cicdadm계정으로 docker 사용
sudo usermod -aG docker cicdadm
```
<br>
