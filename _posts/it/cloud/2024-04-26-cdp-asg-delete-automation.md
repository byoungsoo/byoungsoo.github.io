---
layout: post
title: "EC2 Blue/Green 배포 실패 시 생성된 ASG 자동 삭제 방법"
author: "Bys"
category: cloud
date: 2024-04-25 01:00:00
tags: codedeploy deployment
---

### - Architecture

![cdp-asg-architecture](/assets/it/cloud/codeseries/cdp-asg-architecture.png){: width="60%" height="auto"}  

1. 배포 생성
2. CodeDeploy 서비스에 의해 ASG 그룹 복제
3. 배포 실패
4. CodeDeploy 서비스에서 DeploymentFailure 이벤트에 수신 후, SNS topic 트리거
5. SNS 서비스를 통해 Lambda function invoke

위 과정을 통해 자동삭제를 지원한다.  


### - Demo
#### 1. Create SNS Topic. 
1. Move to menu (Amazon SNS -> Topics -> Create topic)
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
  - Runtime: `Python` (It depends on users)
  - Architecture: `x86_64` (It depends on users)
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
Deploy a new deployment using EC2 Autoscaling Group deployment group and make it fails. Then, CodeDeploy trigger SNS and Lambda function subscribing SNS works.
Now, you can see the ASG created by CodeDeploy as a part of failure is removed automatically.

The Lambda code is sample source code. You can develop on your own language or change. 