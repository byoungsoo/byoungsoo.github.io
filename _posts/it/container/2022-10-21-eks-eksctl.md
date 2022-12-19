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

### 알아두면 좋을 것들 


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
  - name: ng-main
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
    labels: {role: worker-v1.22}
    securityGroups:
      attachIDs: ["sg-03103f41dab2d16fb"]
    ssh:
      allow: true
      publicKeyName: "bys-console"
      # new feature for restricting SSH access to certain AWS security group IDs
    tags:
      name: "worker"
```
<br>

만약 생성 중 Cluster는 생성되고, Node Group생성시 중간에 실패하면 Node Group에 대한 cloudformation stack을 삭제하고 Node Group만 eksctl로 다시 생성한다.  
```bash
eksctl create nodegroup --config-file=cluster.yml
```


## 2. eksctl delete nodegroup
아래 커맨드를 통해 nodegroup을 지울 때는 중요한 내용이 있다. 'eksctl create node' 커맨드를 통해 nodeGroup 생성하게 되면 별도로 iam spec을 설정하지 않는 이상 nodeGroup의 Instance Role이 하나 생성되게 되고, 해당 instance role이 'aws-auth' copnfigMap 파일에 등록이 되게 된다.  
중요한 것은 반대로 지울 때도 'aws-auth' configMap에 해당 instance role을 지운다는 것이다. 만약 같은 instance role을 다른 nodeGroup에서도 사용하는 경우가 있다면 다른 nodeGroup에서는 notReady 상태로 상태가 변경될 수 있다. 이 점을 주의해서 사용해야 한다.  

```bash
eksctl delete nodegroup
```


<br><br><br>

> Ref: [https://eksctl.io/usage/creating-and-managing-clusters/](https://eksctl.io/usage/creating-and-managing-clusters/)  
> Ref: [Config File Scheme](https://eksctl.io/usage/schema/#metadata-version)
> Ref: https://docs.aws.amazon.com/eks/latest/userguide/delete-managed-node-group.html