---
layout: post
title: "Install Nexus"
author: "Bys"
category: solution
date: 2021-12-27 01:00:00
tags: cicd nexus
---

#### - Nexus를 위한 OS계정 등록
```bash
groupadd nexus
useradd -g nexusadm -m nexusadm
```   
<br>

#### - Install Nexus  
공식 홈페이지를 통해 운영체제에 맞는 버전을 다운로드 받는다  
https://help.sonatype.com/repomanager3/product-information/download  

```bash
mkdir -p /nexus/
cp nexus-3.37.1-01-unix.tar.gz /nexus
tar zxvf /nexus/nexus-3.37.1-01-unix.tar.gz
cd /app/nexus
ln -s nexus-3.37.1-01 nexus
```

<br>

아래의 경로에서 Port를 설정해준다  
`/app/nexus/nexus/etc/nexus-default.properties`
```bash
## DO NOT EDIT - CUSTOMIZATIONS BELONG IN $data-dir/etc/nexus.properties
##
# Jetty section
application-port=8090
application-host=0.0.0.0
nexus-args=${jetty.etc}/jetty.xml,${jetty.etc}/jetty-http.xml,${jetty.etc}/jetty-requestlog.xml
nexus-context-path=/

# Nexus section
nexus-edition=nexus-pro-edition
nexus-features=\
 nexus-pro-feature

nexus.hazelcast.discovery.isEnabled=true
```

`/app/nexus/nexus/bin/nexus.rc`
```bash
run_as_user="deploy"
```

#### - Nginx 연동  
`/etc/nginx/conf.d/nexus.conf`  
```bash
server {
        listen 443 ssl;
        server_name nexus.example.com;

        ssl_certificate /etc/nginx/ssl/cert.pem;
        ssl_certificate_key /etc/nginx/ssl/key.pem;
        # Set cipher suite
        ssl_ciphers "ECDHE-RSA-AES256-GCM-SHA384:ECDHE-RSA-AES128-GCM-SHA256:DHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES128-GCM-SHA256:ECDHE-RSA-AES256-SHA384:ECDHE-RSA-AES128-SHA256:ECDHE-RSA-AES256-SHA:ECDHE-RSA-AES128-SHA:DHE-RSA-AES256-SHA256:DHE-RSA-AES128-SHA256:DHE-RSA-AES256-SHA:DHE-RSA-AES128-SHA:AES256-GCM-SHA384:AES128-GCM-SHA256:AES256-SHA256:AES128-SHA256:AES256-SHA:AES128-SHA";
        ssl_prefer_server_ciphers  on;
        # Only Allow TLSv1.2
        ssl_protocols TLSv1.2;

        error_log /var/log/nginx/nexus.example.com.error.log warn;
        access_log /var/log/nginx/nexus.example.com.access.log main;

        root /etc/nginx/sites-available;
        index index.html;

        location / {
                autoindex off;
                proxy_set_header Host $host;
                proxy_set_header X-Real-IP $remote_addr;
                proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                proxy_http_version 1.1;
                proxy_set_header Upgrade $http_upgrade;
                proxy_set_header Connection "Upgrade";
                proxy_buffering off;
                proxy_request_buffering off;

                proxy_pass http://localhost:8090;
        }
}
```

`/etc/nginx/conf.d/nexusRedirect.conf`  
```bash
server {
        listen 80;
        server_name nexus.example.com;

        root /etc/nginx/sites-available;
        index index.html;

        location / {
                autoindex off;
                return 301 https://$host$request_uri;
        }
}
```



#### - Nginx 사용법  
