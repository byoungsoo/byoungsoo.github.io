---
layout: post
title: "AWS load balancer controller migration(v1->v2)"
author: "Bys"
category: container
date: 2022-12-19 01:00:00
tags: aws eks controller migration
---

## AWS Load Balancer Controller Migration (v1.1.9 -> v2.4.4)
EKS v1.21에서 진행하였으며 @As-Is AWS Load Balancer Controller는 v1.1.9, @To-Be AWS Load Balancer Controller v2.4.4로 진행한다.  
해당 내용은 개인적인 환경에서 테스트 한 것으로 반드시 각 환경에서 재 테스트가 필요하다.  

### 1. 알아두면 좋을 것들  
- AWS load balancer controller는 resource생성 시 'elbv2.k8s.aws/cluster: ${clusterName}' Tagging한다. 아래는 aws load balancer controller v2의 IAM Policy 중 일부다. 'elbv2.k8s.aws/cluster'키 값의 유/무를 통해 통해 자신이 생성한 Resource에 대해서만 권한을 가지도록 설정하는 부분들이 존재한다.  따라서, 기존 리소스에 수작업으로 <elbv2.k8s.aws/cluster: cluster-name> 태그를 추가하는 과정이 필요하다.  
```json
{
    "Effect": "Allow",
    "Action": [
        "elasticloadbalancing:ModifyLoadBalancerAttributes",
        "elasticloadbalancing:SetIpAddressType",
        "elasticloadbalancing:SetSecurityGroups",
        "elasticloadbalancing:SetSubnets",
        "elasticloadbalancing:DeleteLoadBalancer",
        "elasticloadbalancing:ModifyTargetGroup",
        "elasticloadbalancing:ModifyTargetGroupAttributes",
        "elasticloadbalancing:DeleteTargetGroup"
    ],
    "Resource": "*",
    "Condition": {
        "Null": {
            "aws:ResourceTag/elbv2.k8s.aws/cluster": "false"
        }
    }
}
```

- Controller를 변경하는 중 기존 서비스중인 ALB는 자체로 영향을 받지 않는다. 다만 migration중 ALB를 제어하지 못하는 시간이 생긴다.  
- (중요) Controller를 변경하고 나서 v2에서 지원하는 annotation 'alb.ingress.kubernetes.io/group.name'을 추가하면 신규 ALB가 생성되게 되며 이에 따라 DNS주소가 변경된다.  
- 'kubernetes.io/ingress.class: "alb"' 제거 및 spec에 'ingressClassName: alb'를 추가한다.  


### 2. aws load balancer controller v1 설치
[Setup](https://kubernetes-sigs.github.io/aws-load-balancer-controller/v1.1/guide/controller/setup/)

### 3. aws load balancer controller v1을 통한 리소스 생성 
```yaml
apiVersion: apps/v1 # for versions before 1.9.0 use apps/v1beta2
kind: Deployment
metadata:
  name: nginx
  namespace: test
spec:
  selector:
    matchLabels:
      app: nginx
  replicas: 2 # tells deployment to run 1 pods matching the template
  template: # create pods using pod definition in this template
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: nginx
        ports:
        - containerPort: 80
```
```yaml
apiVersion: v1
kind: Service
metadata:
  name: nginx
  namespace: test
  labels:
    app: nginx
spec:
  selector:
    app: nginx
  ports:
  - name: http
    port: 80
    protocol: TCP
    targetPort: 80
```
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: nginx
  namespace: test
  labels:
    app: nginx
  annotations:
    kubernetes.io/ingress.class: "alb"
    alb.ingress.kubernetes.io/subnets: bys-dev-sbn-az1-extelb, bys-dev-sbn-az2-extelb
    alb.ingress.kubernetes.io/scheme : internet-facing
    alb.ingress.kubernetes.io/security-groups: test
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 81}]'
    alb.ingress.kubernetes.io/healthcheck-path: /
    alb.ingress.kubernetes.io/healthcheck-interval-seconds: '15'
    alb.ingress.kubernetes.io/healthcheck-timeout-seconds: '10'
    alb.ingress.kubernetes.io/healthy-threshold-count: '2'
    alb.ingress.kubernetes.io/unhealthy-threshold-count: '4'
    alb.ingress.kubernetes.io/healthcheck-port: traffic-port
    alb.ingress.kubernetes.io/success-codes: 200,302
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/tags: migration=test
spec:
  rules:
  - http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: nginx
            port:
              number: 80
