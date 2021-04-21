---
layout: post
title: "Gitlab, Gitlab-Runner 설정 및 연동"
author: "Bys"
category: solution
date: 2021-04-21 01:00:00
tags: cicd gitlab gitlab-runner pipeline docker
---

#### Install Jenkins
/gitlab/gitlab/bin/docker-compose.yml
/gitlab/gitlab-runner

`docker-compose.yml`
```yml
version: '3.5'
services:
  web:
    image: "gitlab/gitlab-ee"
    
```
<br>


`stop_jenkins.sh`
```bash
ps -ef | grep jenkins | grep -v grep | awk '{print $2}' | xargs kill;
```
<br>



`/etc/systemd/system/jenkins.service`
```bash
[Unit]
Description=Jenkins
[Service]
Type=forking
ExecStart=/fsjks/bin/start_jenkins.sh
ExecStop=/fsjks/bin/stop_jenkins.sh
User=jksadm
Group=grubd
UMask=0007
RestartSec=10
Restart=no
[Install]
WantedBy=multi-user.target
```
<br>
