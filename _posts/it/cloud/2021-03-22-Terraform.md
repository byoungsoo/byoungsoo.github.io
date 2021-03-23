---
layout: post
title: "Terraform 사용법, 시작부터 끝까지"
author: "Bys"
category: cloud
date: 2021-03-22 01:00:00
tags: terraform
---


Terraform 사용하기  

#### 사전지식  

**- HCLHashicorp Configuration Language**  
테라폼에서 사용하는 설정 언어. 테라폼에서 모든 설정과 리소스 선언은 HCL을 사용하며 HCL 파일의 확장자는 .tf를 사용한다.


**- 계획Plan**  
테라폼 프로젝트 디렉터리 아래의 모든 .tf 파일의 내용을 실제로 적용 가능한지 확인하는 작업을 계획이라고 한다. 테라폼은 이를 terraform plan 명령어로 제공하며, 이 명령어를 실행하면 어떤 리소스가 생성되고, 수정되고, 삭제될지 계획을 보여준다.

**- 적용Apply**  
테라폼 프로젝트 디렉터리 아래의 모든 .tf 파일의 내용대로 리소스를 생성, 수정, 삭제하는 일을 적용이라고 한다.  

>참고: https://www.44bits.io/ko/post/terraform_introduction_infrastrucute_as_code#%ED%85%8C%EB%9D%BC%ED%8F%BC-%EB%B2%84%EC%A0%84-%EA%B4%80%EB%A6%AC
