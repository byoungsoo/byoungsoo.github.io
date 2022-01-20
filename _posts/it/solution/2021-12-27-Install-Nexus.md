---
layout: post
title: "Install Nexus"
author: "Bys"
category: solution
date: 2021-12-27 01:00:00
tags: cicd nexus
---

### - Nexus를 위한 OS계정 등록
```bash
groupadd nexus
useradd -g nexusadm -m nexusadm
```   
<br>

### - Install Nexus  
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
run_as_user="nexusadm"
```

```bash
# Nexus start
/app/nexus/nexus/bin/nexus start
```

<br>

### - Nexus 사용법  

넥서스를 기동하고 나면 아래와 같이 기본 Repository 들이 보인다.  
언어에 맞게 필요한 Repository를 선택하여 생성 하면 된다.  

![nexus1](/assets/it/solution/nexus/nexus1.png){: width="90%" height="auto"}  

Repository Type은 아래와 같은 용도로 사용한다.  
1. Hosted : A hosted Maven repository can be used to deploy your own as well as third-party components. The maven-releases repository uses a release version policy and the maven-snapshots repository uses a snapshot version policy.  

2. Proxy : A default installation of Nexus Repository Manager includes a proxy repository configured to access the Central Repository via HTTPS using the URL https://repo1.maven.org/maven2/. To reduce duplicate downloads and improve download speeds for your developers and CI servers, you should proxy all other remote repositories you access as proxy repositories as well.  

3. Group : A repository group is the recommended way to expose all your Maven repositories from the repository manager to your users, without needing any further client side configuration. A repository group allows you to expose the aggregated content of multiple proxy and hosted repositories as well as other repository groups with one URL for tool configuration.  

Manual로 관리할 Library가 있다면 Hosted, Maven Central에서 받아오고 싶다면 Proxy로, 이 둘을 모두 묶겠다고 하면 Group을 하나 더 만들어서 추가 하면 된다.  

<br>

### - Nexus Repository 추가  

Repository를 사용하는 방법은 각 언어마다 다르다. 

- Maven Repository  
Maven Nexus레파지토리를 이용하기 위해서는 pom.xml에 아래와 같이 repository를 추가해주면 된다. settings.xml에 mirror 서버를 설정하는 방법도 있다.  

```xml
<profiles>
    <profile>
      <id>nexus</id>
      <!--Enable snapshots for the built in central repo to direct -->
      <!--all requests to nexus via the mirror -->
      <repositories>
        <repository>
          <id>central</id>
          <url>http://central</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>true</enabled></snapshots>
        </repository>
      </repositories>
     <pluginRepositories>
        <pluginRepository>
          <id>central</id>
          <url>http://central</url>
          <releases><enabled>true</enabled></releases>
          <snapshots><enabled>true</enabled></snapshots>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>
```

- NPM Repository  
NPM의 경우 아래와 같이 config에 registry를 설정해 주면 npm install시 nexus를 보게 된다.  

```bash
npm config set registry https://nexus.example.com/repository/npm-group/

# Check log! 
npm --verbose install 
```

만약 



### - Nginx 연동  
추가적으로 Nginx와 같이 설정하여 진행한다면 Nginx를 설치하고 아래와 같이 설정해주면 사용이 가능하다.  
인증서는 각자 알아서 해결해야 한다.  
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