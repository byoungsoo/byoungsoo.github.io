iss: 토큰 발급자 (issuer)
sub: 토큰 제목 (subject)
aud: 토큰 대상자 (audience)
exp: 토큰의 만료시간 (expiraton), 시간은 NumericDate 형식으로 되어있어야 하며 (예: 1480849147370) 언제나 현재 시간보다 이후로 설정되어있어야합니다.
nbf: Not Before 를 의미하며, 토큰의 활성 날짜와 비슷한 개념입니다. 여기에도 NumericDate 형식으로 날짜를 지정하며, 이 날짜가 지나기 전까지는 토큰이 처리되지 않습니다.
iat: 토큰이 발급된 시간 (issued at), 이 값을 사용하여 토큰의 age 가 얼마나 되었는지 판단 할 수 있습니다.
jti: JWT의 고유 식별자로서, 주로 중복적인 처리를 방지하기 위하여 사용됩니다. 일회용 토큰에 사용하면 유용합니다.


## 1. IRSA

1. SA -
   - eks.amazonaws.com/role-arn: arn:aws:iam::558846430793:role/AwsSdkIAMAppRole
2. mutatingwebhookconfiguration
   - pod-identity-webhook 
3. AWS_WEB_IDENTITY_TOKEN_FILE
   - /var/run/secrets/eks.amazonaws.com/serviceaccount/token
4. AssumeRoleWithWebIdentity with token
   - aws sts assume-role-with-web-identity --role-arn <value>  --role-session-name <value>  --web-identity-token <value>
5. Get temporary credntial


```bash
kubectl exec -it awssdk-iam-dev-deploy-767d486487-q7tmk -n aws -- cat  /var/run/secrets/eks.amazonaws.com/serviceaccount/token

eyJhbGciOiJSUzI1NiIsImtpZCI6ImM2MmE5NWQ5NzIxNGI0MTQ0ZWEwMDllNDgyZjNkNzQ3OWIxNDVmZmMifQ.eyJhdWQiOlsic3RzLmFtYXpvbmF3cy5jb20iXSwiZXhwIjoxNjk2Mjc1NTU3LCJpYXQiOjE2OTYxODkxNTcsImlzcyI6Imh0dHBzOi8vb2lkYy5la3MuYXAtbm9ydGhlYXN0LTIuYW1hem9uYXdzLmNvbS9pZC9BODg0NUQzRjBFNUMzODUyMjcyMDREMzNCODYzNUFCQyIsImt1YmVybmV0ZXMuaW8iOnsibmFtZXNwYWNlIjoiYXdzIiwicG9kIjp7Im5hbWUiOiJhd3NzZGstaWFtLWRldi1kZXBsb3ktNzY3ZDQ4NjQ4Ny1xN3RtayIsInVpZCI6IjJmMTFlY2M1LTgxMDgtNDFjYi05MmFmLTQ0MTExMDk3MmFlOCJ9LCJzZXJ2aWNlYWNjb3VudCI6eyJuYW1lIjoiYXdzc2RrLWlhbS1zYSIsInVpZCI6Ijk0NzA2MzY4LTMzYjAtNGFhMS1hZTcwLTEyM2JjZTQ3ZjM0NCJ9fSwibmJmIjoxNjk2MTg5MTU3LCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6YXdzOmF3c3Nkay1pYW0tc2EifQ.p0SmXuX_v4I7eHA-111DAgHOxGJi7AZpfoKfi_5G7WHuVTHPAN6jCeLxOAcBwilKL54ezEonTQLYD1yG_j63UwAiKV0rWMo8-uDC2aoN88y84tlJWavZGyvWzcH8quQQu3vDmMlQ20djwypezAR8gym3Rzh8dmydDHpNDrZJ7lPDDmjGiP0-Ao8gMZZU9NBhP4ZQQHtwgJTZEQ58NFq9zSdRDFlqwSYefmRNsfPC0PngK2dwcSKmIQkSzl7RFGxVj8GyJd8XczRiT_cfbpgsifQXbDTAtMKZ6uv0F4L6xjmqOj2-uXAp4CqIa7e4q9_5RleHHB3dUbQZ7NSNEirRRw
```

`JWT Token`  
```json
{
  "alg": "RS256",
  "kid": "c62a95d97214b4144ea009e482f3d7479b145ffc"
}

{
  "aud": [
    "sts.amazonaws.com"
  ],
  "exp": 1696275557,
  "iat": 1696189157,
  "iss": "https://oidc.eks.ap-northeast-2.amazonaws.com/id/A8845D3F0E5C385227204D33B8635ABC",
  "kubernetes.io": {
    "namespace": "aws",
    "pod": {
      "name": "awssdk-iam-dev-deploy-767d486487-q7tmk",
      "uid": "2f11ecc5-8108-41cb-92af-441110972ae8"
    },
    "serviceaccount": {
      "name": "awssdk-iam-sa",
      "uid": "94706368-33b0-4aa1-ae70-123bce47f344"
    }
  },
  "nbf": 1696189157,
  "sub": "system:serviceaccount:aws:awssdk-iam-sa"
}
```


https://aws.amazon.com/ko/blogs/opensource/introducing-fine-grained-iam-roles-service-accounts/