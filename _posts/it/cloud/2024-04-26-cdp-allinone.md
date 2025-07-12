---
layout: post
title: "CodeDeploy 배포와 관련된 모든 것"
author: "Bys"
category: cloud
date: 2024-04-26 01:00:00
tags: codedeploy deployment
---

# CodeDeploy

CodeDeploy에서는 AppSpec에 [CodeDeploy Hook](https://docs.aws.amazon.com/codedeploy/latest/userguide/reference-appspec-file-structure-hooks.html)을 정의할 수 있으며 배포 방식에 따라 내용이 조금 씩 달라진다. 먼저 Codedeploy agent는 반드시 설치가 되어 있어야 하며 아래 커맨드로 확인 가능하다.  

```bash
$ sudo service codedeploy-agent status
```

[`Codedeploy agent configuration`](https://docs.aws.amazon.com/ko_kr/codedeploy/latest/userguide/codedeploy-agent.html)
```bash
# Codedeploy agent config
/etc/codedeploy-agent/conf

# Codedeploy agent logs
/var/log/aws/codedeploy-agent/
# - Daily rotate, Retention: 7days. 
# - log.rb - https://github.com/aws/aws-codedeploy-agent/blob/3ba4279a7ac66df4f288b58dd4d5d9b7a6cb0bc8/vendor/gems/process_manager-0.0.13/lib/process_manager/log.rb#L26


# Files installed by the CodeDeploy agent
/opt/codedeploy-agent/deployment-root/<deployment-id>/
```

각 배포 방식에서 어떻게 동작하는지는 아래에서 상세히 살펴본다.  

## 1. [EC2 - In place 배포](https://docs.aws.amazon.com/codedeploy/latest/userguide/reference-appspec-file-structure-hooks.html#:~:text=in%20this%20diagram.-,In%2Dplace%20deployments,-In%20an%20in)  

#### 구성
In-place 배포란 현재 EC2 서버에서 배포를 진행하는 방식을 의미한다. 먼저 다음과 같이 진행을 한다. 

1. Amazon Linux 3 기반 Launch Template을 생성

    `Userdata`
    ```bash
    #!/bin/bash
    install_package(){
      max_attempts=5
      attempt_num=1
      success=false
      while [ $success = false ] && [ $attempt_num -le $max_attempts ]; do
        echo "Trying yum install"

        yum -y install $1;
        # Check the exit code of the command
      
        if [ $? -eq 0 ]; then
          echo "Yum install succeeded"
          success=true
        else
          echo "Attempt $attempt_num failed. Sleeping for 3 seconds and trying again..."
          sleep 3
          ((attempt_num++))
        fi
      done
    }
    yum -y update;

    instasll_package ruby
    instasll_package wget
    instasll_package java-11-amazon-corretto.x86_64

    wget https://aws-codedeploy-ap-northeast-2.s3.ap-northeast-2.amazonaws.com/latest/install;
    chmod +x ./install;
    ./install auto;

    service codedeploy-agent start;
    ```
    Userdata 패키지 프로그램 설치 시 Retry 로직이 포함 됨. 자세한 내용은 아래 트러블 슈팅 참고. 

2. 코드 구성

    `appspec.yml`
    ```bash
    version: 0.0
    os: linux
    files:
      - source: awssdk-iam.jar
        destination: /home/ec2-user
    hooks:
      BeforeInstall:
        - location: beforeInstall.sh
      AfterInstall:
        - location: afterInstall.sh
          timeout: 180
      ApplicationStop:
        - location: stop.sh
          timeout: 3600
          runas: root
      ApplicationStart:
        - location: start.sh
          timeout: 3600
          runas: root
      ValidateService:
        - location: status.sh
          timeout: 3600
          runas: root
    ```

    `awssdk-iam.jar` 구성 파일
    ```
    rm -rf awssdk-iam.tar
    tar -cvf awssdk-iam.tar appspec.yml awssdk-iam.jar afterInstall.sh beforeInstall.sh status.sh start.sh stop.sh
    aws s3 cp awssdk-iam.tar s3://bys-dev-s3-temp/
    ```

    `script` 파일
    ```bash
    # start.sh
    sudo nohup java -XX:TieredStopAtLevel=1 -noverify -Dspring.profiles.active=local -Dspring.output.ansi.enabled=always -Dcom.sun.management.jmxremote -Dspring.jmx.enabled=true -Dspring.liveBeansView.mbeanDomain -Dspring.application.admin.enabled=true -Dfile.encoding=UTF-8 -jar /home/ec2-user/awssdk-iam.jar > /dev/null 2>&1 &

    # stop.sh
    ps -ef | grep java | grep -v grep | awk '{print $2}' | xargs kill -9

    # status.sh
    ps -ef | grep java | grep -v grep | wc -l

    # beforeInstall.sh
    rm -rf /home/ec2-user/beforeInstall.txt
    echo `date` >> /home/ec2-user/beforeInstall.txt

    # afterInstall.sh
    rm -rf /home/ec2-user/afterInstall.txt
    echo `date`  >> /home/ec2-user/afterInstall.txt
    ```

3. Autoscaling Group 생성 
4. 배포그룹 생성
  - In-place 배포 생성 (옵션: AutoscalingGroup(ASG), EC2, On-prem)

<br>

#### 동작방식
[Codedeploy agent동작](https://docs.aws.amazon.com/codedeploy/latest/userguide/codedeploy-agent.html#codedeploy-agent-outbound-port)은 폴링 방식을 통해 Codedeploy 서비스를 호출하기 때문에 EC2 인스턴스의 443 포트 아웃바운드 통신이 가능해야 한다. 또한 Codedeploy agent가 EC2 인스턴스에서 동작할 때는 EC2 메타데이터 정보를 활용하기 때문에 IMDS 접근이 가능해야한다.  


1. **일반적인 배포**  
Codedeploy agent는 폴링 방식을 통해 Codedeploy 서비스를 확인하다 배포가 있을 경우, [In-place deployments](https://docs.aws.amazon.com/codedeploy/latest/userguide/reference-appspec-file-structure-hooks.html#:~:text=in%20this%20diagram.-,In%2Dplace%20deployments,-In%20an%20in) 라이프사이클 훅에 따라 동작한다.  

    ![ec2_in_place_lifecycle](/assets/it/cloud/codeseries/ec2_in_place_lifecycle.png){: width="40%" height="auto"}  

   - BlockTraffic
     - BlockTraffic 단계에서는 타겟그룹에 타겟을 제거(DeregisterTargets API)하며 타겟그룹의 deregistration.delay 속성 값에 영향을 받는다.  
     - 만약, 타겟그룹 최초 생성시 설정한 포트와 트래픽 포트가 다른 경우 Draining이 되지 않는 문제가 발생할 수 있다.  
   - AllowTraffic
     - AllowTraffic 단계에서는 타겟그룹에 타겟을 등록(RegisterTargets API)하며 CodeDeploy는 ELB의 헬스체크를 확인한다. 만약, ELB의 타겟 헬스체크가 실패하면 **1시간** 후 Timeout이 발생한다.  


2. **Scale-out 동작 시**  
CodeDeploy 서비스를 통해 In-place 생성 시 옵션을 ASG로 했다면, ASG의 Lifecycle hooks에는 다음과 같은 훅이 자동으로 추가된다.  

![asg_lifecycle_1](/assets/it/cloud/codeseries/asg_lifecycle_1.png){: width="60%" height="auto"}  

CodeDeploy-managed-automatic-launch-deployment-hook-inplace 훅은 아래 ASG 라이프사이클 단계에서 Launch가 시작되고 Pending: Wait 단계에서 훅이 시작된다. 이 단계는 CodeDeploy의 배포가 시작되고 해당 배포를 진행하는 단계이다. 즉, CodeDeploy 서비스를 통해 관리되는 ASG의 경우 노드가 Scale-out 되는 경우 새로운 Application이 기동 될 수 있도록 배포 단계가 실행되어야 하는데 그 단계를 ASG의 라이프사이클 훅을 통해 진행해준다.  

![asg_lifecycle](/assets/it/cloud/codeseries/asg_lifecycle.png){: width="60%" height="auto"}  

<br><br>


## 2. [EC2 - Blue/Green 배포](https://docs.aws.amazon.com/codedeploy/latest/userguide/reference-appspec-file-structure-hooks.html#:~:text=in%20this%20diagram.-,In%2Dplace%20deployments,-In%20an%20in)  

#### 동작방식
[Codedeploy agent동작](https://docs.aws.amazon.com/codedeploy/latest/userguide/codedeploy-agent.html#codedeploy-agent-outbound-port)은 폴링 방식을 통해 Codedeploy 서비스를 호출하기 때문에 EC2 인스턴스의 443 포트 아웃바운드 통신이 가능해야 한다. 또한 Codedeploy agent가 EC2 인스턴스에서 동작할 때는 EC2 메타데이터 정보를 활용하기 때문에 IMDS 접근이 가능해야한다.  


1. **일반적인 배포**  
Codedeploy agent는 폴링 방식을 통해 Codedeploy 서비스를 확인하다 배포가 있을 경우, [In-place deployments](https://docs.aws.amazon.com/codedeploy/latest/userguide/reference-appspec-file-structure-hooks.html#:~:text=in%20this%20diagram.-,In%2Dplace%20deployments,-In%20an%20in) 라이프사이클 훅에 따라 동작한다.  

    ![ec2_blue_green_lifecycle](/assets/it/cloud/codeseries/ec2_blue_green_lifecycle.png){: width="40%" height="auto"}  

   - BlockTraffic
     - BlockTraffic 단계에서는 타겟그룹에 타겟을 제거(DeregisterTargets API)하며 타겟그룹의 deregistration.delay 속성 값에 영향을 받는다.  
     - 만약, 타겟그룹 최초 생성시 설정한 포트와 트래픽 포트가 다른 경우 Draining이 되지 않는 문제가 발생할 수 있다.  
   - AllowTraffic
     - AllowTraffic 단계에서는 타겟그룹에 타겟을 등록(RegisterTargets API)하며 CodeDeploy는 ELB의 헬스체크를 확인한다. 만약, ELB의 타겟 헬스체크가 실패하면 **1시간** 후 Timeout이 발생한다.  


2. **Scale-out 동작 시**  
In-place 배포와 동일


3. **배포 실패 시**
Blue/Green 배포에서는 배포 실패 시 롤백이 진행되며, 새로 생성된 ASG, EC2 인스턴스에 대한 리소스가 남게된다. 이 리소스를 자동으로 정리하기 위해서는 다음의 과정을 통해 자동화 할 수 있다.  
[EC2 Blue/Green 배포 실패 시 생성된 ASG 자동 삭제 방법](https://byoungsoo.github.io/cloud/2024/04/25/cdp-asg-delete-automation.html)


<br><br>

## 3. [EC2 Blue/Green deployment]()  

#### 구성

Blue/Green 배포란 기존환경을 복사한 새로운 환경을 만들어서 배포를 진행하고 트래픽을 변경하는 방식을 의미한다. 

1. EC2 및 소스의 구성은 `EC2 - In place 배포`와 동일하다.  

2. 배포그룹 생성 
  - Blue/Green 선택 후 `Automatically copy Amazon EC2 Auto Scaling group` 옵션 선택
  - Application Load Balancer or Network Load Balancer 에서 타겟그룹 선택 
        - (*구성 후 사용중 실제 타겟그룹과 배포그룹의 타겟그룹이 다른 경우 정상적으로 배포가 되지 않는다.*)

<br>

#### 동작방식

![ec2_blue_green_lifecycle](/assets/it/cloud/codeseries/ec2_blue_green_lifecycle.png){: width="40%" height="auto"}  

1. 배포가 시작되면 `Automatically copy Amazon EC2 Auto Scaling group` 옵션 인 경우, deployment-id를 포함한 ASG가 신규로 생성된다.  
  - 신규로 생성된 ASG는 최초 [Suspended processes](https://docs.aws.amazon.com/autoscaling/ec2/APIReference/API_SuspendProcesses.html) 동작이 설정된다.  
2. AllowTraffic
  - AllowTraffic 단계에서는 타겟그룹에 신규 타겟을 등록(RegisterTargets API)하며 CodeDeploy는 ELB의 헬스체크를 확인한다.
3. BlockTraffic
   - BlockTraffic 단계에서는 타겟그룹에서 기존 타겟을 제거(DeregisterTargets API)하며 타겟그룹의 deregistration.delay 속성 값에 영향을 받는다.  
   - 만약, 타겟그룹 최초 생성시 설정한 포트와 트래픽 포트가 다른 경우 Draining이 되지 않는 문제가 발생할 수 있다.  
4. 만약 배포가 실패하면 신규로 생성된 ASG 환경은 그대로 남아 있게 되며 매뉴얼로 삭제가 필요하다. 이 부분을 자동화 하고 싶다면 아래 링크를 참고한다. 
   - http://bys.github.io/cloud/2024/04/25/cdp-asg-delete-automation.html

<br><br>


## 4. [Lambda Blue/Green deployment]()  

1. 람다 함수 생성 - CodeDeployTestFunction 

2. Alias 생성 - CodeDeployTestFunctionAlias
  - Alias의 Function URL 생성 

3. Version 생성 

4. 만약 신규 코드 생성이 필요한 경우 Lambda Deploy 까지는 해야 한다.  
  - Lambda function URL 자체는 Latest 이기 때문에 최신을 따라가지만 Alias의 Function URL은 특정 버전을 물고 있기 때문에 괜찮다.  

5. 신규 코드에 대한 신규 Version을 생성하고, CodeDeploy를 통해 특정 Alias의 트래픽을 넘긴다.  

```bash
# Lambda Test URL
awscurl --service lambda --region ap-northeast-2 https://pjk53dstey7vmklvucdbimjkni0pammw.lambda-url.ap-northeast-2.on.aws/

# Alias URL
awscurl --service lambda --region ap-northeast-2 https://7fhqven7gptk7ysjhzviizgxgq0mbhbh.lambda-url.ap-northeast-2.on.aws/
```

```python
import json

def lambda_handler(event, context):
    
    print("Start Running CodeDeployTestFunction Lambda V6!")
    print("Testing CodeDeploy")
    
    # TODO implement
    return {
        'statusCode': 200,
        'body': json.dumps('Hello from Lambda! V6')
    }
```

`AppSpec.yml`
```yaml
# This is an appspec.yml template file for use with an AWS Lambda deployment in CodeDeploy.
# The lines in this template starting with the hashtag symbol are 
#   instructional comments and can be safely left in the file or 
#   ignored.
# For help completing this file, see the "AppSpec File Reference" in the  
#   "CodeDeploy User Guide" at
#   https://docs.aws.amazon.com/codedeploy/latest/userguide/app-spec-ref.html
version: 0.0
# In the Resources section specify the name, alias, 
# target version, and (optional) the current version of your AWS Lambda function. 
Resources:
  - MyFunction: # Replace "MyFunction" with the name of your Lambda function 
      Type: AWS::Lambda::Function
      Properties:
        Name: "CodeDeployTestFunction" # Specify the name of your Lambda function
        Alias: "TestFunctionAlias" # Specify the alias for your Lambda function
        CurrentVersion: "7" # Specify the current version of your Lambda function
        TargetVersion: "8" # Specify the version of your Lambda function to deploy
# (Optional) In the Hooks section, specify a validation Lambda function to run during 
# a lifecycle event. Replace "LifeCycleEvent" with BeforeAllowTraffic
# or AfterAllowTraffic. 
```



## 10. [Trouble Shooting]()  
#### - Amazon Linux 3에서 Userdata로 Java가 설치되지 않음
/var/log/cloud-init-output.log를 확인해보면 다음과 같은 오류 메세지가 확인되며 Java가 실행되지 않음. `RPM: error: can't create transaction lock on /var/lib/rpm/.rpm.lock (Resource temporarily unavailable)`

어쨋든 원인은 rmp 에서 경합이 발생하였던 것이고, 원인과 해결방법은 아래 글에서 소개가 되었다.  
[Repost: Amazon Linux 2023 - issue with installing packages with cloud-init](https://repost.aws/questions/QU_tj7NQl6ReKoG53zzEqYOw/amazon-linux-2023-issue-with-installing-packages-with-cloud-init)

일부 시간 때에 SSM 관련된 rpm 작업이 있고, 따라서 retry 로직을 넣어서 문제를 해결할 수 있었다.  

```bash
# AS-IS
yum -y update;

yum -y install ruby
yum -y install wget
yum -y install java-11-amazon-corretto.x86_64

wget https://aws-codedeploy-ap-northeast-2.s3.ap-northeast-2.amazonaws.com/latest/install;
chmod +x ./install;
./install auto;

service codedeploy-agent start;


# TO-BE
install_package(){
  max_attempts=5
  attempt_num=1
  success=false
  while [ $success = false ] && [ $attempt_num -le $max_attempts ]; do
    echo "Trying yum install"

    yum -y install $1;
    # Check the exit code of the command
  
    if [ $? -eq 0 ]; then
      echo "Yum install succeeded"
      success=true
    else
      echo "Attempt $attempt_num failed. Sleeping for 3 seconds and trying again..."
      sleep 3
      ((attempt_num++))
    fi
  done
}
yum -y update;

instasll_package ruby
instasll_package wget
instasll_package java-11-amazon-corretto.x86_64

wget https://aws-codedeploy-ap-northeast-2.s3.ap-northeast-2.amazonaws.com/latest/install;
chmod +x ./install;
./install auto;

service codedeploy
```

<br>

#### - BlockTraffic 단계에서 타겟들이 정상적으로 DeregisterTargets[1] API에 의해 Draining이 되지 않는 문제
DeregisterTargets API[1]/CLI[2]의 문서에 따르면, Port Override를 한 경우 해당 요청에 대해 추가적인 Parameter를 정의하여 요청하게 되어 있으나 현재 CodeDeploy에서는 해당 기능이 아직 제공되고 있지 않기 때문에 **타겟그룹의 최초 생성시 설정한 포트와 트래픽 포트가 다른 경우** 문제가 발생할 수 있다.  



#### - EC2 In-place 설정에서 신규 인스턴스 생성시에도 CodeDeploy에 의한 신규 배포가 생성되지 않음.
CodeDeploy를 통해 EC2 in-place 배포를 설정하면 CodeDeploy에서 생성하는 LAUNCH:Lifecycle 훅이 생성된다. 해당 훅은 인스턴스가 신규로 시작/생성되면 CodeDeploy 서비스를 트리거하여 새로운 배포를 생성하도록 한다. 이를 통해 인스턴스 신규 생성시에도 새로운 코드가 잘 배포될 수 있다.

ASG를 통해 신규 인스턴스를 지속 생성하는데도, CodeDeploy 서비스를 통해 신규 배포가 되지 않음. 

이벤트를 확인해보니 CompleteLifecycleAction API 호출 이력은 존재함. 해당 API는 CodeDeploy가 작업 완료 후 ASG로 fall-back 을 위해 전송하는 API임. 이를 통해 LC 훅은 트리거가 되었으나 CodeDeploy에서 의도적으로 배포를 하지 않는 것으로 인지.
확인해보니 성공한 배포가 존재하지 않았음. 
이 동작은 CodeDeploy 에서 마지막으로 성공한 `배포`를 확인하여 신규 인스턴스에 배포를 생성하기 때문임. 





---

## 📚 References
