---
layout: post
title: "EKS환경 EFK 구축하기"
author: "Bys"
category: solution
date: 2021-04-28 01:00:00
tags: eks efk fluentd elasticsearch kibana
---

#### FluentD 설정  

`Create Configmap cluster-info`
```bash
kubectl create configmap cluster-info \
--from-literal=cluster.name=MyClusterName
--from-literal=logs.region=ap-northeast-2 -n amazon-cloudwatch
```
<br>

`Create Namespace`
```bash
kubectl apply -f https://raw.githubusercontent.com/aws-samples/amazon-cloudwatch-container-insights/latest/k8s-deployment-manifest-templates/deployment-mode/daemonset/container-insights-monitoring/cloudwatch-namespace.yaml
```
<br>


`Fluent DaemonSet `
```bash
kubectl apply -f https://raw.githubusercontent.com/aws-samples/amazon-cloudwatch-container-insights/latest/k8s-deployment-manifest-templates/deployment-mode/daemonset/container-insights-monitoring/fluentd/fluentd.yaml
```
<br>

이렇게 배포를 하면 amazon-cloudwatch namespace상에 fluentd-cloudwatch-* 형태의 pod가 eks cluster에 배포가 된다.  
배포가 된 Pod의 Container로그는 Cloudwatch Log Groups에서 확인 할 수 있다.  


#### AWS ElasticSearch 설정  
