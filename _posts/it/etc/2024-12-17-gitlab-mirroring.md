---
layout: post
title: "Gitlab mirroring을 통해 Github 으로 소스 Push"
author: "Bys"
category: etc
date: 2024-12-17 01:00:00
tags: git gitlab github
---

## Gitlab Mirroring 설정

1. Menu
  > Gitlab > Repository > Settings > Repository > Mirroring repositories


2. HTTPS Add new
- Git repository URL: 
  > https://github.com/byoungsoo/test.git
- Authentication method
  > Username and Password
- Username
  > Username
- Password
  > Token
  > Github repository 에서는 더 이상 패스워드를 이용한 HTTPS 인증을 지원하지 않기 때문에 이 입력란에 토큰을 입력한다.  


3. SSH Add new
- Git repository URL: 
  > ssh://git@github.com/byoungsoo/byoungsoo.github.io.git
  > 반드시 `:` 을 `/` 로 변경해주어야 한다.  
- Detect host key
  > 이 옵션을 선택하면 known_hosts의 public key가 자동으로 탐지 된다.  
- Authentication method
  > SSH public key
- Username
  > 입력하지 않음
- 생성
  > 생성 후, SSH public key가 생성된다.  
  > Github의 SSH 메뉴로 이동해서 SSH Keys를 등록한다.  


<br><br><br>
