---
layout: post
title: "EKS VPC CNI 환경에서 IPv4, IPv6 파드의 SNAT와 통신"
author: "Bys"
category: cloud
date: 2024-07-18 01:00:00
tags: ipv4 ipv6 snat 
---

# [EKS VPC CNI 설정에 따른 IPv4, IPv6 통신]
먼저 EKS 환경에서 각 파드가 어떻게 통신하는지에 대해서 살펴보려고 한다.  

## 1. [IPv4]
#### 1. Case (Pod(10.20.19.220) -> EC2(10.20.1.208)) (Inter VPC)
- CNI ENV
  - ENABLE_IPv4: true
  - AWS_VPC_K8S_CNI_EXTERNALSNAT: false
  - ENABLE_POD_ENI: false

```bash
kubectl get po -o wide
NAME                       READY   STATUS    RESTARTS   AGE   IP             NODE                                              NOMINATED NODE   READINESS GATES
netutil-66bb78c874-vslb5   1/1     Running   0          94s   10.20.19.220   ip-10-20-19-35.ap-northeast-2.compute.internal    <none>           <none>


[root@ip-10-20-1-208 httpd]# tail -f access_log
10.20.19.220 - - [18/Jul/2024:00:25:20 +0000] "GET / HTTP/1.1" 403 3630 "-" "curl/7.79.1"
10.20.19.220 - - [18/Jul/2024:00:25:20 +0000] "GET / HTTP/1.1" 403 3630 "-" "curl/7.79.1"
```

> 같은 VPC 내부에서는 CNI의 SNAT 없이 파드 IP로 통신 한다. 

---

#### 2. Case (Pod(10.20.19.48) -> VPC Peering ->  EC2(192.168.114.53)) (Other VPC)
- CNI ENV
  - ENABLE_IPv4: true
  - AWS_VPC_K8S_CNI_EXTERNALSNAT: false
  - ENABLE_POD_ENI: false

```bash
kubectl get po -o wide
NAME                       READY   STATUS    RESTARTS   AGE   IP             NODE                                              NOMINATED NODE   READINESS GATES
netutil-66bb78c874-vslb5   1/1     Running   0          94s   10.20.19.220   ip-10-20-19-35.ap-northeast-2.compute.internal    <none>           <none>


[root@ip-192-168-114-53 nginx]# tail -f access.log
10.20.19.35 - - [18/Jul/2024:01:33:40 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"
10.20.19.35 - - [18/Jul/2024:01:33:41 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"
```

> VPC Peering를 거쳐 외부 통신시에는 노드 IP로 SNAT 되어 통신 한다. (AWS_VPC_K8S_CNI_EXTERNALSNAT: false)  
> 그리고 ENI(eth1)은 PrimaryIP(10.20.19.148)-SecondaryIP(10.20.19.220)를 가지고 있지만 파드는 노드의 eth0(10.20.19.35)를 통해 통신하게 된다.  

---

#### 3. EXTERNALSNAT Case (Pod(10.20.19.220) -> VPC Peering ->  EC2(192-168-114-53)) (Other VPC)
- CNI ENV
  - ENABLE_IPv4: true
  - AWS_VPC_K8S_CNI_EXTERNALSNAT: true 
  - ENABLE_POD_ENI: false

```bash
kubectl get po -o wide
NAME                       READY   STATUS    RESTARTS   AGE   IP             NODE                                              NOMINATED NODE   READINESS GATES
netutil-66bb78c874-vslb5   1/1     Running   0          94s   10.20.19.220   ip-10-20-19-35.ap-northeast-2.compute.internal    <none>           <none>


[root@ip-192-168-114-53 nginx]# tail -f access.log
10.20.19.220 - - [18/Jul/2024:01:37:48 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"
10.20.19.220 - - [18/Jul/2024:01:37:48 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"
```

> VPC Peering를 거쳐 외부 통신이어도 AWS_VPC_K8S_CNI_EXTERNALSNAT 옵션을 true로 설정하면 파드 IP로 통신 하게 된다.  

