---
layout: post
title: "Kubernetes 클러스터 구축 [kubeadm]"
author: "Bys"
category: container
date: 2022-07-15 01:00:00
tags: kubernetes cluster
---

# Kubernetes
이번에는 개별 서버들에 kubeadm을 이용해서 Kubernetes Master, Worker를 직접 구축해보려고 한다.  
자세한 내용은 공식문서를 참고한다. [Creating a cluster with kubeadm](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/create-cluster-kubeadm/)  

kubeadm을 통해 구성할 때 필요사항은 아래와 같다.  
- Need
  - A compatible Linux host. The Kubernetes project provides generic instructions for Linux distributions based on Debian and Red Hat, and those distributions without a package manager.
  - 2 GB or more of RAM per machine (any less will leave little room for your apps).
  - 2 CPUs or more
  - Full network connectivity between all machines in the cluster
  - Unique hostname, MAC address, and product_uuid for every node 
    ```bash
    #product_uuid
    sudo cat /sys/class/dmi/id/product_uuid
    ```
  - Certain ports are open on your machines.
    [Ports and Protocol](https://kubernetes.io/docs/reference/ports-and-protocols/)
  - Swap disabled. You MUST disable swap in order for the kubelet to work properly.

<br>

- Environment
  - AWS 환경에서 작업을 진행
  - Master: 1대 (AmazonLinux, m5xlarge)
  - Worker: 1대 (AmazonLinux, t3.medium)

<br>

- Objectives  
  - Install a single control-plane Kubernetes cluster
  - Install a Pod network on the cluster so that your Pods can talk to each other


## 1. Master 노드, Worker 노드 환경 구성

### 1.1 Master 노드, Woker 노드 환경 구성

`hostname set`  
호스트명은 모두 Unique해야 하며 dns등록이 필요하다.(여기서는 hosts파일에 등록)  
```bash
sudo hostnamectl set-hostname kube-master-node1
sudo hostnamectl set-hostname kube-worker-node1
```

`swapoff`  
kubelet은 swap메모리 지원을 하지 않기 때문에 모두 off하는 것을 공식적으로 권장하고 있다.  
```bash
sudo swapoff -a
cat /proc/meminfo | grep -i swap
##Print
SwapCached:            0 kB
SwapTotal:             0 kB
SwapFree:              0 kB
```

`security-group`  
aws환경이므로 노드간 통신을 위해 security-group을 설정해야 한다. 
선 sg-master-node, sg-worker-node 두 개의 security group을 생성하여 모든 master 노드에는 sg-master-node, worker 노드에는 sg-worker-node를 적용한다.  

그리고 아래 공식문서를 참고해서 master노드와 worker노드간 필요한 통신그룹을 허용한다.  
[Ports and Protocol](https://kubernetes.io/docs/reference/ports-and-protocols/)


`kubeadm & kubelet & kubectl`  
상세 설치 페이지는 아래 공식문서를 참고한다.  
[Installing kubeadm](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/)

- kubeadm, kubelet, kubectl 설치  

```bash
cat <<EOF | sudo tee /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=https://packages.cloud.google.com/yum/repos/kubernetes-el7-\$basearch
enabled=1
gpgcheck=1
gpgkey=https://packages.cloud.google.com/yum/doc/yum-key.gpg https://packages.cloud.google.com/yum/doc/rpm-package-key.gpg
exclude=kubelet kubeadm kubectl
EOF

# Set SELinux in permissive mode (effectively disabling it)
sudo setenforce 0
sudo sed -i 's/^SELINUX=enforcing$/SELINUX=permissive/' /etc/selinux/config

sudo yum install -y kubelet kubeadm kubectl --disableexcludes=kubernetes

sudo systemctl enable --now kubelet
```
<br>

`container runtime`  
파드에서 컨테이너를 실행하기 위해서는 쿠버네티스는 Container Runtime을 사용한다. 
쿠버네티스는 사용자가 선택한 Container Runtime과의 인터페이스를 위해 기본적으로 CRI(Container Runtime Interface)를 사용한다. 
Kubeadm은 Container Runtime을 알려진 엔드포인트에서 자동적으로 찾아 설치하지만 발견되지 않는다면 오류를 발생시킨다.  

| Runtime                           | Path to Unix domain socket |
| :---                              | :--- |
| containerd                        | unix:///var/run/containerd/containerd.sock |
| CRI-O	                            | unix:///var/run/crio/crio.sock |
| Docker Engine (using cri-dockerd) | unix:///var/run/cri-dockerd.sock |

![kubeadm_container001](/assets/it/container/kubeadm/kubeadm_container001.png){: width="55%" height="auto"}
![kubeadm_container002](/assets/it/container/kubeadm/kubeadm_container002.png){: width="40%" height="auto"}  

kubernetes 1.24 버전에서는 docker 런타임 중단에 따라 기존 docker가 아닌 CRI플러그인으로써 containerd, CRI-O 등을 사용해야 한다.  
여기서는 CRI플러그인으로 containerd를 사용한다. 또한 OCI에 runC를 사용한다.  

자세한 내용은 아래 공식문서를 참고한다. [Container Runtimes](https://kubernetes.io/docs/setup/production-environment/container-runtimes/#containerd)  
여기서는 containerd를 이용한다. [Download containerd](https://github.com/containerd/containerd/blob/main/docs/getting-started.md)  

- container runtime 설치  
  ```bash
  wget https://github.com/containerd/containerd/releases/download/v1.6.6/containerd-1.6.6-linux-amd64.tar.gz
  tar Cxzvf /usr/local containerd-1.6.6-linux-amd64.tar.gz
  ```

- systemd 서비스 등록  
  ```bash
  vim /etc/systemd/system/containerd.service
  ```

  ```txt
  [Unit]
  Description=containerd container runtime
  Documentation=https://containerd.io
  After=network.target local-fs.target

  [Service]
  ExecStartPre=-/sbin/modprobe overlay
  ExecStart=/usr/local/bin/containerd
  Type=notify
  Delegate=yes
  KillMode=process
  Restart=always
  RestartSec=5
  # Having non-zero Limit*s causes performance problems due to accounting overhead
  # in the kernel. We recommend using cgroups to do container-local accounting.
  LimitNPROC=infinity
  LimitCORE=infinity
  LimitNOFILE=infinity
  # Comment TasksMax if your systemd version does not supports it.
  # Only systemd 226 and above support this version.
  TasksMax=infinity
  OOMScoreAdjust=-999

  [Install]
  WantedBy=multi-user.target
  ```

- systemd  
  ```bash
  systemctl daemon-reload
  systemctl enable --now containerd
  ```

- config.toml
  설치를 완료한 후 설정파일을 생성하고 필요한 설정 등을 추가하기 위해서는 containerd에서 제공하는 config 명령을 사용하여 default config 정보를 생성한다.  
  ```bash
  mkdir -p /etc/containerd
  containerd config default > /etc/containerd/config.toml
  ```

  자세한 내용은 아래 공식문서를 참고한다.  
  [containerd config](https://github.com/containerd/cri/blob/master/docs/config.md)



- runc
  ```
  wget https://github.com/opencontainers/runc/releases/download/v1.1.3/runc.amd64
  install -m 755 runc.amd64 /usr/local/sbin/runc
  ```

- CNI Plugin
  ```
  wget https://github.com/containernetworking/plugins/releases/download/v1.1.1/cni-plugins-linux-amd64-v1.1.1.tgz
  tar Cxzvf /opt/cni/bin cni-plugins-linux-amd64-v1.1.1.tgz
  ```

<br>

`container image(Optional)`  
만약 마스터 노드에 인턴텟이 되지 않는다면 아래의 문서를 추가로 참고한다.  
[without an internet connection](https://kubernetes.io/docs/reference/setup-tools/kubeadm/kubeadm-init/#without-internet-connection)

여기까지 모든 환경 구성이 끝나면 master, worker 각각 snapshot을 생성한다. 추후에 

### 1.2 Initializing Control-Plane Node  
공식 문서를 읽어보면 아래와 같은 글이 있다.  

> - (Recommended) If you have plans to upgrade this single control-plane kubeadm cluster to high availability you should specify the --control-plane-endpoint to set the shared endpoint for all control-plane nodes. Such an endpoint can be either a DNS name or an IP address of a load-balancer.  
    - 만약 고가용성 확보를 위한 클러스터를 구축하려면 --control-plane-endpoint를 설정해야 하며 해당 endpoint는 dns이름이나 로드밸런서의 ip로 endpoint로 설정해야 한다. 
      따라서 이번에는 ELB를 생성하여 ELB의 DNS Name을 endpoint로 설정해보려고 한다. NLB를 사용하였으며 TG 포트는 API-Server포트인 6443으로 하였고 동일하게 Listener설정도 6443포트로 맞췄다.  

> - Choose a Pod network add-on, and verify whether it requires any arguments to be passed to kubeadm init. Depending on which third-party provider you choose, you might need to set the --pod-network-cidr to a provider-specific value. See Installing a Pod network add-on.
    - Pod network을 위한 add-on을 선택하는 부분이며 필요한 경우 kubeadm init 인수로 전달해주어야 한다. --pod-network-cidr 을 사용한다.  
    [CNI Add-on](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/create-cluster-kubeadm/#pod-network)  
    

> - (Optional) kubeadm tries to detect the container runtime by using a list of well known endpoints. To use different container runtime or if there are more than one installed on the provisioned node, specify the --cri-socket argument to kubeadm. See Installing a runtime.
    - 아래와 같은 내용이 존재한다. CNI를 설치하지 않으면 ClusterDNS(CoreDNS)는 시작 하지못할 것이라는... 추후에 kubeadm init 후 다시 살펴본다. 여기서도 오류가 발생할 것이다. 
    Caution: This section contains important information about networking setup and deployment order. Read all of this advice carefully before proceeding. You must deploy a Container Network Interface (CNI) based Pod network add-on so that your Pods can communicate with each other. Cluster DNS (CoreDNS) will not start up before a network is installed.



> - (Optional) Unless otherwise specified, kubeadm uses the network interface associated with the default gateway to set the advertise address for this particular control-plane node's API server. To use a different network interface, specify the --apiserver-advertise-address=<ip-address> argument to kubeadm init. To deploy an IPv6 Kubernetes cluster using IPv6 addressing, you must specify an IPv6 address, for example --apiserver-advertise-address=fd00::101

kubeadm init 을 통해 Control Plane 을 구성한다. --control-plane-endpoint를 사용하여 endpoint를 제공하기 때문에 --apiserver-advertise-address 옵션은 사용하지 않는다. 
마스터 서버에서 아래와 같은 커맨드를 입력한다.  
```bash
kubeadm init \
--control-plane-endpoint "nlb-kube-master-a0dca3b259bf3238.elb.ap-northeast-2.amazonaws.com:6443" \
--upload-certs \
--pod-network-cidr "192.168.0.0/16"
```

해당 커맨드를 입력하였더니 오류가 발생했다.  
```txt
kubeadm init \
> --control-plane-endpoint "nlb-kube-master-a0dca3b259bf3238.elb.ap-northeast-2.amazonaws.com:6443" \
> --upload-certs \
> --pod-network-cidr "192.168.0.0/16"

[init] Using Kubernetes version: v1.24.3
[preflight] Running pre-flight checks
	[WARNING FileExisting-tc]: tc not found in system path
	[WARNING Hostname]: hostname "kube-master-node1" could not be reached
	[WARNING Hostname]: hostname "kube-master-node1": lookup kube-master-node1 on 10.20.0.2:53: no such host
error execution phase preflight: [preflight] Some fatal errors occurred:
	[ERROR FileContent--proc-sys-net-bridge-bridge-nf-call-iptables]: /proc/sys/net/bridge/bridge-nf-call-iptables does not exist
	[ERROR FileContent--proc-sys-net-ipv4-ip_forward]: /proc/sys/net/ipv4/ip_forward contents are not set to 1
[preflight] If you know what you are doing, you can make a check non-fatal with `--ignore-preflight-errors=...`
To see the stack trace of this error execute with --v=5 or higher
```
1. WARNING이지만 반드시 hostname을 DNS에 등록을 하자
    - 우선은 /etc/hosts에 추가 함
      ```
      vim /etc/hosts
      10.20.1.232 kube-master-node1
      10.20.1.67 kube-worker-node1
      ```
2. /proc/sys/net/bridge/bridge-nf-call-iptables does not exist
    - 정확한 원인은 추가 확인이 필요하다. 우선 아래와 같이 조치 후 넘어갔다.  
      ```bash
      modprobe br_netfilter
      echo 1 > /proc/sys/net/bridge/bridge-nf-call-iptables
      ```
3. /proc/sys/net/ipv4/ip_forward contents are not set to 1  
    - 마찬가지로 정학환 원인은 추가 확인 필요하다. 아래와 같이 조치 후 넘어갔다.  
      ```bash
      echo '1' > /proc/sys/net/ipv4/ip_forward
      ```
<br>

조치를 한 후 다시 kubeadm init 커맨드를 수행하면 아래와 같이 최종적으로 Your Kubernetes control-plane has initialized successfully! 메세지를 볼 수 있다. 

```bash
# ......생략
[bootstrap-token] Using token: 3qz1dn.ci5py76kh2jkuxad
[bootstrap-token] Configuring bootstrap tokens, cluster-info ConfigMap, RBAC Roles
[bootstrap-token] Configured RBAC rules to allow Node Bootstrap tokens to get nodes
[bootstrap-token] Configured RBAC rules to allow Node Bootstrap tokens to post CSRs in order for nodes to get long term certificate credentials
[bootstrap-token] Configured RBAC rules to allow the csrapprover controller automatically approve CSRs from a Node Bootstrap Token
[bootstrap-token] Configured RBAC rules to allow certificate rotation for all node client certificates in the cluster
[bootstrap-token] Creating the "cluster-info" ConfigMap in the "kube-public" namespace
[kubelet-finalize] Updating "/etc/kubernetes/kubelet.conf" to point to a rotatable kubelet client certificate and key
[addons] Applied essential addon: CoreDNS
[addons] Applied essential addon: kube-proxy

Your Kubernetes control-plane has initialized successfully!

To start using your cluster, you need to run the following as a regular user:

  mkdir -p $HOME/.kube
  sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
  sudo chown $(id -u):$(id -g) $HOME/.kube/config

Alternatively, if you are the root user, you can run:

  export KUBECONFIG=/etc/kubernetes/admin.conf

You should now deploy a pod network to the cluster.
Run "kubectl apply -f [podnetwork].yaml" with one of the options listed at:
  https://kubernetes.io/docs/concepts/cluster-administration/addons/ㅋ

You can now join any number of the control-plane node running the following command on each as root:

  kubeadm join nlb-kube-master-a0dca3b259bf3238.elb.ap-northeast-2.amazonaws.com:6443 --token 3qz1dn.ci5py76kh2jkuxad \
	--discovery-token-ca-cert-hash sha256:b43b7bfb924b8b08d915a4db98a286be911c6bd46849c0d2022f58ec5b834d34 \
	--control-plane --certificate-key 898832cb7763894e8f82e6f5930e043f43c50a5c5a0587ec28667a479ed8ae0d

Please note that the certificate-key gives access to cluster sensitive data, keep it secret!
As a safeguard, uploaded-certs will be deleted in two hours; If necessary, you can use
"kubeadm init phase upload-certs --upload-certs" to reload certs afterward.

Then you can join any number of worker nodes by running the following on each as root:

kubeadm join nlb-kube-master-a0dca3b259bf3238.elb.ap-northeast-2.amazonaws.com:6443 --token 3qz1dn.ci5py76kh2jkuxad \
	--discovery-token-ca-cert-hash sha256:b43b7bfb924b8b08d915a4db98a286be911c6bd46849c0d2022f58ec5b834d34
```
<br>

메세지 마지막 즈음 kubeadm join 메세지는 복사하여 Worker노드에서 실행해 주면 클러스터에 노드가 조인이 된다. (1.4에서 다시)  
위 메세지 중간에 Master 노드에서는 admin.conf를 ~/.kube/config 하위로 카피해주면 kubectl 커맨드가 동작하는 것을 확인 할 수 있다.  
```bash
kubectl get nodes
##Print
NAME                STATUS     ROLES           AGE     VERSION
kube-master-node1   NotReady   control-plane   7m39s   v1.24.3
```
```bash
kubectl get pods -A
##Print
NAMESPACE     NAME                                        READY   STATUS    RESTARTS   AGE
kube-system   coredns-6d4b75cb6d-bn4wg                    0/1     Pending   0          4m33s
kube-system   coredns-6d4b75cb6d-rtl2f                    0/1     Pending   0          4m33s
kube-system   etcd-kube-master-node1                      1/1     Running   2          4m46s
kube-system   kube-apiserver-kube-master-node1            1/1     Running   2          4m47s
kube-system   kube-controller-manager-kube-master-node1   1/1     Running   2          4m47s
kube-system   kube-proxy-ztlqf                            1/1     Running   0          4m33s
kube-system   kube-scheduler-kube-master-node1            1/1     Running   2          4m45s
```

동작 하는 것은 확인하였고 아직 node의 상태가 NotReady이며 coredns파드가 pending 상태이므로 상태를 확인해보았다.  
우선 노드의 경우는 아래와 같이 CNI 플러그인이 초기화 되지 않은 현상이 있었다.  
```bash
kubectl describe node kube-master-node1
##Print
#.....생략
Conditions:
  Type             Status  LastHeartbeatTime                 LastTransitionTime                Reason                       Message
  ----             ------  -----------------                 ------------------                ------                       -------
  MemoryPressure   False   Mon, 18 Jul 2022 07:39:37 +0000   Mon, 18 Jul 2022 07:29:07 +0000   KubeletHasSufficientMemory   kubelet has sufficient memory available
  DiskPressure     False   Mon, 18 Jul 2022 07:39:37 +0000   Mon, 18 Jul 2022 07:29:07 +0000   KubeletHasNoDiskPressure     kubelet has no disk pressure
  PIDPressure      False   Mon, 18 Jul 2022 07:39:37 +0000   Mon, 18 Jul 2022 07:29:07 +0000   KubeletHasSufficientPID      kubelet has sufficient PID available
  Ready            False   Mon, 18 Jul 2022 07:39:37 +0000   Mon, 18 Jul 2022 07:29:07 +0000   KubeletNotReady              container runtime network not ready: NetworkReady=false reason:NetworkPluginNotReady message:Network plugin returns error: cni plugin not initialized
```

coredns의 경우 Scheduling 에 실패했다. 노드가 not-ready상태여서 가능한 노드가 없는 상태이므로 해당 파드는 오류가 발생했다. 
먼저 노드를 정상 복구 시키는 일을 진행해야한다.  
```bash
kubectl describe pod coredns-6d4b75cb6d-bn4wg -n kube-system
##Print
#.....생략
Events:
  Type     Reason            Age                 From               Message
  ----     ------            ----                ----               -------
  Warning  FailedScheduling  2m6s (x3 over 12m)  default-scheduler  0/1 nodes are available: 1 node(s) had untolerated taint {node.kubernetes.io/not-ready: }. preemption: 0/1 nodes are available: 1 Preemption is not helpful for scheduling.
```
<br>

### 1.3 Calico CNI(Container Runtime Interface)  
위 에서 발생한 문제를 해결하기 위해 CNI Plugin을 설치해야 한다. CNI는 다른 노드들에 생성된 Pod(Container)간 통신을 위한 Interface 라고 생각하면 된다. 

Flannel, Calico 등 다양한 종류의 CNI가 존재하며 그 중에서도 이 번에는 가장 많이 사용되는 Calico를 설치해보려고 한다.  
쉽게 설치를 원한다면 Flannel로 설치를 하는 것이 좋다. Flannel은 쿠버네티스 요구사항을 충족하는 매우 간단한 overlay 네트워크이며 다른 플러그인에 비해서 설치 및 구성이 쉬운 것으로 확인된다.  

`Calico`  
자세한 사항은 공식 문서를 확인한다. [Calico Install](https://projectcalico.docs.tigera.io/getting-started/kubernetes/self-managed-onprem/onpremises#install-calico)  
Master 노드에서 아래 명령어를 수행한다.  
```bash
curl https://projectcalico.docs.tigera.io/manifests/calico.yaml -O
kubectl apply -f calico.yaml
```

배포를 진행하고 나면 calico가 배포된 것을 볼 수 있고 이 와 함께 coredns는 Running으로 상태 값이 변경 되었다.  
노드의 상태 또한 Ready상태로 변경되었다.  
```bash
kubectl get nodes
##Print
NAME                STATUS   ROLES           AGE   VERSION
kube-master-node1   Ready    control-plane   53m   v1.24.3
```
```bash
kubectl get pods -A
##Print
NAMESPACE     NAME                                        READY   STATUS    RESTARTS   AGE
kube-system   calico-kube-controllers-6766647d54-gvsn6    0/1     Pending   0          37s
kube-system   calico-node-5kx97                           1/1     Running   0          37s
kube-system   coredns-6d4b75cb6d-bn4wg                    1/1     Running   0          41m
kube-system   coredns-6d4b75cb6d-rtl2f                    1/1     Running   0          41m
kube-system   etcd-kube-master-node1                      1/1     Running   2          41m
kube-system   kube-apiserver-kube-master-node1            1/1     Running   2          41m
kube-system   kube-controller-manager-kube-master-node1   1/1     Running   2          41m
kube-system   kube-proxy-ztlqf                            1/1     Running   0          41m
kube-system   kube-scheduler-kube-master-node1            1/1     Running   2          41m
```

그런데 여기서 calico-kube-controllers가 pending 상태에서 진행되지 않았다.  
해당 이유는 기본적으로 클러스터에서 파드는 컨트롤 플레인에 스케쥴링 되지 않도록 보안상 되어있기 때문이다. 
```bash
kubecetl describe pod calico-kube-controllers-6766647d54-gvsn6 -n kube-system
##Print
#.....생략
Conditions:
  Type           Status
  PodScheduled   False
Volumes:
  kube-api-access-khfdc:
    Type:                    Projected (a volume that contains injected data from multiple sources)
    TokenExpirationSeconds:  3607
    ConfigMapName:           kube-root-ca.crt
    ConfigMapOptional:       <nil>
    DownwardAPI:             true
QoS Class:                   BestEffort
Node-Selectors:              kubernetes.io/os=linux
Tolerations:                 CriticalAddonsOnly op=Exists
                             node-role.kubernetes.io/master:NoSchedule
                             node.kubernetes.io/not-ready:NoExecute op=Exists for 300s
                             node.kubernetes.io/unreachable:NoExecute op=Exists for 300s
Events:
  Type     Reason            Age    From               Message
  ----     ------            ----   ----               -------
  Warning  FailedScheduling  3m14s  default-scheduler  0/1 nodes are available: 1 node(s) had untolerated taint {node-role.kubernetes.io/control-plane: }. preemption: 0/1 nodes are available: 1 Preemption is not helpful for scheduling.
```

따라서 아래와 같이 파드를 컨트롤 플레인에 올릴 수 있도록 설정한다.  
자세한 사항은 공식문서를 참고한다. [Control plane node isolation](https://17billion.github.io/kubernetes/2019/04/24/kubernetes_control_plane_working.html)  
```bash
kubectl taint nodes --all node-role.kubernetes.io/control-plane- node-role.kubernetes.io/master-
```

아래와 같이 정상적으로 calico-kube-controllers 까지 동작이 완료되었다.  
```bash
kubectl describe pod calico-kube-controllers-6766647d54-gvsn6 -n kube-system
##Print
#.....생략
QoS Class:                   BestEffort
Node-Selectors:              kubernetes.io/os=linux
Tolerations:                 CriticalAddonsOnly op=Exists
                             node-role.kubernetes.io/master:NoSchedule
                             node.kubernetes.io/not-ready:NoExecute op=Exists for 300s
                             node.kubernetes.io/unreachable:NoExecute op=Exists for 300s
Events:
  Type     Reason            Age   From               Message
  ----     ------            ----  ----               -------
  Warning  FailedScheduling  14m   default-scheduler  0/1 nodes are available: 1 node(s) had untolerated taint {node-role.kubernetes.io/control-plane: }. preemption: 0/1 nodes are available: 1 Preemption is not helpful for scheduling.
  Normal   Scheduled         10m   default-scheduler  Successfully assigned kube-system/calico-kube-controllers-6766647d54-gvsn6 to kube-master-node1
  Normal   Pulling           10m   kubelet            Pulling image "docker.io/calico/kube-controllers:v3.23.2"
  Normal   Pulled            10m   kubelet            Successfully pulled image "docker.io/calico/kube-controllers:v3.23.2" in 6.944828969s
  Normal   Created           10m   kubelet            Created container calico-kube-controllers
  Normal   Started           10m   kubelet            Started container calico-kube-controllers
```
```bash
kubectl get pods -A
##Print
NAMESPACE     NAME                                        READY   STATUS    RESTARTS   AGE
kube-system   calico-kube-controllers-6766647d54-gvsn6    1/1     Running   0          12m
kube-system   calico-node-5kx97                           1/1     Running   0          12m
kube-system   coredns-6d4b75cb6d-bn4wg                    1/1     Running   0          52m
kube-system   coredns-6d4b75cb6d-rtl2f                    1/1     Running   0          52m
kube-system   etcd-kube-master-node1                      1/1     Running   2          52m
kube-system   kube-apiserver-kube-master-node1            1/1     Running   2          52m
kube-system   kube-controller-manager-kube-master-node1   1/1     Running   2          52m
kube-system   kube-proxy-ztlqf                            1/1     Running   0          52m
kube-system   kube-scheduler-kube-master-node1            1/1     Running   2          52m
```
<br>

### 1.4 Worker노드 Join  
다시 Cluster구성으로 돌아와서 kubeadm init 후 join 커맨드를 확인한다. 

해당 커맨드는 master 노드가 추가 될 때 join되는 커맨드이다.  
```bash
# You can now join any number of the control-plane node running the following command on each as root:
kubeadm join nlb-kube-master-a0dca3b259bf3238.elb.ap-northeast-2.amazonaws.com:6443 --token 3qz1dn.ci5py76kh2jkuxad \
	--discovery-token-ca-cert-hash sha256:b43b7bfb924b8b08d915a4db98a286be911c6bd46849c0d2022f58ec5b834d34 \
	--control-plane --certificate-key 898832cb7763894e8f82e6f5930e043f43c50a5c5a0587ec28667a479ed8ae0d
```

해당 커맨드는 worker 노드가 추가 될 때 join되는 커맨드이다.  
```bash
#Then you can join any number of worker nodes by running the following on each as root:
kubeadm join nlb-kube-master-a0dca3b259bf3238.elb.ap-northeast-2.amazonaws.com:6443 --token 3qz1dn.ci5py76kh2jkuxad \
	--discovery-token-ca-cert-hash sha256:b43b7bfb924b8b08d915a4db98a286be911c6bd46849c0d2022f58ec5b834d34
```

먼저 worker노드에서 root로 위 커맨드를 수행해본다.  
```bash
kubeadm join kyle-nlb-kube-master-a0dca3b259bf3238.elb.ap-northeast-2.amazonaws.com:6443 --token 3qz1dn.ci5py76kh2jkuxad \
> --discovery-token-ca-cert-hash sha256:b43b7bfb924b8b08d915a4db98a286be911c6bd46849c0d2022f58ec5b834d34
##Print
[preflight] Running pre-flight checks
	[WARNING FileExisting-tc]: tc not found in system path
[preflight] Reading configuration from the cluster...
[preflight] FYI: You can look at this config file with 'kubectl -n kube-system get cm kubeadm-config -o yaml'
[kubelet-start] Writing kubelet configuration to file "/var/lib/kubelet/config.yaml"
[kubelet-start] Writing kubelet environment file with flags to file "/var/lib/kubelet/kubeadm-flags.env"
[kubelet-start] Starting the kubelet
[kubelet-start] Waiting for the kubelet to perform the TLS Bootstrap...

This node has joined the cluster:
* Certificate signing request was sent to apiserver and a response was received.
* The Kubelet was informed of the new secure connection details.

Run 'kubectl get nodes' on the control-plane to see this node join the cluster.
```

아래와 같이 worker노드가 추가 된 것을 확인할 수 있다.  
```bash
kubectl get nodes
##Print
NAME                STATUS   ROLES           AGE   VERSION
kube-master-node1   Ready    control-plane   67m   v1.24.3
kube-worker-node1   Ready    <none>          53s   v1.24.3
```


<br>





<br><br><br>

> Ref: https://kubernetes.io/ko/docs/setup/production-environment/tools/kubeadm/install-kubeadm/  
> Ref: https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/create-cluster-kubeadm/  
> Ref: https://lifeplan-b.tistory.com/155?category=886551  
> Ref: https://trylhc.tistory.com/entry/Containerd-%EC%84%A4%EC%B9%98-%EB%B0%8F-%EC%84%A4%EC%A0%95  
> Ref: https://projectcalico.docs.tigera.io/getting-started/kubernetes/self-managed-onprem/onpremises  