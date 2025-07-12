---
layout: post
title: "Kubecost를 통한 EKS 비용계산"
author: "Bys"
category: cloud
date: 2023-04-05 01:00:00
tags: eks kubecost monitoring
---


# Kubecost

Kubecost는 Kubernetes를 사용하는데 있어 실시간 비용과 인사이트를 제공한다고 소개한다. Amazon EKS에서는 Kubecost 번들을 제공하여 EKS의 비용 가시성을 보여준다. 

## [Kubecost Install](https://docs.aws.amazon.com/eks/latest/userguide/cost-monitoring.html)


### [Helm Chart](https://github.com/kubecost/cost-analyzer-helm-chart/tree/develop/cost-analyzer)

Values 파일을 내려 받는다. 
```bash
wget https://raw.githubusercontent.com/kubecost/cost-analyzer-helm-chart/develop/cost-analyzer/values-eks-cost-monitoring.yaml
```

EKS 클러스터에 Prometheus 및 Grafana가 이미 설치되어 있는 경우 Kubecost를 통해 추가 설치가 되지 않도록 설정을 변경할 필요가 있다. 또한 여기서는 ALB Ingress를 사용할 예정으로 아래와 같이 Values파일을 수정한다. 
`values-eks-cost-monitoring.yaml`  
```yaml
# global.prometheus.enabled: false
# global.prometheus.fqdn: your-prometheus-endpoint
global:
  prometheus:
    enabled: false
    fqdn: http://prometheus-server.prometheus.svc
  notifications:
    alertmanager:
      fqdn: http://prometheus-alertmanager.prometheus.svc:9093
  grafana:
    enabled: false
    proxy: false

# Don't schedule in fargate nodes.
affinity:
  nodeAffinity:
    requiredDuringSchedulingIgnoredDuringExecution:
      nodeSelectorTerms:
      - matchExpressions:
        - key: eks.amazonaws.com/compute-type
          operator: NotIn
          values:
          - fargate

# Ingress
ingress:
  enabled: true
  className: alb
  annotations:
    alb.ingress.kubernetes.io/load-balancer-name: bys-dev-k8s-alb-etc
    alb.ingress.kubernetes.io/group.name: eks-main-etc
    alb.ingress.kubernetes.io/subnets: bys-dev-sbn-az1-extelb, bys-dev-sbn-az2-extelb
    alb.ingress.kubernetes.io/scheme : internet-facing
    alb.ingress.kubernetes.io/security-groups: bys-dev-sg-alb-eks-main-etc
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS": 443}]'
    alb.ingress.kubernetes.io/actions.redirect-to-443: '{"Type": "redirect", "RedirectConfig": {"Protocol": "HTTPS", "Port": "443", "StatusCode": "HTTP_301"}}'
    alb.ingress.kubernetes.io/ssl-policy: ELBSecurityPolicy-TLS13-1-2-2021-06
    alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:ap-northeast-2:xxxxx:certificate/yyyyy
    alb.ingress.kubernetes.io/healthcheck-path: /
    alb.ingress.kubernetes.io/healthcheck-interval-seconds: '15'
    alb.ingress.kubernetes.io/healthcheck-timeout-seconds: '10'
    alb.ingress.kubernetes.io/healthy-threshold-count: '2'
    alb.ingress.kubernetes.io/unhealthy-threshold-count: '4'
    alb.ingress.kubernetes.io/healthcheck-port: traffic-port
    alb.ingress.kubernetes.io/success-codes: 200,301,302
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/tags: auto-delete=no
  paths: ["/"] # There's no need to route specifically to the pods-- we have an nginx deployed that handles routing
  pathType: "Prefix"
  hosts:
    - kubecost-main.bys.world
  tls: []
  #  - secretName: cert
  #    hosts:
  #      - kubecost-main.bys.world
```

```bash
helm upgrade -i kubecost oci://public.ecr.aws/kubecost/cost-analyzer --version 1.102.0 \
    --namespace kubecost --create-namespace \
    -f values-eks-cost-monitoring.yaml

helm upgrade -i kubecost kubecost/cost-analyzer -n kubecost -f /Users/bys/workspace/kubernetes/kubecost/bys-dev-eks-main/values-eks-cost-monitoring-new.yaml
```

아래와 같이 kubecost-cost-analyzer가 정상 배포되었는지 확인한다. 
```bash
# kubectl get po -n kubecost
NAME                                      READY   STATUS    RESTARTS   AGE
kubecost-cost-analyzer-5d894c567f-7hd78   2/2     Running   0          3h31m

k get svc -n kubecost
NAME                     TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)             AGE
kubecost-cost-analyzer   ClusterIP   172.20.222.215   <none>        9003/TCP,9090/TCP   3h32m

k get ing -n kubecost
NAME                     CLASS   HOSTS                     ADDRESS                                                           PORTS   AGE
kubecost-cost-analyzer   alb     kubecost-main.bys.world   xxxxx.ap-northeast-2.elb.amazonaws.com                            80      3h32m
```

<br><br>
## 2. Kubecost를 통한 모니터링



---

## 📚 References

[1] **AWS EKS Kubecost**  
- https://docs.aws.amazon.com/eks/latest/userguide/cost-monitoring.html

[2] **Kubecost Helm Chart** - GitHub 저장소  
- https://github.com/kubecost/cost-analyzer-helm-chart/tree/develop/cost-analyzer
