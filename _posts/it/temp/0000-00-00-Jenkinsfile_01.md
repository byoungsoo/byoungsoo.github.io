---
layout: post
title: "Jenkinsfile Test"
author: "Bys"
category: temp
date: 2021-04-06 01:00:00
tags: cicd jenkins pipeline
---

#### Jenkinsfile sample  

- `jenkinsfile`
```groovy
#!groovy

def utils
def BUILD_RESULT
def RCS_URL = "http://111.111.111.111"

pipeline {
    agent {
        node {
            label 'fargate-jenkins-slave-efs'
        }
    }

    options {
        ansiColor('xterm')
        timestamps()
        disableConcurrentBuilds()
    }
    
    parameters {
        string(name : 'RCS_ID', defaultValue : '', description : '')
        string(name : 'ENV', defaultValue : 'dev', description : '')
    }

    environment {
        AWS_DEFAULT_REGION = "ap-northeast-2"

        DEV_ACCOUNT_NUMBER = "604925827383"
        QAS_ACCOUNT_NUMBER = "016414240662"
        PRD_ACCOUNT_NUMBER = "571177120457"
    }

    post {
        always {
            script {
                BUILD_RESULT = currentBuild.result

                if ( params.ENV == 'prd' || params.ENV == 'rb' ) {
                    utils.call_rcs_end(RCS_URL, BUILD_RESULT, params.ENV)
                }
            }
        }
    }

    stages {
        stage('Load Groovy'){
            steps {
                script {
                    utils = load "cicd/groovy/jenkins_utils.Groovy"

                    env.source_artifact = 'sourceArtifact_rnd.zip'
                    env.system_code = 'cphome'
                    env.hq_code = 'cxo'
                    env.mod_code = 'rnd'

                    if ( params.ENV == 'prd' || params.ENV == 'rb' ) {
                        utils.call_rcs_start(RCS_URL, params.ENV)
                    }
                }
            }
        }

        stage('DEV') {
            when { expression { BRANCH_NAME == 'develop' }}
            stages {
                stage("[DEV] Set Environment") {
                    steps {
                        script{
                            utils.awsAssumeRole("arn:aws:iam::" + DEV_ACCOUNT_NUMBER + ":role/IAM-ECS-TASK-JENKINS")
                            
                            env.environment = 'dev'

                            env.ecr_image = 'ecr-an2-' + env.hq_code + '-' + env.environment + '-' + env.system_code
                            env.codebuild = 'cdb-an2-' + env.hq_code + '-' + env.environment + '-' + env.system_code + '-' + env.mod_code
                            env.s3_bucket = 's3-an2-' + env.hq_code + '-' + env.environment + '-' + env.system_code
                            env.managerip = ''
                            env.managerkey = ''
                        }
                    }
                }
            }
        }

        stage('QAS') {
            when { expression { BRANCH_NAME == 'master' && params.ENV != 'prd' && params.ENV != 'rb'}}
            stages {
                stage("[QAS] Set Environment") {
                    steps {
                        script{
                            utils.awsAssumeRole("arn:aws:iam::" + QAS_ACCOUNT_NUMBER + ":role/IAM-ECS-TASK-JENKINS")
                            sh 'aws configure set region ap-northeast-2'

                            env.environment = 'qas'

                            env.ecr_image = 'ecr-an2-' + env.hq_code + '-' + env.environment + '-' + env.system_code
                            env.codebuild = 'cdb-an2-' + env.hq_code + '-' + env.environment + '-' + env.system_code + '-' + env.mod_code
                            env.s3_bucket = 's3-an2-' + env.hq_code + '-' + env.environment + '-' + env.system_code
                            env.managerip = '10.88.114.13'
                            env.managerkey = 'VRNF5MliQHN%2Bj3p6kQn%2BMdqKwhXShzbfJUxZAKtlotMD7bu22XJhdVFl%2BBRqXRjV3Ayz45OuSoUMP08wwbJRpg%3D%3D'
                        }
                    }
                }

                stage("[QAS] Maven Build") {
                    steps {
                        script{
                            println "Maven Build"

                            sh 'cp config/${environment}/devon-core.xml devonhome/conf/devon-core.xml'
                            sh 'cp config/${environment}/devon-framework.xml devonhome/conf/product/devon-framework.xml'
                            sh 'cp config/${environment}/groupPortal.xml devonhome/conf/project/groupPortal.xml'
                            sh 'cp config/${environment}/lgchem.xml devonhome/conf/project/lgchem.xml'
                            sh 'cp config/${environment}/web.xml web/WEB-INF/web.xml'
                            
                            sh 'mvn package'

                            sh "aws s3 cp target/homepage_rnd.war s3://${env.s3_bucket}/source/latest/homepage_rnd.war"
                            sh "aws s3 sync devonhome s3://${env.s3_bucket}/source/latest/devonhome_rnd"
                        }
                    }
                }

                stage("[QAS] Execute CodeBuild") {
                    steps {
                        script{
                            println "Execute CodeBuild"

                            sh 'sed -i -e \"s#S3_BUCKET#${s3_bucket}#g\" cicd/codebuild/buildspec.yml'
                            sh 'sed -i -e \"s#AWS_ACCOUNT_ID#${QAS_ACCOUNT_NUMBER}#g\" cicd/codebuild/buildspec.yml'
                            sh 'sed -i -e \"s#ENVIRONMENT#${environment}#g\" cicd/codebuild/buildspec.yml'
                            sh 'sed -i -e \"s#ECR_IMAGE#${ecr_image}#g\" cicd/codebuild/buildspec.yml'
                            sh 'sed -i -e \"s#ECR_TAG_LATEST#${mod_code}-latest#g\" cicd/codebuild/buildspec.yml'
                            sh 'sed -i -e \"s#ECR_TAG_BACKUP#${mod_code}-backup#g\" cicd/codebuild/buildspec.yml'                            

                            sh 'sed -i -e \"s#AWS_ACCOUNT_ID#${QAS_ACCOUNT_NUMBER}#g\" cicd/codebuild/Dockerfile'
                            sh 'sed -i -e \"s#ECR_IMAGE#${ecr_image}#g\" cicd/codebuild/Dockerfile'
                            sh 'sed -i -e \"s#MANAGERIP#${managerip}#g\" cicd/codebuild/Dockerfile'
                            sh 'sed -i -e \"s#MANAGERKEY#${managerkey}#g\" cicd/codebuild/Dockerfile'

                            env.isCDBResult = sh ( script: 'bash ./cicd/script/codebuild.sh', returnStatus: true ) == 0
                            
                            echo "status: ${isCDBResult}"

                            if ( isCDBResult == 'false' ) {
                                println "codebuild failed"
                                currentBuild.result = "FAILURE"
                                throw new Exception("Throw to stop pipeline")
                            }
                        }
                    }
                }

                stage("[QAS] Execute CodeDeploy") {
                    steps {
                        script{
                            println "Execute CodeDploy"

                            env.ecs_task = sh ( script: 'echo ecs-an2-${hq_code}-${environment}-${system_code}-${mod_code}-tsk | tr [a-z] [A-Z]', returnStdout: true ).trim()
                            env.ecs_con = sh ( script: 'echo ecs-an2-${hq_code}-${environment}-${system_code}-${mod_code}-con | tr [a-z] [A-Z]', returnStdout: true ).trim()
                            
                            sh 'sed -i -e \"s#S3_BUCKET#${s3_bucket}#g\" cicd/codedeploy/appspec.yaml'
                            sh 'sed -i -e \"s#AWS_ACCOUNT_ID#${QAS_ACCOUNT_NUMBER}#g\" cicd/codedeploy/appspec.yaml'
                            sh 'sed -i -e \"s#ECS_TASK#${ecs_task}#g\" cicd/codedeploy/appspec.yaml'
                            sh 'sed -i -e \"s#ECS_CON#${ecs_con}#g\" cicd/codedeploy/appspec.yaml'

                            env.applicationName = sh ( script: 'echo cdd-an2-${hq_code}-${environment}-${system_code}-${mod_code}-app | tr [a-z] [A-Z]', returnStdout: true ).trim()
                            env.deploymentGroupName = sh ( script: 'echo cdd-an2-${hq_code}-${environment}-${system_code}-${mod_code}-dpg | tr [a-z] [A-Z]', returnStdout: true ).trim()

                            sh 'sed -i -e \"s#S3_BUCKET#${s3_bucket}#g\" cicd/codedeploy/create-deployment.json'
                            sh 'sed -i -e \"s#APPLICATION_NAME#${applicationName}#g\" cicd/codedeploy/create-deployment.json'
                            sh 'sed -i -e \"s#MOD_CODE#${mod_code}#g\" cicd/codedeploy/create-deployment.json'
                            sh 'sed -i -e \"s#DEPLOYMENT_GROUP_NAME#${deploymentGroupName}#g\" cicd/codedeploy/create-deployment.json'

                            env.isCDDResult = sh ( script: 'bash ./cicd/script/codedeploy.sh', returnStatus: true ) == 0

                            echo "status: ${isCDDResult}"

                            if ( isCDDResult == 'false' ) {
                               println "codedeploy failed"
                                currentBuild.result = "FAILURE"
                               throw new Exception("Throw to stop pipeline")
                            }
                        }
                    }
                }
            }
        }

        stage('PROD') {
            when { expression { BRANCH_NAME == 'master' && params.ENV == 'prd'}}
            stages {
                stage("[PROD] Set Environment") {
                    steps {
                        script{
                            sh "aws sts get-caller-identity" 

                            utils.awsAssumeRole("arn:aws:iam::" + PRD_ACCOUNT_NUMBER + ":role/IAM-ECS-TASK-JENKINS")
                            
                            env.environment = 'prd'

                            env.ecr_image = 'ecr-an2-' + env.hq_code + '-' + env.environment + '-' + env.system_code
                            env.codebuild = 'cdb-an2-' + env.hq_code + '-' + env.environment + '-' + env.system_code + '-' + env.mod_code
                            env.s3_bucket = 's3-an2-' + env.hq_code + '-' + env.environment + '-' + env.system_code
                            env.managerip = '10.88.124.50'
                            env.managerkey = 'vT2YO%2FLx9BuEVS%2FUHrz3ZaGgJGrNevL0fAkHP2KP4OMSyXhceMrjoORu%2BGhX5btrfCUy02VUB1FXIwedfzuO0w%3D%3D'
                        }
                    }
                }
                stage("[PROD] Maven Build") {
                    steps {
                        script{
                            println "Maven Build"
                            
                            sh 'cp config/${environment}/devon-core.xml devonhome/conf/devon-core.xml'
                            sh 'cp config/${environment}/devon-framework.xml devonhome/conf/product/devon-framework.xml'
                            sh 'cp config/${environment}/groupPortal.xml devonhome/conf/project/groupPortal.xml'
                            sh 'cp config/${environment}/lgchem.xml devonhome/conf/project/lgchem.xml'
                            sh 'cp config/${environment}/web.xml web/WEB-INF/web.xml'
                            
                            //war buuild, devonhome upload
                            sh 'mvn package'

                            sh "aws s3 cp target/homepage_rnd.war s3://${env.s3_bucket}/source/latest/homepage_rnd.war"
                            sh "aws s3 sync devonhome s3://${env.s3_bucket}/source/latest/devonhome_rnd"
                        }
                    }
                }
                
                stage("[PROD] Execute CodeBuild") {
                    steps {
                        script{
                            println "Execute CodeBuild"

                            sh 'sed -i -e \"s#S3_BUCKET#${s3_bucket}#g\" cicd/codebuild/buildspec.yml'
                            sh 'sed -i -e \"s#AWS_ACCOUNT_ID#${PRD_ACCOUNT_NUMBER}#g\" cicd/codebuild/buildspec.yml'
                            sh 'sed -i -e \"s#ENVIRONMENT#${environment}#g\" cicd/codebuild/buildspec.yml'
                            sh 'sed -i -e \"s#ECR_IMAGE#${ecr_image}#g\" cicd/codebuild/buildspec.yml'
                            sh 'sed -i -e \"s#ECR_TAG_LATEST#${mod_code}-latest#g\" cicd/codebuild/buildspec.yml'
                            sh 'sed -i -e \"s#ECR_TAG_BACKUP#${mod_code}-backup#g\" cicd/codebuild/buildspec.yml'                            

                            sh 'sed -i -e \"s#AWS_ACCOUNT_ID#${PRD_ACCOUNT_NUMBER}#g\" cicd/codebuild/Dockerfile'
                            sh 'sed -i -e \"s#ECR_IMAGE#${ecr_image}#g\" cicd/codebuild/Dockerfile'

                            env.isCDBResult = sh ( script: 'bash ./cicd/script/codebuild.sh', returnStatus: true ) == 0
                            
                            echo "status: ${isCDBResult}"

                            if ( isCDBResult == 'false' ) {
                                println "codebuild failed"
                                currentBuild.result = "FAILURE"
                                throw new Exception("Throw to stop pipeline")
                            }else{
                                echo "status: ${isCDBResult}"
                            }
                        }
                    }
                }
                
                stage("[PROD] Execute CodeDeploy") {
                    steps {
                        script{
                            println "Execute CodeDeploy"
                            
                            env.ecs_task = sh ( script: 'echo ecs-an2-${hq_code}-${environment}-${system_code}-${mod_code}-tsk | tr [a-z] [A-Z]', returnStdout: true ).trim()
                            env.ecs_con = sh ( script: 'echo ecs-an2-${hq_code}-${environment}-${system_code}-${mod_code}-con | tr [a-z] [A-Z]', returnStdout: true ).trim()
                            
                            sh 'sed -i -e \"s#S3_BUCKET#${s3_bucket}#g\" cicd/codedeploy/appspec.yaml'
                            sh 'sed -i -e \"s#AWS_ACCOUNT_ID#${PRD_ACCOUNT_NUMBER}#g\" cicd/codedeploy/appspec.yaml'
                            sh 'sed -i -e \"s#ECS_TASK#${ecs_task}#g\" cicd/codedeploy/appspec.yaml'
                            sh 'sed -i -e \"s#ECS_CON#${ecs_con}#g\" cicd/codedeploy/appspec.yaml'

                            env.applicationName = sh ( script: 'echo cdd-an2-${hq_code}-${environment}-${system_code}-${mod_code}-app | tr [a-z] [A-Z]', returnStdout: true ).trim()
                            env.deploymentGroupName = sh ( script: 'echo cdd-an2-${hq_code}-${environment}-${system_code}-${mod_code}-dpg | tr [a-z] [A-Z]', returnStdout: true ).trim()

                            sh 'sed -i -e \"s#S3_BUCKET#${s3_bucket}#g\" cicd/codedeploy/create-deployment.json'
                            sh 'sed -i -e \"s#APPLICATION_NAME#${applicationName}#g\" cicd/codedeploy/create-deployment.json'
                            sh 'sed -i -e \"s#DEPLOYMENT_GROUP_NAME#${deploymentGroupName}#g\" cicd/codedeploy/create-deployment.json'

                            env.isCDDResult = sh ( script: 'bash ./cicd/script/codedeploy.sh', returnStatus: true ) == 0

                            echo "status: ${isCDDResult}"
                            
                            if ( isCDDResult == 'false' ) {
                                println "codedeploy failed"
                                currentBuild.result = "FAILURE"
                                throw new Exception("Throw to stop pipeline")
                            }                            
                        }
                    }
                }
            }
        }
        
        stage('ROLL BACK') {
            when { expression { BRANCH_NAME == 'master' && params.ENV == 'rb'}}
            stages {
                stage("[ROLL BACK] Set Environment") {
                    steps {
                        script{
                            sh "aws sts get-caller-identity" 

                            utils.awsAssumeRole("arn:aws:iam::" + PRD_ACCOUNT_NUMBER + ":role/IAM-ECS-TASK-JENKINS")

                            env.environment = 'prd'

                            env.ecr_image = 'ecr-an2-' + env.hq_code + '-' + env.environment + '-' + env.system_code
                            env.codebuild = 'cdb-an2-' + env.hq_code + '-' + env.environment + '-' + env.system_code
                            env.s3_bucket = 's3-an2-' + env.hq_code + '-' + env.environment + '-' + env.system_code
                            env.managerip = '10.88.124.50'
                            env.managerkey = 'vT2YO%2FLx9BuEVS%2FUHrz3ZaGgJGrNevL0fAkHP2KP4OMSyXhceMrjoORu%2BGhX5btrfCUy02VUB1FXIwedfzuO0w%3D%3D'
                        }
                    }
                }
                
                stage("[ROLL BACK] Execute CodeBuild") {
                    steps {
                        script{
                            println "Execute Roll back CodeBuild"

                            sh 'sed -i -e \"s#S3_BUCKET#${s3_bucket}#g\" cicd/codebuild/buildspec.yml'
                            sh 'sed -i -e \"s#AWS_ACCOUNT_ID#${PRD_ACCOUNT_NUMBER}#g\" cicd/codebuild/buildspec.yml'
                            sh 'sed -i -e \"s#ENVIRONMENT#rb#g\" cicd/codebuild/buildspec.yml'
                            sh 'sed -i -e \"s#ECR_IMAGE#${ecr_image}#g\" cicd/codebuild/buildspec.yml'
                            sh 'sed -i -e \"s#ECR_TAG_LATEST#${mod_code}-latest#g\" cicd/codebuild/buildspec.yml'
                            sh 'sed -i -e \"s#ECR_TAG_BACKUP#${mod_code}-backup#g\" cicd/codebuild/buildspec.yml'                            

                            sh 'sed -i -e \"s#AWS_ACCOUNT_ID#${PRD_ACCOUNT_NUMBER}#g\" cicd/codebuild/Dockerfile'
                            sh 'sed -i -e \"s#ECR_IMAGE#${ecr_image}#g\" cicd/codebuild/Dockerfile'


                            env.isCDBResult = sh ( script: 'bash ./cicd/script/codebuild.sh', returnStatus: true ) == 0
                            
                            echo "status: ${isCDBResult}"

                            if ( isCDBResult == 'false' ) {
                                println "roll back codebuild failed"
                                currentBuild.result = "FAILURE"
                                throw new Exception("Throw to stop pipeline")
                            }
                        }
                    }
                }

                stage("[ROLL BACK] Execute CodeDeploy") {
                    steps {
                        script{
                            println "Execute Roll back CodeDploy"

                            env.ecs_task = sh ( script: 'echo ecs-an2-${hq_code}-${environment}-${system_code}-${mod_code}-tsk | tr [a-z] [A-Z]', returnStdout: true ).trim()
                            env.ecs_con = sh ( script: 'echo ecs-an2-${hq_code}-${environment}-${system_code}-${mod_code}-con | tr [a-z] [A-Z]', returnStdout: true ).trim()
                            
                            sh 'sed -i -e \"s#S3_BUCKET#${s3_bucket}#g\" cicd/codedeploy/appspec.yaml'
                            sh 'sed -i -e \"s#AWS_ACCOUNT_ID#${PRD_ACCOUNT_NUMBER}#g\" cicd/codedeploy/appspec.yaml'
                            sh 'sed -i -e \"s#ECS_TASK#${ecs_task}#g\" cicd/codedeploy/appspec.yaml'
                            sh 'sed -i -e \"s#ECS_CON#${ecs_con}#g\" cicd/codedeploy/appspec.yaml'

                            env.applicationName = sh ( script: 'echo cdd-an2-${hq_code}-${environment}-${system_code}-${mod_code}-app | tr [a-z] [A-Z]', returnStdout: true ).trim()
                            env.deploymentGroupName = sh ( script: 'echo cdd-an2-${hq_code}-${environment}-${system_code}-${mod_code}-dpg | tr [a-z] [A-Z]', returnStdout: true ).trim()

                            sh 'sed -i -e \"s#S3_BUCKET#${s3_bucket}#g\" cicd/codedeploy/create-deployment.json'
                            sh 'sed -i -e \"s#APPLICATION_NAME#${applicationName}#g\" cicd/codedeploy/create-deployment.json'
                            sh 'sed -i -e \"s#DEPLOYMENT_GROUP_NAME#${deploymentGroupName}#g\" cicd/codedeploy/create-deployment.json'

                            env.isCDDResult = sh ( script: 'bash ./cicd/script/codedeploy.sh', returnStatus: true ) == 0
                            
                            echo "status: ${isCDDResult}"

                            if ( isCDDResult =='false' ) {
                                println "rollback codedeploy failed"
                                currentBuild.result = "FAILURE"
                                throw new Exception("Throw to stop pipeline")
                            }
                        }
                    }
                }
            }
        }
    }
}

```
<br>

