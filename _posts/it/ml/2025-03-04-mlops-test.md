---
layout: post
title: "MLOps - Model 빌드/배포 및 테스트"
author: "Bys"
category: ml
date: 2025-03-04 01:00:00
tags: mlops
---


## MLOps
DecisionTreeClassifier를 활용한 Model 빌드의 첫 걸음. 이 단계는 이 동안 설치한 Jupyter notebook, MLflow, MiniO, Seldon 을 활용해 모델을 빌드/배포 하고 테스트 하는 시나리오를 작성한다.  

### Jupyterlab 에서 모델 빌드 

`Model mlflow`
```bash
!pip install numpy
!pip install DecisionTreeClassifier
!pip install mlflow
!pip install boto3
```

```python
import numpy as np
from sklearn.tree import DecisionTreeClassifier
import mlflow
import os

X = np.array([
  [1, 1],
  [1, 2],
  [1, 3],
  [1, 4],
  [2, 1],
  [2, 2],
  [2, 3],
  [2, 4],
  [3, 1],
  [3, 2],
  [3, 3],
  [3, 4],
  [4, 1],
  [4, 2],
  [4, 3],
  [4, 4],
  [10,1],
  [10,15]
])
y = np.array([0, 0, 1, 1, 0, 0, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3])

os.environ['MLFLOW_TRACKING_USERNAME'] = "admin"
os.environ['MLFLOW_TRACKING_PASSWORD'] = "admin"
os.environ['MLFLOW_S3_ENDPOINT_URL']='http://minio.minio.svc.cluster.local:9000'
os.environ['AWS_ACCESS_KEY_ID']='mlflow'
os.environ['AWS_SECRET_ACCESS_KEY']='miniomlflow'

HOST = "https://mlflow.bys.asia/"
EXPERIMENT_NAME = "mlflow-test-experiment"

# Connect to local MLflow tracking server
mlflow.set_tracking_uri(HOST)

# Set the experiment name through which you will label all your exerpiments runs
mlflow.set_experiment(EXPERIMENT_NAME)

# enable autologging for scikit
mlflow.sklearn.autolog()

model = DecisionTreeClassifier(max_depth=3, criterion='gini',min_samples_leaf = 1 ,min_samples_split = 2)

with mlflow.start_run() as run:
    model.fit(X, y)
```

여기까지는 Jupyter notebook 에서 진행.

<br>

### MLflow(MiniO) 에 저장된 Artifact 다운로드 
Jupyter notebook 에서 모델 빌드를 하고 나서 저장된 Artifact 중 model.pkl, requirements.txt 파일을 다운로드 받는다. 

`1. Dockerfile`  
```Dockerfile
FROM python:3.11-bullseye

WORKDIR /microservice
RUN pip install --upgrade pip

COPY base_requirements.txt /microservice/
RUN pip install -r base_requirements.txt

COPY requirements.txt /microservice/
RUN pip install -r requirements.txt

COPY Predictor.py   model.pkl /microservice/

CMD seldon-core-microservice $MODEL_NAME --service-type $SERVICE_TYPE --grpc-port ${GRPC_PORT} --metrics-port ${METRICS_PORT} --http-port ${HTTP_PORT}
```

`2. Predictor.py`  
```python
import joblib


class Predictor(object):

    def __init__(self):
        self.model = joblib.load('model.pkl')

    def predict(self, data_array, column_names):
        return self.model.predict_proba(data_array)
```

`3. base_requirements.txt`
```
seldon-core
joblib
```

`4. model.pkl && requirements.txt`   

위 파일을 모두 한 폴더에 정리한 후 Dockerfile을 빌드한다.  

<br>

### Seldon
아래 SeldonDeployment 파일에서 이미지를 배포한 이미지로 수정하여 배포하면 이전 단계에서 학습시킨 model.pkl 을 파일을 배포할 수 있다.  

`seldondeployment.yaml`
```yaml
apiVersion: machinelearning.seldon.io/v1
kind: SeldonDeployment
metadata:
  labels:
    app: seldon
  name: model-v3
spec:
  annotations:
    project_name: test
    deployment_version: "v3"
  #     seldon.io/engine-separate-pod: "true"
  name: test-specs
  predictors:
      - componentSpecs:
        - spec:
            containers:
              - image: 202949997891.dkr.ecr.ap-northeast-2.amazonaws.com/common/build:mlv3
                imagePullPolicy: Always
                name: predictor
                env:
                  - name: MODEL_NAME
                    value: "Predictor"    
                  - name: SERVICE_TYPE
                    value: MODEL
                  - name: GRPC_PORT
                    value: "5007"
                  - name: METRICS_PORT
                    value: "6007"
                  - name: HTTP_PORT
                    value: "9000"
          hpaSpec:
            maxReplicas: 2
            metrics:
              - resource:
                  name: cpu
                  targetAverageUtilization: 80
                type: Resource
            minReplicas: 1
        graph:
          children:
          name: predictor
          type: MODEL
          endpoint:
            type: REST
            service_host: localhost
            service_port: 9000
          # logger:
          #   url: http://logger/
          #   mode: all
        name: predictor
        annotations:
          predictor_version: "v3"
          team: opendatahub
          seldon.io/svc-name: model-test
        labels:
          team: opendatahub
          version: v1
        replicas: 1
```

