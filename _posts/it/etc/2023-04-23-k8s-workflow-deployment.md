---
layout: post
title: "Kubernetes Deployment생성 Workflow"
author: "Bys"
category: etc
date: 2023-04-24 01:00:00
tags: kubernetes deployment workflow
---

# [Deployment workflow](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/)  

EKS를 통해 deployment 생성 시 로그를 확인하며 Kubernetes에서 실제 리소스 생성이 되는 과정을 살펴본다.  

[![k8s-sequence-deployment](/assets/it/etc/k8s/k8s-sequence-deployment.png){: width="80%" height="auto"}](/assets/it/etc/k8s/k8s-sequence-deployment.png)  

1. Client는 kubectl커맨드를 통해 kube-apiserver로 생성 요청 
```bash
fields @timestamp, requestURI, verb, requestObject.kind, objectRef.name, user.username, userAgent
| filter @logStream like /^kube-apiserver-audit/
| filter objectRef.resource == "deployments"
| filter verb == "create"
| sort @timestamp desc

# Results
requestURI: /apis/apps/v1/namespaces/default/deployments?fieldManager=kubectl-client-side-apply&fieldValidation=Strict
verb: create
requestObject.kind: Deployment
objectRef.resource: deployments
objectRef.name: nginx
user.username: kubernetes-admin
userAgent: kubectl/v1.26.1 (darwin/arm64) kubernetes/8f94681
```

2. kube-controller(deployment-controller)는 kube-apiserver를 watch하고 있다가 deployment 생성을 감지하고 replicaSet을 생성한다. 
```bash
fields @timestamp, requestURI, verb, requestObject.kind, objectRef.name, user.username, userAgent
| filter @logStream like /^kube-apiserver-audit/
| filter objectRef.resource == "replicasets"
| filter verb == "create"
| sort @timestamp desc


# Results
requestURI: /apis/apps/v1/namespaces/default/deployments?fieldManager=kubectl-client-side-apply&fieldValidation=Strict
verb: create
requestObject.kind: ReplicaSet
objectRef.resource: replicasets
objectRef.name: nginx-5cfbf748d
user.username: system:serviceaccount:kube-system:deployment-controller
userAgent: kube-controller-manager/v1.25.8 (linux/amd64) kubernetes/83fe90d/system:serviceaccount:kube-system:deployment-controller
```

3. kube-controller(replicaset-controller)는 kube-apiserver를 watch하고 있다가 replicaset 생성을 감지하고 pod를 생성한다. 
```bash
fields @timestamp, requestURI, verb, requestObject.kind, objectRef.name, user.username, userAgent
| filter @logStream like /^kube-apiserver-audit/
| filter objectRef.resource == "pods"
| filter verb == "create"
| sort @timestamp desc

# Results
requestURI: /api/v1/namespaces/default/pods
verb: create
requestObject.kind: Pod
objectRef.resource: pods
responseObject.metadata.name: nginx-5cfbf748d-wwnzv
user.username: system:serviceaccount:kube-system:replicaset-controller
userAgent: kube-controller-manager/v1.25.8 (linux/amd64) kubernetes/83fe90d/system:serviceaccount:kube-system:replicaset-controller
```

4. Scheduler는 kube-apiserver를 watch하고 있다가 unassigned pod생성을 감지하고 pod를 binding 한다. 
```bash
fields @timestamp, requestURI, verb, requestObject.kind, objectRef.name, user.username, userAgent
| filter @logStream like /^kube-apiserver-audit/
| filter objectRef.resource == "pods"
| filter verb == "create"
| filter user.username == "system:kube-scheduler"
| sort @timestamp desc

# Results
requestURI: /api/v1/namespaces/default/pods/nginx-5cfbf748d-wwnzv/binding
verb: create
requestObject.kind: Binding
objectRef.resource: pods
objectRef.name: nginx-5cfbf748d-wwnzv
user.username: system:kube-scheduler
userAgent: kube-scheduler/v1.25.8 (linux/amd64) kubernetes/83fe90d/scheduler
```

5. kubelet은 kube-apiserver를 watch하고 있다가 자신의 노드로 binding된 pod를 감지하고 docker/containerd runtime을 통해 pod를 실행한다.  

<br><br><br>

- References  
[1] 