---

#### 4. SGP Case (Pod(10.20.19.48) -> EC2(10.20.1.208)) (Inter VPC)
- CNI ENV
  - ENABLE_IPv4: true
  - AWS_VPC_K8S_CNI_EXTERNALSNAT: false
  - ENABLE_POD_ENI: true

```yaml
apiVersion: vpcresources.k8s.aws/v1beta1
kind: SecurityGroupPolicy
metadata:
  name: default-sgp
spec:
  podSelector: {}
  securityGroups:
    groupIds:
      - sg-097db5777d035fa4c
```

```bash
kubectl get po -o wide
NAME                       READY   STATUS    RESTARTS   AGE   IP             NODE                                              NOMINATED NODE   READINESS GATES
netutil-6df8c556b8-n22pg   1/1     Running   0          4s    10.20.19.48    ip-10-20-19-35.ap-northeast-2.compute.internal    <none>           <none>


[root@ip-10-20-1-208 httpd]# tail -f access_log
10.20.19.48 - - [18/Jul/2024:01:54:10 +0000] "GET / HTTP/1.1" 403 3630 "-" "curl/7.79.1"
10.20.19.48 - - [18/Jul/2024:01:54:11 +0000] "GET / HTTP/1.1" 403 3630 "-" "curl/7.79.1"
```

> VPC 내부 통신에서는 파드 IP 그대로 통신하고, SGP도 설정되어 있기 때문에 파드 IP로 통신한다.
> 웹 서버에서 SGP설정에 적용한 보안 그룹(sg-097db5777d035fa4c)을 허용하면 통신이 가능하다.  

---

#### 5. SGP Case (Pod(10.20.19.48) -> VPC Peering ->  EC2(192.168.114.53)) (Other VPC)
- CNI ENV
  - ENABLE_IPv4: true
  - AWS_VPC_K8S_CNI_EXTERNALSNAT: false
  - ENABLE_POD_ENI: true

```yaml
apiVersion: vpcresources.k8s.aws/v1beta1
kind: SecurityGroupPolicy
metadata:
  name: default-sgp
spec:
  podSelector: {}
  securityGroups:
    groupIds:
      - sg-097db5777d035fa4c
```

```bash
kubectl get po -o wide
NAME                       READY   STATUS    RESTARTS   AGE   IP             NODE                                              NOMINATED NODE   READINESS GATES
netutil-6df8c556b8-n22pg   1/1     Running   0          4s    10.20.19.48    ip-10-20-19-35.ap-northeast-2.compute.internal    <none>           <none>

[root@ip-192-168-114-53 nginx]# tail -f access.log
10.20.19.48 - - [18/Jul/2024:02:04:12 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"
10.20.19.48 - - [18/Jul/2024:02:04:13 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"
```

> VPC Peering를 거쳐 외부 통신이어도 SGP 설정시에는 파드의 IP 그대로 통신하기 때문에 웹 서버에서 10.20.19.48 IP를 허용하면 통신이 가능하다.  

---

<br><br><br><br>

---

## 2. [IPv6]
 
#### 1. Case (Pod(2406:da12:86:8705:3614::) -> EC2-IPv4Only(192.168.114.53)) (Inter VPC)
- WorkerNode 
  - Private IPv4 address: 192.168.188.129
  - Private IPv6 address: 2406:da12:86:8705:ab8d:2af6:b35:312f
  - IPv6 Prefix Delegation: 2406:da12:86:8705:3614::/80
- ENABLE_IPv6: true
- AWS_VPC_K8S_CNI_EXTERNALSNAT: false
- ENABLE_POD_ENI: false

```bash
kubectl get po -o wide
NAME                       READY   STATUS    RESTARTS   AGE   IP                         NODE                                                 NOMINATED NODE   READINESS GATES
netutil-5b6d5947fc-kclk8   1/1     Running   0          6s    2406:da12:86:8705:3614::   ip-192-168-188-129.ap-northeast-2.compute.internal   <none>           <none>


[root@ip-192-168-114-53 nginx]# tail -f access.log
# netutil 파드에서 curl 192.168.114.53 에 대한 응답 
192.168.188.129 - - [18/Jul/2024:02:12:17 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"
192.168.188.129 - - [18/Jul/2024:02:12:19 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"
```

