---
layout: post
title: "Openstack AODH(Alarm)"
author: "Bys"
category: cloud
date: 2022-02-11 01:00:00
tags: openstack aodh alarm
---

# AODH

AODH 사용자에게 제공되는 모니터링 서비스이다. 모니터링 이용하여 Heat와 같은 Orchestration 서비스에서 인스턴스 그룹의 Auto-Scaling이 가능하며 사용자에게 클라우드 리소스에 대한 알람을 보낼 수 있다.  

아키텍처는 다음과 같다.  

![aodh_1](/assets/it/cloud/openstack/aodh/aodh_1.png){: width="60%" height="auto"}  

데이터는 Ceilometer에 의해 수집되며 Gnocchi라고 하는 시계열 데이터베이스로 전송이 되게 된다.  
Gnocchi는 측정된 데이터를 시계열 데이터베이스에 맞는 포맷으로 최적화하여 저장하고, Aodh는 알람 서비스로 gnocchi의 데이터를 이용하여 알람을 보낼 수 있게 된다.  

--- 

## 1. 사전 준비 사항  

### 1.1. Nova Instance를 생성  
서버는 2Core 4Memory를 가진 서버로 생성하였다.  
```bash
ops server list
# > output
+--------------------------------------+------+--------+------------------------------------------+-----------------------+--------------+
| ID                                   | Name | Status | Networks                                 | Image                 | Flavor       |
+--------------------------------------+------+--------+------------------------------------------+-----------------------+--------------+
| 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d | Test | ACTIVE | prv_net_1=10.10.10.10, 192.168.10.100    | CentOS7.9-cloud-image | a2.win.small |
+--------------------------------------+------+--------+------------------------------------------+-----------------------+--------------+
```
해당 서버에는 Tomcat WAS를 띄운다.  

<br>

### 1.2. Jmeter 설정 및 HTTP_REQUEST 설정  
Jmeter설치를 진행한다.  
```bash
# install jmeter
brew install jmeter

# open jmeter
open /usr/local/bin/jmeter
```
Jmeter를 열어 적당히 해당 서버로 Http_request를 설정한다.  

<br><br>


## 2. Metric 정보 확인  
metric은 특정 리소스에 항목들을에 대하여 archive-policy를 적용하여 만들게 된다.  
아래와 같이 인스턴스 항목에는 vcpu, cpu, memory, memory.usage 등등이 존재하는 것을 확인 할 수 있다.  

> https://docs.openstack.org/ceilometer/train/admin/telemetry-measurements.html

```bash
ops metric list
# > oputput
+--------------------------------------+---------------------+---------------------------------+---------+--------------------------------------+
| id                                   | archive_policy/name | name                            | unit    | resource_id                          |
+--------------------------------------+---------------------+---------------------------------+---------+--------------------------------------+
| 3060ab53-faa5-4566-93f1-e1f1d866775c | ceilometer-low      | perf.cpu.cycles                 |         | 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d |
| 3b3e2cd7-276d-4c96-91aa-80c78a07219a | ceilometer-low      | disk.ephemeral.size             | GB      | 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d |
| 543f2639-905b-4f2c-91bf-dea93f864077 | ceilometer-low      | vcpus                           | vcpu    | 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d |
| 5fc7b51c-0a74-4c85-81cc-b2504ff28b6a | ceilometer-low      | memory                          | MB      | 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d |
| 90ae5e59-476e-4d6a-984e-313bd413c342 | ceilometer-low      | memory.usage                    | MB      | 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d |
| a8309902-bc63-4e00-90b5-7e31cca0e942 | ceilometer-low-rate | cpu                             | ns      | 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d |
| b5c57456-e508-4d54-8c0c-8ae3604e7d7a | ceilometer-low      | disk.root.size                  | GB      | 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d |
| b9e2c5b9-e2ec-4669-82b1-c9a6a356a141 | ceilometer-low      | compute.instance.booting.time   | sec     | 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d |
| efb70784-e295-4837-9d16-69c4910b5221 | ceilometer-low      | perf.cache.misses               |         | 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d |
+--------------------------------------+---------------------+---------------------------------+---------+--------------------------------------+
```

<br>

