---
layout: post
title: "AWS EKS Ingress ALB"
author: "Bys"
category: cloud
date: 2021-04-26 01:00:00
tags: aws eks ingress alb
---

#### Kubernetes Nginx-Ingress 설정

`Install Helm`  
```bash
wget https://get.helm.sh/helm-v3.1.2-linux-amd64.tar.gz
tar -xvf helm*.gz
mv helm /usr/bin
# Check version
helm version
```
<br>

`Create namespace`
```bash
kubectl create ns ingress-nginx
```
<br>

`Helm Repository Add`  
```bash
helm init
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
```
<br>

`Helm Install`
```bash
#install external_dns
helm install external-dns bitnami/external-dns --namespace ingress-nginx -f external_dns_values.yml --debug 
#install ingress nginx
helm install nginx-internal ingress-nginx/ingress-nginx --namespace ingress-nginx -f ingress_controller_values.yml --debug
```
<br>

`external_dns_values.yml`  
```yaml
sources:
  - service
  - ingress
provider: aws

txtOwnerId: ""

policy: sync
logLevel: info

rbac:
  create: false
  apiVersion: v1
  serviceAccountName: externaldns
  
podAnnotations:
  iam.amazonaws.com/role: TF-EKS-EXTERNAL-DNS-ROLE


domainFilters:
  - inner.ing.cluster.local
```
<br>

`ingress_controller_values.yml`  
```yaml
controller:
  name: ccccontroller
  
  ingressClass: nginx-internal
  
  publishService:
    enabled: true
    
  kind: Deployment
  
  resources:
    request:
      cpu: 50m
      memory: 100Mi
  
  replicaCount: 2
  config:
    proxy-body-size: 100m
    use-forwarded-headers: "true"
    
  affinity:
    podAntiAffinity:
      preferredDuringSchedulingIgnoredDuringExecution:
      - weight: 100
        podAffinityTerm:
          labelSelector:
            matchExpressions:
              - key: "app"
                operator: In
                values:
                  - ingress-nginx
          topologyKey: "kubernetes.io/hostname"
  
  service:
    annotations:
      service.beta.kubernetes.io/aws-load-balancer-type: "nlb"
      service.beta.kubernetes.io/aws-load-balancer-internal: "true"
      service.beta.kubernetes.io/aws-load-balancer-cross-zone-load-balancing-enabled: "true"
    targetPorts:
      http: 80
    externalTrafficPolicy: "Local"

defaultBackend:
  resources:
    requests:
      cpu: 10m
      memory: 20Mi

rbac:
  create: true
  serviceAccountName: default
```
<br>

`Delete All Resources`  
```bash
kubectl delete ns ingress-nginx
kubectl delete clusterrolebinding nginx-internal-ingress-nginx
kubectl delete clusterrolebinding external-dns
kubectl delete clusterrole nginx-internal-ingress-nginx
kubectl delete clusterrole external-dns
kubectl delete validatingwebhookconfiguration nginx-internal-ingress-nginx-admission

```
