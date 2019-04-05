---
layout: post
title: "Openstack 용어 정리"
author: "Byoungsoo Ko"
category: cloud
date: 2019-04-03 18:00:00
---


Openstack
---

Openstack은 Public/Private Cloud 구축 및 운영할 수 있도록 지원하는 오픈 소스 기반의 클라우드 플랫폼  
<br/>
##### [Networking]
Neutron – 네트워크 자원(VLAN, vRouter) 생성/변경/삭제하는 기능, 사용자는 가상 네트워크를 생성하며 트래픽 및 VM간 연결을 제어할 수 있다.

Designate - DNS Service.

<br/>
##### [Compute]
Nova - 컴퓨팅 자원을 가상화하여 CPU, Memory를 할당, 여러 Hypervisor를 지원한다.

Glance - 서버 이미지 템플릿으로 VM생성시 사용. 백업 용도로 저장하여 사용하거나 동일한 서비스를 확장할 때 사용가능 하다.  

<br/>
##### [Storage]
Cinder - VM에서 사용하는 블록 단위의 스토리지, 스토리지 생성/삭제 등 관리하는 기능

Swift – 저장된 컨텐츠가 개별적 URL을 가져 웹으로 접간 가능한 스토리지 서비스.  

Manila - Shared Filesystems.

<br/>
##### [Database]
Trove - 관계형/비관계형 데이터베이스 엔진을 프로비저닝. API를 통해 제어하며, MySQL, Mongodb, postgre, redis, percona 등.  

Sahara - Hadoop Cluster를 서비스로 제공 (AWS EMR)

<br/>
##### [Identity, Key Management]
Keystone – 통합 인증 서비스를 제공, 서비스 카탈로그 및 정책을 정의 한다.  

<br/>
##### [Etc]
Horizon (Dashboard Service) - Admin 혹은 Clinet가 클라우드 자원을 제어할 수 있는 콘솔 화면

Heat (Orchestration) - 클라우드 환경을 Heat 템플릿을 이용하여 프로비저닝 하는 기능 (AWS CloudFormation)  