- `Dockerfile`

```Dockerfile
FROM 315541266977.dkr.ecr.ap-northeast-2.amazonaws.com/ecr-an2-shd-lena:was-base 

# LENA
ENV LENA_CONFIG_TEMPLATE_DOWNLOAD Y
ENV LENA_CONFIG_TEMPLATE_ID cprnd-cluster
ENV LENA_MANAGER_ADDRESS MANAGERIP:7700
ENV LENA_MANAGER_KEY MANAGERKEY
ENV LENA_CONTRACT_CODE ckm9ohGb6Q4ZPr492w0Y91ZfSwFRmNI8k9Y7qqxFXuykFLOGZHQnl8pWXIAXEpyG/1+JNo+OK99DSUyNp6rblpebznmAXmrxhxZmRoxR2+0=
ENV LENA_LICENSE_DOWNLOAD_URL=manager


#DOCKER CONTAINER TIMEZONE
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

#USER
RUN mkdir -p /home/lgcadmin
RUN chown lgcadmin:lgcadmin /home/lgcadmin

#COPY SRC
# RUN mkdir -p /data001/cphome/lgcgfiles
RUN mkdir -p /sorc001/cphome/homepage_rnd
RUN mkdir -p /sorc001/cphome/devonhome_rnd

COPY homepage_rnd.war /sorc001/cphome
COPY devonhome_rnd /sorc001/cphome/devonhome_rnd

RUN unzip /sorc001/cphome/homepage_rnd.war -d /sorc001/cphome/homepage_rnd
RUN rm -rf /sorc001/cphome/homepage_rnd.war
RUN rm -rf /sorc001/cphome/homepage_rnd/upload
RUN ln -s /data001/cphome/src/homepage_rnd/upload /sorc001/cphome/homepage_rnd/upload

#TOMCAT SETTING
# COPY server.xml /usr/local/tomcat/conf/server.xml
# COPY context.xml /usr/local/tomcat/conf/context.xml

#SCOUTER
RUN sed -i -e "s#WAS-01#cphome_rnd#g" /usr/local/lena/servers/appServer/scouter/agent.java/conf/scouter.conf
RUN sed -i -e "s#SCOUTER-IP#MANAGERIP#g" /usr/local/lena/servers/appServer/scouter/agent.java/conf/scouter.conf

#NAS
#RUN mkdir /data001
#RUN chown -R 5000:5000 /data001


#CUSTOM
#RUN apt-get update
#RUN alias ll='ls -l'
#RUN apt-get install -y vim

#PORT
EXPOSE 8180
```
<br>

