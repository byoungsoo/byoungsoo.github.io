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



gitlab-runner:
  install: true
  rbac:
    create: true
  runners:
    locked: false
    # Set secret to an arbitrary value because the runner chart renders the gitlab-runner.secret template only if it is not empty.
    # The parent/GitLab chart overrides the template to render the actual secret name.
    secret: "nonempty"
    config: |
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
          Type = "s3"
          Path = "gitlab-runner"
          Shared = true
          [runners.cache.s3]
            ServerAddress = {{ include "gitlab-runner.cache-tpl.s3ServerAddress" . }}
            BucketName = "runner-cache"
            BucketLocation = "ap-northeast-2"
            Insecure = false
        {{ end }}
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



<br>

#### - Install Gitlab using Helm

```bash
# helm upgrade --install gitlab gitlab/gitlab --namespace gitlab -f values.yaml
helm upgrade --install gitlab ./ --namespace gitlab
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