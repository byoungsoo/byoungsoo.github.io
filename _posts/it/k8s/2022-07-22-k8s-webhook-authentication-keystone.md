---
layout: post
title: "Kubernetes Webhook Token 인증 (Openstack Keystone)"
author: "Bys"
category: k8s
date: 2022-07-20 01:00:00
tags: kubernetes authentication kubeadm k8s-keystone-auth
---

# Kubernetes Authenticating

kubeadm을 통해 클러스터를 구축하였다면 사용자를 인증할 수 있는 방법을 알아보자. (현재 시점 kubernetes version 1.24)  

지난 번에는 Webhook인증에 대해서 알아보았다. [Authenticating](https://kubernetes.io/docs/reference/access-authn-authz/authentication/)  

- Authentication strategies
  1. 쿠버네티스 클러스터의 CA(Certificate Authority, 인증기관)에 의해 서명된 유효한 인증서를 가지고 있는 경우 (CN=UserName)  
    [Minikube 인증서를 통한 사용자 추가 및 인증/인가](https://byoungsoo.github.io/container/2022/07/14/minikube-crt-auth.html)  
  2. Service Account Tokens  
    [Minikube 토큰을 통한 인증/인가](https://byoungsoo.github.io/container/2022/07/15/minikube-token-auth-copy.html)  
  3. [**Webhook Token Authentication**](https://kubernetes.io/docs/reference/access-authn-authz/authentication/#webhook-token-authentication)  
  4. [**client-go credential plugin**](https://kubernetes.io/docs/reference/access-authn-authz/authentication/#client-go-credential-plugins)  


Webhook 인증에 대한 내용을 기반으로 k8s-keystone-auth를 이용해서 Openstack의 Keystone과 연동할 것이며 client-keystone-auth 프로그램을 이용해서 client인증을 진행할 것이다.  
자세한 내용은 공식문서를 참고한다. [k8s-keystone-auth](https://github.com/kubernetes/cloud-provider-openstack/blob/master/docs/keystone-auth/using-keystone-webhook-authenticator-and-authorizer.md)  

- **k8s-keystone-auth**  
Kubernetes webhook 인증과 인과를 Openstack의 Keystone과 연동하기 위한 방법이다. 

> Kubernetes webhook authentication and authorization for OpenStack Keystone. With k8s-keystone-auth, the Kubernetes cluster administrator only need to know the OpenStack project names or roles, it's up to the OpenStack project admin for user management, as a result, the OpenStack users could have access to the Kubernetes cluster.

- Environment
  - AWS 환경에서 작업을 진행
  - Master: 2대 (AmazonLinux, m5xlarge)
  - Worker: 2대 (AmazonLinux, t3.medium)
  - Openstack Keystone Server: 1대 (AmazonLinux, m5xlarge)


## 1. k8s-keystone-auth webhook server 구성  

`authorization policy(Optional)`  
우리는 인가에 대해서 kubernetes native rbac을 사용할 수도 있고, k8s-keystone-auth의 인가를 사용할 수도 있다. 
kubernetes native rbac을 사용할 경우에는 해당 cm을 배포하지 않아도 된다. 여기서는 하지 않는다. 
이 cm은 추후 kube-apiserver 설정 시 옵션과 같이 적용되는 것으로 보인다.  
```bash
--authorization-webhook-config-file=/etc/kubernetes/webhooks/webhookconfig.yaml
--authorization-mode=Node,RBAC,Webhook
```

- keystone-policy-configmap.yaml  
projects와 roles에는 openstack의 projects, roles정보를 넣어주면 된다.  
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: k8s-auth-policy
  namespace: kube-system
data:
  policies: |
    [
      {
        "users": {
          "projects": ["admin"],
          "roles": ["admin"]
        },
        "resource_permissions": {
          "*/pods": ["get", "list", "watch"]
        }
      }
    ]
```
```bash
kubectl apply -f keystone-policy-configmap.yaml
```
여기서는 건너뛴다.  

<br>

`service certificates`  
k8s-keystone-auth는 보안상의 이유로 HTTPS 서비스를 하며 인증서가 필요하다. 여기서는 self-signed 인증서를 사용한다.  
운영환경에서는 신뢰할 수 있는 발급기관에서 서명한 인증서를 사용하여야 한다.  
```bash
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes -subj /CN=k8s-keystone-auth.kube-system/
kubectl --namespace kube-system create secret tls keystone-auth-certs --cert=cert.pem --key=key.pem
```
<br>

`service account & rbac`  
- keystone-rbac.yaml  
서비스 어카운트를 생성하고 권한을 부여한다.  

  ```yaml
  kind: ClusterRole
  apiVersion: rbac.authorization.k8s.io/v1
  metadata:
    labels:
      k8s-app: k8s-keystone-auth
    name: k8s-keystone-auth
  rules:
    # Allow k8s-keystone-auth to get k8s-auth-policy configmap
  - apiGroups: [""]
    resources: ["configmaps"]
    verbs: ["get", "watch", "list"]
  ---
  apiVersion: rbac.authorization.k8s.io/v1
  kind: ClusterRoleBinding
  metadata:
    name: k8s-keystone-auth
    labels:
      k8s-app: k8s-keystone-auth
  roleRef:
    apiGroup: rbac.authorization.k8s.io
    kind: ClusterRole
    name: k8s-keystone-auth
  subjects:
  - kind: ServiceAccount
    name: k8s-keystone
    namespace: kube-system
  ---
  apiVersion: v1
  kind: ServiceAccount
  metadata:
    name: k8s-keystone
    namespace: kube-system
  ```
  <br>


`deployment k8s-keystone-auth`  
- keystone-deployment.yaml  
이 부분에서 --keystone-url의 값 수정이 필요하다. 해당 부분은 연동할 keystone주소로 사용하면 되며 openstack keystone 설치 후 openstack endpoint list로 나온 주소를 넣어주면 된다.  
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: k8s-keystone-auth
  namespace: kube-system
  labels:
    app: k8s-keystone-auth
spec:
  replicas: 2
  selector:
    matchLabels:
      app: k8s-keystone-auth
  template:
    metadata:
      labels:
        app: k8s-keystone-auth
    spec:
      serviceAccountName: k8s-keystone
      containers:
        - name: k8s-keystone-auth
          image: k8scloudprovider/k8s-keystone-auth:latest
          args:
            - ./bin/k8s-keystone-auth
            - --tls-cert-file
            - /etc/pki/tls.crt
            - --tls-private-key-file
            - /etc/pki/tls.key
            - --policy-configmap-name
            - k8s-auth-policy
            - --keystone-url
            - http://10.20.2.79:5000/v3/
          volumeMounts:
            - mountPath: /etc/pki
              name: certs
              readOnly: true
          ports:
            - containerPort: 8443
      volumes:
      - name: certs
        secret:
          secretName: keystone-auth-certs
```
```bash
openstack endpoint list
##Print
+----------------------------------+-----------+--------------+----------------+---------+-----------+------------------------------------------+
| ID                               | Region    | Service Name | Service Type   | Enabled | Interface | URL                                      |
+----------------------------------+-----------+--------------+----------------+---------+-----------+------------------------------------------+
| 17163e018068492290458dfeca512203 | RegionOne | keystone     | identity       | True    | admin     | http://10.20.2.79:5000/v3/               |
| dab859b3777f4a1e8045e8267dbbf560 | RegionOne | keystone     | identity       | True    | public    | http://10.20.2.79:5000/v3/               |
| eba0a3aade384f3d9f3b987407f8acae | RegionOne | keystone     | identity       | True    | internal  | http://10.20.2.79:5000/v3/               |
+----------------------------------+-----------+--------------+----------------+---------+-----------+------------------------------------------+
```
<br>


`service k8s-keystone-auth`  
- keystone-service.yaml  

  ```yaml
  kind: Service
  apiVersion: v1
  metadata:
    name: k8s-keystone-auth-service
    namespace: kube-system
  spec:
    selector:
      app: k8s-keystone-auth
    ports:
      - protocol: TCP
        port: 8443
        targetPort: 8443
  ```
  <br>


## 2. k8s-keystone-auth webhook server 테스트  

`openstack token issue`  
openstack 서버로 가서 token을 발급 받는다.  
```bash
openstack token issue
+------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| Field      | Value                                                                                                                                                                                   |
+------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
| expires    | 2022-07-27T06:06:16+0000                                                                                                                                                                |
| id         | gAAAAABi4MfIzdQ4yrA2o7HmOpt73qz5FAh_GQOPN64E0LiDDAyJLjATHOTRguxhS7OlUIVJMm-GzRd37Ay5dL7sxRfJq3UIcgJxA1fJNVzrN22mLnwTlODhAD2IGNNzEVwG-M9C6CtX65v0M5_fNs-bpGJZ5YNq6XaKHBXjpLgwt4yJ4oHmMaw |
| project_id | 7d3ac9731b5d40c78d1966d48b4122a0                                                                                                                                                        |
| user_id    | a6f69f35144f4cce81ca03c70dfc438d                                                                                                                                                        |
+------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+
```
<br>

`Authentication`  
```bash
token=gAAAAABi4MfIzdQ4yrA2o7HmOpt73qz5FAh_GQOPN64E0LiDDAyJLjATHOTRguxhS7OlUIVJMm-GzRd37Ay5dL7sxRfJq3UIcgJxA1fJNVzrN22mLnwTlODhAD2IGNNzEVwG-M9C6CtX65v0M5_fNs-bpGJZ5YNq6XaKHBXjpLgwt4yJ4oHmMaw

kubectl run curl --rm -it --restart=Never --image curlimages/curl -- \
  -k -XPOST https://k8s-keystone-auth-service.kube-system:8443/webhook -d '
{
  "apiVersion": "authentication.k8s.io/v1beta1",
  "kind": "TokenReview",
  "metadata": {
    "creationTimestamp": null
  },
  "spec": {
    "token": "'$token'"
  }
}'
```
정상 호출이된다면 아래와 같이 출력된다. webhook token인증시 살펴봤던 status데이터에 authenticated값이 true로 넘어오면서 token을 발급받은 username: "admin"이 넘어왔다. 
또한 그 외에도 프로젝트 정보 도메인 정보 등이 모두 같이 넘어왔다.  
```json
{
  "apiVersion": "authentication.k8s.io/v1beta1",
  "kind": "TokenReview",
  "metadata": {
    "creationTimestamp": null
  },
  "spec": {
    "token": "gAAAAABi4Mr2bXOtIfsi3uFYzbQZt8t52yafK8dZJcVqClFpfirTDPIDCqv7aPKngDadW-Tp-PrtOwpNIQy-I-milBhQQpUIzllRNCsFEOzN1_Sj7DHa-uXDixYGC3FyK2tkpFT2IuPR3d7G2xY2cO69CA-NG3pg0OKdz0Jsgx043KrgK5GtaJU"
  },
  "status": {
    "authenticated": true,
    "user": {
      "username": "admin",
      "uid": "a6f69f35144f4cce81ca03c70dfc438d",
      "groups": [
        "7d3ac9731b5d40c78d1966d48b4122a0"
      ],
      "extra": {
        "alpha.kubernetes.io/identity/project/id": [
          "7d3ac9731b5d40c78d1966d48b4122a0"
        ],
        "alpha.kubernetes.io/identity/project/name": [
          "admin"
        ],
        "alpha.kubernetes.io/identity/roles": [
          "reader",
          "admin",
          "member"
        ],
        "alpha.kubernetes.io/identity/user/domain/id": [
          "default"
        ],
        "alpha.kubernetes.io/identity/user/domain/name": [
          "Default"
        ]
      }
    }
  }
}
```
token에 임의의 값을 넣고 실패한 결과를 받아보면 아래와 같이 status.authenticated가 false로 넘어왔음을 알 수 있다.  
```json
{
  "apiVersion": "authentication.k8s.io/v1beta1",
  "kind": "TokenReview",
  "metadata": {
    "creationTimestamp": null
  },
  "spec": {
    "token": "gAAAAABi4M0cKoPwc7VMpfEZdzdNUJt3rr1PISpiazAAVYr7FDhOEIzt2aqQGpamuEW1EOT5H6JAD2z22gH9CJy7SGb81Vag3KV9UssW-ObapTSyndqyYJXSoK2uGDyViHXtaaysurM8Mf9Mtu_Nh04-YmadeK9VLlPpc7TPaBva2pOVMshlN"
  },
  "status": {
    "authenticated": false,
    "user": {
      "username": "",
      "uid": "",
      "groups": null,
      "extra": null
    }
  }
}
```
<br>


## 3. Kubernetes kube-apiserver 설정  

`Webhook config`  
- /etc/kubernetes/webhooks/webhookconfig.yaml  
kube-apiserver에서 authentication-token-webhook-config-file 사용할 webhookconfig파일을 만든다.  
여기서 server의 주소는 kubectl get svc k8s-keystone-auth-service -n kube-system으로 조회해서 나오는 CLUSTER-IP를 사용한다.  

  ```yaml
  apiVersion: v1
  kind: Config
  preferences: {}
  clusters:
    - cluster:
        insecure-skip-tls-verify: true
        # ClusterIP of k8s-keystone-auth service!!
        server: https://10.96.193.242:8443/webhook
      name: webhook
  users:
    - name: webhook
  contexts:
    - context:
        cluster: webhook
        user: webhook
      name: webhook
  current-context: webhook
  ```
<br>


`config kube-apiserver`  
- /etc/kubernetes/manifests/kube-apiserver.yaml  

  ```bash
  spec:
    containers:
    - command:
      - kube-apiserver
      - --advertise-address=10.20.1.232
      - --allow-privileged=true
      - --authorization-mode=Node,RBAC
      - --tls-cert-file=/etc/kubernetes/pki/apiserver.crt
      - --tls-private-key-file=/etc/kubernetes/pki/apiserver.key
      # 추가
      - --authentication-token-webhook-config-file=/etc/kubernetes/webhooks/webhookconfig.yaml
  ```

  ```bash
  # Optional 설정 
  # 여기서는 Kubernetes RBAC을 사용할 것이므로 설정하지 않는다. 
  --authorization-webhook-config-file=/etc/kubernetes/webhooks/webhookconfig.yaml
  --authorization-mode=Node,RBAC,Webhook
  ```

  volumeMounts 설정을 하지 않으면 위에서 추가한 webhookconfig.yaml파일을 읽지 못한다. 
  ```bash
  containers:
  ...
    volumeMounts:
    ...
    - mountPath: /etc/kubernetes/webhooks
      name: webhooks
      readOnly: true
  volumes:
  ...
  - hostPath:
      path: /etc/kubernetes/webhooks
      type: DirectoryOrCreate
    name: webhooks
  ```
설정을 적용하고 kube-apiserver.yaml파일을 저장하면 kube-apiserver가 재기동 하는 것을 볼 수 있다.  
<br>

## 4. Client(kubectl) Configuration  
Webhook token인증 시 client-keystone-auth를 사용하여 exec mode로 token을 받아와 처리한다. [clientkubectl-configuration](https://github.com/kubernetes/cloud-provider-openstack/blob/master/docs/keystone-auth/using-keystone-webhook-authenticator-and-authorizer.md#clientkubectl-configuration)  
> The recommended way of client authentication is to use exec mode with the client-keystone-auth binary.

`install client-keystone-auth`  
최신 버전을 받기 위해서는 아래의 스크립트를 사용하라고 되어 있지만 실제로 해당 스크립트대로 실행하면 client-keystone-auth를 받을 때 404오류가 나온다.  
```bash
repo=kubernetes/cloud-provider-openstack
version=$(curl --silent "https://api.github.com/repos/${repo}/releases/latest" | grep '"tag_name":' | awk -F '"' '{print $4}')
curl -L https://github.com/kubernetes/cloud-provider-openstack/releases//download/${version}/client-keystone-auth -o ~/client-keystone-auth
sudo chmod u+x ~/client-keystone-auth
```
때문에 직접 검색을 통해 client-keystone-auth의 가장 최신 파일을 검색해보면 현재기준으로는 아래의 파일이다.  
```
wget https://github.com/kubernetes/cloud-provider-openstack/releases/download/v1.18.0/client-keystone-auth
sudo chmod u+x ~/client-keystone-auth
mv client-keystone-auth /usr/local/bin/
```
<br>

`config파일 수정`  
user정보에 아래와 같이 정보를 추가한다.  
env에는 openstack keystone에서 인증받는데 필요한 정보를 입력한다.  
```yaml
apiVersion: v1
clusters:
- cluster:
    certificate-authority-data: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUMvakNDQWVhZ0F3SUJBZ0lCQURBTkJna3Foa2lHOXcwQkFRc0ZBREFWTVJNd0VRWURWUVFERXdwcmRXSmwKY201bGRHVnpNQjRYRFRJeU1EY3lOekEwTWpnMU5Wb1hEVE15TURjeU5EQTBNamcxTlZvd0ZURVRNQkVHQTFVRQpBeE1LYTNWaVpYSnVaWFJsY3pDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnRVBBRENDQVFvQ2dnRUJBTTIzCnVkNnRmL1VRenY2ZStiTGdjUDJDUU9IMmlma25mL1VvNUtUKzBJQTUyYjZyUFdnblZGUHFOQkZ2V2t3SHIwMDkKcnN2MXFmekZyclhoUE1lMGxWUTVYczE0aVBKU3JEamlrcjUxd2ZTMVhhQ2pudVlLdWNRQ3dYTWx1M3hGVExQbwpyV3J0OGJKeFlPblpKRXlYYW5qQkNCZCtLSncwbjRkMjY2U2lNS3B1dXRTMEk5M296VkE1Z3JOR0NmT1ZOVVpsCjRvNm02UkZ0TXFhVFJpNWRFOVNSYWczbmViZTZUVFhlYnNFY255UE5OTTg5RllrMTJYNzNiUUo2VEE4d2Vrc3cKdUMrUFlucmd2MW9jbnlTNkpaYmcxUVI5WTg4WGVsNnpNWVhSc0xwUk1wWFptek84OWxvTTJrZjl2ckI2TGRUTApUcm5xZmk4TVpvUlNaZVhHaEFrQ0F3RUFBYU5aTUZjd0RnWURWUjBQQVFIL0JBUURBZ0trTUE4R0ExVWRFd0VCCi93UUZNQU1CQWY4d0hRWURWUjBPQkJZRUZEMkdlUGxHcmM5ZXl0Y2dLMVNURm5EZDFTTW5NQlVHQTFVZEVRUU8KTUF5Q0NtdDFZbVZ5Ym1WMFpYTXdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBSXB3eld5SVNlUjlHV2NEY011RgpFN25MSGp5bmp3TFRMR2lEeUJ2bk5IUXBacGJHOWFJcm1kM0NDNDUycGwvUnFHMldQd1o0T1ZmeWEvc3lZWjdvCnRVbXJhcTFoeXlra1RxdXAyWWhTNFVsZlFQSXBtOTJaNnp4S0dhR25Gb2wrUUhIbVdQUElyVVp6bHZvOXhwSDMKbkwrVGZtU05FYjY3UXJiaTdVeTRkU29TUWxPY2hraUJTMVMyZm5YR0k0QUhsK3B6OTlkdmcycitlWnBNUUVSRAoybUFnNWk2TFNCeEx6VXVtM2R2a0ZERGVPN3FZT3FodW9MNkx1SmRxTE1ZaGdvVlF5dmpZdmttZGprbDR3RDBiCkdVa3JpaUJ2WGFkb1FxQU1mSDN5RTFORDFDd0s3WE9zVWhvL05PV09IajlQQ0VLWkFQbmlUMmRreEd4SmJxa2UKMk1vPQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==
    server: https://nlb-kube-master-a0dca3b259bf3238.elb.ap-northeast-2.amazonaws.com:6443
  name: kubernetes
- context:
    cluster: kubernetes
    user: openstackuser
  name: openstackuser@kubernetes
- name: openstackuser
  user:
    exec:
      apiVersion: client.authentication.k8s.io/v1beta1
      args: null
      command: client-keystone-auth
      env:
      - name: OS_AUTH_URL
        value: http://10.20.2.79:5000/v3/
      - name: OS_DOMAIN_NAME
        value: default
      - name: OS_PROJECT_NAME
        value: admin
      - name: OS_USERNAME
        value: admin
      - name: OS_PASSWORD
        value: password
      interactiveMode: IfAvailable
      provideClusterInfo: false
```
<br>

이제 kubectl 커맨드를 확인해본다. 현재는 admin 유저에게 아무런 권한이 없어서 클러스터내 리소스를 조회하지 못하고 있다.  
```bash
kubectl get all -A
##Print
Error from server (Forbidden): pods is forbidden: User "admin" cannot list resource "pods" in API group "" at the cluster scope
Error from server (Forbidden): replicationcontrollers is forbidden: User "admin" cannot list resource "replicationcontrollers" in API group "" at the cluster scope
Error from server (Forbidden): services is forbidden: User "admin" cannot list resource "services" in API group "" at the cluster scope
Error from server (Forbidden): daemonsets.apps is forbidden: User "admin" cannot list resource "daemonsets" in API group "apps" at the cluster scope
Error from server (Forbidden): deployments.apps is forbidden: User "admin" cannot list resource "deployments" in API group "apps" at the cluster scope
Error from server (Forbidden): replicasets.apps is forbidden: User "admin" cannot list resource "replicasets" in API group "apps" at the cluster scope
Error from server (Forbidden): statefulsets.apps is forbidden: User "admin" cannot list resource "statefulsets" in API group "apps" at the cluster scope
Error from server (Forbidden): horizontalpodautoscalers.autoscaling is forbidden: User "admin" cannot list resource "horizontalpodautoscalers" in API group "autoscaling" at the cluster scope
Error from server (Forbidden): cronjobs.batch is forbidden: User "admin" cannot list resource "cronjobs" in API group "batch" at the cluster scope
Error from server (Forbidden): jobs.batch is forbidden: User "admin" cannot list resource "jobs" in API group "batch" at the cluster scope
```

따라서 admin user에게 kubernetes rbac을 통해 권한을 부여해본다.  

## 5. RBAC 및 계정  
admin 유저에게 rbac을 통해 권한을 부여하고 조회에 관한 테스트를 진행해본다.  

- openstack-role.yaml  
클러스터 전체에 파드만 조회할 수 있는 권한을 부여했다.  

  ```yaml
  apiVersion: rbac.authorization.k8s.io/v1
  kind: ClusterRole
  metadata:
    # "namespace" omitted since ClusterRoles are not namespaced
    name: openstack-admin-clusterrole
  rules:
  - apiGroups: ["*"]
    #
    # at the HTTP level, the name of the resource for accessing Secret
    # objects is "secrets"
    resources: ["pods"]
    verbs: ["get", "watch", "list"]
  ```

- openstack-rolebinding.yaml  
  admin 사용자에게 클러스터 전체에 파드만 조회할 수 있는 권한을 바인딩시킨다.  

  ```yaml
  apiVersion: rbac.authorization.k8s.io/v1
  # This cluster role binding allows anyone in the "manager" group to read secrets in any namespace.
  kind: ClusterRoleBinding
  metadata:
    name: openstack-admin-clusterrolebinding
  subjects:
  - kind: User
    name: admin # Name is case sensitive
    apiGroup: rbac.authorization.k8s.io
  roleRef:
    kind: ClusterRole
    name: openstack-admin-clusterrole
    apiGroup: rbac.authorization.k8s.io
  ```

  파드만 조회할 수 있는 권한을 주었기 때문에 pod 외에 리소스는 조회가 안되는 것을 확인할 수 있다.  
  ```bash
  kubectl get all -A
  ##Print
  NAMESPACE     NAME                                        READY   STATUS    RESTARTS       AGE
  default       busybox                                     1/1     Running   1 (40m ago)    160m
  default       dnsutils                                    1/1     Running   2 (37m ago)    157m
  kube-system   calico-kube-controllers-555bc4b957-dfgbw    1/1     Running   6 (93m ago)    161m
  kube-system   calico-node-dw2ht                           1/1     Running   0              161m
  kube-system   calico-node-f782m                           1/1     Running   0              161m
  kube-system   calico-node-tl89p                           1/1     Running   0              161m
  kube-system   calico-node-twmn7                           1/1     Running   0              161m
  kube-system   coredns-6d4b75cb6d-bwxk8                    1/1     Running   0              163m
  kube-system   coredns-6d4b75cb6d-gksnn                    1/1     Running   0              163m
  kube-system   etcd-kube-master-node1                      1/1     Running   10             163m
  kube-system   etcd-kube-master-node2                      1/1     Running   0              162m
  kube-system   k8s-keystone-auth-86ddfc6c7-fjw2t           1/1     Running   0              153m
  kube-system   k8s-keystone-auth-86ddfc6c7-tsdwh           1/1     Running   0              153m
  kube-system   kube-apiserver-kube-master-node1            1/1     Running   1 (76m ago)    82m
  kube-system   kube-apiserver-kube-master-node2            1/1     Running   6 (88m ago)    87m
  kube-system   kube-controller-manager-kube-master-node1   1/1     Running   3 (83m ago)    163m
  kube-system   kube-controller-manager-kube-master-node2   1/1     Running   1 (95m ago)    162m
  kube-system   kube-proxy-8wt9j                            1/1     Running   0              162m
  kube-system   kube-proxy-9tpjn                            1/1     Running   0              163m
  kube-system   kube-proxy-bs74m                            1/1     Running   0              162m
  kube-system   kube-proxy-dftkv                            1/1     Running   0              162m
  kube-system   kube-scheduler-kube-master-node1            1/1     Running   27 (83m ago)   163m
  kube-system   kube-scheduler-kube-master-node2            1/1     Running   16 (95m ago)   162m

  Error from server (Forbidden): replicationcontrollers is forbidden: User "admin" cannot list resource "replicationcontrollers" in API group "" at the cluster scope
  Error from server (Forbidden): services is forbidden: User "admin" cannot list resource "services" in API group "" at the cluster scope
  Error from server (Forbidden): daemonsets.apps is forbidden: User "admin" cannot list resource "daemonsets" in API group "apps" at the cluster scope
  Error from server (Forbidden): deployments.apps is forbidden: User "admin" cannot list resource "deployments" in API group "apps" at the cluster scope
  Error from server (Forbidden): replicasets.apps is forbidden: User "admin" cannot list resource "replicasets" in API group "apps" at the cluster scope
  Error from server (Forbidden): statefulsets.apps is forbidden: User "admin" cannot list resource "statefulsets" in API group "apps" at the cluster scope
  Error from server (Forbidden): horizontalpodautoscalers.autoscaling is forbidden: User "admin" cannot list resource "horizontalpodautoscalers" in API group "autoscaling" at the cluster scope
  Error from server (Forbidden): cronjobs.batch is forbidden: User "admin" cannot list resource "cronjobs" in API group "batch" at the cluster scope
  Error from server (Forbidden): jobs.batch is forbidden: User "admin" cannot list resource "jobs" in API group "batch" at the cluster scope
  ```

  권한을 변경해보자. 모든 리소스에 대한 조회 권한을 주고 다시 적용한다.   

- openstack-role.yaml  
  
  ```yaml
  apiVersion: rbac.authorization.k8s.io/v1
  kind: ClusterRole
  metadata:
    # "namespace" omitted since ClusterRoles are not namespaced
    name: openstack-admin-clusterrole
  rules:
  - apiGroups: ["*"]
    #
    # at the HTTP level, the name of the resource for accessing Secret
    # objects is "secrets"
    resources: ["*"]
    verbs: ["get", "watch", "list"]
  ```

  이제는 모든 리소스가 조회되는 것을 확인 할 수 있다.  
  ```bash
  kubectl get all -A
  ##Print
  NAMESPACE     NAME                                            READY   STATUS    RESTARTS       AGE
  default       pod/busybox                                     1/1     Running   1 (42m ago)    162m
  default       pod/dnsutils                                    1/1     Running   2 (39m ago)    159m
  kube-system   pod/calico-kube-controllers-555bc4b957-dfgbw    1/1     Running   6 (95m ago)    163m
  kube-system   pod/calico-node-dw2ht                           1/1     Running   0              163m
  kube-system   pod/calico-node-f782m                           1/1     Running   0              163m
  kube-system   pod/calico-node-tl89p                           1/1     Running   0              163m
  kube-system   pod/calico-node-twmn7                           1/1     Running   0              163m
  kube-system   pod/coredns-6d4b75cb6d-bwxk8                    1/1     Running   0              165m
  kube-system   pod/coredns-6d4b75cb6d-gksnn                    1/1     Running   0              165m
  kube-system   pod/etcd-kube-master-node1                      1/1     Running   10             165m
  kube-system   pod/etcd-kube-master-node2                      1/1     Running   0              164m
  kube-system   pod/k8s-keystone-auth-86ddfc6c7-fjw2t           1/1     Running   0              155m
  kube-system   pod/k8s-keystone-auth-86ddfc6c7-tsdwh           1/1     Running   0              155m
  kube-system   pod/kube-apiserver-kube-master-node1            1/1     Running   1 (77m ago)    84m
  kube-system   pod/kube-apiserver-kube-master-node2            1/1     Running   6 (90m ago)    89m
  kube-system   pod/kube-controller-manager-kube-master-node1   1/1     Running   3 (85m ago)    165m
  kube-system   pod/kube-controller-manager-kube-master-node2   1/1     Running   1 (97m ago)    164m
  kube-system   pod/kube-proxy-8wt9j                            1/1     Running   0              164m
  kube-system   pod/kube-proxy-9tpjn                            1/1     Running   0              165m
  kube-system   pod/kube-proxy-bs74m                            1/1     Running   0              164m
  kube-system   pod/kube-proxy-dftkv                            1/1     Running   0              164m
  kube-system   pod/kube-scheduler-kube-master-node1            1/1     Running   27 (85m ago)   165m
  kube-system   pod/kube-scheduler-kube-master-node2            1/1     Running   16 (97m ago)   164m

  NAMESPACE     NAME                                TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)                  AGE
  default       service/kubernetes                  ClusterIP   10.96.0.1       <none>        443/TCP                  165m
  kube-system   service/k8s-keystone-auth-service   ClusterIP   10.96.193.242   <none>        8443/TCP                 155m
  kube-system   service/kube-dns                    ClusterIP   10.96.0.10      <none>        53/UDP,53/TCP,9153/TCP   165m

  NAMESPACE     NAME                         DESIRED   CURRENT   READY   UP-TO-DATE   AVAILABLE   NODE SELECTOR            AGE
  kube-system   daemonset.apps/calico-node   4         4         4       4            4           kubernetes.io/os=linux   163m
  kube-system   daemonset.apps/kube-proxy    4         4         4       4            4           kubernetes.io/os=linux   165m

  NAMESPACE     NAME                                      READY   UP-TO-DATE   AVAILABLE   AGE
  kube-system   deployment.apps/calico-kube-controllers   1/1     1            1           163m
  kube-system   deployment.apps/coredns                   2/2     2            2           165m
  kube-system   deployment.apps/k8s-keystone-auth         2/2     2            2           155m

  NAMESPACE     NAME                                                 DESIRED   CURRENT   READY   AGE
  kube-system   replicaset.apps/calico-kube-controllers-555bc4b957   1         1         1       163m
  kube-system   replicaset.apps/coredns-6d4b75cb6d                   2         2         2       165m
  kube-system   replicaset.apps/k8s-keystone-auth-86ddfc6c7          2         2         2       155m
  ```
  <br>

  하지만 마찬가지로 조회권한만 있기 때문에 아래와 같이 생성은 할 수 없다.  
  ```bash
  kubectl apply -f https://k8s.io/examples/application/nginx-with-request.yaml
  ##Print
  Error from server (Forbidden): error when creating "https://k8s.io/examples/application/nginx-with-request.yaml": deployments.apps is forbidden: User "admin" cannot create resource "deployments" in API group "apps" in the namespace "default"
  ```
  <br>

  만약 config파일에 openstack user가 아닌 다른 계정을 넣으면 아래와 같이 token을 return 받을 수 없다. (admin -> admin123)
  ```yaml
  - name: OS_USERNAME
    value: admin1
  ```
  ```bash
  kubectl get po -A
  ##Print
  Invalid user credentials were provided
  Unable to connect to the server: getting credentials: exec plugin didn't return a token or cert/key pair
  ```
  <br>

  openstack의 추가 사용자를 생성하여 인증을 받아본다. 
  사실상 openstack의 role은 kubernetes rbac과는 아무런 상관이 없다. 따라서 단순히 user 인증용으로만 생성한다고 생각하면된다.  
  이는 aws에서도 동일하다.  
  ```bash
  openstack user create --domain default --project admin --password password openstack-reader-user
  openstack role add --user openstack-reader-user --project admin reader
  ```

- ~/.kube/config  

  ```yaml
  - name: openstackuser
    user:
      exec:
        apiVersion: client.authentication.k8s.io/v1beta1
        args: null
        command: client-keystone-auth
        env:
        - name: OS_AUTH_URL
          value: http://10.20.2.79:5000/v3/
        - name: OS_DOMAIN_NAME
          value: default
        - name: OS_PROJECT_NAME
          value: admin
        - name: OS_USERNAME
          value: openstack-reader-user
        - name: OS_PASSWORD
          value: password
        interactiveMode: IfAvailable
        provideClusterInfo: false
  ```

  리소스 조회시 마찬가지로 인증은 되었지만 권한 없음이 나온다. 위와 같이 rbac을 통해 권한 설정 후 진행하면 된다.  
  이로써 신규 계정을 추가하여 openstack의 인증을 받고 권한은 kubernetes rbac을 통해 컨트롤 하는 방법을 테스트해봤다.  
  ```bash
  kubectl get po -A
  ##Print
  Error from server (Forbidden): pods is forbidden: User "openstack-reader-user" cannot list resource "pods" in API group "" at the cluster scope

  kubectl get po -n kube-system
  ##Print
  Error from server (Forbidden): pods is forbidden: User "openstack-reader-user" cannot list resource "pods" in API group "" in the namespace "kube-system"
  ```
  
<br>


## 6. 정리  
정리를 하면 k8s-keystone-auth를 통해 kubernetes webhook token authentication을 통해 openstack keystone과 연동을 시켰다.  

그리고 client(kubectl)쪽에서는 keystone의 유저정보를 사용하여 exec mode로 토큰을 얻고 유효한 토큰임을 keystone에 인증을 받는 과정을 거쳤다.  
이 때 사용자는 모든 권한을 Kubernetes의 RBAC을 통해 부여 받는다. 따라서 적절한 user에게 role을 설정하여 주입한다.  
<br>

## 7. TroubleShooting  
여기까지 구성하는데 꽤 많은 시간을 소비하였고 헛짓도 많이 하는 바람에 kubeadm reset을 시키고 다시 구성하는 작업만 4~5번을 반복한 것 같다.  
먼저 k8s-keystone-auth의 문제가 아니었음에도 어딘지를 몰라서 많이 찾았다.  

### 7.1 calico cni  
먼저 cluster 내부에서 정상적으로 dns lookup이 되지 않았다. 또한 pod -> pod 간 ping이 안되는 구간이 있었다.  
테스트를 위해 아래 스크립트를 호출했을 때 k8s-keystone-auth-service.kube-system 룩업이 되지 않았다.  
이유는 dns 서비스에 정상적으로 접근할 수 없었기 때문이다.  
```bash
kubectl run curl --rm -it --restart=Never --image curlimages/curl -- \
-k -XPOST https://k8s-keystone-auth-service.kube-system:8443/webhook -d '
{
  "apiVersion": "authentication.k8s.io/v1beta1",
  "kind": "TokenReview",
  "metadata": {
    "creationTimestamp": null
  },
  "spec": {
    "token": "'$TOKEN'"
  }
}'
```
[dns-debugging](https://kubernetes.io/docs/tasks/administer-cluster/dns-debugging-resolution/)홈페이지에서 많은 도움을 받았다. 
우선 dnsutils 파드를 배포하여 어떤부분에서 문제가 있었는지를 파악했고, 워커노드쪽에서 우선 AllTraffic - kube-master-node, kube-worker-node 보안그룹을 열고 우선 통신에 대한 확인을 했다.  
이 후에는 정상적으로 dns lookup이 가능하였다. 

### 7.2 unknown apiVersion "authentication.k8s.io/v1"
마지막 test 과정에서 unknown apiVersion 오류가 발생했었다. 지금 생각해보면 당연히 apiVersion이 잘못된 것이었을 것이다. 
그런데 이 때는 위의 네트워크 이슈랑 같이 맞물려서 어떤게 문제인지 제대로 파악하지 못했던 것 같다.  

[![kubeadm_auth_webhook006](/assets/it/k8s/k8s/kubeadm_auth_webhook006.png){: width="80%" height="auto"}](/assets/it/k8s/k8s/kubeadm_auth_webhook006.png)  
[Issue:Missing support for client.authentication.k8s.io/v1](https://github.com/kubernetes-client/java/issues/2290)

네트워크 정상 통신 여부를 모두 확인한 뒤에는 해당 이슈가 나왔을 때 위의 문서들을 찾았다. 결국 공식문서에 나와있던 v1으로는 호출시 오류가 발생하는게 맞고 v1beta1으로 버전을 수정했다.  




---

## 📚 References

[1] **Using client keystone auth**  
- https://github.com/kubernetes/cloud-provider-openstack/blob/master/docs/keystone-auth/using-client-keystone-auth.md

[2] **Using keystone webhook authenticator and authorizer**  
- https://github.com/kubernetes/cloud-provider-openstack/blob/master/docs/keystone-auth/using-keystone-webhook-authenticator-and-authorizer.md

[3] **Debugging DNS Resolution**  
- https://kubernetes.io/docs/tasks/administer-cluster/dns-debugging-resolution/
