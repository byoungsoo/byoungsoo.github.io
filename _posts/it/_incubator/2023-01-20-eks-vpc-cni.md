---
layout: post
title: "EKS VPC CNI란"
author: "Bys"
category: incubator
date: 2023-01-01 01:00:00
tags: incubator
---

# [CNI](https://github.com/containernetworking/cni)  
CNI는 CNCF 프로젝트며 Linux container들의 네트워크 인터페이스를 구성하기 위한 libraries와 specification으로 구성되어있다. 
즉, CNI는 컨테이너 간의 네트워킹을 제어할 수 있는 플러그인을 만들기 위한 표준이다.

# [VPC CNI](https://github.com/aws/amazon-vpc-cni-k8s)xrf
AWS에서 제공하며 VPC의 ENI를 사용하여 쿠버네티스의 파드 네트워킹을 위한 네트워크 플러그인이다.   
> Networking plugin for pod networking in Kubernetes using Elastic Network Interfaces on AWS.

- For each Kubernetes node (ec2 instance), create multiple elastic network interfaces (ENIs) and allocate their secondary IP addresses.
- For each pod, pick a free secondary IP address, assign it to the pod, wire host and pod networking stack to allow:
    - Pod to Pod on a single host communication
    - Pod to Pod on different hosts communication
    - Pod to other AWS service communication
    - Pod to on-premises data center communication
    - Pod to internet communication

## 1. [Components]()  
VPC CNI는 두 가지 component로 구성된다. 

1. CNI Plugin  
    CNI Plugin, which will wire up the host's and pod's network stack when called.
    > 쿠버네티스 1.24 이전까지는 cni-bin-dir과 network-plugin 커맨드 라인 파라미터를 사용해 kubelet이 CNI 플러그인을 관리하게 할 수도 있었다. 이 커맨드 라인 파라미터들은 쿠버네티스 1.24에서 제거되었으며, CNI 관리는 더 이상 kubelet 범위에 포함되지 않는다.

2. IPAMD (node-Local IP Address Management (IPAM) daemon)
    - maintaining a warm-pool of available IP addresses
    - assigning an IP address to a Pod.
    - L-IPAM 데몬은 각 호스트에서 warm-pool을 유지하며 kubelet이 파드 추가 요청을 받을 때 마다 즉시 secondary IP를 할당한다. 
    - IP 주소가 설정된 임계치 보다 낮으면 아래의 동작을 통해 warm-pool을 유지한다. (ENI생성 및 EC2에 attach, ENI에 secondary IP할당, IP주소가 할당 가능해지면 IP들을 warm-pool에 추가한다) 
    CreateNetworkInterface -> AttachNetworkInterface -> ModifyNetworkInterfaceAttribute -> AssignPrivateIpAddresses


## 2. [IPAMD](https://github.com/aws/amazon-vpc-cni-k8s/blob/master/docs/eni-and-ip-target.md)  
CNI binary는 gRPC를 통해 ipamd를 호출하며 새로운 파드를 위한 IP를 요청한다. 만약 할당 가능한 IP가 없을 경우 오류를 반환한다. 유지가능한 IP의 숫자는 WARM_ENI_TARGET, WARM_IP_TARGET, MINIMUM_IP_TARGET 값에 의해 조절된다. 
기본 값은 WARM_ENI_TARGET=1 이며 이는 ipamd가 반드시 "a full ENI" 만큼의 가능한 IP를 



## 3. AWS_VPC_K8S_CNI_EXTERNALSNAT 옵션

#### Default: False일 경우, VPC CNI에 의해 SNAT: Enabled

1. 상황 
```bash
$ k get po -o wide
NAME                        READY   STATUS    RESTARTS   AGE   IP              NODE                                              NOMINATED NODE   READINESS GATES
netutil-545ff95f58-7fs8p    1/1     Running   0          66m   100.64.47.199   ip-10-20-11-162.ap-northeast-2.compute.internal   <none>           <none>

$ nslookup external-test-622b1645b82d4963.elb.ap-northeast-2.amazonaws.com
Non-authoritative answer:
Name:	external-test-622b1645b82d4963.elb.ap-northeast-2.amazonaws.com
Address: 52.78.83.169
```

2. SSH를 통해 노드 접속 후, tcpdump 수행 및 파드 내부에서 curl 요청
```bash
# 10.20.11.162 노드 접속
$ tcpdump dst 52.78.83.169 -w tcpdump.pcap

# VPC 외부 통신을 요청 할 경우 
$ kubectl exec -it netutil-545ff95f58-7fs8p -- /bin/bash
bash-5.1# curl http://external-test-622b1645b82d4963.elb.ap-northeast-2.amazonaws.com
```

