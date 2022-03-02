---
layout: post
title: "Git Command"
author: "Bys"
category: command
date: 2021-04-21 00:00:00
tags: git gitcommand
---


#### **- Git Config**

Windows의 경우 cmd창을 열고 아래의 명령어를 실행한다.
git을 설치했지만 명령어가 안되는 경우 환경변수 설정을 확인한다.   


`Git Config 설정`  
git 설치 후 환경 설정, commit 시 해당 이력이 남는다. 
```bash
git config --global user.name "John Doe"
git config --global user.email johndoe@example.com
```
<br>

User ID와 Password Credential 저장을 위해서는 아래와 같이 credential store 설정을 한다.  
```bash
git config credential.helper store
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


#### **- Git Clone**

`git clone`  
```
git clone https://github.com/USERNAME/git-url.git
```
git을 시작하며 git 레파지토리의 프로젝트를 복사해오거나 로컬의 프로젝트를 git레파지토리로 업로드 할 때 사용한다.

github에서 특정 project1을 나의 로컬로 복사해오고 싶다면 아래와 같이 명령을 수행한다.

```git
cd D:\dev\Workspace\project1 
git clone https://github.com/USERNAME/project1.git
```
<br>

`git init`  
나의 로컬 project1을 github로 push하고 싶다면 github에서 레파지토리를 만들고 아래의 명령을 수행한다.

```git
cd D:\dev\Workspace\project1
git init
git add --all
git commit -m "Initial Commit" 
git remote add origin https://github.com/USERNAME/project1.git 
git push -ur origin master 
```
<br>


#### **- Git Pull**
pull의 명령어는 원격저장소의 변경된 데이터를 로컬에 반영할 수 있다.

`git pull`

로컬데이터와 충돌이 없을 경우 변경된 데이터를 로컬에 반영할 때 (Fast-Foward)
각 Branch에 맞게 아래와 같이 사용 할 수 있다.

```git
git pull origin master
```
```git
git pull origin develop
```
<br>



일반적으로 변경된 데이터를 로컬에 반영하려고 git pull을 사용하였으나 로컬 데이터에도 변경이 발생하여 충돌이 발생한 경우는 충돌 된 부분을 수작업으로 반영하고 merge를 해주어야 한다.

다만, 원격저장소의 데이터를 기준으로 로컬 데이터를 강제로 덮어쓰고 싶다면 아래의 명령을 수행 하면 된다.

`git pull (overwrite force)`
```git
git fetch --all 
git reset --hard origin/master 
git pull origin master  
```
이렇게 명령을 수행하게 되면 원격저장소(origin)의 master를 기준으로 로컬데이터를 엎어쓰게 된다. 

<br><br> 