> VPC 내부 통신에서 IPv4 주소로 요청했기 때문에 파드는 노드의 IPv4 주소를 통해 통신한다.  

---

#### 2. Case (Pod(2406:da12:86:8705:3614::) -> EC2-Dualstack(192.168.148.217, 2406:da12:86:8704::fd82)) (Inter VPC)
- WorkerNode 
  - Private IPv4 address: 192.168.188.129
  - Private IPv6 address: 2406:da12:86:8705:ab8d:2af6:b35:312f
  - IPv6 Prefix Delegation: 2406:da12:86:8705:3614::/80
- ENABLE_IPv6: true
- AWS_VPC_K8S_CNI_EXTERNALSNAT: false
- ENABLE_POD_ENI: false

```bash
kubectl get po -o wide
NAME                       READY   STATUS    RESTARTS   AGE   IP                         NODE                                                 NOMINATED NODE   READINESS GATES
netutil-5b6d5947fc-kclk8   1/1     Running   0          6s    2406:da12:86:8705:3614::   ip-192-168-188-129.ap-northeast-2.compute.internal   <none>           <none>


[root@ip-192-168-148-217 ~]# tail -f /var/log/nginx/access.log
# netutil 파드에서 curl 192.168.148.217 에 대한 응답 
192.168.188.129 - - [18/Jul/2024:02:26:16 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"
192.168.188.129 - - [18/Jul/2024:02:26:16 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"

# netutil 파드에서 curl -g -6 'http://[2406:da12:86:8704::fd82%eth0]:80/' 에 대한 응답
2406:da12:86:8705:3614:: - - [18/Jul/2024:02:28:37 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"
2406:da12:86:8705:3614:: - - [18/Jul/2024:02:28:39 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"
```

> IPv4, IPv6 Dual-stack 에서도 IPv4 주소로 curl 수행하면 노드의 IPv4 주소로, IPv6 주소로 curl을 수행하면 파드의 IPv6를 통해 통신한다.  

---

#### 3. Case (Pod(2406:da12:86:8705:3614::) -> VPC Peering ->  EC2(10.20.1.108)) (Other VPC)
- WorkerNode 
  - Private IPv4 address: 192.168.188.129
  - Private IPv6 address: 2406:da12:86:8705:ab8d:2af6:b35:312f
  - IPv6 Prefix Delegation: 2406:da12:86:8705:3614::/80
- ENABLE_IPv6: true
- AWS_VPC_K8S_CNI_EXTERNALSNAT: false
- ENABLE_POD_ENI: false


```bash
kubectl get po -o wide
NAME                       READY   STATUS    RESTARTS   AGE   IP                         NODE                                                 NOMINATED NODE   READINESS GATES
netutil-5b6d5947fc-kclk8   1/1     Running   0          6s    2406:da12:86:8705:3614::   ip-192-168-188-129.ap-northeast-2.compute.internal   <none>           <none>



[root@ip-10-20-1-208 httpd]# tail -f access_log
# netutil 파드에서 curl 10.20.1.108 에 대한 응답 
192.168.188.129 - - [18/Jul/2024:05:42:33 +0000] "GET / HTTP/1.1" 403 3630 "-" "curl/7.79.1"
192.168.188.129 - - [18/Jul/2024:05:42:34 +0000] "GET / HTTP/1.1" 403 3630 "-" "curl/7.79.1"
```

> VPC Peering를 거쳐 외부 통신시에는 노드 IP로 SNAT 되어 통신 함  
> 노드 IP인 192.168.188.129을 통해 CNI SNAT 되어 통신됨



