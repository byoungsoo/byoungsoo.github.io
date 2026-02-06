---
layout: post
title: "Kiro 사용자별 활동 보고서 Athena 를 통해 조회하기"
author: "Bys"
category: cloud
date: 2026-01-27 01:00:00
keywords: "kiro"
tags: kiro
---


## 1. [Kiro Dashbaord](https://kiro.dev/docs/cli/enterprise/monitor-and-track/dashboard/#dashboard-metrics)  
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
  'quoteChar'     = '"'
)
STORED AS INPUTFORMAT 'org.apache.hadoop.mapred.TextInputFormat' 
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
LOCATION 's3://bys-manage-s3-ue1-kiro/user-activity-report/AWSLogs/'
TBLPROPERTIES (
  'classification' = 'csv',
  'skip.header.line.count' = '1'
);
```

#### 6. 테스트 쿼리 실행
```SQL
SELECT 
    userid,
    COUNT(DISTINCT date) as active_days,
    SUM(CAST(inline_aicodelines AS INT)) as total_inline_aicodelines,
    SUM(CAST(inline_acceptancecount AS INT)) as total_inline_acceptancecount,
    SUM(CAST(inline_suggestionscount AS INT)) as total_inline_suggestionscount,
    SUM(CAST(chat_aicodelines AS INT)) as total_chat_aicodelines,
    SUM(CAST(chat_messagesinteracted AS INT)) as total_chat_messagesinteracted,
    SUM(CAST(chat_messagessent AS INT)) as total_chat_messagessent,
    SUM(CAST(inlinechat_acceptanceeventcount AS INT)) as total_inlinechat_acceptanceeventcount,
    SUM(CAST(inlinechat_acceptedlineadditions AS INT)) as total_inlinechat_acceptedlineadditions,
    SUM(CAST(inlinechat_acceptedlinedeletions AS INT)) as total_inlinechat_acceptedlinedeletions,
    SUM(CAST(inlinechat_dismissaleventcount AS INT)) as total_inlinechat_dismissaleventcount,
    SUM(CAST(inlinechat_dismissedlineadditions AS INT)) as total_inlinechat_dismissedlineadditions,
    SUM(CAST(inlinechat_dismissedlinedeletions AS INT)) as total_inlinechat_dismissedlinedeletions,
    SUM(CAST(inlinechat_rejectedlineadditions AS INT)) as total_inlinechat_rejectedlineadditions,
    SUM(CAST(inlinechat_rejectedlinedeletions AS INT)) as total_inlinechat_rejectedlinedeletions,
    SUM(CAST(inlinechat_rejectioneventcount AS INT)) as total_inlinechat_rejectioneventcount,
    SUM(CAST(inlinechat_totaleventcount AS INT)) as total_inlinechat_totaleventcount
FROM kiro.user_daily_activity
GROUP BY userid
ORDER BY total_inline_aicodelines DESC
LIMIT 20;
```

| # | userid | active_days | inline_aicodelines | inline_acceptancecount | inline_suggestionscount | chat_aicodelines | chat_messagesinteracted | chat_messagessent | inlinechat_acceptanceeventcount | inlinechat_acceptedlineadditions | inlinechat_acceptedlinedeletions | inlinechat_dismissaleventcount | inlinechat_dismissedlineadditions | inlinechat_dismissedlinedeletions | inlinechat_rejectedlineadditions | inlinechat_rejectedlinedeletions | inlinechat_rejectioneventcount | inlinechat_totaleventcount |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | 24a80dec-50e1-70f7-3155-4ccb9e69943f | 9 | 191 | 143 | 646 | 6 | 0 | 15 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |




---

## 📚 References
[1] Amazon Q Developer에서 특정 사용자의 활동 보기
https://docs.aws.amazon.com/ko_kr/amazonq/latest/qdeveloper-ug/q-admin-user-telemetry.html

[2] Amazon Q Developer 사용자 활동 보고서 지표
https://docs.aws.amazon.com/ko_kr/amazonq/latest/qdeveloper-ug/user-activity-metrics.html