```

### 4. aws load balancer controller v1.1.9 삭제 및 v2.4.4 생성 
생성을 한 후 v2.4.4의 로그를 보면 아래와 같이 기존 Ingress 리소스에 권한이 없어 작업을 하지 못하는 것을 확인할 수 있다. 이는 IAM Policy에 Null Condition이 들어가 있기 때문이다.  
```bash
$ kubectl logs -f -l app.kubernetes.io/instance=aws-load-balancer-controller -n kube-system

{"level":"error","ts":1671438093.1735396,"logger":"controller.ingress","msg":"Reconciler error","name":"nginx","namespace":"test","error":"AccessDenied: User: arn:aws:sts::111122223333:assumed-role/AmazonEKSLoadBalancerControllerRole/1671437923207273240 is not authorized to perform: elasticloadbalancing:AddTags on resource: arn:aws:elasticloadbalancing:ap-northeast-2:111122223333:targetgroup/b01868f7-5a04f165222a86523e6/ae1c4856facad74d because no identity-based policy allows the elasticloadbalancing:AddTags action\n\tstatus code: 403, request id: 0536d0c4-5c18-4ffa-9135-3ab811602897"} 
```

### 5. aws load balancer controller v1.1.9 Controller를 통해 생성된 Resource에 'elbv2.k8s.aws/cluster' 태그를 추가한다.  
aws load balancer controller v1.1.9 Controller를 통해 생성된 Resource인 ALB/TargetGroup/Security Group에 수작업으로 `elbv2.k8s.aws/cluster: cluster-name` 태그를 추가한다.  

생성을 한 후 v2.4.4의 로그를 보면 태깅이 추가 됨과 동시에 자동으로 기존 targetgroup을 연결하는 targetgroupbinding 리소스를 생성하는 것을 확인할 수 있다.  
```bash
$ kubectl logs -f -l app.kubernetes.io/instance=aws-load-balancer-controller -n kube-system

{"level":"info","ts":1671438585.975024,"logger":"controllers.ingress","msg":"added resource tags","arn":"arn:aws:elasticloadbalancing:ap-northeast-2:111122223333:listener/app/b01868f7-test-nginx-cf8a/d2d154e4d7ab5662/f8026de8309a10e2"}
{"level":"info","ts":1671438586.0462134,"logger":"controllers.ingress","msg":"adding resource tags","arn":"arn:aws:elasticloadbalancing:ap-northeast-2:111122223333:listener-rule/app/b01868f7-test-nginx-cf8a/d2d154e4d7ab5662/f8026de8309a10e2/997e6b5cbd38914d","change":{"auto-delete":"no","elbv2.k8s.aws/cluster":"eks-test","ingress.k8s.aws/resource":"81:1","ingress.k8s.aws/stack":"test/nginx","migration":"test3"}}
{"level":"info","ts":1671438586.1028056,"logger":"controllers.ingress","msg":"added resource tags","arn":"arn:aws:elasticloadbalancing:ap-northeast-2:111122223333:listener-rule/app/b01868f7-test-nginx-cf8a/d2d154e4d7ab5662/f8026de8309a10e2/997e6b5cbd38914d"}
{"level":"info","ts":1671438586.1034424,"logger":"controllers.ingress","msg":"modifying listener rule","stackID":"test/nginx","resourceID":"81:1","arn":"arn:aws:elasticloadbalancing:ap-northeast-2:111122223333:listener-rule/app/b01868f7-test-nginx-cf8a/d2d154e4d7ab5662/f8026de8309a10e2/997e6b5cbd38914d"}
{"level":"info","ts":1671438586.1395128,"logger":"controllers.ingress","msg":"modified listener rule","stackID":"test/nginx","resourceID":"81:1","arn":"arn:aws:elasticloadbalancing:ap-northeast-2:111122223333:listener-rule/app/b01868f7-test-nginx-cf8a/d2d154e4d7ab5662/f8026de8309a10e2/997e6b5cbd38914d"}
{"level":"info","ts":1671438586.1396384,"logger":"controllers.ingress","msg":"creating targetGroupBinding","stackID":"test/nginx","resourceID":"test/nginx-nginx:80"}
{"level":"info","ts":1671438586.2250423,"logger":"controllers.ingress","msg":"created targetGroupBinding","stackID":"test/nginx","resourceID":"test/nginx-nginx:80","targetGroupBinding":{"namespace":"test","name":"k8s-test-nginx-4fa33f903b"}}
{"level":"info","ts":1671438586.22507,"logger":"controllers.ingress","msg":"successfully deployed model","ingressGroup":"test/nginx"}
```

### 6. 기존 ALB Ingress manifest 수정시 정상동작 확인. 
`alb.ingress.kubernetes.io/success-codes`, `alb.ingress.kubernetes.io/tags` 등의 어노테이션 값 수정 및 `kubernetes.io/ingress.class: "alb"`제거 `spec: ingressClassName: alb` 추가 후 정상 반영을 확인 해보면 아래와 같이 정상 로그를 볼 수 있다.  

```bash
$ kubectl logs -f -l app.kubernetes.io/instance=aws-load-balancer-controller -n kube-system

