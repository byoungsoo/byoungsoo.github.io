---
layout: post
title: "Minikube 토큰을 통한 인증/인가"
author: "Bys"
category: container
date: 2022-07-15 01:00:00
tags: kubernetes minikube csr crt authentication
---

# Minikube

## 1. Token을 이용한 인증

서비스 어카운트는 파드에서 실행되고 있는 프로세스에 대한 ID를 제공해준다. 
이 의미는 파드 내부의 특정 프로세스들도 Kubernetes API-Server로 접근 할 때 인증에 대한 부분을 Service Account의 토큰을 통해 인증받을 수 있다는 의미이다.  

> A service account provides an identity for processes that run in a Pod.
When you (a human) access the cluster (for example, using kubectl), you are authenticated by the apiserver as a particular User Account (currently this is usually admin, unless your cluster administrator has customized your cluster). Processes in containers inside pods can also contact the apiserver. When they do, they are authenticated as a particular Service Account (for example, default).

자세한 내용은 아래 공식홈페이지를 참고한다.  
[Configure Service Accounts for Pods](https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/)

이번 실습에서는 Secret에서 생성된 토큰을 통해 인증이 정상적으로 잘 되는지를 확인해보려고 한다.  
<br>


### 1.1 Service Account & Secret 생성

우선 아래와 같이 Service Account와 Secret을 생성하여 배포한다.  
`dev-sa.yml`
```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  namespace: dev
  name: dev-sa
```

`dev-secret.yml`
```yaml
apiVersion: v1
kind: Secret
metadata:
  namespace: dev
  name: dev-secret
  annotations:
    kubernetes.io/service-account.name: dev-sa
type: kubernetes.io/service-account-token
```

생성된 dev-secret을 describe 커맨드로 보면 아래와 같이 token 정보가 보인다. 
```bash
kubectl describe secret dev-secret
##Print
Name:         dev-secret
Namespace:    dev
Labels:       <none>
Annotations:  kubernetes.io/service-account.name: dev-sa
              kubernetes.io/service-account.uid: 6af0a60b-41d9-46f4-a466-95c2fa3694a4

Type:  kubernetes.io/service-account-token

Data
====
ca.crt:     1111 bytes
namespace:  3 bytes
token:      eyJhbGciOiJSUzI1NiIsImtpZCI6IjN3cWM2WmtlbWFIQVNtdGY0MGRVckJmUDRFRkdjZFl0T2J3dmV3am5kc2MifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZXYiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlY3JldC5uYW1lIjoiZGV2LXNlY3JldCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJkZXYtc2EiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiI2YWYwYTYwYi00MWQ5LTQ2ZjQtYTQ2Ni05NWMyZmEzNjk0YTQiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6ZGV2OmRldi1zYSJ9.BHE-TNXT01y6v-XTiDuvH6lhUk5yUe1HyiFoyNeF6yP4G8IIUwXeUTwHCP9L55QRvvuRrGH8xrfK7oN38gLgb4NHbTXw9vR95K5e4fTJIFSkxZ6jz1z1ga5E8zPZaVcQ16C0fvSZNm2W9tz1p3cOqXPi60sYedQB_ZcDKZGWTU22T-T8eGROC4veYXHy96A9Qg2sHmdGxQxFoslVm25GNiSvjzj79zC_K84IQ1QX3oECrKu0jJpLWi7wV37djeb8bVoXNGXtRKhboW8rvzegO8V5lskCFaJz_xxrVbo_l_uI4bzcc9ulXMHKyoibN5A2SjyqpKV81_TYNkCo3D7ZRQ
```
<br>

### 1.2 Role & Rolebinding 생성

ServiceAccount에 어떤 부여할 Role을 생성하고 Binding해준다.  
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: dev
  name: dev-reader
rules:
- apiGroups: ["*"] # "" indicates the core API group
  resources: ["*"]
  verbs: ["get", "watch", "list"]
```

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: dev-reader-rolebinding
  namespace: dev
subjects:
- kind: ServiceAccount
  name: dev-sa
  namespace: dev
roleRef:
  kind: Role
  name: dev-reader
  apiGroup: rbac.authorization.k8s.io
```
<br>

