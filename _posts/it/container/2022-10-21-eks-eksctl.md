---
layout: post
title: "eksctl을 통한 eks 설치"
author: "Bys"
category: container
date: 2022-10-21 01:00:00
tags: kubernetes eksctl
---

# eksctl

eksctl은 eks클러스터를 생성, 관리하기 위한 커맨드 툴로 kubernetes가 아닌 eks를 사용하기 위한 툴이다.   
[Install](https://docs.aws.amazon.com/eks/latest/userguide/eksctl.html)


## 1. eksctl create cluster with nodegroup
아래 cluster.yaml 파일을 생성 한 후 eksctl로 create cluster를 생성하면 cloudformation stack이 생성된다. eksctl을 통해 EKS Cluster와 Managed Node Group을 쉽게 생성할 수 있다. 

```bash
eksctl create cluster -f cluster.yaml
```
`cluster.yaml`
```yaml
apiVersion: eksctl.io/v1alpha5
kind: ClusterConfig

metadata:
  name: bys-dev-eks-main
  region: ap-northeast-2

vpc:
  id: "vpc-0ca96cd5c37d3bae8"
  subnets:
    private:
      bys-dev-sbn-az1-app:
          id: "subnet-0ea5be4984975e8ed"
      bys-dev-sbn-az2-app:
          id: "subnet-0b4076508ce121c27"

managedNodeGroups:
  - name: bys-dev-ng-manged-v1
    amiFamily: AmazonLinux2
    instanceType: m5.large
    volumeSize: 80
    minSize: 4
    maxSize: 10
    desiredCapacity: 4
    privateNetworking: true
    subnets:
      - bys-dev-sbn-az1-app
      - bys-dev-sbn-az2-app
    labels: {role: worker}
    securityGroups:
      attachIDs: ["sg-03103f41dab2d16fb"]
    ssh:
      allow: true
      publicKeyName: "bys-console"
      # new feature for restricting SSH access to certain AWS security group IDs
    tags:
      name: "worker"
      auto-delete: "no"
```
<br>


<br><br><br>

> Ref: [https://eksctl.io/usage/creating-and-managing-clusters/](https://eksctl.io/usage/creating-and-managing-clusters/)  
