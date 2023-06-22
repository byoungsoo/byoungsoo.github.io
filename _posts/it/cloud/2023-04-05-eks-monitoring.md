---
layout: post
title: "Prometheus와 Grafana를 이용한 EKS 모니터링"
author: "Bys"
category: cloud
date: 2023-04-05 01:00:00
tags: eks prometheus grafana monitoring
---


# Prometheus & Grafana

![prometheus-architecture001](/assets/it/cloud/eks/prometheus-architecture001.png){: width="70%" height="auto"}

Prometheus를 배포하면 prometheus서버는 여러 방식을 통해 메트릭을 수집한다. 이 후 prometheus의 수집 데이터를 grafana는 시각화 하여 편리하게 보여준다. 이를 통해 Kubernetes 전체 상태에 대한 모니터링이 가능하다.

## [Prometheus Install](https://docs.aws.amazon.com/eks/latest/userguide/prometheus.html)

```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm upgrade -i prometheus prometheus-community/prometheus --namespace prometheus -f prometheus-values-alb.yaml

helm uninstall prometheus -n prometheus

# https://github.com/prometheus-community/helm-charts
```

`prometheus-values-alb.yaml`  
```yaml
alertmanager:
  persistence:
    storageClass: ebs-sc-gp3
    size: 20Gi

server:
  persistentVolume:
    storageClass: ebs-sc-gp3
    size: 50Gi
  service:
    type: ClusterIP
    servicePort: 80
  ingress:
    enabled: true
    ingressClassName: alb
    annotations: 
      alb.ingress.kubernetes.io/group.name: eks-monitoring
      alb.ingress.kubernetes.io/subnets: subnet-02e6d788fad8afdcf, subnet-020255d69e8c814da
      alb.ingress.kubernetes.io/scheme : internet-facing
      alb.ingress.kubernetes.io/security-groups: bys-dev-sg-alb-eks-monitoring
      alb.ingress.kubernetes.io/ssl-policy: ELBSecurityPolicy-TLS13-1-2-2021-06
      alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:ap-northeast-2:558846430793:certificate/xxxxx
      alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}, {"HTTPS": 443}]'
      alb.ingress.kubernetes.io/actions.ssl-redirect: '{"Type": "redirect", "RedirectConfig": {"Protocol": "HTTPS", "Port": "443", "StatusCode": "HTTP_301"}}'
      alb.ingress.kubernetes.io/healthcheck-path: /
      alb.ingress.kubernetes.io/healthcheck-interval-seconds: '15'
      alb.ingress.kubernetes.io/healthcheck-timeout-seconds: '10'
      alb.ingress.kubernetes.io/healthy-threshold-count: '2'
      alb.ingress.kubernetes.io/unhealthy-threshold-count: '4'
      alb.ingress.kubernetes.io/healthcheck-port: traffic-port
      alb.ingress.kubernetes.io/success-codes: 200,301,302
      alb.ingress.kubernetes.io/target-type: ip
      alb.ingress.kubernetes.io/tags: auto-delete=no
    hosts:
      - prometheus-main.bys.world
    path: /
    extraPaths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: ssl-redirect
            port:
              name: use-annotation
    tls:
      - hosts:
          - prometheus-main.bys.world
        secretName: cert
  resources:
    limits:
      cpu: 500m
      memory: 512Mi
    requests:
      cpu: 500m
      memory: 512Mi

prometheus-node-exporter:
  affinity:
    nodeAffinity:
      requiredDuringSchedulingIgnoredDuringExecution:
        nodeSelectorTerms:
        - matchExpressions:
          - key: eks.amazonaws.com/compute-type
            operator: NotIn
            values:
            - fargate

```

