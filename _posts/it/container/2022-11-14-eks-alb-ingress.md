---
layout: post
title: "EKS ALB Ingress 살펴보기"
author: "Bys"
category: container
date: 2022-11-14 01:00:00
tags: kubernetes eks alb ingress
---

# AWS

## AWS Load Balancer Controller Annotation 특징 살펴보기
아래는 ALB ingress를 배포하면서 ALB ingress의 annotations 특징을 살펴보려고 한다. 
```yaml
apiVersion: apps/v1 # for versions before 1.9.0 use apps/v1beta2
kind: Deployment
metadata:
  name: nginx-deploy
  namespace: test
spec:
  selector:
    matchLabels:
      app: nginx-test
  replicas: 2 # tells deployment to run 1 pods matching the template
  template: # create pods using pod definition in this template
    metadata:
      labels:
        app: nginx-test
    spec:
      containers:
      - name: nginx-test
        image: nginx
        ports:
        - containerPort: 80
---
apiVersion: v1
kind: Service
metadata:
  name: nginx-svc
  namespace: test
  labels:
    app: nginx-test
spec:
  selector:
    app: nginx-test
  ports:
  - name: http
    port: 80
    protocol: TCP
    targetPort: 80
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: nginx-ingress
  namespace: test
  labels:
    app: nginx-test
  annotations:
    alb.ingress.kubernetes.io/group.name: nginx-group
    alb.ingress.kubernetes.io/subnets: bys-dev-sbn-az1-extelb, bys-dev-sbn-az2-extelb
    alb.ingress.kubernetes.io/scheme : internet-facing
    alb.ingress.kubernetes.io/security-groups: bys-dev-sg-alb-nginx
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 81}]'
    alb.ingress.kubernetes.io/healthcheck-path: /
    alb.ingress.kubernetes.io/healthcheck-interval-seconds: '15'
    alb.ingress.kubernetes.io/healthcheck-timeout-seconds: '10'
    alb.ingress.kubernetes.io/healthy-threshold-count: '2'
    alb.ingress.kubernetes.io/unhealthy-threshold-count: '4'
    alb.ingress.kubernetes.io/healthcheck-port: traffic-port
    alb.ingress.kubernetes.io/success-codes: 200,302
    alb.ingress.kubernetes.io/target-type: ip
spec:
  ingressClassName: alb
  rules:
  - http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: nginx-svc
            port:
              number: 80
```

위 와 같이 배포를 하고 나면 아래와 같이 ingress가 생성된 것을 확인 할 수 있다. 
```bash
$ k get pod,svc,targetgroupbinding,ing -n test

NAME                                READY   STATUS    RESTARTS   AGE
pod/nginx-deploy-7f8f66d685-lglmm   1/1     Running   0          89m
pod/nginx-deploy-7f8f66d685-llg4l   1/1     Running   0          89m

NAME                TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)   AGE
service/nginx-svc   ClusterIP   172.20.86.145   <none>        80/TCP    89m

NAME                           SERVICE-NAME   SERVICE-PORT   TARGET-TYPE   AGE
k8s-test-nginxsvc-c3ca7df12f   nginx-svc      80             ip            66m

NAME                                      CLASS   HOSTS   ADDRESS                                                            PORTS   AGE
ingress.networking.k8s.io/nginx-ingress   alb     *       k8s-v1dev-0ee906a5cb-1817684647.ap-northeast-2.elb.amazonaws.com   80      62m

```

그 중 annotation의 특징을 먼저 살펴보자  
```yaml
annotations:
    # group.name의 특징은 group.name이 같으면 single alb로 설정이 가능하다는 것이다. 
    # 다만 test 결과 동일한 Cluster 내에서만 single alb로 설정이 가능하다. 다른 클러스터에서 group.name을 동일하게 주고 생성해도 id값이 다른 alb가 별도로 생성되게 된다.  
    # ingress1번 배포를 하고 ingress2 배포 시 특정 schme같은 설정을 변경하면 ingress2번 배포시 오류가 발생한다. 
    alb.ingress.kubernetes.io/group.name: nginx-group
```


## TargetGroupBinding 살펴보기
ingress를 배포하고 나면 

```yaml
Name:         k8s-test-nginxsvc-c3ca7df12f
Namespace:    test
Labels:       ingress.k8s.aws/stack=v1-dev
Annotations:  <none>
API Version:  elbv2.k8s.aws/v1beta1
Kind:         TargetGroupBinding
Spec:
  Ip Address Type:  ipv4
  Networking:
    Ingress:
      From:
        Security Group:
          Group ID:  sg-07d020b78fab821d5
      Ports:
        Port:      80
        Protocol:  TCP
  Service Ref:
    Name:            nginx-svc
    Port:            80
  Target Group ARN:  arn:aws:elasticloadbalancing:ap-northeast-2:558846430793:targetgroup/k8s-test-nginxsvc-c3ca7df12f/36e4fe400a255758
  Target Type:       ip
Status:
  Observed Generation:  1
Events:                 <none>
```



<br><br><br>

> Ref: https://kubernetes-sigs.github.io/aws-load-balancer-controller/v2.2/guide/ingress/annotations/