### 2.1 CPU Metric에 대한 이해
현재 openstack 특정 버전 이 후 부터 측정되는 metric 중에 cpu_util의 값이 존재하지 않으므로 cpu 메트릭을 이용하여 cpu_util을 계산해야 한다.  
cpu메트릭은 cpu사용량을 nanosecond 단위로 측정한 것이므로 아래와 같이 계산한다.  
1(ns) = 0.000000001(sec)  
cpu_util(%) = cpu(ns) / 1000000000 / granularity(sec) / cpu_core(vpcu) * 100  

```bash
# metric list
ops metric list
+--------------------------------------+---------------------+---------------------------------+---------+--------------------------------------+
| id                                   | archive_policy/name | name                            | unit    | resource_id                          |
+--------------------------------------+---------------------+---------------------------------+---------+--------------------------------------+
| 3060ab53-faa5-4566-93f1-e1f1d866775c | ceilometer-low      | perf.cpu.cycles                 |         | 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d |
| 3b3e2cd7-276d-4c96-91aa-80c78a07219a | ceilometer-low      | disk.ephemeral.size             | GB      | 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d |
| 543f2639-905b-4f2c-91bf-dea93f864077 | ceilometer-low      | vcpus                           | vcpu    | 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d |
| 5fc7b51c-0a74-4c85-81cc-b2504ff28b6a | ceilometer-low      | memory                          | MB      | 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d |
| 90ae5e59-476e-4d6a-984e-313bd413c342 | ceilometer-low      | memory.usage                    | MB      | 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d |
| a8309902-bc63-4e00-90b5-7e31cca0e942 | ceilometer-low-rate | cpu                             | ns      | 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d |
| b5c57456-e508-4d54-8c0c-8ae3604e7d7a | ceilometer-low      | disk.root.size                  | GB      | 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d |
| b9e2c5b9-e2ec-4669-82b1-c9a6a356a141 | ceilometer-low      | compute.instance.booting.time   | sec     | 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d |
| efb70784-e295-4837-9d16-69c4910b5221 | ceilometer-low      | perf.cache.misses               |         | 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d |
+--------------------------------------+---------------------+---------------------------------+---------+--------------------------------------+
```
<br>

### 2.2. Metric 수집 정보 확인  
메모리 사용량(memory.usage)에 대한 메트릭이 있는 것을 확인 하였으니 항목에 대한 측정치를 확인 하려면 아래와 같이 확인 할 수 있다.  
'ceilometer-low'라는 archive-policy에 의하여 5분 주기로 수집이 되고 있으며 timestamp, granularity, value 값을 가지고 있다.  

```bash
# metric about memory.usage
ops metric measures show 90ae5e59-476e-4d6a-984e-313bd413c342 --start 2022-02-09T17:35:00+09:00
+---------------------------+-------------+-------+
| timestamp                 | granularity | value |
+---------------------------+-------------+-------+
| 2022-02-09T17:35:00+09:00 |       300.0 | 324.0 |
| 2022-02-09T17:40:00+09:00 |       300.0 | 324.0 |
| 2022-02-09T17:45:00+09:00 |       300.0 | 324.0 |
| 2022-02-09T17:50:00+09:00 |       300.0 | 324.0 |
| 2022-02-09T17:55:00+09:00 |       300.0 | 324.0 |
+---------------------------+-------------+-------+

# metric about cpu nanosecond
openstack metric measures show --resource-id 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d --aggregation rate:mean cpu --start 2022-02-11T05:00:00+00:00
openstack metric aggregates '(metric cpu rate:mean)' id=1c02a8a3-bd1a-41f6-91de-2b9555c35e6d --start 2022-02-11T05:00:00+00:00

# metric about cpu second
openstack metric aggregates '(/ (aggregate rate:mean (metric cpu mean)) 1000000000)' id=1c02a8a3-bd1a-41f6-91de-2b9555c35e6d --start 2022-02-10T05:00:00+00:00

# metric about cpu utils --> cpu_used_time(sec)/cpu_data_period(second)
openstack metric aggregates '(* (/ (/ (aggregate rate:mean (metric cpu mean)) 300000000000) 2) 100)' id=1c02a8a3-bd1a-41f6-91de-2b9555c35e6d --start 2022-02-11T06:40:00+00:00
```


