---
layout: post
title: "Elastic APM"
author: "Bys"
category: solution
date: 2022-04-21 01:00:00
tags: h2 db
---

## Elastic APM

### 1. Elastic APM 이란  
APM은 Application Performance Monitoring으로 Application에 대한 성능정보 및 호출이력 JVM 리소스 모니터링 등 기본적인 Metric 정보를 확인할 수 있다.  
다양한 APM이 존재하지만 이번에 Elastic APM을 사용한 이유는 구성 환경 중 이미 EFK를 활용하여 로깅시스템을 구축하였고, Kibana와 통합된 화면으로 APM정보를 확인 할 수 있다는 점에서 사용을 해보게 되었다. 

![elastic-apm001.png](/assets/it/solution/elastic-apm/elastic-apm001.png){: width="80%" height="auto"}  

### 2. Elastic-APM의 구성요소  

![elastic-apm002.png](/assets/it/solution/elastic-apm/elastic-apm002.png){: width="80%" height="auto"}  

실제로 Beats, Logstash와 연동하여 elasticsearch로도 데이터를 전달할 수 있는 아키텍처이며 여기서는 elastic-apm-agents와 elastic-apm-server를 사용하여 구성하였다. 
agent,server 방식에서는 아래와 같은 구성 요소를 갖는다. 

1. **Elastic-apm-agent**  
   elastic-apm-agent는 실제로 사용자가 모니터링할 Application에 심어지며 elastic-apm-server로 Application의 메트릭 정보를 수집하여 apm-server로 전송한다.  
2. **Elastic-apm-server**  
   elastic-apm-server는 사용자 Application의 데이터를 수집하여 Elasticsearch로 전송한다.  
3. **Elasticsearch**  
   elasticsearch는 apm-server로 부터 수집된 데이터를 저장하는 공간이다.  
4. **Kibana**  
   kibana는 elasticsearch에 저장된 메트릭 정보를 사용자가 볼 수 있는 화면이다.  

### 3. Elastic-APM 설치  

