---
slug: k8s-kube-proxy
author: Bys
categories:
- k8s
date: '2023-06-29 01:00:00'
tags:
- eks
- node
- iptables
title: Kubernetes kube-proxy 알아보기
description: "Kubernetes kube-proxy의 동작 원리와 iptables 규칙 관리 방법. 서비스 트래픽 라우팅, iptables/ipvs 모드 비교, 실제 네트워크 패킷 흐름 분석을 통해 kube-proxy를 심층 분석합니다."
---


# [kube-proxy](https://kubernetes.io/docs/concepts/overview/components/#kube-proxy)  
- kube-proxy는 워커노드의 컴포넌트로 동작하며 Network proxy 역할을 수행한다. 이는 Kubernetes의 서비스 개념을 구체화한 것이다. 
  > kube-proxy is a network proxy that runs on each node in your cluster, implementing part of the Kubernetes Service concept.
- kube-proxy는 노드의 network rule(ex. iptables rule)을 관리한다. 이러한 network rule들은 파드로의 In/Out 네트워크 통신을 허용한다.  
  > kube-proxy maintains network rules on nodes. These network rules allow network communication to your Pods from network sessions inside or outside of your cluster.
- kube-proxy는 OS의 패킷 필터링 계층을 사용하고 그렇지 않으면 kube-proxy가 트래픽을 전달한다.  
  > kube-proxy uses the operating system packet filtering layer if there is one and it's available. Otherwise, kube-proxy forwards the traffic itself

<br>

## Kube-proxy의 동작 방식

### 1. iptables
1. Table - iptables는 여러 테이블로 구성되어 있으며, 각 테이블은 특정 유형의 패킷 처리를 담당한다.  
  - Filter: 기본 테이블, 패킷 필터링을 담당
    - Target
      - ACCEPT
      - REJECT
      - DROP
      - LOG
  - NAT: 네트워크 주소 변환을 처리
    - Target
      - MARK: MARK 타겟은 패킷에 내부적인 마크를 설정한다.  
      - MASQUERADE: MASQUERADE는 소스 네트워크 주소 변환(SNAT)의 특별한 형태다. 패킷의 소스 IP 주소를 해당 인터페이스의 IP 주소로 변경한다.  
      - RETURN: 이 체인의 처리를 중단하고 호출한 체인으로 돌아가게 한다.  
      - SNAT
      - DNAT
      - REDIRECT
  - Mangle: 패킷 헤더 수정을 담당
  - Raw: 연결 추적을 위한 설정

2. Chains - 각 테이블은 여러 체인을 포함하고 있습니다. 주요 체인은 다음과 같다.  
  - PREROUTING: 패킷이 네트워크 인터페이스에 도착하자마자 처리
  - INPUT: 로컬 시스템으로 들어오는 패킷 처리
  - FORWARD: 시스템을 통과하는 패킷 처리
  - OUTPUT: 로컬 시스템에서 나가는 패킷 처리
  - POSTROUTING: 패킷이 네트워크 인터페이스를 떠나기 직전 처리

<br>

