---
layout: post
title: "Gitlab, Gitlab-Runner (Helm)"
author: "Bys"
category: solution
date: 2023-11-23 01:00:00
tags: cicd gitlab gitlab-runner helm
---

#### - Gitlab, Gitlab-Runner를 위한 OS계정 등록
```bash
groupadd cicdadm
useradd -g cicdadm -m cicdadm
```   
<br>

#### - [Install Gitlab using Helm](https://docs.gitlab.com/charts/installation/deployment.html)  
[Chart](https://gitlab.com/gitlab-org/charts/gitlab)  
```bash
kubectl create ns gitlab

helm repo add gitlab https://charts.gitlab.io/
helm repo update

helm pull gitlab/gitlab
tar xvf 
```

최상단 `values.yaml` 파일 수정  
```yaml
  ## https://docs.gitlab.com/charts/charts/globals#configure-ingress-settings
  ingress:
    # apiVersion: ""
    # configureCertmanager: true
    # provider: nginx
    class: alb
    annotations:
      alb.ingress.kubernetes.io/group.name: gitlab
      alb.ingress.kubernetes.io/subnets: subnet-03720d77c88c997f3, subnet-0810de67a8498a53d
      alb.ingress.kubernetes.io/scheme : internet-facing
      alb.ingress.kubernetes.io/security-groups: shared-sg-alb-gitlab
      alb.ingress.kubernetes.io/ssl-policy: ELBSecurityPolicy-TLS13-1-2-2021-06
      alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:ap-northeast-2:111122223333:certificate/11111-7a4f-47ca-98a1-6a9f3a08ecda
      alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}, {"HTTPS": 443}]'
      alb.ingress.kubernetes.io/actions.ssl-redirect: '{"Type": "redirect", "RedirectConfig": {"Protocol": "HTTPS", "Port": "443", "StatusCode": "HTTP_301"}}'
      alb.ingress.kubernetes.io/healthcheck-path: /
      alb.ingress.kubernetes.io/healthcheck-interval-seconds: '15'
      alb.ingress.kubernetes.io/healthcheck-timeout-seconds: '10'
      alb.ingress.kubernetes.io/healthy-threshold-count: '2'
      alb.ingress.kubernetes.io/unhealthy-threshold-count: '4'
      alb.ingress.kubernetes.io/healthcheck-port: traffic-port
      alb.ingress.kubernetes.io/success-codes: 200,301,302
      alb.ingress.kubernetes.io/target-type: ip
    enabled: true
    tls: {}

    path: /
    extraPaths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: ssl-redirect
            port:
              name: use-annotation
    pathType: Prefix

......

# Disabled nginx-ingress to use alb ingress class
nginx-ingress: &nginx-ingress
  enabled: false

......

certmanager-issuer:
#   # The email address to register certificates requested from Let's Encrypt.
#   # Required if using Let's Encrypt.
  email: 123@naver.com
```


```yaml
# gitlab-runner
image:
  registry: registry.gitlab.com
  image: docker:stable

# redis
master:
  persistence:
    size: 30Gi
```
위 와 같이 values.yaml 파일을 수정하고 추가적으로 수정이 필요한 내부 Chart의 values.yaml 파일을 수정한다. 


```bash
# helm upgrade --install gitlab gitlab/gitlab --namespace gitlab -f values.yaml
helm upgrade --install gitlab ./ --namespace gitlab
```
위 커맨드를 통해 설치시 AWS Load Balancer Controller를 통해 ALB가 설치된다. Nginx ingress를 사용하지 않고 ALB를 통해 서비스하기 위해서 사용한다. 또한 내부적으로 PVC 볼륨 사이즈 값들을 수정하였는데 remote가 아닌 수정된 내부 Chart values 파일을 적용하기 위해서는 pull 받은 디렉토리의 경로에서 차트를 실행시킨다.  

```bash
kubectl get secrets gitlab-gitlab-initial-root-password -n gitlab -o jsonpath={.data.password}|base64 -d
```

<br>

#### - Install Docker-Compose  
```bash
sudo curl -L "https://github.com/docker/compose/releases/download/1.29.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

docker-compose --version
```
<br>

#### - Install Gitlab  

**- Path**  git
/gitlab/gitlab/docker-compose.yml  
/gitlab/gitlab-runner  

`docker-compose.yml` 
```yml
version: '3.5'
services:
  service:
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
cd /gitlab/gitlab
docker-compose up -d
```
<br>


`stop_gitlab.sh`
```bash
cd /gitlab/gitlab
docker-compose down
```
<br>

여기 까지 설치 후 혹시나 패스워드를 모를 경우 아래와 같이 진행한다.  
docker-compose로 서비스가 되었기 때문에 서비스에 진입을 해야 한다.  
```bash
docker exec -it gitlab_svc_1 /bin/bash
cd /var/opt/gitlab

gitlab-rails console -e production
u = User.where(id: 1).first
# => #<User id:1 @root>
u.password = 'newpassword'
u.password_confirmation = 'newpassword'
u.save
exit
```
User는 root에 초기 설정된 패스워드로 진행한다.  
<br>


#### - Install Gitlab-Runner  
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
  --docker-volumes "/fsgitlab/gitlab/repository:/fsgitlab/gitlab/repository" \
  --docker-volumes "/fsgitlab/gitlab-runner/.kube:/root/.kube" \
  --docker-volumes "/gitlab/gitlab-runner/.gradle:/root/.gradle" \
  --docker-volumes "/var/run/docker.sock:/var/run/docker.sock"


sudo service gitlab-runner restart
```

위 설정은 아래 config 파일에 저장 됨  
`/etc/gitlab-runner/config.toml`  

최종설정은 아래와 같으며 environment에 DOCKER_AUTH_CONFIG 는 Runner에서 각 Stage마다 Image Pull 할 때 인증에 대한 처리를 위해서 설정필요  
pull_policy의 경우 Docker Image에 대한 정책 설정이다.  

```bash
concurrent = 2
check_interval = 0

[session_server]
  session_timeout = 1800

[[runners]]
  name = "docker-runner"
  url = "http://10.75.235.125:11010"
  token = "Dv45HecFqMTiiBzf4QSE"
  executor = "docker"
  environment = ["DOCKER_AUTH_CONFIG={ \"credHelpers\": { \"222383050459.dkr.ecr.ap-northeast-2.amazonaws.com\": \"ecr-login\" }} "]
  [runners.custom_build_dir]
  [runners.cache]
    [runners.cache.s3]
    [runners.cache.gcs]
    [runners.cache.azure]
  [runners.docker]
    tls_verify = false
    image = "222383050459.dkr.ecr.ap-northeast-2.amazonaws.com/common:docker-stable"
    privileged = false
    disable_entrypoint_overwrite = false
    oom_kill_disable = false
    disable_cache = false
    volumes = ["/fsgitlab/gitlab/repository:/fsgitlab/gitlab/repository", "/fsgitlab/gitlab-runner/.gradle:/root/.gradle", "/var/run/docker.sock:/var/run/docker.sock", "/cache", "/fsgitlab/gitlab-runner/.kube:/root/.kube"]
    pull_policy = ["if-not-present"]
    shm_size = 0

```
<br>


#### - Gitlab-Runner Credential Helper 설정
Credential Helper는 ecr docker login을 유지하기 위해 사용하였으며,
해당 프로젝트에서는 gitlab-runner 각 Stage에서 사용하는 ecr image를 pull 하기 위해서 ecr_login에 대한 부분을 처리하기 위해 설정.  
Credential Helper를 설치하기 위해서는 아래를 참고한다. 
https://github.com/awslabs/amazon-ecr-credential-helper

`Install golang && export PATH`
```bash
yum install go
export GOPATH=$HOME/go
export PATH=$PATH:$GOPATH/bin
```

`docker-credential-ecr-login`  
go get 을 통해 docker-credential-ecr-login을 설치  
environment 설정에 DOCKER_AUTH_CONFIG 설정을 잡아주면 된다.  
```bash
go get -u github.com/awslabs/amazon-ecr-credential-helper/ecr-login/cli/docker-credential-ecr-login
#아래 경로로 move
/usr/local/bin/docker-credential-ecr-login
```

`/etc/gitlab-runner/config.toml`
```bash
concurrent =1 
check_interval = 0

[session_server]
  session_timeout = 1800

[[runners]]
  name = "docker-runner"
  url = "http://10.75.235.125:11010"
  token = "Dv45HecFQmtasdf"
  executor = "docker"
  environment = ["DOCKER_AUTH_CONFIG={ \"credHelpers\": { \"222383050459.dkr.ecr.ap-northeast-2.amazonaws.com\": \"ecr-login\" }} "]
  .......
```

공식 문서의 샘플  
To use this credential helper for a specific ECR registry, create a credHelpers section with the URI of your ECR registry:
```json
{
	"credHelpers": {
		"public.ecr.aws": "ecr-login",
		"<aws_account_id>.dkr.ecr.<region>.amazonaws.com": "ecr-login"
	}
}
```