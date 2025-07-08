---
layout: post
title: "EKS multus CNI"
author: "Bys"
category: cloud
date: 2025-03-17 01:00:00
tags: multus eks  
---

# [EKS Multus CNI](https://github.com/k8snetworkplumbingwg/multus-cni?tab=readme-ov-file#how-it-works)
Multus CNI는 여러 네트워크 인터페이스를 파드에 연결할 수 있게 해주는 쿠버네티스용 컨테이너 네트워크 인터페이스(CNI) 플러그인이다. 일반적으로 쿠버네티스에서 각 파드는 루프백을 제외하고 하나의 네트워크 인터페이스만 가지고 있지만, Multus를 사용하면 여러 인터페이스를 가진 multi-homed 파드를 생성할 수 있다. 이는 Multus가 다른 여러 CNI 플러그인을 호출할 수 있는 CNI 플러그인인 "메타 플러그인" 역할을 함으로써 이루어진다.  

다음은 멀티스 CNI에서 프로비저닝한 파드에 연결된 네트워크 인터페이스의 그림이다. 이 다이어그램은 eth0, net0, net1 세 가지 인터페이스가 있는 파드를 보여준다. eth0 는 쿠버네티스 클러스터 네트워크를 연결하여 쿠버네티스 서버/서비스(예: kube-apiserver, kubelet 등)와 연결한다. net0, net1 은 추가 네트워크 연결이며 다른 CNI 플러그인(예: vlan/vxlan/ptp)을 사용하여 다른 네트워크에 연결한다.

![multus-cni](/assets/it/cloud/eks/multus-cni.png){: width="60%" height="auto"}


### [Install multus on EKS](https://github.com/aws-samples/eks-install-guide-for-multus/blob/main/README.md), [Install multus](https://github.com/aws-samples/eks-install-guide-for-multus/blob/main/cfn/templates/nodegroup/README.md#install-multus), [Manifest on amazon-vpc-cni-k8s](https://github.com/aws/amazon-vpc-cni-k8s/tree/master/config/multus)


`Deploy multus cni`
```bash
kubectl apply -f https://github.com/aws/amazon-vpc-cni-k8s/blob/master/config/multus/v4.1.4-eksbuild.3/multus-daemonset-thick.yml
```

`ipvlcan configuration`
이 manifest를 만들 때 master 에는 실제 인터페이스 (eth0, eht1, ens5, ens6) 를 지정해주어야 한다. `ip link show` 커맨드를 통해 확인할 수 있다.  
```yaml
apiVersion: "k8s.cni.cncf.io/v1"
kind: NetworkAttachmentDefinition
metadata:
  name: ipvlan-conf-1
spec:
  config: '{
      "cniVersion": "0.3.0",
      "type": "ipvlan",
      "master": "ens6",
      "mode": "l2",
      "ipam": {
        "type": "host-local",
        "subnet": "100.64.0.0/19",
        "rangeStart": "100.64.30.10",
        "rangeEnd": "100.64.30.100",
        "gateway": "100.64.0.1"
      }
    }'
```

### [EKS multus Docs](https://docs.aws.amazon.com/eks/latest/userguide/pod-multiple-network-interfaces.html)
- Amazon EKS won’t be building and publishing single root I/O virtualization (SR-IOV) and Data Plane Development Kit (DPDK) CNI plugins. However, you can achieve packet acceleration by connecting directly to Amazon EC2 Elastic Network Adapters (ENA) through Multus managed host-device and ipvlan plugins.

- Amazon EKS is supporting Multus, which provides a generic process that enables simple chaining of additional CNI plugins. Multus and the process of chaining is supported, but AWS won’t provide support for all compatible CNI plugins that can be chained, or issues that may arise in those CNI plugins that are unrelated to the chaining configuration.

- Amazon EKS is providing support and life cycle management for the Multus plugin, but isn’t responsible for any IP address or additional management associated with the additional network interfaces. The IP address and management of the default network interface utilizing the Amazon VPC CNI plugin remains unchanged.

- Only the Amazon VPC CNI plugin is officially supported as the default delegate plugin. You need to modify the published Multus installation manifest to reconfigure the default delegate plugin to an alternate CNI if you choose not to use the Amazon VPC CNI plugin for primary networking.

- Multus is only supported when using the Amazon VPC CNI as the primary CNI. We do not support the Amazon VPC CNI when used for higher order interfaces, secondary or otherwise.

- To prevent the Amazon VPC CNI plugin from trying to manage additional network interfaces assigned to Pods, add the following tag to the network interface:
  - key: node.k8s.amazonaws.com/no_manage
  - value: : true

- Multus is compatible with network policies, but the policy has to be enriched to include ports and IP addresses that may be part of additional network interfaces attached to Pods.


### Test
아래의 샘플 파드를 배포해보면 아래와 같이 eth0 가 CNI로 부터 할당되고, net1 이 ipvlan-conf-1 에 의해서 제공되고 있음을 확인할 수 있다.  

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: sampleapp-1
  annotations:
      k8s.v1.cni.cncf.io/networks: ipvlan-conf-1
spec:
  containers:
  - name: network-multitool
    command: ["sh", "-c", "trap : TERM INT; sleep infinity & wait"]
    image: praqma/network-multitool
