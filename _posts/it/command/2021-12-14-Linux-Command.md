---
layout: post
title: "Linux Command"
author: "Bys"
category: command
tags: linux command cli
---


`압축하기` 
```bash
tar -cpvf dr.tar dr 

-c 압축 
-p 권한유지 
-v 진행사항 
-f 파일이름변경 
```
 

`압축풀기`  
```bash
tar -xvf dr.tar 

-x 압축해제 
-v 진행사항 
-f 파일 이름 
```
 

`Jar 클래스 파일 검색`  
```bash
find . -type f -name '*.jar' | while read LINE;do echo $LINE; jar tvf $LINE | grep "log4j";done 
find . -type f -name '*.jar' | while read LINE;do echo $LINE; jar tvf $LINE;done 
```
 

 

`TCPDUMP`  
```bash
tcpdump -i eth0 -w test.pcap 
```
 

`SCP`  
```bash
scp httpd_imws.conf wasadm@10.43.43.15:/was/jbcs-httpd24-2.4/httpd/conf 
scp -Rp safenet wasadm@10.43.43.15:/was/jboss-eap-7.2/modules/com/safenet 
scp -rp com_kal_cms_war.ear dco3user@10.43.43.23:/home/dco3user 
```
 

`ChangeOwner`  
```bash
chown wasadmin:wasadmin dr 

# 기존 gid 3030 -> wasadm 변경 
find -group 3030 | while read line ; do chgrp wasadm "$line"; done 
# 기존 uid 3030 -> wasadm 변경 
find -user 3030 | while read line ; do chown wasadm "$line"; done 
```

`Change Mode`  
```bash
chmod 755 

-R 하위폴더 권한까지 변경 (755 rwxr-xr-x)
```


`문자, 라인, 단어 수`  
```bash
ps -ef | grep java | wc -l 

-c 전체 문자 수 출력 
-l 전체 라인 수 출력 
-w 전체 단어 수 출력 
```
 

`호스트에 장착된 네트워크 인터페이스의 통신 상태를 보여 줌`  
```bash
netstat -ano 

-a 활성화된 모든 TCP 연결 정보를 보여 줌 
-n 출력결과가 숫자형태가 되도록 함 (IP 주소, 포트 번호 등) 
-o 해당 포트를 연 프로세스 ID를 보여줌 
ㅣ…/ 
```
 

`다운로드`
```bash
wget https://archive.apache.org/dist/httpd/httpd-2.4.20.tar.gz 
```
 

`Kill`  
```bash
ps -ef | grep httpd | awk '{print $2}' | xargs kill -9 

ps -ef | grep travelmn | awk '{print $2}' | xargs kill -9 

ps -ef | grep lmms | grep httpd | awk '{print $2}' | xargs kill -9 
```
 

`Text검색`  
```bash
grep -rin JAVA_OPTS 

-b 검색 결과의 각 행 앞에 검색된 위치의 블록 번호를 표시한다. 검색 내용이 디스크의 어디쯤 있는지 위치를 알아내는데 유용하다. 
-c 검색 결과를 출력하는 대신, 찾아낸 행의 총수를 출력한다.  (count) 
-h 파일 이름을 출력하지 않는다. 
-I 대소문자를 구분 하지 않는다.(대문자와 소문자를 동일하게 취급). (ignore) 
-l 패턴이 존재하는 파일의 이름만 출력한다.(개행문자로 구분) (list file) 
-n 파일 내에서 행 번호를 함께 출력한다. (number) 
-s 에러 메시지 외에는 출력하지 않는다. 종료상태를 검사할 때 유용하게 쓸 수 있다. 
-v 패턴이 존재하지 않는 행만 출력한다. (invert) 
-w 패턴 표현식을 하나의 단어로 취급하여 검색한다. (word) 
```