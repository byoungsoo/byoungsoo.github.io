---
layout: post
title: "AWS EKS Cluster 생성"
author: "Bys"
category: container
date: 2021-03-25 01:00:00
tags: aws eks eksctl
---

#### - EKS Cluster 생성  

--클러스터 생성 이미지 캡처

마스터 계정만 접근 가능하므로 최초에 cluster를 생성한 IAM User로 aws configure 인증 후 작업


#### - EKS Cluster 설정  
`aws-auth configmap 적용`
클러스터의 유저와 IAM 역할을 관리하기 위해 최초로 aws-auth configmap을 설정한다.  

아래의 명령을 통해 적용여부 체크
```bash
kubectl describe configmap -n kube-system aws-auth
```

적용이 안되었다면 아래의 명령을 통해 yaml 파일을 받은 후 수정을 한 뒤 재 배포 한다.  
```bash
curl -o aws-auth-cm.yaml https://s3.us-west-2.amazonaws.com/amazon-eks/cloudformation/2020-10-29/aws-auth-cm.yaml
```

추가 되는 내용은 EKSAdminRole과 같이 role을 하나 생성하여 맵핑시켜주고 이 후 클러스터 접근에 필요한 인원들의 IAM Role을 해당 Role로 제어 할 수 있다.  
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: aws-auth
  namespace: kube-system
data:
  mapRoles: |
    - rolearn: arn:aws:iam::718652001716:role/SMP-DEV-ROLE-EC2-SSM
      username: system:node:{{EC2PrivateDNSName}}
      groups:
        - system:bootstrappers
        - system:nodes
    - rolearn: arn:aws:iam::718652001716:role/EKSAdminRole
      username: EKSAdminRole
      groups:
        - system:masters

#Sample
apiVersion: v1
data:
  mapRoles: |
    - rolearn: <arn:aws:iam::111122223333:role/eksctl-my-cluster-nodegroup-standard-wo-NodeInstanceRole-1WP3NUE3O6UCF>
      username: <system:node:{{EC2PrivateDNSName}}>
      groups:
        - <system:bootstrappers>
        - <system:nodes>
    - rolearn: <arn:aws:iam::111122223333:role/EKSAdminRole>
      username: EKSAdminRole
      groups:
        - system:masters
  mapUsers: |
    - userarn: <arn:aws:iam::111122223333:user/admin>
      username: <admin>
      groups:
        - <system:masters>
    - userarn: <arn:aws:iam::111122223333:user/ops-user>
      username: <ops-user>
      groups:
        - <system:masters>
```


`Asoociate IAM OIDC to Cluster`  
EKS OIDC 자격 증명 공급  
```bash
eksctl utils associate-iam-oidc-provider --region=ap-northeast-2 --cluster=smp-dev-eks-cluster --approve
```
<br>


`Launch Template Userdata`  
Worker Node로 사용 할 Launch Template의 User Data에 셋팅을 아래와 같이 해준다.  
dns_cluster_ip의 경우 service_ip range를 변경할 경우 반드시 설정하여 같이 넘겨준다.  
kubelet_extra_args의 경우 추가적으로 설정 할 정보를 넘겨준다. 아래와 같이 넘겨주면 node-label을 추가할 수 있다.  
```bash
MIME-Version: 1.0
Content-Type: multipart/mixed; boundary="==EKSWorkerNode=="

--==EKSWorkerNode==
Content-Type: text/x-shellscript; charset="us-ascii"


echo "===[start:bootstrap.sh]===================================="
cluster_name=smp-dev-eks-cluster
#dns_cluster_ip=172.16.16.10
kubelet_extra_args=--node-labels=node-group-type=smp-dev-test
cluster_endpoint=https://A02EF3CEEB0EEBD45FF57F01A8F5BABD.gr7.ap-northeast-2.eks.amazonaws.com
certificate_authority_data=LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUM1ekNDQWMrZ0F3SUJBZ0lCQURBTkJna3Foa2lHOXcwQkFRc0ZBREFWTVJNd0VRWURWUVFERXdwcmRXSmwKY201bGRHVnpNQjRYRFRJeE1EWXhOVEF4TURFek9Wb1hEVE14TURZeE16QXhNREV6T1Zvd0ZURVRNQkVHQTFVRQpBeE1LYTNWaVpYSnVaWFJsY3pDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnRVBBRENDQVFvQ2dnRUJBTjNLCmxlczB1elFoQ0ZqVnZoTlBUTUVOVHJxZ3BNaWYxUGJFLzhqYWZNa0RxcDJmVE9SdXJSdEFGRXlxVW9HL0VvMHoKNW5nclVYSFllUS9KL1Z2WlFXMEtMbStaaDVJMEoyc3Z6M2hKRXVuRjh1bUFacTBvUFFMN2I0a2hVaTdiTHNFZwpnSWxCYXBRUFdmaGtQZ0lFUWNRY3U5MGRIZjRwQlNlQ1FPVHFMVVRTRldHT2hZZFB5dXQ0UEVwVlRkQzRoeDRoClNrYy9UQldhVFlmY0t2MUVPajBVaXBNQXNHL2N5V1JSdSs3eFcvUTNmWUFQbjNZSnlVNkYzNXFVczIyc05xd3EKNXhPeW1acWlid0l6RDZoSVlFdDJFSWZVeUhqM0NHQlpjeDNZV0VmQjJvQUc3a2EyVUgyVURDWHkzTDhLSy8zYQpaczFZWnV5WXAxT1J6RlJCZVpjQ0F3RUFBYU5DTUVBd0RnWURWUjBQQVFIL0JBUURBZ0trTUE4R0ExVWRFd0VCCi93UUZNQU1CQWY4d0hRWURWUjBPQkJZRUZKdGxoRmpEV1c5TzZHbWtyMDZMN20ybTFBK0pNQTBHQ1NxR1NJYjMKRFFFQkN3VUFBNElCQVFCSHcvMkp1Z29jamE2enpvcE9KVGlkc2o5NjBjUVVpTHNHZlgyOUF3ZXEvZ1M5Ujk0QQpkTWQ1Y2FaZnE0Vm44bWpSOWN0MEc3YWh6bml6RG0zbDV0TkRNZFdCdW9nWUt5Y283QzdJM1NKeGZndm5EcFFrCk5teXhMUHdZdm9wZlEwc1FINmpvZ2xqTk9YSzYrb0F4azNJL1Z4M2p4enpYNXQ5M3A4RHBGMzNudjJ0VkNFODcKQmdySkF6UklWbGNDUnN2Tjc0WC9hSjZKbWFRMDNlT3hndFk2QnpxeUV2NURaWFVnWjdSREVVaEVGdWNFdmxWWApwOXlZTkR0UDRjVVU3RUJuKzFOQW5URUVtZ2FsKytLbzNsVC9jemswQ1piYUJ3ZlhJcEdYWnlTR2NVNzBZOWVHCnJ2a0txM2kvT3MzR1FNQmlCaERzRUdnTUZuWUJCTEZOWUZROAotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==

#/etc/eks/bootstrap.sh --apiserver-endpoint ${cluster_endpoint} --b64-cluster-ca ${certificate_authority_data} --dns-cluster-ip ${dns_cluster_ip} ${cluster_name} --kubelet-extra-args ${kubelet_extra_args}

/etc/eks/bootstrap.sh --apiserver-endpoint ${cluster_endpoint} --b64-cluster-ca ${certificate_authority_data}  ${cluster_name} --kubelet-extra-args ${kubelet_extra_args}

echo "===[end:bootstrap.sh]======================================"

--==EKSWorkerNode==--\
```