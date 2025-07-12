---
layout: post
title: "kubernetes Operator"
author: "Bys"
category: k8s
date: 2025-01-24 01:00:00
tags: kubernetes pattern operator
---

# [Operator]

### [Operator Pattern]()


---


```yaml
apiVersion: operators.coreos.com/v1alpha1
kind: CatalogSource
metadata:
  name: argocd-catalog
spec:
  sourceType: grpc
  image: quay.io/argoprojlabs/argocd-operator-registry@sha256:f0d2bb73e8b9d0561c931b2f2afc81cf28ca5711c476b02ac1887770e799ab92 # 0.13.0
  displayName: Argo CD Operators
  publisher: Argo CD Community

```

CatalogSource:
Operator의 저장소를 정의하는 객체입니다. 사용자가 OLM을 통해 특정 CatalogSource를 추가하면, OLM은 그 소스에서 Operator를 검색하고 설치할 수 있습니다.
예를 들어, Red Hat의 OperatorHub 같은 공용 저장소가 CatalogSource의 예입니다.



Catalog는 여러 Operator의 메타데이터를 포함하는 저장소입니다. 각 Operator의 정보(이름, 버전, 설명, 의존성 등)를 담고 있어 사용자가 적합한 Operator를 선택하고 설치할 수 있도록 돕습니다.

CatalogSource: Catalog의 원본을 정의하는 객체입니다. OLM은 이 CatalogSource를 통해 Operator를 검색하고 다운로드합니다.
Operator 메타데이터: 각 Operator에 대한 정보가 포함되어 있어, 사용자는 어떤 기능을 제공하는지, 어떤 버전이 있는지를 쉽게 확인할 수 있습니다.



Catalog와 OLM의 관계
Catalog 배포:

CatalogSource를 배포하면, OLM은 그 CatalogSource에서 제공하는 Operator 목록을 인식합니다. 이 CatalogSource는 OLM이 사용할 수 있는 Operator의 저장소 역할을 합니다.
Operator 설치:

사용자가 Catalog에서 원하는 특정 Operator를 선택하고 구독(Subscription)하면, OLM이 해당 Operator를 Kubernetes 클러스터에 자동으로 설치하고 관리합니다. 즉, Catalog 자체를 배포하는 것만으로는 자동으로 서비스가 배포되지는 않지만, OLM이 CatalogSource를 통해 Operator를 인식할 수 있게 하는 것입니다.
자동화:

OLM은 Catalog에 있는 Operator의 설치 및 업데이트를 자동으로 처리하므로, 사용자는 복잡한 설정 없이 쉽게 서비스를 관리할 수 있습니다.

OLM의 구성 요소
CatalogSource: Operator의 저장소로, OLM이 사용할 수 있는 Operator 목록을 제공합니다.
Subscription: 특정 Operator를 구독하여 OLM이 설치 및 업데이트를 자동으로 관리하도록 지정합니다.
OperatorGroup: Operator가 작동할 수 있는 네임스페이스를 정의합니다.
InstallPlan: OLM이 Operator를 설치하기 위해 따라야 할 계획을 수립합니다.


---

## 📚 References

[1] **Operators**  
- https://docs.redhat.com/ko/documentation/openshift_container_platform/4.9/html-single/operators/index#olm-architecture_olm-arch

[2] **쿠버네티스 패턴**  
- O'REILLY