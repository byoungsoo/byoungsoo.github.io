---
layout: post
title: "AWS EKS Ingress ALB"
author: "Bys"
category: cloud
date: 2021-04-26 01:00:00
tags: aws eks ingress alb
---

#### EKS Ingress ALB

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
```bash
helm upgrade -i aws-load-balancer-controller eks/aws-load-balancer-controller \
--set clusterName=ClusterName \
--set serviceAccount.create=false \
--set serviceAccount.name=aws-load-balancer-controller \
-n kube-system
```
<br>
