---
layout: post
title: "AWS Backup 서비스를 이용한 EKS Backup"
author: "Bys"
category: cloud
date: 2026-03-10 01:00:00
keywords: "eks backup"
tags: eks backup
---

# EKS Backup

## 개요 - AWS Backup 서비스를 통한 EKS Backup
AWS Backup 을 사용하여 EKS 클러스터 상태(ETCD)와 Persistent Volume(EBS, EFS)에 대한 `통합 복구 지점` 및 `하위 복구 지점`을 구성할 수 있다. 

- **통합 복구 지점** – EKS 클러스터 전체를 백업한 지점 (여러 하위 복구 지점을 묶어서 관리)
- **하위 복구 지점** – 클러스터 내 개별 리소스의 백업 지점
- **클러스터 상태** – Kubernetes 리소스 정의 (Secret, ConfigMap, StatefulSet, DaemonSet, Role 등)
- **클러스터 설정 하위 복구 지점** – 클러스터 상태 백업
- **영구 볼륨 하위 복구 지점** – EBS, EFS, S3 볼륨 백업

## EKS Backups 에 포함되는 것
- EKS 클러스터의 상태
  - 클러스터 기능 설정
  - IAM 역할
  - VPC 구성
  - 네트워크 설정
  - 로깅 설정
  - 암호화 설정
  - Add-ons
  - Access Entries (접근 항목)
  - Managed Node Groups
  - Fargate Profiles
  - Pod Identity Associations
  - Kubernetes 매니페스트 파일 (리소스 정의)
- 스토리지 - Amazon EBS, Amazon EFS, and Amazon S3

## EKS Backups 에 포함되지 않는 것
- Auto-generated EKS resources like nodes, auto-generated pods, events, leases, and jobs
  - 일시적인 상태 정보인 events, leases 등의 리소스, 인프라 리소스인 nodes, deployments 등을 통해 자동생성된 pods 등은 백업되지 않음


## [Amazon EBS 백업 - 제한 사항](https://docs.aws.amazon.com/ko_kr/aws-backup/latest/devguide/eks-backups.html#eks-limitations)
- AWS Controllers for Kubernetes(ACK), In-tree 컨트롤러를 통해 생성된 혹은 CSI로 마이그레이션된 볼륨은 지원하지 않음
- 교차계정 EFS 백업 지원하지 않음
- Amazon FSx CSI Driver를 통해 생성한 스토리지는 백업 지원하지 않음
- S3 스토리지 백업이 필요한 경우 제약사항 일부 있으므로 문서 참고
- AWS Outposts는 백업지원하지 않음


## 백업 절차
### 1. 사전 준비 단계
- EKS 는 반드시 API 또는 API_AND_CONFIG_MAP 인가모드로 설정
- AWS Backup > Settings > EKS Service opt-in (최초 1회 필요)
- AWS Backup > Vaults 생성
- 백업 주기 결정 (단일 백업, 일일, 주간 스케쥴 백업 등)
- 보관 기간 설정 
- 백업 대상 리소스 태그 전략 (Date, ClusterName 등)
- IAM access entries 에 유효하지 않은 IAM Role은 제거 
- Backup/Restore IAM Role 에 대해 IAM access entries 에 ClusterAdminPolicy 로 추가필요 (복원 중 403 오류가 발생하지 않음)
- (신규 클러스터 복원) IRSA 사용여부 확인 (신규 클러스터 생성 후 파드 오류 발생)
- (신규 클러스터 복원) EKS 관리형 Add-ons 를 제외한 파드의 설정 또는 설정 파일에 ClusterName 이 포함되는 경우 (Karpenter, ClusterAutoscaler 등)


### 2. 단계
- 백업 시작:
  - EKS > Clusters > `ClusterName` > Update history & Backups > Create backup ()
    - Vault 설정
    - IAM 역할 설정

- 백업 확인:
  - AWS Backup > Vaults > `Vault name` > Recovery points 확인
    - composite: 통합 복구지점

