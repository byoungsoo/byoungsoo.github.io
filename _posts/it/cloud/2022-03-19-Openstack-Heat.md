---
layout: post
title: "Openstack Heat(Orchestration)"
author: "Bys"
category: cloud
date: 2022-03-19 01:00:00
tags: openstack heat orchestration
---

# Heat (Orchestration)

## 1. Orchestration
Heat(Orchestration)는 Openstack 클라우드 내의 인프라 및 애플리케이션의 Life Cycle을 관리하기 위한 서비스다.  
Openstack native REST API, AWS CloudFormation을 통해 선언적 템플릿(declarative template)을 사용하여 Orchestration을 한다.  

## 2. Architecture 
- heat
The heat tool is a CLI which communicates with the heat-api to execute AWS CloudFormation APIs. End developers could also use the heat REST API directly.

- heat-api
The heat-api component provides an OpenStack-native REST API that processes API requests by sending them to the heat-engine over RPC.

- heat-api-cfn
The heat-api-cfn component provides an AWS Query API that is compatible with AWS CloudFormation and processes API requests by sending them to the heat-engine over RPC.

- heat-engine
The heat-engine’s main responsibility is to orchestrate the launching of templates and provide events back to the API consumer.

--- 


## 3. 실습
아래 Heat Template은 Autoscaling 그룹 대상 서버를 생성하며 Load balancer를 생성한다. 
로드밸런서의 Listener를 설정하며 앞서 생성한 Autoscaling 그룹 서버를 Target으로 설정한다. 또한 Autoscaling 서버의 Scale-Up, Scale-down 등의 정책을 설정한다. 
Security Group을 생성하여 LB, Autoscaling 서버에 붙여주며 정상 동작을 할 수 있도록 지원한다.  


### 3.1. Apply Command

Template을 생성 한 후 적용 커맨드는 아래와 같다.  
```bash
# Plan
ops stack create -e param_autoscaling_group.env -t template_autoscaling_group.yaml --dry-run was_asg

# Apply
ops stack create -e param_autoscaling_group.env -t template_autoscaling_group.yaml  was_asg
```

### 3.2. Template 파일 생성 

파라미터 파일에는 해당 openstack 환경에 맞는 알맞은 값들을 넣어준다.  

`param_autoscaling_group.env`  
```env
# Define Parameters
parameters:
    # AutoScalingGroup
    min_size: 1
    desired_capacity: 1
    max_size: 5
    # AutoScalingGroup Instance
    instance_name: was_server
    key_name: key_pari_name
    flavor: flavor_id or name
    availability_zone: nova
    image: ubutnu_tomcat_8
    volume_size: 100
    volume_type: SDD
    delete_on_termination: true
    network_id: network_id
    subnet_id: subnet_id

    # Security Group Parameter
    sg_server_name_1: sg-was-server
    sg_server_protocol_1: tcp
    sg_server_port_1: 22
    sg_server_remote_ip_1: my_ip/cidr

    sg_server_protocol_2: tcp
    sg_server_port_2: 8080
    sg_server_remote_ip_2: my_ip/cidr

    sg_server_lb_ip_range: lb_serveer_range_ip/cidr

    sg_lb_name_1: sg-loadbalancer
    sg_lb_protocol_1: tcp
    sg_lb_port_1: 8080
    sg_lb_remote_ip_1: my_ip/cidr

    # Scale Policy Parameter
    adjustment_type: change_in_capacity
    scale_up_cooldown: 60
    scaling_up_adjustment: 1
    scale_down_cooldown: 60
    scaling_down_adjustment: -1

    # Alarm Parameter
    alarm_high_description: Scale up if CPU > 15% for 5 minutes
    alarm_low_description: Scale down if CPU < 5% for 5 minutes
    metric: cpu
    aggregation_method: rate:mean
    granularity: 300
    evaluation_periods: 1
    resource_type: instance
    high_comparison_operator: gt
    high_threshold: 90000000000
    low_comparison_operator: lt
    low_threshold: 30000000000


    # LoadBalancer Parameter
    external_network_id: external_net_id
    lb_listen_protocol: HTTP
    lb_listen_port: 8080
    lb_algorithm: ROUND_ROBIN
    lb_target_protocol: HTTP
    lb_target_port: 8080
    lb_health_check_type: HTTP
    lb_health_check_http_method: GET
    lb_health_check_url: /
    lb_health_check_success_codes: 200
    lb_health_check_interval: 20
    lb_health_check_unhealthy_threshold: 2
    lb_health_check_timeout: 30
```
<br>
heat template을 적용하면 autoscaling_group을 생성하는 OS::Heat::AutoScalingGroup 타입에서는 template_lb_server.yaml 파일을 이용하여 desired_capacity 수 만큼 loop를 수행한다. 
desired 수가 3이라면 template_lb_server.yaml의 내용을 3번 반복 한다. 따라서 template_lb_server.yaml 에는 서버 및 루트 볼륨을 생성하는 스크립트와 로드밸런서의 타겟으로 등록하는 내용이 들어있다. 
이 외에 다른 resource 타입들에 대해서는 한 번만 수행하면서 실행하게 된다.  

