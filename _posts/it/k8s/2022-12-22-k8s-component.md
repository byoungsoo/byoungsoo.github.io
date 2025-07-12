---
layout: post
title: "Kubernetes Component"
author: "Bys"
category: k8s
date: 2022-12-22 01:00:00
tags: kubernetes node
---

# [Kubernetes Component](https://kubernetes.io/docs/concepts/overview/components/)

![kubernetes-component001](/assets/it/k8s/k8s/kubernetes-component001.png){: width="100%" height="auto"}  

다음은 Kubernetes를 설치한 이 후에 가장 기본적인 상태를 조회한 것이다.  
Pod를 보면 EKS에서는 볼 수 없었던 control plane 구성 요소인 kube-apiserver, kube-controller-manager, kube-scheduler, etcd 컴포너트를 볼 수 있다.  
해당 컴포넌트들은 모두 master node의 IP로 구성되어 있다. kube-proxy, calico-node는 daemonSet 형태로 구성되어있다. 
```bash
# kubectl get nodes -o wide
NAME                STATUS   ROLES           AGE   VERSION   INTERNAL-IP    EXTERNAL-IP   OS-IMAGE         KERNEL-VERSION                  CONTAINER-RUNTIME
kube-master-node1   Ready    control-plane   12h   v1.24.3   10.20.15.204   <none>        Amazon Linux 2   5.10.135-122.509.amzn2.x86_64   containerd://1.6.6
kube-master-node2   Ready    control-plane   12h   v1.24.3   10.20.14.123   <none>        Amazon Linux 2   5.10.130-118.517.amzn2.x86_64   containerd://1.6.6
kube-worker-node1   Ready    worker          12h   v1.24.3   10.20.14.27    <none>        Amazon Linux 2   5.10.130-118.517.amzn2.x86_64   containerd://1.6.6
kube-worker-node2   Ready    worker          12h   v1.24.3   10.20.15.49    <none>        Amazon Linux 2   5.10.157-139.675.amzn2.x86_64   containerd://1.6.6

# kubectl get pods -A -o wide
NAMESPACE     NAME                                        READY   STATUS    RESTARTS      AGE   IP                NODE                NOMINATED NODE   READINESS GATES
kube-system   kube-apiserver-kube-master-node1            1/1     Running   2             12h   10.20.15.204      kube-master-node1   <none>           <none>
kube-system   kube-apiserver-kube-master-node2            1/1     Running   0             12h   10.20.14.123      kube-master-node2   <none>           <none>
kube-system   kube-controller-manager-kube-master-node1   1/1     Running   3 (12h ago)   12h   10.20.15.204      kube-master-node1   <none>           <none>
kube-system   kube-controller-manager-kube-master-node2   1/1     Running   0             12h   10.20.14.123      kube-master-node2   <none>           <none>
kube-system   kube-scheduler-kube-master-node1            1/1     Running   3 (12h ago)   12h   10.20.15.204      kube-master-node1   <none>           <none>
kube-system   kube-scheduler-kube-master-node2            1/1     Running   0             12h   10.20.14.123      kube-master-node2   <none>           <none>
kube-system   etcd-kube-master-node1                      1/1     Running   11            12h   10.20.15.204      kube-master-node1   <none>           <none>
kube-system   etcd-kube-master-node2                      1/1     Running   0             12h   10.20.14.123      kube-master-node2   <none>           <none>

kube-system   coredns-6d4b75cb6d-2jlrc                    1/1     Running   0             12h   192.168.57.129    kube-master-node1   <none>           <none>
kube-system   coredns-6d4b75cb6d-dwn7l                    1/1     Running   0             12h   192.168.57.130    kube-master-node1   <none>           <none>
kube-system   calico-kube-controllers-84c476996d-9p6cm    1/1     Running   0             12h   192.168.238.193   kube-master-node2   <none>           <none>
kube-system   calico-node-6jsqs                           1/1     Running   0             12h   10.20.14.27       kube-worker-node1   <none>           <none>
kube-system   calico-node-8ghcs                           1/1     Running   0             12h   10.20.15.49       kube-worker-node2   <none>           <none>
kube-system   calico-node-j462m                           1/1     Running   0             12h   10.20.14.123      kube-master-node2   <none>           <none>
kube-system   calico-node-rpvc9                           1/1     Running   0             12h   10.20.15.204      kube-master-node1   <none>           <none>
kube-system   kube-proxy-527gr                            1/1     Running   0             12h   10.20.15.49       kube-worker-node2   <none>           <none>
kube-system   kube-proxy-gfvp9                            1/1     Running   0             12h   10.20.15.204      kube-master-node1   <none>           <none>
kube-system   kube-proxy-htqvz                            1/1     Running   0             12h   10.20.14.123      kube-master-node2   <none>           <none>
kube-system   kube-proxy-jkdkb                            1/1     Running   0             12h   10.20.14.27       kube-worker-node1   <none>           <none>

# kubectl get svc -A
NAMESPACE     NAME         TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)                  AGE
default       kubernetes   ClusterIP   10.96.0.1    <none>        443/TCP                  12h
kube-system   kube-dns     ClusterIP   10.96.0.10   <none>        53/UDP,53/TCP,9153/TCP   12h
```

