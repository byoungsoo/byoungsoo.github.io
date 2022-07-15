---
layout: post
title: "Kubernetes 클러스터 구축"
author: "Bys"
category: container
date: 2022-07-15 01:00:00
tags: kubernetes cluster
---

# Kubernetes
이번에는 개별 서버들에 kubeadm을 이용해서 Kubernetes Master, Worker를 직접 구축해보려고 한다.  
자세한 내용은 아래를 참고한다.  
[Creating a cluster with kubeadm](https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/create-cluster-kubeadm/)

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

구성하기 위한 환경으로는 AWS의 EC2를 사용하였다. 

- 환경 
  - Master: 1대 (AmazonLinux, m5xlarge)
  - Worker: 1대 (AmazonLinux, t3.medium)


- Objectives
Install a single control-plane Kubernetes cluster
Install a Pod network on the cluster so that your Pods can talk to each other


## 1. Master 노드, Worker 노드 환경 구성

### 1.1 Master 노드, Woker 노드 환경 구성

```bash
yum update -y
sudo hostnamectl set-hostname kube-master-node1
sudo hostnamectl set-hostname kube-worker-node1
```

만약 마스터 노드에 인턴텟이 되지 않는다면 아래의 문서를 추가로 참고한다. 
https://kubernetes.io/docs/reference/setup-tools/kubeadm/kubeadm-init/#without-internet-connection


<br><br><br>

> Ref: https://kubernetes.io/ko/docs/setup/production-environment/tools/kubeadm/install-kubeadm/
> Ref: https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/create-cluster-kubeadm/
> Ref: https://lifeplan-b.tistory.com/155?category=886551
