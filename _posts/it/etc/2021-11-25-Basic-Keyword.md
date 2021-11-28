---
layout: post
title: "Basic - Keyword"
author: "Bys"
category: etc
date: 2021-11-22 01:00:00
tags: network
---

개념을 잘 정리할 수 있도록 키워드만 추려 정리

개념 - 장점 - 활용

### - Container  
`개념`  
```
Container란 OS Level의 가상화 기술로 리눅스 커널을 공유하면서 프로세스를 격리된 환경에서 실행하는 기술  
```

`역사`  
Linux cgroups, namespace -> Linux Container(LXC) 기반 기술  -> Docker

1. Linux cgroups 
cgroups는 유저 프로세스의 리소스 사용을 분리하여 관리하는 Linux 커널의 기능 -> 네임스페이스 내의 프로세스는 다른 네임스페이스의 프로세스로부터 독립적이므로 리소스로부터 안전  

2. LXC(Linux Containers)  
Linux cgroups, namespace 등의 기능을 통해 컨테이너라고 불리는 것의 가장 첫 번째 구현  
OS Level의 가상 환경  

`장점`  
1. 하드웨어를 가상화하는 가상 머신과 달리 커널을 공유하는 방식이기 때문에 실행 속도가 빠르고, 성능 상의 손실이 거의 없음  
2. Guest OS의 관리가 필요 없음  
3. Portability - 독자적인 실행 환경을 가지고 있어 이식성이 좋음  
4. Stateless - 특정 실행 환경을 쉽게 재사용할 수 있음  

`활용`  


<br>

### - Docker  
`개념`
```
컨테이너 기반의 오픈소스 가상화 플랫폼으로 컨테이너를 실행하고 관리하는 도구 (Container Runtime)  
Dockerfile, Docker image, Docker registries, Docker containers  
```

`역사`  
초기의 도커는 LXC를 기반으로 컨테이너를 생성하고 관리 -> 현재는 containerd, runC 기반으로 동작  
(OCI(Open Container Initiative)라는 Container Runtime 표준을 만들고 이 표준대로 Container Runtime을 만들기 시작했는데 Docker에서 만든 Container Runtime이 바로 containerd)  

`장점`

`활용`

<br>

### - 참고
cri-o는 쿠버네티스를 위한 경량 컨테이너 런타임 프로젝트