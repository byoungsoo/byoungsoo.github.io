---
layout: post
title: "AWS EKS - External Secrets 사용하기"
author: "Bys"
category: incubator
date: 2023-12-21 01:00:00
tags: ecs aws
---


```
aws iam create-policy --policy-name bys-dev-iam-secrets-reader-policy --policy-document '{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "secretsmanager:ListSecrets",
                "secretsmanager:GetSecretValue"
            ],
            "Resource": [
                "*"
            ]
        }
    ]
}'
```
arn:aws:iam::558846430793:policy/bys-dev-iam-secrets-reader-policy


aws iam create-group --group-name secret-readers
aws iam attach-group-policy --policy-arn $POLICY_ARN --group-name secret-readers




1. SecretStore 정의
```yaml
apiVersion: external-secrets.io/v1alpha1
kind: SecretStore
metadata:
  name: my-secret-store
spec:
  provider:
    aws:  # set secretStore provider to AWS.
      service: SecretsManager # Configure service to be Secrets Manager
      region: ap-northeast-2   # Region where the secret is.
      role:  
    #   auth:
    #     secretRef:
    #       accessKeyIDSecretRef: 
    #         name: aws-secret # References the secret we created
    #         key: access-key  
    #       secretAccessKeySecretRef:
    #         name: aws-secret
    #         key: secret
```

2. ExternalSecret를 정의 -> SecretStore사용
```yaml
apiVersion: external-secrets.io/v1alpha1
kind: ExternalSecret
metadata:
  name: my-external-secret
spec:
  refreshInterval: 1m
  secretStoreRef:
    name: my-secret-store #The secret store name we have just created.
    kind: SecretStore
  target:
    name: my-kubernetes-secret # Secret name in k8s
  data:
  - secretKey: password # which key it's going to be stored
    remoteRef:
      key: super-secret # Our secret-name goes here

```


3. 






<br><br><br>

> Ref: https://docs.aws.amazon.com/codebuild/latest/userguide/build-env-ref-env-vars.html
> Ref: https://docs.aws.amazon.com/codepipeline/latest/userguide/reference-variables.html