{"level":"info","ts":1671439425.7947881,"logger":"controllers.ingress","msg":"adding resource tags","resourceID":"sg-xxxxxxx","change":{"migration":"success-2"}}
{"level":"info","ts":1671439425.8844564,"logger":"controllers.ingress","msg":"added resource tags","resourceID":"sg-xxxxxxx"}
{"level":"info","ts":1671439426.0646636,"logger":"controllers.ingress","msg":"adding resource tags","arn":"arn:aws:elasticloadbalancing:ap-northeast-2:111122223333:targetgroup/b01868f7-9c0326b1277d3d9d1ae/b76047357541cfa4","change":{"migration":"success-2"}}
{"level":"info","ts":1671439426.1150038,"logger":"controllers.ingress","msg":"added resource tags","arn":"arn:aws:elasticloadbalancing:ap-northeast-2:111122223333:targetgroup/b01868f7-9c0326b1277d3d9d1ae/b76047357541cfa4"}
{"level":"info","ts":1671439426.257319,"logger":"controllers.ingress","msg":"adding resource tags","arn":"arn:aws:elasticloadbalancing:ap-northeast-2:111122223333:loadbalancer/app/b01868f7-test-nginxv2-9c2b/319b31b75ea12a52","change":{"migration":"success-2"}}
{"level":"info","ts":1671439426.3024468,"logger":"controllers.ingress","msg":"added resource tags","arn":"arn:aws:elasticloadbalancing:ap-northeast-2:111122223333:loadbalancer/app/b01868f7-test-nginxv2-9c2b/319b31b75ea12a52"}
{"level":"info","ts":1671439426.4008522,"logger":"controllers.ingress","msg":"adding resource tags","arn":"arn:aws:elasticloadbalancing:ap-northeast-2:111122223333:listener/app/b01868f7-test-nginxv2-9c2b/319b31b75ea12a52/1402387aa3a29373","change":{"migration":"success-2"}}
{"level":"info","ts":1671439426.454699,"logger":"controllers.ingress","msg":"added resource tags","arn":"arn:aws:elasticloadbalancing:ap-northeast-2:111122223333:listener/app/b01868f7-test-nginxv2-9c2b/319b31b75ea12a52/1402387aa3a29373"}
{"level":"info","ts":1671439426.5617793,"logger":"controllers.ingress","msg":"adding resource tags","arn":"arn:aws:elasticloadbalancing:ap-northeast-2:111122223333:listener-rule/app/b01868f7-test-nginxv2-9c2b/319b31b75ea12a52/1402387aa3a29373/6929f026eec9a6c9","change":{"migration":"success-2"}}
{"level":"info","ts":1671439426.612608,"logger":"controllers.ingress","msg":"added resource tags","arn":"arn:aws:elasticloadbalancing:ap-northeast-2:111122223333:listener-rule/app/b01868f7-test-nginxv2-9c2b/319b31b75ea12a52/1402387aa3a29373/6929f026eec9a6c9"}
{"level":"info","ts":1671439426.61331,"logger":"controllers.ingress","msg":"successfully deployed model","ingressGroup":"test/nginxv2"}
```



<br><br><br>
> Ref: https://kubernetes-sigs.github.io/aws-load-balancer-controller/v2.4/deploy/upgrade/migrate_v1_v2/  
> Ref: https://kubernetes-sigs.github.io/aws-load-balancer-controller/v2.4/guide/ingress/annotations  
> Ref: https://kubernetes-sigs.github.io/aws-load-balancer-controller/v2.4/guide/ingress/ingress_class  