현재 생성되어 있는 archive-policy는 아래와 같으며 definition항목, gnocchi에 내장된 aggregation_methods등에 의해 정의 된다.  
ceilometer-low의 경우 데이터 포인트를 30일동안 8640개를 찍겠다는 의미이며 결국 5분 주기로 aggregation_methods인 측정을 수행한다는 의미로 이해하면 된다.  
```bash
ops metric archive-policy list
# > output
+----------------------+-------------+-----------------------------------------------------------------------+---------------------------------+
| name                 | back_window | definition                                                            | aggregation_methods             |
+----------------------+-------------+-----------------------------------------------------------------------+---------------------------------+
| bool                 |        3600 | - points: 31536000, timespan: 365 days, 0:00:00, granularity: 0:00:01 | last                            |
| ceilometer-dashboard |           0 | - points: 72, timespan: 6:00:00, granularity: 0:05:00                 | rate:mean, mean                 |
| ceilometer-high      |           0 | - points: 3600, timespan: 1:00:00, granularity: 0:00:01               | mean                            |
|                      |             | - points: 1440, timespan: 1 day, 0:00:00, granularity: 0:01:00        |                                 |
|                      |             | - points: 8760, timespan: 365 days, 0:00:00, granularity: 1:00:00     |                                 |
| ceilometer-high-rate |           0 | - points: 3600, timespan: 1:00:00, granularity: 0:00:01               | rate:mean, mean                 |
|                      |             | - points: 1440, timespan: 1 day, 0:00:00, granularity: 0:01:00        |                                 |
|                      |             | - points: 8760, timespan: 365 days, 0:00:00, granularity: 1:00:00     |                                 |
| ceilometer-low       |           0 | - points: 8640, timespan: 30 days, 0:00:00, granularity: 0:05:00      | mean                            |
| ceilometer-low-rate  |           0 | - points: 8640, timespan: 30 days, 0:00:00, granularity: 0:05:00      | rate:mean, mean                 |
| high                 |           0 | - points: 3600, timespan: 1:00:00, granularity: 0:00:01               | std, count, min, max, sum, mean |
|                      |             | - points: 10080, timespan: 7 days, 0:00:00, granularity: 0:01:00      |                                 |
|                      |             | - points: 8760, timespan: 365 days, 0:00:00, granularity: 1:00:00     |                                 |
| low                  |           0 | - points: 8640, timespan: 30 days, 0:00:00, granularity: 0:05:00      | std, count, min, max, sum, mean |
| medium               |           0 | - points: 10080, timespan: 7 days, 0:00:00, granularity: 0:01:00      | std, count, min, max, sum, mean |
|                      |             | - points: 8760, timespan: 365 days, 0:00:00, granularity: 1:00:00     |                                 |
+----------------------+-------------+-----------------------------------------------------------------------+---------------------------------+
```

<br><br>


## 3. 알람 설정
Gnocchi를 활용하여 알람을 설정한다.  

위 에서 방식에 따르면 threshold(ns) 계산식은 아래와 같다.  
cpu_util(%) = cpu(ns) / 1000000000 / granularity(sec) / cpu_core(vpcu) * 100  
cpu(ns) = cpu_util(%) * 1000000000 * granularity(sec) * cpu_core(vpcu) / 100  
cpu(ns) = 70 * 1000000000 * 300 * 2 / 100 = 420000000000  
cpu(ns) = 15 * 1000000000 * 300 * 2 / 100 = 90000000000  

### 3.1 CPU 알람 설정

```bash
# CPU 사용량이 5분 간격으로 15% 이상 연속적으로 3번을 초과한 경우
# This creates an alarm that will fire when the average cpu utilization for an individual instance exceeds 15% for three consecutive 5 minute periods.
  openstack alarm create \
  --name cpu_usage_over_15 \
  --type gnocchi_resources_threshold \
  --description 'instance cpu' \
  --metric cpu \
  --threshold 90000000000 \
  --comparison-operator gt \
  --aggregation-method rate:mean \
  --granularity 300 \
  --evaluation-periods 3 \
  --resource-id 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d \
  --resource-type instance \
  --repeat-actions true \
  --alarm-action "https://user:{token}@{jenkins_url}/job/test-aodh-cpu15-alarm/build" 
```

<br>