- 복구 단계:
  - EKS > Clusters > `ClusterName` > Update history & Backups > Action > Restore backup (StartRestoreJob API 호출)
  - 복구 시 백업데이터를 통해 `기존 클러스터` 에 복구할지 `신규 클러스터`에 복구할지 결정이 필요
  
- 복원 확인:
  - EKS 워크로드 정상 여부 확인
  - EKS Add-ons 정상 여부 확인
  - PVC, PV 정상 여부 확인
  - Ingress 정상 여부 확인


## 테스트
**시나리오**
1. 클러스터 백업
2. 리소스 변경:
    - 단일 Deployment 리소스 삭제
    - ClusterAutoscaler 리소스 삭제(Deployment, ServiceAccount, ClusterRole, Role, ClusterRoleBinding, RoleBinding)
    - EFS CSI Driver 애드온 삭제
    - Pod, PVC, PV(EBS) 삭제
    - Pod, Service, Ingress(ALB) 삭제 
    - EKS 관리형 노드 그룹 삭제
    - Fargate 프로파일 삭제
    - Kubernetes 지원 설정 (Extended -> Standard) 변경
    - EKS 클러스터 태그 삭제/추가 (backup-check-v2: delete 삭제, backup-check-v3: add 추가)
3. 기존 클러스터에 복구 선택
4. 신규 클러스터에 복구 선택


**기존 클러스터에 복구 결과**
1. 단일 Deployment 리소스 정상 복구
2. ClusterAutoscaler 리소스 정상 복구 (Deployment, ServiceAccount, ClusterRole, Role, ClusterRoleBinding, RoleBinding)
3. EFS CSI Driver 에 대한 k8s 리소스 정상 복구, EKS 관리형 Add-on 자체는 복구 되지 않음 (Pod Identity 연결도 복구되지 않음)
4. Pod, PVC, PV(EBS) 정상 복구 확인, 데이터 정상 확인
5. Pod, Service, Ingress(ALB) 복구 (ALB 주소는 변경 됨)
6. 노드 그룹 복구 되지 않음
7. Fargate 프로파일 복구 되지 않음
8. Kubernetes 지원 설정 복구 되지 않음
9. EKS 클러스터 태그 복구되지 않음


**신규 클러스터에 복구 결과**
1. 단일 Deployment 리소스 정상 복구
2. ClusterAutoscaler 리소스 정상 복구 (Deployment, ServiceAccount, ClusterRole, Role, ClusterRoleBinding, RoleBinding)
  1. IRSA 등록 오류 발생 - IAM Identity Provider 등록 및 IAM Trust Relationship 설정 시 오류 해결
3. EFS CSI Driver 애드온 정상 복구 (모든 Add-ons 복구)
4. Pod, PVC, PV(EBS) 정상 복구 확인, 데이터 정상 확인
5. Pod, Service, Ingress(ALB) 복구 안됨
  1. AWS LBC TLS 시크릿을 변경해줘야 하기 때문에 재 배포 필요
6. 노드 그룹 복구
7. Fargate 프로파일 복구
8. Kubernetes 지원 설정 (Standard -> Extended) 복구
9.  EKS 클러스터 태그 복구


## 체크리스트
- 기존 클러스터를 통해 복구 시:
  - 쿠버네티스 리소스에 대해서는 모두 정상 복구 완료
  - AWS 리소스 일부에 대해서는 Non-destructive Restore (비파괴적 복원)로 진행되기 때문에 일부 리소스에 대해 Override 하지 않음
  - 백업 이 후 생성된 리소스에 대해서는 그대로 유지

- 신규 클러스터를 통해 복구 시:
  - `클러스터 보안 그룹(Cluster Security Group)`이 변경 > `커스텀 네트워크`, `SGP` 사용할 경우 보안 그룹 추가/변경 작업 필요
  - IAM Identity Provider 가 등록되어 있지 않기 때문에 복구되는 파드 중 `IRSA`를 사용하는 파드가 있으면 파드 오류 발생
  - 특정 파드의 경우 설정에 ClusterName 을 가지고 있는 경우가 존재하기 때문에 사전에 식별이 필요
  - Mutating webhook 이 있는 경우 secret tls가 있으면 신규로 설정해주어야 함
  - PV(EBS) 백업 시 태그에 KubernetesCluster 이름 등이 기존과 동일하여 변경 필요
  



