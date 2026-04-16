---
slug: eks-cloudwatch-monitoring
author: Bys
categories:
- cloud
category: cloud
date: '2026-03-10 01:00:00'
draft: true
keywords: eks alarm sms
tags:
- eks
- alarm
- sms
title: CloudWatch 통한 모니터링 환경 구축
---



# [CloudWatch Container Insights](https://docs.aws.amazon.com/ko_kr/AmazonCloudWatch/latest/monitoring/Container-Insights-metrics-enhanced-EKS.html)  
EKS 에서 컨테이너 메트릭 수집 및 로깅 기능을 제공한다.  
Container Insights 를 EKS Addon 으로 설치하면 `FluentBit, CloudWatch Agent` 를 통해 애플리케이션 로그와 시스템 로그, 성능 로그 등을 추출하여 CloudWatch 로 전송한다. 


## 1. Architecture
![cloudwatch001.gif](/assets/it/cloud/monitoring/cloudwatch001.gif)

Container Insights 를 통해 수집된 EKS 로그와 메트릭을 통해 모니터링 환경을 구성할 수 있으며 CloudWatch 서비스와 통합되기 때문에 AWS Native 한 모니터링 환경 구축이 가능하다. 아키텍처는 큰 틀 안에서 서비스 연계에 따라 매우 다양하게 그려질 수 있다.     

1. Container Insights 를 설치하면 EKS 클러스터내 로그와 메트릭이 수집된다.  
2. 수집 된 메트릭을 통해 CloudWatch Alarm을 설정할 수 있으며 아키텍처는 SNS, Lambda 등이 활용될 수 있다.  
3. 수집 된 로그를 OpenSearch로 전송하여 로그 검색 및 분석, 시각화 대시보드, 알림 등을 구성할 수 있다.  


#### [EMF](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/CloudWatch_Embedded_Metric_Format_Specification.html)  
참고로 CloudWatch 서비스에서는 EMF(embedded metric format)라는 JSON 명세의 포맷이 존재한다. CloudWatch 임베디드 메트릭 형식은 구조화된 로그 이벤트에 포함된 메트릭 값을 CloudWatch Logs가 자동으로 추출하도록 지시하는 데 사용되는 JSON 사양이며 추출된 값은 CloudWatch Metrics 에서 지표로 사용 가능하다.  
아래 예시에시와 같은 포맷으로 Logs 가 전달되면 자동으로 메트릭 값을 추출하여 활용이 가능하다.  
```
{
    "_aws": {
        "CloudWatchMetrics": [
            {
                "Metrics": [
                    {"Name": "RequestLatency", "Unit": "Milliseconds"}
                ],
                "Namespace": "MyApplication"
            }
        ]
    },
    "Service": "PaymentService",
    "Environment": "Production",
    "K8s.Cluster": "main-cluster",
    "K8s.Namespace": "payment-ns",
    "K8s.Pod": "payment-pod-123",
    "K8s.Node": "worker-node-1",
    "K8s.Workload": "payment-deployment",
    "RequestLatency": 135.5,
    "timestamp": 1622163600000
}
```
Container Insights는 임베디드 메트릭 포맷 형태로 로그를 전달하기 때문에 자동으로 메트릭을 확인할 수 있다.  



## 2. SNS 연동
CloudWatch Metrics action을 통해 바로 Lambda 함수를 호출할 수도 있지만 SNS를 통해 거쳐가면 수신자에 대한 구독관리가 용이하므로 SNS 서비스를 두었다. SNS 서비스는 Email, HTTPS, Lambda 와 같이 다른 서비스로 Notification이 가능하기 때문에 아키텍처의 그림과 같이 구독을 통해 설정했다.  
1. Email 
2. Slack
   - HTTPS 설정 - https://global.sns-api.chatbot.amazonaws.com
   - Lambda webhook
3. Lambda