### 3.2 Memory 알람 설정

```bash
# 메모리 사용량이 5분 간격으로 1024MB 이상 연속적으로 3번을 초과한 경우
# This creates an alarm that will fire when the average memory utilization for an individual instance exceeds 1024MB for three consecutive 5 minute periods.
openstack alarm create \
  --name memory_usage_over_1024 \
  --type gnocchi_resources_threshold \
  --description 'instance memory' \
  --metric memory.usage\
  --threshold 1024 \
  --comparison-operator gt \
  --aggregation-method mean \
  --granularity 300 \
  --evaluation-periods 3 \
  --resource-id 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d \
  --resource-type instance \
  --repeat-action true \
  --alarm-action "https://user:{token}@{jenkins_url}/job/test-aodh-memory1024-alarm/build"
```

alarm-action에 들어가는 URL을 호출할 때, 넘어가는 body의 파라미터는 아래와 같다.  
```python
# aodh > notifier > rest.py
body = {'alarm_name': alarm_name, 'alarm_id': alarm_id,
        'severity': severity, 'previous': previous,
        'current': current, 'reason': reason,
        'reason_data': reason_data}
headers['content-type'] = 'application/json'
kwargs = {'data': json.dumps(body),
          'headers': headers}

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

max_retries = self.conf.rest_notifier_max_retries
session = requests.Session()
session.mount(action.geturl(),
              requests.adapters.HTTPAdapter(max_retries=max_retries))
resp = session.post(action.geturl(), **kwargs)
LOG.info('Notifying alarm <%(id)s> gets response: %(status_code)s '
          '%(reason)s.', {'id': alarm_id,
                          'status_code': resp.status_code,
                          'reason': resp.reason})
```
Openstack은 오픈소스이므로 Github에 올려져 있는 소스를 참고하면 정보를 알 수 있다.  



<br>

### 3.3 알람 설정 확인
테스트 이전 사용량 및 알람을 확인  
테스트 이전 메트릭은 현재 조건 값을 충족하지 않으므로 alarm-state는 ok상태이다.  
```bash
# cpu check 
openstack metric aggregates '(* (/ (/ (aggregate rate:mean (metric cpu mean)) 300000000000) 2) 100)' id=1c02a8a3-bd1a-41f6-91de-2b9555c35e6d --start 2022-02-11T06:40:00+00:00
+------------+---------------------------+-------------+---------------------+
| name       | timestamp                 | granularity |               value |
+------------+---------------------------+-------------+---------------------+
| aggregated | 2022-02-11T06:45:00+00:00 |       300.0 |               0.135 |
| aggregated | 2022-02-11T06:50:00+00:00 |       300.0 | 0.26666666666666666 |
| aggregated | 2022-02-11T06:55:00+00:00 |       300.0 |   2.651666666666667 |
| aggregated | 2022-02-11T07:00:00+00:00 |       300.0 |  0.9400000000000001 |
| aggregated | 2022-02-11T07:05:00+00:00 |       300.0 |               3.465 |
| aggregated | 2022-02-11T07:10:00+00:00 |       300.0 |   5.401666666666666 |
+------------+---------------------------+-------------+---------------------+

# memory check 
openstack metric aggregates '(metric memory.usage mean)' id=1c02a8a3-bd1a-41f6-91de-2b9555c35e6d --start 2022-02-11T06:40:00+00:00
+--------------------------------------------------------+---------------------------+-------------+--------+
| name                                                   | timestamp                 | granularity |  value |
+--------------------------------------------------------+---------------------------+-------------+--------+
| 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d/memory.usage/mean | 2022-02-11T06:40:00+00:00 |       300.0 |  400.0 |
| 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d/memory.usage/mean | 2022-02-11T06:45:00+00:00 |       300.0 |  400.0 |
| 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d/memory.usage/mean | 2022-02-11T06:50:00+00:00 |       300.0 |  400.0 |
| 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d/memory.usage/mean | 2022-02-11T06:55:00+00:00 |       300.0 |  399.0 |
| 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d/memory.usage/mean | 2022-02-11T07:00:00+00:00 |       300.0 |  399.0 |
| 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d/memory.usage/mean | 2022-02-11T07:05:00+00:00 |       300.0 |  593.0 |
| 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d/memory.usage/mean | 2022-02-11T07:10:00+00:00 |       300.0 |  893.0 |
+--------------------------------------------------------+---------------------------+-------------+--------+


# alarm state check
ops alarm list
+--------------------------------------+-----------------------------+------------------------+-------------------+----------+---------+
| alarm_id                             | type                        | name                   | state             | severity | enabled |
+--------------------------------------+-----------------------------+------------------------+-------------------+----------+---------+
| 5ac83325-c1e4-42e7-8787-dd5261216e84 | gnocchi_resources_threshold | memory_usage_over_1024 | ok                | low      | True    |
| 6278561b-f369-4fc7-bba9-51870b7414ef | gnocchi_resources_threshold | cpu_usage_over_15      | ok                | low      | True    |
| 7ed5a6bc-8e33-4bd9-a2c9-1eb280001e55 | gnocchi_resources_threshold | cpu_hi_webhook_test    | insufficient data | low      | True    |
| f8ce29a5-2392-4bf6-a46b-1d13d39cc025 | gnocchi_resources_threshold | cpu_hi_webhook_test    | insufficient data | low      | True    |
| 8583d619-f8f0-49fe-b332-93493a82d0c8 | gnocchi_resources_threshold | cpu_hi_webhook         | insufficient data | low      | True    |
| a6d4dcfa-e534-4058-8547-548d1f645098 | gnocchi_resources_threshold | cpu_hi                 | insufficient data | low      | True    |
+--------------------------------------+-----------------------------+------------------------+-------------------+----------+---------+
```

