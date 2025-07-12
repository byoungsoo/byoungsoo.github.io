---
layout: post
title: "EKS KMS를 통한 암호화"
author: "Bys"
category: cloud
date: 2023-06-13 01:00:00
tags: kubernetes eks kms secret
---

## 1. Kubernetes Secret Encryption
쿠버네티스에서 secret은 일반적으로 패스워드나 API key와 같은 민감한 데이터를 저장할 때 사용한다. Secret 리소스를 생성하면 Kubernetes API서버는 secret을 base64로 인코딩한 형태로 etcd에 저장한다. 

## 2. EKS Encrypt with KMS
심층 보안을 위해 envelope encryption 이라고 하는 방법이 존재하며 이는 암호화를 위한 키를 또 다른 키로 암호화 하는 것을 의미한다. Kubernetes native에서는 EKS를 위해 사용할 수 있는 envelope encryption 방법이 존재하지 않는다. 
EKS Cluster내에서 secret을 저장할 때 AWS KMS를 통해 암호화 하여 저장하는 방법을 제공한다. 


### 3. [Install](https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/enable-kms.html)
`Create KMS Key`  
- Symmetric
- Can encrypt and decrypt data
- Created in the same AWS Region as the cluster
- If the KMS key was created in a different account, the IAM principal must have access to the KMS key.


`Enable encryption in EKS`  
```bash
eksctl utils enable-secrets-encryption \
    --cluster bys-dev-eks-test \
    --key-arn arn:aws:kms:ap-northeast-2:558846430793:key/5122a0b7-6051-4646-9447-b95ddd4d3408
```

### 4. [동작방법](https://aws.amazon.com/ko/blogs/containers/using-eks-encryption-provider-support-for-defense-in-depth/)

![eks-kms-encryption](/assets/it/cloud/eks/eks-kms-encryption.png){: width="80%" height="auto"}

1. User는 Kubectl 커맨드를 통해 Secret리소스를 생성.  
2. EKS API서버는 내부적으로 DEK(Data Encryption Key)를 생성하고 이를 사용하여 plaintext payload를 암호화.  
  > The Kubernetes API server in the control plane generates a DEK locally, and uses this to encrypt the plaintext payload in the secret. Note that we generate a unique DEK for every single write, and also that the plaintext DEK is never saved to disk.
3. EKS API 서버는 AWS API인 'kms:Encrypt'를 호출하며 DEK를 KMS 키를 사용해 암호화. 
4. EKS API 서버는 DEK로 암호화된 secret을 etcd에 저장. 
5. Secret을 파드 등에서 사용할 때는 EKS API서버에서 암호화된 secret을 읽고 DEK를 통해 복호화 하며 파드에서 실행되는 application은 평상시 처럼 secret을 사용.



---

## 📚 References

[1] **Workshop**  
- https://aws.amazon.com/ko/blogs/containers/using-eks-encryption-provider-support-for-defense-in-depth/
