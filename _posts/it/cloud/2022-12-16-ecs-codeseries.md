---
layout: post
title: "ECS에 CodeSeries를 통해 배포하기 (Multi Account환경)"
author: "Bys"
category: cloud
date: 2022-12-16 01:00:00
tags: codecommit codebuild codepipeline codeseries aws ecs
---

# CodeSeries
CodeSeries는 AWS에서 제공하는 CI/CD의 집합이다. Repository, Build, Deploy, Pipeline 등 우리가 Application을 개발하고 빌드하고 배포하는데 필요한 서비스들을 제공해준다.  
여기서는 Multi Account환경(shared, dev)에서 CodeCommit, CodeBuild, CodeDeploy, CodePipeline을 통해 ECS환경에 Blue/Green 배포를 하는 환경을 구성해 볼 것이다.  

계정별 서비스를 살펴보면 CodeCommit, CodeBuild CodePipeline은 shared계정에 생성하며 CodeDeploy, ECS, ECR 서비스는 dev계정에서 생성한다.  

![codeseries-architecture001](/assets/it/cloud/codeseries/codeseries-architecture001.png){: width="100%" height="100%"}  


- 구축하면서 알게 된 점 및 알아두면 좋을 점 
    1. CodePipeline, CodeBuild, CodeDeploy는 각각 자신의 Role을 갖는다.  
    2. CodePipeline, CodeBuild, CodeDeploy는 S3 접근이 필요하다.  
    3. 만약 aws-cli를 통해 S3주소를 변경하게 되면 각 서비스에서 사용하는 role에 붙은 policy의 s3주소를 모두 수정해주어야 정상 동작한다.  
    4. CodeBuild에서 S3에 캐시를 저장하고 사용하면 빌드 속도가 개선된다.  
    5. CodeBuild에서 생성하는 Artifact과 CodePipeline에서 설정한 Artifact은 다른 것이다. CodePipeline을 통해 Artifact을 설정하기 때문에 CodeBuild에서 Artifact는 설정을 안해도 된다.  
    6. CodePipeline에서 Cross Account의 CodeDeploy에 작업할 때 수행되는 권한은 Deploy단계의 actions에 설정된 roleArn이다.  

## 1. [CodeCommit](https://docs.aws.amazon.com/codecommit/latest/userguide/welcome.html)
1. CodeCommit을 사용하기 위해서는 IAM User 등록이 되어야 한다.  

2. IAM User에 AWSCodeCommitFullAccess, AWSCodeCommitPowerUser, AWSCodeCommitReadOnly과 같은 적당한 policy를 부여한다.  

3. Create Git credentials for HTTPS connections to CodeCommit 이 부분을 설정 해야 한다. 일반적으로 git clone, git pull, git push 와 같은 작업을 수행할 때 인증하는 User가 위 에서 생성한 IAM User가 아니다.  
Git credential을 생성하면 Username, Password가 생성되는데 이 credential이 CodeCommit의 repository에 인증할 수 있는 credential이다.  

4. Repository를 생성하고 이 후 부터는 일반적인 Git repository처럼 사용하면 된다.  

## 2. [CodeBuild](https://docs.aws.amazon.com/codebuild/latest/userguide/welcome.html)
1. CodeBuild에서 수행할 buildspec.yml파일을 정의한다.  
    - buildspec.yml 파일은 CodeBuild가 수행되면서 읽는 파일이다. 해당 내용에 어떤 build가 이루어지는지 spec을 정의한다. Ex) Jenkinsfile, .gitlab-ci.yml
    - Source코드의 root에 존재하면 default로 읽고, 특정위치에 설정을 해도 된다. S3에 업로드하고 사용해도 된다.  
 
2. CodeBuild에서 사용할 Role을 셋팅한다. (bys-shared-iam-cdb-role)
    - CodeBuild에서 build작업 중 ECR을 이용하기 때문에 bys-shared-iam-cdb-role에는 AmazonEC2ContainerRegistryFullAccess정책을 추가한다.  
    - CodeBuild에서 Assume Role작업이 있다면 bys-shared-iam-cdb-role에 sts permission과 대상 Role에 Trust relationship 설정이 필요하다.  

