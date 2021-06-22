---
layout: post
title: "AWS EKS Cluster 생성"
author: "Bys"
category: cloud
date: 2021-03-25 01:00:00
tags: aws eks eksctl
---

#### - EKS Cluster 생성  

--클러스터 생성 이미지 캡처

마스터 계정만 접근 가능하므로 최초에 cluster를 생성한 IAM User로 aws configure 인증 후 작업


#### - EKS Cluster 설정  
`aws-auth configmap 적용`
클러스터의 유저와 IAM 역할을 관리하기 위해 최초로 aws-auth configmap을 설정한다.  

아래의 명령을 통해 적용여부 체크
```bash
kubectl describe configmap -n kube-system aws-auth
```

적용이 안되었다면 아래의 명령을 통해 yaml 파일을 받은 후 수정을 한 뒤 재 배포 한다.  
```bash
curl -o aws-auth-cm.yaml https://s3.us-west-2.amazonaws.com/amazon-eks/cloudformation/2020-10-29/aws-auth-cm.yaml
```

추가 되는 내용은 EKSAdminRole과 같이 role을 하나 생성하여 맵핑시켜주고 이 후 클러스터 접근에 필요한 인원들의 IAM Role을 해당 Role로 제어 할 수 있다.  
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: aws-auth
  namespace: kube-system
data:
  mapRoles: |
    - rolearn: arn:aws:iam::718652001716:role/SMP-DEV-ROLE-EC2-SSM
      username: system:node:{{EC2PrivateDNSName}}
      groups:
        - system:bootstrappers
        - system:nodes
    - rolearn: arn:aws:iam::718652001716:role/EKSAdminRole
      username: EKSAdminRole
      groups:
        - system:masters

#Sample
apiVersion: v1
data:
  mapRoles: |
    - rolearn: <arn:aws:iam::111122223333:role/eksctl-my-cluster-nodegroup-standard-wo-NodeInstanceRole-1WP3NUE3O6UCF>
      username: <system:node:{{EC2PrivateDNSName}}>
      groups:
        - <system:bootstrappers>
        - <system:nodes>
    - rolearn: <arn:aws:iam::111122223333:role/EKSAdminRole>
      username: EKSAdminRole
      groups:
        - system:masters
  mapUsers: |
    - userarn: <arn:aws:iam::111122223333:user/admin>
      username: <admin>
      groups:
        - <system:masters>
    - userarn: <arn:aws:iam::111122223333:user/ops-user>
      username: <ops-user>
      groups:
        - <system:masters>
```


`Asoociate IAM OIDC to Cluster`  
EKS OIDC 자격 증명 공급  
```bash
eksctl utils associate-iam-oidc-provider --region=ap-northeast-2 --cluster=smp-dev-eks-cluster --approve
```
<br>