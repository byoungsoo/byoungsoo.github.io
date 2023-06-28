---
layout: post
title: "Minikube 인증서를 통한 사용자 추가 및 인증/인가"
author: "Bys"
category: k8s
date: 2022-07-14 01:00:00
tags: kubernetes minikube csr crt authentication
---

# Minikube

## 1.Minikube 인증서를 이용한 사용자 인증  

이번에는 인증서를 통한 신규 사용자 생성 및 권한에 대해 확인 해 본다.  

- Normal user  
A few steps are required in order to get a normal user to be able to authenticate and invoke an API. First, this user must have a certificate issued by the Kubernetes cluster, and then present that certificate to the Kubernetes API.  

자세한 내용은 아래 공식홈페이지를 참고한다.  
[Certificate Signing Requests](https://kubernetes.io/docs/reference/access-authn-authz/certificate-signing-requests/)

사용자는 클러스터에 의해 발급된 인증서를 가지고 인증을 진행하면 허가가 가능하다. 
따라서 개인 키파일을 만들어 서명요청서를 만들고 클러스터의 키파일로 서명을 요청한다. 
클러스터의 관리자가 서명요청을 승인하면 클러스터에 의해 서명된 인증서가 발급되게 되고 사용자는 발급된 인증서를 통해 config파일을 구성하고 인증을 진행하게 된다.  

아래의 과정을 따라가며 진행해본다.  
<br>


### 1.1 Create private key 

openssl 커맨드를 통해 개인 키파일을 하나 생성한다.  
```bash
openssl genrsa -out devuser.key 2048
```

openssl 커맨드를 통해 생성된 키파일을 이용하여 csr(Certificate Signing Request)파일을 생성한다.  
CN 은 반드시 생성할 유저이름으로 지정한다.  
```bash
openssl req -new -key devuser.key -out devuser.csr
```

csr파일의 내용을 base64로 인코딩하여 출력한다.  
```bash
cat devuser.csr | base64 | tr -d "\n"
```
<br>


### 1.2 Create CertificateSigningRequest 

Certificate 서명 요청을 위해 아래와 같이 수행한다.  
Request는 위에서 csr파일의 base64인코딩으로 출력된 값을 입력한다. 

```bash
cat <<EOF | kubectl apply -f -
apiVersion: certificates.k8s.io/v1
kind: CertificateSigningRequest
metadata:
  name: devuser
spec:
  request: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURSBSRVFVRVNULS0tLS0KTUlJQ3RqQ0NBWjRDQVFBd1ZURUxNQWtHQTFVRUJoTUNTMUl4RlRBVEJnTlZCQWNNREVSbFptRjFiSFFnUTJsMAplVEVjTUJvR0ExVUVDZ3dUUkdWbVlYVnNkQ0JEYjIxd1lXNTVJRXgwWkRFUk1BOEdBMVVFQXd3SWRHVnpkSFZ6ClpYSXdnZ0VpTUEwR0NTcUdTSWIzRFFFQkFRVUFBNElCRHdBd2dnRUtBb0lCQVFESjhwZThWN3pTYnpBN0thZHkKMkQzeDM4QVhtTUZ0WTAwZDIyUWZBVUxxcmNXMG1XVldBUno0U3p0TTk5WUNuME94WmZtbUJqRGYxVSsyVUhocgpDSkxJck9GWGZZTUw2ZVZaUEhRaVRXM3ZPSk1ReHF4UGhVZGRnTG5ZSkViWjdwcW92dzgxSzYwQVJhaHF6Z2x3Ci8yR3BqNUozNTZoUjBkdGx6R0Yyc2I0TGpxM0VQR3RTVTVrdUVqQnh0RkIyM2piNXorOHJzcENXdFF3VGMvYlcKYmY0NUQvbGdEdk1ycGk0dEx1S0cwN1FCV0R1d3I3ZitwUWg1LytXVTZwMU9XbGZPQVlhc1c4Ri9xVGFqNks1MwpvYU1ZTzEzQVA1RFJzcEFzTmVPdFNkYkI2QmFCZ2k0bG1PVDV4NUhzdkR4L0VGNEVPN0ozK1IxbU8wQVZ4RE1ECm1ueU5BZ01CQUFHZ0hEQWFCZ2txaGtpRzl3MEJDUWN4RFF3TGNYVmtkRzVuYjJkdk1TRXdEUVlKS29aSWh2Y04KQVFFTEJRQURnZ0VCQUFQM09QMUJBZlBrdkR0V1BtVWRUNElkZ1ltOFNjcXVOZ1J2OEZ2azRWaXdEZW5hWHovRgpETjRHOVdMK0F6aW9WSEVRU3R2NFU1TzYybU1EbHB2aWVuWGIvWHE3cEs2c2FQZmNhV3ZGenN0Z0lqVWw4NDdqCkRyaS8xNFArKyttRFplTGNNTWlHcnNYOHp6UXRTbTBRS1JCaXg3SEVGSnhaTmEycTZGNVNvRHU5N2RYbUxjNVkKdVFlTXZmYTNsNDRxb20yblRNVmFvdWV2WUFablZkNndIcDltbGJCK2E3TVRDazhIVTRDMUZFcXU3RnMvRmk3bAp0VGJtc1hEemZ6WFNOTUgyVlNYOVZ5TU1nbGZtcVRKWUR4TVFPb3hUOVBQOVg3eEdaZDRGK0pYbXU0Vll2a0lYCjl6ZHM0U2hRdHB6WnczbVQ3NHI3Z1VrbXB4MGdJOWFyU1pBPQotLS0tLUVORCBDRVJUSUZJQ0FURSBSRVFVRVNULS0tLS0K
  signerName: kubernetes.io/kube-apiserver-client
  expirationSeconds: 86400  # one day
  usages:
  - client auth
EOF
```

요청을 진행하고 나면 아래와 같이 Pending(승인이 안된상태) 상태의 test이라는 이름의 서명요청이 생성되었다.  
```bash
kubectl get csr
##Print
NAME       AGE   SIGNERNAME                            REQUESTOR       REQUESTEDDURATION   CONDITION
devuser   3s    kubernetes.io/kube-apiserver-client   minikube-user   24h                 Pending
```

아래 커맨드를 통해 승인을 진행한다.  
```bash
kubectl certificate approve devuser
```

csr을 확인해보면 Pending상태가 Approved, Issued 된 것을 볼 수 있다.  
승인이 되고 발급까지 진행이 된 것이다.  
```bash
k get csr
##Print
NAME       AGE   SIGNERNAME                            REQUESTOR       REQUESTEDDURATION   CONDITION
devuser   23s   kubernetes.io/kube-apiserver-client   minikube-user   24h                 Approved,Issued
```


아래 커맨드를 통해 devuser의 certificate값을 확인할 수 있다.  
```bash
kubectl get csr devuser -o yaml
```
```yaml
##Print
apiVersion: certificates.k8s.io/v1
kind: CertificateSigningRequest
metadata:
  annotations:
    kubectl.kubernetes.io/last-applied-configuration: |
      {"apiVersion":"certificates.k8s.io/v1","kind":"CertificateSigningRequest","metadata":{"annotations":{},"name":"devuser"},"spec":{"expirationSeconds":86400,"request":"LS0tLS1CRUdJTiBDRVJUSUZJQ0FURSBSRVFVRVNULS0tLS0KTUlJQ3ZqQ0NBYVlDQVFBd1hURUxNQWtHQTFVRUJoTUNTMUl4RlRBVEJnTlZCQWNNREVSbFptRjFiSFFnUTJsMAplVEVjTUJvR0ExVUVDZ3dUUkdWbVlYVnNkQ0JEYjIxd1lXNTVJRXgwWkRFWk1CY0dBMVVFQXd3UWQzZDNMblJsCmMzUjFjMlZ5TG1OdmJUQ0NBU0l3RFFZSktvWklodmNOQVFFQkJRQURnZ0VQQURDQ0FRb0NnZ0VCQU1ueWw3eFgKdk5Kdk1Ec3BwM0xZUGZIZndCZVl3VzFqVFIzYlpCOEJRdXF0eGJTWlpWWUJIUGhMTzB6MzFnS2ZRN0ZsK2FZRwpNTi9WVDdaUWVHc0lrc2lzNFZkOWd3dnA1Vms4ZENKTmJlODRreERHckUrRlIxMkF1ZGdrUnRudW1xaS9EelVyCnJRQkZxR3JPQ1hEL1lhbVBrbmZucUZIUjIyWE1ZWGF4dmd1T3JjUThhMUpUbVM0U01IRzBVSGJlTnZuUDd5dXkKa0phMURCTno5dFp0L2prUCtXQU84eXVtTGkwdTRvYlR0QUZZTzdDdnQvNmxDSG4vNVpUcW5VNWFWODRCaHF4Ygp3WCtwTnFQb3JuZWhveGc3WGNBL2tOR3lrQ3cxNDYxSjFzSG9Gb0dDTGlXWTVQbkhrZXk4UEg4UVhnUTdzbmY1CkhXWTdRQlhFTXdPYWZJMENBd0VBQWFBY01Cb0dDU3FHU0liM0RRRUpCekVOREF0eGRXUjBibWR2WjI4eElUQU4KQmdrcWhraUc5dzBCQVFzRkFBT0NBUUVBbFNwVDE2cHI3MGduSi82SklVK1h1RVR1ZHA5dllrYlJ5a1g2cm1ZMwpRSi9UNDk4K0NWTEtkQmtMWEdNSjRyZzMvbXRSZmdBbERpSUVMT3lSWnFiMGpvWW9UTjhPR0NNbjhqVkxoYVNVClBSNGhNWndSdy9hRTJsU2Zna2FUbXdLRVllTXFZSUVvTmVhQzlJcTRYbEV1VVhVOVlTcCtra2E2VmZ3YWc2Yk0KSk1FYWVmUTdjeXR5ZkRzMGhuOU91NlJ6WTF5QUxEK2dvRWJQSzNaOEdPbWcvWkRnZ3AwMmhsMlNPbVlhVnpDOQppakNHU3lnSW4xKzEvVEp4dEZSdlZ1SW9qZzVVeWhQWndBUDdza1BBOXFSdmlDSloyb3dFamFmYjVIQzlza1d1ClNGRTQvZWd6b1lENlRUSFV0K3R0MzRidGZpZzJPQmFyNnZlUmZMZGxFVG1IK2c9PQotLS0tLUVORCBDRVJUSUZJQ0FURSBSRVFVRVNULS0tLS0K","signerName":"kubernetes.io/kube-apiserver-client","usages":["client auth"]}}
  creationTimestamp: "2022-07-14T04:56:25Z"
  name: devuser
  resourceVersion: "421568"
  uid: cb4009cb-485e-4ca3-b975-4b78db0fd177
spec:
  expirationSeconds: 86400
  groups:
  - system:masters
  - system:authenticated
  request: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURSBSRVFVRVNULS0tLS0KTUlJQ3ZqQ0NBYVlDQVFBd1hURUxNQWtHQTFVRUJoTUNTMUl4RlRBVEJnTlZCQWNNREVSbFptRjFiSFFnUTJsMAplVEVjTUJvR0ExVUVDZ3dUUkdWbVlYVnNkQ0JEYjIxd1lXNTVJRXgwWkRFWk1CY0dBMVVFQXd3UWQzZDNMblJsCmMzUjFjMlZ5TG1OdmJUQ0NBU0l3RFFZSktvWklodmNOQVFFQkJRQURnZ0VQQURDQ0FRb0NnZ0VCQU1ueWw3eFgKdk5Kdk1Ec3BwM0xZUGZIZndCZVl3VzFqVFIzYlpCOEJRdXF0eGJTWlpWWUJIUGhMTzB6MzFnS2ZRN0ZsK2FZRwpNTi9WVDdaUWVHc0lrc2lzNFZkOWd3dnA1Vms4ZENKTmJlODRreERHckUrRlIxMkF1ZGdrUnRudW1xaS9EelVyCnJRQkZxR3JPQ1hEL1lhbVBrbmZucUZIUjIyWE1ZWGF4dmd1T3JjUThhMUpUbVM0U01IRzBVSGJlTnZuUDd5dXkKa0phMURCTno5dFp0L2prUCtXQU84eXVtTGkwdTRvYlR0QUZZTzdDdnQvNmxDSG4vNVpUcW5VNWFWODRCaHF4Ygp3WCtwTnFQb3JuZWhveGc3WGNBL2tOR3lrQ3cxNDYxSjFzSG9Gb0dDTGlXWTVQbkhrZXk4UEg4UVhnUTdzbmY1CkhXWTdRQlhFTXdPYWZJMENBd0VBQWFBY01Cb0dDU3FHU0liM0RRRUpCekVOREF0eGRXUjBibWR2WjI4eElUQU4KQmdrcWhraUc5dzBCQVFzRkFBT0NBUUVBbFNwVDE2cHI3MGduSi82SklVK1h1RVR1ZHA5dllrYlJ5a1g2cm1ZMwpRSi9UNDk4K0NWTEtkQmtMWEdNSjRyZzMvbXRSZmdBbERpSUVMT3lSWnFiMGpvWW9UTjhPR0NNbjhqVkxoYVNVClBSNGhNWndSdy9hRTJsU2Zna2FUbXdLRVllTXFZSUVvTmVhQzlJcTRYbEV1VVhVOVlTcCtra2E2VmZ3YWc2Yk0KSk1FYWVmUTdjeXR5ZkRzMGhuOU91NlJ6WTF5QUxEK2dvRWJQSzNaOEdPbWcvWkRnZ3AwMmhsMlNPbVlhVnpDOQppakNHU3lnSW4xKzEvVEp4dEZSdlZ1SW9qZzVVeWhQWndBUDdza1BBOXFSdmlDSloyb3dFamFmYjVIQzlza1d1ClNGRTQvZWd6b1lENlRUSFV0K3R0MzRidGZpZzJPQmFyNnZlUmZMZGxFVG1IK2c9PQotLS0tLUVORCBDRVJUSUZJQ0FURSBSRVFVRVNULS0tLS0K
  signerName: kubernetes.io/kube-apiserver-client
  usages:
  - client auth
  username: minikube-user
status:
  certificate: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURRakNDQWlxZ0F3SUJBZ0lRZVhXdlR6czhMelZNdlpEdUZEUzFwekFOQmdrcWhraUc5dzBCQVFzRkFEQVYKTVJNd0VRWURWUVFERXdwdGFXNXBhM1ZpWlVOQk1CNFhEVEl5TURjeE5EQTBOVEUwTVZvWERUSXlNRGN4TlRBMApOVEUwTVZvd1hURUxNQWtHQTFVRUJoTUNTMUl4RlRBVEJnTlZCQWNUREVSbFptRjFiSFFnUTJsMGVURWNNQm9HCkExVUVDaE1UUkdWbVlYVnNkQ0JEYjIxd1lXNTVJRXgwWkRFWk1CY0dBMVVFQXhNUWQzZDNMblJsYzNSMWMyVnkKTG1OdmJUQ0NBU0l3RFFZSktvWklodmNOQVFFQkJRQURnZ0VQQURDQ0FRb0NnZ0VCQU1ueWw3eFh2Tkp2TURzcApwM0xZUGZIZndCZVl3VzFqVFIzYlpCOEJRdXF0eGJTWlpWWUJIUGhMTzB6MzFnS2ZRN0ZsK2FZR01OL1ZUN1pRCmVHc0lrc2lzNFZkOWd3dnA1Vms4ZENKTmJlODRreERHckUrRlIxMkF1ZGdrUnRudW1xaS9EelVyclFCRnFHck8KQ1hEL1lhbVBrbmZucUZIUjIyWE1ZWGF4dmd1T3JjUThhMUpUbVM0U01IRzBVSGJlTnZuUDd5dXlrSmExREJOego5dFp0L2prUCtXQU84eXVtTGkwdTRvYlR0QUZZTzdDdnQvNmxDSG4vNVpUcW5VNWFWODRCaHF4YndYK3BOcVBvCnJuZWhveGc3WGNBL2tOR3lrQ3cxNDYxSjFzSG9Gb0dDTGlXWTVQbkhrZXk4UEg4UVhnUTdzbmY1SFdZN1FCWEUKTXdPYWZJMENBd0VBQWFOR01FUXdFd1lEVlIwbEJBd3dDZ1lJS3dZQkJRVUhBd0l3REFZRFZSMFRBUUgvQkFJdwpBREFmQmdOVkhTTUVHREFXZ0JTOFBaei8wZitMN2tnbnlLZyszSVhoVGt1dzN6QU5CZ2txaGtpRzl3MEJBUXNGCkFBT0NBUUVBUkt5RE83NmFXbHovOHpuajd5LzFBTy9mZkhQNEhWa2UrNVJIcXdVU1NXTEdvUjYrYm9VOVBjdUgKZVl2VitlMmdhbk9VYkIxVWUxemtpZTdmaVA1eXZmZ0ZFN0NDdjYrT28wRUFvdU9nVytXM09VT3ZQMm5nbER0NQp0NitFcVBXb2E3OWtzWjQ4cTg0THVtMDlGckh2MGpybXA2bUNjM0kyeGJZMGtHUnR0MEhJZnpqdUVnZlh5SjB0CjJQWUZRM2syKy9SY2lpM05pTUZkZFJGY01MNEFuM0E1UkNqb2xGVFJTVGNSd2s0b3gwWGMyN1owV3ZsVFNNSWsKTXBXUkFTbVltaGE1M0NwL3ZKQUp2WUdPOVVDRXdZSDhhK0JpLzdGUFhlbkNaQlVZaWliR1VuYS91eVB0aUJUMQo4UkIvNU8velI5bTFHdGkvUmpyTzRFTFpQcHphc0E9PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==
  conditions:
  - lastTransitionTime: "2022-07-14T04:56:41Z"
    lastUpdateTime: "2022-07-14T04:56:41Z"
    message: This CSR was approved by kubectl certificate approve.
    reason: KubectlApprove
    status: "True"
    type: Approved
```


아래 커맨드를 통해 certificate 파일을 생성한다.  
```bash
kubectl get csr devuser -o jsonpath='{.status.certificate}'| base64 -d > devuser.crt
```

devuser.crt파일이 생성된 것을 확인 할 수 있다.  
```bash
ls -l
##Print
total 12
-rw-rw-r-- 1 minikube minikube 1192 Jul 14 05:00 devuser.crt
-rw-rw-r-- 1 minikube minikube 1029 Jul 14 04:56 devuser.csr
-rw-rw-r-- 1 minikube minikube 1679 Jul 14 04:55 devuser.key
```
<br>


### 1.3 Authorization  
dev namespace에 대해서만 파드의 reader권한 있는 role을 생성한다.  
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: dev
  name: pod-reader
rules:
- apiGroups: [""] # "" indicates the core API group
  resources: ["pods"]
  verbs: ["get", "watch", "list"]
```

위 에서 dev namespace에 대해서 조회 role을 devuser와 binding을 해준다.  
```yaml
apiVersion: rbac.authorization.k8s.io/v1
# This role binding allows "jane" to read pods in the "default" namespace.
# You need to already have a Role named "pod-reader" in that namespace.
kind: RoleBinding
metadata:
  name: read-pods
  namespace: dev
subjects:
# You can specify more than one "subject"
- kind: User
  name: devuser # "name" is case sensitive
  apiGroup: rbac.authorization.k8s.io
roleRef:
  # "roleRef" specifies the binding to a Role / ClusterRole
  kind: Role #this must be Role or ClusterRole
  name: pod-reader # this must match the name of the Role or ClusterRole you wish to bind to
  apiGroup: rbac.authorization.k8s.io
```

이렇게 되면 devuser는 dev namespace에 있는 pod만 조회가 가능해야 한다.  

<br>


### 1.4 Add to kubeconfig 

```bash
kubectl config set-credentials devuser --client-key=devuser.key --client-certificate=devuser.crt --embed-certs=true

kubectl config set-context devuser --cluster=minikube --user=devuser

kubectl config use-context devuser
```

커맨드를 통해 devuser의 context가 생성된 것을 확인할 수 있다.  
```bash
k config get-contexts
##Print
CURRENT   NAME       CLUSTER    AUTHINFO   NAMESPACE
          minikube   minikube   minikube   dev
*         devuser    minikube   devuser
```
<br>


### 1.5 Check
정상 확인을 위해 get pod 커맨드를 확인 해보면 아래와 같이 전체 클러스터의 파드를 조회하고자 하면 오류가 발생한다.  
인증은 되었지만 권한이 없어 오류가 발생한 부분이다.  
```bash
k get pods -A
##Print
Error from server (Forbidden): pods is forbidden: User "testuser" cannot list resource "pods" in API group "" at the cluster scope
```

dev namespace의 pod에 대해서 정상 조회가 가능하다.  
```bash
k get pods -n dev
##Print
NAME                                READY   STATUS    RESTARTS   AGE
nginx-deployment-544dc8b7c4-hcn8g   1/1     Running   0          45h
nginx-deployment-544dc8b7c4-rsfkw   1/1     Running   0          45h
```





<br><br><br>
> Ref: https://kubernetes.io/docs/reference/access-authn-authz/certificate-signing-requests/