---
layout: post
title: "Project KoreanAir - Issue"
author: "Bys"
category: issue
date: 2020-01-02 01:00:00
tags: troubleshooting issue
---

#### **- NAS 동기화 이슈**  
상암 망(운영서버)에서 DR로 NAS동기화 작업 진행 되면서 httpd.conf등 파일에 하드 코딩 되어진 공인 IP 들이 상암 공인 IP 대역으로 변경 되게 됨. (DR망은 별도의 공인 IP가 있기 때문에 이로 복구가 필요함)  
현재 AppScan(?)을 통해 공인 IP를 전수 조사 후, 일괄 변경 예정이다.   

=> 해결방법 
1) 공인 IP 대역을 Domain 기반으로 변경하는 방법이 있으나, 어떠한 문제로 인해서 X  
2) 기존 설정 파일 백업 후, Recovery 작업을 진행 
DR 서버 공인 IP 변경 파일을 상암 서버 /was/script/dr/~ 경로에 먼저 백업을 한다. NAS 동기화가 완료되고 나면 상암에 존재하는 백업 파일들을 다시 그 대로 DR로 복사하여 Recovery 작업을 진행한다. 

<br>

#### **- mdas 제약사항**   
Mdas.koreanair.com 상세페이지 정보 조회가 안 되는 현상 (index query(?)) robocopy (동기화) 전 운영, DR서버의 솔루션을 내리고 동기화를 진행해야 하는 문제가 있다.  
(WEXBIPRDAP, WEXCMPRDAP)  

<br>

#### **- Airnavx Library**   
http://airnavx.koreanair.com:8000/airnavx/ 사이트 정상 기동 후 My Library를 정상적으로 불러오지 못 함.  
확인 결과 운영 서버 OS를 스냅샷 하여 그대로 복사 해 옴. 프로세스가 정상 기동 상태가 아님. Airnavx WEB/WAS 솔루션 재 기동 후 정상 처리. 
(ANAVPRDWB, ANAVPRDAP) 

<br>

#### **- Windows 서버 IIS 기동 관련**   
Application Test 점검 후, 몇 몇 서비스 들이 정상 기동 되지 않았다. 확인 해보니 윈도우 서버들이었다.  
윈도우 서버들의 상태를 확인 해보니 IIS 기동이 정상적으로 되지 않았다. (작업 관리자에서 Java.exe 프로세스 및 실행 경로 확인) 

MESPRDAP서버의 경우  
IIS기동 (mesadmin계정) 
TBR, CBR (mesadmin 계정) 
Airnavx (xadmuser 계정) 

<br>

#### **- SSO Proxy 서버**  
M2b.koreanair.com 의 경우 web/was 정상 기동 중이었으나, 접속이 안되었음 
SSO Proxy 서버가 있는 구조로 SSO Proxy 서버에서 M2B 관련된 무언가 기동이 되지 않았음. 
IAMPXPRDWB1 - 10.7.13.130 

<br>

#### **- LMMS 권한문제 (ERPPRDDB1)**  
lmms.koreanair.com의 경우 SSO로그인 이 후 SOA서비스를 통해 ERP DB에 가서 ERP권한을 받아오는 절차가 있다. 
때문에 lmms.koreanair.com의 경우 로그인 후 권한을 정상적으로 받아오지 못할 때는 ERPPRDB1 과 SOA서비스가 정상인지도 같이 확인을 해봐야 한다. 

두 번째로 ERPPRDAP 서버와 연계가 되어있어 ERPPRDAP서버 Web/WAS 재기동 후 정상 처리 
내용은 나오는데 work orders(), defer() 등 괄호 안에 숫자가 안나오는 경우 발생 -> erpprdap 서버 재 기동  
 
<br>

#### **- 서버시간 문제**  
데이터를 쫓아 올라가다보면 get Cookies하는 부분이 NULL로 떨어짐, set Cookies 하는 부분을 찾아 어떻게 가지고 오는지 봄. 
Cookie의 인증 만료일자가 서버시간의 GMT 로 날아왔지만 이미 만료된 인증임. IE, Chrome 등이 서로 다른 이유는 IE의 경우 해당 만료일자를 보고   
Chrome이나 Firefox와 같은 것들은 자신들의 기준에 따라 Max 5분까지 보는 것으로 보임  

-> 추가   

주의해야 하는 브라우저 특성  
IE, Microsoft Edge  
MaxGge 속성을 구현하지 않았다.  
Max e속성과 동일한 시간으로 계산된 Expires속성을 세팋해서 전송해야 한다.  
• RfC5255 CookieProcessor는 동일한 시간의 Max•age와 Expi「es를 모두 추가한다.  
• LegacyC00kieProcessor는 VO쿠카와 always add expires 설정을 활성화한 상하에 대해 Expires를 추가한다 (`71쿠 
키를 사용하려면 always add expires를 활성화할 것을 권장)  

 ![scouter](/assets/it/issue/koreanair/koaissue1.png){: width="90%" height="auto"}   

<br>

#### **- MQ 서비스**  
DR 테스트 중 MQ 서비스가 안되고 있다고 연락을 받았습니다.  
해당 사유는 1,2 번 서버 둘다 기동되어야 정상 서비스가 되는데, Unix 쪽에 cluster 구성이 되어있지 않아 1번 서버만 마운트 되고 있어서 발생하는 문제입니다. 이후 설정이 변경되어야 할지, 구성을 바꿔야 할지 김광준책임님과 협의할 사항입니다.  