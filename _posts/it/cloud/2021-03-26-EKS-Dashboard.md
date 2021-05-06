
---
layout: post
title: "AWS EKS Dashboard사용"
author: "Bys"
category: cloud
date: 2021-03-26 01:00:00
tags: aws eks 
---

#### EKS Dashboard  

`Install Kubernetes Metrics Server`
```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
# Check Deploy
kubectl get deployment metrics-server -n kube-system
```
<br>


`Deploy Kubernetes Dashboard`
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.0.5/aio/deploy/recommended.yaml
```

`Create eks-admin // eks-admin-service-account.yaml`  
```bash
kubectl apply -f eks-admin-service-account.yaml
```
<br>

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
