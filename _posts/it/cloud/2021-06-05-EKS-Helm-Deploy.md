---
layout: post
title: "AWS EKS Helm Deploy"
author: "Bys"
category: cloud
date: 2021-04-29 01:00:00
tags: aws eks helm deploy
---

#### Helm 설정  
`Instasll Helm`
```bash
curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh
```
<br>

#### Helm Create  
helm create "name" 을 통해서 default chart를 생성할 수 있다.  
아래와 같이 command를 실행하면 현재 폴더에 helm 폴더가 생기고 하위에 templates, Chart.yaml, Values.yaml 이 생성된다.  
templates 하위에는 deployment.yaml, service.yaml, serviceaccount.yaml ingress.yaml 등이 기본적으로 생성되며 모두 values.yaml에 설정한 값을 이용하여 배포된다.  
```bash
helm create helm
```
<br>

`Helm Deploy`  
위에서 생성한 chart를 이용하여 values.yaml 값을 적정하게 설정한 후 아래와 같이 커맨드를 수행하면 배포가 진행된다.  

```bash
#Command
helm upgrade -i test ./helm/ -f ./helm/values.yaml -n test

#Comments
Release "test" does not exist. Installing it now.
NAME: test
LAST DEPLOYED: Fri Jul 16 17:24:50 2021
NAMESPACE: test
STATUS: deployed
REVISION: 1
NOTES:
1. Get the application URL by running these commands:
  export POD_NAME=$(kubectl get pods --namespace test -l "app.kubernetes.io/name=helm,app.kubernetes.io/instance=test" -o jsonpath="{.items[0].metadata.name}")
  export CONTAINER_PORT=$(kubectl get pod --namespace test $POD_NAME -o jsonpath="{.spec.containers[0].ports[0].containerPort}")
  echo "Visit http://127.0.0.1:8080 to use your application"
  kubectl --namespace test port-forward $POD_NAME 8080:$CONTAINER_PORT
```

```bash
helm upgrade -i --debug ${APP_NAME} ./helm/ -f ./helm/${HELM_VALUES_FILE} -n ${APPLICATION_NS}

helm ls -A #helm ls -n namespace
```

