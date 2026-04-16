---
layout: post
title: "AWS Log Insights를 CloudWatch Logs 확인"
author: "Bys"
category: cloud
date: 2023-09-08
 01:00:00
tags: aws vpc flow log insights
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
