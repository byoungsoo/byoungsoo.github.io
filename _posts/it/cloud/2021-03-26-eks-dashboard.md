---
layout: post
title: "EKS Dashboard 사용"
author: "Bys"
category: cloud
date: 2021-03-26 01:00:00
tags: aws eks 
---

#### - EKS Dashboard  

`Install Kubernetes Metrics Server`
```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
# Check Deploy
kubectl get deployment metrics-server -n kube-system
```
<br>


`Create eks-admin // eks-admin-service-account.yaml`  
eks-admin 의 sa를 생성하고 해당 sa와 cluster-admin의 Cluster-Role을 바인딩해준다.  
```bash
kubectl apply -f eks-admin-service-account.yaml
```

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: eks-admin
  namespace: kube-system
---
apiVersion: rbac.authorization.k8s.io/v1beta1
kind: ClusterRoleBinding
metadata:
  name: eks-admin
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: eks-admin
  namespace: kube-system
```

<br>

`Deploy Kubernetes Dashboard`
아래와 같이 배포를 하면 kubernetes-dashboard Namespace에 Pod가 생성되며 Dashboard를 이용할 수 있다.  
```bash
helm repo add kubernetes-dashboard https://kubernetes.github.io/dashboard/

kubectl create ns kubernetes-dashboard
helm upgrade --install kubernetes-dashboard kubernetes-dashboard/kubernetes-dashboard --namespace kubernetes-dashboard --version 7.4.0
```
<br>

`EKS Dashboard Token`
EKS Dashboard를 접속하기 위해서는 Token이 필요하다.  
아래 명령을 통해 Token 값을 획득한다.  
```bash
aws eks get-token --cluster-name bys-dev-eks-test | jq -r '.status.token'
```
<br>


`EKS Dashboard Access`
EKS Dashboard를 접속하기 위해서는 Local에서 kubectl proxy 명령을 통해 localhost를 통해 들어갈 수 있다.  
```bash
kubectl proxy &
http://localhost:18001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/#/overview?namespace=_all

kubectl -n kubernetes-dashboard port-forward svc/kubernetes-dashboard-kong-proxy 8443:443 --address 10.20.1.208
https://localhost:8443/#/login
https://3.39.219.95:8443/#/login

```
<br>

만약 Local에서 접속을 할 수 없다면 Tunneling 방식을 통해서 서버를 통해 들어가야 한다.  
서버에서 kubectl proxy 명령을 사용하고 해당 서버로 Tunneling을 한다.  
Putty의 Tunnels에서 SourcePort는 18001 / Destination은 127.0.0.1:8001 이며 내 로컬에서는 localhost:18001로 동일하게 접속한다.  

<br>

