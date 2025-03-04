---
layout: post
title: "Apache Spark Operator"
author: "Bys"
category: solution
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

When the application completes, the executor pods terminate and are cleaned up, but the driver pod persists logs and remains in “completed” state in the Kubernetes API until it’s eventually garbage collected or manually cleaned up.

<br><br>
