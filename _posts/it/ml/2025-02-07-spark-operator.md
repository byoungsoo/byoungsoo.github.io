---
layout: post
title: "MLOps - Apache Spark Operator"
author: "Bys"
category: ml
date: 2025-02-07 01:00:00
tags: mlops spark
---

## MLOps

### [Install Spark(Official)](https://github.com/kubeflow/spark-operator/tree/master/charts/spark-operator-chart) 

`values.yaml`
```yaml
spark:
  jobNamespaces:
  - spark
```


```bash
helm repo add spark-operator https://kubeflow.github.io/spark-operator
helm repo update

helm install spark-operator spark-operator/spark-operator -n spark -f /Users/bys/workspace/kubernetes/mlops/spark/values.yaml
helm delete spark-operator -n spark

kubectl apply -f https://raw.githubusercontent.com/kubeflow/spark-operator/refs/heads/master/examples/spark-pi.yaml
```

<br>

### [Running Spark on Kubernetes 공식문서](https://spark.apache.org/docs/latest/running-on-kubernetes.html)
Application 이 완료되면 executor 파드들은 종료되고 사라지지만, driver 파드는 logs를 위해 completed 상태로 유지된다. spark-submit cli를 통해서도 확인 가능.



### Test
SparkApplication 을 배포하면 Driver 파드가 생성되며 드라이버 파드는 작업 플랜을 계획하고 API 서버로 다시 executors 파드 생성을 요청한다. Executors 파드들은 분산 처리를 하는 작업 단위이며 완료되면 driver 파드에게 보고하고 종료된다.  
> When the application completes, the executor pods terminate and are cleaned up, but the driver pod persists logs and remains in “completed” state in the Kubernetes API until it’s eventually garbage collected or manually cleaned up.

`파이 계산 샘플`  
[SparkApplication](https://raw.githubusercontent.com/kubeflow/spark-operator/refs/heads/master/examples/spark-pi.yaml)
```yaml
apiVersion: sparkoperator.k8s.io/v1beta2
kind: SparkApplication
metadata:
  name: spark-pi
  namespace: spark
spec:
  type: Scala
  mode: cluster
  image: spark:3.5.3
  imagePullPolicy: IfNotPresent
  mainClass: org.apache.spark.examples.SparkPi
  mainApplicationFile: local:///opt/spark/examples/jars/spark-examples.jar
  arguments:
  - "5000"
  sparkVersion: 3.5.3
  driver:
    labels:
      version: 3.5.3
    cores: 1
    memory: 512m
    serviceAccount: spark-operator-spark
  executor:
    labels:
      version: 3.5.3
    instances: 1
    cores: 1
    memory: 512m
```


```bash
brew install apache-spark
spark-submit --status sparkjob:spark-pi-driver --master  k8s://https://364455D087196228AE6E206BF4F48568.gr7.us-east-1.eks.amazonaws.com
```

<br><br>
