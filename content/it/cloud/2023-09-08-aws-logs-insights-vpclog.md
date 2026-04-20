---
slug: aws-logs-insights-vpclog
author: Bys
categories:
- cloud
category: cloud
date: '2023-09-08 01:00:00'
tags:
- aws
- vpc
- flow
- log
- insights
title: AWS Log Insights를 CloudWatch Logs 확인
description: "AWS CloudWatch Log Insights를 사용한 VPC Flow Logs 쿼리 및 분석 방법. VPC 트래픽 패턴 분석, 보안 이슈 탐지, 네트워크 트러블슈팅을 위한 실용적인 쿼리 예제를 설명합니다."
---



## VPC Flow Logs Query

- srcAddr, dstAddr, dstPort
```bash
fields @timestamp, @message
| filter srcAddr == '10.20.2.177'
| filter dstAddr in ['10.25.30.84', '10.25.43.168', '10.25.59.190']
| filter dstPort == 443
| sort @timestamp desc
```



---

## 📚 References

[1] **Log Insights document**  
- https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/AnalyzingLogData.html
