---
slug: kiro-user-report
author: Bys
categories:
- cloud
category: cloud
date: '2026-01-27 01:00:00'
keywords: kiro
tags:
- kiro
title: Kiro 사용자별 사용량(Credit Usage) 확인하기 - Athena Query
description: "Kiro 사용자별 Credit Usage를 Athena Query로 조회하는 방법 - Organization 관리자를 위한 Per-user activity 분석 가이드"
---



## 1. [Kiro Dashboard](https://kiro.dev/docs/cli/enterprise/monitor-and-track/dashboard/#dashboard-metrics)  
Kiro 대시보드에서는 Pro, Pro+, Power tier 별 총 구독자수, 활성화 된 구독자수 등 전체적인 지표를 보여준다. Organization 을 관리 하는 입장에서는 사용자별 사용량에 대한 파악이 어렵기 때문에 개별 사용자 레포트를 발행하여 확인할 필요가 있다.  


## 2. [Per-user activity](https://kiro.dev/docs/cli/enterprise/monitor-and-track/user-activity/)  
유저별 사용량을 확인하기 위해서는 'Kiro user activity report' 설정을 활성화하여 가능하다.  
  - Amazon Q Developer > Settings > Kiro user activity report (토글 활성화) > S3 bucket 선택

해당 설정을 활성화 하면 매일(09:00 KST, 00:00 UTC) S3 버킷에 사용자별 활동 보고서가 업로드 되며 이를 Athena 쿼리를 통해 조회할 수 있도록 몇 가지 추가 작업을 진행한다.  

## 3. User activity by Athena query
#### 1. Kiro user activity report 용 S3 Bucket 생성

#### 2. Bucket policy 설정. SSE-KMS를 구성하는 경우 추가설정 필요[1]
- Account 변경 필요  

```json
{
    "Version":"2012-10-17",		 	 	 
    "Statement": [
        {
            "Sid": "QDeveloperLogsWrite",
            "Effect": "Allow",
            "Principal": {
                "Service": "q.amazonaws.com"
            },
            "Action": [
                "s3:PutObject"
            ],
            "Resource": [
                "arn:aws:s3:::bucketName/prefix/*"
            ],
            "Condition": {
                "StringEquals": {
                    "aws:SourceAccount": "111122223333"
                },
                "ArnLike": {
                    "aws:SourceArn": "arn:aws:codewhisperer:us-east-1:111122223333:*"
                }
            }
        }
    ]
}
```

#### 3. Amazon Q Developer > Settings > Kiro user activity report (토글 활성화) > S3 bucket 선택

#### 4. Athena > Launch query editor(Query your data in Athena console 선택)

#### 5. Athena 테이블 생성  
- Database 이름, Table 이름은 사용자별로 변경 가능
- LOCATION은 올바른 S3로 변경 필요

**New**
```SQL
CREATE EXTERNAL TABLE IF NOT EXISTS `kiro`.`user_activity_report` (
  `date` STRING,
  `userid` STRING,
  `client_type` STRING,
  `chat_conversations` DOUBLE,
  `credits_used` DOUBLE,
  `overage_cap` DOUBLE,
  `overage_credits_used` DOUBLE,
  `overage_enabled` STRING,
  `profileid` STRING,
  `subscription_tier` STRING,
  `total_messages` DOUBLE
)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
WITH SERDEPROPERTIES (
  'separatorChar' = ',',
  'quoteChar'     = '"',
  'escapeChar'    = '\\'
)
STORED AS INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat' 
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
LOCATION 's3://bys-manage-s3-ue1-kiro/user-activity-report/AWSLogs/692806374063/KiroLogs/user_report/'
TBLPROPERTIES (
  'classification' = 'csv',
  'skip.header.line.count' = '1'
);
```

