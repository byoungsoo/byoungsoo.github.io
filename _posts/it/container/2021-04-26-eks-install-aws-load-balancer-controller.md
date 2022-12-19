---
layout: post
title: "EKS AWS Load Balancer Controller 설치"
author: "Bys"
category: container
date: 2022-11-14 01:00:00
tags: aws eks ingress alb
---

# AWS Load Balancer Controller
Kubernetes에서는 CSP사들의 resource를 사용하기 위해서 자체적으로 개발해서 가지고 있는 controller 들이 존재한다. 그것을 In-tree controller라고 부른다. 우리는 In-tree controller를 사용하는 경우 kubernetes를 설치할 때 이미 자체적으로 가지고 있으므로 별도의 controller 설치 없이 사용이 가능하다.  
그러나 그 외에 controller가 없는 경우에는 각 CSP사에서 제공하는 controller들을 설치하여 사용해야 한다. Out-of-Tree controller라고 한다. 
AWS Load Balancer Controller의 경우는 Out-of-Tree controller로 'AWS Load Balancer Controller'를 별도로 설치하지 않는 경우 아무리 annotations를 명시하여 Ingress를 배포해도 실제 AWS ALB는 provisioning 되지 않는다. 
따라서 ALB를 Ingress로 사용하기 위해서는 아래의 컨트롤러 설치가 필수이다. 

## - eksctl 설치  
`Install eksctl`
```bash
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin
eksctl version
```
<br>

## Install - AWS Load Balancer Controller

`ALB Ingress IAM Policy`  
ServiceAccount의 Role에서 사용할 Policy를 지정  
aws-load-balancer-controller 버전 2.4.4이므로 배포 버전확인 필요  
```bash
curl -o iam_policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.4.4/docs/install/iam_policy.json

aws iam create-policy \
  --policy-name AWSLoadBalancerControllerIAMPolicy \
  --policy-document file://iam_policy.json
```

`Asoociate IAM OIDC to Cluster`  
EKS OIDC 자격 증명 공급  
```bash
eksctl utils associate-iam-oidc-provider --region=ap-northeast-2 --cluster=ClusterName --approve
```
<br>

`Create an IAM role and annotate the kubernetes service account named aws-load-balancer-controller in the kube-system namespace for the AWS Load Balancer Controller`  
아래 내용을 배포하면 AWS IAM Role이 하나 생성 되면서 aws-load-balancer-controller ServiceAccount에 해당 IAM Role을 맵핑시켜준다.  
```bash
export ACCOUNT_ID=`aws sts get-caller-identity | jq -r .Account`
eksctl create iamserviceaccount \
--cluster=ClusterName \
--namespace=kube-system \
--name=aws-load-balancer-controller \
--attach-policy-arn=arn:aws:iam::$ACCOUNT_ID:policy/AWSLoadBalancerControllerIAMPolicy \
--override-existing-serviceaccounts \
--approve
```

eksctl create iamserviceaccount \
  --cluster=my-cluster \
  --namespace=kube-system \
  --name=aws-load-balancer-controller \
  --role-name "AmazonEKSLoadBalancerControllerRole" \
  --attach-policy-arn=arn:aws:iam::111122223333:policy/AWSLoadBalancerControllerIAMPolicy \
  --approve
  
<br>


`Install the TargetGroupBinding custom resource definitions`  
사용자 리소스 정의 배포  
```bash
kubectl apply -k "github.com/aws/eks-charts/stable/aws-load-balancer-controller/crds?ref=master"

```
<br>

`Install the AWS Load Balancer Controller`  
Helm Repo를 추가시켜주고 Helm 배포를 진행한다.  
폐쇄망에서는 내부동작중 Waf URL을 호출하지 않도록 Waf관련 enable을 모두 false로 변경한다.  
```bash
helm repo add eks https://aws.github.io/eks-charts

# Private Image
helm upgrade -i aws-load-balancer-controller eks/aws-load-balancer-controller \
--set clusterName=ClusterName \
--set serviceAccount.create=false \
--set serviceAccount.name=aws-load-balancer-controller \
--set image.repository=222383050459.dkr.ecr.ap-northeast-2.amazonaws.com/opensource-components \
--set image.tag=aws-load-balancer-controller-v2.4.4 \
--set enableWaf=false \
--set enableWafv2=false \
--set enableShield=false \
-n kube-system

# Public Image
helm upgrade -i aws-load-balancer-controller eks/aws-load-balancer-controller \
--set clusterName=ClusterName \
--set serviceAccount.create=false \
--set serviceAccount.name=aws-load-balancer-controller \
--set image.repository=602401143452.dkr.ecr.ap-northeast-2.amazonaws.com/amazon/aws-load-balancer-controller \
--set enableWaf=false \
--set enableWafv2=false \
--set enableShield=false \
-n kube-system
```
<br>


<br><br><br>

> Ref: https://docs.aws.amazon.com/eks/latest/userguide/aws-load-balancer-controller.html
> Ref: https://kubernetes-sigs.github.io/aws-load-balancer-controller/v2.2/guide/ingress/annotations/