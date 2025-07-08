---
layout: post
title: "Gitlab, Gitlab-Runner 를 Kubernetes 환경으로 전환하기"
author: "Bys"
category: solution
date: 2023-12-12 01:00:00
tags: cicd gitlab gitlab-runner helm
---

이 내용은 [docker-compose를 통해 구성한 Gitlab](https://byoungsoo.github.io/solution/2021/04/21/gitlab.html)을 Kubernetes 환경으로 전환하기 위한 과정을 정리하였다.  


#### - [Install Gitlab using Helm](https://docs.gitlab.com/charts/installation/deployment.html)  
[Chart](https://gitlab.com/gitlab-org/charts/gitlab)  
```bash
kubectl create ns gitlab

helm repo add gitlab https://charts.gitlab.io/
helm repo update

```

최상단 `values.yaml` 파일 수정  
```yaml
global:
  hosts:
    domain: bys.asia

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
      alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:ap-northeast-2:202949997891:certificate/57b91a03-9fb1-4f3a-a192-94ab30a5e105
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


## Settings to for the Let's Encrypt ACME Issuer
certmanager-issuer:
#   # The email address to register certificates requested from Let's Encrypt.
#   # Required if using Let's Encrypt.
  email: skwltg90@naver.com


## https://docs.gitlab.com/charts/charts/nginx/
## https://docs.gitlab.com/charts/architecture/decisions#nginx-ingress
## Installation & configuration of charts/ingress-nginx:
nginx-ingress: &nginx-ingress
  enabled: false

## Installation & configuration of gitlab/gitlab-runner
## See requirements.yaml for current version
# bys
gitlab-runner:
  install: false
# Redis
redis:
  master:
    persistence:
      size: 30Gi
  replica:
    persistence:
      size: 30Gi

# Minio
minio:
  persistence:
    size: 20Gi

# Postgresql
postgresql:
  primary:
    persistence:
      size: 30Gi
  readReplicas:
    persistence:
      size: 30Gi

## Prometheus
prometheus:
  server:
    persistentVolume:
      size: 30Gi

gitlab:
  gitaly:
    persistence:
      size: 80Gi
```
위 와 같이 values.yaml 파일을 수정한다. Chart의 구조상 Dependency가 있는 Chart에 대해서는 Chart의 이름: 에서 values.yaml파일의 값을 다시 수정한다.  

<br>

#### - Install Gitlab using Helm

```bash
helm repo update gitlab
helm upgrade --install gitlab gitlab/gitlab --namespace gitlab -f /Users/bys/workspace/kubernetes/gitlab/latest/values.yaml
helm upgrade --install gitlab gitlab/gitlab --namespace gitlab --version 8.11.1 -f /Users/bys/workspace/kubernetes/gitlab/latest/values.yaml
```
위 커맨드를 통해 설치시 AWS Load Balancer Controller를 통해 ALB가 설치된다. Nginx ingress를 사용하지 않고 ALB를 통해 서비스하기 위해서 사용한다. 또한 내부적으로 PVC 볼륨 사이즈 값들을 수정하였는데 remote가 아닌 수정된 내부 Chart values 파일을 적용하기 위해서는 pull 받은 디렉토리의 경로에서 차트를 실행시킨다.  

```bash
kubectl get secrets gitlab-gitlab-initial-root-password -n gitlab -o jsonpath={.data.password}|base64 -d
```
<br>


### TroubleShooting  
#### - [Kaniko의 ECR Credential 설정](https://github.com/GoogleContainerTools/kaniko#pushing-to-amazon-ecr)  

빌드 수행 전 echo 커맨드를 통해 /kaniko/.docker/config.json 파일안에 ecr-login을 통해 자격증명 설정을 해준다. kaniko 이미지에는 기본적으로 ECR credential helper가 설치되어 있는 것으로 확인된다.  
> The Amazon ECR credential helper is built into the kaniko executor image.  

`gitlab-ci.yml`
```bash
### Kaniko Build
- mkdir -p /kaniko/.docker
- echo "{"credsStore":"ecr-login","credHelpers":{"$REGISTRY_URL":"ecr-login"}}" > /kaniko/.docker/config.json

- /kaniko/executor
  --ignore-path=/var/mail
  --ignore-path=/var/spool/mail
  --context "${CI_PROJECT_DIR}"
  --dockerfile "${CI_PROJECT_DIR}/${DOCKER_FILE_NAME}"
  --destination "${ECR_BASE_URL}/${DOCKER_ECR_IMAGE_REPO}:${BASE_IMAGE_TAG}"
```
<br>


#### - [Kubernetes executor, Runner 설정](https://docs.gitlab.com/runner/executors/kubernetes.html)
Image를 Pull, Push 하기 위해서는 nerdctl 커맨드를 사용하여야 했는데 containerd 런타임을 통해 수행되기 때문에 노드의 containerd.sock을 마운트하여 사용할 수 있도록 한다.  
> containerd is a high-level container runtime. To put it simply, it's a daemon that manages the complete container lifecycle on a single host: creates, starts, stops containers, pulls and stores images, configures mounts, networking, etc.

[`values.yaml`](https://gitlab.com/gitlab-org/charts/gitlab/-/blob/master/values.yaml?ref_type=heads#L1237)
```yaml
[[runners]]
  [runners.kubernetes]
  image = "202949997891.dkr.ecr.ap-northeast-2.amazonaws.com/common/build:kaniko-debug"
  pull_policy = ["always", "if-not-present"]
  {{- if .Values.global.minio.enabled }}
  [[runners.kubernetes.volumes.host_path]]
    name = "containerdsock"
    mount_path = "/run/containerd/containerd.sock"
    read_only = true
    host_path = "/run/containerd/containerd.sock"
  [runners.cache]
```

`test`
```yaml
concurrent = 10
check_interval = 15
[[runners]]
  name = "standard-runner"
  executor = "kubernetes"
  tags = ["standard", "default"]
  [runners.kubernetes]
    image = "202949997891.dkr.ecr.ap-northeast-2.amazonaws.com/common/build:kaniko-debug"
    pull_policy = ["always", "if-not-present"]
    namespace = "gitlab"
    cpu_request="2"
    cpu_limit = "2"
    memory_request= "4Gi"
    memory_limit = "4Gi"

    [runners.kubernetes.node_selector]
      "karpenter.sh/nodepool" = "al2023-np"

    [[runners.kubernetes.volumes.host_path]]
      name = "containerdsock"
      mount_path = "/run/containerd/containerd.sock"
      read_only = true
      host_path = "/run/containerd/containerd.sock"
    [runners.cache]
      Type = "s3"
      Path = "cache"
      Shared = true
      [runners.cache.s3]
        ServerAddress = {{ include "gitlab-runner.cache-tpl.s3ServerAddress" . }}
        BucketName = "bys-shared-s3-gitlab-runner-cache"
        BucketLocation = "ap-northeast-2"
        Insecure = false
```
<br>


#### - [Kubernetes Runner 추가 등록](https://gitlab.com/gitlab-org/charts/gitlab-runner)
Gitlab을 설치할 때 사용한 values.yaml 파일을 이용하면 Runners 가 하나만 등록 가능한 것으로 보이기 때문에 별도의 gitlab-runner를 추가로 등록한다. 아래의 Runner는 큰 규모의 빌드(Ex. jupyter-spark 이미지)를 실행하기 위한 러너다. 아래의 러너는 tags로 `high, large` 를 가지고 있다. 추후 gitlab-ci.yml 파일에서 tags를 통해 아래의 runner 와 맵핑할 수 있다. 추가적으로 runners.kubernetes.node_selector 를 통해 어떤 노드에서 실행시킬지 Karpenter 노드 풀을 지정하고 있으므로 추가적으로 EBS 볼륨의 스펙까지 컨트롤 할 수 있다.  

[GitLab Runner](https://gitlab.com/gitlab-org/charts/gitlab-runner)



`large-values.yaml`
```yaml
serviceAccount:
  create: false
  name: "gitlab-gitlab-runner"
runnerToken: "glrt-dDoxCnU6Mm4qVIah8DUHQOZUpY-kC5cQ.0w1ruk0xi"
runners:
  config: |
    concurrent = 10
    check_interval = 15
    [[runners]]
      name = "gitlab-runner-large"
      executor = "kubernetes"
      url= "https://gitlab.bys.asia"
      tags = ["high", "large"]
      [runners.kubernetes]
        image = "202949997891.dkr.ecr.ap-northeast-2.amazonaws.com/common/build:kaniko"
        pull_policy = ["always", "if-not-present"]
        namespace = "gitlab"
        cpu_request="8"
        cpu_limit = "8"
        memory_request= "16Gi"
        memory_limit = "16Gi"
        [runners.kubernetes.node_selector]
          "karpenter.sh/nodepool" = "al2023-large-np"

        [[runners.kubernetes.volumes.host_path]]
          name = "containerdsock"
          mount_path = "/run/containerd/containerd.sock"
          read_only = true
          host_path = "/run/containerd/containerd.sock"
```

```bash
helm search repo -l gitlab/gitlab-runner
helm repo update gitlab
helm upgrade --install gitlab-runner-large gitlab/gitlab-runner -n gitlab -f /Users/bys/workspace/kubernetes/gitlab/latest/gitlab-runner-large-values.yaml
```

Gitlab 에서 분리하여 일반 standard 러너도 별도로 관리하기로 변경 하였다. runnerToken은 Gitlab 콘솔에서 Runner 인스턴스를 생성하면 조회할 수 있다.  
`standard-values.yaml`  
```yaml
serviceAccount:
  create: false
  name: "gitlab-runner"
runnerToken: "glrt-4vtXro-fT0J3GalIJkOXKHQ6MQp1OjIH.01.0w186pwti"
runners:
  config: |
    concurrent = 10
    check_interval = 15
    [[runners]]
      name = "gitlab-runner-standard"
      executor = "kubernetes"
      url= "https://gitlab.bys.asia"
      [runners.kubernetes]
        image = "202949997891.dkr.ecr.ap-northeast-2.amazonaws.com/common/build:kaniko"
        pull_policy = ["always", "if-not-present"]
        namespace = "gitlab"
        cpu_request="2"
        cpu_limit = "2"
        memory_request= "4Gi"
        memory_limit = "4Gi"
        [runners.kubernetes.node_selector]
          "karpenter.sh/nodepool" = "al2023-np"

        [[runners.kubernetes.volumes.host_path]]
          name = "containerdsock"
          mount_path = "/run/containerd/containerd.sock"
          read_only = true
          host_path = "/run/containerd/containerd.sock"
```

```bash
helm search repo -l gitlab/gitlab-runner
helm repo update gitlab
helm upgrade --install gitlab-runner-standard gitlab/gitlab-runner -n gitlab -f /Users/bys/workspace/kubernetes/gitlab/latest/gitlab-runner-standard-values.yaml
```


<br>

#### - Gitlab 구성 요소에 대한 역할

- 웹 인터페이스 및 코어 서비스
  - gitlab-webservice-default: GitLab의 주요 웹 인터페이스를 제공하는 Rails 애플리케이션이다. 사용자 UI, API 요청 처리, 프로젝트 관리 기능을 담당한다.
  - gitlab-sidekiq-all-in-1: 백그라운드 작업 처리기로, 이메일 발송, 저장소 업데이트, CI/CD 파이프라인 처리 등 비동기 작업을 수행한다.
  - gitlab-gitlab-shell: SSH 접근을 관리하며, Git 저장소에 대한 SSH 기반 상호작용을 처리한다.

- 저장소 관리
  - gitlab-gitaly: Git 저장소 저장 및 액세스를 관리하는 서비스로, GitLab의 Git 작업을 효율적으로 처리한다.
  - gitlab-registry: Docker 컨테이너 이미지 저장소로, GitLab 프로젝트에서 빌드된 컨테이너 이미지를 저장하고 관리한다.

- 데이터베이스 및 저장소
  - gitlab-postgresql: GitLab의 주 데이터베이스로, 사용자 정보, 프로젝트 메타데이터 등을 저장한다.
  - gitlab-redis-master: 캐싱, 세션 관리, 작업 큐를 위한 인메모리 데이터 구조 저장소이다.
  - gitlab-minio: S3 호환 객체 저장소로, 아티팩트, CI 캐시, LFS 파일 등을 저장한다.

- CI/CD 구성 요소
  - gitlab-gitlab-runner: CI/CD 작업을 실행하는 GitLab Runner 인스턴스이다.
  - gitlab-runner-large: 더 많은 리소스가 필요한 CI/CD 작업을 위한 대용량 Runner이다. 나의 용도...
  - gitlab-kas: Kubernetes Agent Server로, Kubernetes 클러스터와 GitLab 간의 통신을 담당한다.

- 모니터링 및 유틸리티
  - gitlab-prometheus-server: 모니터링 시스템으로 GitLab 인스턴스의 다양한 메트릭을 수집한다.
  - gitlab-gitlab-exporter: GitLab 관련 메트릭을 추출하여 Prometheus에 제공한다.
  - gitlab-toolbox: 백업, 복원, 데이터베이스 마이그레이션 등의 관리 작업을 수행하는 유틸리티이다.

- 인증서 관리
  - gitlab-certmanager: TLS 인증서 자동 획득 및 관리를 담당한다.
  - gitlab-certmanager-cainjector: 인증 기관 정보를 Kubernetes 리소스에 주입한다.
  - gitlab-certmanager-webhook: 인증서 관련 커스텀 리소스 검증을 위한 웹훅 서버이다.
  - cm-acme-http-solver: Let's Encrypt와 같은 ACME 기반 인증서 발급을 위한 HTTP 챌린지를 해결한다.


<br>

#### - [Mirroring Repositories](https://docs.gitlab.com/17.2/ee/user/project/repository/mirror/index.html)
https를 통해 엔드포인트 연결 시에는 username/password 방식으로 동작해야 한다. 
Github에 연결할 때는 패스워드 인증 기능이 되지 않기 때문에 username에는 사용자, password에는 token을 입력한다. 
  - URL: https://github.com/byoungsoo/cloudformation.git
  - User: bys
  - Password: <token>


<br>

#### - [EKS on Gitlab v17 에서 v18로 업그레이드](https://docs.gitlab.com/charts/installation/database_upgrade/)
Gitlab 17 버전에서 18버전으로 업그레이드 할 때는 `helm upgrade` 를 통해 업그레이드가 되지 않는다. 여러가지 오류들이 발생하며 PostgreSQL을 16버전으로 업그레이드 먼저 해주어야 하는 것을 알게되었다.  


[태그](https://gitlab.com/gitlab-org/charts/gitlab/-/tags)를 확인하여 현 시점에서 최신 버전인 `Version v9.1.1 - contains GitLab EE 18.1.1`을 확인했다.  



```bash
# GITLAB_RELEASE should be the version of the chart you are installing, starting with 'v': v6.0.0
GITLAB_RELEASE=v9.1.1


## 1. DB 백업 단계
curl -O https://gitlab.com/gitlab-org/charts/gitlab/-/raw/${GITLAB_RELEASE}/scripts/database-upgrade
bash database-upgrade -r gitlab -n gitlab pre


# 2. Delete existing PostgreSQL data
kubectl delete statefulset RELEASE-NAME-postgresql
kubectl delete pvc data-RELEASE_NAME-postgresql-0


# 3. Upgrade GitLab
helm upgrade --install gitlab gitlab/gitlab --namespace gitlab \
--set gitlab.migrations.enabled=false \
-f /Users/bys/workspace/kubernetes/gitlab/latest/values.yaml

## 4. DB 복구 단계
kubectl rollout status -w deployment/gitlab-toolbox -n gitlab
bash database-upgrade -r gitlab -n gitlab post
```

DB 백업 단계에서 S3쪽(gitlab-backup)으로 업로드가 되지 않아서 생성된 데이터를 toobox 컨테이너 내부로 접속하여 S3 Presigned URL을 통해 직접 업로드 했다. 이 후 DB 복구 단계에서 post를 호출할 때 -f 옵션을 통해 Presigned URL을 추가해서 백업 파일을 직접 다운 받을 수 있도록 database-upgrade 스크립트를 수정해서 수행했다. 
```bash
post() {
......
# Restore the database
    in_toolbox backup-utility --restore -t "${fake_timestamp}" --skip registry,uploads,artifacts,lfs,packages,external_diffs,terraform_state,ci_secure_files,repositories -f <PresignedURL>

}
```

이 작업을 통해 Gitlab 18 버전으로 정상적인 마이그레이션을 할 수 있었다. 18버전에서 오류가 발생하여 17버전으로 Helm Rollback을 한 경우에도 이미 데이터베이스 스키마가 망가져서 파이프라인 정보나 이런 것들도 조회가 되지 않았다. 아예 18버전으로 데이터 마이그레이션 까지 해서 띄우는 것이 좋다.  

이 후 권한이슈가 일부 있어서 Runner 설정을 변경했다. Runner는 Gitlab 과 분리하여 별도의 차트로 관리하게 하고 SA와 Role을 정리했다.  Role은 [문서](https://docs.gitlab.com/runner/executors/kubernetes/#informers) 참고.  