`template_autoscaling_group.yaml`  
```yaml
heat_template_version: 2018-08-31

description: |
  The heat template is used to create OS::Heat::AutoScalingGroup, OS::Heat::ScalingPolicy
  OS::Aodh::GnocchiAggregationByResourcesAlarm, OS::Octavia::LoadBalancer etc

parameters:
  # Asg Parameter
  min_size:
    type: number
  desired_capacity:
    type: number
  max_size:
    type: number
  # Instance Parameter
  instance_name:
    type: string
  key_name:
    type: string
  flavor:
    type: string
  availability_zone:
    type: string 
  image:
    type: string
  volume_size:
    type: string
  volume_type:
    type: string
  delete_on_termination:
    type: boolean
  network_id:
    type: string
  subnet_id:
    type: string
  # Security Group Parameter
  sg_server_name_1:
    type: string
  sg_server_protocol_1:
    type: string
  sg_server_port_1:
    type: number
  sg_server_remote_ip_1:
    type: string
  sg_server_protocol_2:
    type: string
  sg_server_port_2:
    type: number
  sg_server_remote_ip_2:
    type: string
  sg_server_lb_ip_range:
    type: string
  sg_lb_name_1:
    type: string
  sg_lb_protocol_1:
    type: string
  sg_lb_port_1:
    type: number
  sg_lb_remote_ip_1:
    type: string
  # Scale Policy Parameter
  adjustment_type:
    type: string
  scale_up_cooldown:
    type: number
  scaling_up_adjustment:
    type: number
  scale_down_cooldown:
    type: number
  scaling_down_adjustment:
    type: number
  # Alarm Parameter
  alarm_high_description:
    type: string
  alarm_low_description:
    type: string
  metric:
    type: string
  aggregation_method:
    type: string
  granularity:
    type: number
  evaluation_periods:
    type: number
  resource_type:
    type: string
  high_comparison_operator:
    type: string
  high_threshold:
    type: number
  low_comparison_operator:
    type: string
  low_threshold:
    type: number
  # LoadBalancer Parameter
  external_network_id:
    type: string
  lb_listen_protocol:
    type: string
  lb_listen_port:
    type: number
  lb_algorithm:
    type: string
  lb_target_protocol:
    type: string
  lb_target_port:
    type: number
  lb_health_check_type:
    type: string
  lb_health_check_http_method:
    type: string
  lb_health_check_url:
    type: string
  lb_health_check_success_codes:
    type: string
  lb_health_check_interval:
    type: number
  lb_health_check_unhealthy_threshold:
    type: number
  lb_health_check_timeout:
    type: number
  
resources:
  asg:
    type: OS::Heat::AutoScalingGroup
    properties:
      min_size: { get_param: min_size }
      desired_capacity: { get_param: desired_capacity }
      max_size: { get_param: max_size }
      resource:
        type: template_lb_server.yaml
        properties:
          instance_name: { get_param: instance_name }
          key_name: { get_param: key_name }
          flavor: { get_param: flavor }
          availability_zone: { get_param: availability_zone }
          security_groups: { get_resource: server_security_group_1}
          image: { get_param: image }
          volume_size: { get_param: volume_size }
          volume_type: { get_param: volume_type }
          delete_on_termination: { get_param: delete_on_termination }
          network_id: { get_param: network_id }
          subnet_id: { get_param: subnet_id }
          metadata: {"metering.server_group": {get_param: "OS::stack_id"}}
          user_data:
            get_file: user_data.sh
          pool_id: { get_resource: pool}
          lb_target_port: { get_param: lb_target_port }

  server_security_group_1:
    type: OS::Neutron::SecurityGroup
    properties:
      name: {get_param: sg_server_name_1}
      rules: [
        {
          direction: ingress,
          protocol: {get_param: sg_server_protocol_1},
          port_range_min: {get_param: sg_server_port_1},
          port_range_max: {get_param: sg_server_port_1},
          remote_ip_prefix: {get_param: sg_server_remote_ip_1}
        },
        {
          direction: ingress,
          protocol: {get_param: sg_server_protocol_2},
          port_range_min: {get_param: sg_server_port_2},
          port_range_max: {get_param: sg_server_port_2},
          remote_ip_prefix: {get_param: sg_server_remote_ip_2}
        }
      ]
  additional_server_security_group_rule_1:
    type: OS::Neutron::SecurityGroupRule
    properties:
      security_group: {get_resource: server_security_group_1}
      direction: ingress
      protocol: tcp
      port_range_min: {get_param: sg_server_port_2}
      port_range_max: {get_param: sg_server_port_2}
      remote_ip_prefix: {get_param: sg_server_lb_ip_range}
  default_server_security_group_rule_1:
    type: OS::Neutron::SecurityGroupRule
    properties:
      security_group: {get_resource: server_security_group_1}
      direction: ingress
      protocol: tcp
      remote_group: {get_resource: server_security_group_1}
  default_server_security_group_rule_2:
    type: OS::Neutron::SecurityGroupRule
    properties:
      security_group: {get_resource: server_security_group_1}
      direction: ingress
      protocol: udp
      remote_group: {get_resource: server_security_group_1}
  default_server_security_group_rule_3:
    type: OS::Neutron::SecurityGroupRule
    properties:
      security_group: {get_resource: server_security_group_1}
      direction: ingress
      protocol: icmp
      remote_group: {get_resource: server_security_group_1}

  lb_security_group_1:
    type: OS::Neutron::SecurityGroup
    properties:
      name: {get_param: sg_lb_name_1}
      rules: [
        {
          direction: ingress,
          protocol: {get_param: sg_lb_protocol_1},
          port_range_min: {get_param: sg_lb_port_1},
          port_range_max: {get_param: sg_lb_port_1},
          remote_ip_prefix: {get_param: sg_lb_remote_ip_1}
        }
      ]
  default_lb_security_group_rule_1:
    type: OS::Neutron::SecurityGroupRule
    properties:
      security_group: {get_resource: lb_security_group_1}
      direction: ingress
      protocol: tcp
      remote_group: {get_resource: lb_security_group_1}
  default_lb_security_group_rule_2:
    type: OS::Neutron::SecurityGroupRule
    properties:
      security_group: {get_resource: lb_security_group_1}
      direction: ingress
      protocol: udp
      remote_group: {get_resource: lb_security_group_1}


  scale_up_policy:
    type: OS::Heat::ScalingPolicy
    properties:
      adjustment_type: { get_param: adjustment_type }
      auto_scaling_group_id: {get_resource: asg}
      cooldown: { get_param: scale_up_cooldown }
      scaling_adjustment: { get_param: scaling_up_adjustment }

  scale_down_policy:
    type: OS::Heat::ScalingPolicy
    properties:
      adjustment_type: { get_param: adjustment_type }
      auto_scaling_group_id: {get_resource: asg}
      cooldown: { get_param: scale_down_cooldown }
      scaling_adjustment: { get_param: scaling_down_adjustment }
  
  cpu_alarm_high:
    type: OS::Aodh::GnocchiAggregationByResourcesAlarm
    properties:
      description: { get_param: alarm_high_description }
      metric: { get_param: metric }
      aggregation_method: { get_param: aggregation_method }
      granularity: { get_param: granularity }
      evaluation_periods: { get_param: evaluation_periods }
      resource_type: { get_param: resource_type }
      comparison_operator: { get_param: high_comparison_operator }
      threshold: { get_param: high_threshold }
      alarm_actions:
        - str_replace:
            template: trust+url
            params:
              url: {get_attr: [scale_up_policy, signal_url]}
      query:
        list_join:
          - ''
          - - {'=': {server_group: {get_param: "OS::stack_id"}}}

  cpu_alarm_low:
    type: OS::Aodh::GnocchiAggregationByResourcesAlarm
    properties:
      description: { get_param: alarm_low_description }
      metric: { get_param: metric }
      aggregation_method: { get_param: aggregation_method }
      granularity: { get_param: granularity }
      evaluation_periods: { get_param: evaluation_periods }
      resource_type: { get_param: resource_type }
      comparison_operator: { get_param: low_comparison_operator }
      threshold: { get_param: low_threshold }
      alarm_actions:
        - str_replace:
            template: trust+url
            params:
              url: {get_attr: [scale_down_policy, signal_url]}
      query:
        list_join:
          - ''
          - - {'=': {server_group: {get_param: "OS::stack_id"}}}

  lb:
    type: OS::Octavia::LoadBalancer
    properties:
      vip_subnet: {get_param: subnet_id}
  
  listener:
    type: OS::Octavia::Listener
    properties:
      loadbalancer: {get_resource: lb}
      protocol: {get_param: lb_listen_protocol}
      protocol_port: {get_param: lb_listen_port}
  
  pool:
    type: OS::Octavia::Pool
    properties:
      listener: {get_resource: listener}
      lb_algorithm: {get_param: lb_algorithm}
      protocol: {get_param: lb_target_protocol}

  lb_monitor:
    type: OS::Octavia::HealthMonitor
    properties:
      pool: { get_resource: pool }
      type: {get_param: lb_health_check_type}
      http_method: { get_param: lb_health_check_http_method }
      url_path: { get_param: lb_health_check_url }
      expected_codes: { get_param: lb_health_check_success_codes }
      delay: { get_param: lb_health_check_interval }
      max_retries: { get_param: lb_health_check_unhealthy_threshold }
      timeout: { get_param: lb_health_check_timeout }

  lb_floating:
    type: OS::Neutron::FloatingIP
    properties:
      floating_network_id: {get_param: external_network_id}
      port_id: {get_attr: [lb, vip_port_id]}

outputs:
  scale_up_url:
    description: Scale Up URL
    value: {get_attr: [scale_up_policy, signal_url]}
    
  scale_down_url:
    description: Scale Down URL
    value: {get_attr: [scale_down_policy, signal_url]}

  LoadBalancerIP:
    description: The IP address of the load balancing pool
    value: {get_attr: [lb, vip_address]}

  LoadBalancerPort:
    description: The IP address of the load balancing pool
    value: {get_attr: [lb, vip_port_id]}
    
  url:
    description: This URL is the "external" URL that can be used to access the lb
    value:
      str_replace:
        template: http://host
        params:
          host: { get_attr: [lb_floating, floating_ip_address] }
       
  gnocchi_query:
    description: Gnocchi Query
    value:
      str_replace:
        template: >
          gnocchi measures aggregation --resource-type instance
          --query 'server_group="asg_server_group"'
          --granularity 300 --aggregation rate:mean -m cpu
        params:
          asg_server_group: { get_param: "OS::stack_id" }
```
<br>

