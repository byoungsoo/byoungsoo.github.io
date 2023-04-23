---
layout: post
title: "CTR Containerd Command"
author: "Bys"
category: command
date: 2022-12-30 01:00:00
tags: cli containerd 
---

## Containerd
Kubernetes v1.24 부터는 Dockershim 지원은 종료되고 CRI표준을 따르는 Container Runtime환경으로 제공한다. Containerd와 CRI-O등을 사용한다. EKS에서는 Containerd를 Runtime 환경으로 제공하기 때문에 이에 대한 커맨드를 알 필요가 생겼다. 

## ctr cli
```bash
ctr image list
ctr image delete 

ctr image pull --user 유저명:비밀번호 이미지명(전체경로)
# Ex
```bash
$ ctr image pull --user AWS:$(aws ecr get-login-password --region ap-northeast-2) 602401143452.dkr.ecr.ap-northeast-2.amazonaws.com/eks/pause:3.5
602401143452.dkr.ecr.ap-northeast-2.amazonaws.com/eks/pause:3.5:                  resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:529cf6b1b6e5b76e901abc43aee825badbd93f9c5ee5f1e316d46a83abbce5a2:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:666eebd093e91212426aeba3b89002911d2c981fefd8806b1a0ccb4f1b639a60: done           |++++++++++++++++++++++++++++++++++++++|
layer-sha256:0692f38991d53a0c28679148f99de26a44d630fda984b41f63c5e19f839d15a6:    done           |++++++++++++++++++++++++++++++++++++++|
config-sha256:6996f8da07bd405c6f82a549ef041deda57d1d658ec20a78584f9f436c9a3bb7:   done           |++++++++++++++++++++++++++++++++++++++|
elapsed: 0.2 s                                                                    total:  741.0  (3.6 KiB/s)
unpacking linux/amd64 sha256:529cf6b1b6e5b76e901abc43aee825badbd93f9c5ee5f1e316d46a83abbce5a2...
done: 35.246662ms
```