`prometheus-values-nlb.yaml`  
```yaml
alertmanager:
  persistence:
    storageClass: ebs-sc-gp3
    size: 20Gi

server:
  persistentVolume:
    storageClass: ebs-sc-gp3
    size: 50Gi
  service:
    type: LoadBalancer
    servicePort: 80
    annotations:
      service.beta.kubernetes.io/aws-load-balancer-name: bys-dev-nlb-eks-prometheus
      service.beta.kubernetes.io/aws-load-balancer-type: external
      service.beta.kubernetes.io/aws-load-balancer-nlb-target-type: ip
      service.beta.kubernetes.io/aws-load-balancer-scheme: "internet-facing"
      service.beta.kubernetes.io/aws-load-balancer-attributes: load_balancing.cross_zone.enabled=true
      service.beta.kubernetes.io/aws-load-balancer-additional-resource-tags: auto-delete=no

prometheus-node-exporter:
  resources:
    requests:
      cpu: 100m
      memory: 64Mi
    limits:
      cpu: 100m
      memory: 64Mi
  affinity:
    nodeAffinity:
      requiredDuringSchedulingIgnoredDuringExecution:
        nodeSelectorTerms:
        - matchExpressions:
          - key: eks.amazonaws.com/compute-type
            operator: NotIn
            values:
            - fargate
```

## [Grafana Install](https://github.com/grafana/helm-charts)

```bash
helm repo add grafana https://grafana.github.io/helm-charts
helm repo update
helm upgrade -i grafana grafana/grafana --namespace prometheus -f grafana-values-alb.yaml

# helm uninstall grafana -n prometheus

# https://github.com/grafana/helm-charts
```

`grafana-values-alb.yaml`  
```yaml
persistence:
  type: pvc
  enabled: true
  storageClassName: ebs-sc-gp3
  accessModes:
    - ReadWriteOnce
  size: 100Gi
  # annotations: {}
  finalizers:
    - kubernetes.io/pvc-protection
  # subPath: ""
  # existingClaim:

datasources:
  datasources.yaml:
    apiVersion: 1
    datasources:
    - name: Prometheus
      type: prometheus
      url: http://prometheus-server.prometheus.svc.cluster.local
      access: proxy
      isDefault: true

ingress:
  enabled: true
  ingressClassName: alb
  annotations:
    alb.ingress.kubernetes.io/group.name: eks-monitoring
    alb.ingress.kubernetes.io/subnets: subnet-02e6d788fad8afdcf, subnet-020255d69e8c814da
    alb.ingress.kubernetes.io/scheme : internet-facing
    alb.ingress.kubernetes.io/security-groups: bys-dev-sg-alb-eks-monitoring
    alb.ingress.kubernetes.io/ssl-policy: ELBSecurityPolicy-TLS13-1-2-2021-06
    alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:ap-northeast-2:558846430793:certificate/xxxxx
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}, {"HTTPS": 443}]'
    alb.ingress.kubernetes.io/actions.ssl-redirect: '{"Type": "redirect", "RedirectConfig": {"Protocol": "HTTPS", "Port": "443", "StatusCode": "HTTP_301"}}'
    alb.ingress.kubernetes.io/healthcheck-path: /
    alb.ingress.kubernetes.io/healthcheck-interval-seconds: '15'
    alb.ingress.kubernetes.io/healthcheck-timeout-seconds: '10'
    alb.ingress.kubernetes.io/healthy-threshold-count: '2'
    alb.ingress.kubernetes.io/unhealthy-threshold-count: '4'
    alb.ingress.kubernetes.io/healthcheck-port: traffic-port
    alb.ingress.kubernetes.io/success-codes: 200,301,302
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/tags: auto-delete=no
  hosts:
    - grafana-main.bys.world
  labels: {}
  paths:
    - /
  tls:
    - hosts:
        - grafana-main.bys.world
      secretName: cert

  extraPaths:
    - path: /
      pathType: Prefix
      backend:
        service:
          name: ssl-redirect
          port:
            name: use-annotation
```

`grafana-values-nlb.yaml`  
```yaml
persistence:
  type: pvc
  enabled: true
  storageClassName: ebs-sc-gp3
  accessModes:
    - ReadWriteOnce
  size: 100Gi
  # annotations: {}
  finalizers:
    - kubernetes.io/pvc-protection
  # subPath: ""
  # existingClaim:
  
datasources:
  datasources.yaml:
    apiVersion: 1
    datasources:
    - name: Prometheus
      type: prometheus
      url: http://prometheus-server.prometheus.svc.cluster.local
      access: proxy
      isDefault: true

service:
  type: LoadBalancer
  port: 80
  targetPort: 3000
  annotations:
      service.beta.kubernetes.io/aws-load-balancer-type: external
      service.beta.kubernetes.io/aws-load-balancer-nlb-target-type: ip
      service.beta.kubernetes.io/aws-load-balancer-scheme: "internet-facing"
      service.beta.kubernetes.io/aws-load-balancer-attributes: load_balancing.cross_zone.enabled=true
```