다음은 기본으로 생성되어 있는 kube-dns의 서비스다. Endpoints의 주소는 192.168.57.129:53, 192.168.57.130:53 두 IP로 CoreDns Pod의 ClusterIP임을 알 수 있다.  
```bash
# kubectl describe svc kube-dns -n kube-system
Name:              kube-dns
Namespace:         kube-system
Labels:            k8s-app=kube-dns
                   kubernetes.io/cluster-service=true
                   kubernetes.io/name=CoreDNS
Annotations:       prometheus.io/port: 9153
                   prometheus.io/scrape: true
Selector:          k8s-app=kube-dns
Type:              ClusterIP
IP Family Policy:  SingleStack
IP Families:       IPv4
IP:                10.96.0.10
IPs:               10.96.0.10
Port:              dns  53/UDP
TargetPort:        53/UDP
Endpoints:         192.168.57.129:53,192.168.57.130:53
Port:              dns-tcp  53/TCP
TargetPort:        53/TCP
Endpoints:         192.168.57.129:53,192.168.57.130:53
Port:              metrics  9153/TCP
TargetPort:        9153/TCP
Endpoints:         192.168.57.129:9153,192.168.57.130:9153
Session Affinity:  None
Events:            <none>
```

다음은 기본으로 생성되어 있는 kubernetes 서비스다. Endpoints의 주소는 10.20.14.123:6443, 10.20.15.204:6443 두 IP로 MasterNode의 IP임을 알 수 있다.  
```bash
# kubectl describe svc kubernetes
Name:              kubernetes
Namespace:         default
Labels:            component=apiserver
                   provider=kubernetes
Annotations:       <none>
Selector:          <none>
Type:              ClusterIP
IP Family Policy:  SingleStack
IP Families:       IPv4
IP:                10.96.0.1
IPs:               10.96.0.1
Port:              https  443/TCP
TargetPort:        6443/TCP
Endpoints:         10.20.14.123:6443,10.20.15.204:6443
Session Affinity:  None
Events:            <none>
```


