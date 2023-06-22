---
layout: post
title: "공개키 기반 SSH/HTTPS 접속 방식"
author: "Bys"
category: etc
date: 2023-05-07 01:00:00
tags: pki https ssh
---

### 1. HTTPS 인증 과정

![pki-https](/assets/it/etc/pki/pki-https.png){: width="90%" height="auto"}  

1. Client는 https 프로토콜을 통해 브라우저에서 접속을 원하는 서버로 요청
2. 서버는 자신의 인증서를 Client에게 전달 함
   - 인증서에는 다음과 같은 정보가 포함 됨
     - Issuer - 발급자 정보 
     - Signature - 발급자 서명
     - Validity - 유효기간
     - Subject - 소유자의 정보, DN형식 
     - SubjectPublicKeyInfo - 소유자의 공개키
3. Client는 브라우저에 내장된 CA의 공개키를 이용하여 Signature를 복호화 하므로써 이 인증서가 유효한 인증서인지를 확인 하게 됨
4. 유효한 인증서의 경우 Public key를 사용할 수 있으며 임시 대칭키를 하나 생성하여 Public key를 사용하여 암호화 진행
5. 암호화된 대칭키를 서버로 전송
6. 서버에서는 암호화된 대칭키를 Private key로 복호화 하여 대칭키를 확보 

이 프로세스를 거치고 나면 Client와 서버간 같은 대칭 키를 안전하게 확보할 수 있으며 암호화된 통신이 가능하게 된다.  

<br>

### 2. SSH 인증 과정





<br><br><br>

---

**Reference**  
- https://retro-blue.tistory.com/43
- https://ikcoo.tistory.com/359

---
