---
layout: post
title: "AWS 용어 정리"
author: "Byoungsoo Ko"
category: cloud
date: 2019-04-01 18:00:00
tags: cloud aws
---
AWS - Amazon Web Service

--------------------

Region - 전 세계에 분산된 물리적 위치



AZ - Availability Zones

리전에서 운영되는 하나 이상의 개별 데이터 센터 ap-northeast-2 => 아시아 태평양 (서울)

AZ는 기본적으로 격리되어 있지만 한 리전의 여러 AZ는 지연 시간이 짧은 링크를 통해 연결되어 있다.



--------------------

CLI - Command Line Interface

AWS 서비스를 관리하는 통합 도구. 여러 AWS 서비스를 명령행에서 관리하고 스크립트를 통해 자동화 할 수 있다.


--------------------

Compute
Amazon EC2 - Amazon Elastic Compute Cloud

클라우드에서 안전하고 규모 조정이 가능한 컴퓨팅 파워를 제공하는 웹 서비스. (웹 서비스로 서버 생성)

웹 서비스 인터페이스를 통해 간편하게 EC2 인스턴스를 생성 가능하다.



AMI - Amazon Machine Images

EC2 인스턴스를 생성하기 위한 기본 파일. AWS에서는 빈 EC2 인스턴스에 직접 OS를 설치할 수 없다.

때문에 미리 OS가 설치된 AMI를 이용하여 EC2 인스턴스를 생성한다. AMI는 단순히 OS만 설치되는 것이 아니라

각 종 서버 애플리케이션, 데이터베이스, 방화벽 등의 네트워크 솔루션 등도 함께 설치 가능

루트 볼륨용 템플릿, 시작 권한, 블록 디바이스 맵핑



Amazon ECS - Amazon EC2 Container Service

컨테이너 서비스.



Amazon Lambda - Amazon Lambda



--------------------
Storage
Amazon S3 - Amazon Simple Storage Service

기본적으로 웹 서버 역할도 수행이 가능하다

객체 수준 스토리지 ( 폴더 파일을 사용하지 않고, 객체이름을 폴더처럼 구분하여 사용)

 99.99999999% 내구성 설계 (기본 3벌 복제, 3곳의 가용영역에 저장)

이벤트 트리거 설정 가능

Bucket을 만들고 특정 객체들을 Bucket으로 관리할 수 있음 ( Bucket 이름은 전 세계적으로 고유하게 설정)



Amazon EBS - Amazon Elastic Block Store

AWS 클라우드의 Amazon EC2 인스턴스에 사용할 영구 블록 스토리지 볼륨 제공

EC2는 EBS와 네트워크를 통해 연결(Network Storage)



Instance Storage

인스턴스 스토리지는 휘발성으로 실제 Host되는 서버에 되어 있는 저장소(고속)



Amazon Glacier - Amazon Glacier

데이터 보관 및 장기 백업을 위한 비용이 저렴한 스토리지 서비스.  아카이브 데이터 보관 느낌



AWS Storage Gateway - Amazon Storage Gateway

온프레미스 스토리지 환경과 AWS 클라우드 양쪽을 넘나들며 하이브리드 스토리지를 사용할 수 있다.



Amazon EFS - Amazon Elastic File System

디스크 공유가 가능한 네트워크 파일 스토리지

여러 인스턴스가 동일한 스토리지를 사용해야 하는 경우 사용(NAS - Network Attached Storage)

NFSv4 파일 시스템






--------------------

Database
Amazon RDS - Amazon Relational Database Service

Amazon Aurora, PostgreSQL, MySQL, MariaDB, Oracle, Microsoft SQL Server 등 선택 가능



Amazon Aurora - Amazon Aurora

Mysql 및 PostgreSQL과 호환되는 관계형 데이터베이스 엔진.

(고사양 상업용 데이터베이스 - 속도 및 가용성) + (오픈 소스 데이터베이스 - 단순성 및 비용 효율성 ) 결합



Amazon DynamoDB - Amazon DynamoDB

NoSQL 데이터베이스 서비스. 유연한 데이터 모델과 안정적인 성능으로 모바일, 웹, 게임, 광고 기술, 사물 인터넷 등에 적합



Amazon ElastiCache - Amazon ElastiCache

클라우드에서 인 메모리 캐시를 손쉽게 배포, 운영 및 조정할 수 있게 해주는 웹 서비스.



--------------------

Network & Contents Delivery
Amazon VPC - Amazon Virtual Private Cloud

사용자가 정의한 가상 네트워크에서 AWS 리소스를 시작할 수 있도록

AWS 클라우드에서 논리적으로 격리된 공간을 프로비저닝 가능.



Amazon Route 53 - Amazon Route 53

가용성과 확장성이 우수한 클라우드 DNS 웹 서비스.



Amazon ELB - Elastic Load Balancing

수신되는 애플리케이션 트래픽을 여러 EC2 인스턴스에 자동으로 배포.



Amazon CloudFront - Amazon CloudFront

웹 사이트, API, 도영상 콘텐츠 또는 기타 웹 자산의 전송을 가속화하는 CDN 서비스.



Amazon EIP - Amazon Elastic IP

기본적으로는 Instance생성 시 Public IP가 부여되고 재기동시 변경 되지만,고정 된 IP를 위한 서비스



Amazon NACL - Amazon Network Access Control List

Subnet 단위의 방화벽으로 기본 적으로는 모든 Inbound, Outbound 트래픽을 허용

상태 비저장 규칙으로 Inbound, Outbound Rule을 각각 따로 설정



--------------------

Migration
AWS Application Discovery Service - AWS Application Discovery Service

온프레미스 데이터 센터에서 실행되는 애플리케이션. 관련 종속성 및 성능 프로파일을 자동으로 식별하여

사업자가 빠르면서도 안정적으로 애플리케이션 마이그레이션 프로젝트를 계획.


AWS SMS - AWS Server Migration Service

에이전트 없는 서비스로 수천 개의 온프레미스 워크로드를 AWS로 쉽고 빠르게 마이그레이션 가능.


AWS Database Migration Service - AWS Database Migration Service

데이터베이스를 AWS로 간편하고 안전하게 마이그레이션 가능. 마이그레이션 하는 동안 소스 데이터베이스가 변함없이 운영되어

데이터베이스를 사용하는 애플리케이션의 가동 중지를 최소화 한다.



AWS Snowball - AWS Snowball

페타바이트 규모의 데이터 전송 솔루션으로 안전한 어플라이언스를 사용하여 AWS에서 대량의 데이터를 송수신.



AWS S3 Transfer Acceleration

중간에 CloudFront (Edge Location)을 통해서 S3 Bucket으로 전송



--------------------

Management Tools
Amazon CloudWatch- Amazon CloudWatch

AWS 클라우드 리소스 및 AWS에서 실행하는 애플리케이션을 모니터링하는 서비스



AWS OpsWorks- AWS OpsWorks

Chef를 사용하여 응용 프로그램 설정 및 실행을 돕는 서비스.

Elastic Beanstalk보다 자유도가 높다.

--------------------
