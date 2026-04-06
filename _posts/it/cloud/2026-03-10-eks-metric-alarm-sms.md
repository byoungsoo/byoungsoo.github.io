---
layout: post
title: "Container Insight 메트릭 알람 SMS 수신 "
author: "Bys"
category: cloud
date: 2026-03-10 01:00:00
keywords: "eks alarm sms"
tags: eks alarm sms
---

# SNS SMS 알람 수신
서울 리전에서는 AWS SNS 서비스를 통한 SMS 알람 수신을 지원하지 않는다. 하지만, 도쿄 리전의 AWS SNS 서비스를 연계하여 알람을 수신할 수 있다.  


## 아키텍처
⏺ ┌─────────────────────────────────────────────────────────────────────────────┐
  │                          ap-northeast-2 (서울)                               │
  │                                                                             │
  │  ┌─────────────────────────────────────────────────────────────────────┐   │
  │  │  EKS Cluster: bys-dev-eks-mix                                       │   │
  │  │                                                                     │   │
  │  │  Container Insights 메트릭                                           │   │
  │  │  Namespace: ContainerInsights                                       │   │
  │  │  Metric: pod_container_status_terminated                            │   │
  │  │  Dimensions: ClusterName / Namespace / PodName                     │   │
  │  └──────────────────────────────┬──────────────────────────────────────┘   │
  │                                 │ 메트릭 수집                                │
  │                                 ▼                                           │
  │  ┌──────────────────────────────────────────────────────────────────────┐  │
  │  │  CloudWatch Alarm                                                    │  │
  │  │  Name : EKS_Mix_PodContainerTerminatedAlarm                         │  │
  │  │  Query : SELECT SUM(pod_container_status_terminated)                │  │
  │  │          FROM SCHEMA("ContainerInsights", ClusterName,Namespace,    │  │
  │  │          PodName) WHERE ClusterName = 'bys-dev-eks-mix'             │  │
  │  │  조건  : SUM >= 1 / Period 60s / EvaluationPeriods 1               │  │
  │  │  상태  : ALARM / OK / INSUFFICIENT_DATA                             │  │
  │  └──────────────────────────────┬───────────────────────────────────────┘  │
  │                                 │ Alarm Action (상태 변경 시)               │
  │                                 ▼                                           │
  │  ┌──────────────────────────────────────────────────────────────────────┐  │
  │  │  SNS Topic                                                           │  │
  │  │  Name : EKS_Mix_PodContainerTerminated                              │  │
  │  │  ARN  : arn:aws:sns:ap-northeast-2:558846430793:...                 │  │
  │  │                                                                     │  │
  │  │  ※ 향후 이메일, Slack 등 구독 추가 가능                               │  │
  │  └──────────────────────────────┬───────────────────────────────────────┘  │
  │                                 │ Lambda 구독 (invoke)                      │
  │                                 ▼                                           │
  │  ┌──────────────────────────────────────────────────────────────────────┐  │
  │  │  Lambda                                                              │  │
  │  │  Name    : CloudwatchSMSLambda                                      │  │
  │  │  Runtime : Python 3.12                                              │  │
  │  │  Role    : AWSCloudWatchSNSLambdaRole                               │  │
  │  │            └─ sns:Publish (전 리전)                                  │  │
  │  │            └─ logs:PutLogEvents                                     │  │
  │  │  Env     : TOPIC_ARN = arn:aws:sns:ap-northeast-1:...:             │  │
  │  │                        DEV_AP2_EKS_SMS_Alarm                       │  │
  │  │            SMS_REGION = ap-northeast-1                             │  │
  │  │                                                                     │  │
  │  │  처리 로직:                                                          │  │
  │  │  1. SNS 페이로드에서 CW Alarm 메시지 파싱                            │  │
  │  │  2. StateChangeTime UTC → KST 변환                                  │  │
  │  │  3. SMS 본문 생성                                                   │  │
  │  │     [ALARM or OK]                                                   │  │
  │  │     Name: {AlarmName}                                               │  │
  │  │     Time: {YYYY-MM-DD HH:MM KST}                                   │  │
  │  │  4. 도쿄 SNS Topic으로 publish                                      │  │
  │  └──────────────────────────────┬───────────────────────────────────────┘  │
  │                                 │ boto3.client('sns',                       │
  │                                 │   region_name='ap-northeast-1')           │
  │                                 │ sns.publish(TopicArn=...)                 │
  └─────────────────────────────────┼───────────────────────────────────────────┘
                                    │
                                    │  리전 간 API 호출
                                    │
  ┌─────────────────────────────────┼───────────────────────────────────────────┐
  │                          ap-northeast-1 (도쿄)          │                   │
  │                                 ▼                                           │
  │  ┌──────────────────────────────────────────────────────────────────────┐  │
  │  │  SNS Topic (SMS 수신자 중앙 관리 허브)                                │  │
  │  │  Name : DEV_AP2_EKS_SMS_Alarm                                       │  │
  │  │  ARN  : arn:aws:sns:ap-northeast-1:558846430793:...                 │  │
  │  │  Mode : Sandbox (Verified 번호만 수신 가능)                          │  │
  │  │                                                                     │  │
  │  │  구독자 (SMS)                                                        │  │
  │  │  ├─ +821052369497  ← Verified ✅                                    │  │
  │  │  ├─ +82xxxxxxxxxx  ← 추가 가능                                      │  │
  │  │  └─ +82xxxxxxxxxx  ← 추가 가능                                      │  │
  │  └──────────────────────────────┬───────────────────────────────────────┘  │
  └─────────────────────────────────┼───────────────────────────────────────────┘
                                    │ SMS 발송
                                    ▼
                              📱 +821052369497
                           ┌─────────────────┐
                           │   [ALARM]       │
                           │   Name: EKS_... │
                           │   Time: KST     │
                           └─────────────────┘

  흐름 요약:
  1. EKS 클러스터의 pod_container_status_terminated 합산값이 1 이상 되면
  2. CloudWatch Alarm 이 ALARM 상태로 전환
  3. 서울 SNS Topic 으로 알림 전송
  4. SNS가 서울 Lambda 호출
  5. Lambda가 도쿄 리전 SNS API 를 통해 SMS 직접 발송
  6. 📱 문자 수신


  SNS 경유가 유리한 경우:
  - 알람 수신자가 여러 명이거나 채널이 다양할 때 (이메일 + SMS + Slack 동시)
  - 향후 알림 채널을 쉽게 추가/제거하고 싶을 때
  - 프로덕션 환경에서 메시지 유실 없이 안정적으로 운영할 때

  Lambda 직접 호출이 유리한 경우:
  - SMS 단일 채널로만 보낼 때
  - 구조를 최대한 단순하게 유지하고 싶을 때
  - 이번처럼 테스트 목적일 때


AWSCloudWatchSNSLambdaRole



aws sns publish \
  --topic-arn arn:aws:sns:ap-northeast-2:558846430793:EKS_Mix_PodContainerTerminated \
  --message '{"AlarmName":"EKS_Mix_PodContainerTerminatedAlarm","NewStateValue":"ALARM","OldStateValue":"OK","NewStateReason":"테스트 알람 (도쿄 Topic 경유)","Region":"아시아 태평양 (서울)","AWSAccountId":"558846430793"}' \
  --region ap-northeast-2