---
slug: k8s-managed-fields
author: Bys
categories:
- k8s
category: k8s
date: '2025-08-11 01:00:00'
tags:
- k8s
title: Kubernetes - managed fields 이해하기
description: "Kubernetes Server-Side Apply와 managed fields의 동작 원리. Client-Side Apply와의 차이점, 필드 소유권 충돌 해결 방법, 운영 환경에서 server-side apply를 안전하게 적용하는 방법을 설명합니다."
---


# [Managed Fields 이해하기](https://kubernetes.io/docs/reference/using-api/server-side-apply/#field-management)  

## [Client-Side Apply](https://kubernetes.io/docs/reference/using-api/server-side-apply/#comparison-with-client-side-apply)  
문서[1]에 따라 `kubectl apply` 커맨드를 입력시 `--field-manager` 값은 Default로 `kubectl-client-side-apply` 가 적용된다. 따라서, 우리가 일반적으로 사용하는 `kubectl apply` 커맨드의 경우 client-side-apply 를 사용하는 것이며, client-side-apply 시에는 로컬 환경의 값이 overwrite 되는 것이 기본이다. 따라서, User A와 User B 가 서로 다른 매니페스트를 로컬에 가지고 있는 경우 위험한 상황이 발생할 수 있다.  

예를 들어, User A가 1.19 버전의 매니페스트를 배포 했는데, User B가 1.18 버전을 배포한다면 이미지 버전이 overwrite 되어 서버에 덮어쓰게 된다. 따라서, 운영환경에서 이러한 위험함을 방지하기 위해서 server-side-apply 를 적용하여 관리한다.  
```yaml
# User A
apiVersion: v1
kind: Pod
metadata:
  name: my-pod
spec:
  containers:
  - name: nginx
    image: nginx:1.19
    env:
    - name: ENV_VAR
      value: "production"

# User B
apiVersion: v1
kind: Pod
metadata:
  name: my-pod
spec:
  containers:
  - name: nginx
    image: nginx:1.18
    env:
    - name: ENV_VAR
      value: "production"
```


## [Server-Side Apply](https://kubernetes.io/docs/reference/using-api/server-side-apply/)  
server-side-apply[2]는 클러스터의 컨트롤 플레인이 필드에 대한 변경을 추적할 수 있도록 하는 메커니즘을 제공한다. (kubectl.kubernetes.io/last-applied-configuration 는 legacy 방식)  
서버사이드를 통해 적용된 필드는 `managed fields` 로 관리되며 다른 관리자에 의해 필드 값이 변경될 때는 충돌을 일으킨다. 다른 관리자에 의해 관리되는 필드는 특정 방식에 의해 강제로 overwrite 될 수 있지만, 이러한 방식으로 필드의 값이 강제로 변경되면 소유권도 이전되게 된다.  

아래에서 `metadata.labels.test-label`, `data` 필드는 kubectl 매니저에 의해 관리된다. 이렇게 적용된 필드는 다른 kubectl 매니저에 의해 관리되며 다른 User에 의해 해당 필드가 수정될 경우 충돌이 발생한다.  
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: test-cm
  namespace: default
  labels:
    test-label: test
  managedFields:
  - manager: kubectl
    operation: Apply # note capitalization: "Apply" (or "Update")
    apiVersion: v1
    time: "2010-10-10T0:00:00Z"
    fieldsType: FieldsV1
    fieldsV1:
      f:metadata:
        f:labels:
          f:test-label: {}
      f:data:
        f:key: {}
data:
  key: some value
```

```bash
# 최초 적용
kubectl apply --server-side --field-manager=bys -f config.yaml

# data 값을 변경하여 아래와 같이 적용하면 user-b의 conflict 오류가 발생한다.  
kubectl apply --server-side --field-manager=bys-test -f config.yaml

error: Apply failed with 1 conflict: conflict with "bys-test": .data.key
Please review the fields above--they currently have other managers. Here
are the ways you can resolve this warning:
* If you intend to manage all of these fields, please re-run the apply
  command with the `--force-conflicts` flag.
* If you do not intend to manage all of the fields, please edit your
  manifest to remove references to the fields that should keep their
  current managers.
* You may co-own fields by updating your manifest to match the existing
  value; in this case, you'll become the manager if the other manager(s)
  stop managing the field (remove it from their configuration).
See https://kubernetes.io/docs/reference/using-api/server-side-apply/#conflicts
```

<br>


다만, `apply` 커맨드가 아닌 `patch` 커맨드를 통해 Update를 수행하면 필드의 소유권을 이전하며 업데이트를 진행할 수 있다.  

```bash
# patch 를 통해 Update
kubectl patch configmap test-cm --patch='{"data":{"key":"new value"}}'

# managed-fields 를 포함하여 조회 
kubectl  get cm test-cm -o yaml --show-managed-fields                        
```

아래와 같이 manager는 `kubectl-patch`로 소유권이 이전되었으며, .data 값이 new value 로 변경된 것을 알 수 있다. 
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  creationTimestamp: "2025-08-11T02:14:46Z"
  labels:
    test-label: test
  managedFields:
  - apiVersion: v1
    fieldsType: FieldsV1
    fieldsV1:
      f:metadata:
        f:labels:
          f:test-label: {}
    manager: bys
    operation: Apply
    time: "2025-08-11T02:14:55Z"
  - apiVersion: v1
    fieldsType: FieldsV1
    fieldsV1:
      f:data:
        f:key: {}
    manager: kubectl-patch
    operation: Update
    time: "2025-08-11T02:18:05Z"
  name: test-cm
  namespace: default
  resourceVersion: "590350392"
  uid: b2f911d1-db6f-43e6-9a74-7e42874548b3
data:
  key: new value
```


결론적으로, client-side apply 를 통해 매니페스트를 적용하는 경우, 여러 사용자에의해 충돌관리 없이 필드 값들이 overwrite 되여 운영상 문제가 발생할 수 있다. 따라서, 특정 필드들에 대해서는 소유권을 가지게 하여 해당 소유권을 가진 관리자가 아닌 다른 관리자가 필드를 업데이트 할 때는 충돌을 일으키게 할 수 있다. 이런 경우 의도된 값 변경인 경우 --force-conflicts 플래그를 통해 값을 덮어쓸 수 있다. 일반적으로 그렇지 않은 경우 변경하려는 필드를 제거하거나 필드 관리자에게 문의해야 한다.  

---

## 📚 References

[1] **kubectl apply**
- https://kubernetes.io/docs/reference/kubectl/generated/kubectl_apply/

[1] **Server-Side Apply**
- https://kubernetes.io/docs/reference/using-api/server-side-apply/

