---
layout: post
title: "Argocd multi-cluster 설정 및 ApplicationSet"
author: "Bys"
category: solution
date: 2026-01-26 01:00:00
keywords: "argocd, applicationset"
tags: argocd applicationset
---

# [Argocd multi-cluster](https://argo-cd.readthedocs.io/en/stable/operator-manual/cluster-management/)  
Argocd 멀티 클러스터 아키텍처에서는 하나의 Argocd 에서 다중 클러스터에 application 을 배포한다. 

**시나리오**  
1. AWS 에는 Shared(Management, 관리계정), Dev, Prd 계정이 존재하고 각 계정에 EKS 클러스터가 존재한다고 가정
2. Argocd 는 Shared 계정에 존재하는 eks-shared 에 설치
3. eks-shared 에 존재하는 Argocd 에서 eks-dev, eks-prd 에 application 배포

**Prerequisite**
1. aws eks update-kubeconfig 를 통해 shared, dev, prd 계정의 EKS 클러스터가 각각 .kube/config에 등록 (argocd cli 사용시 필요)
2. argocd cli

## 1. 다중 클러스터 설정  
Argocd가 관리계정에 설치가 되었다면 `argocd cluster add <eks-context-name> --name <alias>` 커맨드를 통해 각각 dev, prd 계정의 EKS 클러스터를 추가한다. 
```bash
$ argocd login argocd.bys.digital --grpc-web
Username: admin
Password:

$ argocd cluster list --grpc-web
SERVER                          NAME                 VERSION  STATUS   MESSAGE                                                  PROJECT
https://kubernetes.default.svc  bys-shared-eks-main           Unknown  Cluster has no applications and is not being monitored.

$ argocd cluster add eks-main --name bys-dev-eks-main --grpc-web
WARNING: This will create a service account `argocd-manager` on the cluster referenced by context `eks-main` with full cluster level privileges. Do you want to continue [y/N]? y
{"level":"info","msg":"ServiceAccount \"argocd-manager\" already exists in namespace \"kube-system\"","time":"2026-01-26T22:50:58+09:00"}
{"level":"info","msg":"ClusterRole \"argocd-manager-role\" updated","time":"2026-01-26T22:50:58+09:00"}
{"level":"info","msg":"ClusterRoleBinding \"argocd-manager-role-binding\" updated","time":"2026-01-26T22:50:58+09:00"}
{"level":"info","msg":"Using existing bearer token secret \"argocd-manager-long-lived-token\" for ServiceAccount \"argocd-manager\"","time":"2026-01-26T22:50:58+09:00"}
Cluster 'https://A8845D3F0E5C385227204D33B8635ABC.sk1.ap-northeast-2.eks.amazonaws.com' added

$ argocd cluster add eks-test --name bys-dev-eks-test --grpc-web
WARNING: This will create a service account `argocd-manager` on the cluster referenced by context `eks-test` with full cluster level privileges. Do you want to continue [y/N]? y
{"level":"info","msg":"ServiceAccount \"argocd-manager\" already exists in namespace \"kube-system\"","time":"2026-01-26T22:51:14+09:00"}
{"level":"info","msg":"ClusterRole \"argocd-manager-role\" updated","time":"2026-01-26T22:51:14+09:00"}
{"level":"info","msg":"ClusterRoleBinding \"argocd-manager-role-binding\" updated","time":"2026-01-26T22:51:14+09:00"}
{"level":"info","msg":"Using existing bearer token secret \"argocd-manager-long-lived-token\" for ServiceAccount \"argocd-manager\"","time":"2026-01-26T22:51:14+09:00"}
Cluster 'https://FD553DA42E597A1A4A5076908E2794B5.gr7.ap-northeast-2.eks.amazonaws.com' added


$ argocd cluster list --grpc-web
SERVER                                                                         NAME                 VERSION  STATUS   MESSAGE                                                  PROJECT
https://kubernetes.default.svc                                                 bys-shared-eks-main           Unknown  Cluster has no applications and is not being monitored.
https://A8845D3F0E5C385227204D33B8635ABC.sk1.ap-northeast-2.eks.amazonaws.com  bys-dev-eks-main              Unknown  Cluster has no applications and is not being monitored.
https://FD553DA42E597A1A4A5076908E2794B5.gr7.ap-northeast-2.eks.amazonaws.com  bys-dev-eks-test              Unknown  Cluster has no applications and is not being monitored.
```
각각의 클러스터들이 추가된것을 확인할 수 있다.  

여기서 많은 시간을 소비했는데, 상태 값이 Status: `Unknown`, MESSAGE: `Cluster has no applications and is not being monitored.` 이렇게 나와서 등록이 제대로 안된 줄 알았다. 여러 문서들을 확인해본 결과 최초 Application 이 배포되지 않아서 인 것으로 확인했다. 메세지가 너무 부정확하다.


