---
layout: post
title: "AWS Presigned URL"
author: "Bys"
category: cloud
date: 2024-09-12 01:00:00
tags: aws
---

## Presigned URL 
Presigned URL 은 제한된 시간 동안 특정 객체에 대한 접근 권한을 부여하는 특수 URL 이다. AWS 보안 자격 증명(Security Credentials)을 가진 사용자가 생성하며, 수신자는 AWS 계정이나 IAM 자격 증명 없이도 지정된 작업(GET, PUT 등)을 수행할 수 있다.  


### Presigned URL 에 대한 이해  
일반적으로 AWS SDK 또는 CLI를 사용하여 AWS 에 API를 요청 하는 경우 SDK 및 CLI 클라이언트는 사용자 요청 인증에 사용자가 제공한 액세스 키를 사용하므로 서명 프로세스를 건너뛸 수 있다[1]. 따라서, 일반적인 상황에서는 항상 SDK 또는 CLI를 사용하는게 바람직하다.  다만, 특수상황에서 Presigned URL을 사용해야 하는 경우가 있다. 예를 들어, S3의 경우 특정 기간동안 데이터를 다운로드하도록 Public 제공을 해야 하는 경우 제공자가 Presigned URL 을 생성하여 사용자에게 제공해줄 수 있다. S3의 경우는 presigned URL을 생성하는 함수를 제공하지만 다른 서비스를 통해 Presigned URL이 만들어지는 과정을 확인해보고 그 내용을 자세히 이해해보기로 한다.  


먼저, SigV4를 알아야한다. AWS Signature Version 4(SigV4)는 AWS API 요청에 인증 정보를 추가하기 위한 AWS 서명 프로토콜이다. SDK가 제공되지 않는 등의 사유로 인해서 직접 REST API를 호출해야 하는 경우, HTTP/HTTPS 요청은 메시지에 대한 무결성과 인증을 보장해야하고 서명처리가 되어 있어야 한다[2]. 이 때 각 요청에 서명을 추가하는 방법은 아래 두 가지 방법 중 하나를 선택할 수 있다:
1. HTTP 인증 헤더: HTTP 인증 헤더를 사용하여 서명을 추가합니다.  
2. 쿼리 문자열 매개변수: 서명을 쿼리 문자열 값으로 추가합니다. 요청 서명이 그 URL의 일부이기 때문에 이런 종류의 URL을 미리 서명된(pre-signed) URL이라고 지칭합니다.  

1번을 예로 들면, iam.amazonaws.com의 ListUser를 조회하기 위한 동작이며 Authorization 헤더에는 Signature가 반드시 포함되어야 한다.   
```
GET https://iam.amazonaws.com/?Action=ListUsers&Version=2010-05-08 HTTP/1.1
Authorization: AWS4-HMAC-SHA256 Credential=AKIDEXAMPLE/20150830/us-east-1/iam/aws4_request, SignedHeaders=content-type;host;x-amz-date, Signature=5d672d79c15b13162d9279b0855cfba6789a8edb4c82c400e06b5924a6f2b5d7
content-type: application/x-www-form-urlencoded; charset=utf-8
host: iam.amazonaws.com
x-amz-date: 20150830T123600Z
```

<br>

이때 Signature 생성 방법을 간단히 보면 아래와 같다[3].  
1. Create a canonical request (표준 요청 생성)
요청 내용(호스트, 작업, 헤더 등)을 표준 형식으로 정렬한다. Canonical request는 서명할 문자열을 생성하는 데 사용되는 입력 중 하나이다.  

```
<HTTPMethod>\n
<CanonicalURI>\n
<CanonicalQueryString>\n
<CanonicalHeaders>\n
<SignedHeaders>\n
<HashedPayload>
```

2. Create a hash of the canonical request (표준 요청에 대한 해시 생성)
'AWS secret access key'를 초기 해시 작업에 대한 키로 사용하여 요청 날짜, 리전 및 서비스에 대한 키가 추가된 일련의 해시 작업(HMAC 작업)을 수행하여 서명 키를 추출한다.  

3. Create a string to sign (서명할 문자열 생성)
표준 요청과 추가 정보(예: 알고리즘, 요청 날짜, 자격 증명 범위, 표준 요청의 다이제스트(해시))를 사용하여 서명할 문자열을 생성한다.  

```
"AWS4-HMAC-SHA256" + "\n" +
timeStampISO8601Format + "\n" +
<Scope> + "\n" +
Hex(SHA256Hash(<CanonicalRequest>))
```

