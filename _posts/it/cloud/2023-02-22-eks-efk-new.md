---
layout: post
title: "EKS EFK 구축 (FluentBit, OpenSearch)"
author: "Bys"
category: cloud
date: 2023-02-22 01:00:00
tags: eks efk fluentbit opensearch elasticsearch kibana
---

# EFK
EFK(ElasticSearch, FluentD, Kibana)란 EKS환경에 ElasticSearch(ES), FluentD, Kibana를 이용해 이용해 EKS환경에 Logging 아키텍처를 구성하는 것을 의미한다.  
AWS에서는 오픈소스인 ES를 가지고 서비스를 진행 중 Folk를 진행하여 자체 서비스로 개발을 진행해왔다. 따라서 이제 OpenSearch는 ES는 아니며 AWS의 개별 서비스로 봐야하지만 전신은 ES다.  
AWS에서는 ES 대신 OpenSearch를 이용하며 FluentD대신 경량화 버전인 FluentBit을 이용해 구성할 예정이다. 아키텍처는 먼저 FluentBit을 이용해 Cloudwatch로 로그를 전송하며 Cloudwatch로 전송된 로그를 AWS Kinesis를 이용하여 OpenSearch로 전송한다.   

## 1. FluentBit Daemon 구성 (Cloudwatch 로그 전송)
[Installation Guide](https://docs.aws.amazon.com/ko_kr/AmazonCloudWatch/latest/monitoring/Container-Insights-setup-logs-FluentBit.html)

`Create Configmap cluster-info`
```bash
ClusterName=cluster-name
RegionName=cluster-region
FluentBitHttpPort='2020'
FluentBitReadFromHead='Off'
[[ ${FluentBitReadFromHead} = 'On' ]] && FluentBitReadFromTail='Off'|| FluentBitReadFromTail='On'
[[ -z ${FluentBitHttpPort} ]] && FluentBitHttpServer='Off' || FluentBitHttpServer='On'
kubectl create configmap fluent-bit-cluster-info \
--from-literal=cluster.name=${ClusterName} \
--from-literal=http.server=${FluentBitHttpServer} \
--from-literal=http.port=${FluentBitHttpPort} \
--from-literal=read.head=${FluentBitReadFromHead} \
--from-literal=read.tail=${FluentBitReadFromTail} \
--from-literal=logs.region=${RegionName} -n amazon-cloudwatch
```
<br>

`Create Namespace`
```bash
kubectl apply -f https://raw.githubusercontent.com/aws-samples/amazon-cloudwatch-container-insights/latest/k8s-deployment-manifest-templates/deployment-mode/daemonset/container-insights-monitoring/cloudwatch-namespace.yaml
```
<br>


`FluentBit DaemonSet`
```bash
kubectl apply -f https://raw.githubusercontent.com/aws-samples/amazon-cloudwatch-container-insights/latest/k8s-deployment-manifest-templates/deployment-mode/daemonset/container-insights-monitoring/fluent-bit/fluent-bit.yaml
```

데몬 셋을 배포할 때 OUTPUT 부분에 .$kubernetes['namespace_name'].$kubernetes['container_name'] 부분을 추가하여 namespace별 container_name별 LogGroup이 생성되도록 해야 Cloudwatch -> OpenSearch로 스트림을 보낼 때 index_name을 구분할 수 있다.  
```conf
[OUTPUT]
    Name                cloudwatch_logs
    Match               application.*
    region              ${AWS_REGION}
    log_group_name      /aws/containerinsights/${CLUSTER_NAME}
    log_group_template  /aws/containerinsights/${CLUSTER_NAME}.$kubernetes['namespace_name'].$kubernetes['container_name']
    log_stream_prefix   ${HOST_NAME}-
    auto_create_group   true
    extra_user_agent    container-insights
```

<br>

`IAM Policy`  
IRSA를 적용하였다. fluent-bit ServiceAccount에 IAM role을 적용하고 해당 role의 Policy에는 CloudwatchFullAccess 권한을 부여하였다.  
```bash
# kubectl describe sa fluent-bit -n amazon-cloudwatch
Name:                fluent-bit
Namespace:           amazon-cloudwatch
Labels:              <none>
Annotations:         eks.amazonaws.com/role-arn: arn:aws:iam::558846430793:role/EKSFluentBitServiceRole
Image pull secrets:  <none>
Mountable secrets:   <none>
Tokens:              <none>
Events:              <none>
```

<br>

이렇게 배포를 하면 amazon-cloudwatch namespace에 fluent-bit-* 이름의 pod가 eks cluster에 배포가 된다. 배포가 된 Pod의 Container로그는 Cloudwatch Log Groups에서 확인 할 수 있다.  
기본적으로 /var/log/containers 아래 kubelet이 컨테이너 로그를 생성하며 이 로그를 Cloudwatch로 전송한다. 수정을 위해서는 fluent-bit.yaml 파일을 수정한다.  

<br><br>

## 2. AWS OpenSearch 구성

- 접근 제한  

`AWS OpenSearch(최신)`  
Public으로 접근 가능하도록 구성하였으며 Access policy에는 아래와 같이 IP조건을 추가하여 Access를 제한하였다.  
```bash
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": "*"
      },
      "Action": "es:*",
      "Resource": "arn:aws:es:ap-northeast-2:558846430793:domain/bys-dev-opensearch-eks/*",
      "Condition": {
        "IpAddress": {
          "aws:SourceIp": [
            "54.239.116.0/23",
            "54.239.119.0/25",
            "121.128.248.86/32"
          ]
        }
      }
    }
  ]
}
```

만약 해당 SouceIp가 해당 대역이 아닌 경우 아래와 같은 오류 메세지가 발생하며 오류가 발생한다.  
```json
{"Message":"User: anonymous is not authorized to perform: es:ESHttpGet because no resource-based policy allows the es:ESHttpGet action"}
```

<br>

- 권한 설정

`Lambda Role 권한설정`  
Cloudwatch Loggroup에서 subscription filter를 걸어 OpenSearch로 전달할 경우 Lambda function이 생성되어 전달을 하게 되는데 이 때 Lambda가 사용하는 role을 아래와 같이 등록해주어야 한다. 
fine-grained access control 설정이 되어있는 상황에서 아래의 절차를 따라 설정한다.  
1. Open OpenSearch Dashboards. You can find a link to OpenSearch Dashboards in the domain summary of your OpenSearch Service console.
2. From the navigation pane, choose Security.
3. Choose Roles.
4. Choose the all_access role.
5. Choose the Mapped users tab.
6. On the Mapped users dialog page, choose Manage mapping.
7. Under Backend roles, enter the Lambda function execute role ARN.
8. Choose Map. Your logs should now stream to your OpenSearch Service domain.

<br><br>

## 3. Cloudwatch -> OpenSearch로 데이터 전송
Cloudwatch Log Groups -> Subscription filters -> Create Amazon OpenSearch Service subscription filter 생성
해당 설정이 완료되면 Lambda Function이 자동으로 생성되며 Cloudwatch 에서 Lambda를 통해 OpenSearch로 데이터가 들어간다.  

Lambda Function의 소스를 보면 데이터 Index를 다음과 같이 보낸다. (cwl-*)  
CloudWatch 로그 그룹을 동일한 Amazon OpenSearch 도메인으로 스트리밍하기 위해서는 아래와 같이 indexName을 그룹별로 보낼 수 있도록 설정한다.  
```javascript
var logGroupName = payload.logGroup.toLowerCase().split('/').join('-');
logGroupName = logGroupName.split('.').join('-');
console.log('logGroupName2: ' + logGroupName);
var indexName = [
            'cwl' + logGroupName + '-' +
                 + timestamp.getUTCFullYear(),              // year
            ('0' + (timestamp.getUTCMonth() + 1)).slice(-2),  // month
            ('0' + timestamp.getUTCDate()).slice(-2)          // day
        ].join('.');
```

logGroupName을 두 번에 걸쳐 split하는 이유는 Fluent-bit에서 '/'를 지원하지 않기 때문에 아래와 같은 패턴을 사용했기 때문이다.  

```ruby
log_group_template  /aws/containerinsights/${CLUSTER_NAME}.$kubernetes['namespace_name'].$kubernetes['container_name']
```

최종적으로 indexName은 아래와 같은 패턴으로 넘어오게 된다.  
```txt
cwl-aws-containerinsights-bys-dev-eks-main-aws-awssdk-iam-dev-2023.02.22
```

post API를 통해 전송 시 아래 bys-FailedItems 부분에 failedItems 항목을 출력하여 오류시 오류를 확인할 수 있도록 한다.  
```javascript
// post documents to the Amazon Elasticsearch Service
post(elasticsearchBulkData, function(error, success, statusCode, failedItems) {
    console.log('bys-FailedItems: ' + JSON.stringify(failedItems));
    console.log('Response: ' + JSON.stringify({
        "statusCode": statusCode
    }));

    if (error) {
        logFailure(error, failedItems);
        context.fail(JSON.stringify(error));
    } else {
        console.log('Success: ' + JSON.stringify(success));
        context.succeed('Success');
    }
});
```

<br><br>

## 3. OpenSearch Index 관리

`Index 등록`  
- Stack management -> Index patterns -> Create index pattern  

Create Index Patterns를 통해 등록할 때 Index는 아래와 같은 패턴에 날짜를 붙여 넘어오기 때문에 *를 붙여 등록한다.  
```bash
# Index Patterns
cwl-aws-containerinsights-bys-dev-eks-main-aws-awssdk-iam-dev*

# Index Samples
cwl-aws-containerinsights-bys-dev-eks-main-aws-awssdk-iam-dev-2023.02.22
cwl-aws-containerinsights-bys-dev-eks-main-aws-awssdk-iam-dev-2023.02.23
cwl-aws-containerinsights-bys-dev-eks-main-aws-awssdk-iam-dev-2023.02.24
```


`Index 관리정책`  
- Index management -> Create Policy

Kibana Index가 지속적으로 쌓임에 따라 관리의 필요성이 생겨 Policy를 적용하여 관리를 하였다.  
아래와 같이 hot-warm-delete 정책을 만들어 관리한다. 3일이 지난 Index의 경우 hot-warm으로 보내는 정책을 만들고 15일이 지난 index의 경우 delete가 된다. 이 때 url로 알람을 보낸다.  
Index가 생성되는 패턴은 IndexName-yyyy.mm.dd 패턴이었기 때문에, ism_template으로 index_patterns를 잡아주면 신규로 생성되는 index에도 해당 정책이 바로 적용이 된다.  
`policy`
```json
{
  "policy": {
    "description": "hot warm delete workflow",
    "default_state": "hot",
    "schema_version": 1,
    "states": [
      {
        "name": "hot",
        "actions": [
          {
            "rollover": {
              "min_index_age": "3d",
              "min_primary_shard_size": "30gb"
            }
          }
        ],
        "transitions": [
          {
            "state_name": "warm"
          }
        ]
      },
      {
        "name": "warm",
        "actions": [
          {
            "replica_count": {
              "number_of_replicas": 5
            }
          }
        ],
        "transitions": [
          {
            "state_name": "delete",
            "conditions": {
              "min_index_age": "15d"
            }
          }
        ]
      },
      {
        "name": "delete",
        "actions": [
          {
            "notification": {
              "destination": {
                "chime": {
                  "url": "https://app.chime.aws/conversations/new?email=bys@test.com"
                }
              },
              "message_template": {
                "source": "The index {{ctx.index}} is being deleted"
              }
            }
          },
          {
            "delete": {}
          }
        ]
      }
    ],
    "ism_template": {
      "index_patterns": ["cwl*"],
      "priority": 100
    }
  }
}
```

<br><br>

## 10. [Trouble Shooting](https://aws.amazon.com/premiumsupport/knowledge-center/opensearch-troubleshoot-cloudwatch-logs/)

### 1. Lambda Function 권한 문제
Cloudwatch로그 그룹에서 OpenSearch Service subscription filter를 통해 생성한 Lambda Function에서 OpenSearch로 로그 스트리밍을 전송할 때 권한 문제가 발생하였다. (Cloudwatch LogGroups에서 확인)  
`Error Message`  
```bash
ERROR	Invoke Error 	
{
    "errorType": "Error",
    "errorMessage": "{\"statusCode\":403,\"responseBody\":{\"error\":{\"root_cause\":[{\"type\":\"security_exception\",\"reason\":\"no permissions for [indices:data/write/bulk] and User [name=arn:aws:iam::558846430793:role/AmazonLambdaOpensearchRole, backend_roles=[arn:aws:iam::558846430793:role/AmazonLambdaOpensearchRole], requestedTenant=null]\"}],\"type\":\"security_exception\",\"reason\":\"no permissions for [indices:data/write/bulk] and User [name=arn:aws:iam::558846430793:role/AmazonLambdaOpensearchRole, backend_roles=[arn:aws:iam::558846430793:role/AmazonLambdaOpensearchRole], requestedTenant=null]\"},\"status\":403}}",
    "stack": [
        "Error: {\"statusCode\":403,\"responseBody\":{\"error\":{\"root_cause\":[{\"type\":\"security_exception\",\"reason\":\"no permissions for [indices:data/write/bulk] and User [name=arn:aws:iam::558846430793:role/AmazonLambdaOpensearchRole, backend_roles=[arn:aws:iam::558846430793:role/AmazonLambdaOpensearchRole], requestedTenant=null]\"}],\"type\":\"security_exception\",\"reason\":\"no permissions for [indices:data/write/bulk] and User [name=arn:aws:iam::558846430793:role/AmazonLambdaOpensearchRole, backend_roles=[arn:aws:iam::558846430793:role/AmazonLambdaOpensearchRole], requestedTenant=null]\"},\"status\":403}}",
        "    at intoError (file:///var/runtime/index.mjs:46:16)",
        "    at postError (file:///var/runtime/index.mjs:711:51)",
        "    at done (file:///var/runtime/index.mjs:743:11)",
        "    at fail (file:///var/runtime/index.mjs:755:11)",
        "    at Object.fail (file:///var/runtime/index.mjs:782:20)",
        "    at /var/task/index.js:43:25",
        "    at IncomingMessage.<anonymous> (/var/task/index.js:177:13)",
        "    at IncomingMessage.emit (node:events:539:35)",
        "    at endReadableNT (node:internal/streams/readable:1345:12)",
        "    at processTicksAndRejections (node:internal/process/task_queues:83:21)"
    ]
}
```

fine-grained access control 설정이 되어있는 상황에서 아래의 절차를 따라 설정한다.  
1. Open OpenSearch Dashboards. You can find a link to OpenSearch Dashboards in the domain summary of your OpenSearch Service console.
2. From the navigation pane, choose Security.
3. Choose Roles.
4. Choose the all_access role.
5. Choose the Mapped users tab.
6. On the Mapped users dialog page, choose Manage mapping.
7. Under Backend roles, enter the Lambda function execute role ARN.
8. Choose Map. Your logs should now stream to your OpenSearch Service domain.

<br>

### 2. Lambda Function 로그 설정
아래 bys-FailedItems 부분에 failedItems 항목을 출력하여 오류시 오류를 확인할 수 있도록 한다. 이렇게 하면 OpenSearch로 400오류 같은 코드와 함께 로그 전송이 실패했을 경우 상세 내용을 살펴볼 수 있다.  
```javascript
// post documents to the Amazon Elasticsearch Service
post(elasticsearchBulkData, function(error, success, statusCode, failedItems) {
    console.log('bys-FailedItems: ' + JSON.stringify(failedItems));
    console.log('Response: ' + JSON.stringify({
        "statusCode": statusCode
    }));

    if (error) {
        logFailure(error, failedItems);
        context.fail(JSON.stringify(error));
    } else {
        console.log('Success: ' + JSON.stringify(success));
        context.succeed('Success');
    }
});
```

<br>

### 3. OpenSearch Index name 
reateLogStream API responded with error='InvalidParameterException', message='1 validation error detected: Value '/aws/containerinsights/bys-dev-eks-main/$(kubernetes['namespace_name'])/$(kubernetes['pod_name'])' at 'logGroupName' failed to satisfy constraint: Member must satisfy regular expression pattern: [\\.\\-_/#A-Za-z0-9]+'

Index를 분리하기 위해 Cloudwatch log group을 다르게 설정하였다.
```ruby
[OUTPUT]
    Name                cloudwatch_logs
    Match               application.*
    region              ${AWS_REGION}
    log_group_name      /aws/containerinsights/${CLUSTER_NAME}
    log_group_template  /aws/containerinsights/${CLUSTER_NAME}.$kubernetes['namespace_name'].$kubernetes['container_name']
    log_stream_prefix   ${HOST_NAME}-
    auto_create_group   true
    extra_user_agent    container-insights
```

또는 Application 그룹 밑으로 넣고 싶은 경우는 아래 처럼 수정한다.  
```ruby
[OUTPUT]
    Name                cloudwatch_logs
    Match               application.*
    region              ${AWS_REGION}
    log_group_name      /aws/containerinsights/${CLUSTER_NAME}/application
    log_stream_prefix   ${CLUSTER_NAME}.$kubernetes['namespace_name'].$kubernetes['container_name']
    auto_create_group   true
    extra_user_agent    container-insights
```

Kubernetes 메타데이터는 [문서](https://docs.fluentbit.io/manual/pipeline/outputs/cloudwatch#log-stream-and-group-name-templating-using-record_accessor-syntax) 참고.

<br>

### 4. IMDS 문제 
FluentBit에서 로그를 전송하지 못하고 있어 로그를 확인해보니 아래와 같은 오류들이 찍히고 있었다. 
```bash
[filter:aws:aws.3] Could not retrieve ec2 metadata from IMDS
```

fluent-bit.yaml을 살펴보면 imds_version이 v1으로 설정되어있다.  
Container image는 IMDS v2를 사용하기 위해 설정되어있으나 conf 수정이 되지 않은 것으로 보인다. 자세한 내용은 Github Issue를 확인하고 수정방법은 v1 -> v2로 변경한다.  
```ruby
[FILTER]
    Name                aws
    Match               dataplane.*
    imds_version        v1
[FILTER]
    Name                aws
    Match               host.*
    imds_version        v1
```

참고 - [Github Issue](https://github.com/fluent/fluent-bit/issues/2840#issuecomment-1296177358)




<br><br><br>

- References   
[1] [Set up Fluent Bit as a DaemonSet to send logs to CloudWatch Logs](https://docs.aws.amazon.com/ko_kr/AmazonCloudWatch/latest/monitoring/Container-Insights-setup-logs-FluentBit.html)  
[2] [Fine-grained access control in Amazon OpenSearch Service](https://docs.amazonaws.cn/en_us/opensearch-service/latest/developerguide/fgac.html#fgac-enabling)  
[3] [How do I troubleshoot CloudWatch Logs so that it streams to my Amazon OpenSearch Service domain?](https://aws.amazon.com/premiumsupport/knowledge-center/opensearch-troubleshoot-cloudwatch-logs/?nc1=h_ls)  
[4] [Templating Log Group and Stream Names based on Kubernetes metadata](https://github.com/aws/amazon-cloudwatch-logs-for-fluent-bit#templating-log-group-and-stream-names-based-on-kubernetes-metadata)  
[5] [Opensearch Example policy](https://opensearch.org/docs/latest/im-plugin/ism/policies/#delete)  