## 2. [Argocd project](https://argo-cd.readthedocs.io/en/stable/user-guide/projects/)  
ArgoCD 에서는 Project 레벨로 Application을 관리할 수 있다. 

- 격리 및 그룹화
  - 관련된 애플리케이션들을 하나의 단위로 묶어서 관리
  - Kubernetes의 namespace와 유사한 역할을 하지만, ArgoCD 레벨에서 작동

- 접근 제어 경계
  - 어떤 Git 저장소에서 배포할 수 있는지 제한
  - 어떤 클러스터와 namespace에 배포할 수 있는지 제한
  - 누가 어떤 작업을 수행할 수 있는지 RBAC으로 제어

```bash
$ argocd proj list --grpc-web
NAME     DESCRIPTION  DESTINATIONS  SOURCES  CLUSTER-RESOURCE-WHITELIST  NAMESPACE-RESOURCE-BLACKLIST  SIGNATURE-KEYS  ORPHANED-RESOURCES  DESTINATION-SERVICE-ACCOUNTS
default               *,*           *        */*                         <none>                        <none>          disabled            <none>

$ argocd proj create test

# 최초 생성시에는 DESTINATIONS, SOURCES, CLUSTER-RESOURCE-WHITELIST 모두 none 임을 알 수 있다. 이 상태에서는 해당 project 에서 application 배포가 불가하다.  
$ argocd proj list --grpc-web
NAME     DESCRIPTION  DESTINATIONS  SOURCES  CLUSTER-RESOURCE-WHITELIST  NAMESPACE-RESOURCE-BLACKLIST  SIGNATURE-KEYS  ORPHANED-RESOURCES  DESTINATION-SERVICE-ACCOUNTS
default               *,*           *        */*                         <none>                        <none>          disabled            <none>
test                  <none>        <none>   <none>                      <none>                        <none>          disabled            <none>

$ argocd proj create shared
$ argocd proj add-source shared '*' --grpc-web
$ argocd proj add-destination shared '*' '*' --grpc-web
$ argocd proj allow-cluster-resource shared '*' '*' --grpc-web

$ argocd proj create dev
$ argocd proj add-source dev '*' --grpc-web
$ argocd proj add-destination dev '*' '*' --grpc-web
$ argocd proj allow-cluster-resource dev '*' '*' --grpc-web


$ argocd proj list
NAME     DESCRIPTION  DESTINATIONS  SOURCES  CLUSTER-RESOURCE-WHITELIST  NAMESPACE-RESOURCE-BLACKLIST  SIGNATURE-KEYS  ORPHANED-RESOURCES  DESTINATION-SERVICE-ACCOUNTS
default               *,*           *        */*                         <none>                        <none>          disabled            <none>
dev                   *,*           *        */*                         <none>                        <none>          disabled            <none>
shared                *,*           *        */*                         <none>                        <none>          disabled            <none>
test                  <none>        <none>   <none>                      <none>                        <none>          disabled            <none>
```
이번 테스트에서는 소스/목적지를 모두 허용해주고 논리적인 분리만 진행하도록 한다.  


## 3. Application 
단일 클러스터를 Argocd 로 관리할 때는 아래와 같은 Application을 통해 관리하고 있었다. 여기서 `spec.destination.server`를 변경해주던가 또는 `spec.destination.name`을 통해 타겟을 변경해줄 수 있다.  
```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: aws-load-balancer-controller
  namespace: argocd
spec:
  project: default
  sources:
    - repoURL: 'https://gitlab.bys.digital/bys/argocd-apps.git'
      targetRevision: main
      ref: values
    - repoURL: https://aws.github.io/eks-charts
      chart: aws-load-balancer-controller
      targetRevision: 1.16.0
      helm:
        releaseName: aws-load-balancer-controller
        valueFiles:
          - $values/shared-ap2-eks-main/aws-lbc/values.yaml
  destination:
    server: https://kubernetes.default.svc  
    namespace: kube-system
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
      - CreateNamespace=false
      - ServerSideApply=true  # 권장
```

아래 Application은 마찬가지로 Shared 계정의 argocd 에 배포하지만, spec,destination 의 target 클러스터 변경을 통해 앱을 배포할 수 있다.  
```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: aws-load-balancer-controller
  namespace: argocd
spec:
  project: default
  sources:
    - repoURL: 'https://gitlab.bys.digital/bys/argocd-apps.git'
      targetRevision: main
      ref: values
    - repoURL: https://aws.github.io/eks-charts
      chart: aws-load-balancer-controller
      targetRevision: 1.16.0
      helm:
        releaseName: aws-load-balancer-controller
        valueFiles:
          - $values/dev-ap2-eks-test/aws-lbc/values.yaml
  destination:
    name: bys-dev-eks-test # 변경
    namespace: kube-system
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
      - CreateNamespace=false
      - ServerSideApply=true  # 권장
```