1. **APM-Server**  
   [APM Download](https://www.elastic.co/kr/downloads/apm)에 접속 > 알맞은 OS를 선택한 후 다운로드 받는다. (여기서는 Choose platform: Linux x86_64)  
   이 때 다운로드 되는 apm-server버전은 elasticsearch 버전과 동일해야 해야한다. apm-server-7.1.1-linux-x86_64.tar.gz
   tar파일 압축해제하고 내용을 확인해보면 아래와 같다.  
   ```bash
   tar -xvf apm-server-7.1.1-linux-x86_64.tar.gz
   cd apm-server-7.1.1-linux-x86_64
   #ls -al
   -rw-r--r--  1 bys  bys     13675  5 23  2019 LICENSE.txt
   -rw-r--r--  1 bys  bys    127188  5 23  2019 NOTICE.txt
   -rw-r--r--  1 bys  bys       660  5 23  2019 README.md
   -rwxr-xr-x  1 bys  bys  42070043  5 23  2019 apm-server
   -rw-------  1 bys  bys     32776  5 23  2019 apm-server.yml
   -rw-r--r--  1 bys  bys     96070  5 23  2019 fields.yml
   drwxr-xr-x  3 bys  bys        96  5 23  2019 ingest
   drwxr-xr-x  2 bys  bys        64  5 23  2019 kibana
   ```

   apm-server.yml 파일을 통해 apm-server의 설정을 해준다.  
   1) APM Server  
   여기서 host: "localhost:8200" 이 부분은 apm 서버의 listen 호스트와 포트 설정으로 외부에서도 접속하기 위해 0.0.0.0:8200으로 변경하였다.  
   2) Outputs  
   Output.elasticsearch:의 설정은 apm-server가 어떤 elasticsearch로 데이터를 보낼 지 설정하는 주소다.  
   3) Logging
   별도 파일로 로깅을 하기 위해 Logging설정을 해준다. (해당 로깅 설정을 하면 서버를 기동할 때 -e 옵션을 빼주어야 한다.)  

   `apm-server.yml`
   ```yaml
   ################### APM Server Configuration #########################
   ############################# APM Server ######################################
   apm-server:
   # Defines the host and port the server is listening on.  use "unix:/path/to.sock" to listen on a unix domain socket.
   #host: "localhost:8200"
   host: "0.0.0.0:8200"

   # Maximum permitted size in bytes of a request's header accepted by the server to be processed.
   #max_header_size: 1048576

   # Maximum permitted duration for reading an entire request.
   #read_timeout: 30s

   # Maximum permitted duration for writing a response.
   #write_timeout: 30s

   # Maximum duration in seconds before releasing resources when shutting down the server.
   #shutdown_timeout: 5s

   # Maximum allowed size in bytes of a single event
   #max_event_size: 307200
   
   ......

   #================================ Outputs =====================================

   # Configure what output to use when sending the data collected by apm-server.

   #-------------------------- Elasticsearch output ------------------------------
   output.elasticsearch:
   # Array of hosts to connect to.
   # Scheme and port can be left out and will be set to the default (http and 9200)
   # In case you specify and additional path, the scheme is required: http://localhost:9200/path
   # IPv6 addresses should always be defined as: https://[2001:db8::1]:9200
   hosts: ["elasticsearch-domain:9200"]
   protocol: "https"
   username: "user1"
   password: "password123"

   # Boolean flag to enable or disable the output module.
   #enabled: true

   # Set gzip compression level.

   ......

   #================================ Logging ======================================
   #
   # There are three options for the log output: syslog, file, stderr.
   # Under Windows systems, the log files are per default sent to the file output,
   # under all other system per default to syslog.

   # Sets log level. The default log level is info.
   # Available log levels are: error, warning, info, debug
   logging.level: info

   # Enable debug output for selected components. To enable all selectors use ["*"]
   # Other available selectors are "beat", "publish", "service"
   # Multiple selectors can be chained.
   logging.selectors: [ ]

   # Send all logging output to syslog. The default is false.
   #logging.to_syslog: true

   # If enabled, apm-server periodically logs its internal metrics that have changed
   # in the last period. For each metric that changed, the delta from the value at
   # the beginning of the period is logged. Also, the total values for
   # all non-zero internal metrics are logged on shutdown. The default is true.
   #logging.metrics.enabled: false

   # The period after which to log the internal metrics. The default is 30s.
   #logging.metrics.period: 30s

   # Logging to rotating files. Set logging.to_files to false to disable logging to
   # files.
   logging.to_files: true
   logging.files:
   # Configure the path where the logs are written. The default is the logs directory
   # under the home path (the binary location).
   path: /app/apm/apm-server-7.1.1-linux-x86_64/logs

   # The name of the files where the logs are written to.
   name: apm-server

   # Configure log file size limit. If limit is reached, log file will be
   # automatically rotated
   #rotateeverybytes: 10485760 # = 10MB

   # Number of rotated log files to keep. Oldest files will be deleted first.
   keepfiles: 7

   # The permissions mask to apply when rotating log files. The default value is 0600.
   # Must be a valid Unix-style file permissions mask expressed in octal notation.
   #permissions: 0600
   ......
   ```
   
   해당 설정을 모두 마치면 APM Server를 기동한다.  
   ./apm-server -c apm-server.yml 의 커맨드를 수행해도 되지만 백그라운드에서 실행하기 위해서는 start.sh파일을 하나 만들고 아래와 같이 작성한다.  
   ```bash
   vim start.sh
   ./apm-server -c apm-server.yml 1> /dev/null 2>&1 &

   vim stop.sh
   export apm_pid=`ps -ef | grep -v grep | grep apm-server | awk '{print $2}'`
   echo stop apm-server $apm_pid
   kill -9 $apm_pid
   ```

   APM Server가 정상 기동되면 아래와 같이 connection to elasticsearch 와 같은 메세지가 보이게 된다.  
   ```log
   2022-07-05T14:31:52.203+0900	INFO	[http_client]	beater/client.go:49	HTTP Server ready
   2022-07-05T14:31:52.203+0900	INFO	[onboarding]	beater/onboarding.go:36	Publishing onboarding document
   2022-07-05T14:31:53.203+0900	INFO	pipeline/output.go:95	Connecting to backoff(elasticsearch(https://elasticsearch-domain.com:9200))
   2022-07-05T14:31:53.230+0900	INFO	elasticsearch/client.go:734	Attempting to connect to Elasticsearch version 7.10.1
   2022-07-05T14:31:53.243+0900	INFO	template/load.go:129	Template already exists and will not be overwritten.
   2022-07-05T14:31:53.243+0900	INFO	[index-management]	idxmgmt/supporter.go:196	Loaded index template.
   2022-07-05T14:31:53.243+0900	INFO	pipeline/output.go:105	Connection to backoff(elasticsearch(https://elasticsearch-domain.com:9200)) established
   ```

2. **APM-Agents**  
   APM서버가 잘 기동이 되었다면 Application에 Agents를 설치하면된다.  
   Application기동시 옵션으로 넣어주면 되며 -Delastic.apm.server_urls=""에는 반드시 apm-server가 설치된 도메인 이름을 적어줘야한다.  
   ```bash
   export CATALINA_OPTS="$CATALINA_OPTS -javaagent:/app/elastic-apm/elastic-apm-agent-1.32.0.jar"
   export CATALINA_OPTS="$CATALINA_OPTS -Delastic.apm.service_name=bys-service"
   export CATALINA_OPTS="$CATALINA_OPTS -Delastic.apm.application_packages=com.bys"
   export CATALINA_OPTS="$CATALINA_OPTS -Delastic.apm.environment=prod"
   export CATALINA_OPTS="$CATALINA_OPTS -Delastic.apm.server_urls=apm-server.test.com:8200"
   ```
   설정 후 Application이 모두 정상 기동되는지 확인한다.  

### 3. Elastic-APM 데이터 확인  
   Application이 정상 기동되었다면 데이터 수집이 정상적인지 Kibana화면을 통해 확인할 수 있다.  

   ![elastic-apm003.png](/assets/it/solution/elastic-apm/elastic-apm003.png){: width="95%" height="auto"}  

   JVM  
   ![elastic-apm004.png](/assets/it/solution/elastic-apm/elastic-apm004.png){: width="95%" height="auto"}  