**Old**
```SQL
CREATE EXTERNAL TABLE IF NOT EXISTS `kiro`.`user_daily_activity` (
  `userid` STRING,
  `date` STRING,
  `chat_aicodelines` BIGINT,
  `chat_messagesinteracted` BIGINT,
  `chat_messagessent` BIGINT,
  `codefix_acceptanceeventcount` BIGINT,
  `codefix_acceptedlines` BIGINT,
  `codefix_generatedlines` BIGINT,
  `codefix_generationeventcount` BIGINT,
  `codereview_failedeventcount` BIGINT,
  `codereview_findingscount` BIGINT,
  `codereview_succeededeventcount` BIGINT,
  `dev_acceptanceeventcount` BIGINT,
  `dev_acceptedlines` BIGINT,
  `dev_generatedlines` BIGINT,
  `dev_generationeventcount` BIGINT,
  `docgeneration_acceptedfileupdates` BIGINT,
  `docgeneration_acceptedfilescreations` BIGINT,
  `docgeneration_acceptedlineadditions` BIGINT,
  `docgeneration_acceptedlineupdates` BIGINT,
  `docgeneration_eventcount` BIGINT,
  `docgeneration_rejectedfilecreations` BIGINT,
  `docgeneration_rejectedfileupdates` BIGINT,
  `docgeneration_rejectedlineadditions` BIGINT,
  `docgeneration_rejectedlineupdates` BIGINT,
  `inlinechat_acceptanceeventcount` BIGINT,
  `inlinechat_acceptedlineadditions` BIGINT,
  `inlinechat_acceptedlinedeletions` BIGINT,
  `inlinechat_dismissaleventcount` BIGINT,
  `inlinechat_dismissedlineadditions` BIGINT,
  `inlinechat_dismissedlinedeletions` BIGINT,
  `inlinechat_rejectedlineadditions` BIGINT,
  `inlinechat_rejectedlinedeletions` BIGINT,
  `inlinechat_rejectioneventcount` BIGINT,
  `inlinechat_totaleventcount` BIGINT,
  `inline_aicodelines` BIGINT,
  `inline_acceptancecount` BIGINT,
  `inline_suggestionscount` BIGINT,
  `testgeneration_acceptedlines` BIGINT,
  `testgeneration_acceptedtests` BIGINT,
  `testgeneration_eventcount` BIGINT,
  `testgeneration_generatedlines` BIGINT,
  `testgeneration_generatedtests` BIGINT,
  `transformation_eventcount` BIGINT,
  `transformation_linesgenerated` BIGINT,
  `transformation_linesingested` BIGINT
)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
WITH SERDEPROPERTIES (
  'separatorChar' = ',',
  'quoteChar'     = '"',
  'escapeChar'    = '\\'
)
STORED AS INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat' 
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
LOCATION 's3://bys-manage-s3-ue1-kiro/user-activity-report/AWSLogs/692806374063/KiroLogs/by_user_analytic/'
TBLPROPERTIES (
  'classification' = 'csv',
  'skip.header.line.count'='1'
);
```

#### 6. 테스트 쿼리 실행

**New**
```SQL
-- 사용자별, Client Type 별 일일 크레딧 사용량 조회 쿼리
SELECT 
    date,
    userid,
    client_type,
    subscription_tier,
    CAST(total_messages AS BIGINT) as total_messages,
    CAST(chat_conversations AS BIGINT) as chat_conversations,
    ROUND(credits_used, 2) as credits_used,
    ROUND(overage_credits_used, 2) as overage_credits_used,
    overage_enabled,
    CAST(overage_cap AS BIGINT) as overage_cap
FROM kiro.user_activity_report
ORDER BY date DESC, credits_used DESC
LIMIT 50;

-- 사용자별 일일 크레딧 사용량 조회 쿼리
SELECT 
    date,
    userid,
    subscription_tier,
    ROUND(SUM(credits_used), 2) as daily_credits_used,
    ROUND(SUM(overage_credits_used), 2) as daily_overage_credits,
    CAST(SUM(total_messages) AS BIGINT) as daily_total_messages,
    CAST(SUM(chat_conversations) AS BIGINT) as daily_conversations
FROM kiro.user_activity_report
GROUP BY date, userid, subscription_tier
ORDER BY date DESC, daily_credits_used DESC;

-- 사용자별 월별 크레딧 사용량 조회 쿼리
SELECT 
    SUBSTRING(date, 1, 7) as year_month,
    userid,
    subscription_tier,
    COUNT(DISTINCT date) as active_days,
    ROUND(SUM(credits_used), 2) as monthly_credits_used,
    ROUND(SUM(overage_credits_used), 2) as monthly_overage_credits,
    CAST(SUM(total_messages) AS BIGINT) as monthly_total_messages,
    CAST(SUM(chat_conversations) AS BIGINT) as monthly_conversations
FROM kiro.user_activity_report
GROUP BY SUBSTRING(date, 1, 7), userid, subscription_tier
ORDER BY year_month DESC, monthly_credits_used DESC;
```

**사용자별 월별 사용량**

| # | year_month | userid | subscription_tier | active_days | monthly_credits_used | monthly_overage_credits | monthly_total_messages | monthly_conversations |
|---|------------|--------|-------------------|-------------|----------------------|-------------------------|------------------------|-----------------------|
| 1 | 2026-03 | 24a80dec-50e1-70f7-3155-4ccb9e69943f | PRO | 5 | 50.99 | 0.0 | 308 | 7 |
| 2 | 2026-02 | 24a80dec-50e1-70f7-3155-4ccb9e69943f | PRO | 11 | 89262.99 | 0.0 | 1124 | 41 |

---

## 📚 References
[1] Amazon Q Developer에서 특정 사용자의 활동 보기
https://docs.aws.amazon.com/ko_kr/amazonq/latest/qdeveloper-ug/q-admin-user-telemetry.html

[2] Amazon Q Developer 사용자 활동 보고서 지표
https://docs.aws.amazon.com/ko_kr/amazonq/latest/qdeveloper-ug/user-activity-metrics.html

[3] Viewing per-user activity
https://kiro.dev/docs/enterprise/monitor-and-track/user-activity/