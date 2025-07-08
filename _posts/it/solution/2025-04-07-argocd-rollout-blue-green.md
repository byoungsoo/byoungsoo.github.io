---
layout: post
title: "Argocd Rollout 과 ALB(AWS Load Balancer Controller)를 통해 Blue/Green 배포 하기"
author: "Bys"
category: solution
date: 2025-04-07 01:00:00
tags: argocd deployment
---

## ArgoCD

#### [ArgoCD Rollout](https://argo-rollouts.readthedocs.io/en/stable/#what-is-argo-rollouts)


`rollout.yaml`  
```yaml
apiVersion: argoproj.io/v1alpha1
kind: Rollout
metadata:
  name: rollout-bluegreen
spec:
  replicas: 2
  revisionHistoryLimit: 2
  selector:
    matchLabels:
      app: rollout-bluegreen
  template:
    metadata:
      labels:
        app: rollout-bluegreen
    spec:
      containers:
      - name: rollouts-demo
        image: argoproj/rollouts-demo:blue
        # image: argoproj/rollouts-demo:green
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
        lifecycle:
          preStop:
            exec:
              command: ["/bin/sh", "-c", "sleep 15"]

  strategy:
    blueGreen: 
      #activeService는 현재 운영중인 Blue 서비스
      activeService: rollout-bluegreen-active
      
      #previewService는 새롭게 배포될 Green 서비스
      previewService: rollout-bluegreen-preview
      
      #autoPromotioEnabled 옵션은 Blue/Green 배포를 자동으로 진행할 것인지 여부. false 옵션을 사용해 수동으로 지정
      autoPromotionEnabled: true

      autoPromotionSeconds: 60
```

`service.yaml`  
```yaml
kind: Service
apiVersion: v1
metadata:
  name: rollout-bluegreen-active
spec:
  type: ClusterIP
  selector:
    app: rollout-bluegreen
  ports:
  - name: http
    protocol: TCP
    port: 8080
    targetPort: 8080
---
kind: Service
apiVersion: v1
metadata:
  name: rollout-bluegreen-preview
spec:
  type: ClusterIP
  selector:
    app: rollout-bluegreen
  ports:
  - name: http
    protocol: TCP
    port: 8080
    targetPort: 8080
```

`ingress.yaml`  
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  namespace: default
  name: rollouts-test-active-ingress
  annotations:
    alb.ingress.kubernetes.io/subnets: bys-dev-sbn-az1-extelb, bys-dev-sbn-az2-extelb, bys-dev-sbn-az3-extelb, bys-dev-sbn-az4-extelb
    alb.ingress.kubernetes.io/scheme : internet-facing
    alb.ingress.kubernetes.io/security-groups: bys-dev-sg-temp-alltraffic
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS": 443}]'
    alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:ap-northeast-2:558846430793:certificate/3d2ce654-c747-4b3e-905b-17304b8962ef
    alb.ingress.kubernetes.io/healthcheck-path: /
    alb.ingress.kubernetes.io/healthcheck-interval-seconds: '15'
    alb.ingress.kubernetes.io/healthcheck-timeout-seconds: '10'
    alb.ingress.kubernetes.io/healthy-threshold-count: '2'
    alb.ingress.kubernetes.io/unhealthy-threshold-count: '4'
    alb.ingress.kubernetes.io/healthcheck-port: traffic-port
    alb.ingress.kubernetes.io/success-codes: 200,302
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/tags: auto-delete=no
spec:
  ingressClassName: alb
  rules:
    - host: "rollouts-test-active.bys.asia"
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: rollout-bluegreen-active
                port:
                  number: 8080
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  namespace: default
  name: rollouts-test-preview-ingress
  annotations:
    alb.ingress.kubernetes.io/subnets: bys-dev-sbn-az1-extelb, bys-dev-sbn-az2-extelb, bys-dev-sbn-az3-extelb, bys-dev-sbn-az4-extelb
    alb.ingress.kubernetes.io/scheme : internet-facing
    alb.ingress.kubernetes.io/security-groups: bys-dev-sg-temp-alltraffic
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS": 443}]'
    alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:ap-northeast-2:558846430793:certificate/3d2ce654-c747-4b3e-905b-17304b8962ef
    alb.ingress.kubernetes.io/healthcheck-path: /
    alb.ingress.kubernetes.io/healthcheck-interval-seconds: '15'
    alb.ingress.kubernetes.io/healthcheck-timeout-seconds: '10'
    alb.ingress.kubernetes.io/healthy-threshold-count: '2'
    alb.ingress.kubernetes.io/unhealthy-threshold-count: '4'
    alb.ingress.kubernetes.io/healthcheck-port: traffic-port
    alb.ingress.kubernetes.io/success-codes: 200,302
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/tags: auto-delete=no
spec:
  ingressClassName: alb
  rules:
    - host: "rollouts-test-preview.bys.asia"
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: rollout-bluegreen-preview
                port:
                  number: 8080
```

<br><br><br>

- References  

