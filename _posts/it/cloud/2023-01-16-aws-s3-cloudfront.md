---
layout: post
title: "AWS CodeBuild, S3, Cloudfront를 통한 웹 서비스 (Cross Account 및 권한설정)"
author: "Bys"
category: cloud
date: 2023-01-16 01:00:00
tags: aws codecommit codebuild s3 cloudfront 
---

# CodeBuild, S3, CloudFront를 통한 Web 서비스
여기서는 CodeBuild, S3, CloudFront를 통한 Web 서비스를 하는 방법에 대해서 알아볼 것이다.  
이번 글에서는 아래의 아키텍처를 구성하면서 S3의 권한과 CloudFront설정에 대한 부분을 중점적으로 확인해 보도록 한다.  

![s3-cloudfront-architecture](/assets/it/cloud/s3-cloudfront/s3-cloudfront-architecture.png){: width="100%" height="100%"}  

- 참고
여기서는 node.js를 통해 진행하므로 node.js를 미리 설치한다. 자세한 내용은 기타 설치문서 참고.
    - brew install nvm
    - nvm instasll v18.13.0

## 1. CodeCommit, CodeBuild 구성 

1. CI/CD 계정에 CodeCommit을 생성한다. `bys-shared-cdcm-s3cfn`  

2. CI/CD 계정에 CodeBuild를 생성한다. `bys-shared-cdb-s3cfn`
    - 코드빌드에 대한 자세한 구성은 [CodeSeries 구성](2022-12-16-ecs-codeseries.md)를 참고한다.  
    - `bys-shared-iam-cdb-role` IAM role은 'AWSCodeCommitFullAccess', 'AmazonS3FullAccess' 정책을 할당했다.  
        ```bash
        # Create Template
        aws codebuild create-project --generate-cli-skeleton > codebuild.json

        # Create CodeBuild project
        aws codebuild create-project --cli-input-json file://codebuild.json
        ```
        `codebuild.json` [Schema 참고](https://docs.aws.amazon.com/codebuild/latest/userguide/create-project-cli.html)
        ```json
        {
            "name": "bys-shared-cdb-s3cloudfront",
            "description": "bys-shared-cdb-s3cloudfront",
            "source": {
                "type": "CODECOMMIT",
                "location": "https://git-codecommit.ap-northeast-2.amazonaws.com/v1/repos/bys-shared-cdcm-s3cfn",
                "gitCloneDepth": 1,
                "gitSubmodulesConfig": {
                    "fetchSubmodules": false
                },
                "buildspec": "codeseries/codebuild/buildspec.yml",
                "insecureSsl": false
            },
            "secondarySources": [],
            "sourceVersion": "refs/heads/main",
            "secondarySourceVersions": [],
            "artifacts": {
                "type": "S3",
                "location": "bys-shared-s3-codeseries-cloudfront",
                "path": "/dev/artifacts",
                "name": "BuildArtifact"
            },
            "secondaryArtifacts": [],
            "cache": {
                "type": "S3",
                "location": "bys-shared-s3-codeseries-cloudfront/sample/dev/cahce"
            },
            "environment": {
                "type": "LINUX_CONTAINER",
                "image": "aws/codebuild/amazonlinux2-x86_64-standard:4.0",
                "computeType": "BUILD_GENERAL1_SMALL",
                "environmentVariables": [],
                "privilegedMode": false,
                "imagePullCredentialsType": "CODEBUILD"
            },
            "serviceRole": "arn:aws:iam::202949997891:role/service-role/bys-shared-iam-cdb-role",
            "timeoutInMinutes": 60,
            "queuedTimeoutInMinutes": 480,
            "encryptionKey": "arn:aws:kms:ap-northeast-2:202949997891:key/7d16264a-10b4-4b7e-bea6-1863ac0b50c1",
            "tags": [
                {
                    "key": "Name",
                    "value": "bys-shared-cdb-s3cloudfront"
                }
            ],
            "badgeEnabled": false,
            "logsConfig": {
                "cloudWatchLogs": {
                    "status": "ENABLED"
                },
                "s3Logs": {
                    "status": "ENABLED",
                    "location": "bys-shared-s3-codeseries-cloudfront/dev/logs",
                    "encryptionDisabled": false
                }
            }
        }
        ```

3. CodeBuild 소스 구성 
    - dist/index.html
    - codeseries/codebuild/buildspec.yml  
        `buildspec.yml`
        ```yaml
        version: 0.2
        phases:
        install:
            runtime-versions:
            nodejs: 16
            commands:
        build:
            commands:
            - aws s3 cp --recursive dist/ s3://bys-dev-s3-cloudfront-service
        ```

## 2. S3 구성

#### S3를 구성하면서 알아 두면 좋을 점  
1. S3는 region 서비스이며 bucket 명은 global unique해야 한다. 
2. S3는 내/외부 모두에서 접근이 가능하므로 Bucket Policy와 ACLs를 통해서 접근제어가 가능하다. 
3. ACLs Disabled(AWS 권장)인 경우 Bucket Policy 만으로 접근 제어가 이루어진다.  
4. S3의 Object Ownership는 ACLs의 사용여부에 따라 `ACLs Disabled -> Bucket owner enforced` / `ACLs Enabled -> Bucket owner preferred, Object writer`로 나뉜다. [Bucket Ownership](https://docs.aws.amazon.com/AmazonS3/latest/userguide/about-object-ownership.html)
    - ACLs Disabled(AWS 권장)인 경우 무조건 Bucker의 소유계정이 모든 업로드된 Object에 대해 소유권을 가지게 되며 Object들에 대한 접근제어는 bucket policy 설정을 통해 제어한다.  
    - ACLs Enabled인 경우 'Bucket owner preferred'와 'Object writer'로 Ownership을 선택할 수 있다.
    - 'Bucket owner preferred'가 설정되어있는 상황에서 다른 계정에서 `--acl bucket-owner-full-control` 옵션을 통해 bucket에 object를 쓰는 경우, bucket을 소유한 계정에서 새 객체를 소유하고 제어 할 수 있다. 
    - 'Object writer'가 설정되어있는 상황에서 다른 계정에서 업로드한 경우 object를 업로드한 writer가 소유권을 가진다. ACLs를 통해 Permission 부여는 가능하다.  
5. 소유권을 누가 가지고 있느냐에 따라 접근제어가 달라지므로 이는 매우 중요하다.  
6. [Canned ACL ](https://docs.aws.amazon.com/AmazonS3/latest/userguide/acl-overview.html#canned-acl)
7. 

--------------------------

#### S3 구성
1. CI/CD 계정에서 S3를 생성한다. `bys-shared-s3-codeseries-cloudfront`
이 S3는 CodeBuild에서 사용하는 S3용도이며 log, cache용도로 사용한다. 

2. DEV 계정에서 S3를 생성한다. `bys-dev-s3-cloudfront-service`  
이 계정의 S3는 CodeBuild에서 서비스될 resource들이 업로드 될 S3이며 Cloudfront를 통해 서비스 되는 S3다.  
    - ACLs disabled (recommended)
    - Check 'Block all public access'
    - Static website hosting enabled

2. bucket policy 설정 
    - CodeBuild에서 업로드가 가능해야 한다. CodeBuild가 사용하는 role에 대해서 's3:PutObject'가 가능해야 한다.  
        ```json
        {
            "Version": "2008-10-17",
            "Statement": [
                {
                    "Sid": "FromCodeBuild",
                    "Effect": "Allow",
                    "Principal": {
                        "AWS": "arn:aws:iam::202949997891:role/service-role/bys-shared-iam-cdb-role"
                    },
                    "Action": [
                        "s3:Delete*",
                        "s3:Put*"
                    ],
                    "Resource": [
                        "arn:aws:s3:::bys-dev-s3-cloudfront-service",
                        "arn:aws:s3:::bys-dev-s3-cloudfront-service/*"
                    ]
                }
            ]
        }
        ```
    - CloudFront의 OAC가 접근 가능해야 한다. CloudFront의 OAC에 대해서 's3:GetObject'가 가능해야 한다. [S3 Origin Access](https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/private-content-restricting-access-to-s3.html)  
        ```json
        {
            "Version": "2012-10-17",
            "Statement": {
                "Sid": "AllowCloudFrontServicePrincipalReadOnly",
                "Effect": "Allow",
                "Principal": {
                    "Service": "cloudfront.amazonaws.com"
                },
                "Action": "s3:GetObject",
                "Resource": "arn:aws:s3:::bys-dev-s3-cloudfront-service/*",
                "Condition": {
                    "StringEquals": {
                        "AWS:SourceArn": "arn:aws:cloudfront::558846430793:distribution/E1H6SYS9DTGCA"
                    }
                }
            }
        }
        ```


## 3. CloudFront 구성

1. Origin Domain - `bys-dev-s3-cloudfront-service.s3.ap-northeast-2.amazonaws.com`
2. Origin access
   - Origin access 생성 - Origin access controls 생성. `bys-dev-cloudfront-oac-s3cfn`
   - Origin access control settings (recommended) - `bys-dev-cloudfront-oac-s3cfn` 선택 후 생성
3. 생성 완료 후 위의 S3 bucket policy의 cloudfront정보를 업데이트한 후 S3에 반영


## 4. Test
1. CodeCommit - 소스 반영
2. CodeBuild - Run build
3. S3 - index.html 업로드 확인
4. Cloudfront - domain/index.html 주소를 통해 index.html 정상접속 확인


<br><br><br>

> [S3 cloudfront access policy](https://docs.aws.amazon.com/AmazonS3/latest/userguide/example-bucket-policies.html)
> [S3 Canned ACL](https://docs.aws.amazon.com/AmazonS3/latest/userguide/acl-overview.html#canned-acl)