- `Buildspec.yaml`

```yaml
version: 0.2

env:
  variables:
    BUILD_ENV: "ENVIRONMENT"
phases:
  pre_build:
    commands:
      - echo Logging in to Amazon ECR...
      - |
        if [ $BUILD_ENV != "rb" ] ; then      
          aws s3 cp s3://S3_BUCKET/source/latest/homepage_rnd.war homepage_rnd.war
          aws s3 sync s3://S3_BUCKET/source/latest/devonhome_rnd devonhome_rnd
        fi
      - $(aws ecr get-login --no-include-email --registry-ids 315541266977 --region ap-northeast-2)
  build:
    commands:
      - echo Build started on `date`
      - |
        if [ $BUILD_ENV != "rb" ] ; then
          docker build -t ECR_IMAGE:ECR_TAG_LATEST .
        fi
  post_build:
    commands:
      - echo Build completed on `date`
      - echo Pushing the Docker image...
      - $(aws ecr get-login --no-include-email --region ap-northeast-2)
      - |
        if [ $BUILD_ENV = "qas" ] ; then
          docker pull AWS_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/ECR_IMAGE:ECR_TAG_LATEST
          docker tag AWS_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/ECR_IMAGE:ECR_TAG_LATEST AWS_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/ECR_IMAGE:ECR_TAG_BACKUP
          docker push AWS_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/ECR_IMAGE:ECR_TAG_BACKUP
        fi
      - |
        if [ $BUILD_ENV = "rb" ] ; then
          docker pull AWS_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/ECR_IMAGE:ECR_TAG_BACKUP
          docker tag AWS_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/ECR_IMAGE:ECR_TAG_BACKUP AWS_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/ECR_IMAGE:ECR_TAG_LATEST
          docker push AWS_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/ECR_IMAGE:ECR_TAG_LATEST
        fi
      - |        
        if [ $BUILD_ENV != "rb" ] ; then
          docker tag ECR_IMAGE:ECR_TAG_LATEST AWS_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/ECR_IMAGE:ECR_TAG_LATEST
          docker push AWS_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/ECR_IMAGE:ECR_TAG_LATEST
        fi
```
<br>



