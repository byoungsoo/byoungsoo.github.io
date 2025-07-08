---
layout: post
title: "MLOps - Seldon Core Operator"
author: "Bys"
category: ml
date: 2025-02-25 01:00:00
tags: mlops seldon
---

## MLOps

### Seldon Core


### [Install Seldon-Core-Operator(Official)](https://github.com/SeldonIO/seldon-core/tree/master/helm-charts/seldon-core-operator) 
`values.yaml`
```yaml
istio:
  enabled: true
  gateway: istio-system/seldon-gateway
  tlsMode: ""
```


```bash
helm repo add seldonio https://storage.googleapis.com/seldon-charts
helm repo update

helm upgrade -i seldon-core-operator seldonio/seldon-core-operator -n seldon -f /Users/bys/workspace/kubernetes/mlops/seldon/values.yaml
helm delete seldon-core-operator -n seldon
```

`Istio`  
```bash
helm upgrade -i istio-base istio/base -n istio-system -f /Users/bys/workspace/kubernetes/mlops/seldon/istio/istiod-values.yaml
helm upgrade -i istiod istio/istiod -n istio-system -f /Users/bys/workspace/kubernetes/mlops/seldon/istio/base-values.yaml
helm upgrade -i istio-ingressgateway istio/gateway -n istio-system -f /Users/bys/workspace/kubernetes/mlops/seldon/istio/gateway-values.yaml
```

```yaml
service:
  # Type of service. Set to "None" to disable the service entirely
  type: LoadBalancer
  ports:
  - name: https
    port: 443
    protocol: TCP
    targetPort: 80
  annotations:
    service.beta.kubernetes.io/aws-load-balancer-name: k8s-seldon-ingressgateway
    service.beta.kubernetes.io/aws-load-balancer-type: external
    service.beta.kubernetes.io/aws-load-balancer-nlb-target-type: ip
    service.beta.kubernetes.io/aws-load-balancer-subnets: bys-dev-ue1-sbn-1a-extelb, bys-dev-ue1-sbn-1b-extelb, bys-dev-ue1-sbn-1c-extelb, bys-dev-ue1-sbn-1d-extelb, bys-dev-ue1-sbn-1f-extelb
    service.beta.kubernetes.io/aws-load-balancer-scheme: "internet-facing"
    service.beta.kubernetes.io/aws-load-balancer-attributes: load_balancing.cross_zone.enabled=true
    service.beta.kubernetes.io/aws-load-balancer-ssl-negotiation-policy: ELBSecurityPolicy-TLS13-1-2-2021-06
    service.beta.kubernetes.io/aws-load-balancer-ssl-ports: "443"
    service.beta.kubernetes.io/aws-load-balancer-ssl-cert: arn:aws:acm:us-east-1:558846430793:certificate/a5207b24-ae67-49ac-b34e-f34ed0088bca
    service.beta.kubernetes.io/aws-load-balancer-backend-protocol: tcp
    service.beta.kubernetes.io/aws-load-balancer-healthcheck-protocol: tcp
    service.beta.kubernetes.io/aws-load-balancer-healthcheck-port: traffic-port
    service.beta.kubernetes.io/aws-load-balancer-healthcheck-healthy-threshold: "2"
    service.beta.kubernetes.io/aws-load-balancer-healthcheck-unhealthy-threshold: "3"
    service.beta.kubernetes.io/aws-load-balancer-healthcheck-interval: "20"
    service.beta.kubernetes.io/aws-load-balancer-security-groups: sg-07e6c272df0bed7ee
    service.beta.kubernetes.io/aws-load-balancer-additional-resource-tags: auto-delete=no
  loadBalancerIP: ""
  loadBalancerSourceRanges: []
  externalTrafficPolicy: ""
  externalIPs: []
  ipFamilyPolicy: ""
  ipFamilies: []
```

```yaml
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: seldon-gateway
  namespace: istio-system
spec:
  selector:
    istio: ingressgateway # use istio default controller
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```



### SeldonDeployment
```yaml
apiVersion: machinelearning.seldon.io/v1
kind: SeldonDeployment
metadata:
  labels:
    app: seldon
  name: model-test
spec:
  annotations:
    project_name: test
    deployment_version: v1
  name: model-test-spec
  predictors:
      - componentSpecs:
        - spec:
            containers:
              - image: 202949997891.dkr.ecr.ap-northeast-2.amazonaws.com/common/build:mlv1
                imagePullPolicy: Always
                name: model-test-predictor
                env:
                  - name: MODEL_NAME
                    value: "Predictor"
                  - name: SERVICE_TYPE
                    value: MODEL
                  - name: GRPC_PORT
                    value: "5005"
                  - name: METRICS_PORT
                    value: "6005"
                  - name: HTTP_PORT
                    value: "9000"
          hpaSpec:
            maxReplicas: 1
            metrics:
              - resource:
                  name: cpu
                  targetAverageUtilization: 80
                type: Resource
            minReplicas: 1
        graph:
          children:
          name: model-test-predictor
          endpoint:
            type: REST
            service_host: localhost
            service_port: 9000
          type: MODEL
          logger:
            url: http://logger/
            mode: all
        name: predictor
        annotations:
          predictor_version: "v1"
          team: opendatahub
          seldon.io/svc-name: model-test
        labels:
          team: mlops
          version: v1
        replicas: 1
```




`ingress.yaml`  
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  annotations:
    project_name: model-test
    deployment_version: v1
    alb.ingress.kubernetes.io/group.name: mlops
    alb.ingress.kubernetes.io/subnets: bys-dev-ue1-sbn-1a-extelb, bys-dev-ue1-sbn-1b-extelb, bys-dev-ue1-sbn-1c-extelb, bys-dev-ue1-sbn-1d-extelb, bys-dev-ue1-sbn-1f-extelb
    alb.ingress.kubernetes.io/scheme : internet-facing
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTPS": 443}]'
    alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:us-east-1:558846430793:certificate/a5207b24-ae67-49ac-b34e-f34ed0088bca
    alb.ingress.kubernetes.io/security-groups: sg-07e6c272df0bed7ee
    alb.ingress.kubernetes.io/healthcheck-path: /
    alb.ingress.kubernetes.io/healthcheck-interval-seconds: '15'
    alb.ingress.kubernetes.io/healthcheck-timeout-seconds: '10'
    alb.ingress.kubernetes.io/healthy-threshold-count: '2'
    alb.ingress.kubernetes.io/unhealthy-threshold-count: '4'
    alb.ingress.kubernetes.io/healthcheck-port: traffic-port
    alb.ingress.kubernetes.io/success-codes: 200,302
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/tags: auto-delete=no
  labels:
    app: seldon
  name: model-test
spec:
  ingressClassName: "alb"
  rules:
    - host: model-test.bys.asia
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: model-test
                port:
                  number: 8000
```


`Test`  
```bash
curl -vvvv -X POST 'https://model-test.bys.asia/api/v1.0/predictions' --header 'Content-Type: application/json' --data-raw '{ "data": { "ndarray": [[2,1]] }}'

{"data":{"names":["t:0","t:1","t:2","t:3"],"ndarray":[[0.25,0.25,0.25,0.25]]},"meta":{"requestPath":{"model-test-predictor":"202949997891.dkr.ecr.ap-northeast-2.amazonaws.com/common/build:mlv1"}}}
```


<br><br>