SNS 서비스의 경우 구독 프로토콜에 따라 SNS를 분리할 수도 있고, 수신자에 따라 SNS를 분리할 수도 있다.  


## 3. Lambda 포맷
SNS서비스를 통해 Lambda 함수를 트리거 하게 될 경우 [Amazon SNS 알림 스키마](https://docs.aws.amazon.com/ko_kr/AmazonCloudWatch/latest/monitoring/Notify_Users_Alarm_Changes.html#alarm-sns-schema)에 따라 메세지 포맷이 전송된다.  

람다에서는 아래와 같은 포맷을 통해 데이터를 가져올 수 있다.

```python
raw_message=event["Records"][0]["Sns"]["Message"]
alarm=json.loads(raw_message)
```


## 4. SMS 문자발송
SMS 문자 발송을 위해서는 람다함수에서 SMS 서비스가 가능한 리전(ex. ap-northeast-1, 도쿄리전)을 통해 sns.publish 를 트리거 해야 한다. sns.publish 에서 phone 파라미터를 통해 직접 전송할 수도 있지만 수신자 관리를 위해 Topic 파라미터를 전달한다.  

`예시코드`  
```python
def lambda_handler(event, context):
    print("Received event:", json.dumps(event))

    for record in event.get("Records", []):
        raw_message = record["Sns"]["Message"]

        try:
            alarm = json.loads(raw_message)
            alarm_name = alarm.get("AlarmName", "N/A")
            new_state = alarm.get("NewStateValue", "N/A")
            old_state = alarm.get("OldStateValue", "N/A")
            state_change_time = alarm.get("StateChangeTime", "")

            # UTC -> KST 변환
            utc_time = datetime.strptime(state_change_time, "%Y-%m-%dT%H:%M:%S.%f%z")
            kst_time = utc_time.astimezone(KST).strftime("%Y-%m-%d %H:%M KST")

            sms_body = (
                f"[{new_state}]\n"
                f"Name: {alarm_name}\n"
                f"Time: {kst_time}"
            )
        except (json.JSONDecodeError, KeyError, ValueError):
            sms_body = f"[CW ALARM]\n{raw_message[:160]}"

        print(f"Sending SMS via Topic {TOPIC_ARN} in {SMS_REGION}: {sms_body}")

        sns = boto3.client("sns", region_name=SMS_REGION)
        response = sns.publish(
            TopicArn=TOPIC_ARN,
            Message=sms_body,
            MessageAttributes={
                "AWS.SNS.SMS.SMSType": {
                    "DataType": "String",
                    "StringValue": "Transactional",
                }
            },
        )
        print("SNS publish response:", response)

    return {"statusCode": 200, "body": "SMS sent"}
```

## 5. Container Insights 추가 지표 수집 

Container Insights(CloudWatch Agent)를 통해 ENA(Elastic Network Adapter) 네트워크 성능 메트릭을 추가 수집할 수 있으며, 이를 통해 인스턴스의 네트워크 대역폭/PPS/연결 추적 등의 허용량 초과 여부를 모니터링할 수 있다.

#### [수집 가능한 ENA 메트릭](https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/install-CloudWatch-Observability-EKS-addon.html#CloudWatch-Observability-EKS-addon-CustomAgentConfig)  

| 메트릭 | 설명 |
|--------|------|
| `bw_in_allowance_exceeded` | 인바운드 대역폭 허용량 초과로 큐잉/드롭된 패킷 수 |
| `bw_out_allowance_exceeded` | 아웃바운드 대역폭 허용량 초과로 큐잉/드롭된 패킷 수 |
| `conntrack_allowance_exceeded` | 연결 추적 허용량 초과로 큐잉/드롭된 패킷 수 |
| `linklocal_allowance_exceeded` | 로컬 프록시 서비스(DNS, IMDS 등) PPS 초과로 드롭된 패킷 수 |
| `pps_allowance_exceeded` | 양방향 PPS 허용량 초과로 큐잉/드롭된 패킷 수 |

> CloudWatch에 수집 시 메트릭 이름 앞에 `ethtool_` 접두사가 붙는다. (예: `ethtool_bw_in_allowance_exceeded`)

EKS 관리형 Add-on(`amazon-cloudwatch-observability`)으로 Container Insights를 설치한 경우, `--configuration-values` 옵션을 통해 CloudWatch Agent 설정을 커스터마이징해야 한다.
기본 설정(Container Insights + Application Signals)을 유지하면서 `ethtool` 수집을 추가하는 설정:

```json
{
  "manager": {
    "applicationSignals": {
      "autoMonitor": {
        "monitorAllServices": false,
        "restartPods": true,
        "languages": [],
        "exclude": {
          "java": { "namespaces": [], "deployments": [], "daemonsets": [], "statefulsets": [] },
          "python": { "namespaces": [], "deployments": [], "daemonsets": [], "statefulsets": [] },
          "dotnet": { "namespaces": [], "deployments": [], "daemonsets": [], "statefulsets": [] },
          "nodejs": { "namespaces": [], "deployments": [], "daemonsets": [], "statefulsets": [] }
        },
        "customSelector": {
          "java": { "namespaces": [], "deployments": ["default/eunho-invitation"], "daemonsets": [], "statefulsets": [] },
          "python": { "namespaces": [], "deployments": ["default/eunho-invitation"], "daemonsets": [], "statefulsets": [] },
          "dotnet": { "namespaces": [], "deployments": [], "daemonsets": [], "statefulsets": [] },
          "nodejs": { "namespaces": [], "deployments": ["default/eunho-invitation"], "daemonsets": [], "statefulsets": [] }
        }
      }
    }
  },
  "agent": {
    "config": {
      "metrics": {
        "metrics_collected": {
          "ethtool": {
            "interface_include": [
              "eth*"
            ],
            "metrics_include": [
              "bw_in_allowance_exceeded",
              "bw_out_allowance_exceeded",
              "conntrack_allowance_exceeded",
              "linklocal_allowance_exceeded",
              "pps_allowance_exceeded"
            ]
          }
        }
      },
      "logs": {
        "metrics_collected": {
          "application_signals": {},
          "kubernetes": {
            "enhanced_container_insights": true
          }
        }
      },
      "traces": {
        "traces_collected": {
          "application_signals": {}
        }
      }
    }
  },
  "containerLogs": {
    "fluentBit": {
      "resources": {
         "requests": {
          "cpu": "100m",
          "memory": "300Mi"
        },
        "limits": {
          "cpu": "500m",
          "memory": "500Mi"
        }
      }
    }
  }
}
```
커스텀 `agent.config`를 지정하면 기본 설정이 **덮어쓰기** 된다. 따라서 `logs`(Container Insights, Application Signals)와 `traces` 설정을 반드시 함께 포함해야 기존 기능이 유지된다.

특정 네트워크 인터페이스만 모니터링하려면 `interface_include`, `interface_exclude` 옵션을 추가할 수 있다.  
```json
"ethtool": {
  "interface_include": ["eth*"],
  "interface_exclude": ["eth1"],
  "metrics_include": ["bw_in_allowance_exceeded", "..."]
}
```  


---

## 📚 References
[1] 경보 변경 시 사용자에게 알림 - 경보의 상태 변경 시 Amazon SNS 알림 스키마  
https://docs.aws.amazon.com/ko_kr/AmazonCloudWatch/latest/monitoring/Notify_Users_Alarm_Changes.html#alarm-sns-schema

[2] Install CloudWatch agent with EKS add-on - Custom agent configuration  
https://docs.aws.amazon.com/AmazonCloudWatch/latest/monitoring/install-CloudWatch-Observability-EKS-addon.html#CloudWatch-Observability-EKS-addon-CustomAgentConfig