4. Calculate the signature (서명 계산)
서명 키를 만든 후 서명할 문자열에 대해 키가 추가된 해시 작업을 수행하여 서명을 계산합니다. 파생된 서명 키를 이 작업에 대한 해시 키로 사용합니다.

5. Add the signature to the request(요청에 서명 추가)
서명을 계산한 이후에 HTTP 헤더 또는 요청의 쿼리 문자열에 서명을 추가합니다.


<br>

또한 위 과정을 자동화 하기 위해 문서[4]에서는 쉘 스크립트를 제공하고 있습니다. 
간단히 그 내용을 살펴보면 Signature 생성을 설명한 위의 과정을 코드로 제공하고 있는 것을 알 수 있습니다.  
```
......생략 

HTTPMETHOD="GET"
CANONICAL_URI="/${S3KEY}"
#
CANONICAL_HEADERS="host:${HOST}\n"
SIGNED_HEADERS="host"
PAYLOAD_HASH="UNSIGNED-PAYLOAD"
#
ALGORITHM="AWS4-HMAC-SHA256"
CREDENTIAL_SCOPE="${DATESTAMP}/${REGION}/${SERVICENAME}/aws4_request"
#
CANONICAL_QUERYSTRING="X-Amz-Algorithm=${ALGORITHM}"
CANONICAL_QUERYSTRING="${CANONICAL_QUERYSTRING}&X-Amz-Credential=$(urlEncode "${AK}/${CREDENTIAL_SCOPE}")"
CANONICAL_QUERYSTRING="${CANONICAL_QUERYSTRING}&X-Amz-Date=${AMZ_DATE}"
CANONICAL_QUERYSTRING="${CANONICAL_QUERYSTRING}&X-Amz-Expires=${AMZ_EXPIRES}"
CANONICAL_QUERYSTRING="${CANONICAL_QUERYSTRING}&X-Amz-SignedHeaders=${SIGNED_HEADERS}"
CANONICAL_REQUEST="${HTTPMETHOD}\n${CANONICAL_URI}\n${CANONICAL_QUERYSTRING}\n${CANONICAL_HEADERS}\n${SIGNED_HEADERS}\n${PAYLOAD_HASH}"

# step 2. String To Sign
STRING_TO_SIGN="${ALGORITHM}\n${AMZ_DATE}\n${CREDENTIAL_SCOPE}\n$(getHexaHash "$(echo -e "${CANONICAL_REQUEST}")")"

# step 3. Signature
SIGNATURE="$(getSignatureKey $SK $DATESTAMP $REGION $SERVICENAME $STRING_TO_SIGN)"

# step 4.  Create a request URL
CANONICAL_QUERYSTRING="${CANONICAL_QUERYSTRING}&X-Amz-Signature=${SIGNATURE}"
```


<br>

### Presigned URL 만들기  
위 내용을 토대로 다음과 같이 만들 수 있다.  

`Latest Python`
```python
import hmac
from datetime import datetime
from hashlib import sha256
import requests


class SigV4Auth(object):
    def __init__(self, access_key, secret_key, session_token=None):
        self.access_key = access_key
        self.secret_key = secret_key
        self.session_token = session_token


    def add_auth(self, request):
        timestamp = datetime.utcnow().strftime('%Y%m%dT%H%M%SZ')
        request.headers['X-Amz-Date'] = timestamp

        if self.session_token:
            request.headers['X-Amz-Security-Token'] = self.session_token

        # https://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html
        canonical_headers = ''.join('{0}:{1}\n'.format(k.lower(), request.headers[k]) for k in sorted(request.headers))
        signed_headers = ';'.join(k.lower() for k in sorted(request.headers))
        payload_hash = sha256(request.body.encode('utf-8')).hexdigest()
        canonical_request = '\n'.join([request.method, '/', '', canonical_headers, signed_headers, payload_hash])

        # https://docs.aws.amazon.com/general/latest/gr/sigv4-create-string-to-sign.html
        algorithm = 'AWS4-HMAC-SHA256'
        credential_scope = '/'.join([timestamp[0:8], 'us-east-1', 'sts', 'aws4_request'])
        canonical_request_hash = sha256(canonical_request.encode('utf-8')).hexdigest()
        string_to_sign = '\n'.join([algorithm, timestamp, credential_scope, canonical_request_hash])

        # https://docs.aws.amazon.com/general/latest/gr/sigv4-calculate-signature.html
        key = 'AWS4{0}'.format(self.secret_key).encode('utf-8')
        key = hmac.new(key, timestamp[0:8].encode('utf-8'), sha256).digest()
        key = hmac.new(key, 'us-east-1'.encode('utf-8'), sha256).digest()
        key = hmac.new(key, 'sts'.encode('utf-8'), sha256).digest()
        key = hmac.new(key, 'aws4_request'.encode('utf-8'), sha256).digest()
        signature = hmac.new(key, string_to_sign.encode('utf-8'), sha256).hexdigest()

        # https://docs.aws.amazon.com/general/latest/gr/sigv4-add-signature-to-request.html
        authorization = '{0} Credential={1}/{2}, SignedHeaders={3}, Signature={4}'.format(
            algorithm, self.access_key, credential_scope, signed_headers, signature)
        request.headers['Authorization'] = authorization



    def generate_sigv4_auth_request(header_value=None):
        """Helper function to prepare a AWS API request to subsequently generate a "AWS Signature Version 4" header.

        :param header_value: Vault allows you to require an additional header, X-Vault-AWS-IAM-Server-ID, to be present
            to mitigate against different types of replay attacks. Depending on the configuration of the AWS auth
            backend, providing a argument to this optional parameter may be required.
        :type header_value: str
        :return: A PreparedRequest instance, optionally containing the provided header value under a
            'X-Vault-AWS-IAM-Server-ID' header name pointed to AWS's simple token service with action "GetCallerIdentity"
        :rtype: requests.PreparedRequest
        """

        request = requests.Request(
            method='POST',
            url='https://sts.amazonaws.com/',
            headers={'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8', 'Host': 'sts.amazonaws.com'},
            data='Action=GetCallerIdentity&Version=2011-06-15',
        )
        # if header_value:
        #     request.headers['X-Vault-AWS-IAM-Server-ID'] = header_value
        prepared_request = request.prepare()

        return 0
```

