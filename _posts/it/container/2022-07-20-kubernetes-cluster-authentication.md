---
layout: post
title: "[Kubeadm] Kubernetes Webhook Token 인증"
author: "Bys"
category: container
date: 2022-07-20 01:00:00
tags: kubernetes authentication kubeadm
---

# Kubernetes Authenticating

kubeadm을 통해 클러스터를 구축하였다면 사용자를 인증할 수 있는 방법을 알아보자. (현재 시점 kubernetes version 1.24)  

Kubernetes 사용자에 대해서 먼저 알아보자. 
쿠버네티스는 일반사용자와 Kubernetes에서 관리하는 Service Account라는 두 가지 카테고리의 사용자가 존재한다. 
쿠버네티스는 일반 사용자 계정을 표현하는 개체가 없으며 API등을 통해서 추가하하는 것도 불가하다. 
하지만 이와 관련하여 유저는 아래와 같은 방법으로 인증이 가능하다. 자세한 내용은 공식문서를 참고한다. [Authenticating](https://kubernetes.io/docs/reference/access-authn-authz/authentication/)  

- Authentication strategies
  1. 쿠버네티스 클러스터의 CA(Certificate Authority, 인증기관)에 의해 서명된 유효한 인증서를 가지고 있는 경우 (CN=UserName)  
    [Minikube 인증서를 통한 사용자 추가 및 인증/인가](https://byoungsoo.github.io/container/2022/07/14/minikube-crt-auth.html)  
  2. Service Account Tokens  
    [Minikube 토큰을 통한 인증/인가](https://byoungsoo.github.io/container/2022/07/15/minikube-token-auth-copy.html)  
  3. [**Webhook Token Authentication**](https://kubernetes.io/docs/reference/access-authn-authz/authentication/#webhook-token-authentication)  
  4. Static Token File
  5. Bootstrap Tokens 
  6. OpenID Connect Tokens
  7. Authenticating Proxy 

여기서는 Webhook을 통해 사용자 인증과정을 살펴본다.  

- **Webhook Token Authentication**  
Webhook 기반의 인증 기법은 다양한 형태의 외부 인증 서버와 연동할 수 있다는 장점을 갖고 있다.  
webhook은 특정 이벤트가 발생했을 때, 사전에 정의된 URL을 호출하여 후속 작업을 처리할 수 있도록 도와주는 방법이다.  
Kubernetes에서는 인증전략 중 하나로 Webhook Token을 통한 인증이 가능하도록 하였다. 
여기서는 별도의 Webhook 인증서버를 구성하여 테스트를 진행할 것이다.  
(클러스터 구축은 모두 완료 된 상태에서 진행 [Kubeadm을 통한 클러스터 구축](https://byoungsoo.github.io/container/2022/07/18/kubernetes-cluster-setup.html))   

- Flow  
[![kubeadm_auth_webhook001](/assets/it/container/kubeadm/kubeadm_auth_webhook001.png){: width="80%" height="auto"}](/assets/it/container/kubeadm/kubeadm_auth_webhook001.png)  

- Environment
  - AWS 환경에서 작업을 진행
  - Master: 2대 (AmazonLinux, m5xlarge)
  - Worker: 2대 (AmazonLinux, t3.medium)
  - Webhook Server: 1대 (AmazonLinux, t3.small)



## 1. Webhook 구성  
우선 Sample이 존재하는 Python flask로 서버를 구성해서 테스트를 하고, 이 후 spring security oauth를 이용해 auth서버를 별도로 구성해서 Java로도 다시 테스트 예정이다.  

### 1.1 Webhook 서버 환경 설정
`Install Python`  
Python이 이미 설치되어 있는지 확인  
```bash
python3 --version
```

Python이 설치되어 있지 않은 경우
```bash
sudo yum install python3
```
<br>

`Install flask`  
Flask는 Python Web Framework이다. Micro Web Framework으로 간단한 웹 사이트 또는 API 서버를 만드는 유용하다.  
여기서는 간단하게 webhook인증서버를 만들기 위해 사용한다.  
```bash
pip3 install flask
```
<br>

`webhook.py`  
해당 소스 코드는 status['authenticated'] = True를 통해 실제로는 항상 인증이 되는 소스코드라고 생각하면 된다.  
tokenReview에 대해서는 잠시 후 더 알아본다. 우선 webhook.py파일을 만들고 해당 소스 코드를 넣어준다.  
```bash
vim webhook.py
```
```python
import pprint
from flask import Flask, request, jsonify
 
app = Flask(__name__)
 
@app.route('/', methods=['POST'])
def auth():
        # API 서버로부터 TokenReview 수신
        tokenReview = request.json

        # 인증 결과 (하드코딩)
        status = {}
        status['authenticated'] = True
        status['user'] = {
                'username': 'testUser',
                'uid': 'testUser',
                'groups': ['system:masters'] 
                 # group 설정을 'system:masters'로 설정하면 해당 user에게 관리자 권한을 부여함
                 # group 설정을 공백 or 설정하지 않으면 해당 username과 동일한 rolebinding 권한을 부여함
        }
 
        # TokenReview에 인증결과 객체 삽입
        tokenReview['status'] = status

        # 출력
        pprint.pprint(tokenReview)
 
        # API 서버로 json 응답
        return jsonify(tokenReview)
 
 
if __name__ == '__main__':
        app.run(host= '0.0.0.0', port=6000, debug=True)
```
```bash
# 서버 실행
python3 webhook.py
##Print
 * Serving Flask app 'webhook' (lazy loading)
 * Environment: production
   WARNING: This is a development server. Do not use it in a production deployment.
   Use a production WSGI server instead.
 * Debug mode: on
 * Running on all addresses (0.0.0.0)
   WARNING: This is a development server. Do not use it in a production deployment.
 * Running on http://127.0.0.1:6000
 * Running on http://10.20.2.254:6000 (Press CTRL+C to quit)
 * Restarting with stat
 * Debugger is active!
 * Debugger PIN: 289-576-382
```
여기까지 진행을 했다면 임시로 테스트할 인증서버는 구성이 완료가 되었다.  
<br>

## 2. Kubernetes Webhook 인증 설정  
이제는 kubernetes 에서 Webhook Token Authentication을 사용할 수 있도록 설정을 변경해야 한다.  

Webhook Token Authentication 방식에서는 사용자가 만약 아래와 같이 kube-apiserver로 요청을 했다면 해당하는 토큰이 정상인지 확인을 받아야 한다. 
원래는 토큰을 발행해준 서버로 요청을 보내 토큰의 정상유무를 확인 받아야 한다. 여기서는 흐름만 알기 위해 flask인증 서버를 통해 유효성 체크를 진행하며 어떤 토큰이 들어와도 무조건 True이다.  
```bash
kubectl get pods -A --token my_token_value
```

자세한 내용은 공식 문서를 참고한다.  
[webhook-token-authentication](https://kubernetes.io/docs/reference/access-authn-authz/authentication/#webhook-token-authentication)

### 2.1 API-Server 설정  
이제는 토큰을 이용해 요청할 경우 api-server가 우리가 구성한 webhook서버로 TokenReview 요청을 할 수 있도록 설정을 변경한다.  

먼저 아래와 동일하게 Config설정을 해준다.  
`webhook.yaml`  
```bash
#신규생성
vim /etc/kubernetes/pki/webhook.yaml
```
```yaml
apiVersion: v1
kind: Config
clusters:
- cluster:
    insecure-skip-tls-verify: true  # http 요청을 통해 진행하므로 생략
    server: http://10.20.2.58:6000  # WebHook server의 주소 및 포트
  name: kubernetes
contexts:
- context:
    cluster: kubernetes
    user: testUser
  name: testContext
current-context: testContext
users:
- name: testUser
```

`api-server.yaml`  
```bash
#기존파일 수정
vim /etc/kubernetes/manifests/kube-apiserver.yaml
```
--authentication-token-webhook-config-file=/etc/kubernetes/pki/webhook.yaml 옵션을 추가 해주어야 한다.  
--authentication-token-webhook-config-file 옵션은 외부의 webhook 서비스에 어떻게 접근 할 건지에 대한 기술이 되어 있는 설정파일이며 설정되는 파일이 위에서 설정한 webhook.yaml파일이다.  
```yaml
apiVersion: v1
kind: Pod
metadata:
  annotations:
    kubeadm.kubernetes.io/kube-apiserver.advertise-address.endpoint: 10.20.2.10:6443
  creationTimestamp: null
  labels:
    component: kube-apiserver
    tier: control-plane
  name: kube-apiserver
  namespace: kube-system
spec:
  containers:
  - command:
    - kube-apiserver
    - --advertise-address=10.20.2.10
    - --allow-privileged=true
    - --authorization-mode=Node,RBAC
    - --client-ca-file=/etc/kubernetes/pki/ca.crt
    - --enable-admission-plugins=NodeRestriction
    - --enable-bootstrap-token-auth=true
    - --etcd-cafile=/etc/kubernetes/pki/etcd/ca.crt
    - --etcd-certfile=/etc/kubernetes/pki/apiserver-etcd-client.crt
    - --etcd-keyfile=/etc/kubernetes/pki/apiserver-etcd-client.key
    - --etcd-servers=https://127.0.0.1:2379
    - --kubelet-client-certificate=/etc/kubernetes/pki/apiserver-kubelet-client.crt
    - --kubelet-client-key=/etc/kubernetes/pki/apiserver-kubelet-client.key
    - --kubelet-preferred-address-types=InternalIP,ExternalIP,Hostname
    - --proxy-client-cert-file=/etc/kubernetes/pki/front-proxy-client.crt
    - --proxy-client-key-file=/etc/kubernetes/pki/front-proxy-client.key
    - --requestheader-allowed-names=front-proxy-client
    - --requestheader-client-ca-file=/etc/kubernetes/pki/front-proxy-ca.crt
    - --requestheader-extra-headers-prefix=X-Remote-Extra-
    - --requestheader-group-headers=X-Remote-Group
    - --requestheader-username-headers=X-Remote-User
    - --secure-port=6443
    - --service-account-issuer=https://kubernetes.default.svc.cluster.local
    - --service-account-key-file=/etc/kubernetes/pki/sa.pub
    - --service-account-signing-key-file=/etc/kubernetes/pki/sa.key
    - --service-cluster-ip-range=10.96.0.0/12
    - --tls-cert-file=/etc/kubernetes/pki/apiserver.crt
    - --tls-private-key-file=/etc/kubernetes/pki/apiserver.key
    # 추가 필요한 부분
    - --authentication-token-webhook-config-file=/etc/kubernetes/pki/webhook.yaml
```
설정을 변경하고 파일을 저장하면 파드가 kube-apiserver 파드가 자동으로 재기동 되는 것을 확인 할 수 있다. 해당 설정은 모든 master노드에서 변경해주어야 한다.  

```bash
kubectl get pods -A -o wide --watch
##Print
NAMESPACE     NAME                                        READY   STATUS    RESTARTS      AGE   IP                NODE                NOMINATED NODE   READINESS GATES
#......생략
kube-system   kube-apiserver-kube-master-node1            1/1     Running   0             46m   10.20.1.232       kube-master-node1   <none>           <none>
kube-system   kube-apiserver-kube-master-node2            1/1     Running   0             34m   10.20.2.10        kube-master-node2   <none>           <none>

##Watch 옵션을 주면 파일 저장 후 잠시 후 파드가 재기동 되는 것을 확인할 수 있다. 
kube-system   kube-apiserver-kube-master-node2            0/1     ContainerCreating   0             35m   10.20.2.10        kube-master-node2   <none>           <none>
kube-system   kube-apiserver-kube-master-node2            0/1     Terminating         0             35m   10.20.2.10        kube-master-node2   <none>           <none>
kube-system   kube-apiserver-kube-master-node2            0/1     Terminating         0             35m   10.20.2.10        kube-master-node2   <none>           <none>
kube-system   kube-apiserver-kube-master-node2            0/1     Pending             0             0s    <none>            kube-master-node2   <none>           <none>
kube-system   kube-apiserver-kube-master-node2            0/1     Running             0             0s    10.20.2.10        kube-master-node2   <none>           <none>
kube-system   kube-apiserver-kube-master-node2            0/1     Running             0             10s   10.20.2.10        kube-master-node2   <none>           <none>
kube-system   kube-apiserver-kube-master-node2            1/1     Running             0             10s   10.20.2.10        kube-master-node2   <none>           <none>
```
<br>

### 2.2 Webhook 인증 확인  

컨피그 파일을 수정하여 token인증이 정상적으로 되는지 확인 해본다.  
`config`
```bash
vim ~/.kube/config
```
- Old Config  
이전에 kubeadm을 통해 클러스터를 구축하고 사용하던 config파일에서 user 정보를 살펴보면 kubernetes-admin 사용자는 클러스터 CA의 서명이 들어간 인증서 데이터를 통해 해당하는 클러스터에 인증을 받고 있다. 
이 부분을 삭제하고 유저 정보만 넣어준다.  

  ```bash
  apiVersion: v1
  clusters:
  - cluster:
      certificate-authority-data: ~~~~~~UEdWOW0KR0ZvPQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==
      server: https://nlb-kube-master-a0dca3b259bf3238.elb.ap-northeast-2.amazonaws.com:6443
    name: kubernetes
  contexts:
  - context:
      cluster: kubernetes
      user: kubernetes-admin
    name: kubernetes-admin@kubernetes
  current-context: kubernetes-admin@kubernetes
  kind: Config
  preferences: {}
  users:
  - name: kubernetes-admin
    user:
      ##해당 부분
      client-certificate-data: ~~~~~~ZWRHU5d0M5NWc9PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==
      client-key-data: ~~~~~~U1BNWFN5T0xmc0FEdEpoMCs1cldFTFEvZFJLdEJRREp1V0VHZzZUd3VXb1MvRmZ0cnlYbUYydwotLS0tLUVORCBSU0EgUFJJVkFURSBLRVktLS0tLQo=
  ```

- New Config  
유저 이름은 아무거나 상관없다. 어차피 여기서는 어떤 token이든 인증이 될 예정이다. 다만 기존에 인증을 해주고 있던 인증서 정보는 모두 삭제한다.  

  ```bash
  apiVersion: v1
  clusters:
  - cluster:
      certificate-authority-data: ~~~~~~UEdWOW0KR0ZvPQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==
      server: https://nlb-kube-master-a0dca3b259bf3238.elb.ap-northeast-2.amazonaws.com:6443
    name: kubernetes
  contexts:
  - context:
      cluster: kubernetes
      user: testUser
    name: testContext
  current-context: testContext
  kind: Config
  preferences: {}
  users:
  - name: testUser
  ```
<br>

이제 testUser는 kubectl --token을 통해 인증이 되는지 확인해보자.  
아래와 같이 --token 값을 전달해주면 정상적으로 인증이 되는 것을 확인 할 수 있다.  
```bash
kubectl get pods -A --token testToken
##Print
NAMESPACE     NAME                                        READY   STATUS    RESTARTS        AGE
kube-system   calico-kube-controllers-555bc4b957-vhzgz    1/1     Running   0               5h43m
kube-system   calico-node-4b5l7                           1/1     Running   0               5h43m
kube-system   calico-node-85prh                           1/1     Running   0               5h43m
kube-system   calico-node-qmll8                           1/1     Running   0               5h43m
kube-system   calico-node-r7dpc                           1/1     Running   0               5h43m
kube-system   coredns-6d4b75cb6d-85svl                    1/1     Running   0               20h
kube-system   coredns-6d4b75cb6d-s42c7                    1/1     Running   0               20h
kube-system   etcd-kube-master-node1                      1/1     Running   3               20h
kube-system   etcd-kube-master-node2                      1/1     Running   3               20h
kube-system   kube-apiserver-kube-master-node1            1/1     Running   0               4h51m
kube-system   kube-apiserver-kube-master-node2            1/1     Running   1 (4h50m ago)   4h57m
kube-system   kube-controller-manager-kube-master-node1   1/1     Running   8 (4h55m ago)   20h
kube-system   kube-controller-manager-kube-master-node2   1/1     Running   4 (4h51m ago)   20h
kube-system   kube-proxy-2px5l                            1/1     Running   0               20h
kube-system   kube-proxy-fz7f7                            1/1     Running   0               20h
kube-system   kube-proxy-n4csn                            1/1     Running   0               20h
kube-system   kube-proxy-qtmqk                            1/1     Running   0               20h
kube-system   kube-scheduler-kube-master-node1            1/1     Running   8 (4h55m ago)   20h
kube-system   kube-scheduler-kube-master-node2            1/1     Running   4 (4h51m ago)   20h
```
또한 인증서버에는 client 요청이 들어올 때마다 로그가 찍히는 것을 확인 할 수 있다.  

[![kubeadm_auth_webhook004](/assets/it/container/kubeadm/kubeadm_auth_webhook004.png){: width="50%" height="auto"}](/assets/it/container/kubeadm/kubeadm_auth_webhook004.png)  

<br>

kubectl 요청시 --token 옵션 없이는 system:anonymous로 인식이 되며 권한이 없어 조회가 되지 않는다.  
```bash
kubectl get pods -A
##Print
Please enter Username: testUser
Please enter Password: Error from server (Forbidden): pods is forbidden: User "system:anonymous" cannot list resource "pods" in API group "" at the cluster scope
```
그런데 여기서 인증은 마치 system:anonymous로 되고 권한이 없어 조회가 되지 않는 것 처럼 보인다.  

해당 내용을 살펴보니 webhook 인증과 같이 다른 인증방식에 의해 처리되지 않은 유저는 system:anonymous 사용자 이름과 system:unauthenticated 그룹이 제공된다고 한다. 
보안상 system:anonymous 유저는 접근이 되지 않도록 하는 것이 좋으며 인증이 되지 않게 하려면 kube-apiserver에 --anonymous-auth=false 옵션을 추가한다.  
자세한 내용은 공식 문서를 참고한다.  [Kubelet authentication](https://kubernetes.io/docs/reference/access-authn-authz/kubelet-authn-authz/#kubelet-authentication)  
`kube-apiserver.yaml`  
```bash
vim /etc/kubernetes/manifests/kube-apiserver.yaml
```
```bash
apiVersion: v1
kind: Pod
metadata:
  annotations:
    kubeadm.kubernetes.io/kube-apiserver.advertise-address.endpoint: 10.20.2.10:6443
  creationTimestamp: null
  labels:
    component: kube-apiserver
    tier: control-plane
  name: kube-apiserver
  namespace: kube-system
spec:
containers:
  - command:
    # 생략
    - --tls-private-key-file=/etc/kubernetes/pki/apiserver.key
    - --authentication-token-webhook-config-file=/etc/kubernetes/pki/webhook.yaml
    ## 추가 
    - --anonymous-auth=false
```
해당 설정을 저장하고 나면 kube-apiserver 파드가 재기동되게 되며 설정을 마친다.  

설정을 마치고 아래와 같이 조회하면 오류 로그가 바뀐 것을 볼 수 있다. 이제는 인증되지 않은 사용자로 나오게 된다.  
```bash
kubectl get pods -A
Please enter Username: testUser
Please enter Password: error: You must be logged in to the server (Unauthorized)
```

하지만 해당 설정을 변경하고 나면 문제가 생긴다. 401 Unauthorized 코드로 kube-apiserver의 Startup probe가 failed 상태로 되었다.  
아마도 익명의 요청을 통해 Startup probe를 진행시키고 있지 않았을까 싶다. 시간이 될 때 더 내용을 들여다 보기로 하고 여기서는 인증을 확인하는 목적이었으니 --anonymous-auth=false 옵션을 다시 빼준다.  
```bash
kubectl describe pod kube-apiserver-kube-master-node1 -n kube-system --token 123
##Print
Events:
  Type     Reason     Age                    From     Message
  ----     ------     ----                   ----     -------
  Normal   Pulled     7m7s                   kubelet  Container image "k8s.gcr.io/kube-apiserver:v1.24.3" already present on machine
  Normal   Created    7m7s                   kubelet  Created container kube-apiserver
  Normal   Started    7m7s                   kubelet  Started container kube-apiserver
  Warning  Unhealthy  117s (x28 over 6m57s)  kubelet  Startup probe failed: HTTP probe failed with statuscode: 401
```
<br>

마지막 확인으로는 Postman을 통해 확인한다.  
kube-apiserver에 요청을 하더라도 우리는 Bearer Token값만 임의로 넣어주게 되면 Webhook 인증을 통해 클러스터의 파드 정보를 조회 할 수 있다.  
[![kubeadm_auth_webhook005](/assets/it/container/kubeadm/kubeadm_auth_webhook005.png){: width="80%" height="auto"}](/assets/it/container/kubeadm/kubeadm_auth_webhook005.png)  

<br>


### 2.3 TokenReview과정 살펴보기 
사용자가 kubectl --token 을 통해 api-server로 요청을 하면 api-server는 webhook인증서버로 TokenReview 객체를 보내게 된다.  

> When a client attempts to authenticate with the API server using a bearer token as discussed above, the authentication webhook POSTs a JSON-serialized TokenReview object containing the token to the remote service.

Authentication webhook은 POST요청으로 TokenReview라는 JSON 객체를 아래와 같이 우리의 인증서버로 보내게 된다.  
```yaml
{
  "kind": "TokenReview",
  "apiVersion": "authentication.k8s.io/v1beta1",
  "metadata": {
    "creationTimestamp": null
  },
  "spec": {
    "token": "mytoken123"
  }
}
```
<br>


먼저 Postman을 통해 요청하는 모습과 응답을 살펴보자.  

[![kubeadm_auth_webhook003](/assets/it/container/kubeadm/kubeadm_auth_webhook003.png){: width="50%" height="auto"}](/assets/it/container/kubeadm/kubeadm_auth_webhook003.png)  

1. client는 kubectl --token을 통해 token값을 보내게 된다. 
2. kube-apiserver의 authentication webhook은 우리의 인증서버로 TokenReview JSON객체를 보내게 되며 거기에는 token값이 들어있다. 
3. 우리의 인증서버는 해당 token값을 받아 유효한 토큰인지 확인 한 후(아래 소스코드는 생략), 다시 TokenReview 객체에 status.authenticated, status.user 정보를 추가하여 return한다.  
  ```python
  #인증서버 소스
  def auth():
        # API 서버로부터 TokenReview 수신
        tokenReview = request.json

        # 인증 결과 (하드코딩)
        status = {}
        status['authenticated'] = True
        status['user'] = {
                'username': 'testUser',
                'uid': 'testUser',
                'groups': ['system:masters'] 
                # group 설정을 'system:masters'로 설정하면 해당 user에게 관리자 권한을 부여함
                # group 설정을 공백 or 설정하지 않으면 해당 username과 동일한 rolebinding 권한을 부여함
        }

        # TokenReview에 인증결과 객체 삽입
        tokenReview['status'] = status

        # 출력
        pprint.pprint(tokenReview)

        # API 서버로 json 응답
        return jsonify(tokenReview)
  ```
4. kube-apiserver에서는 TokenReview + 인증결과(status) 값을 받아 client에게 요청한 응답을 주게 된다.  

따라서 자체 인증서버를 자체 구축하고 Kubernetes에서 Webhook Token 인증을 사용하려고 하면 TokenReview JSON 객체를 받아서 안에 있는 token에 대한 유효성을 체크하고, 
체크가 완료 되었다면 다시 TokenReview객체에 status 데이터를 담아서 TokenReview 객체를 리턴해주는 부분을 구현해주면 된다.  

하지만 Token값을 매번 보내줘야 한다는 점에서 번거로운 부분이 있다. 그래서 다음 번에는 [client-go-credential-plugins](https://kubernetes.io/docs/reference/access-authn-authz/authentication/#client-go-credential-plugins) 방식을 통해서 인증하는 부분을 확인해본다. 이 방식은 Webhook Token 기반으로 진행되는 것으로 보이니 해당 내용을 꼭 숙지한다.  
(아마도 aws, openstack 등을 살펴보니 사용자가 token을 보내주는 부분을 각 서비스의 cli를 통해 IAM, Keystone과 같은 서비스를 사용하여 token을 get하고 그 토큰을 이용하여 webhook token방식으로 인증하는 것이 아닌가 싶다.)  

<br><br><br>

> Ref: [https://kubernetes.io/docs/reference/access-authn-authz/authentication/](https://kubernetes.io/docs/reference/access-authn-authz/authentication/)  
> Ref: [https://kubernetes.io/docs/reference/access-authn-authz/authentication/#webhook-token-authentication](https://kubernetes.io/docs/reference/access-authn-authz/authentication/#webhook-token-authentication)  
> Ref: [https://kubernetes.io/docs/reference/access-authn-authz/kubelet-authn-authz/#kubelet-authentication](https://kubernetes.io/docs/reference/access-authn-authz/kubelet-authn-authz/#kubelet-authentication)  
> Ref: [https://kubernetes.io/docs/reference/access-authn-authz/webhook/](https://kubernetes.io/docs/reference/access-authn-authz/webhook/)  
> Ref: [https://kubernetes.io/docs/reference/access-authn-authz/authentication/#client-go-credential-plugins](https://kubernetes.io/docs/reference/access-authn-authz/authentication/#client-go-credential-plugins)  
> Ref: [https://github.com/kubernetes/cloud-provider-openstack/blob/master/docs/keystone-auth/using-client-keystone-auth.md](https://github.com/kubernetes/cloud-provider-openstack/blob/master/docs/keystone-auth/using-client-keystone-auth.md)  
> Ref: [https://ikcoo.tistory.com/27](https://ikcoo.tistory.com/27)  
> Ref: [https://coffeewhale.com/kubernetes/authentication/webhook/2020/05/05/auth04/](https://coffeewhale.com/kubernetes/authentication/webhook/2020/05/05/auth04/)  