3. Wireshark를 통해 tcpdump.pcap을 확인하면 다음과 같다.  
즉, 목적지가 VPC가 아닌 외부(52.78.83.169)일 경우 노드 IP로 SNAT가 진행된다.  
```txt
1	0.000000	10.20.11.162	52.78.83.169	TCP	74	1891 → 80 [SYN] Seq=0 Win=62727 Len=0 MSS=8961 SACK_PERM TSval=2695293856 TSecr=0 WS=128
2	0.002345	10.20.11.162	52.78.83.169	TCP	66	1891 → 80 [ACK] Seq=1 Ack=1 Win=62848 Len=0 TSval=2695293858 TSecr=4276808170
3	0.002373	10.20.11.162	52.78.83.169	HTTP	193	GET / HTTP/1.1 
4	0.003628	10.20.11.162	52.78.83.169	TCP	66	1891 → 80 [ACK] Seq=128 Ack=239 Win=62720 Len=0 TSval=2695293859 TSecr=4276808171
5	0.003666	10.20.11.162	52.78.83.169	TCP	66	1891 → 80 [ACK] Seq=128 Ack=854 Win=62208 Len=0 TSval=2695293859 TSecr=4276808171
6	0.003943	10.20.11.162	52.78.83.169	TCP	66	1891 → 80 [FIN, ACK] Seq=128 Ack=854 Win=62208 Len=0 TSval=2695293860 TSecr=4276808171
7	0.005122	10.20.11.162	52.78.83.169	TCP	66	1891 → 80 [ACK] Seq=129 Ack=855 Win=62208 Len=0 TSval=2695293861 TSecr=4276808173
8	1.230796	10.20.11.162	52.78.83.169	TCP	74	64318 → 80 [SYN] Seq=0 Win=62727 Len=0 MSS=8961 SACK_PERM TSval=2695295086 TSecr=0 WS=128
9	1.233127	10.20.11.162	52.78.83.169	TCP	66	64318 → 80 [ACK] Seq=1 Ack=1 Win=62848 Len=0 TSval=2695295089 TSecr=4276809400
10	1.233172	10.20.11.162	52.78.83.169	HTTP	193	GET / HTTP/1.1 
11	1.234458	10.20.11.162	52.78.83.169	TCP	66	64318 → 80 [ACK] Seq=128 Ack=239 Win=62720 Len=0 TSval=2695295090 TSecr=4276809402
12	1.234484	10.20.11.162	52.78.83.169	TCP	66	64318 → 80 [ACK] Seq=128 Ack=854 Win=62208 Len=0 TSval=2695295090 TSecr=4276809402
13	1.234596	10.20.11.162	52.78.83.169	TCP	66	64318 → 80 [FIN, ACK] Seq=128 Ack=854 Win=62208 Len=0 TSval=2695295090 TSecr=4276809402
14	1.235821	10.20.11.162	52.78.83.169	TCP	66	64318 → 80 [ACK] Seq=129 Ack=855 Win=62208 Len=0 TSval=2695295092 TSecr=4276809403
15	2.703649	10.20.11.162	52.78.83.169	TCP	74	61602 → 80 [SYN] Seq=0 Win=62727 Len=0 MSS=8961 SACK_PERM TSval=2695296559 TSecr=0 WS=128
16	2.706458	10.20.11.162	52.78.83.169	TCP	66	61602 → 80 [ACK] Seq=1 Ack=1 Win=62848 Len=0 TSval=2695296562 TSecr=4276810873
```

<br>

#### Default: true일 경우, VPC CNI에 의해 SNAT: Disabled

1. SSH를 통해 노드 접속 후, tcpdump 수행 및 파드 내부에서 curl 요청
```bash
# 10.20.11.162 노드 접속
$ tcpdump -i eth1 dst 52.78.83.169 -w tcpdump1.pcap

# VPC 외부 통신을 요청 할 경우 
$ kubectl exec -it netutil-545ff95f58-7fs8p -- /bin/bash
bash-5.1# curl http://external-test-622b1645b82d4963.elb.ap-northeast-2.amazonaws.com
```

