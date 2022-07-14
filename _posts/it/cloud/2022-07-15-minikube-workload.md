---
layout: post
title: "Kubernetes 워크로드"
author: "Bys"
category: cloud
date: 2022-07-07 01:00:00
tags: kubernetes minikube workload
---

# Minikube
이번 장에서는 Deployment, Service, Ingress 등의 워크로드에 대해서 알아본다.  

## 1. Deployment
Kubernetes 에서 Deployment의 워크로드에 대해서 알아본다. 

### 1.1 Nginx 배포  
테스트를 위해 간단히 Nginx를 배포 해본다.  
```bash
vim deployment-nginx.yml
kubectl apply -f deployment-nginx.yml
```

`deployment-nginx.yml`  
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
  namespace: dev
spec:
  selector:
    matchLabels:
      app: nginx
  replicas: 2 # tells deployment to run 4 pods matching the template
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: nginx:latest
        ports:
        - containerPort: 80
```

배포를 진행하고 상태를 확인하면 아래와 같이 2개의 파드가 실행 중인 것을 확인할 수 있다.  
```bash
kubectl get pods -o wide -n dev
##Print
NAME                                READY   STATUS    RESTARTS   AGE   IP           NODE       NOMINATED NODE   READINESS GATES
nginx-deployment-544dc8b7c4-j8h4r   1/1     Running   0          20m   172.17.0.4   minikube   <none>           <none>
nginx-deployment-544dc8b7c4-lpgbs   1/1     Running   0          20m   172.17.0.3   minikube   <none>           <none>
```


## 2. Service


<br><br><br>

> Ref: 