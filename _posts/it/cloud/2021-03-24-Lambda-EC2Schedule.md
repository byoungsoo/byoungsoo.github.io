---
layout: post
title: "Lambda함수와 EC2 Tags를 이용한 EC2 Instance Schedule"
author: "Bys"
category: cloud
date: 2021-03-24 01:00:00
tags: aws ec2 lambda schedule tags
---

해당 내용들은 모두 EC2 Tag기반으로 Lambda가 Instance를 조회하기 때문에 EC2에 Tag를 꼭 등록해주어야 작동한다.

다음과 같이 Tag를 등록하고 동작시키면 작동이 된다.  

Scheduled: True  
ScheduleStart: 09:00  
ScheduleStop: 18:00  


#### Version1. Lambda를 이용한 Stop  
```Python
import boto3
import time
##
# First function will try to filter for EC2 instances that contain a tag named `Scheduled` which is set to `True`
# If that condition is meet function will compare current time (H:M) to a value of the additional tags which defines the trigger `ScheduleStop` or `ScheduleStart`.
# Value of the `ScheduleStop` or `ScheduleStart` must be in the following format `H:M` - example `09:00`  
# 
# In order to trigger this function make sure to setup CloudWatch event which will be executed every minute. 
# Following Lambda Function needs a role with permission to start and stop EC2 instances and writhe to CloudWatch logs.
# 
# Example EC2 Instance tags: 
# 
# Scheduled     : True
# ScheduleStop  : 
##

#define boto3 the connection
ec2 = boto3.resource('ec2')

def lambda_handler(event, context):
    
    # Get current time in format H:M
    current_time = time.strftime("%H:%M")
    
    # Find all the instances that are tagged with Scheduled:True
    filters = [{
            'Name': 'tag:Scheduled',
            'Values': ['True']
        }
    ]

    # Search all the instances which contains scheduled filter 
    instances = ec2.instances.filter(Filters=filters)

    stopInstances = []   

    # Locate all instances that are tagged to start or stop.
    for instance in instances:
        for tag in instance.tags:
            if tag['Key'] == 'ScheduleStop':
                stopInstances.append(instance.id)
                pass
            pass
        pass
        
    # shut down all instances tagged to stop. 
    if len(stopInstances) > 0:
        # perform the shutdown
        stop = ec2.instances.filter(InstanceIds=stopInstances).stop()
        print stop
    else:
        print "No instances to shutdown."
```  
<br><br>


#### Version2. Lambda를 이용한 Start  

```Python
import boto3
import time
##
# First function will try to filter for EC2 instances that contain a tag named `Scheduled` which is set to `True`
# If that condition is meet function will compare current time (H:M) to a value of the additional tags which defines the trigger `ScheduleStop` or `ScheduleStart`.
# Value of the `ScheduleStop` or `ScheduleStart` must be in the following format `H:M` - example `09:00`  
# 
# In order to trigger this function make sure to setup CloudWatch event which will be executed every minute. 
# Following Lambda Function needs a role with permission to start and stop EC2 instances and writhe to CloudWatch logs.
# 
# Example EC2 Instance tags: 
# 
# Scheduled     : True
# ScheduleStart : 
##

#define boto3 the connection
ec2 = boto3.resource('ec2')

def lambda_handler(event, context):
    
    # Get current time in format H:M
    current_time = time.strftime("%H:%M")
    
    # Find all the instances that are tagged with Scheduled:True
    filters = [{
            'Name': 'tag:Scheduled',
            'Values': ['True']
        }
    ]

    # Search all the instances which contains scheduled filter 
    instances = ec2.instances.filter(Filters=filters)

    stopInstances = []   
    startInstances = []   

    # Locate all instances that are tagged to start or stop.
    for instance in instances:
        for tag in instance.tags:
            if tag['Key'] == 'ScheduleStart':
                startInstances.append(instance.id)
                pass
            pass
        pass
    
    print current_time

    # start instances tagged to stop. 
    if len(startInstances) > 0:
        # perform the start
        start = ec2.instances.filter(InstanceIds=startInstances).start()
        print (start)
    else:
        print ("No instances to start.")
```
<br><br>

#### Version1, 2 Event 등록  
Cloudwatch - Events - Rules - Create Rule  

![scouter](/assets/it/cloud/ec2schedule/createRule.png){: width="90%" height="auto"}  

위의 이미지와 같이 Cron expression 0 10 ? * MON-SUN * 을 통해 월요일-일요일 매 10:00:00GMT (18:00:00 KMT) 시간에 StopEC2Instance 스케줄이 돌도록 설정한다.  

<br><br>

![scouter](/assets/it/cloud/ec2schedule/cloudEvents.png){: width="90%" height="auto"}  

마찬가지로 StartEC2Instance 스케줄이 돌도록 시간을 설정한다.  


<br><br>

#### Version3. Lambda를 이용한 start, stop  
Cron expression */1 * * ? * MON-SUN * 을 통해 월요일-금요일 매분 마다 스케줄이 돌도록 설정한다.  
매분 마다 Event가 수행 되다가 tags의 ScheduleStart, ScheduleStop 에 등록된 시간이 현재 시간과 같으면 기동 및 종료 되는 스크립트이다.  

```Python
import boto3
import time
##
# First function will try to filter for EC2 instances that contain a tag named `Scheduled` which is set to `True`
# If that condition is meet function will compare current time (H:M) to a value of the additional tags which defines the trigger `ScheduleStop` or `ScheduleStart`.
# Value of the `ScheduleStop` or `ScheduleStart` must be in the following format `H:M` - example `09:00`  
# 
# In order to trigger this function make sure to setup CloudWatch event which will be executed every minute. 
# Following Lambda Function needs a role with permission to start and stop EC2 instances and writhe to CloudWatch logs.
# 
# Example EC2 Instance tags: 
# 
# Scheduled     : True
# ScheduleStart : 06:00
# ScheduleStop  : 18:00
##

#define boto3 the connection
ec2 = boto3.resource('ec2')

def lambda_handler(event, context):
    
    # Get current time in format H:M
    current_time = time.strftime("%H:%M")
    
    # Find all the instances that are tagged with Scheduled:True
    filters = [{
            'Name': 'tag:Scheduled',
            'Values': ['True']
        }
    ]

    # Search all the instances which contains scheduled filter 
    instances = ec2.instances.filter(Filters=filters)

    stopInstances = []   
    startInstances = []   

    # Locate all instances that are tagged to start or stop.
    for instance in instances:
        for tag in instance.tags:
            if tag['Key'] == 'ScheduleStop':

                if tag['Value'] == current_time:
                    stopInstances.append(instance.id)
                    pass
                pass

            if tag['Key'] == 'ScheduleStart':
                if tag['Value'] == current_time:
                    startInstances.append(instance.id)
                    pass
                pass
            pass
        pass
    
    print current_time
    
    # shut down all instances tagged to stop. 
    if len(stopInstances) > 0:
        # perform the shutdown
        stop = ec2.instances.filter(InstanceIds=stopInstances).stop()
        print stop
    else:
        print "No instances to shutdown."

    # start instances tagged to stop. 
    if len(startInstances) > 0:
        # perform the start
        start = ec2.instances.filter(InstanceIds=startInstances).start()
        print start
    else:
        print "No instances to start."
```

