---
layout: post
title: "samba(삼바)를 이용한 Windows to Linux 데이터 이관"
author: "Bys"
category: cloud
date: 2021-03-04 01:00:00
tags: git gitcommand
---


## Samba를 이용한 Windows to Linux 데이터 이관

> 삼바(samba)는 Windows 운영체제를 사용하는 PC에서 Linux 또는 UNIX 서버에 접속하여 파일이나 프린터를 공유하여 사용할 수 있도록 해 주는 소프트웨어이다.
>> 출처: https://ko.wikipedia.org/wiki/%EC%82%BC%EB%B0%94_(%EC%86%8C%ED%94%84%ED%8A%B8%EC%9B%A8%EC%96%B4)



프로젝트에서 기존 On-Premise의 Windows 시스템을 Cloud Amazon Linux2로 이관하면서 On-Premise에서 사용하던 NAS를 AWS상에 EFS로 이관하는 방법에 대한 설명이다.


아래와 같이 AWS에서도 DataSync 서비스를 제공하여 이관 서비스를 제공해주고 있다.

> DataSync는 NFS(Network File System) 공유, SMB(Server Message Block) 공유, 자체 관리형 객체 스토리지, AWS Snowcone, Amazon Simple Storage Service(Amazon S3) 버킷, Amazon Elastic File System(Amazon EFS) 파일 시스템 및 Amazon FSx for Windows File Server 파일 시스템 간에 데이터를 복사할 수 있습니다.
>> 출처: https://aws.amazon.com/ko/datasync/?nc2=h_ql_prod_mt_ds&whats-new-cards.sort-by=item.additionalFields.postDateTime&whats-new-cards.sort-order=desc

하지만 이번에는 별도로 samba 설치하여 진행하기로 한다.

순서는 아래와 같다.

1. Linux 서버에서 samba를 설치 및 기동

2. /etc/samba/smb.conf 파일에 /smbdir mount
	
3. Copy할 서버(Windows)에 접속

4. 컴퓨터에서 우클릭 후 네트워크 드라이브 연결

5. 정보 입력 및 samba user password 입력

6. Windows서버에 smbdir mount되어 동기화 가능

7. robocopy명령어를 통해 SRC -> TARGET으로 데이터 동기화 진행
	robocopy /MIR /COPY:DT /DCOPY:T /ipg:6000 X:\data001\lgcgfiles K:\





