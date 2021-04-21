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
    image: "gitlab/gitlab-ee:13.10.3-ee.0"
    restart: always
    hostname: "gitlab.example.com"
    privileged: true
    environment:
      GITLAB_OMNIBUS_CONFIG: |
        external_url 'https://gitlab.example.com'
        # external_url 'http://10.75.235.125'
    ports:
    - "80:80"
    - "443:443"
#    - "22:22"
    volumes:
    - 'config:/etc/gitlab'
    - 'data:/var/opt/gitlab'
    - 'logs:/var/log/gitlab'
volumes:
  config:
  logs:
  data:
    
```
<br>

`start_gitlab.sh`
```bash
docker-compose up -d
```
<br>


`stop_gitlab.sh`
```bash
docker-compose down
```
<br>

