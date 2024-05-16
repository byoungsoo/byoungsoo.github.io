---
layout: post
title: "CodeDeploy EC2 Autoscaling 배포 후, 배포 실패시 존재하는 ASG 자동으로 제거하기"
author: "Bys"
category: incubator
date: 2025-05-15 01:00:00
tags: incubator
---

# [CodeDeploy](https://docs.aws.amazon.com/ko_kr/codedeploy/latest/userguide/integrations-aws-auto-scaling.html#integrations-aws-auto-scaling-deploy)  
CodeDeploy를 이용하여 EC2/On-premises에 블루/그린 배포 타입으로 'Automatically copy Amazon EC2 Auto Scaling group' 옵션을 사용하여 배포를 진행할 때, 만약 배포가 실패하면 복제된 오토스케일링 그룹은 삭제가 되지 않고 남아 있게된다. 현재는 이런 리소스를 사용자가 직접 수작업으로 삭제를 해줘야 하는 불편함이 있으므로 CodeDeploy 트리거 설정과, Amazon SNS, AWS Lambda 서비스를 이용하여 자동으로 삭제되는 아키텍처를 구성해보도록 한다. 

## Architecture

![cdp-asg-architecture](/assets/it/cloud/codeseries/cdp-asg-architecture.png){: width="70%" height="auto"}  

아키텍처는 위 와 같다. CodeDeploy 서비스에서 배포 실패 또는 배포 중지가 발생하면, SNS 토픽을 트리거하고, SNS 토픽을 구독하는 Lambda 함수가 실행되어 배포 실패로 남아 있는 ASG를 삭제한다. Lambda는 Python runtime을 사용하고, boto3를 통해 ASG를 삭제하는 아키텍처의 간략한 그림이다.  

단계별로 진행되는 내용은 아래와 같다.  
#### 1. Create SNS Topic. 
1. Move to SNS (Amazon SNS -> Topics -> Create topic)
2. Input
  - Type: `Standard`
  - Name: `CodeDeploy_CDP_BG_FailedTopic`
3. Create Topic


#### 2. Configure CodeDeploy Trigger
1. Move to CodeDeploy (CodeDeploy > Applications > Click <application-name> > Click <deployment-group-name> > Edit > Advanced > Triggers > Create trigger)
2. Input
  - Name: `Trigger-CDP-BG-failed`
  - Events: `DeploymentFailure, DeploymentStop`
  - Amazon SNS topics: choose `CodeDeploy_CDP_BG_FailedTopic` created in step one.
3. Save changes


#### 3. Create AWS Lambda function and IAM Role
1. Move to Lambda (Lambda > Functions > Create function)
2. Input
  - Function name: `CodeDeployDeploymentHandler`
  - Runtime: `Python` runtime
  - Architecture: `x86_64`
3. Create function

4. Move to IAM (IAM > Roles > Search 'CodeDeployDeploymentHandler-role' > Click CodeDeployDeploymentHandler-role-<id> > Permission tab > Add permission > Attach policies)
5. Input
  - Search: `AutoScalingFullAccess`
6. Add permissions


#### 4. Create subscription in SNS topic
1. Move to subscription (Amazon SNS > Topics > CodeDeploy_CDP_BG_FailedTopic(Created in step one) > Subscriptions tab > Create subscription)
2. Input
  - Protocol: `AWS Lambda`
  - Endpoint: `arn:aws:lambda:ap-northeast-2:111122223333:function:CodeDeployDeploymentHandler` (ARN of Lambda function created in step three)
3. Create subscription


#### 5. Modify AWS Lambda code
1. Move to Lambda (Lambda > Functions > CodeDeployDeploymentHandler)
2. Input
  - Code source: Copy and paste below source code at 'lambda_function.py' and click the button 'Deploy'
```python
import json
import boto3


def lambda_handler(event, context):
    # TODO implement
    message = event['Records'][0]['Sns']['Message']

    # Print the message content for debugging purposes (optional)
    print(f"Received SNS message: {message}")

    # Parse the JSON message (if applicable)
    try:
        sns_data = json.loads(message)
    except json.JSONDecodeError:
        sns_data = 'JSONDecodeError Occurred'  # Handle non-JSON messages

    print(f"Processing message data: {sns_data}")
    deploymentGroupName = sns_data['deploymentGroupName']
    deploymentId = sns_data['deploymentId']

    print(f"deploymentGroupName: {deploymentGroupName}")
    print(f"deploymentId: {deploymentId}")

    response = delete_failed_asg(deploymentGroupName, deploymentId)
    return response


def delete_failed_asg(deploymentGroupName, deploymentId):
    asg_client = boto3.client('autoscaling')
    asg_name = "CodeDeploy_" + deploymentGroupName + "_" + deploymentId

    print(f"asg_name: {asg_name}")

    response = asg_client.delete_auto_scaling_group(
        AutoScalingGroupName=asg_name,
        ForceDelete=True
    )
    return response
```


#### 6. Test
배포가 실패되는 신규 배포를 생성한다. 만약, 배포가 실패하면 바로 SNS 토픽이 트리거 되며 이를 구독하는 Lambda가 실행된다. 정상적으로 배포 실패로 남아 있는 ASG가 삭제되는 것을 확인할 수 있다. 

<br><br><br>

- References  
[1] CodeDeploy Trigger - https://docs.aws.amazon.com/codedeploy/latest/userguide/monitoring-sns-event-notifications-create-trigger.html
[2] AWS Lambda with Amazon SNS - https://docs.aws.amazon.com/lambda/latest/dg/with-sns.html
[3] AWS Lambda with Amazon SNS - https://docs.aws.amazon.com/ko_kr/lambda/latest/dg/with-sns-example.html