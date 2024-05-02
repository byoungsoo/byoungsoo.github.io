---
layout: post
title: "CodeDeploy 배포와 관련된 모든 것"
author: "Bys"
category: cloud
date: 2024-04-26 01:00:00
tags: incubator
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

3. Autoscaling Group 생성 
4. In-place 배포 생성 (선택: ASG / 옵션: AutoscalingGroup(ASG), EC2, On-prem)


#### 동작방식
[Codedeploy agent동작](https://docs.aws.amazon.com/codedeploy/latest/userguide/codedeploy-agent.html#codedeploy-agent-outbound-port)은 폴링 방식을 통해 Codedeploy 서비스를 호출하기 때문에 EC2 인스턴스의 443 포트 아웃바운드 통신이 가능해야 한다. 또한 Codedeploy agent가 EC2 인스턴스에서 동작할 때는 EC2 메타데이터 정보를 활용하기 때문에 IMDS 접근이 가능해야한다.  


1. 일반적인 배포  
Codedeploy agent는 폴링 방식을 통해 Codedeploy 서비스를 확인하다 배포가 있을 경우, [In-place deployments](https://docs.aws.amazon.com/codedeploy/latest/userguide/reference-appspec-file-structure-hooks.html#:~:text=in%20this%20diagram.-,In%2Dplace%20deployments,-In%20an%20in) 라이프사이클 훅에 따라 동작한다.  

    ![in_place_lifecycle](/assets/it/cloud/ec2/in_place_lifecycle.png){: width="40%" height="auto"}  


2. Scale-out 동작 시 
CodeDeploy 서비스를 통해 In-place 생성 시 옵션을 ASG로 했다면, ASG의 Lifecycle hooks에는 다음과 같은 훅이 자동으로 추가된다.  

![asg_lifecycle_1](/assets/it/cloud/ec2/asg_lifecycle_1.png){: width="60%" height="auto"}  

CodeDeploy-managed-automatic-launch-deployment-hook-inplace 훅은 아래 ASG 라이프사이클 단계에서 Launch가 시작되고 Pending: Wait 단계에서 훅이 시작된다. 이 단계는 CodeDeploy의 배포가 시작되고 해당 배포를 진행하는 단계이다. 즉, CodeDeploy 서비스를 통해 관리되는 ASG의 경우 노드가 Scale-out 되는 경우 새로운 Application이 기동 될 수 있도록 배포 단계가 실행되어야 하는데 그 단계를 ASG의 라이프사이클 훅을 통해 진행해준다.  


![asg_lifecycle](/assets/it/cloud/ec2/asg_lifecycle.png){: width="60%" height="auto"}  


## 2. [EC2 Blue/Green deployment]()  



## 10. [Trouble Shooting]()  
#### Amazon Linux 3에서 Userdata로 Java가 설치되지 않음
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

<br><br><br>

- References  
[1] EC2 Autoscaling Lifecycle hook - https://docs.aws.amazon.com/autoscaling/ec2/userguide/ec2-auto-scaling-lifecycle.html

