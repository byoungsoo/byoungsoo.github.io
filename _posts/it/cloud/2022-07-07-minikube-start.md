---
layout: post
title: "Minikube 사용하기"
author: "Bys"
category: cloud
date: 2022-07-07 01:00:00
tags: kubernetes minikube network bridge
---

# Minikube

## 1. Minikube
Minikube는 쉽게 배우고 개발할 수 있도록 하는 데 중점을 둔 로컬 Kubernetes이다. Minikube를 이용해서 빠르게 로컬환경에서 쿠버네티스 클러스터를 구축해본다.  
자세한 내용은 Minikube 페이지를 참고한다.  
[Minikube](https://minikube.sigs.k8s.io)  


## 2. 설치 
Minikube는 Linux, Mac 등 다양한 환경에서 구성할 수 있으며 여기서는 AWS Amazon Linux EC2 서버에서 설치를 진행한다.  

### 2.1 Docker설치
Docker를 설치하여 추후 Minikube 기동시 드라이버로 Docker를 사용할 예정이므로 Docker를 먼저 설치한다. 

Minikube Driver로 사용한 리스트는 다음 페이지를 참고한다. [Minikube Drivers](https://minikube.sigs.k8s.io/docs/drivers/)  

```bash
sudo yum install docker
docker -v
```

### 2.2 Kubectl설치  
Kubectl은 Kubernetes 클러스터 전체를 컨트롤히가 위한 User-Client이다. 
```bash
curl -LO https://storage.googleapis.com/kubernetes-release/release/`curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt`/bin/linux/amd64/kubectl
chmod +x ./kubectl
mv ./kubectl /usr/local/bin/kubectl
```


### 2.3 Minikube설치
```bash
curl -Lo minikube https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
chmod +x minikube
install minikube /usr/local/bin/
yum install conntrack
```

Minikube설치를 마치면 기동은 아래와 같이 시작할 수 있다. 이 때 주의할 점은 root계정으로는 실행이 불가하다. 
```
minikube start --driver=docker
```

아래의 명령을 통해 minikube 유저를 생성하면서 docker그룹으로 넣어준다. 
```
adduser -G docker minikube  
```

그리고 minikube 유저로 Minikube 클러스터를 생성한다.
```bash
minikube start --driver=docker
##Print
😄  minikube v1.26.0 on Amazon 2 (xen/amd64)
✨  Using the docker driver based on existing profile
👍  Starting control plane node minikube in cluster minikube
🚜  Pulling base image ...
🔄  Restarting existing docker container for "minikube" ...
🐳  Preparing Kubernetes v1.24.1 on Docker 20.10.17 ...
🔎  Verifying Kubernetes components...
    ▪ Using image gcr.io/k8s-minikube/storage-provisioner:v5
🌟  Enabled addons: default-storageclass, storage-provisioner
🏄  Done! kubectl is now configured to use "minikube" cluster and "default" namespace by default
```

## Minikube 환경 분석  

Minikube를 로컬에서 실행했기 때문에 MasterNode와 WorkerNode는 모두 같다.
```bash
k get nodes -A -o wide
##Print
NAME       STATUS   ROLES           AGE   VERSION   INTERNAL-IP    EXTERNAL-IP   OS-IMAGE             KERNEL-VERSION                  CONTAINER-RUNTIME
minikube   Ready    control-plane   11m   v1.24.1   192.168.49.2   <none>        Ubuntu 20.04.4 LTS   5.10.118-111.515.amzn2.x86_64   docker://20.10.17
```

1차 적으로는 해당 kubectl 커맨드가 가능한 것은 minikube 기동 시 홈 디렉토리 하위 .kube/config 파일의 설정이 자동으로 업데이트가 된다.  
config에는 context정보가 설정이 된다. context 정보는 user + cluster 정보의 조합으로 구성된다. 

user는 minikube 유저이며 /home/minikube/.minikube/profiles/minikube/client.crt 인증서 및 /home/minikube/.minikube/profiles/minikube/client.key 파일을 통해 로그인이 가능하다. 
해당 인증서는 Kubernetes의 cluster의 ca로 서명이 된 인증서이다. 따라서 해당 인증서로 접속이 가능하다. 
신규 유저를 생성할 때도 마찬가지로 ca의 인증서를 사용하면 가능하며 /home/minikube/.minikube/certs 위치에 존재한다. 
해당 설정은 나중에 추가적으로 알아본다.  

cluster정보에는 ca인증서 정보 및 마스터 서버의 api-server주소가 나와있다. client는 api-server에 8443 포트를 통해 cluster에 api를 호출할 수 있다.  
`~/.kube/config`  
```yaml 
apiVersion: v1
clusters:
- cluster:
    certificate-authority: /home/minikube/.minikube/ca.crt
    extensions:
    - extension:
        last-update: Fri, 08 Jul 2022 07:07:09 UTC
        provider: minikube.sigs.k8s.io
        version: v1.26.0
      name: cluster_info
    server: https://192.168.49.2:8443
  name: minikube
contexts:
- context:
    cluster: minikube
    extensions:
    - extension:
        last-update: Fri, 08 Jul 2022 07:07:09 UTC
        provider: minikube.sigs.k8s.io
        version: v1.26.0
      name: context_info
    namespace: default
    user: minikube
  name: minikube
current-context: minikube
kind: Config
preferences: {}
users:
- name: minikube
  user:
    client-certificate: /home/minikube/.minikube/profiles/minikube/client.crt
    client-key: /home/minikube/.minikube/profiles/minikube/client.key
```


### 1. Nginx 배포  
Worker Node의 네트워크 분석을 위해 Minikube클러스터에 Nginx를 배포한다. 

```bash
vim nginx-deploy.yml
kubectl apply -f nginx-deploy.yml
```

`nginx-deploy.yml`  
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx-deployment
spec:
  selector:
    matchLabels:
      app: nginx
  replicas: 2 # tells deployment to run 2 pods matching the template
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: nginx:latest
        ports:
        - containerPort: 80
```

배포를 진행하고 상태를 확인하면 아래와 같이 2개의 파드가 실행 중인 것을 확인할 수 있다.  
```bash
kubectl get pods -o wide
##Print
NAME                                READY   STATUS    RESTARTS   AGE   IP           NODE       NOMINATED NODE   READINESS GATES
nginx-deployment-544dc8b7c4-j8h4r   1/1     Running   0          20m   172.17.0.4   minikube   <none>           <none>
nginx-deployment-544dc8b7c4-lpgbs   1/1     Running   0          20m   172.17.0.3   minikube   <none>           <none>
```


### 2. WorkerNode의 네트워크 분석
2개의 nginx pod 가 배포된 상태에서 네트워크를 분석해본다.  

아래의 커맨드를 통해 Minikube 노드에 접속할 수 있다. 
```bash
ssh -i /home/minikube/.minikube/machines/minikube/id_rsa docker@$(minikube ip)
```

![minikube001](/assets/it/cloud/minikube/minikube001.png){: width="70%" height="auto"}

전체적인 구성은 아래와 위와 같으며 아래와 같이 확인을 진행해본다. 


Minikube 서버에 접속 후 (WorkerNode로 한정) 아래의 커맨드로 Network을 살펴본다.  
```bash
ip addr show type bridge
bridge link show / brctl show
ip addr show type veth
ip route
```
<br>

우선 첫 번째 커맨드를 통해 bridge 타입의 주소를 확인해 본다. minikube의 driver 타입을 docker로 하였기 때문에 docker0 bridge가 하나 생성이 되었다. 
```bash
ip addr show type bridge
##Print
2: docker0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc noqueue state UP group default
    link/ether 02:42:23:5e:fe:cf brd ff:ff:ff:ff:ff:ff
    inet 172.17.0.1/16 brd 172.17.255.255 scope global docker0
       valid_lft forever preferred_lft forever
```
<br>

brctl, bridge 커맨드를 통해 위에서 확인한 2개의 veth 인터페이스와 docker0의 bridge 가 서로 링크되어 있는 것을 확인할 수 있다. 
```bash
brctl show
##Print
bridge name	bridge id		STP enabled	interfaces
docker0		8000.0242235efecf	no		veth0c89d51
							vethe85326f
```

```bash
bridge link show
##Print
11: veth0c89d51@if10: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 master docker0 state forwarding priority 32 cost 2
13: vethe85326f@if12: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 master docker0 state forwarding priority 32 cost 2
```
<br>

ip addr 커맨드를 통해 veth 타입의 주소를 확인하면 2개의 veth을 가진 인터페이스를 확인 할 수 있다. 
```bash
ip addr show type veth
##Print
11: veth0c89d51@if10: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc noqueue master docker0 state UP group default
    link/ether ce:db:47:40:39:85 brd ff:ff:ff:ff:ff:ff link-netnsid 3
13: vethe85326f@if12: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc noqueue master docker0 state UP group default
    link/ether 16:c6:bf:76:f9:01 brd ff:ff:ff:ff:ff:ff link-netnsid 2
```
<br>

ip route 를 확인해보면 아래와 같다. 
172.17.0.0/16 는 docker0 장치에 연결되어 172.17.0.1로 링크된다. 
```bash
ip route
##Print 
default via 192.168.49.1 dev eth0
172.17.0.0/16 dev docker0 proto kernel scope link src 172.17.0.1
192.168.49.0/24 dev eth0 proto kernel scope link src 192.168.49.2
```
<br>

여기까지는 하나의 노드에서 진행된 경우고, 여러개의 WorkerNode를 생성하게 되면 Pod간 통신을 위해 CNI를 설치해야 한다.  
CNI란 Container Network Interface로 Tunneling을 통해 이루어진다.  

![minikube002](/assets/it/cloud/minikube/minikube002.png){: width="50%" height="auto"}

<br>

## Minikube 사용자 추가  



<br><br><br>

> Ref: https://youtu.be/U35C0EPSwoY