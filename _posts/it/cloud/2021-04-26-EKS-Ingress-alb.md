---
layout: post
title: "AWS EKS Ingress ALB"
author: "Bys"
category: cloud
date: 2021-04-26 01:00:00
tags: aws eks ingress alb
---

#### - eksctl 설치  
`Install eksctl`
```bash
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin
eksctl version
```
<br>

#### EKS Ingress ALB

`ALB Ingress IAM Policy`  
ServiceAccount의 Role에서 사용할 Policy를 지정  
aws-load-balancer-controller 버전 2.2.0이므로 배포 버전확인 필요  
```bash
curl -o iam_policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.2.0/docs/install/iam_policy.json

aws iam create-policy \
  --policy-name AWSLoadBalancerControllerIAMPolicy \
  --policy-document file://iam_policy.json
```

`Asoociate IAM OIDC to Cluster`  
EKS OIDC 자격 증명 공급  
```bash
ekstcl utils associate-iam-oidc-provider --region=ap-northeast-2 --cluster=ClusterName --approve
```
<br>

`Create an IAM role and annotate the kubernetes service account named aws-load-balancer-controller in the kube-system namespace for the AWS Load Balancer Controller`  
아래 내용을 배포하면 AWS IAM Role이 하나 생성 되면서 aws-load-balancer-controller ServiceAccount에 해당 IAM Role을 맵핑시켜준다.  
```bash
export idnumber=`aws sts get-caller-identity | jq -r .Account`
eksctl create iamserviceaccount \
--cluster=ClusterName \
--namespace=kube-system \
--name=aws-load-balancer-controller \
--attach-policy-arn=arn:aws:iam::ACCOUNT_ID:policy/AWSLoadBalancerControllerIAMPolicy \
--override-existing-serviceaccounts \
--approve
```
<br>


`Install the TargetGroupBinding custom resource definitions`  
사용자 리소스 정의 배포  
```bash
kubectl apply -k "github.com/aws/eks-charts/stable/aws-load-balancer-controller//crds?ref=master"
```
<br>

`Install the AWS Load Balancer Controller`  
Helm Repo를 추가시켜주고 Helm 배포를 진행한다.  
폐쇄망에서는 내부동작중 Waf URL을 호출하지 않도록 Waf관련 enable을 모두 false로 변경한다.  
```bash
helm repo add eks https://aws.github.io/eks-charts
helm upgrade -i aws-load-balancer-controller eks/aws-load-balancer-controller \
--set clusterName=ClusterName \
--set serviceAccount.create=false \
--set serviceAccount.name=aws-load-balancer-controller \
--set image.repository=222383050459.dkr.ecr.ap-northeast-2.amazonaws.com/opensource-components \
--set image.tag=aws-load-balancer-controller-v2.2.0 \
--set enableWaf=false \
--set enableWafv2=false \
--set enableShield=false \
-n kube-system
```
<br>
