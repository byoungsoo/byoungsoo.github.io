---
layout: post
title: "AWS Log Insights를 통한 EKS Control Plane로그 확인"
author: "Bys"
category: cloud
date: 2022-11-04 01:00:00
tags: aws eks log insights
---


## Logs Insights
[Query 구문](https://docs.aws.amazon.com/ko_kr/AmazonCloudWatch/latest/logs/CWL_QuerySyntax.html)  
Cloudwatch의 Log groups에 쌓여 있는 로그를 대상으로 쿼리를 수 행할 수 있다.  
EKS에서 발생하는 문제 중 Control Plane쪽 관련된 로그를 확인하기 위해서 사용을 해봤다. 우선 EKS Cluster에서 Logging관련하여 API server, Audit, Authenticator로그를 On시켜놓으면 Cloudwatch Log groups에 로그가 쌓이기 시작한다.  
그리고 Logs Insights에서는 아래와 같은 쿼리를 통해 조회가 가능하다.  


## [EKS](https://aws.amazon.com/ko/premiumsupport/knowledge-center/eks-get-control-plane-logs/) 
- EKS Log Groups format - `/aws/eks/<cluster-name>/cluster`
    - `kube-apiserver-audit-4e9f9d6f9d15381d05d77c5df3f7d329`
    - `kube-apiserver-934e06efeb2e9416b02fa19f9a2d1774`
    - `authenticator-1111222272c52cc05e3177863bdcc12d`
    - `cloud-controller-manager-934e06efeb2e9416b02fa19f9a2d1774`
    - `kube-scheduler-934e06efeb2e9416b02fa19f9a2d1774`
    
```bash
# kubernetes verb
verb = list, get, watch, create, patch, delete

# Kubernetes Objects
objectRef.resource = configmaps, deployments, replicasets, pods, statefulsets, persistentvolumeclaims, serviceaccounts, targetgroupbindings ......
```

### Authenticator Log
1. access denied관련
message에서 access denied가 있는 메세지를 확인할 수 있다. 
```bash
filter @logStream like /^authenticator/
| fields @logStream, @timestamp, @message
| sort @timestamp desc
| filter @message like "access denied"
| limit 50
```

### kube-controller-manager log 
```bash
fields @timestamp, @message
  | filter @logStream like /^kube-controller-manager/
  | filter @message like "FLAG"
  | sort @timestamp desc
```

### cloud-controller-manager log 
```bash
fields @timestamp, @message
  | filter @logStream like /^cloud-controller-manager/
  | filter @message like ""
  | sort @timestamp desc
```


### kube-apiserver Log
```bash
fields @timestamp, @message
  | filter @logStream like /^kube-apiserver-/
  | filter @logStream not like /^kube-apiserver-audit/
  | filter @message like "FLAG"
  | sort @timestamp desc
```

### etcd Log
```bash
fields @timestamp, @message
  | filter @logStream like /^etcd-/
  | filter @message like "apply request took too long"
  | sort @timestamp desc
```


### Audit Log
- Event object
```bash
fields @timestamp, userAgent, verb, objectRef.resource, objectRef.name, user.extra.sessionName.0
| filter @logStream like "kube-apiserver-audit" 
| filter verb not in ["list", "watch", "get"]
| filter objectRef.resource == "events"
| sort @timestamp desc
```


- Lease object
```bash
fields @timestamp, userAgent, verb, objectRef.resource, objectRef.name, user.extra.sessionName.0
| filter @logStream like "kube-apiserver-audit" 
| filter verb not in ["list", "watch", "get"]
| filter objectRef.resource == "leases"
| filter objectRef.name == "ip-10-20-42-209.ap-northeast-2.compute.internal"
| sort @timestamp desc
```

- Node object
```bash
fields @timestamp, userAgent, verb, objectRef.resource, objectRef.name, requestObject.status.conditions.3.type
| filter @logStream like "kube-apiserver-audit" 
| filter verb not in ["list", "watch", "get"]
| filter objectRef.resource == "nodes"
| filter objectRef.name == "ip-10-20-42-209.ap-northeast-2.compute.internal"
| sort @timestamp desc
```

- Pod object
```bash
fields @timestamp, userAgent, verb, objectRef.resource, objectRef.name, @messages
| filter @logStream like "kube-apiserver-audit" 
| filter verb not in ["list", "watch", "get"]
| filter objectRef.resource == "pods"
| filter objectRef.name == "aws-load-balancer-controller-7875bd8f78-7b866"
| sort @timestamp desc
```

- PV object
```bash
fields @timestamp, userAgent, verb, objectRef.resource, objectRef.name, @messages
| filter @logStream like "kube-apiserver-audit" 
| filter verb not in ["list", "watch", "get"]
| filter objectRef.resource == "persistentvolumes"
| filter objectRef.name == "pvc-af163ed3-b2f1-4fbc-9179-e722afaf3de4"
| sort @timestamp desc
```


- Multiple objects
```bash
fields @timestamp, userAgent, verb, objectRef.resource, objectRef.name
| filter @logStream like "kube-apiserver-audit" 
| filter verb not in ["list", "watch", "get"]
| filter objectRef.resource in ["persistentvolumeclaims", "persistentvolumes", "volumeattachments", "volumesnapshots", "volumesnapshotcontents"]
| sort @timestamp desc
```

- aws-auth 관련  
query를 통해 aws-auth configmaps와 관련된 수정/삭제에 대한 기록을 조회해 볼 수 있다.  
```bash
fields @timestamp, verb, user.username, objectRef.resource, objectRef.namespace, objectRef.name , responseObject.code, @message
| filter @logStream like /^kube-apiserver-audit/
| filter objectRef.resource == "configmaps"
| filter objectRef.name == "aws-auth"
|filter verb not in ["watch", "list", "get"]
| sort @timestamp desc
```

- aws-node에 대한 serviceaccounts에 대한 조회 api 이외의 api로그를 확인 해 볼 수 있다.  
```bash
fields @timestamp, verb, user.username, objectRef.resource, objectRef.namespace, objectRef.name , responseObject.code, @message
  | filter @logStream like /^kube-apiserver-audit/
  | filter verb not in ["get", "list", "watch"]
  | filter objectRef.resource == "serviceaccounts"
  | filter objectRef.name  = "aws-node"
  | sort @timestamp desc
```

- HPA
```bash
fields @timestamp, responseObject.spec.targetCPUUtilizationPercentage, responseObject.status.currentCPUUtilizationPercentage, responseObject.status.desiredReplicas, responseObject.status.currentReplicas
| filter @logStream like /audit/
| filter requestURI like '/apis/autoscaling/v1/namespaces/test-namespace/horizontalpodautoscalers/test-hpa/status'
| sort @timestamp asc
```

- Eviction API
```bash
fields @timestamp, @message
| filter @logStream like /^kube-apiserver-audit/
| filter requestURI like '/eviction'
| sort @timestamp desc
```


- 특정 requestURI를 통해 찾을 때 아래의 내용을 통해 확인한다. [API Docs](https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.24/#-strong-api-overview-strong-)  

```bash
# Delete pods
fields @logStream, @timestamp, requestURI, @message
| filter @logStream like /^kube-apiserver-audit/
| filter requestURI like "/api/v1/namespaces/{namespace}/pods/{name}"
| filter verb == "delete"
| sort @timestamp desc

# Delete nodes
fields @logStream, @timestamp, requestURI, @message
| filter @logStream like /^kube-apiserver-audit/
| filter requestURI like "/api/v1/nodes/{name}"
| filter verb == "delete"
| sort @timestamp desc
```

  `Test`  
  ```bash
  kubectl delete node ip-10-20-10-35.ap-northeast-2.compute.internal
  ```

  아래의 내용을 통해 Log insight 조회 
  ```bash
  fields @logStream, @timestamp, requestURI, @message
  | filter @logStream like /^kube-apiserver-audit/
  | filter requestURI like "/api/v1/node"
  | filter verb == "delete"
  | sort @timestamp desc
  ```

아래와 같은 결과를 확인할 수 있다.  
![delete_audit_log001.png](/assets/it/cloud/eks/delete_audit_log001.png){: width="95%" height="auto"}  

### Controller
`EKS Controller List`
```txt
attachdetach-controller
certificate-controller
clusterrole-aggregation-controller
cronjob-controller
daemon-set-controller
deployment-controller
disruption-controller
eks-vpc-resource-controller
endpoint-controller
endpointslice-controller
endpointslicemirroring-controller
ephemeral-volume-controller
expand-controller
job-controller
namespace-controller
node-controller
pv-protection-controller
pvc-protection-controller
replicaset-controller
replication-controller
resourcequota-controller
service-account-controller
service-controller
statefulset-controller
tagging-controller
ttl-after-finished-controller
ttl-controller
```

```bash
fields @timestamp, verb, user.username, objectRef.resource, objectRef.namespace, objectRef.name , responseObject.code, @message
  | filter @logStream like /^kube-apiserver-audit/
  | filter objectRef.resource == "persistentvolumeclaims"
  | filter objectRef.name  = "efs-provisioner-test"
  | sort @timestamp desc
```

`vpc-resource-controller`
```bash
fields @timestamp, verb, user.username, objectRef.resource, objectRef.namespace, objectRef.name , responseObject.code, @message
| filter @logStream like /^kube-apiserver-audit/
| filter objectRef.namespace == "default"
| filter verb not in ["list", "watch", "get"]
| filter user.username like "vpc-resource-controller"
| sort @timestamp desc
```

`watch object`
```bash
fields @timestamp, verb, user.username, objectRef.resource, objectRef.namespace, objectRef.name , responseObject.code, @message
| filter @logStream like /^kube-apiserver-audit/
| filter user.username like "kube-controller"
| filter objectRef.resource == "replicasets"
| filter verb == "watch"
| sort @timestamp desc
```

`deployment-controller`
```bash
fields @timestamp, verb, user.username, objectRef.resource, objectRef.namespace, objectRef.name , responseObject.code, @message
| filter @logStream like /^kube-apiserver-audit/
# | filter user.username like "kube-controller"
| filter objectRef.resource == "replicasets"
| filter verb == "create"
| sort @timestamp desc
```
> kube-controller-manager/v1.25.8 (linux/amd64) kubernetes/83fe90d/system:serviceaccount:kube-system:deployment-controller -> create -> replicasets

`replicaset-controller`  
```bash
fields @timestamp, verb, user.username, objectRef.resource, objectRef.namespace, objectRef.name , responseObject.code, @message
  | filter @logStream like /^kube-apiserver-audit/
  | filter verb == "create"
  | filter userAgent  like "replicaset-controller"
  | sort @timestamp desc
```
> kube-controller-manager/v1.25.8 (linux/amd64) kubernetes/83fe90d/system:serviceaccount:kube-system:replicaset-controller -> create -> pods


### TargetGroupBindings
`Sample Data`
```
objectRef.apiGroup	 |   elbv2.k8s.aws
objectRef.apiVersion |   v1beta1
objectRef.name	     |   k8s-default-nginx-5ce4c141a0
objectRef.namespace	 |   default
objectRef.resource	 |   targetgroupbindings
```

```bash
# verb - create, patch
fields @timestamp, @message
  | filter @logStream like /^kube-apiserver-audit/
  | filter verb not in ["get", "list", "watch"]
  | filter objectRef.namespace != "kubernetes-dashboard"
  | filter objectRef.resource == "targetgroupbindings"
  | sort @timestamp desc
```



<br><br>

## ECS

### ECS Application Logs
```json
"logConfiguration": 
{
  "logDriver": "awslogs",
  "secretOptions": null,
  "options": {
    "awslogs-group": "/aws/ecs/containerinsights/bys-dev-ecs-main/application",
    "awslogs-region": "ap-northeast-2",
    "awslogs-stream-prefix": "ecs"
  }
}
```
- ECS Log Groups format - `<awslogs-group of task-definition>`
    - `ecs/<container-name>/<task-id>`

### Task ID
```
fields @logStream, @timestamp, @message
  | filter @logStream like "ef7512c63ef2498880ecf78fd8f9ed11"
  | sort @timestamp desc


fields @logStream, @timestamp, @message
  | filter @logStream like /ecs\/awssdk-iam-dev\/ef7512c63ef2498880ecf78fd8f9ed11/
  | sort @timestamp desc
```

## Container name 
```
fields @logStream, @timestamp, @message
  | filter @logStream like /^ecs\/nginx*/
  | sort @timestamp desc
```


<br><br><br>

> Ref: [Log Insights document](https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/AnalyzingLogData.html)
> Ref: [EKS Log Insights](https://aws.amazon.com/premiumsupport/knowledge-center/eks-get-control-plane-logs/)