아래의 Ingress를 통해 seldon-operator를 통해 배포되는 서비스를 노출할 수 있다.  
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
    alb.ingress.kubernetes.io/healthcheck-path: /api/v1.0/predictions
    alb.ingress.kubernetes.io/healthcheck-interval-seconds: '10'
    alb.ingress.kubernetes.io/healthcheck-timeout-seconds: '5'
    alb.ingress.kubernetes.io/healthy-threshold-count: '2'
    alb.ingress.kubernetes.io/unhealthy-threshold-count: '2'
    alb.ingress.kubernetes.io/healthcheck-port: traffic-port
    alb.ingress.kubernetes.io/success-codes: 200-405
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

<br><br>


`Test`  
```bash
X = np.array([
  [1, 1],
  [1, 2],
  [1, 3],
  [1, 4],
  [2, 1],
  [2, 2],
  [2, 3],
  [2, 4],
  [3, 1],
  [3, 2],
  [3, 3],
  [3, 4],
  [4, 1],
  [4, 2],
  [4, 3],
  [4, 4],
])
y = np.array([0, 0, 1, 1, 0, 0, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2])

curl -X POST 'https://model-test.bys.asia/api/v1.0/predictions' --header 'Content-Type: application/json' --data-raw '{ "data": { "ndarray": [[6,0]] }}'
{"data":{"names":["t:0","t:1","t:2"],"ndarray":[[0.0,0.0,1.0]]},"meta":{"requestPath":{"model-test-predictor":"202949997891.dkr.ecr.ap-northeast-2.amazonaws.com/common/build:mlv2"}}}

curl -X POST 'https://model-test.bys.asia/api/v1.0/predictions' --header 'Content-Type: application/json' --data-raw '{ "data": { "ndarray": [[2, 4]] }}'
{"data":{"names":["t:0","t:1","t:2"],"ndarray":[[0.0,1.0,0.0]]},"meta":{"requestPath":{"model-test-predictor":"202949997891.dkr.ecr.ap-northeast-2.amazonaws.com/common/build:mlv2"}}}

curl -X POST 'https://model-test.bys.asia/api/v1.0/predictions' --header 'Content-Type: application/json' --data-raw '{ "data": { "ndarray": [[10,0]] }}'
{"data":{"names":["t:0","t:1","t:2"],"ndarray":[[0.0,0.0,1.0]]},"meta":{"requestPath":{"predictor":"202949997891.dkr.ecr.ap-northeast-2.amazonaws.com/common/build:mlv2"}}}

curl -X POST 'https://model-test.bys.asia/api/v1.0/predictions' --header 'Content-Type: application/json' --data-raw '{ "data": { "ndarray": [[15,4]] }}'
{"data":{"names":["t:0","t:1","t:2"],"ndarray":[[0.0,0.0,1.0]]},"meta":{"requestPath":{"predictor":"202949997891.dkr.ecr.ap-northeast-2.amazonaws.com/common/build:mlv2"}}}
```

```bash
X = np.array([
  [1, 1],
  [1, 2],
  [1, 3],
  [1, 4],
  [2, 1],
  [2, 2],
  [2, 3],
  [2, 4],
  [3, 1],
  [3, 2],
  [3, 3],
  [3, 4],
  [4, 1],
  [4, 2],
  [4, 3],
  [4, 4],
  [10,1],
  [10,15]
])
y = np.array([0, 0, 1, 1, 0, 0, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3])

curl -X POST 'https://model-test.bys.asia/api/v1.0/predictions' --header 'Content-Type: application/json' --data-raw '{ "data": { "ndarray": [[6,0]] }}'
{"data":{"names":["t:0","t:1","t:2","t:3"],"ndarray":[[0.0,0.0,1.0,0.0]]},"meta":{"requestPath":{"predictor":"202949997891.dkr.ecr.ap-northeast-2.amazonaws.com/common/build:mlv3"}}}

curl -X POST 'https://model-test.bys.asia/api/v1.0/predictions' --header 'Content-Type: application/json' --data-raw '{ "data": { "ndarray": [[10,0]] }}'
{"data":{"names":["t:0","t:1","t:2","t:3"],"ndarray":[[0.0,0.0,0.0,1.0]]},"meta":{"requestPath":{"predictor":"202949997891.dkr.ecr.ap-northeast-2.amazonaws.com/common/build:mlv3"}}}

curl -X POST 'https://model-test.bys.asia/api/v1.0/predictions' --header 'Content-Type: application/json' --data-raw '{ "data": { "ndarray": [[15,4]] }}'
{"data":{"names":["t:0","t:1","t:2","t:3"],"ndarray":[[0.0,0.0,0.0,1.0]]},"meta":{"requestPath":{"predictor":"202949997891.dkr.ecr.ap-northeast-2.amazonaws.com/common/build:mlv3"}}}
```


#### 분석

```txt
 - X: 16개의 데이터 포인트, 각각 2개의 특성을 가짐
 - y: 각 데이터 포인트의 클래스 레이블 (0, 1, 2 중 하나)

이 모델은 다음과 같은 패턴을 학습하게 된다.  
    첫 번째 특성이 1 또는 2인 경우:
        대체로 클래스 0과 1로 분류됨
        두 번째 특성이 1-2면 클래스 0, 3-4면 클래스 1인 패턴이 보임

    첫 번째 특성이 3 또는 4인 경우:
        모두 클래스 2로 분류됨

model v3 에서는 (10,1), (10, 15) 에 대한 데이터 클래스를 3으로 추가 제공하였고, 추론결과 (15,4)와 같은 데이터는 t4 클래스로 분류되는 것을 알 수 있다. 
```

