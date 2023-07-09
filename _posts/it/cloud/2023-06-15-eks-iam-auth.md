---
layout: post
title: "EKS 인증 및 인가 과정 dive deep"
author: "Bys"
category: cloud
date: 2023-06-16 01:00:00
tags: kubernetes eks aws iam authentication authorization
---

# [Cluster authentication](https://docs.aws.amazon.com/eks/latest/userguide/cluster-auth.html)

![iam-auth001](/assets/it/cloud/eks/iam-auth001.png){: width="70%" height="auto"}

Amazon EKS에서는 EKS 클러스터에 접근하기 위한 인증을 위해 IAM을 사용하며 아키텍처는 위의 그림과 같다.  

<br><br>

## 1. 동작과정 Dive Deep
### 1.1 Client는 IAM Identity token 정보와 함께 EKS API 서버로 요청을 전달  

Debug모드를 통해 Client에서 kubectl 커맨드를 통해 요청시 어떻게 수행되는지 알아본다.  

```bash
$ kubectl get po --v=9

I0615 23:46:30.672462   49991 loader.go:373] Config loaded from file:  /Users/bys/.kube/config
I0615 23:46:30.682572   49991 round_trippers.go:466] curl -v -XGET  -H "User-Agent: kubectl/v1.27.2 (darwin/arm64) kubernetes/7f6f68f" -H "Accept: application/json;as=Table;v=v1;g=meta.k8s.io,application/json;as=Table;v=v1beta1;g=meta.k8s.io,application/json" 'https://AAAABBBBCCCC.sk1.ap-northeast-2.eks.amazonaws.com/api/v1/namespaces/default/pods?limit=500'
......생략
I0615 23:46:30.868596   49991 round_trippers.go:577] Response Headers: ~
I0615 23:46:30.868761   49991 request.go:1188] Response Body: ~ 
```
위 메세지에서 알 수 있듯이 최초에 ~/.kube/config 파일을 로드한다.  

<br>

```yaml
  user:
    exec:
      apiVersion: client.authentication.k8s.io/v1beta1
      args:
      - token
      - -i
      - bys-dev-eks-main
      command: aws-iam-authenticator
      env:
      - name: AWS_STS_REGIONAL_ENDPOINTS
        value: regional
      - name: AWS_DEFAULT_REGION
        value: ap-northeast-2
      interactiveMode: IfAvailable
      provideClusterInfo: false
```
config 파일 내부에 user정보는 위와 같이 exec 모드로 `aws-iam-authenticator token -i bys-dev-eks-main` 커맨드를 수행하여 token을 넘겨주도록 설정되어 있다.  

<br>

커맨드를 수행 시 아래와 같은 token이 발행되는 것을 확인할 수 있다.  
```json
$ aws-iam-authenticator token -i bys-dev-eks-main

{
  "kind": "ExecCredential",
  "apiVersion": "client.authentication.k8s.io/v1beta1",
  "spec": {
    "interactive": false
  },
  "status": {
    "expirationTimestamp": "2023-06-15T15:07:44Z",
    "token": "k8s-aws-v1.aHR0cHM6Ly9zdHMuYXAtbm9ydGhlYXN0LTIuYW1hem9uYXdzLmNvbS8_QWN0aW9uPUdldENhbGxlcklkZW50aXR5JlZlcnNpb249MjAxMS0wNi0xNSZYLUFtei1BbGdvcml0aG09QVdTNC1ITUFDLVNIQTI1NiZYLUFtei1DcmVkZW50aWFsPUFLSUFZRUhPWFpaRVJHM1lVRlVJJTJGMjAyMzA2MTUlMkZhcC1ub3J0aGVhc3QtMiUyRnN0cyUyRmF3czRfcmVxdWVzdCZYLUFtei1EYXRlPTIwMjMwNjE1VDE0NTM0NFomWC1BbXotRXhwaXJlcz0wJlgtQW16LVNpZ25lZEhlYWRlcnM9aG9zdCUzQngtazhzLWF3cy1pZCZYLUFtei1TaWduYXR1cmU9MWJlMGM1NTI4MWI3ZThlOTc4MDdkYzRkZjk1MDQ3YzA0NmU4MDJiMzlkYWIxOTQxYmY3MDNkMWRhZGIyNDYyOA"
  }
}
```

