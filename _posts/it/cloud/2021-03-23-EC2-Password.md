---
layout: post
title: "EC2 Password 접속하기"
author: "Bys"
category: cloud
date: 2021-03-04 01:00:00
tags: aws ec2 password
---

#### EC2 User, Password로 접속하는 방법 (Key File X)  

`vim /etc/ssh/sshd_config`  
sshd_config 파일의 PasswordAuthentication 값을 no -> yes로 변경해준다.  
Password 인증을 허용해주는 설정이다.  
```bash
# For this to work you will also need host keys in /etc/ssh/ssh_known_hosts
#HostbasedAuthentication no
# Change to yes if you don't trust ~/.ssh/known_hosts for
# HostbasedAuthentication
#IgnoreUserKnownHosts no
# Don't read the user's ~/.rhosts and ~/.shosts files
#IgnoreRhosts yes

# To disable tunneled clear text passwords, change to no here!
#PasswordAuthentication yes
#PermitEmptyPasswords no
#PasswordAuthentication no
PasswordAuthentication yes
```
다만 해당 내용을 AMI로 저장하고 사용하려고 하면 PasswordAuthentication 값은 자동으로 no 값으로 셋팅된다.  

이 후 아래의 명령 실행
```bash
sudo systemctl restart sshd 
```

AMI이미지로 사용할 경우는 아래와 같이 PasswordAuthentication yes 로 설정해놓은 파일을 sshd_config.orig 파일로 만들어놓고 EC2 생성 시 User Setting 부분에 아래와 같이 넣어준다.

`/etc/ssh/sshd_config`
```bash
#!/bin/bash 
sudo su - 
/bin/cp /etc/ssh/sshd_config.orig /etc/ssh/sshd_config 
/bin/systemctl restart sshd 
```

위 와 같이 설정 후 계정과 패스워드를 입력하면 key파일 없이 접속 가능하다.