## 4. ApplicationSet
단일 클러스터를 Argocd 로 관리할 때는 Application을 통해 관리하고 있었고, 멀티 클러스터가 되었을 때도 프로젝트를 나누어서 각각에 대한 Application을 만들어주려고 했는데 클러스터가 늘어날 수록 너무 불필요한 작업이 반복될 것 같았다. 몇 가지 내용을 확인해본 결과 ApplicationSet 를 배포함으로서 관리할 수 있는 방안이 있었다. AWS-LBC, Karpenter 와 같은 시스템 서비스들의 경우 ApplicationSet 으로 관리하는것이 훨씬 좋은 선택이었다.  

아래와 같이 [spec.generators](https://argo-cd.readthedocs.io/en/stable/operator-manual/applicationset/Generators-List/)설정을 통해 클러스터를 리스트업 한다.  
`ApplicationSet`  
```yaml
apiVersion: argoproj.io/v1alpha1
kind: ApplicationSet
metadata:
  name: aws-load-balancer-controller
  namespace: argocd
spec:
  goTemplate: true
  goTemplateOptions: ["missingkey=error"]
  generators:
    - list:
        elements:
          - project: shared
            cluster: bys-shared-eks-main
            valuesPath: shared-ap2-eks-main/aws-lbc/values.yaml
          - project: dev
            cluster: bys-dev-eks-main
            valuesPath: dev-ap2-eks-main/aws-lbc/values.yaml
          - project: dev
            cluster: bys-dev-eks-test
            valuesPath: dev-ap2-eks-test/aws-lbc/values.yaml
  template:
    metadata:
      name: '{{.cluster}}-aws-load-balancer-controller'
      labels:
        app.kubernetes.io/name: aws-load-balancer-controller
        app.kubernetes.io/instance: '{{.cluster}}'
        cluster: '{{.cluster}}'
    spec:
      project: '{{.project}}'
      sources:
        - repoURL: 'https://gitlab.bys.digital/bys/argocd-apps.git'
          targetRevision: main
          ref: values
        - repoURL: https://aws.github.io/eks-charts
          chart: aws-load-balancer-controller
          targetRevision: 1.16.0
          helm:
            releaseName: aws-load-balancer-controller
            valueFiles:
              - '$values/{{.valuesPath}}'
      destination:
        name: '{{.cluster}}'  # 🔑 모든 클러스터에 name 사용
        namespace: kube-system
      syncPolicy:
        automated:
          prune: true
          selfHeal: true
        syncOptions:
          - CreateNamespace=false
          - ServerSideApply=true
```

ApplicationSet을 배포하면 아래와 같이 리스트된 클러스터에 모두 일괄 배포되는 것을 볼 수 있다.  
```bash
$ argocd app list
NAME                                                     CLUSTER              NAMESPACE    PROJECT  STATUS  HEALTH   SYNCPOLICY  CONDITIONS  REPO                                            PATH  TARGET
argocd/bys-dev-eks-main-aws-load-balancer-controller     bys-dev-eks-main     kube-system  default  Synced  Healthy  Auto-Prune  <none>      https://gitlab.bys.digital/bys/argocd-apps.git        main
argocd/bys-dev-eks-test-aws-load-balancer-controller     bys-dev-eks-test     kube-system  default  Synced  Healthy  Auto-Prune  <none>      https://gitlab.bys.digital/bys/argocd-apps.git        main
argocd/bys-shared-eks-main-aws-load-balancer-controller  bys-shared-eks-main  kube-system  default  Synced  Healthy  Auto-Prune  <none>      https://gitlab.bys.digital/bys/argocd-apps.git        main
```


마지막으로 Application 들이 정상 배포되면 아래와 같이 클러스터의 상태 정보도 모두 Unknown 에서 Successful로 변경되는 것을 볼 수 있다.  
```bash
$ argocd cluster list
SERVER                                                                         NAME                 VERSION  STATUS      MESSAGE  PROJECT
https://FD553DA42E597A1A4A5076908E2794B5.gr7.ap-northeast-2.eks.amazonaws.com  bys-dev-eks-test     1.34     Successful
https://kubernetes.default.svc                                                 bys-shared-eks-main  1.34     Successful
https://A8845D3F0E5C385227204D33B8635ABC.sk1.ap-northeast-2.eks.amazonaws.com  bys-dev-eks-main     1.34     Successful
```


결론적으로, 각각의 클러스터에서 관리되는 App의 경우는 Applications 로 배포를 진행하고, 멀티 클러스터에 일괄 배포를 위해서는 ApplicationSets 를 통해 관리할 수 있다.  

---

## 📚 References
[1] **ApplicationSet**  
- https://argo-cd.readthedocs.io/en/latest/operator-manual/applicationset/

[2] **Generators**  
- https://argo-cd.readthedocs.io/en/stable/operator-manual/applicationset/Generators/