<br><br>

## 4. Jmeter를 활용한 부하 및 Alarm Test

### 4.1 Jmeter를 활용한 부하
대상 서버에는 임시로 was및 소스를 올려놨고 테스트를 진행했다.  

![aodh_3](/assets/it/cloud/openstack/aodh/aodh_3.png){: width="60%" height="auto"}  

부하 테스트를 시작한 시간은 16:12분이다.  

예상하는 결과는 16:15분에 측정된 값, 16:20분에 측정된 값, 16:30분에 측정된 값이
설정한 알람의 조건보다 (CPU > 15%, MEMORY > 1024MB) 값이 모두 크다면 알람의 상태는 ok -> alarm 상태로 변경되며 jenkins build가 수행 되는 것이다.  

<br>

### 4.2 Alarm 결과 확인
약 15분 후 결과 확인
```bash
# cpu check 
openstack metric aggregates '(* (/ (/ (aggregate rate:mean (metric cpu mean)) 300000000000) 2) 100)' id=1c02a8a3-bd1a-41f6-91de-2b9555c35e6d --start 2022-02-11T06:40:00+00:00
+------------+---------------------------+-------------+---------------------+
| name       | timestamp                 | granularity |               value |
+------------+---------------------------+-------------+---------------------+
| aggregated | 2022-02-11T06:45:00+00:00 |       300.0 |               0.135 |
| aggregated | 2022-02-11T06:50:00+00:00 |       300.0 | 0.26666666666666666 |
| aggregated | 2022-02-11T06:55:00+00:00 |       300.0 |   2.651666666666667 |
| aggregated | 2022-02-11T07:00:00+00:00 |       300.0 |  0.9400000000000001 |
| aggregated | 2022-02-11T07:05:00+00:00 |       300.0 |               3.465 |
| aggregated | 2022-02-11T07:10:00+00:00 |       300.0 |   5.401666666666666 |
| aggregated | 2022-02-11T07:15:00+00:00 |       300.0 |              27.975 |
| aggregated | 2022-02-11T07:20:00+00:00 |       300.0 |   53.18666666666667 |
| aggregated | 2022-02-11T07:25:00+00:00 |       300.0 |   56.45166666666667 |
+------------+---------------------------+-------------+---------------------+

# memory check 
openstack metric aggregates '(metric memory.usage mean)' id=1c02a8a3-bd1a-41f6-91de-2b9555c35e6d --start 2022-02-11T06:40:00+00:00
+--------------------------------------------------------+---------------------------+-------------+--------+
| name                                                   | timestamp                 | granularity |  value |
+--------------------------------------------------------+---------------------------+-------------+--------+
| 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d/memory.usage/mean | 2022-02-11T06:40:00+00:00 |       300.0 |  400.0 |
| 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d/memory.usage/mean | 2022-02-11T06:45:00+00:00 |       300.0 |  400.0 |
| 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d/memory.usage/mean | 2022-02-11T06:50:00+00:00 |       300.0 |  400.0 |
| 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d/memory.usage/mean | 2022-02-11T06:55:00+00:00 |       300.0 |  399.0 |
| 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d/memory.usage/mean | 2022-02-11T07:00:00+00:00 |       300.0 |  399.0 |
| 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d/memory.usage/mean | 2022-02-11T07:05:00+00:00 |       300.0 |  593.0 |
| 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d/memory.usage/mean | 2022-02-11T07:10:00+00:00 |       300.0 |  893.0 |
| 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d/memory.usage/mean | 2022-02-11T07:15:00+00:00 |       300.0 | 1207.0 |
| 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d/memory.usage/mean | 2022-02-11T07:20:00+00:00 |       300.0 | 1374.0 |
| 1c02a8a3-bd1a-41f6-91de-2b9555c35e6d/memory.usage/mean | 2022-02-11T07:25:00+00:00 |       300.0 | 1544.0 |
+--------------------------------------------------------+---------------------------+-------------+--------+

# alarm state check
ops alarm list
+--------------------------------------+-----------------------------+------------------------+-------------------+----------+---------+
| alarm_id                             | type                        | name                   | state             | severity | enabled |
+--------------------------------------+-----------------------------+------------------------+-------------------+----------+---------+
| 5ac83325-c1e4-42e7-8787-dd5261216e84 | gnocchi_resources_threshold | memory_usage_over_1024 | alarm             | low      | True    |
| 6278561b-f369-4fc7-bba9-51870b7414ef | gnocchi_resources_threshold | cpu_usage_over_15      | alarm             | low      | True    |
| 7ed5a6bc-8e33-4bd9-a2c9-1eb280001e55 | gnocchi_resources_threshold | cpu_hi_webhook_test    | insufficient data | low      | True    |
| f8ce29a5-2392-4bf6-a46b-1d13d39cc025 | gnocchi_resources_threshold | cpu_hi_webhook_test    | insufficient data | low      | True    |
| 8583d619-f8f0-49fe-b332-93493a82d0c8 | gnocchi_resources_threshold | cpu_hi_webhook         | insufficient data | low      | True    |
| a6d4dcfa-e534-4058-8547-548d1f645098 | gnocchi_resources_threshold | cpu_hi                 | insufficient data | low      | True    |
+--------------------------------------+-----------------------------+------------------------+-------------------+----------+---------+
```

