---
layout: post
title: "AWS Log Insights를 통한 EKS Control Plane로그 확인"
author: "Bys"
category: cloud
date: 2022-11-04 01:00:00
tags: eks container insights log
---


## Logs Insights
Cloudwatch의 Log groups에 쌓여 있는 로그를 대상으로 쿼리를 수 행할 수 있다.  
EKS에서 발생하는 문제 중 Control Plane쪽 관련된 로그를 확인하기 위해서 사용을 해봤다. 우선 EKS Cluster에서 Logging관련하여 API server, Audit, Authenticator로그를 On시켜놓으면 
Cloudwatch Log groups에 로그가 `/aws/eks/<cluster-name>/cluster/authenticator-1111222272c52cc05e3177863bdcc12d`경로에 쌓이기 시작한다.  

그리고 Logs Insights에서는 아래와 같은 쿼리를 통해 조회가 가능하다.  


### Authenticator Log
1. access denied관련
message에서 access denied가 있는 메세지를 확인할 수 있다. 
```
filter @logStream like /^authenticator/
| fields @logStream, @timestamp, @message
| sort @timestamp desc
| filter @message like "access denied"
| limit 50
```


### Audit Log
1. aws-auth 관련
query를 통해 aws-auth configmaps와 관련된 수정/삭제에 대한 기록을 조회해 볼 수 있다. 
```
fields @logStream, @timestamp, @message
| filter @logStream like /^kube-apiserver-audit/
| filter objectRef.resource == “configmaps” and objectRef.name == "aws-auth" and (verb == “patch” or verb == "delete")
| sort @timestamp desc
```

2. aws-node에 대한 serviceaccounts에 대한 조회 api 이외의 api로그를 확인 해 볼 수 있다. 
```
fields @logStream, @timestamp, @message
  | filter @logStream like /^kube-apiserver-audit/
  | filter verb not in ["get", "list", "watch"]
  | filter objectRef.resource == "serviceaccounts"
  | filter objectRef.name  = "aws-node"
  | sort @timestamp desc
```

### Controller
```
fields @logStream, @timestamp, @message
  | filter @logStream like /^kube-apiserver-audit/
  | filter objectRef.resource == "persistentvolumeclaims"
  | filter objectRef.name  = "efs-provisioner-test"
  | sort @timestamp desc
```


<br><br><br>

> Ref: [Log Inssights document](https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/AnalyzingLogData.html)
> Ref: [Log Insights](https://aws.amazon.com/premiumsupport/knowledge-center/eks-get-control-plane-logs/ )