2. Wireshark를 통해 tcpdump1.pcap을 확인하면 다음과 같다.  
파드의 IP그대로 통신이 된 것을 알 수 있다. 
```
1	0.000000	100.64.47.199	52.78.83.169	TCP	74	45852 → 80 [SYN] Seq=0 Win=62727 Len=0 MSS=8961 SACK_PERM TSval=2695624695 TSecr=0 WS=128
2	0.002528	100.64.47.199	52.78.83.169	TCP	66	45852 → 80 [ACK] Seq=1 Ack=1 Win=62848 Len=0 TSval=2695624698 TSecr=4277139009
3	0.002555	100.64.47.199	52.78.83.169	HTTP	193	GET / HTTP/1.1 
4	0.003839	100.64.47.199	52.78.83.169	TCP	66	45852 → 80 [ACK] Seq=128 Ack=239 Win=62720 Len=0 TSval=2695624699 TSecr=4277139011
5	0.003886	100.64.47.199	52.78.83.169	TCP	66	45852 → 80 [ACK] Seq=128 Ack=854 Win=62208 Len=0 TSval=2695624699 TSecr=4277139011
6	0.003985	100.64.47.199	52.78.83.169	TCP	66	45852 → 80 [FIN, ACK] Seq=128 Ack=854 Win=62208 Len=0 TSval=2695624699 TSecr=4277139011
7	0.005200	100.64.47.199	52.78.83.169	TCP	66	45852 → 80 [ACK] Seq=129 Ack=855 Win=62208 Len=0 TSval=2695624700 TSecr=4277139012
8	0.786081	100.64.47.199	52.78.83.169	TCP	74	45854 → 80 [SYN] Seq=0 Win=62727 Len=0 MSS=8961 SACK_PERM TSval=2695625481 TSecr=0 WS=128
9	0.788755	100.64.47.199	52.78.83.169	TCP	66	45854 → 80 [ACK] Seq=1 Ack=1 Win=62848 Len=0 TSval=2695625484 TSecr=4277139795
10	0.788807	100.64.47.199	52.78.83.169	HTTP	193	GET / HTTP/1.1 
11	0.790135	100.64.47.199	52.78.83.169	TCP	66	45854 → 80 [ACK] Seq=128 Ack=239 Win=62720 Len=0 TSval=2695625485 TSecr=4277139797
12	0.790165	100.64.47.199	52.78.83.169	TCP	66	45854 → 80 [ACK] Seq=128 Ack=854 Win=62208 Len=0 TSval=2695625485 TSecr=4277139797
13	0.790305	100.64.47.199	52.78.83.169	TCP	66	45854 → 80 [FIN, ACK] Seq=128 Ack=854 Win=62208 Len=0 TSval=2695625485 TSecr=4277139797
14	0.791595	100.64.47.199	52.78.83.169	TCP	66	45854 → 80 [ACK] Seq=129 Ack=855 Win=62208 Len=0 TSval=2695625487 TSecr=4277139799
15	1.482525	100.64.47.199	52.78.83.169	TCP	74	45858 → 80 [SYN] Seq=0 Win=62727 Len=0 MSS=8961 SACK_PERM TSval=2695626178 TSecr=0 WS=128
16	1.485640	100.64.47.199	52.78.83.169	TCP	66	45858 → 80 [ACK] Seq=1 Ack=1 Win=62848 Len=0 TSval=2695626181 TSecr=4277140492
```

#### iptables
위 와 같이 VPC 내부로 갈 때는 SNAT가 되지 않고, VPC 외부로 갈 때에는 AWS_VPC_K8S_CNI_EXTERNALSNAT 옵션에 의해 SNAT가 결정되는 이유는 AWS_VPC_K8S_CNI_EXTERNALSNAT 옵션 여부 활성화에 따라서 iptables가 달라지기 때문이다. 

AWS_VPC_K8S_CNI_EXTERNALSNAT 옵션 False일 때 노드의 iptables를 확인해보면 아래와 같다. 즉, any - any는 AWS-SNAT-CHAIN-0 Chain이 적용된다. 다시 목적지가 VPC대역(10.20.0.0/16)이면 SNAT되지 않고, VPC대역(10.20.0.0/16)이 아니면 다음 타겟인 AWS-SNAT-CHAIN-1이 적용된다. 마찬가지로 목적지가 VPC대역(100.64.0.0/16)이면 SNAT되지 않고, VPC대역(100.64.0.0/16)이 아니면 다음 타겟인 AWS-SNAT-CHAIN-2이 적용된다. AWS-SNAT-CHAIN-2 체인에서 모든 목적지의 주소에 대해서 SNAT된다. 

결국은 VPC 대역이면 SNAT되지 않고 파드의 IP가 그대로 사용되지만, VPC대역이 아니면 SNAT되어 노드의 IP가 사용된다. 

```bash
# 10.20.11.162 노드 접속하여 iptables

$ iptables -t nat -nvL POSTROUTING
Chain POSTROUTING (policy ACCEPT 32 packets, 2554 bytes)
 pkts  bytes target            prot opt in     out     source               destination
3303K  244M  KUBE-POSTROUTING  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* kubernetes postrouting rules */
   68  4730  AWS-SNAT-CHAIN-0  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* AWS SNAT CHAIN */

$ iptables -t nat -nvL AWS-SNAT-CHAIN-0
Chain AWS-SNAT-CHAIN-0 (1 references)
 pkts  bytes target            prot opt in     out     source               destination
   85  5919  AWS-SNAT-CHAIN-1  all  --  *      *       0.0.0.0/0            !10.20.0.0/16         /* AWS SNAT CHAIN */

$ iptables -t nat -nvL AWS-SNAT-CHAIN-1
Chain AWS-SNAT-CHAIN-1 (1 references)
 pkts  bytes target            prot opt in     out     source               destination
   52  3184  AWS-SNAT-CHAIN-2  all  --  *      *       0.0.0.0/0            !100.64.0.0/16        /* AWS SNAT CHAIN */

$ iptables -t nat -nvL AWS-SNAT-CHAIN-2
Chain AWS-SNAT-CHAIN-2 (1 references)
 pkts  bytes target            prot opt in     out     source               destination
   61  3740  SNAT              all  --  *      !vlan+  0.0.0.0/0            0.0.0.0/0            /* AWS, SNAT */ ADDRTYPE match dst-type !LOCAL to:10.20.11.162 random-fully
```

AWS_VPC_K8S_CNI_EXTERNALSNAT 옵션이 True일 때는 위의 SNAT 관련 Chain이 없다.  


## 10. [Trouble Shooting]()  

<br><br><br>

- References  
[1] 