**예상한 바와 같이 측정된 값이 5분 간격으로 3번 연속적으로 조건 값을 넘기자 알람이 수행되었다.**

<br><br>

## 5. Alarm에 대한 종료

알람은 repeat-action 값이 Default:true 이기 때문에 alarm-state값이 alarm -> ok 로 다 변경 되기 전까지 1분 간격으로 지속적으로 오게 된다.  

**알람의 상태변경은 마찬가지로 5분간격으로 3번 연속적으로 조건 값을 만족하지 못해야 한다.**

다음 시간을 보면 테스트를 종료한 16:25분 이 후 부터 CPU 사용량은 줄어들게 되지만 JMeter Thread가 차례로 줄어들기 때문에 16시 30분에는 19.38%의 사용량이 측정되었고,  
따라서 16시 35분, 16시 40분, 16시 45분 까지의 3개의 데이터를 확인 한 후, alarm-state는 alarm -> ok가 되었다.  

```bash
openstack metric aggregates '(* (/ (/ (aggregate rate:mean (metric cpu mean)) 300000000000) 2) 100)' id=1c02a8a3-bd1a-41f6-91de-2b9555c35e6d --start 2022-02-11T06:40:00+00:00
+------------+---------------------------+-------------+---------------------+
| name       | timestamp                 | granularity |               value |
+------------+---------------------------+-------------+---------------------+
| aggregated | 2022-02-11T06:45:00+00:00 |       300.0 |               0.135 |
| aggregated | 2022-02-11T06:50:00+00:00 |       300.0 | 0.26666666666666666 |
| aggregated | 2022-02-11T06:55:00+00:00 |       300.0 |   2.651666666666667 |
| aggregated | 2022-02-11T07:00:00+00:00 |       300.0 |  0.9400000000000001 |
| aggregated | 2022-02-11T07:05:00+00:00 |       300.0 |               3.465 |
| aggregated | 2022-02-11T07:10:00+00:00 |       300.0 |   5.401666666666666 |
| aggregated | 2022-02-11T07:15:00+00:00 |       300.0 |              27.975 |
| aggregated | 2022-02-11T07:20:00+00:00 |       300.0 |   53.18666666666667 |
| aggregated | 2022-02-11T07:25:00+00:00 |       300.0 |   56.45166666666667 |
| aggregated | 2022-02-11T07:30:00+00:00 |       300.0 |  19.398333333333333 |
| aggregated | 2022-02-11T07:35:00+00:00 |       300.0 |  1.2016666666666667 |
| aggregated | 2022-02-11T07:40:00+00:00 |       300.0 |  1.5916666666666666 |
| aggregated | 2022-02-11T07:45:00+00:00 |       300.0 |  3.1466666666666665 |
| aggregated | 2022-02-11T07:50:00+00:00 |       300.0 |   5.738333333333333 |
+------------+---------------------------+-------------+---------------------+
```