3. CodeBuild에서 사용할 S3를 생성해야 한다. 여기서는 추후 CodePipeline에서 사용할 S3와 같이 사용한다.  

4. Codebuild에서 buildspec.yml에 사용할 환경변수를 셋팅할 수 있는데 제공되는 환경변수 [Environment variables in build environments](https://docs.aws.amazon.com/codebuild/latest/userguide/build-env-ref-env-vars.html)내용을 참고한다.  
추가적으로 CodeCommit에서 넘어오는 환경변수를 받아사용해야 하는 경우는 CodePipeline단계에서 설정하며 [Variables](https://docs.aws.amazon.com/codepipeline/latest/userguide/reference-variables.html)를 참고한다.  

5. [CodeBuild생성](https://docs.aws.amazon.com/codebuild/latest/userguide/create-project-cli.html#cli.source.location)  
최종적으로는 아래와 같이 정의된 파일과 함께 생성할 수 있다.  

```bash
# Create Template
aws codebuild create-project --generate-cli-skeleton > codebuild.json

# Create CodeBuild project
aws codebuild create-project --cli-input-json file://cdb-dev.json
aws codebuild update-project --cli-input-json file://cdb-dev.json
```

`cdb-dev.json`  
```json
{
    "name": "bys-shared-cdb-awssdk-iam-dev",
    "description": "bys-shared-cdb-awssdk-iam-dev",
    "source": {
        "type": "CODECOMMIT",
        "location": "https://git-codecommit.ap-northeast-2.amazonaws.com/v1/repos/awssdk-iam",
        "gitCloneDepth": 1,
        "gitSubmodulesConfig": {
            "fetchSubmodules": false
        },
        "buildspec": "codeseries/codebuild/buildspec.yml",
        "insecureSsl": false
    },
    "secondarySources": [],
    "sourceVersion": "refs/heads/develop",
    "secondarySourceVersions": [],
    "artifacts": {
        "type": "S3",
        "location": "bys-shared-s3-codeseries-awssdk-iam",
        "path": "/dev/artifacts",
        "name": "BuildArtifact"
    },
    "secondaryArtifacts": [],
    "cache": {
        "type": "S3",
        "location": "bys-shared-s3-codeseries-awssdk-iam/dev/cahce"
    },
    "environment": {
        "type": "LINUX_CONTAINER",
        "image": "aws/codebuild/amazonlinux2-x86_64-standard:3.0",
        "computeType": "BUILD_GENERAL1_SMALL",
        "environmentVariables": [],
        "privilegedMode": false,
        "imagePullCredentialsType": "CODEBUILD",
        "privilegedMode": true
    },
    "serviceRole": "arn:aws:iam::202949997891:role/service-role/bys-shared-iam-cdb-role",
    "timeoutInMinutes": 60,
    "queuedTimeoutInMinutes": 480,
    "encryptionKey": "arn:aws:kms:ap-northeast-2:202949997891:key/11112222-71d6-4265-9dab-111122223333",
    "tags": [
        {
            "key": "Name",
            "value": "bys-shared-cdb-awssdk-iam-dev"
        }
    ],
    "badgeEnabled": false,
    "logsConfig": {
        "cloudWatchLogs": {
            "status": "ENABLED"
        },
        "s3Logs": {
            "status": "ENABLED",
            "location": "bys-shared-s3-codeseries-awssdk-iam/dev/logs",
            "encryptionDisabled": false
        }
    }
}
```

`codeseries/codebuild/buildspec.yml`
```yaml
version: 0.2
env:
  shell: bash
  variables:
    DOCKER_DRIVER: "overlay2"
    #Application
    APPLICATION_NAME: awssdk-iam
    APPLICATION_PORT: 10012
    APPLICATION_PATH: iam
    APPLICATION_VERSION: 0.0.1
    ALB_INGRESS_GROUP: awssdk
    APPLICATION_NS: aws

    # AWS
    AWS_REGION: "ap-northeast-2"
    CODESERIES_S3: bys-shared-s3-codeseries-awssdk-iam
    KMS_KEY_ID: "11112222-71d6-4265-9dab-111122223333"
    DEV_ACCOUNT_NO: "558846431111"


    DEV_REGISTRY_URL: "558846431111.dkr.ecr.ap-northeast-2.amazonaws.com"
    DEV_ASSUME_ROLE_NAME: "bys-dev-iam-deploy-role"
    DEV_ROLE_SESSION_NAME: "DEV"

    STG_REGISTRY_URL: ${STG_ACCOUNT_NO}.dkr.ecr.ap-northeast-2.amazonaws.com
    STG_ASSUME_ROLE_NAME: "bys-stg-iam-deploy-role"
    STG_ROLE_SESSION_NAME: "STG"

    PRD_REGISTRY_URL: ${PRD_ACCOUNT_NO}.dkr.ecr.ap-northeast-2.amazonaws.com
    PRD_ASSUME_ROLE_NAME: "bys-prd-iam-deploy-role"
    PRD_ROLE_SESSION_NAME: "PRD"


phases:
  install:
    runtime-versions:
      java: corretto11
#    commands:
#      - echo ${COMMIT_ID}
#      - echo ${BRANCH_NAME}
  pre_build:
    on-failure: ABORT
    commands:
      - echo "Current Path"
      - pwd

        # Env
      - if [ "${BRANCH_NAME}" == "develop" ]; then
          export ENVIRONMENT="dev";
          export ACCOUNT_NO=${DEV_ACCOUNT_NO};
          export ASSUME_ROLE_NAME=${DEV_ASSUME_ROLE_NAME};
          export ROLE_SESSION_NAME=${DEV_ROLE_SESSION_NAME};
          export REGISTRY_URL=${DEV_REGISTRY_URL};
        elif [ "${BRANCH_NAME}" == "stage" ]; then
          export ENVIRONMENT="stg";
          export ACCOUNT_NO=${STG_ACCOUNT_NO};
          export ASSUME_ROLE_NAME=${STG_ASSUME_ROLE_NAME};
          export ROLE_SESSION_NAME=${STG_ROLE_SESSION_NAME};
          export REGISTRY_URL=${STG_REGISTRY_URL};
        elif [ "${BRANCH_NAME}" == "master" ]; then
          export ENVIRONMENT="prd";
          export ACCOUNT_NO=${PRD_ACCOUNT_NO};
          export ASSUME_ROLE_NAME=${PRD_ASSUME_ROLE_NAME};
          export ROLE_SESSION_NAME=${PRD_ROLE_SESSION_NAME};
          export REGISTRY_URL=${PRD_REGISTRY_URL};
        fi
      - echo ${ENVIRONMENT}
      - aws sts get-caller-identity

  build:
    on-failure: ABORT
    commands:
      
      ### Gradle Build ###
      - echo Gradle Build started on `date`
      - echo pwd `pwd`

      # Change Variables in Dockerfile
      - sed -i "s/<APPLICATION_NAME>/${APPLICATION_NAME}/g" build.gradle

      - sed -i "s/<APPLICATION_NAME>/${APPLICATION_NAME}/g" src/main/resources/awssdk-xray-rules.json
      - sed -i "s/<APPLICATION_PATH>/${APPLICATION_PATH}/g" src/main/resources/awssdk-xray-rules.json
      - sed -i "s/<ENVIRONMENT>/${ENVIRONMENT}/g" src/main/resources/awssdk-xray-rules.json
  
  
      - sed -i "s/<APPLICATION_PATH>/${APPLICATION_PATH}/g" src/main/resources/application-dev.yaml
      - sed -i "s/<APPLICATION_PORT>/${APPLICATION_PORT}/g" src/main/resources/application-dev.yaml

      - ./gradlew clean bootJar
      - ls -al build/libs/

      ### Docker Build ###
      # Change Variables in Dockerfile
      - sed -i "s/<APPLICATION_NAME>/${APPLICATION_NAME}/g" env/${ENVIRONMENT}/Dockerfile
      - sed -i "s/<APPLICATION_VERSION>/${APPLICATION_VERSION}/g" env/${ENVIRONMENT}/Dockerfile
      - sed -i "s/<APPLICATION_PORT>/${APPLICATION_PORT}/g" env/${ENVIRONMENT}/Dockerfile

      # Change Files
      - cp env/${ENVIRONMENT}/Dockerfile Dockerfile
      - cp env/${ENVIRONMENT}/docker-entrypoint.sh docker-entrypoint.sh

      # AWS ECR Login
      - $(aws ecr get-login --no-include-email --region ${AWS_REGION})

      - COMMIT_ID_SHA=$(echo $CODEBUILD_RESOLVED_SOURCE_VERSION | cut -b -8)
      - docker build -t ${APPLICATION_NAME}:${COMMIT_ID_SHA} .

      # Assume Role
      - ASSUME_ROLE_CREDENTIALS=$(aws sts assume-role --role-arn arn:aws:iam::${ACCOUNT_NO}:role/${ASSUME_ROLE_NAME} --role-session-name ${ROLE_SESSION_NAME} --region ${AWS_REGION})
      - export AWS_ACCESS_KEY_ID=$(echo $ASSUME_ROLE_CREDENTIALS | jq .Credentials.AccessKeyId | sed 's/"//g')
      - export AWS_SECRET_ACCESS_KEY=$(echo $ASSUME_ROLE_CREDENTIALS | jq .Credentials.SecretAccessKey | sed 's/"//g')
      - export AWS_SESSION_TOKEN=$(echo $ASSUME_ROLE_CREDENTIALS | jq .Credentials.SessionToken | sed 's/"//g')

      # AWS ECR Login Account Changed
      - $(aws ecr get-login --no-include-email --region ${AWS_REGION})

      ### Check Latest Image
      - export IS_LATEST=`aws ecr describe-images --region ${AWS_REGION} --repository-name ecs/${ENVIRONMENT}-${APPLICATION_NAME} --query imageDetails[].imageTags | grep latest | wc -l`
      ### Docker Backup
      - >
        if [ "$IS_LATEST" == "1" ]; then
          echo "Image Backup";
          docker pull ${REGISTRY_URL}/ecs/${ENVIRONMENT}-${APPLICATION_NAME}:latest;
          docker tag ${REGISTRY_URL}/ecs/${ENVIRONMENT}-${APPLICATION_NAME}:latest ${REGISTRY_URL}/ecs/${ENVIRONMENT}-${APPLICATION_NAME}:backup;
          docker push ${REGISTRY_URL}/ecs/${ENVIRONMENT}-${APPLICATION_NAME}:backup;
          docker rmi ${REGISTRY_URL}/ecs/${ENVIRONMENT}-${APPLICATION_NAME}:latest;
        fi

      ### Docker Push into ECR
      - docker tag ${APPLICATION_NAME}:${COMMIT_ID_SHA} ${REGISTRY_URL}/ecs/${ENVIRONMENT}-${APPLICATION_NAME}:${COMMIT_ID_SHA}
      - docker push ${REGISTRY_URL}/ecs/${ENVIRONMENT}-${APPLICATION_NAME}:${COMMIT_ID_SHA}

      - docker tag ${APPLICATION_NAME}:${COMMIT_ID_SHA} ${REGISTRY_URL}/ecs/${ENVIRONMENT}-${APPLICATION_NAME}:latest
      - docker push ${REGISTRY_URL}/ecs/${ENVIRONMENT}-${APPLICATION_NAME}:latest

      ### Docker Delete Images
      - docker rmi ${APPLICATION_NAME}:${COMMIT_ID_SHA}
      - docker rmi ${REGISTRY_URL}/ecs/${ENVIRONMENT}-${APPLICATION_NAME}:${COMMIT_ID_SHA}
      - docker rmi ${REGISTRY_URL}/ecs/${ENVIRONMENT}-${APPLICATION_NAME}:latest
      - docker rmi ${REGISTRY_URL}/ecs/${ENVIRONMENT}-${APPLICATION_NAME}:backup

  post_build:
    commands:
      ### Upload appspec.yml for CodeDeploy
      - echo post_build completed on `date`
      
      # Change Variables in appspec.yml
      - export TD_REVISION=$(aws ecs describe-task-definition --task-definition bys-${ENVIRONMENT}-td-${APPLICATION_NAME} | jq -r '.taskDefinition.revision')
      - echo ${TD_REVISION}

      - sed -i "s/<AWS_REGION>/${AWS_REGION}/g" codeseries/codedeploy/appspec.yml
      - sed -i "s/<ACCOUNT_NO>/${ACCOUNT_NO}/g" codeseries/codedeploy/appspec.yml
      - sed -i "s/<ENVIRONMENT>/${ENVIRONMENT}/g" codeseries/codedeploy/appspec.yml
      - sed -i "s/<APPLICATION_NAME>/${APPLICATION_NAME}/g" codeseries/codedeploy/appspec.yml
      - sed -i "s/<APPLICATION_PORT>/${APPLICATION_PORT}/g" codeseries/codedeploy/appspec.yml
      - sed -i "s/<TD_REVISION>/${TD_REVISION}/g" codeseries/codedeploy/appspec.yml

artifacts:
  files:
    - 'appspec.yml'
    - 'taskdef.json'
  base-directory: 'codeseries/codedeploy/'
cache:
  paths:
    - '/root/.gradle/caches/**/*'
    - '/root/.gradle/wrapper/**/*'
```

<br>

## 3. [CodePipeline](https://docs.aws.amazon.com/codepipeline/latest/userguide/welcome.html)
1. CodePipeline에서는 이전에 생성 된 CodeCommit, CodeBuild를 활용해 파이프라인을 구축한다. 코드 파이프라인의 구조는 [CodePipeline pipeline structure](https://docs.aws.amazon.com/codepipeline/latest/userguide/reference-pipeline-structure.html)를 참고한다.  

2. Source 단계에서 namespace에 SourceVariables를 설정하면 Build단계에서 #{SourceVariables.CommitId}과 같은 변수를 설정해 사용할 수 있다. [Variables](https://docs.aws.amazon.com/codepipeline/latest/userguide/reference-variables.html)문서를 참고한다.  

    ```bash
    aws codepipeline create-pipeline --cli-input-json file://cdpl-dev.json
    aws codepipeline update-pipeline --cli-input-json file://cdpl-dev.json
    ```
    `cdpl-dev.json`
    ```json
    {
        "pipeline": {
            "name": "bys-shared-cdpl-awssdk-iam-dev",
            "roleArn": "arn:aws:iam::202949997891:role/service-role/bys-shared-iam-cdpl-awssdk-iam-role",
            "artifactStore": {
                "type": "S3",
                "location": "bys-shared-s3-codeseries-awssdk-iam",
                "encryptionKey": {
                    "id": "arn:aws:kms:ap-northeast-2:202949997891:key/11112222-71d6-4265-9dab-111122223333",
                    "type": "KMS"
                }
            },
            "stages": [
                {
                    "name": "Source",
                    "actions": [
                        {
                            "name": "Source",
                            "actionTypeId": {
                                "category": "Source",
                                "owner": "AWS",
                                "provider": "CodeCommit",
                                "version": "1"
                            },
                            "runOrder": 1,
                            "configuration": {
                                "BranchName": "develop",
                                "OutputArtifactFormat": "CODE_ZIP",
                                "PollForSourceChanges": "false",
                                "RepositoryName": "awssdk-iam"
                            },
                            "outputArtifacts": [
                                {
                                    "name": "SourceArtifact"
                                }
                            ],
                            "inputArtifacts": [],
                            "region": "ap-northeast-2",
                            "namespace": "SourceVariables"
                        }
                    ]
                },
                {
                    "name": "Build",
                    "actions": [
                        {
                            "name": "Build",
                            "actionTypeId": {
                                "category": "Build",
                                "owner": "AWS",
                                "provider": "CodeBuild",
                                "version": "1"
                            },
                            "runOrder": 1,
                            "configuration": {
                                "EnvironmentVariables": "[{\"name\":\"COMMIT_ID\",\"value\":\"#{SourceVariables.CommitId}\",\"type\":\"PLAINTEXT\"},{\"name\":\"BRANCH_NAME\",\"value\":\"#{SourceVariables.BranchName}\",\"type\":\"PLAINTEXT\"}]",
                                "ProjectName": "bys-shared-cdb-awssdk-iam-dev"
                            },
                            "outputArtifacts": [
                                {
                                    "name": "BuildArtifact"
                                }
                            ],
                            "inputArtifacts": [
                                {
                                    "name": "SourceArtifact"
                                }
                            ],
                            "region": "ap-northeast-2",
                            "namespace": "BuildVariables"
                        }
                    ]
                }
            ],
            "version": 2
        }
    }
    ```
    여기 까지 완료하면 shared 계정에 CodeCommit, CodeBuild를 구성하고 CodePipeline을 통해 소스가 커밋되면 빌드하는 단계까지 구성이 완료되었다.  
    지금 부터는 4. CodeDeploy를 구성을 먼저하고, 다시 이 단계로 돌아온다.  

3. [CodePipeline with resources from another AWS account](https://docs.aws.amazon.com/codepipeline/latest/userguide/pipelines-create-cross-account.html)
cross account 설정을 위한 순서는 아래와 같으며 자세한 내용은 링크를 참고한다.  
   - Shared계정에 KMS키를 생성하고 Define Key Usage Permissions로는 Codepipeline의 ServiceRole을 등록한다.  
   - Shared계정에 있는 S3에 Dev계정의 CodeDeploy가 접근해야 하므로 S3 Bucket Policy에 다음과 같이 CodeDeploy가 접근할 수 있도록 한다. S3는 558846431111:root에서 접근할 수 있도록 열어준다.  
   ```json
   {
       "Version": "2012-10-17",
       "Id": "SSEAndSSLPolicy",
       "Statement": [
           {
               "Sid": "DenyUnEncryptedObjectUploads",
               "Effect": "Deny",
               "Principal": "*",
               "Action": "s3:PutObject",
               "Resource": "arn:aws:s3:::bys-shared-s3-codeseries-awssdk-iam/*",
               "Condition": {
                   "StringNotEquals": {
                       "s3:x-amz-server-side-encryption": "aws:kms"
                   }
               }
           },
           {
               "Sid": "DenyInsecureConnections",
               "Effect": "Deny",
               "Principal": "*",
               "Action": "s3:*",
               "Resource": "arn:aws:s3:::bys-shared-s3-codeseries-awssdk-iam/*",
               "Condition": {
                   "Bool": {
                       "aws:SecureTransport": "false"
                   }
               }
           },
           {
               "Sid": "",
               "Effect": "Allow",
               "Principal": {
                   "AWS": "arn:aws:iam::558846431111:root"
               },
               "Action": [
                   "s3:Get*",
                   "s3:Put*"
               ],
               "Resource": "arn:aws:s3:::bys-shared-s3-codeseries-awssdk-iam/*"
           },
           {
               "Sid": "",
               "Effect": "Allow",
               "Principal": {
                   "AWS": "arn:aws:iam::558846431111:root"
               },
               "Action": "s3:ListBucket",
               "Resource": "arn:aws:s3:::bys-shared-s3-codeseries-awssdk-iam"
           }
       ]
   }
   ```

   - bys-dev-iam-create-cdp-in-shared CrossAccount Role설정한다. 코드파이프라인에서 CodeDeploy단계의 Role로 사용된다.   
    ```json
    {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Action": [
                    "s3:Get*"
                ],
                "Resource": [
                    "arn:aws:s3:::bys-shared-s3-codeseries-awssdk-iam/*"
                ]
            },
            {
                "Effect": "Allow",
                "Action": [
                    "s3:ListBucket"
                ],
                "Resource": [
                    "arn:aws:s3:::bys-shared-s3-codeseries-awssdk-iam"
                ]
            }
        ]
    }
    {
        "Version": "2012-10-17",
        "Statement": [
        {
            "Effect": "Allow",
            "Action": [
            "kms:DescribeKey",
            "kms:GenerateDataKey*",
            "kms:Encrypt",
            "kms:ReEncrypt*",
            "kms:Decrypt"
            ],
            "Resource": [
            "arn:aws:kms:ap-northeast-2:202949997891:key/111122222-71d6-4265-9dab-111122223333"
            ]
        }
    ]
    }
    {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Sid": "VisualEditor0",
                "Effect": "Allow",
                "Action": "iam:PassRole",
                "Resource": "*"
            }
        ]
    }
    {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Action": [
                    "ecs:RegisterTaskDefinition"
                ],
                "Resource": "*"
            }
        ]
    }
    {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Action": [
                    "codedeploy:CreateDeployment",
                    "codedeploy:GetDeployment",
                    "codedeploy:GetDeploymentConfig",
                    "codedeploy:GetApplication",
                    "codedeploy:GetApplicationRevision",
                    "codedeploy:RegisterApplicationRevision"
                ],
                "Resource": "*"
            }
        ]
    }
    ```

   - Codepipeline 편집 기존 `cdpl-dev.json` 파일에 아래의 내용을 추가한다. 
   provider는 CodeDeployToECS가 되어야지만 CodeDeploy를 이용한 ECS Blue/Green배포가 가능하다. 자세한 내용은 [CodeDeploy blue-green](https://docs.aws.amazon.com/codepipeline/latest/userguide/action-reference-ECSbluegreen.html)를 참고한다.  
   ```json
               {
                   "name": "DevDeploy",
                   "actions": [
                       {
                           "inputArtifacts": [
                               {
                                   "name": "BuildArtifact"
                               }
                           ],
                           "name": "DevDeploy",
                           "actionTypeId": {
                               "category": "Deploy",
                               "owner": "AWS",
                               "version": "1",
                               "provider": "CodeDeployToECS"
                           },
                           "outputArtifacts": [],
                           "configuration": {
                               "AppSpecTemplateArtifact": "BuildArtifact",
                               "AppSpecTemplatePath": "appspec.yml",
                               "TaskDefinitionTemplateArtifact": "BuildArtifact",
                               "TaskDefinitionTemplatePath": "taskdef.json",
                               "ApplicationName": "bys-dev-cddp-ecs-main-awssdk-iam",
                               "DeploymentGroupName": "bys-dev-cddpg-ecs-main-awssdk-iam"
                           },
                           "runOrder": 1,
                           "roleArn": "arn:aws:iam::558846431111:role/bys-dev-iam-create-cdp-in-shared"
                       }
                   ]
               }
   ```

   - Codepipeline을 업데이트한다.  
   ```bash
   aws codepipeline update-pipeline --cli-input-json file://cdpl-dev.json
   ```

## 4. [CodeDeploy](https://docs.aws.amazon.com/codedeploy/latest/userguide/deployment-configurations.html)

1. Application생성 
Compute platform을 Amazon ECS로 생성 

2. Deployment Group생성

![codedeploy001](/assets/it/cloud/codeseries/codedeploy001.png){: width="70%" height="auto"}  

<br>

## 5. TroublesShooting
파이프라인을 구성하면서 겪었던 여러가지 문제들...

#### 5.1. An AppSpec file is required, but could not be found in the revision
해당 오류는 Dev계정의 CodeDeploy에서 발생한 오류다.  
Codepipeline에서 Deploy단계를 추가할 때 아래와 같이 provider를 CodeDeploy로 주었을 때 발생한 오류다. provider는 CodeDeployToECS가 되어야지만 CodeDeploy를 이용한 ECS Blue/Green배포가 가능하다.  
따라서 provider를 'CodeDeploy'에서 'CodeDeployToECS'로 변경했다. 자세한 내용은 [CodeDeploy blue-green](https://docs.aws.amazon.com/codepipeline/latest/userguide/action-reference-ECSbluegreen.html)를 참고한다.  
```json
"actionTypeId": {
    "category": "Deploy",
    "owner": "AWS",
    "version": "1",
    "provider": "CodeDeploy"
}

"actionTypeId": {
    "category": "Deploy",
    "owner": "AWS",
    "version": "1",
    "provider": "CodeDeployToECS"
}
```
이 부분에서 CodeDeployToECS를 하는게 맞는건지 많은 의심이 있었지만 결국 맞았다.  

#### 5.2. You are missing permissions to access input artifact: BuildArtifact.
CodeDeployToECS로 provider를 변경하고 나서 CodePipeline에서 발생한 오류다. CodeDeploy를 호출하지 못하였다.  
해당 오류가 발생한 이유는 권한에 대한 혼동을 했기 때문이다. 이번 설정에서 Codepipeline(Shared Account), CodeDeploy(Dev Account)에서 설정된 Role은 아래와 같이 3개다. 
1. CodePipeline Role - bys-shared-iam-cdpl-awssdk-iam-role
2. CodeDeploy Group Role - bys-dev-iam-cdp-role
3. CodePipeline에서 Deploy단계에 설정한 Role - bys-dev-iam-create-cdp-in-shared

![crossaccount-cdpl001](/assets/it/cloud/codeseries/crossaccount-cdpl001.png){: width="100%" height="auto"}  

이 중 CodePipeline의 단계를 수행하는 Role은 bys-dev-iam-create-cdp-in-shared Role이다. 해당 오류를 겪을 때 정확히 어떤 권한에서 어떤 Permission에 대한 문제가 있는지 나오지 않았다.  
하지만 소스코드를 본 결과 정확하게 S3에 403오류로 인한 메세지가 나타나는 것이었고 3개의 권한에 순서대로 S3권한을 주어가며 테스트 해봤지만 모두 동일한 오류가 발생했다. 그래서 S3의 권한 문제인지 헷갈리기 시작했었다.  
모든 Permission을 제거한 뒤 다시 Admin권한을 순서대로 부여 해 보며 테스트 한 결과 bys-dev-iam-create-cdp-in-shared 권한에서 오류가 발생하는 것임을 찾았다. 그리고 최종적으로는 아래와 같은 권한들이 필요했다. 각각은 Inline-Policy로 적용했다.  
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:Get*"
            ],
            "Resource": [
                "arn:aws:s3:::bys-shared-s3-codeseries-awssdk-iam/*"
            ]
        },
        {
            "Effect": "Allow",
            "Action": [
                "s3:ListBucket"
            ],
            "Resource": [
                "arn:aws:s3:::bys-shared-s3-codeseries-awssdk-iam"
            ]
        }
    ]
}
{
    "Version": "2012-10-17",
    "Statement": [
      {
        "Effect": "Allow",
        "Action": [
           "kms:DescribeKey",
           "kms:GenerateDataKey*",
           "kms:Encrypt",
           "kms:ReEncrypt*",
           "kms:Decrypt"
          ],
        "Resource": [
           "arn:aws:kms:ap-northeast-2:202949997891:key/11112222-71d6-4265-9dab-111122223333"
          ]
      }
   ]
}
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "VisualEditor0",
            "Effect": "Allow",
            "Action": "iam:PassRole",
            "Resource": "*"
        }
    ]
}
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ecs:RegisterTaskDefinition"
            ],
            "Resource": "*"
        }
    ]
}
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "codedeploy:CreateDeployment",
                "codedeploy:GetDeployment",
                "codedeploy:GetDeploymentConfig",
                "codedeploy:GetApplication",
                "codedeploy:GetApplicationRevision",
                "codedeploy:RegisterApplicationRevision"
            ],
            "Resource": "*"
        }
    ]
}
```



<br><br><br>

> Ref: https://docs.aws.amazon.com/codebuild/latest/userguide/build-env-ref-env-vars.html  
> Ref: https://docs.aws.amazon.com/codepipeline/latest/userguide/reference-variables.html  
> Ref: https://docs.aws.amazon.com/codebuild/latest/userguide/view-project-details.html#view-project-details-cli  
> Ref: https://docs.aws.amazon.com/codebuild/latest/userguide/create-project-cli.html#cli.badgeenabled  
> Ref: https://docs.aws.amazon.com/codepipeline/latest/userguide/pipelines-create.html#pipelines-create-cli