```

`describe`
```bash
│ Events:
│   Type    Reason          Age   From               Message
│   ----    ------          ----  ----               -------
│   Normal  Scheduled       4s    default-scheduler  Successfully assigned default/sampleapp-1 to ip-10-20-136-182.ap-northeast-2.compute.internal
│   Normal  AddedInterface  4s    multus             Add eth0 [10.20.139.27/32] from aws-cni
│   Normal  AddedInterface  3s    multus             Add net1 [100.64.30.10/19] from default/ipvlan-conf-1
│   Normal  Pulling         3s    kubelet            Pulling image "praqma/network-multitool"
```

`kubectl get po -o wide`
```bash
NAME          READY   STATUS    RESTARTS   AGE   IP             NODE                                              NOMINATED NODE   READINESS GATES
sampleapp-1   1/1     Running   0          27s   10.20.139.27   ip-10-20-136-182.ap-northeast-2.compute.internal   <none>           <none>
```


`kubectl exec sampleapp-1 -- ip -d address`
```bash
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00 promiscuity 0 minmtu 0 maxmtu 0 numtxqueues 1 numrxqueues 1 gso_max_size 65536 gso_max_segs 65535
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host
       valid_lft forever preferred_lft forever
3: eth0@if71: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 9001 qdisc noqueue state UP group default
    link/ether ba:67:e2:85:23:52 brd ff:ff:ff:ff:ff:ff link-netnsid 0 promiscuity 0 minmtu 68 maxmtu 65535
    veth numtxqueues 2 numrxqueues 2 gso_max_size 65536 gso_max_segs 65535
    inet 10.20.139.27/32 scope global eth0
       valid_lft forever preferred_lft forever
    inet6 fe80::b867:e2ff:fe85:2352/64 scope link
       valid_lft forever preferred_lft forever
4: net1@if4: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 9001 qdisc noqueue state UNKNOWN group default
    link/ether 0a:a3:dd:85:5b:e7 brd ff:ff:ff:ff:ff:ff link-netnsid 0 promiscuity 0 minmtu 68 maxmtu 65535
    ipvlan  mode l2 bridge numtxqueues 1 numrxqueues 1 gso_max_size 65536 gso_max_segs 65535
    inet 100.64.30.10/19 brd 100.64.31.255 scope global net1
       valid_lft forever preferred_lft forever
    inet6 fe80::aa3:dd00:285:5be7/64 scope link
       valid_lft forever preferred_lft forever
```

<br>


### SR-IOV (Single Root - IO Virtualization)
SR-IOV란 하나의 물리적 PCIe 장치(주로 네트워크 카드)를 여러 가상 기능(Virtual Functions, VF)으로 나누어 여러 VM이나 컨테이너에게 직접 할당할 수 있는 가상화 기술이다. 이를 통해 가상화된 환경에서도 거의 네이티브에 가까운 I/O 성능을 얻을 수 있다.
다만, [EKS 문서](https://docs.aws.amazon.com/eks/latest/userguide/pod-multiple-network-interfaces.html) 에서는 아래와 같이 SR-IOV, DPDK 를 building/publishing 하지 않을 것을 설명한다. 대신 multus 의 host-device, ipvlan 플러그인을 통해 EC2 ENA에 직접 연결함으로서 패킷 가속화를 할 수 있음을 설명한다. 따라서, SR-IOV를 설치하는 것이 의미가 없는 것으로 확인되지만 아래에서는 만약 설치시 어떻게 진행해야 하는지를 설명한다.  

> Amazon EKS won’t be building and publishing single root I/O virtualization (SR-IOV) and Data Plane Development Kit (DPDK) CNI plugins. However, you can achieve packet acceleration by connecting directly to Amazon EC2 Elastic Network Adapters (ENA) through Multus managed host-device and ipvlan plugins.


먼저 EC2에 접속해서 벤더, 장치, 드라이버를 확인한다.  
```bash
$ sudo yum install pciutils
$ lspci -nn | grep Ethernet
00:05.0 Ethernet controller [0200]: Amazon.com, Inc. Elastic Network Adapter (ENA) [1d0f:ec20]
00:06.0 Ethernet controller [0200]: Amazon.com, Inc. Elastic Network Adapter (ENA) [1d0f:ec20]


$ ethtool -i ens5
driver: ena
version: 2.13.2g
firmware-version:
expansion-rom-version:
bus-info: 0000:00:05.0
supports-statistics: yes
supports-test: no
supports-eeprom-access: no
supports-register-dump: no
supports-priv-flags: yes
```

확인이 완료되면 sriov-configmap에 아래와 같이 수정한다.  
`sriov-configmap.yaml`
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: sriovdp-config
  namespace: kube-system
data:
  config.json: |
    {
        "resourceList": [
            {
                "resourceName": "aws_ena_vf",
                "selectors": {
                    "vendors": ["1d0f"],
                    "devices": ["ec20"],
                    "drivers": ["ena"]
                }
            }
        ]
    }
```

이 후, [SR-IOV 문서](https://github.com/k8snetworkplumbingwg/sriov-network-device-plugin?tab=readme-ov-file#sr-iov-network-device-plugin-for-kubernetes)에 따라 데몬셋을 배포하고 설치를 진행하면 EKS 워커노드에 intel.com/aws_ena_vf 장치가 추가로 할당된 것을 확인할 수 있다.  
```bash
----------------------------------------------------------------
│ Capacity:
│   cpu:                   2
│   ephemeral-storage:     20893676Ki
│   hugepages-1Gi:         0
│   hugepages-2Mi:         0
│   intel.com/aws_ena_vf:  2
│   memory:                3876528Ki
│   pods:                  29
│ Allocatable:
│   cpu:                   1930m
│   ephemeral-storage:     18181869946
│   hugepages-1Gi:         0
│   hugepages-2Mi:         0
│   intel.com/aws_ena_vf:  2
│   memory:                3186352Ki
│   pods:                  29
----------------------------------------------------------------
```
<br><br><br>

