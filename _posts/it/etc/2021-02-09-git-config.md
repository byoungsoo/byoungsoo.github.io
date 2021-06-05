---
layout: post
title: "git config 설정 - git 사용법#1"
author: "Bys"
category: etc
date: 2021-02-09 00:00:00
tags: git gitcommand
---



Windows의 경우 cmd창을 열고 아래의 명령어를 실행한다.

git을 설치했지만 명령어가 안되는 경우 환경변수 설정을 확인한다. 

<br>

`Git Config 설정`

git 설치 후 환경 설정, commit 시 해당 이력이 남는다. 
```bash
git config --global user.name "John Doe"
git config --global user.email johndoe@example.com
```
<br>

User ID와 Password Credential 저장을 위해서는 아래와 같이 credential store 설정을 한다.  
```bash
git config credential.helpler store
```
<br>

`Git Config 조회`

설정이 완료 되면 아래 명령어로 환경변수 적용을 확인 할 수 있다.
```bash
git config --list
user.name=John Doe
user.email=johndoe@example.com
color.status=auto
color.branch=auto
color.interactive=auto
color.diff=auto
...
```
<br><br>
Git Clone
```
git clone https://github.com/USERNAME/git-url.git
```