## 트러블 슈팅
**- 백업 복구 중 오류 발생 - 1**
```
The specified principalArn is invalid: invalid principal. (Service: Eks, Status Code: 400, Request ID: fee4dac9-b41e-4041-9e24-552b0e674d28) (SDK Attempt Count: 1)
```
> 기존 클러스터의 IAM access entries 에 유효하지 않은(삭제된) IAM Role 이 등록되어 있었고, 이 IAM Role 을 참고해서 복원하려다가 오류가 발생



**- 백업 복구 중 오류 발생 - 2**
```
1. ClusterRole, ClusterRoleBinding 과 같은 리소스 복구가 진행되지 않아 워크로드 오류 발생
2. Fargate 프로파일 리소스 복구가 진행되지 않음 
```

> Restore IAM role 은 AmazonEKSClusterAdminPolicy 과 같이 충분한 권한을 가지고 있어야 함


Logs Insights 필터 정보
```
userAgent: Kubernetes Java Client/23.0.0-SNAPSHOT
user.username: arn:aws:sts::<<account>>:assumed-role/AWSBackupRestoreRole/~~~AWBSackup-AWSBackupRestoreRole~~~
```



**- (신규클러스터) 복구 후 특정 파드 오류 발생**
```
│ I0305 09:02:27.846174       1 aws_cloud_provider.go:414] Successfully load 606 EC2 Instance Types [r6gd.large r6i.metal m5n.8xlarge m5d.8xlarge r6in.24xlarge i2.4xlarge m5dn.2xlarge im4gn.large d2.8xlarge x2idn.24xlarge mac2.metal is4gen.xlarge m5n.xlarge r6a.large t3a.nano m4.16xlarge r6in.16xlarge c6i.8xlarge c5a.12xlarge r5.8xlarge c6g.metal m6a.xlarge
│ I0305 09:02:27.846506       1 auto_scaling_groups.go:360] Regenerating instance to ASG map for ASG names: []
│ I0305 09:02:27.846525       1 auto_scaling_groups.go:367] Regenerating instance to ASG map for ASG tags: map[k8s.io/cluster-autoscaler/dev-eks-mix: k8s.io/cluster-autoscaler/enabled:]
│ E0305 09:02:28.252114       1 aws_manager.go:125] Failed to regenerate ASG cache: WebIdentityErr: failed to retrieve credentials
│ caused by: InvalidIdentityToken: No OpenIDConnect provider found in your account for https://oidc.eks.ap-northeast-2.amazonaws.com/id/B1385EE544E2FDF612BFF447A32B8C5E
│     status code: 400, request id: d2ca4afe-78e5-45c5-8ab6-a60c91756b59
│ F0305 09:02:28.252362       1 aws_cloud_provider.go:419] Failed to create AWS Manager: WebIdentityErr: failed to retrieve credentials
```

> IRSA 를 사용하는 파드가 있는 경우 백업 복구된 신규 클러스터의 IAM Identity Provider 등록이 되지 않아 발생하는 오류


**- (신규클러스터) 복구 후 특정 파드의 설정**
```
│ spec:
│   containers:
│   - command:
│     - ./cluster-autoscaler
│     - --v=4
│     - --stderrthreshold=info
│     - --cloud-provider=aws
│     - --skip-nodes-with-local-storage=false
│     - --expander=least-waste
│     - --node-group-auto-discovery=asg:tag=k8s.io/cluster-autoscaler/enabled,k8s.io/cluster-autoscaler/bys-dev-eks-mix
```

> 복구 완료 후 cluster-autoscaler 와 같은 파드의 경우 클러스터 이름을 설정으로 가지고 있을 수 있으므로 내용 확인이 필요