`Script - sts.sh`  
```bash
#!/bin/bash

# ./apists.sh -e 60

### Main ###
# if [ -z $AWS_DEFAULT_REGION ] || [ -z $AWS_SECRET_ACCESS_KEY ] || [ -z $AWS_ACCESS_KEY_ID ]
# then
#   echo "Please set $AWS_DEFAULT_REGION, $AWS_SECRET_ACCESS_KEY, and $AWS_ACCESS_KEY_ID environment variables"
# exit 1
# fi
SK=""
AK=""
REGION="ap-northeast-2"

SERVICENAME="sts"
HOST="${SERVICENAME}.amazonaws.com"
ENDPOINT="https://${SERVICENAME}.amazonaws.com"


export PATH="/usr/local/ssl/bin:$PATH"
urlEncode() {
  LINE="$1"
  LENGTH="${#LINE}"
  I=0
  while [ $I -lt $LENGTH ]
    do
    C="${LINE:I:1}"
  case $C in
    [a-zA-Z0-9.~_-]) printf "$C" ;;
    *) printf '%%%02X' "'$C" ;;
  esac
    let I=I+1
  done
}

getHexaDecimalString() {
  read LINE
  LENGTH="${#LINE}"
  I=0
  while [ $I -lt $LENGTH ]
    do
    C="${LINE:I:1}"
   printf '%2x' "'$C"
   let I=I+1
  done
}

getSignatureKey() {
  SECRET_KEY=$1
  DATESTAMP=$2
  REGIONNAME=$3
  SERVICENAME=$4
  STRING_TO_SIGN=$5

  HEX_KEY=$(echo -n "AWS4${SECRET_KEY}" | getHexaDecimalString)
  HEX_KEY=$(echo -n "${DATESTAMP}" | openssl dgst -sha256 -mac HMAC -macopt hexkey:${HEX_KEY})
  HEX_KEY=$(echo -n "${REGIONNAME}" | openssl dgst -sha256 -mac HMAC -macopt hexkey:${HEX_KEY#* })
  HEX_KEY=$(echo -n "${SERVICENAME}" | openssl dgst -sha256 -mac HMAC -macopt hexkey:${HEX_KEY#* })
  SIGNING_KEY=$(echo -n "aws4_request" | openssl dgst -sha256 -mac HMAC -macopt hexkey:${HEX_KEY#* })

  SIGNATURE=$(echo -en "${STRING_TO_SIGN}" | openssl dgst -binary -hex -sha256 -mac HMAC -macopt hexkey:${SIGNING_KEY#* })
  echo "${SIGNATURE#* }"
} 

getHexaHash() {
  PAYLOAD="$@"
  HASH=$(echo -n "${PAYLOAD}" | openssl dgst -sha256)
  echo  "${HASH#* }"
}


while getopts ":p:e:" OPT; do
  case $OPT in
        p)
         URI=$OPTARG
         ;;
        e)
         EXPIRES=$OPTARG
         ;;
       *)
   echo "Invalid option: -$OPTARG" >&2
   exit 3
   ;;
   esac
done


# step 1. Create a Canonical request
AMZ_DATE=$(date -u +%Y%m%dT%H%M%SZ)
DATESTAMP=$(date -u +%Y%m%d)
AMZ_EXPIRES=$((${EXPIRES}*60))   # minute -> second

HTTPMETHOD="POST"
CANONICAL_URI="/${URI}"
#
CANONICAL_HEADERS="host:${HOST}\n"
SIGNED_HEADERS="host"
PAYLOAD_HASH="UNSIGNED-PAYLOAD"
#
ALGORITHM="AWS4-HMAC-SHA256"
CREDENTIAL_SCOPE="${DATESTAMP}/${REGION}/${SERVICENAME}/aws4_request"
#
CANONICAL_QUERYSTRING="Action=&Version=2011-GetCallerIdentity06-15"
CANONICAL_QUERYSTRING="${CANONICAL_QUERYSTRING}&X-Amz-Algorithm=${ALGORITHM}"
CANONICAL_QUERYSTRING="${CANONICAL_QUERYSTRING}&X-Amz-Credential=$(urlEncode "${AK}/${CREDENTIAL_SCOPE}")"
CANONICAL_QUERYSTRING="${CANONICAL_QUERYSTRING}&X-Amz-Date=${AMZ_DATE}"
CANONICAL_QUERYSTRING="${CANONICAL_QUERYSTRING}&X-Amz-Expires=${AMZ_EXPIRES}"
CANONICAL_QUERYSTRING="${CANONICAL_QUERYSTRING}&X-Amz-SignedHeaders=${SIGNED_HEADERS}"
CANONICAL_REQUEST="${HTTPMETHOD}\n${CANONICAL_URI}\n${CANONICAL_QUERYSTRING}\n${CANONICAL_HEADERS}\n${SIGNED_HEADERS}\n${PAYLOAD_HASH}"

# step 2. String To Sign
STRING_TO_SIGN="${ALGORITHM}\n${AMZ_DATE}\n${CREDENTIAL_SCOPE}\n$(getHexaHash "$(echo -e "${CANONICAL_REQUEST}")")"

# step 3. Signature
SIGNATURE="$(getSignatureKey $SK $DATESTAMP $REGION $SERVICENAME $STRING_TO_SIGN)"

# step 4.  Create a request URL
CANONICAL_QUERYSTRING="${CANONICAL_QUERYSTRING}&X-Amz-Signature=${SIGNATURE}"

echo "request_url = ${ENDPOINT}/${URI}?${CANONICAL_QUERYSTRING}"
```