- `Appspec.yaml`  

```yaml
version: 0.0
Resources:
  - TargetService:
      Type: AWS::ECS::Service
      Properties:
        TaskDefinition: "arn:aws:ecs:ap-northeast-2:AWS_ACCOUNT_ID:task-definition/ECS_TASK"
        LoadBalancerInfo:
          ContainerName: "ECS_CON"
          ContainerPort: 8180
        PlatformVersion: "1.4.0"
```
<br>




- `pom.xml`  

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>com.lgchem</groupId>
	<!-- 아래 artifactId와 name 을 수정하세요 -->
	<artifactId>front</artifactId>
	<name>LG Chem RO Front System</name>
	<!-- 끝 -->
	<packaging>war</packaging>
	<version>2.0</version>
	<build>
		<sourceDirectory>src</sourceDirectory>
		<outputDirectory>web/WEB-INF/classes</outputDirectory>
		<extensions>
			<extension>
				<groupId>com.github.platform-team</groupId>
				<artifactId>aws-maven</artifactId>
				<version>6.0.0</version>
			</extension>
		</extensions>
		<!-- 아래 경로를 수정하세요 -->
		<!-- <outputDirectory>web/WEB-INF/classes</outputDirectory> -->
		<finalName>LGCG_RO_FRONT</finalName>
		<!-- 끝 -->
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<configuration>
					<source>1.8</source>
					<target>1.8</target>
					<!-- 아래 구문을 넣어 컴파일을 skip하도록 합니다 -->
					<!-- <excludes> <exclude>**/*.*</exclude> </excludes> -->
					<!-- 끝 -->
					<encoding>UTF-8</encoding>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.codehaus.sonar</groupId>
				<artifactId>sonar-maven-plugin</artifactId>
				<version>3.4.1</version>
			</plugin>

			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-war-plugin</artifactId>
				<version>3.2.2</version>
				<configuration>
					<warName>lgcg_ro_front</warName>
					<failOnMissingWebXml>false</failOnMissingWebXml>
					<webResources>
						<webResource>
							<directory>${basedir}/web</directory>
						</webResource>
						
					</webResources>
				</configuration>

			</plugin>
		</plugins>
	</build>
	<dependencies>
		<dependency>
			<groupId>javax.servlet</groupId>
			<artifactId>javax.servlet-api</artifactId>
			<version>3.1.0</version>
			<scope>provided</scope>
		</dependency>
		<dependency>
			<groupId>javax.servlet.jsp</groupId>
			<artifactId>jsp-api</artifactId>
			<version>2.0</version>
			<scope>provided</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>encryptUtil</artifactId>
			<systemPath>${project.lib.path}/encryptUtil-1.2_jdk1.4.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>poi-examples</artifactId>
			<systemPath>${project.lib.path}/poi-examples-3.17.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>json-simple</artifactId>
			<systemPath>${project.lib.path}/json-simple-1.1.1.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>jstl</artifactId>
			<systemPath>${project.lib.path}/jstl.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>commons-codec</artifactId>
			<systemPath>${project.lib.path}/commons-codec-1.10.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>devon-core</artifactId>
			<systemPath>${project.lib.path}/devon-core.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>ehcache-core</artifactId>
			<systemPath>${project.lib.path}/ehcache-core-2.6.2.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>activation</artifactId>
			<systemPath>${project.lib.path}/activation.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>cglib-nodep</artifactId>
			<systemPath>${project.lib.path}/cglib-nodep-2.2.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>poi-contrib</artifactId>
			<systemPath>${project.lib.path}/poi-contrib-3.6.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>commons-lang</artifactId>
			<systemPath>${project.lib.path}/commons-lang-2.6.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>xalan</artifactId>
			<systemPath>${project.lib.path}/xalan.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>commons-collections</artifactId>
			<systemPath>${project.lib.path}/commons-collections4-4.1.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>requisite</artifactId>
			<systemPath>${project.lib.path}/requisite.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>commons-logging</artifactId>
			<systemPath>${project.lib.path}/commons-logging.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>poi-ooxml-schemas</artifactId>
			<systemPath>${project.lib.path}/poi-ooxml-schemas-3.6.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>poi</artifactId>
			<systemPath>${project.lib.path}/poi-3.11.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>slf4j-nop</artifactId>
			<systemPath>${project.lib.path}/slf4j-nop-1.6.1.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>ooxml-schemas</artifactId>
			<systemPath>${project.lib.path}/ooxml-schemas-1.0.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>standard</artifactId>
			<systemPath>${project.lib.path}/standard.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>commons-fileupload</artifactId>
			<systemPath>${project.lib.path}/commons-fileupload-1.2.1.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>jaxrpc</artifactId>
			<systemPath>${project.lib.path}/jaxrpc.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>axis</artifactId>
			<systemPath>${project.lib.path}/axis.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>mail</artifactId>
			<systemPath>${project.lib.path}/mail.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>poi-scratchpad</artifactId>
			<systemPath>${project.lib.path}/poi-scratchpad-3.6.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>xmlbeans</artifactId>
			<systemPath>${project.lib.path}/xmlbeans-2.6.0.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>jxl</artifactId>
			<systemPath>${project.lib.path}/jxl.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>commons-io</artifactId>
			<systemPath>${project.lib.path}/commons-io-1.2.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>ojdbc14</artifactId>
			<systemPath>${project.lib.path}/ojdbc14.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>slf4j-api</artifactId>
			<systemPath>${project.lib.path}/slf4j-api-1.6.1.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>wsdl4j</artifactId>
			<systemPath>${project.lib.path}/wsdl4j.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>saaj</artifactId>
			<systemPath>${project.lib.path}/saaj.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>gwt-servlet-deps</artifactId>
			<systemPath>${project.lib.path}/gwt-servlet-deps.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>commons-discovery</artifactId>
			<systemPath>${project.lib.path}/commons-discovery-0.2.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>mariadb-java-client</artifactId>
			<systemPath>${project.lib.path}/mariadb-java-client-2.6.0.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>devon-framework</artifactId>
			<systemPath>${project.lib.path}/devon-framework.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>poi-excelant</artifactId>
			<systemPath>${project.lib.path}/poi-excelant-3.17.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
		<dependency>
			<groupId>com.lgchem</groupId>
			<artifactId>poi-ooxml</artifactId>
			<systemPath>${project.lib.path}/poi-ooxml-3.11.jar</systemPath>
			<version>2.0</version>
			<scope>system</scope>
		</dependency>
	</dependencies>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.lib.path>${basedir}/web/WEB-INF/lib</project.lib.path>
	</properties>
</project>
```
<br>