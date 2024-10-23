`apists.sh`  

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