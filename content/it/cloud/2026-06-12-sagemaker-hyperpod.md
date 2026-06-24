---
slug: sagemaker-hyperpod
author: Bys
categories:
- cloud
date: '2026-06-12 01:00:00'
tags:
- aws
- sagemaker
- hyperpod
- slurm
title: SageMaker HyperPod (Slurm)
---

# [SageMaker HyperPod with Slurm](https://docs.aws.amazon.com/sagemaker/latest/dg/sagemaker-hyperpod-slurm.html)  

SageMaker HyperPod의 경우 Orchestrator로 Slurm, EKS를 선택할 수 있다.  

## 1. Architecture
![hyperpod-architecture](/assets/it/cloud/sagemaker/hyperpod-architecture.gif)

Hyperpod cluster를 구성할 때는 Controller 노드, Login 노드, Compute 노드를 구성하게 된다. Controller 노드는 Slurm이 설치되고 순수하게 Slurm 스케줄링만 담당하게 되며, Login 노드는 사용자가 접속해서 sbatch를 제출하는 용도다. Compute 노드는 잡이 수행되는 리소스 노드이다.  

기존 ParallelCluster 에서는 Head노드가 두 역할을 같이 담당했었는데, 다수의 사용자가 SSH, SSM 접속을 하고 여러 잡을 제출하게 되면 리소스 부하가 생기게 되어 Slurm의 잡 스케줄링 연산에 부하가 가지 않도록 노드를 분리했다. 참고로 Head 노드는 수량을 2개로 하여 이중화가 가능하다.

일반적으로 Compute 노드의 경우에는 같은 서브넷에 두게 되는데 EFA 네트워크를 이용하기 위해서는 동일 서브넷에 위치해야 한다. 

## 2. Overview
SageMaker HyperPod의 Slurm 은 머신러닝(ML) 워크로드를 실행하고 Large-language Model, Diffusion Model 등 최첨단 Foundation model을 개발하기 위한 탄력적인 클러스터를 프로비저닝하는 데 도움을 준다.  


#### ParallelCluster 서비스와 차이점
| 항목 | SageMaker HyperPod | AWS ParallelCluster |
|------|-------------------|-------------------|
| 목적 | ML/AI 학습 전용 (LLM, FM 등) | 범용 HPC (날씨/기후 시뮬레이션, 유전체 분석, 금융 리스크 계산 등) |
| 관리 수준 | 완전 관리형 (AWS가 클러스터 관리) | 자체 관리형 (사용자가 클러스터 소유/관리) |
| 자동 복구 | 결함 노드 자동 감지/교체 + 체크포인트에서 학습 자동 재개 | 기본 제공 안 됨 (직접 구현 필요) |
| 스케줄러 | Slurm (또는 EKS) | Slurm, AWS Batch |
| 인스턴스 타입 | ml.* 전용 인스턴스 (ml.p5, ml.trn1 등) | 표준 EC2 인스턴스 (p5, trn1 등) |
| 스케일링 | 연속 프로비저닝 (부분 용량 수용) | 자동 스케일링 (Slurm 큐 기반 노드 추가/제거) |
| 네트워크 | EFA 자동 구성 | EFA 수동 구성 |
| 과금 | 인스턴스 사용 시간 + HyperPod 관리 비용 | EC2 인스턴스 비용만 (ParallelCluster 자체는 무료) |
| 라이프사이클 | DLAMI 기본 제공 + 라이프사이클 스크립트 | 커스텀 AMI + post-install 스크립트 |
| 배포 방식 | SageMaker API / 콘솔 | CloudFormation 기반 CLI 도구 |

- 복원력: HyperPod는 GPU 장애 시 자동으로 노드를 교체하고 학습을 재개하는 것이 핵심 가치. ParallelCluster는 이런 기능이 없어서 직접 구현해야 함.
- 대상 워크로드: HyperPod는 수천 개 GPU로 수주~수개월 걸리는 대규모 FM 학습에 특화. ParallelCluster는 다양한 HPC 워크로드(CFD, 분자 시뮬레이션, 렌더링 등)에 범용적.
- 운영 부담: HyperPod는 AWS가 클러스터 상태를 관리. ParallelCluster는 사용자가 OS 패치, 스케줄러 설정, 장애 대응을 직접 수행.




---

## 📚 References

[1] **Orchestrating SageMaker HyperPod clusters with Slurm**  
- https://docs.aws.amazon.com/sagemaker/latest/dg/sagemaker-hyperpod-slurm.html