실제로 Jenkins build 수행 이력을 확인 하면 아래와 같다.  

![aodh_5](/assets/it/cloud/openstack/aodh/aodh_5.png){: width="40%" height="auto"}  

16시 46분까지 알람을 보낸 이 후 알람이 종료 되었다.  

<br><br>

## 6. AODH 추후 고려할 점  

cpu, memory 를 수집하는 주기가 현재는 5분 주기 다보니 alarm을 받기 위해서는 evaluation-periods 값을 1로 설정한다고 할 지라도 최소 5분은 지나야 한다.  
archive-policy를 1분 단위로 측정하도록 default 설정을 변경하는 것이 어떨지 고민해보면 좋을 것 같다.  
수집주기가 1분이 된다면 사용자들이 알람설정을 하는데 있어서 조금 더 유연하게 설정할 수 있을 것이다.  

추가적으로 알람설정시 aggregation_method 부분에 cpu_util을 바로 사용할 수 있도록 설정하는 부분은 찾아봤지만 확인이 어려웠다.  
따라서 추후 화면에서 아래와 같은 계산식을 통해 사용자에게 cpu_util 값을 입력 받고, cpu(ns)을 알람에 설정하는 것으로 해야 할 것으로 보인다.  
cpu(ns) = cpu_util(%) * 1000000000 * granularity(sec) * cpu_core(vpcu) / 100 

---

## 목차 
## 1. 사전 준비 사항  
+ ### 1.1. Nova Instance를 생성  
+ ### 1.2. Jmeter 설정 및 HTTP_REQUEST 설정  

## 2. Metric 정보 확인  
+ ### 2.1. CPU Metric에 대한 이해
+ ### 2.2. Metric 수집 정보 확인  

## 3. 알람 설정
+ ### 3.1 CPU 알람 설정
+ ### 3.2 Memory 알람 설정
+ ### 3.3 알람 설정 확인

## 4. Jmeter를 활용한 부하 및 Alarm Test
+ ### 4.1 Jmeter를 활용한 부하
+ ### 4.2 Alarm 결과 확인

## 4. Jmeter를 활용한 부하 및 Alarm Test

## 5. Alarm에 대한 종료

## 6. AODH 추후 고려할 점  

<br><br><br>

> Ref: https://docs.openstack.org/aodh/train/admin/telemetry-alarms.html  
> Ref: https://docs.openstack.org/python-openstackclient/ussuri/cli/plugin-commands/aodh.html  
> Ref: https://gnocchi.osci.io/rest.html#  
> Ref: https://docs.openstack.org/ceilometer/train/admin/telemetry-measurements.html  
> Ref: https://docs.infomaniak.cloud/user-guide/0200.AODH/  
> Ref: https://stackoverflow.com/questions/56216683/openstack-get-vm-cpu-util-with-stein-version  
> Ref: https://access.redhat.com/documentation/en-us/red_hat_openstack_platform/16.0/html-single/auto_scaling_for_instances/index#example_auto_scaling_based_on_cpu_use  