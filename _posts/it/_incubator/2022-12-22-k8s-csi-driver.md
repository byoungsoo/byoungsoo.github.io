---
layout: post
title: "kubernetes CSI Driver"
author: "Bys"
category: incubator
date: 2023-01-01 01:00:00
tags: kubernetes node
---

# [Kubernetes Volume](https://kubernetes.io/docs/concepts/storage/volumes/)

1. CSI?
The Container Storage Interface is a community-based project for developing a standardized API enabling communication between container orchestration (CO) platforms and storage plugins. In theory, a standardized communication protocol allows storage providers to write plugins more easily, to just one specification. 

CSI Driver is responsible for 
CSI Node - 



Temp
-------------------------
쿠버네티스 CSI 플러그인


쿠버네티스 CSI은 기본적으로 프로비저너(Provisioner), 어태쳐(Attacher), 컨트롤러(Controller), 노드서버(NodeServer)로 이루어져 있습니다.

- 프로비저너(Provisioner)
  - 프로비저너는 클러스터에 PVC가 생성되는 걸 모니터링하고 있다가 PVC가 생성되면 PV를 생성하는 걸 담당합니다.

- 어태쳐(Attacher)
  - 어태처는 파드가 PVC를 사용하려할 때 해당 컨테이너에서 PV를 마운트하는 걸 담당합니다.

- 컨트롤러(Controller)
  - 컨트롤러는 쿠버네티스 컨테이너에서 사용할 볼륨을 스토리지 서버에서 생성 및 삭제하는 걸 담당합니다.

- 노드서버(NodeServer)
  - 노드서버는 파드가 배포될 노드에서 스토리지 볼륨에 마운트할 수 있게 환경을 만드는 걸 담당합니다.


-------------------------
<br><br><br>

> Ref: https://docs.aws.amazon.com/ko_kr/AmazonECS/latest/developerguide/instance_IAM_role.html
> Ref: https://tech.gluesys.com/blog/2022/06/21/CSI.html


> device-plugin-registration - https://kubernetes.io/docs/concepts/extend-kubernetes/compute-storage-net/device-plugins/#device-plugin-registration
> pluginwatcher - https://github.com/kubernetes/kubernetes/blob/master/pkg/kubelet/pluginmanager/pluginwatcher/README.md
> CSI Spec - https://github.com/container-storage-interface/spec/blob/master/spec.md#rpc-interface