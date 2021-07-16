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

`Helm Deploy`  
Helm Deploy시 

helm argo -n argo argo/argo-cd -f values.yaml
```bash
helm upgrade -i --debug ${APP_NAME}-${ENV} ./helm/ -f ./helm/${HELM_VALUES_FILE} -n ${APPLICATION_NS}
```