## 1. [Control Plane Components](https://kubernetes.io/docs/concepts/overview/components/#control-plane-components)  
[Constants and well-known values and paths](https://kubernetes.io/docs/reference/setup-tools/kubeadm/implementation-details/)  
Control Plane 컴포넌트들은 kubelet이 특정경로에서 static pod manifest를 읽어 동작 시킨다. 
/etc/kubernetes/manifests as the path where kubelet should look for static Pod manifests. 
* Static Pods are managed directly by the kubelet daemon on a specific node, without the API server observing them.  

```bash
ls -l /etc/kubernetes/manifests
total 16
-rw------- 1 root root 2279 Dec 20 03:10 etcd.yaml
-rw------- 1 root root 3338 Dec 20 03:10 kube-apiserver.yaml
-rw------- 1 root root 2850 Dec 20 03:10 kube-controller-manager.yaml
-rw------- 1 root root 1435 Dec 20 03:10 kube-scheduler.yaml
```

### kube-apiserver
kube-apiserver는 Kubernetes의 API서버다. [kube-apiserver](https://kubernetes.io/docs/reference/command-line-tools-reference/kube-apiserver/)

### etcd
Consistent and highly-available key value store used as Kubernetes' backing store for all cluster data.  
etcd는 Kubernetes에서 사용하는 key-value 저장소이다. Kubernetes cluster와 관련된 데이터는 etcd에 저장되므로 backup plan을 준비해야 한다.  

### kube-scheduler 
Control plane component that watches for newly created Pods with no assigned node, and selects a node for them to run on.  
노드가 할당되지 않은 신규로 생성된 pod를 감지하고, pod가 실행될 노드를 선택한다.  

### kube-controller-manager 
Control plane component that runs controller processes.
Logically, each controller is a separate process, but to reduce complexity, they are all compiled into a single binary and run in a single process.

Controller 프로세스를 실행하는 프로세스다. 각 controller는 모두 분리된 프로세스지만 복잡성을 줄이기 위해서 모두 하나의 binary로 컴파일 되어 single process로 동작한다.  

- Node controller: Responsible for noticing and responding when nodes go down.
  - node controller는 노드의 상태를 모니터링하며 API서버에 주기적으로 노드의 상태를 알린다. (주기설정: node-monitor-period / default: 5초)

- Job controller: Watches for Job objects that represent one-off tasks, then creates Pods to run those tasks to completion.
- EndpointSlice controller: Populates EndpointSlice objects (to provide a link between Services and Pods).
- ServiceAccount controller: Create default ServiceAccounts for new namespaces.

### clud-controller-manaoger 
A Kubernetes control plane component that embeds cloud-specific control logic.

As with the kube-controller-manager, the cloud-controller-manager combines several logically independent control loops into a single binary that you run as a single process. 
cloud-controller-manter 또한 kube-controller-manager와 마찬가지로 논리적으로 여러 독립적인 컨트롤 루프를 single binary로 합쳐 단일 프로세스로 동작한다. 

- Node controller: For checking the cloud provider to determine if a node has been deleted in the cloud after it stops responding
  - node controller는 AWS와 같은 CSP사가 제공하는 node가 응답을 멈춘 후에 cloud에서 삭제되었는지 판별하기 위해 있는 controller다. 
- Route controller: For setting up routes in the underlying cloud infrastructure
- Service controller: For creating, updating and deleting cloud provider load balancers

<br>

## 2. [Node Components](https://kubernetes.io/docs/concepts/overview/components/#node-components)  
- Node components run on every node.
- Maintaining running pods and providing the Kubernetes runtime environment.


### kubelet 
- An agent that runs on each node in the cluster. 
- It makes sure that containers are running in a Pod.
- The kubelet takes a set of PodSpecs that are provided through various mechanisms
- Ensures that the containers described in those PodSpecs are running and healthy. 
- The kubelet doesn't manage containers which were not created by Kubernetes.

#### [kubelet pkg](https://github.com/kubernetes/kubernetes/tree/master/pkg/kubelet)
참고로 kubelet은 다음과 같은 바이너리 소스들을 포함하여 역할을 수행한다. 
- cadvisor
- cri
- eviction
- images
- volumemanager
- metrics
- network
- nodestatus
- pleg(pod lifecycle event generator)
- pluginmanster
- ......

### kube-proxy 
- kube-proxy is a network proxy that runs on each node in your cluster.
- Implementing part of the Kubernetes Service concept.
- kube-proxy maintains network rules on nodes.
- kube-proxy uses the operating system packet filtering layer if there is one and it's available. Otherwise, kube-proxy forwards the traffic itself
- 

### Container runtime
- The container runtime is the software that is responsible for running containers.
- containerd, CRI-O, and any other implementation of the Kubernetes CRI (Container Runtime Interface).


## 3. AddOns
Addons use Kubernetes resources DaemonSet, Deployment, etc) to implement cluster features.

### DNS 
- All Kubernetes clusters should have cluster DNS
- Cluster DNS is a DNS server in addition to the other DNS server(s) in your environment, which serves DNS records for Kubernetes services.
- Containers started by Kubernetes automatically include this DNS server

일반적으로 CoreDns를 사용하며 Kubernetes에 의해 생성된 Pod는 /etc/resolv.conf 설정에 nameserver로 아래의 kube-dns 서비스 주소를 갖는다. 그리고 Endpoints로는 CoreDns의 Pod IP가 설정된다. 
```bash
$ kubectl get svc kube-dns -kube-system
NAMESPACE     NAME         TYPE        CLUSTER-IP    EXTERNAL-IP   PORT(S)         AGE
kube-system   kube-dns     ClusterIP   172.20.0.10   <none>        53/UDP,53/TCP   8d

kubectl describe svc kube-dns -n kube-system
Name:              kube-dns
Namespace:         kube-system
Labels:            eks.amazonaws.com/component=kube-dns
                   k8s-app=kube-dns
                   kubernetes.io/cluster-service=true
                   kubernetes.io/name=CoreDNS
Annotations:       prometheus.io/port: 9153
                   prometheus.io/scrape: true
Selector:          k8s-app=kube-dns
Type:              ClusterIP
IP Family Policy:  SingleStack
IP Families:       IPv4
IP:                172.20.0.10
IPs:               172.20.0.10
Port:              dns  53/UDP
TargetPort:        53/UDP
Endpoints:         10.20.11.154:53,10.20.11.156:53
Port:              dns-tcp  53/TCP
TargetPort:        53/TCP
Endpoints:         10.20.11.154:53,10.20.11.156:53
Session Affinity:  None
Events:            <none>
```

그럼 요청을 받은 CoreDns는 어떻게 동작을 할까? 
우선 [Pod's DNS Policy](https://kubernetes.io/docs/concepts/services-networking/dns-pod-service/#pod-s-dns-policy)를 알고 있을 필요가 있다.
- ClusterFirst  
Any DNS query that does not match the configured cluster domain suffix, such as "www.kubernetes.io", is forwarded to an upstream nameserver by the DNS server. 
- Default  
The Pod inherits the name resolution configuration from the node that the Pods run on. See related discussion for more details.

기본적으로 Pod를 생성할 때 'spec.dnsPolicy'를 설정하지 않으면 Default 값은 'ClusterFirst'이다. 하지만 CoreDns는 Deployment 설정에 'spec.dnsPolicy: Default'로 설정된다. 
따라서 노드로부터 DNS설정을 상속받기 때문에 노드의 DNS정보가 들어가게 된다.  



---

## 📚 References

[1] **Kubernetes Components**  
- https://kubernetes.io/docs/concepts/overview/components/
