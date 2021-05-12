---
layout: post
title: "AWS EKS Ingress ALB"
author: "Bys"
category: cloud
date: 2021-04-26 01:00:00
tags: aws eks ingress alb
---

#### EKS Ingress ALB

`ALB Ingress IAM Policy`  
```bash
```

`Asoociate IAM OIDC to Cluster`  
```bash
ekstcl utils associate-iam-oidc-provider --region=ap-northeast-2 --cluster=ClusterName --approve
```
<br>

`Create an IAM role and annotate the kubernetes service account named aws-load-balancer-controller in the kube-system namespace for the AWS Load Balancer Controller`
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
```bash
kubectl apply -k "github.com/aws/eks-charts/stable/aws-load-balancer-controller//crds?ref=master"
```
<br>

`Install the AWS Load Balancer Controller`  
다만 해당 사이트에서 https://github.com/aws/eks-charts/tree/master/stable/aws-load-balancer-controller  
values.yaml 파일을 보면 아래와 같은 내용이 있다.  
> This is required if using a custom CNI where the managed control plane nodes are unable to initiate network connections to the pods, for example using Calico CNI plugin on EKS. This is not required or recommended if using the Amazon VPC CNI plugin.
hostNetwork: false

Secondary IP를 사용하면서 VPC CNI Plugin을 사용하는 경우 POD 내부에서 외부로 DNS Lookup이 안되는 현상이 있었고, 이 문제를 해결하기 위해서는 hostNetwork 값을 true로 변경해준다.  

```bash
helm repo add eks https://aws.github.io/eks-charts
helm upgrade -i aws-load-balancer-controller eks/aws-load-balancer-controller \
--set clusterName=ClusterName \
--set serviceAccount.create=false \
--set serviceAccount.name=aws-load-balancer-controller \
--set hostNetwork=true \
-n kube-system
```
<br>