kube-proxy는 [spec.externalTrafficPolicy](https://kubernetes.io/docs/concepts/services-networking/service-traffic-policy/)의 설정에 따라서 라우팅되는 엔드포인트를 필터링 한다[1]. 서비스에는 spec.externalTrafficPolicy 필드가 존재하며 Cluster(Default 설정) 또는 Local 값을 갖으며 서비스의 externalTrafficPolicy 정책이 Local인 경우 트래픽이 랜덤으로 분산되지 않는다.  

- spec.externalTrafficPolicy: Local
externalTrafficPolicy가 Local로 설정된 서비스에 대해서 kube-proxy는 같은 노드에 있는 엔드포인트로 트래픽을 분산한다. [문서](https://kubernetes.io/blog/2022/12/30/advancements-in-kubernetes-traffic-engineering/#:~:text=healthy%20Node%2C%20when-,externalTrafficPolicy%20is%20Local,-One%20such%20scenario)를 확인하면 첫 번째 노드의 A파드에서 B서비스로 요청시 A파드가 존재하는 노드의 kube-proxy는 자신의 노드에 존재하는 1개의 B파드로만 트래픽을 전달한다. 

- spec.externalTrafficPolicy: Cluster(Default)
externalTrafficPolicy가 Cluster로 설정된 서비스에 대해서 kube-proxy는 모든 엔드포인트로 랜덤하게 트래픽을 분산한다. [문서](https://kubernetes.io/blog/2022/12/30/advancements-in-kubernetes-traffic-engineering/#:~:text=Figure%207%3A%20Service%20routing%20when%20internalTrafficPolicy%20is%20Cluster)를 확인하면 첫 번째 노드의 A파드에서 B서비스로 요청시 A파드가 존재하는 노드의 kube-proxy는 iptables를 기반으로 트래픽을 3개의 B파드로 전달한다. 

<br>



<br>

---
### Scenario test

#### Case1. PREROUTING - When `externalTrafficPolicy` is `Cluster`.

##### 1. nginx 샘플 배포  

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nginx
  namespace: test
spec:
  selector:
    matchLabels:
      app: nginx
  replicas: 2
  template:
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: nginx
        ports:
        - containerPort: 80
---
apiVersion: v1
kind: Service
metadata:
  name: nginx
  namespace: test
  labels:
    app: nginx
  annotations:
spec:
  type: ClusterIP
  selector:
    app: nginx
  ports:
  - name: http
    port: 80
    protocol: TCP
    targetPort: 80
```
해당 매니페스트를 배포 하면 서비스의 externalTrafficPolicy는 기본 설정인 Cluster 값을 갖는다.  

##### 2. 배포 확인  

```bash
$ kubectl get po -o wide -n test
NAME                    READY   STATUS    RESTARTS   AGE   IP             NODE                                              NOMINATED NODE   READINESS GATES
nginx-55f598f8d-25sx9   1/1     Running   0          13m   10.20.11.179   ip-10-20-11-149.ap-northeast-2.compute.internal   <none>           <none>
nginx-55f598f8d-wcqt7   1/1     Running   0          13m   10.20.10.127   ip-10-20-10-207.ap-northeast-2.compute.internal   <none>           <none>

$ kubectl get svc -o wide -n test
NAME    TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)   AGE   SELECTOR
nginx   ClusterIP   172.20.247.136   <none>        80/TCP    30m   app=nginx
```


##### 3. 워커노드의 iptables 확인  

- PREROUTING 확인 (패킷의 도착지 주소를 변경하는 과정)
```bash
$ iptables -L PREROUTING -v -n -t nat

Chain PREROUTING (policy ACCEPT 2703 packets, 245K bytes)
pkts bytes target     prot opt in     out     source               destination
4823K  502M KUBE-SERVICES  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* kubernetes service portals */
```

- KUBE-SERVICES 확인
KUBE-SERVICES를 KUBE-SVC-IU46S4VZFN77LK6S target에 대해서는 목적지를 172.20.247.136로 변경하며 172.20.247.136는 test네임스페이스의 nginx서비스 IP주소  
```bash
$ iptables -L KUBE-SERVICES -v -n -t nat

Chain KUBE-SERVICES (2 references)
pkts bytes target                     prot opt in     out     source               destination
    0     0 KUBE-SVC-IU46S4VZFN77LK6S  tcp  --  *      *       0.0.0.0/0            172.20.247.136       /* test/nginx:http cluster IP */ tcp dpt:80
1946  117K KUBE-NODEPORTS             all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* kubernetes service nodeports; NOTE: this must be the last rule in this chain */ ADDRTYPE match dst-type LOCAL
```


- KUBE-SVC-IU46S4VZFN77LK6S 확인
다시 KUBE-SVC-IU46S4VZFN77LK6S를 확인하면 (KUBE-SEP-XAAQ4E3V5J6GEKUJ, KUBE-SEP-S7XCV467AW272WRX) 두 개의 타겟이 존재하며 랜덤으로 분산이 되지만 통계 확률적으로는 0.5단위로 분산 됨  
```bash
$ iptables -L KUBE-SVC-IU46S4VZFN77LK6S -v -n -t nat

Chain KUBE-SVC-IU46S4VZFN77LK6S (1 references)
pkts bytes target                     prot opt in     out     source               destination
    0     0 KUBE-SEP-XAAQ4E3V5J6GEKUJ  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* test/nginx:http -> 10.20.10.127:80 */ statistic mode random probability 0.50000000000
    0     0 KUBE-SEP-S7XCV467AW272WRX  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* test/nginx:http -> 10.20.11.179:80 */
```

- KUBE-SEP-XAAQ4E3V5J6GEKUJ, KUBE-SEP-S7XCV467AW272WRX를 확인하면 최종적으로 172.20.247.136 서비스 IP는 (10.20.10.127, 10.20.11.179)로 목적지가 변경 됨  
```bash
$ iptables -L KUBE-SEP-XAAQ4E3V5J6GEKUJ -v -n -t nat

Chain KUBE-SEP-XAAQ4E3V5J6GEKUJ (1 references)
pkts bytes target          prot opt in     out     source               destination
    0     0 KUBE-MARK-MASQ  all  --  *      *       10.20.10.127         0.0.0.0/0            /* test/nginx:http */
    0     0 DNAT            tcp  --  *      *       0.0.0.0/0            0.0.0.0/0            /* test/nginx:http */ tcp to:10.20.10.127:80


$ iptables -L KUBE-SEP-S7XCV467AW272WRX -v -n -t nat

Chain KUBE-SEP-S7XCV467AW272WRX (1 references)
pkts bytes target          prot opt in     out     source               destination
    0     0 KUBE-MARK-MASQ  all  --  *      *       10.20.11.179         0.0.0.0/0            /* test/nginx:http */
    0     0 DNAT            tcp  --  *      *       0.0.0.0/0            0.0.0.0/0            /* test/nginx:http */ tcp to:10.20.11.179:80
```


##### 4. 파드의 수를 3개로 늘린 후 KUBE-SVC-IU46S4VZFN77LK6S 확인
- KUBE-SVC-IU46S4VZFN77LK6S를 확인하면 파드를 3개로 늘린 상황에서는 아래와 같이 트래픽이 랜덤 분산되며 통계적으로 0.3, 0.5, 0.2의 확률이다. 
```bash
$ iptables -L KUBE-SVC-IU46S4VZFN77LK6S -v -n -t nat
Chain KUBE-SVC-IU46S4VZFN77LK6S (1 references)
pkts bytes target                     prot opt in     out     source               destination
    0     0 KUBE-SEP-XAAQ4E3V5J6GEKUJ  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* test/nginx:http -> 10.20.10.127:80 */ statistic mode random probability 0.33333333349
    0     0 KUBE-SEP-K34D3XS7VWEHKFPE  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* test/nginx:http -> 10.20.10.218:80 */ statistic mode random probability 0.50000000000
    0     0 KUBE-SEP-S7XCV467AW272WRX  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* test/nginx:http -> 10.20.11.179:80 */
```

---

#### Case2. PREROUTING - When `externalTrafficPolicy` is `Local`.


---

#### Case3. POSTROUTING - When `AWS_VPC_K8S_CNI_EXTERNALSNAT` is `false`

1. POSTROUTING 
모든 프로토콜의 source와 destination에 대해서 KUBE-POSTROUTING, AWS-SNAT-CHAIN-0 규칙이 순서대로 적용된다.  
```bash
[root@ip-10-20-136-210 ~]# iptables -L POSTROUTING -v -n -t nat
Chain POSTROUTING (policy ACCEPT 1111 packets, 75960 bytes)
 pkts bytes target     prot opt in     out     source               destination
32314 2183K KUBE-POSTROUTING  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* kubernetes postrouting rules */
32189 2174K AWS-SNAT-CHAIN-0  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* AWS SNAT CHAIN */
```


2. KUBE-POSTROUTING
MARK가 0x4000 아닌 패킷에 대해서 RETURN 동작을 수행 RETURN은 체인의 처리를 중단하고 호출한 체인으로 돌아가게 한다. 추가로, MARK는 패킷에 내부적인 마크를 설정하는 것을 의미한다. MASQUERADE는 패킷의 출발지 주소를 게이트웨이 장치에 부여된 공인 IP 주소로 변경 되도록 한다.  
```bash
[root@ip-10-20-136-210 ~]# iptables -L KUBE-POSTROUTING -v -n -t nat
Chain KUBE-POSTROUTING (1 references)
 pkts bytes target     prot opt in     out     source               destination
 4946  330K RETURN     all  --  *      *       0.0.0.0/0            0.0.0.0/0            mark match ! 0x4000/0x4000
    0     0 MARK       all  --  *      *       0.0.0.0/0            0.0.0.0/0            MARK xor 0x4000
    0     0 MASQUERADE  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* kubernetes service traffic requiring SNAT */ random-fully
```
모든 패킷이 RETURN 되므로 MASQUERADE에 의해 SNAT 되지 않고, 상위 체인으로 돌아간다.  


3. AWS-SNAT-CHAIN-0
목적지 주소가 VPC대역(100.64.0.0./16, 10.20.0.0/16)이면 모두 RETURN되므로 SNAT를 수행하지 않는다. 그 외의 트래픽 중 VLAN 인터페이스로 나가지 않는 패킷에 대해, 목적지가 로컬이 아닌 경우 SNAT를 수행한다. SNAT 시 소스 IP를 10.20.136.210으로 변경한다. 'random-fully' 옵션은 SNAT 과정에서 사용되는 소스 포트를 완전히 무작위로 선택하도록 한다. 
```bash
[root@ip-10-20-136-210 ~]# iptables -L AWS-SNAT-CHAIN-0 -v -n -t nat
Chain AWS-SNAT-CHAIN-0 (1 references)
 pkts bytes target     prot opt in     out     source               destination
    0     0 RETURN     all  --  *      *       0.0.0.0/0            100.64.0.0/16        /* AWS SNAT CHAIN */
19848 1422K RETURN     all  --  *      *       0.0.0.0/0            10.20.0.0/16         /* AWS SNAT CHAIN */
 6577  406K SNAT       all  --  *      !vlan+  0.0.0.0/0            0.0.0.0/0            /* AWS, SNAT */ ADDRTYPE match dst-type !LOCAL to:10.20.136.210 random-fully
```

> AWS_VPC_K8S_CNI_EXTERNALSNAT 옵션이 false인 경우 VPC 내부 에서는 파드 IP로 통신하지만, VPC 외부 대역인 경우에는 노드의 IP로 SNAT 되어 통신한다.  

---

#### Case4. POSTROUTING - When `AWS_VPC_K8S_CNI_EXTERNALSNAT` is `true`
1. POSTROUTING 
모든 프로토콜의 source와 destination에 대해서 KUBE-POSTROUTING 규칙이 적용된다. AWS_VPC_K8S_CNI_EXTERNALSNAT 옵션을 변경한 후, AWS-SNAT-CHAIN-0 체인이 사라졌다.  
```bash
[root@ip-10-20-20-176 ~]# iptables -L POSTROUTING -v -n -t nat
Chain POSTROUTING (policy ACCEPT 186 packets, 12174 bytes)
 pkts bytes target     prot opt in     out     source               destination
1508K  101M KUBE-POSTROUTING  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* kubernetes postrouting rules */
```

2. KUBE-POSTROUTING
MARK가 0x4000 아닌 패킷에 대해서 RETURN 동작을 수행 RETURN은 체인의 처리를 중단하고 호출한 체인으로 돌아가게 한다. 추가로, MARK는 패킷에 내부적인 마크를 설정하는 것을 의미한다. MASQUERADE는 패킷의 출발지 주소를 게이트웨이 장치에 부여된 공인 IP 주소로 변경 되도록 한다.  
```bash
[root@ip-10-20-20-176 ~]# iptables -L KUBE-POSTROUTING -v -n -t nat
Chain KUBE-POSTROUTING (1 references)
 pkts bytes target     prot opt in     out     source               destination
 6050  408K RETURN     all  --  *      *       0.0.0.0/0            0.0.0.0/0            mark match ! 0x4000/0x4000
    0     0 MARK       all  --  *      *       0.0.0.0/0            0.0.0.0/0            MARK xor 0x4000
    0     0 MASQUERADE  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* kubernetes service traffic requiring SNAT */ random-fully
```
> AWS_VPC_K8S_CNI_EXTERNALSNAT 옵션이 true인 경우 어떤 곳에서도 SNAT 를 수행하는 곳이 없기 때문에 파드 IP 그대로 통신을 하게 된다.  

---

#### Case5. POSTROUTING - When `AWS_VPC_K8S_CNI_RANDOMIZESNAT` is `none`
[AWS_VPC_K8S_CNI_RANDOMIZESNAT](https://github.com/aws/amazon-vpc-cni-k8s?tab=readme-ov-file#aws_vpc_k8s_cni_randomizesnat) 옵션 설명에 따르면 none 이 아닌 다른 옵션(Default: prng)을 사용할 경우에는 OS level (/proc/sys/net/ipv4/ip_local_port_range)에 정의되지 않은 소스포트를 할당한다. 

1. POSTROUTING 
```bash
[root@ip-10-20-20-176 ~]# iptables -L POSTROUTING -v -n -t nat
Chain POSTROUTING (policy ACCEPT 127 packets, 9109 bytes)
 pkts bytes target     prot opt in     out     source               destination
1547K  104M KUBE-POSTROUTING  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* kubernetes postrouting rules */
37274 2518K AWS-SNAT-CHAIN-0  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* AWS SNAT CHAIN */
```

2. AWS-SNAT-CHAIN-0
SNAT시 'random-fully' 옵션이 제거 된 것을 볼 수 있다. 따라서, OS level (/proc/sys/net/ipv4/ip_local_port_range)에 정의된 소스포트를 이용하여 동작한다.  

```bash
[root@ip-10-20-20-176 ~]# iptables -L AWS-SNAT-CHAIN-0 -v -n -t nat
Chain AWS-SNAT-CHAIN-0 (1 references)
 pkts bytes target     prot opt in     out     source               destination
    0     0 RETURN     all  --  *      *       0.0.0.0/0            100.64.0.0/16        /* AWS SNAT CHAIN */
27512 1922K RETURN     all  --  *      *       0.0.0.0/0            10.20.0.0/16         /* AWS SNAT CHAIN */
   47  2820 SNAT       all  --  *      !vlan+  0.0.0.0/0            0.0.0.0/0            /* AWS, SNAT */ ADDRTYPE match dst-type !LOCAL to:10.20.20.176
```

```bash
[root@ip-10-20-20-176 ~]# cat /proc/sys/net/ipv4/ip_local_port_range
32768   60999
```

서비스에는 spec.externalTrafficPolicy 필드가 존재하며 Cluster(Default 설정) 또는 Local 값을 갖으며 서비스의 externalTrafficPolicy 정책이 Local인 경우 트래픽이 랜덤으로 분산되지 않는다.


<br><br>


### 2. nftables
nftables 또한 netfilter 프레임워크 위에서 동작하지만 해시 기반 이라는 점 에서 속도면이 빠르다. 


##### 1. 전체 kube-proxy nftables 테이블 확인

```bash
# kube-proxy가 생성한 전체 nftables 룰셋 확인
$ nft list table ip kube-proxy

table ip kube-proxy {
        comment "rules for kube-proxy"
        set cluster-ips {
                type ipv4_addr
                comment "Active ClusterIPs"
                elements = { 172.20.0.1, 172.20.0.10,
                              172.20.154.213, 172.20.158.209,
                              172.20.166.27 }
        }

        set nodeport-ips {
                type ipv4_addr
                comment "IPs that accept NodePort traffic"
                elements = { 10.20.128.129 }
        }

        map no-endpoint-services {
                type ipv4_addr . inet_proto . inet_service : verdict
                comment "vmap to drop or reject packets to services with no endpoints"
                elements = { 172.20.154.213 . tcp . 443 comment "kube-system/aws-load-balancer-webhook-service:webhook-server" : goto reject-chain }
        }

        map no-endpoint-nodeports {
                type inet_proto . inet_service : verdict
                comment "vmap to drop or reject packets to service nodeports with no endpoints"
        }

        map firewall-ips {
                type ipv4_addr . inet_proto . inet_service : verdict
                comment "destinations that are subject to LoadBalancerSourceRanges"
        }

        map service-ips {
                type ipv4_addr . inet_proto . inet_service : verdict
                comment "ClusterIP, ExternalIP and LoadBalancer IP traffic"
                elements = { 172.20.0.10 . tcp . 53 : goto service-NWBZK7IH-kube-system/kube-dns/tcp/dns-tcp,
                              172.20.0.10 . udp . 53 : goto service-FY5PMXPG-kube-system/kube-dns/udp/dns,
                              172.20.166.27 . tcp . 8080 : goto service-2SONTFIS-sgp/nginx-service/tcp/,
                              172.20.0.1 . tcp . 443 : goto service-2QRHZV4L-default/kubernetes/tcp/https,
                              172.20.158.209 . tcp . 443 : goto service-QJWSRGPH-kube-system/eks-extension-metrics-api/tcp/metrics-api,
                              172.20.0.10 . tcp . 9153 : goto service-AS2KJYAD-kube-system/kube-dns/tcp/metrics }
        }

        map service-nodeports {
                type inet_proto . inet_service : verdict
                comment "NodePort traffic"
        }

        chain filter-prerouting-pre-dnat {
                type filter hook prerouting priority dstnat - 10; policy accept;
                ct state new jump firewall-check
        }

        chain filter-output-pre-dnat {
                type filter hook output priority -110; policy accept;
                ct state new jump firewall-check
        }

        chain filter-input {
                type filter hook input priority filter; policy accept;
                ct state new jump nodeport-endpoints-check
                ct state new jump service-endpoints-check
        }

        chain filter-forward {
                type filter hook forward priority filter; policy accept;
                ct state new jump service-endpoints-check
                ct state new jump cluster-ips-check
        }

        chain filter-output {
                type filter hook output priority filter; policy accept;
                ct state new jump service-endpoints-check
                ct state new jump cluster-ips-check
        }

        chain nat-prerouting {
                type nat hook prerouting priority dstnat; policy accept;
                jump services
        }

        chain nat-output {
                type nat hook output priority -100; policy accept;
                jump services
        }

        chain nat-postrouting {
                type nat hook postrouting priority srcnat; policy accept;
                jump masquerading
        }

        chain nodeport-endpoints-check {
                ip daddr @nodeport-ips meta l4proto . th dport vmap @no-endpoint-nodeports
        }

        chain service-endpoints-check {
                ip daddr . meta l4proto . th dport vmap @no-endpoint-services
        }

        chain firewall-check {
                ip daddr . meta l4proto . th dport vmap @firewall-ips
        }

        chain services {
                ip daddr . meta l4proto . th dport vmap @service-ips
                ip daddr @nodeport-ips meta l4proto . th dport vmap @service-nodeports
        }

        chain masquerading {
                meta mark & 0x00004000 == 0x00000000 return
                meta mark set meta mark ^ 0x00004000
                masquerade fully-random
        }

        chain cluster-ips-check {
                ip daddr @cluster-ips reject comment "Reject traffic to invalid ports of ClusterIPs"
                ip daddr 172.20.0.0/16 drop comment "Drop traffic to unallocated ClusterIPs"
        }

        chain mark-for-masquerade {
                meta mark set meta mark | 0x00004000
        }

        chain reject-chain {
                comment "helper for @no-endpoint-services / @no-endpoint-nodeports"
                reject
        }

......

        chain service-2QRHZV4L-default/kubernetes/tcp/https {
                numgen random mod 2 vmap { 0 : goto endpoint-WANRV762-default/kubernetes/tcp/https__10.20.131.112/443, 1 : goto endpoint-YWEXS4MY-default/kubernetes/tcp/https__10.20.136.125/443 }
        }

        chain endpoint-T5VHWRWL-sgp/nginx-service/tcp/__100.64.7.86/80 {
                ip saddr 100.64.7.86 jump mark-for-masquerade
                meta l4proto tcp dnat to 100.64.7.86:80
        }

        chain service-2SONTFIS-sgp/nginx-service/tcp/ {
                numgen random mod 2 vmap { 0 : goto endpoint-T5VHWRWL-sgp/nginx-service/tcp/__100.64.7.86/80, 1 : goto endpoint-2FSOWKR2-sgp/nginx-service/tcp/__100.64.8.163/80 }
        }

        chain endpoint-2FSOWKR2-sgp/nginx-service/tcp/__100.64.8.163/80 {
                ip saddr 100.64.8.163 jump mark-for-masquerade
                meta l4proto tcp dnat to 100.64.8.163:80
        }
}
```

##### 2. 서비스 라우팅 맵 확인 (KUBE-SERVICES에 해당)
```bash
# service-ips 맵 확인 — ClusterIP 기반 라우팅 맵
$ nft list map ip kube-proxy service-ips

table ip kube-proxy {
        map service-ips {
                type ipv4_addr . inet_proto . inet_service : verdict
                comment "ClusterIP, ExternalIP and LoadBalancer IP traffic"
                elements = { 172.20.0.10 . tcp . 53 : goto service-NWBZK7IH-kube-system/kube-dns/tcp/dns-tcp,
                              172.20.0.10 . udp . 53 : goto service-FY5PMXPG-kube-system/kube-dns/udp/dns,
                              172.20.166.27 . tcp . 8080 : goto service-2SONTFIS-sgp/nginx-service/tcp/,
                              172.20.0.1 . tcp . 443 : goto service-2QRHZV4L-default/kubernetes/tcp/https,
                              172.20.158.209 . tcp . 443 : goto service-QJWSRGPH-kube-system/eks-extension-metrics-api/tcp/metrics-api,
                              172.20.0.10 . tcp . 9153 : goto service-AS2KJYAD-kube-system/kube-dns/tcp/metrics }
        }
}
```

##### 3. 특정 서비스의 엔드포인트 분산 확인 (KUBE-SVC-xxx에 해당)

`numgen random mod 2` 는 0~1 사이의 랜덤 숫자 생성한다. 라우팅 비율은 50:50 균등 분산이 이루어진다. 
- 0 → 10.20.131.112:443으로 DNAT
- 1 → 10.20.136.125:443으로 DNAT

```bash
# 특정 서비스 체인 확인
$ nft list chain ip kube-proxy service-2QRHZV4L-default/kubernetes/tcp/https

table ip kube-proxy {
        chain service-2QRHZV4L-default/kubernetes/tcp/https {
                numgen random mod 2 vmap { 0 : goto endpoint-WANRV762-default/kubernetes/tcp/https__10.20.131.112/443, 1 : goto endpoint-YWEXS4MY-default/kubernetes/tcp/https__10.20.136.125/443 }
        }
}
```


##### 4. 엔드포인트 DNAT 확인 (KUBE-SEP-xxx에 해당)

1. ip saddr 10.20.131.112 jump mark-for-masquerade: 출발지가 자기 자신(10.20.131.112)이면 MASQUERADE 마킹 → 헤어핀(hairpin) 트래픽 처리
> MASQUERADE = 패킷의 출발지(src) IP를 나가는 인터페이스의 IP로 자동 변경하는 것  MASQUERADE 적용 시 (정상 동작). SNAT(파드 IP -> 노드 IP)


2. meta l4proto tcp dnat to 10.20.131.112:443: 목적지 IP를 10.20.131.112:443으로 DNAT
```bash
# 특정 엔드포인트 체인 확인
$ nft list chain ip kube-proxy endpoint-WANRV762-default/kubernetes/tcp/https__10.20.131.112/443

table ip kube-proxy {
        chain endpoint-WANRV762-default/kubernetes/tcp/https__10.20.131.112/443 {
                ip saddr 10.20.131.112 jump mark-for-masquerade
                meta l4proto tcp dnat to 10.20.131.112:443
        }
}
```





---

## 📚 References

