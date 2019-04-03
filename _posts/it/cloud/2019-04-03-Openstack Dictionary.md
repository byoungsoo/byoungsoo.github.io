---
layout: post
title: "Openstack 용어 정리"
author: "Byoungsoo Ko"
category: cloud
---


###Openstack
Openstack은 Public/Private Cloud 구축 및 운영할 수 있도록 지원하는 오픈 소스 기반의 클라우드 플랫폼


[Compute]
Nova - 컴퓨팅 자원을 가상화하여 CPU, Memory를 할당, 여러 Hypervisor를 지원한다.

[Networking]
Neutron – 네트워크 자원(VLAN, vRouter) 생성/변경/삭제하는 기능, 사용자는 가상 네트워크를 생성하며 트래픽 및 VM간 연결을 제어할 수 있다.

[Storage]
Cinder - VM에서 사용하는 블록 단위의 스토리지, 스토리지 생성/삭제 등 관리하는 기능

Swift – 저장된 컨텐츠가 개별적 URL을 가져 웹으로 접간 가능한 스토리지 서비스.

[Identity]
Keystone – 통합 인증 서비스를 제공, 서비스 카탈로그 및 정책을 정의 한다.