### 1.3 Config설정 
위에서 조회된 토큰 값을 가지고 
```bash
kubectl config set-credentials devreaduser --token eyJhbGciOiJSUzI1NiIsImtpZCI6IjN3cWM2WmtlbWFIQVNtdGY0MGRVckJmUDRFRkdjZFl0T2J3dmV3am5kc2MifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZXYiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlY3JldC5uYW1lIjoiZGV2LXNlY3JldCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJkZXYtc2EiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiI2YWYwYTYwYi00MWQ5LTQ2ZjQtYTQ2Ni05NWMyZmEzNjk0YTQiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6ZGV2OmRldi1zYSJ9.BHE-TNXT01y6v-XTiDuvH6lhUk5yUe1HyiFoyNeF6yP4G8IIUwXeUTwHCP9L55QRvvuRrGH8xrfK7oN38gLgb4NHbTXw9vR95K5e4fTJIFSkxZ6jz1z1ga5E8zPZaVcQ16C0fvSZNm2W9tz1p3cOqXPi60sYedQB_ZcDKZGWTU22T-T8eGROC4veYXHy96A9Qg2sHmdGxQxFoslVm25GNiSvjzj79zC_K84IQ1QX3oECrKu0jJpLWi7wV37djeb8bVoXNGXtRKhboW8rvzegO8V5lskCFaJz_xxrVbo_l_uI4bzcc9ulXMHKyoibN5A2SjyqpKV81_TYNkCo3D7ZRQ

kubectl config set-context devreaduser --cluster=minikube --user=devreaduser

kubectl config use-context devreaduser
```
<br>


### 1.4 Check
확인을 해보면 현재 devreaduser는 dev namespace에 있는 리소스만 조회 가능하며 다른 namespace에 대해서는 조회권한이 없는 것을 확인 할 수 있다.  
```bash
kubectl get pods -n dev
##Print
NAME                                READY   STATUS    RESTARTS   AGE
nginx-deployment-544dc8b7c4-hcn8g   1/1     Running   0          2d20h
nginx-deployment-544dc8b7c4-rsfkw   1/1     Running   0          2d20h
[minikube@kyle-docker ~]$ k get all -n dev
NAME                                    READY   STATUS    RESTARTS   AGE
pod/nginx-deployment-544dc8b7c4-hcn8g   1/1     Running   0          2d20h
pod/nginx-deployment-544dc8b7c4-rsfkw   1/1     Running   0          2d20h

NAME                    TYPE       CLUSTER-IP       EXTERNAL-IP   PORT(S)        AGE
service/nginx-service   NodePort   10.100.191.220   <none>        80:30000/TCP   2d20h

NAME                               READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/nginx-deployment   2/2     2            2           2d20h

NAME                                          DESIRED   CURRENT   READY   AGE
replicaset.apps/nginx-deployment-544dc8b7c4   2         2         2       2d20h
```

```bash
kubectl get all -A
##Print
Error from server (Forbidden): pods is forbidden: User "system:serviceaccount:dev:dev-sa" cannot list resource "pods" in API group "" at the cluster scope
Error from server (Forbidden): replicationcontrollers is forbidden: User "system:serviceaccount:dev:dev-sa" cannot list resource "replicationcontrollers" in API group "" at the cluster scope
Error from server (Forbidden): services is forbidden: User "system:serviceaccount:dev:dev-sa" cannot list resource "services" in API group "" at the cluster scope
Error from server (Forbidden): daemonsets.apps is forbidden: User "system:serviceaccount:dev:dev-sa" cannot list resource "daemonsets" in API group "apps" at the cluster scope
Error from server (Forbidden): deployments.apps is forbidden: User "system:serviceaccount:dev:dev-sa" cannot list resource "deployments" in API group "apps" at the cluster scope
Error from server (Forbidden): replicasets.apps is forbidden: User "system:serviceaccount:dev:dev-sa" cannot list resource "replicasets" in API group "apps" at the cluster scope
Error from server (Forbidden): statefulsets.apps is forbidden: User "system:serviceaccount:dev:dev-sa" cannot list resource "statefulsets" in API group "apps" at the cluster scope
Error from server (Forbidden): horizontalpodautoscalers.autoscaling is forbidden: User "system:serviceaccount:dev:dev-sa" cannot list resource "horizontalpodautoscalers" in API group "autoscaling" at the cluster scope
Error from server (Forbidden): cronjobs.batch is forbidden: User "system:serviceaccount:dev:dev-sa" cannot list resource "cronjobs" in API group "batch" at the cluster scope
Error from server (Forbidden): jobs.batch is forbidden: User "system:serviceaccount:dev:dev-sa" cannot list resource "jobs" in API group "batch" at the cluster scope
```

<br><br><br>
> Ref: https://kubernetes.io/docs/tasks/configure-pod-container/configure-service-account/