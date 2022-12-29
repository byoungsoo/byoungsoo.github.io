---
layout: post
title: "EKS를 위한 eksctl 사용"
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
  name: bys-dev-eks-v123
  region: ap-northeast-2
  version: "1.23"

vpc:
  id: "vpc-0ca96cd5c37d3bae8"
  subnets:
    private:
      bys-dev-sbn-az1-app:
          id: "subnet-0ea5be4984975e8ed"
      bys-dev-sbn-az2-app:
          id: "subnet-0b4076508ce121c27"

managedNodeGroups:
  - name: ng-1
    amiFamily: AmazonLinux2
    instanceType: m5.large
    volumeSize: 80
    minSize: 2
    maxSize: 10
    desiredCapacity: 2
    privateNetworking: true
    subnets:
      - bys-dev-sbn-az1-app
      - bys-dev-sbn-az2-app
    ssh:
      allow: true
      publicKeyName: "bys-console"
    tags:
      auto-delete: "no"
```
<br>

만약 생성 중 Cluster는 생성되고, Node Group생성시 중간에 실패하면 Node Group에 대한 cloudformation stack을 삭제하고 Node Group만 eksctl로 다시 생성한다.  
```bash
eksctl create nodegroup --config-file=cluster.yml
```
이렇게 생성된 노드는 managedNodeGroup으로 aws-auth ConfigMap에 노드의 InstanceRole이 자동으로 등록된다. 

<br>

## 2. [eksctl delete nodegroup](https://docs.aws.amazon.com/eks/latest/userguide/delete-managed-node-group.html)  
아래 커맨드를 통해 nodegroup을 지울 때는 중요한 내용이 있다. 'eksctl create node' 커맨드를 통해 nodeGroup 생성하게 되면 별도로 iam spec을 설정하지 않는 이상 nodeGroup의 Instance Role이 하나 생성되게 되고, 해당 instance role이 'aws-auth' copnfigMap 파일에 등록이 되게 된다.  
중요한 것은 반대로 지울 때도 'aws-auth' configMap에 해당 instance role을 지운다는 것이다. 만약 같은 instance role을 다른 nodeGroup에서도 사용하는 경우가 있다면 다른 nodeGroup에서는 notReady 상태로 상태가 변경될 수 있다. 이 점을 주의해서 사용해야 한다.  

```bash
eksctl delete nodegroup
```

## 3. eksctl create iamserviceaccount
아래는 eksctl 커맨드를 통해 ServiceAccount와 IAM Role을 관리하는 방법이다.  

```bash
# Create IAM Role with ServiceAccount
eksctl create iamserviceaccount \
  --cluster=bys-dev-eks-main \
  --namespace=aws  \
  --name=awssdk-storage-sa \
  --role-name "AwsSdkStorageAppRole" \
  --attach-policy-arn=arn:aws:iam::aws:policy/AmazonS3FullAccess \
  --override-existing-serviceaccounts \
  --approve
```
- AwsSdkStorageAppRole이름의 IAM Role을 생성한다.
- `bys-dev-eks-main` EKS Cluster에 `aws` namespace에 `awssdk-storage-sa` ServiceAccount를 생성한다. 
- ServiceAccount와 IAM Role을 맵핑한다. ([IRSA](https://docs.aws.amazon.com/eks/latest/userguide/iam-roles-for-service-accounts.html) / eks.amazonaws.com/role-arn: arn:aws:iam::558846430793:role/AwsSdkStorageAppRole) 


```bash
# Create ServiceAccount and attach exist role to ServiceAccount, it need to add trust relationship manually.
eksctl create iamserviceaccount \
  --cluster=bys-dev-eks-main \
  --namespace=kube-system \
  --name=aws-load-balancer-controller \
  --attach-role-arn=arn:aws:iam::558846430793:role/AmazonEKSLoadBalancerControllerRole \
  --override-existing-serviceaccounts \
  --approve
```
- `bys-dev-eks-main`라는 EKS Cluster에 `kube-system` namespace에 `aws-load-balancer-controller` ServiceAccount를 생성한다. 
- ServiceAccount와 IAM Role을 맵핑한다. ([IRSA](https://docs.aws.amazon.com/eks/latest/userguide/iam-roles-for-service-accounts.html) / eks.amazonaws.com/role-arn: arn:aws:iam::558846430793:role/AwsSdkStorageAppRole) 

<br>

## 4. [eksctl Add-ons](https://docs.aws.amazon.com/eks/latest/userguide/managing-add-ons.html)
기본적으로 AWS Console > EKS > Add-ons 메뉴에서 보이는 Add-on 서비스들은 managed Add-ons다. 즉, AWS에서 제공하는 API를 통해 Cluster로 배포된 Add-ons서비스이다. 이렇게 Managed Add-ons를 관리하는 방법은 awscli, eksctl, AWS Console 등 AWS에서 제공하는 API를 이용하는 경우다. 
다음은 eksctl을 통해 Add-ons를 Upgrade하는 방법이다. 각각의 version은 AWS공식문서에서 제공하는 Kubernetes와 호환되는 버전을 찾아 입력한다. 

1. Add-ons확인
    ```bash
    eksctl utils describe-addon-versions --kubernetes-version 1.23 | grep AddonName
    ```

2. Config파일 생성  

    `update-addon-vpccni.yaml`
    ```yaml
    apiVersion: eksctl.io/v1alpha5
    kind: ClusterConfig
    metadata:
      name: bys-dev-eks-main
      region: ap-northeast-2

    addons:
    - name: vpc-cni
      version: 1.12.0-eksbuild.1
      resolveConflicts: overwrite
    ```

    `update-addon-coredns.yaml`
    ```yaml
    apiVersion: eksctl.io/v1alpha5
    kind: ClusterConfig
    metadata:
      name: bys-dev-eks-main
      region: ap-northeast-2

    addons:
    - name: coredns
      version: v1.8.4-eksbuild.2
      resolveConflicts: overwrite
    ```

    `update-addon-kubeproxy.yaml`
    ```yaml
    apiVersion: eksctl.io/v1alpha5
    kind: ClusterConfig
    metadata:
      name: bys-dev-eks-main
      region: ap-northeast-2

    addons:
    - name: kube-proxy
      version: v1.21.14-minimal-eksbuild.4			
      resolveConflicts: overwrite
    ```

3. 적용
    ```bash
    eksctl update addon -f update-addon-vpccni.yaml
    eksctl update addon -f update-addon-coredns.yaml
    eksctl update addon -f update-addon-kubeproxy.yaml
    ```


<br><br><br>

> Ref: [https://eksctl.io/usage/creating-and-managing-clusters/](https://eksctl.io/usage/creating-and-managing-clusters/)  
> Ref: https://docs.aws.amazon.com/eks/latest/userguide/delete-managed-node-group.html