최초 로그인은 admin 유저에 password는 아래의 커맨드로 확인한다. 
```bash
kubectl get secret --namespace prometheus grafana -o jsonpath="{.data.admin-password}" | base64 --decode ; echo
```
admin 접속 후 별도 User를 생성하여 사용한다. 

<br><br>

## 2. Prometehus & Grafana 동작방식

![prometheus-architecture004](/assets/it/cloud/eks/prometheus-architecture004.png){: width="90%" height="auto"}  [Ref](https://mihai-albert.com/2022/02/13/out-of-memory-oom-in-kubernetes-part-3-memory-metrics-sources-and-tools-to-collect-them/)

#### Prometheus Server
Prometheus 서버는 데이터를 수집해서 시계열 데이터에 저장하는 역할을 한다. 
- Retrieval
  - 메트릭을 수집하는 역할 
- TSBD(Time-series Database)
  - 수집된 데이터의 시계열 데이터베이스 
- HTTP server
  - Prometheus에 저장된 데이터를 제공하기 위한 REST API제공


#### Prometheus Node Expoter
메트릭 수집기로 메트릭 수집을 위해 DaemonSet으로 배포되며 시스템 메트릭(Underlyting node)을 수집한다. 수집된 메트릭은 prometheus server가 pull방식으로 데이터를 가져가며 시계열 데이터 베이스에 저장된다. 

#### [Metrics server](https://kubernetes.io/docs/reference/instrumentation/metrics/)  
Kubernetes API 서버는 모니터링 및 분석을 위해 지표를 제공하고 있으며 HTTP API로 /metrics 엔드포인트로 접근해 메트릭을 가져올 수 있다. 
```bash
kubectl get --raw /metrics
```
metrics-server는 kubernetes노드의 kubelet에서 주기적으로 메트릭을 수집하며 이 메트릭들이 집계되고 저장되어 Metrics API로 제공된다.  

#### [kube-state-metrics](https://github.com/kubernetes/kube-state-metrics/tree/main/docs)  
kube-state-metrics는 Kubernetes 오브젝트의 상태(metrics based on deployments, replica sets, 등)로부터 완전히 새로운 메트릭을 만드는데 중점을 둔다. Prometheus를 이용하면 이런 kube-state-metrics에도 접근이 가능하다. 


#### Grafana
Grafana는 최종적으로 Prometheus의 서버의 시계열 데이터에 수집된 데이터를 시각화 하여 보여준다. [Dashboard](https://grafana.com/grafana/dashboards/?search=Kubernetes)는 직접 만들어서 사용할 수도 있지만 링크에서 이미 정의된 Dashboard를 사용해도 된다. 


## 3. Test
그라파나에 접속 후 + New Dashboard를 통해 원하는 메트릭의 지표를 시각화 하여 볼 수 있다.  

이 중 API Server의 Endpoint를 모니터링 하는 Test Dashboard를 생성해 본다. [Endpoint Metrics](https://github.com/kubernetes/kube-state-metrics/blob/main/docs/endpoint-metrics.md)은 문서에서 참고할 수 있으며 kube_endpoint_address_available은 DEPRECATED로 kube_endpoint_address 메트릭의 SUM을 이용한다. 

DataSource로 Prometheus를 선택한 후 아래와 같이 +Query의 Code모드에서 아래의 정보를 입력하면 Endpoint의 숫자를 확인할 수 있다. 
```txt
sum(kube_endpoint_address{app_kubernetes_io_instance="prometheus", endpoint="kubernetes"})
kube_endpoint_address_available{app_kubernetes_io_instance="prometheus",endpoint="metrics-server"}
```

아래와 같이 Visualization은 Time series 또는 gauge를 활용할 수 있다.   

![grafana-dashboard001](/assets/it/cloud/eks/grafana-dashboard001.png){: width="95%" height="auto"}

![grafana-dashboard002](/assets/it/cloud/eks/grafana-dashboard002.png){: width="95%" height="auto"}


<br><br><br>

- References  
[1] http://blog.itaysk.com/2019/01/15/Kubernetes-metrics-and-monitoring  
[2] kube-state-metrics - https://github.com/kubernetes/kube-state-metrics#kube-state-metrics-vs-metrics-server  