위 토큰을 [JWT](https://jwt.io/)사이트로 이동하여 Decode를 해보면 payload에는 아래와 같은 정보가 확인된다.  
아래 URL은 Pre-signed AWS API요청으로 자세한 내용은 [문서](https://docs.aws.amazon.com/IAM/latest/UserGuide/create-signed-request.html)에서 확인할 수 있다.   
```txt
"https://sts.ap-northeast-2.amazonaws.com/?Action=GetCallerIdentity
&Version=2011-06-15
&X-Amz-Algorithm=AWS4-HMAC-SHA256
&X-Amz-Credential=AKIAYTTTTTYYYYYXXXXX%2F20230615%2Fap-northeast-2%2Fsts%2Faws4_request
&X-Amz-Date=20230615T145344Z
&X-Amz-Expires=0
&X-Amz-SignedHeaders=host%3Bx-k8s-aws-id
&X-Amz-Signature=1be0c55281b7e8e97807dc4df95047c046e802b39dab1941bf703d1dadb24628"
```
참고로 x-k8s-aws-id 헤더에는 클러스터 ID가 들어간다.  



이 중 실제 `X-Amz-Crednetial=AKIAYTTTTTYYYYYXXXXX` 정보는 해당 커맨드를 수행한 IAM User의 Access Key정보와 같다.  

<br>

### 1.2 EKS API Server는 Webhook Token Authentication 설정을 통해 aws-iam-authenticator 서버로 요청을 전달  
Amazon EKS 에서는 아키텍처에서 볼 수 있듯이 aws-iam-authenticator를 Webhook Token 인증을 위한 서버로 구성하고 Control Plane설정에서 아마 다음과 같은 형태로 webhook 서버로 인증을 받을 수 있도록 설정했을 것이다. `--authentication-token-webhook-config-file=/etc/kubernetes/~~~/webhook.yaml`

<br>

### 1.3 aws-iam-authenticator 서버 
EKS API 서버는 IAM Identity Token을 aws-iam-authenticator 서버로 전달하고 aws-iam-authenticator 서버는 IAM 서비스를 통해 IAM Identity가 유효한지 검증한다. token 정보를 참고해보면 IAM서비스의 GetCallerIdentity를 호출한다. IAM Identity Token을 통해 정상적으로 검증이 되면 aws-iam-authenticator 서버는 IAM Identity정보를 IAM 서비스로 부터 받게 된다. 

aws-iam-authenticator 서버는 IAM 서비스로 부터 IAM Identity 정보를 받으면 EKS 클러스터의 어떤 User/Group과 맵핑되는지 확인하기 위해 aws-auth ConfigMap을 확인 한다. 따라서 aws-auth ConfigMap에 특정 MapUsers 혹은 MapRoles를 등록하는 과정은 검증된 IAM Identity에 어떤 EKS 권한을 부여할지를 결정하기 위해 반드시 필요하다.  

아래와 같은 형태로 mapRoles 또는 mapUsers를 등록할 수 있다.  
```yaml
apiVersion: v1
data:
  mapRoles: |
    - groups:
      - system:bootstrappers
      - system:nodes
      rolearn: arn:aws:iam::111122223333:role/KarpenterInstanceNodeRole
      username: system:node:{{EC2PrivateDNSName}}
    - groups:
      - system:bootstrappers
      - system:nodes
      rolearn: arn:aws:iam::111122223333:role/eksctl-bys-dev-eks-main-nodegroup
      username: system:node:{{EC2PrivateDNSName}}
  mapUsers: |
    - userarn: arn:aws:iam::111122223333:user/admin
      username: admin
      groups:
      - system:masters
kind: ConfigMap
metadata:
  name: aws-auth
  namespace: kube-system
```

aws-iam-authenticator 서버는 aws-auth ConfigMap에서 확인한 kubernetes User/Group을 EKS API서버로 전달한다.  

<br>

### 1.4. EKS API 서버의 인가 과정
EKS API 서버는 Kubernetes의 User/Group 정보를 받으면 RBAC(Role-based Access Control)기반의 Role/ClusterRole을 부여한다.  

<br><br>

이렇게 Amazon EKS 에서는 인증/인가 과정이 동작하며 인증 과정은 AWS IAM 서비스를 통해 인가 과정은 Kubernetes 기반의 RBAC으로 동일하게 작동한다.  

<br><br><br>

- References  
[1] [Cluster authentication](https://docs.aws.amazon.com/eks/latest/userguide/cluster-auth.html)  
[2] [aws-iam-authenticator](https://github.com/kubernetes-sigs/aws-iam-authenticator)
[3] https://itnext.io/how-does-client-authentication-work-on-amazon-eks-c4f2b90d943b  