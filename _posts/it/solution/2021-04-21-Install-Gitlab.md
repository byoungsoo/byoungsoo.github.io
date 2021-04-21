---
layout: post
title: "Gitlab, Gitlab-Runner 설정 및 연동"
author: "Bys"
category: solution
date: 2021-04-21 01:00:00
tags: cicd gitlab gitlab-runner pipeline docker
---

#### Install Gitlab  
/gitlab/gitlab/bin/docker-compose.yml  
/gitlab/gitlab-runner  

`docker-compose.yml` Using Docker Volume
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

#### Install Gitlab-Runner  
`install_gitlab-runner.sh`
```bash
curl -L "https://packages.gitlab.com/install/repositories/runner/gitlab-runner/script.rpm.sh" | sudo bash
export GITLAB_RUNNER_DISABLE_SKEL=true; sudo -E yum install gitlab-runner

gitlab-runner --version

sudo mkdir /home/gitlab-runner/.gradle  
sudo chown -R cicdadm:grbud /gitlab/gitlab-runner/.gradle  

docker gitlab-runner register --non-interactive \
--url "http://10.75.235.125:11010" \
--registration-token "" \
--executor "docker" \ 
--docker-image docker:stable \
--description "docker-runner" \
--tag-list "docker,aws" \
--run-untagged="true" \
--locked="false" \ 
--access-level="not_protected" \ 
--docker-volumes "/gitlab/gitlab-runner/.gradle:/root/.gradle" \ 
--docker-volumes "/var/run/docker.sock:/var/run/docker.sock" \ 

sudo service gitlab-runner restart
```

위 설정은 아래 config 파일에 저장 됨
`/etc/gitlab-runner/config.toml`

<br>
