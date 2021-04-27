---
layout: post
title: "Docker를 이용한 Gitlab, Gitlab-Runner 구성"
author: "Bys"
category: solution
date: 2021-04-21 01:00:00
tags: cicd gitlab gitlab-runner pipeline docker docker-compose
---

#### Gitlab, Gitlab-Runner를 위한 OS계정 등록
```bash
groupadd cicdadm
useradd -g cicdadm -m cicdadm
```   
<br>

#### Install Docker  
```bash
sudo yum install docker
# cicdadm계정으로 docker 사용
sudo usermod -aG docker cicdadm
```
<br>

#### Install Docker-Compose  
```bash
sudo curl -L "https://github.com/docker/compose/releases/download/1.29.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

docker-compose --version
```
<br>

#### Install Gitlab  

**- Path**  
/gitlab/gitlab/bin/docker-compose.yml  
/gitlab/gitlab-runner  

`docker-compose.yml` 
```yml
version: '3.5'
services:
  web:
    image: "gitlab/gitlab-ee:13.10.3-ee.0"
    restart: always
    hostname: "10.20.11.239"
    privileged: true
    environment:
      GITLAB_OMNIBUS_CONFIG: |
         external_url 'http://10.20.11.239'
    ports:
    - "11010:80"
#    - "443:443"
#    - "22:22"
    volumes:
    - 'config:/etc/gitlab'
    - 'data:/var/opt/gitlab'
    - 'logs:/var/log/gitlab'
# Using Docker Volume
volumes:
  config:
  logs:
  data:
```

| Volumes | Container location | Usage |
|---|:---:|:---|
| config | /etc/gitlab | GitLab 구성 파일 저장용 |
| data | /var/opt/gitlab | 애플리케이션 데이터 저장용 |
| logs | /var/log/gitlab | 로그 저장용 |

<br>

`start_gitlab.sh`
```bash
cd /gitlab/gitlab/bin
docker-compose up -d
```
<br>


`stop_gitlab.sh`
```bash
cd /gitlab/gitlab/bin
docker-compose down
```
<br>



#### Install Gitlab-Runner  
`install_gitlab-runner.sh`
```bash
curl -L "https://packages.gitlab.com/install/repositories/runner/gitlab-runner/script.rpm.sh" | sudo bash

# Debian buster users should disable skel to prevent No such file or directory Job failures
export GITLAB_RUNNER_DISABLE_SKEL=true; sudo -E yum install gitlab-runner

gitlab-runner --version

sudo mkdir /gitlab/gitlab-runner/.gradle  
sudo chown -R cicdadm:cicdadm /gitlab/gitlab-runner/.gradle  

sudo gitlab-runner register --non-interactive \
  --url "http://10.20.11.239:11010" \
  --registration-token "sTygbanb-nN9LVzsmxZK" \
  --executor "docker" \
  --docker-image docker:stable \
  --description "docker-runner" \
  --tag-list "docker,aws" \
  --run-untagged="true" \
  --locked="false" \
  --access-level="not_protected" \
  --docker-volumes "/gitlab/gitlab-runner/.gradle:/root/.gradle" \
  --docker-volumes "/var/run/docker.sock:/var/run/docker.sock"

sudo service gitlab-runner restart
```

위 설정은 아래 config 파일에 저장 됨
`/etc/gitlab-runner/config.toml`

<br>