`template_lb_server.yaml`  
```yaml
heat_template_version: 2018-08-31
description: A load-balancer server
parameters:
  instance_name:
    type: string
  key_name:
    type: string
  flavor:
    type: string
  availability_zone:
    type: string 
  security_groups:
    type: string
  image:
    type: string
  volume_size:
    type: string
  volume_type:
    type: string
  delete_on_termination:
    type: boolean
  network_id:
    type: string
  subnet_id:
    type: string
  metadata:
    type: json
  user_data:
    type: string

  pool_id:
    type: string
  lb_target_port:
    type: number


resources:
  server:
    type: OS::Nova::Server
    properties:
      name: { get_param: instance_name }
      key_name: { get_param: key_name }
      flavor: { get_param: flavor }
      availability_zone: { get_param: availability_zone }
      security_groups: 
        - {get_param: security_groups}
      block_device_mapping_v2:
            - boot_index: 0
              delete_on_termination: { get_param: delete_on_termination}
              volume_id: { get_resource: cinder_volume}
      networks:
        - network: {get_param: network_id }
          subnet: { get_param: subnet_id }
      metadata: {get_param: metadata}
      user_data: {get_param: user_data}
      user_data_format: RAW

  cinder_volume:
    type: OS::Cinder::Volume
    properties:
      image: { get_param: image }
      size: { get_param: volume_size }
      volume_type: { get_param: volume_type }
      availability_zone: { get_param: availability_zone }

  # When use snapshot image
  #cinder_volume:
  #  type: OS::Cinder::Volume
  #  properties:
  #    snapshot_id: c71ada9c-db4a-445b-96cf-5a1033a069f7

      
  member:
    type: OS::Octavia::PoolMember
    properties:
      pool: {get_param: pool_id}
      address: {get_attr: [server, first_address]}
      protocol_port: {get_param: lb_target_port}
      subnet: {get_param: subnet_id}

outputs:
  server_ip:
    description: IP Address of the load-balanced server.
    value: { get_attr: [server, first_address] }
  lb_member:
    description: LB member details.
    value: { get_attr: [member, show] }
```
<br>

user_data.sh 파일 안에는 template_lb_server.yaml 파일에서 서버가 생성될 때 넘겨주는 파라미터 파일로 서버의 최초 기동시 동작하기 원하는 스크립트를 작성하여 입력한다.  

`user_data.sh`
```bash
# yum -y install httpd vim 
# systemctl enable httpd.service
# systemctl start httpd.service

# sed -i "s/Require all denied/Require all granted/g" /etc/httpd/conf/httpd.conf
# echo TestPage >> /var/www/html/index.html
```

<br>


<br><br><br>

> Ref: http://www.howardism.org/Technical/OpenStack/using-heat-templates.html  
> Ref: https://docs.openstack.org/heat/train/template_guide/openstack.html  