`apis3.sh`

```bash
#!/bin/bash

# ./apis3.sh -e 60 -b bys-dev-s3-temp -k awssdk-iam.tar

### Main ###
# if [ -z $AWS_DEFAULT_REGION ] || [ -z $AWS_SECRET_ACCESS_KEY ] || [ -z $AWS_ACCESS_KEY_ID ]
# then
#   echo "Please set $AWS_DEFAULT_REGION, $AWS_SECRET_ACCESS_KEY, and $AWS_ACCESS_KEY_ID environment variables"
# exit 1
# fi
# SK="$AWS_SECRET_ACCESS_KEY"
# AK="$AWS_ACCESS_KEY_ID"
# REGION="$AWS_DEFAULT_REGION"
SK=""
AK=""
REGION="ap-northeast-2"

SERVICENAME="s3"
HOST="${BUCKET}.${SERVICENAME}.amazonaws.com"
ENDPOINT="https://${BUCKET}.${SERVICENAME}.amazonaws.com"



export PATH="/usr/local/ssl/bin:$PATH"
urlEncode() {
  LINE="$1"
  LENGTH="${#LINE}"
  I=0
  while [ $I -lt $LENGTH ]
    do
    C="${LINE:I:1}"
  case $C in
    [a-zA-Z0-9.~_-]) printf "$C" ;;
    *) printf '%%%02X' "'$C" ;;
  esac
    let I=I+1
  done
}

getHexaDecimalString() {
  read LINE
  LENGTH="${#LINE}"
  I=0
  while [ $I -lt $LENGTH ]
    do
    C="${LINE:I:1}"
   printf '%2x' "'$C"
   let I=I+1
  done
}

getSignatureKey() {
  SECRET_KEY=$1
  DATESTAMP=$2
  REGIONNAME=$3
  SERVICENAME=$4
  STRING_TO_SIGN=$5

  HEX_KEY=$(echo -n "AWS4${SECRET_KEY}" | getHexaDecimalString)
  HEX_KEY=$(echo -n "${DATESTAMP}" | openssl dgst -sha256 -mac HMAC -macopt hexkey:${HEX_KEY})
  HEX_KEY=$(echo -n "${REGIONNAME}" | openssl dgst -sha256 -mac HMAC -macopt hexkey:${HEX_KEY#* })
  HEX_KEY=$(echo -n "${SERVICENAME}" | openssl dgst -sha256 -mac HMAC -macopt hexkey:${HEX_KEY#* })
  SIGNING_KEY=$(echo -n "aws4_request" | openssl dgst -sha256 -mac HMAC -macopt hexkey:${HEX_KEY#* })

  SIGNATURE=$(echo -en "${STRING_TO_SIGN}" | openssl dgst -binary -hex -sha256 -mac HMAC -macopt hexkey:${SIGNING_KEY#* })
  echo "${SIGNATURE#* }"
} 

getHexaHash() {
  PAYLOAD="$@"
  HASH=$(echo -n "${PAYLOAD}" | openssl dgst -sha256)
  echo  "${HASH#* }"
}

# [ $# -ne 6 ] && exit 2
 
while getopts ":b:k:e:" OPT; do
  case $OPT in
        b)
         BUCKET=$OPTARG
         ;;
        k)
         S3KEY=$OPTARG
         ;;
        e)
         EXPIRES=$OPTARG
         ;;
       *)
   echo "Invalid option: -$OPTARG" >&2
   exit 3
   ;;
   esac
done

# step 1. Create a Canonical request
AMZ_DATE=$(date -u +%Y%m%dT%H%M%SZ)
DATESTAMP=$(date -u +%Y%m%d)
AMZ_EXPIRES=$((${EXPIRES}*60))   # minute -> second

HTTPMETHOD="GET"
CANONICAL_URI="/${S3KEY}"
#
CANONICAL_HEADERS="host:${HOST}\n"
SIGNED_HEADERS="host"
PAYLOAD_HASH="UNSIGNED-PAYLOAD"
#
ALGORITHM="AWS4-HMAC-SHA256"
CREDENTIAL_SCOPE="${DATESTAMP}/${REGION}/${SERVICENAME}/aws4_request"
#
CANONICAL_QUERYSTRING="X-Amz-Algorithm=${ALGORITHM}"
CANONICAL_QUERYSTRING="${CANONICAL_QUERYSTRING}&X-Amz-Credential=$(urlEncode "${AK}/${CREDENTIAL_SCOPE}")"
CANONICAL_QUERYSTRING="${CANONICAL_QUERYSTRING}&X-Amz-Date=${AMZ_DATE}"
CANONICAL_QUERYSTRING="${CANONICAL_QUERYSTRING}&X-Amz-Expires=${AMZ_EXPIRES}"
CANONICAL_QUERYSTRING="${CANONICAL_QUERYSTRING}&X-Amz-SignedHeaders=${SIGNED_HEADERS}"
CANONICAL_REQUEST="${HTTPMETHOD}\n${CANONICAL_URI}\n${CANONICAL_QUERYSTRING}\n${CANONICAL_HEADERS}\n${SIGNED_HEADERS}\n${PAYLOAD_HASH}"

# step 2. String To Sign
STRING_TO_SIGN="${ALGORITHM}\n${AMZ_DATE}\n${CREDENTIAL_SCOPE}\n$(getHexaHash "$(echo -e "${CANONICAL_REQUEST}")")"

# step 3. Signature
SIGNATURE="$(getSignatureKey $SK $DATESTAMP $REGION $SERVICENAME $STRING_TO_SIGN)"

# step 4.  Create a request URL
CANONICAL_QUERYSTRING="${CANONICAL_QUERYSTRING}&X-Amz-Signature=${SIGNATURE}"

echo "request_url = ${ENDPOINT}/${S3KEY}?${CANONICAL_QUERYSTRING}"
```

---

## 📚 References
[1] **API 요청용 AWS Signature Version 4**  
- https://docs.aws.amazon.com/ko_kr/IAM/latest/UserGuide/reference_sigv.html

[2] **AWS API 호출**  
- https://aws.amazon.com/ko/blogs/korea/aws-api-call-1/

[3] **Create a signed AWS API request**  
- https://docs.aws.amazon.com/IAM/latest/UserGuide/reference_sigv-create-signed-request.html

[4] **AWS API 호출 - pre-signed URL 생성하기**  
- https://aws.amazon.com/ko/blogs/korea/aws-api-call-2-s3-pre-signed-url/