#### 4. EXTERNALSNAT Case (Pod(2406:da12:86:8705:3614::) -> EC2-Dualstack(192.168.148.217, 2406:da12:86:8704::fd82)) (Inter VPC)
- WorkerNode 
  - Private IPv4 address: 192.168.188.129
  - Private IPv6 address: 2406:da12:86:8705:ab8d:2af6:b35:312f
  - IPv6 Prefix Delegation: 2406:da12:86:8705:3614::/80
- ENABLE_IPv6: true
- AWS_VPC_K8S_CNI_EXTERNALSNAT: true
- ENABLE_POD_ENI: false


```bash
kubectl get po -o wide
NAME                       READY   STATUS    RESTARTS   AGE   IP                         NODE                                                 NOMINATED NODE   READINESS GATES
netutil-5b6d5947fc-kclk8   1/1     Running   0          6s    2406:da12:86:8705:3614::   ip-192-168-188-129.ap-northeast-2.compute.internal   <none>           <none>


[root@ip-192-168-148-217 nginx]# tail -f access.log
# netutil 파드에서 curl 192.168.148.217 에 대한 응답 
192.168.188.129 - - [18/Jul/2024:05:51:07 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"
192.168.188.129 - - [18/Jul/2024:05:51:08 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"

# netutil 파드에서 curl -g -6 'http://[2406:da12:86:8704::fd82%eth0]:80/' 에 대한 응답
2406:da12:86:8705:3614:: - - [18/Jul/2024:05:51:33 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"
2406:da12:86:8705:3614:: - - [18/Jul/2024:05:51:34 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"
```
> AWS_VPC_K8S_CNI_EXTERNALSNAT 옵션에 상관없이 IPv4 주소를 호출하면 노드 IP로 통신하고, IPv6 주소를 호출하면 Pod IP를 통해 통신한다. 



#### 5. SGP Case (Pod(2406:da12:86:8705::ce08) -> EC2-Dualstack(192.168.148.217, 2406:da12:86:8704::fd82)) (Inter VPC)
- WorkerNode 
  - Private IPv4 address: 192.168.188.129
  - Private IPv6 address: 2406:da12:86:8705:ab8d:2af6:b35:312f
  - IPv6 Prefix Delegation: 2406:da12:86:8705:3614::/80
- ENABLE_IPv6: true
- AWS_VPC_K8S_CNI_EXTERNALSNAT: false
- ENABLE_POD_ENI: false

```bash
➜  temp kubectl get po -o wide
NAME                       READY   STATUS    RESTARTS   AGE   IP                        NODE                                                 NOMINATED NODE   READINESS GATES
netutil-79c7497789-59pks   1/1     Running   0          7s    2406:da12:86:8705::ce08   ip-192-168-188-129.ap-northeast-2.compute.internal   <none>           <none>

[root@ip-192-168-148-217 nginx]# tail -f access.log
# netutil 파드에서 curl 192.168.148.217 에 대한 응답 
192.168.188.129 - - [18/Jul/2024:05:57:53 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"
192.168.188.129 - - [18/Jul/2024:05:57:55 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"

# netutil 파드에서 curl -g -6 'http://[2406:da12:86:8704::fd82%eth0]:80/' 에 대한 응답
2406:da12:86:8705::ce08 - - [18/Jul/2024:05:59:49 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"
2406:da12:86:8705::ce08 - - [18/Jul/2024:05:59:50 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.79.1" "-"
```

> AWS_VPC_K8S_CNI_EXTERNALSNAT 옵션에 상관없이 IPv4 주소를 호출하면 노드 IP로 통신하고, IPv6 주소를 호출하면 Pod IP를 통해 통신한다. 
> SGP를 적용했기 때문에 Pod IP의 ENI는 별도의 SGP에 설정된 보안그룹을 가지고 있기 때문에 웹 서버에서 그 보안그룹을 열어주어야 통신이 가능하다. 
> 웹 서버 보안그룹에서 노드 보안 그룹을 제거하면 IPv4 통신은 불가능해진다.  


<br><br><br>

- References  
[1] 