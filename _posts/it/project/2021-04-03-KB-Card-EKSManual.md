---
layout: post
title: "Project - KBCard - EKS Manual"
author: "Bys"
category: project
date: 2021-04-03 01:00:00
tags: project issue
---


## 목차
### 1. 명령어  
+ ### 1.1 kubectl  

### 2. EKS 구성  
+ ### 2.1 사전 구성 서비스  
  + #### 2.1.1 metrics-server  
  + #### 2.1.2 aws-load-balancer-controller  
  + #### 2.1.3 cloudwatch-agent  
  + #### 2.1.4 fluentd-cloudwatch  
+ ### 2.2 API서비스
  + #### 2.2.1 Frism 연계
  + #### 2.2.2 API서비스 파이프라인
  + #### 2.2.3 API서비스 Helm 배포  
  + #### 2.2.4 API서비스 Values.yaml
  + #### 2.2.5 API서비스 수작업 배포 및 변경
+ ### 2.3 Logging
  + #### 2.3.1 Fluentd 배포
  + #### 2.3.2 ElasticSearch 구성
  + #### 2.3.3 Kibana 구성
  
------

## 1. 명령어  

+ ## 1.1 kubectl  
  ```bash
  # 특정namespace pod 조회 # 상세조회 : -o wide
  kubectl get pods -n <namespace> -o wide

  # 로그 조회
  kubectl logs -f <pod_name> -n <namespace>

  # 서비스 조회
  kubectl get svc -A -o wide

  # 디플로이먼트 조회
  kubectl get deployment -A -o wdie

  # Ingress 조회
  kubectl get ing -A

  # pod 상세 정보 출력
  kubectl describe pod <pod_name> -n <namespace>

  # pod 컨테이너 접속
  kubectl exec -it <pod_name > -n <namespace> /bin/bash

  # POD 자원사용량 확인
  kubectl top pods -A

  # NODE 자원사용량 확인
  kubectl top nodes
  ```


## 2. EKS 구성  

+ ## 2.1 사전 구성 서비스  

  + ### 2.1.1 metrics-server  
    Metric Server 어플리케이션을 클러스터에 배포하여 노드 및 파드의 Metric을 수집  

    -참고
    ```yaml
    apiVersion: v1
    kind: ServiceAccount
    metadata:
      labels:
        k8s-app: metrics-server
      name: metrics-server
      namespace: kube-system
    ---
    apiVersion: rbac.authorization.k8s.io/v1
    kind: ClusterRole
    metadata:
      labels:
        k8s-app: metrics-server
        rbac.authorization.k8s.io/aggregate-to-admin: "true"
        rbac.authorization.k8s.io/aggregate-to-edit: "true"
        rbac.authorization.k8s.io/aggregate-to-view: "true"
      name: system:aggregated-metrics-reader
    rules:
    - apiGroups:
      - metrics.k8s.io
      resources:
      - pods
      - nodes
      verbs:
      - get
      - list
      - watch
    ---
    apiVersion: rbac.authorization.k8s.io/v1
    kind: ClusterRole
    metadata:
      labels:
        k8s-app: metrics-server
      name: system:metrics-server
    rules:
    - apiGroups:
      - ""
      resources:
      - pods
      - nodes
      - nodes/stats
      - namespaces
      - configmaps
      verbs:
      - get
      - list
      - watch
    ---
    apiVersion: rbac.authorization.k8s.io/v1
    kind: RoleBinding
    metadata:
      labels:
        k8s-app: metrics-server
      name: metrics-server-auth-reader
      namespace: kube-system
    roleRef:
      apiGroup: rbac.authorization.k8s.io
      kind: Role
      name: extension-apiserver-authentication-reader
    subjects:
    - kind: ServiceAccount
      name: metrics-server
      namespace: kube-system
    ---
    apiVersion: rbac.authorization.k8s.io/v1
    kind: ClusterRoleBinding
    metadata:
      labels:
        k8s-app: metrics-server
      name: metrics-server:system:auth-delegator
    roleRef:
      apiGroup: rbac.authorization.k8s.io
      kind: ClusterRole
      name: system:auth-delegator
    subjects:
    - kind: ServiceAccount
      name: metrics-server
      namespace: kube-system
    ---
    apiVersion: rbac.authorization.k8s.io/v1
    kind: ClusterRoleBinding
    metadata:
      labels:
        k8s-app: metrics-server
      name: system:metrics-server
    roleRef:
      apiGroup: rbac.authorization.k8s.io
      kind: ClusterRole
      name: system:metrics-server
    subjects:
    - kind: ServiceAccount
      name: metrics-server
      namespace: kube-system
    ---
    apiVersion: v1
    kind: Service
    metadata:
      labels:
        k8s-app: metrics-server
      name: metrics-server
      namespace: kube-system
    spec:
      ports:
      - name: https
        port: 443
        protocol: TCP
        targetPort: https
      selector:
        k8s-app: metrics-server
    ---
    apiVersion: apps/v1
    kind: Deployment
    metadata:
      labels:
        k8s-app: metrics-server
      name: metrics-server
      namespace: kube-system
    spec:
      selector:
        matchLabels:
          k8s-app: metrics-server
      strategy:
        rollingUpdate:
          maxUnavailable: 0
      template:
        metadata:
          labels:
            k8s-app: metrics-server
        spec:
          containers:
          - args:
            - --cert-dir=/tmp
            - --secure-port=443
            - --kubelet-preferred-address-types=InternalIP,ExternalIP,Hostname
            - --kubelet-use-node-status-port
            - --metric-resolution=15s
            image: k8s.gcr.io/metrics-server/metrics-server:v0.5.0
            imagePullPolicy: IfNotPresent
            livenessProbe:
              failureThreshold: 3
              httpGet:
                path: /livez
                port: https
                scheme: HTTPS
              periodSeconds: 10
            name: metrics-server
            ports:
            - containerPort: 443
              name: https
              protocol: TCP
            readinessProbe:
              failureThreshold: 3
              httpGet:
                path: /readyz
                port: https
                scheme: HTTPS
              initialDelaySeconds: 20
              periodSeconds: 10
            resources:
              requests:
                cpu: 100m
                memory: 200Mi
            securityContext:
              readOnlyRootFilesystem: true
              runAsNonRoot: true
              runAsUser: 1000
            volumeMounts:
            - mountPath: /tmp
              name: tmp-dir
          nodeSelector:
            kubernetes.io/os: linux
          priorityClassName: system-cluster-critical
          serviceAccountName: metrics-server
          volumes:
          - emptyDir: {}
            name: tmp-dir
    ---
    apiVersion: apiregistration.k8s.io/v1
    kind: APIService
    metadata:
      labels:
        k8s-app: metrics-server
      name: v1beta1.metrics.k8s.io
    spec:
      group: metrics.k8s.io
      groupPriorityMinimum: 100
      insecureSkipTLSVerify: true
      service:
        name: metrics-server
        namespace: kube-system
      version: v1beta1
      versionPriority: 100
    ```

  + ### 2.1.2 aws-load-balancer-controller  
    Kubernetes Cluster에 Ingress 배포 시 AWS ALB 생성 요청 및 Rule을 추가 하는  Controller 역할  
    
    -참고
    
    ```bash
    #ALB Ingress IAM Policy
    curl -o iam_policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.2.0/docs/install/iam_policy.json
    
    aws iam create-policy \
    --policy-name AWSLoadBalancerControllerIAMPolicy \
    --policy-document file://iam_policy.json
    
    #Asoociate IAM OIDC to Cluster
    ekstcl utils associate-iam-oidc-provider --region=ap-northeast-2 --cluster=ClusterName --approve

    #iamserviceaccount 생성
    export idnumber=`aws sts get-caller-identity | jq -r .Account`
    eksctl create iamserviceaccount \
    --cluster=ClusterName \
    --namespace=kube-system \
    --name=aws-load-balancer-controller \
    --attach-policy-arn=arn:aws:iam::ACCOUNT_ID:policy/AWSLoadBalancerControllerIAMPolicy \
    --override-existing-serviceaccounts \
    --approve
    
    #TargetGroupBinding custom resource definitions
    kubectl apply -k "github.com/aws/eks-charts/stable/aws-load-balancer-controller//crds?ref=master"

    #Install the AWS Load Balancer Controller
    helm upgrade -i aws-load-balancer-controller eks/aws-load-balancer-controller \
    --set clusterName=ClusterName \
    --set serviceAccount.create=false \
    --set serviceAccount.name=aws-load-balancer-controller \
    --set image.repository=222383050459.dkr.ecr.ap-northeast-2.amazonaws.com/opensource-components \
    --set image.tag=aws-load-balancer-controller-v2.2.0 \
    --set enableWaf=false \
    --set enableWafv2=false \
    --set enableShield=false \
    -n kube-system
    ```

  + ### 2.1.3 cloudwatch-agent   
    아래 내용에 configmap for cwagent config: cwagentconfig.json 형식에 ClusterName은 필수로 설정을 해주어야 한다.  
    
    ```yaml
    apiVersion: v1
    kind: Namespace
    metadata:
      name: amazon-cloudwatch
      labels:
        name: amazon-cloudwatch
        
    ---
    apiVersion: v1
    kind: ServiceAccount
    metadata:
      name: cloudwatch-agent
      namespace: amazon-cloudwatch

    ---
    kind: ClusterRole
    apiVersion: rbac.authorization.k8s.io/v1
    metadata:
      name: cloudwatch-agent-role
    rules:
      - apiGroups: [""]
        resources: ["pods", "nodes", "endpoints"]
        verbs: ["list", "watch"]
      - apiGroups: ["apps"]
        resources: ["replicasets"]
        verbs: ["list", "watch"]
      - apiGroups: ["batch"]
        resources: ["jobs"]
        verbs: ["list", "watch"]
      - apiGroups: [""]
        resources: ["nodes/proxy"]
        verbs: ["get"]
      - apiGroups: [""]
        resources: ["nodes/stats", "configmaps", "events"]
        verbs: ["create"]
      - apiGroups: [""]
        resources: ["configmaps"]
        resourceNames: ["cwagent-clusterleader"]
        verbs: ["get","update"]

    ---
    kind: ClusterRoleBinding
    apiVersion: rbac.authorization.k8s.io/v1
    metadata:
      name: cloudwatch-agent-role-binding
    subjects:
      - kind: ServiceAccount
        name: cloudwatch-agent
        namespace: amazon-cloudwatch
    roleRef:
      kind: ClusterRole
      name: cloudwatch-agent-role
      apiGroup: rbac.authorization.k8s.io
      
    ---
    apiVersion: v1
    data:
      # Configuration is in Json format. No matter what configure change you make,
      # please keep the Json blob valid.
      cwagentconfig.json: |
        {
          "logs": {
            "metrics_collected": {
              "kubernetes": {
                "cluster_name": "myClusterNameDev",
                "metrics_collection_interval": 60
              }
            },
            "force_flush_interval": 5,
            "endpoint_override": "logs.ap-northeast-2.amazonaws.com"
          }
        }
    kind: ConfigMap
    metadata:
      name: cwagentconfig
      namespace: amazon-cloudwatch
      
    ---
    apiVersion: apps/v1
    kind: DaemonSet
    metadata:
      name: cloudwatch-agent
      namespace: amazon-cloudwatch
    spec:
      selector:
        matchLabels:
          name: cloudwatch-agent
      template:
        metadata:
          labels:
            name: cloudwatch-agent
        spec:
          containers:
            - name: cloudwatch-agent
              image: amazon/cloudwatch-agent:1.247347.6b250880
              #ports:
              #  - containerPort: 8125
              #    hostPort: 8125
              #    protocol: UDP
              resources:
                limits:
                  cpu:  200m
                  memory: 200Mi
                requests:
                  cpu: 200m
                  memory: 200Mi
              # Please don't change below envs
              env:
                - name: HOST_IP
                  valueFrom:
                    fieldRef:
                      fieldPath: status.hostIP
                - name: HOST_NAME
                  valueFrom:
                    fieldRef:
                      fieldPath: spec.nodeName
                - name: K8S_NAMESPACE
                  valueFrom:
                    fieldRef:
                      fieldPath: metadata.namespace
                - name: CI_VERSION
                  value: "k8s/1.3.6"
              # Please don't change the mountPath
              volumeMounts:
                - name: cwagentconfig
                  mountPath: /etc/cwagentconfig
                - name: rootfs
                  mountPath: /rootfs
                  readOnly: true
                - name: dockersock
                  mountPath: /var/run/docker.sock
                  readOnly: true
                - name: varlibdocker
                  mountPath: /var/lib/docker
                  readOnly: true
                - name: containerdsock
                  mountPath: /run/containerd/containerd.sock
                  readOnly: true
                - name: sys
                  mountPath: /sys
                  readOnly: true
                - name: devdisk
                  mountPath: /dev/disk
                  readOnly: true
          volumes:
            - name: cwagentconfig
              configMap:
                name: cwagentconfig
            - name: rootfs
              hostPath:
                path: /
            - name: dockersock
              hostPath:
                path: /var/run/docker.sock
            - name: varlibdocker
              hostPath:
                path: /var/lib/docker
            - name: containerdsock
              hostPath:
                path: /run/containerd/containerd.sock
            - name: sys
              hostPath:
                path: /sys
            - name: devdisk
              hostPath:
                path: /dev/disk/
          terminationGracePeriodSeconds: 60
          serviceAccountName: cloudwatch-agent
    ```
     

  + ### 2.1.4 fluentd-cloudwatch  
    EKS 로그 시스템을 구축하기 위해 배포 FluentD를 통해 Cloudwatch로 로그를 전송  

    ```yaml
    apiVersion: v1
    kind: ServiceAccount
    metadata:
      name: fluentd
      namespace: amazon-cloudwatch
    ---
    apiVersion: rbac.authorization.k8s.io/v1
    kind: ClusterRole
    metadata:
      name: fluentd-role
    rules:
      - apiGroups: [""]
        resources:
          - namespaces
          - pods
          - pods/logs
        verbs: ["get", "list", "watch"]
    ---
    apiVersion: rbac.authorization.k8s.io/v1
    kind: ClusterRoleBinding
    metadata:
      name: fluentd-role-binding
    roleRef:
      apiGroup: rbac.authorization.k8s.io
      kind: ClusterRole
      name: fluentd-role
    subjects:
      - kind: ServiceAccount
        name: fluentd
        namespace: amazon-cloudwatch
    ---
    apiVersion: v1
    kind: ConfigMap
    metadata:
      name: fluentd-config
      namespace: amazon-cloudwatch
      labels:
        k8s-app: fluentd-cloudwatch
    data:
      fluent.conf: |
        @include containers.conf
        @include systemd.conf
        @include host.conf

        <match fluent.**>
          @type null
        </match>
      containers.conf: |
        <source>
          @type tail
          @id in_tail_container_logs
          @label @containers
          path /var/log/containers/*.log
          exclude_path ["/var/log/containers/cloudwatch-agent*", "/var/log/containers/fluentd*"]
          pos_file /var/log/fluentd-containers.log.pos
          tag *
          read_from_head true
          <parse>
            @type json
            time_format %Y-%m-%dT%H:%M:%S.%NZ
          </parse>
        </source>

        <source>
          @type tail
          @id in_tail_cwagent_logs
          @label @cwagentlogs
          path /var/log/containers/cloudwatch-agent*
          pos_file /var/log/cloudwatch-agent.log.pos
          tag *
          read_from_head true
          <parse>
            @type json
            time_format %Y-%m-%dT%H:%M:%S.%NZ
          </parse>
        </source>

        <source>
          @type tail
          @id in_tail_fluentd_logs
          @label @fluentdlogs
          path /var/log/containers/fluentd*
          pos_file /var/log/fluentd.log.pos
          tag *
          read_from_head true
          <parse>
            @type json
            time_format %Y-%m-%dT%H:%M:%S.%NZ
          </parse>
        </source>

        <label @fluentdlogs>
          <filter **>
            @type kubernetes_metadata
            @id filter_kube_metadata_fluentd
          </filter>

          <filter **>
            @type record_transformer
            @id filter_fluentd_stream_transformer
            <record>
              stream_name ${tag_parts[3]}
              group_name "/aws/containerinsights/#{ENV.fetch('CLUSTER_NAME')}/application"
            </record>
          </filter>

          <match **>
            @type relabel
            @label @NORMAL
          </match>
        </label>

        <label @containers>
          <filter **>
            @type kubernetes_metadata
            @id filter_kube_metadata
          </filter>

          <filter **>
            @type record_transformer
            @id filter_containers_stream_transformer
            enable_ruby
            remove_keys $.docker, $.kubernetes.container_name, $.kubernetes.namespace_name, $.kubernetes.container_image, $.kubernetes.container_image_id, $.kubernetes.pod_id, $.kubernetes.labels, $.kubernetes.master_url, $.kubernetes.namespace_id
            <record>
              stream_name ${tag_parts[3]}
              group_name "/aws/containerinsights/#{ENV.fetch('CLUSTER_NAME')}/application/${record['kubernetes']['namespace_name']}/${record['kubernetes']['container_name']}"
            </record>
          </filter>

          <filter **>
            @type concat
            key log
            multiline_start_regexp /\((D|E|T|I)\)\s\d{2}[:]\d{2}[:]\d{2}\[\d{3}\]/
            separator ""
            flush_interval 5
            timeout_label @NORMAL
          </filter>

          <match **>
            @type relabel
            @label @NORMAL
          </match>
        </label>

        <label @cwagentlogs>
          <filter **>
            @type kubernetes_metadata
            @id filter_kube_metadata_cwagent
          </filter>

          <filter **>
            @type record_transformer
            @id filter_cwagent_stream_transformer
            <record>
              stream_name ${tag_parts[3]}
              group_name "/aws/containerinsights/#{ENV.fetch('CLUSTER_NAME')}/application"
            </record>
          </filter>

          <filter **>
            @type concat
            key log
            multiline_start_regexp /^(date:){0,1}\d{4}[-/]\d{1,2}[-/]\d{1,2}/
            separator ""
            flush_interval 5
            timeout_label @NORMAL
          </filter>

          <match **>
            @type relabel
            @label @NORMAL
          </match>
        </label>

        <label @NORMAL>
          <match **>
            @type cloudwatch_logs
            @id out_cloudwatch_logs_containers
            region "#{ENV.fetch('REGION')}"
            log_group_name_key group_name
            remove_log_group_name_key false
            log_stream_name_key stream_name
            remove_log_stream_name_key true
            auto_create_stream true
            <buffer>
              flush_interval 5
              chunk_limit_size 2m
              queued_chunks_limit_size 32
              retry_forever true
            </buffer>
          </match>
        </label>
      systemd.conf: |
        <source>
          @type systemd
          @id in_systemd_kubelet
          @label @systemd
          filters [{ "_SYSTEMD_UNIT": "kubelet.service" }]
          <entry>
            field_map {"MESSAGE": "message", "_HOSTNAME": "hostname", "_SYSTEMD_UNIT": "systemd_unit"}
            field_map_strict true
          </entry>
          path /var/log/journal
          <storage>
            @type local
            persistent true
            path /var/log/fluentd-journald-kubelet-pos.json
          </storage>
          read_from_head true
          tag kubelet.service
        </source>

        <source>
          @type systemd
          @id in_systemd_kubeproxy
          @label @systemd
          filters [{ "_SYSTEMD_UNIT": "kubeproxy.service" }]
          <entry>
            field_map {"MESSAGE": "message", "_HOSTNAME": "hostname", "_SYSTEMD_UNIT": "systemd_unit"}
            field_map_strict true
          </entry>
          path /var/log/journal
          <storage>
            @type local
            persistent true
            path /var/log/fluentd-journald-kubeproxy-pos.json
          </storage>
          read_from_head true
          tag kubeproxy.service
        </source>

        <source>
          @type systemd
          @id in_systemd_docker
          @label @systemd
          filters [{ "_SYSTEMD_UNIT": "docker.service" }]
          <entry>
            field_map {"MESSAGE": "message", "_HOSTNAME": "hostname", "_SYSTEMD_UNIT": "systemd_unit"}
            field_map_strict true
          </entry>
          path /var/log/journal
          <storage>
            @type local
            persistent true
            path /var/log/fluentd-journald-docker-pos.json
          </storage>
          read_from_head true
          tag docker.service
        </source>

        <label @systemd>
          <filter **>
            @type kubernetes_metadata
            @id filter_kube_metadata_systemd
          </filter>

          <filter **>
            @type record_transformer
            @id filter_systemd_stream_transformer
            <record>
              stream_name ${tag}-${record["hostname"]}
              group_name "/aws/containerinsights/#{ENV.fetch('CLUSTER_NAME')}/dataplane"
            </record>
          </filter>

          <match **>
            @type cloudwatch_logs
            @id out_cloudwatch_logs_systemd
            region "#{ENV.fetch('REGION')}"
            log_group_name_key group_name
            remove_log_group_name_key false
            log_stream_name_key stream_name
            auto_create_stream true
            remove_log_stream_name_key true
            <buffer>
              flush_interval 5
              chunk_limit_size 2m
              queued_chunks_limit_size 32
              retry_forever true
            </buffer>
          </match>
        </label>
      host.conf: |
        <source>
          @type tail
          @id in_tail_dmesg
          @label @hostlogs
          path /var/log/dmesg
          pos_file /var/log/dmesg.log.pos
          tag host.dmesg
          read_from_head true
          <parse>
            @type syslog
          </parse>
        </source>

        <source>
          @type tail
          @id in_tail_secure
          @label @hostlogs
          path /var/log/secure
          pos_file /var/log/secure.log.pos
          tag host.secure
          read_from_head true
          <parse>
            @type syslog
          </parse>
        </source>

        <source>
          @type tail
          @id in_tail_messages
          @label @hostlogs
          path /var/log/messages
          pos_file /var/log/messages.log.pos
          tag host.messages
          read_from_head true
          <parse>
            @type syslog
          </parse>
        </source>

        <label @hostlogs>
          <filter **>
            @type kubernetes_metadata
            @id filter_kube_metadata_host
          </filter>

          <filter **>
            @type record_transformer
            @id filter_containers_stream_transformer_host
            <record>
              stream_name ${tag}-${record["host"]}
              group_name "/aws/containerinsights/#{ENV.fetch('CLUSTER_NAME')}/host"
            </record>
          </filter>

          <match host.**>
            @type cloudwatch_logs
            @id out_cloudwatch_logs_host_logs
            region "#{ENV.fetch('REGION')}"
            log_group_name_key group_name
            remove_log_group_name_key false
            log_stream_name_key stream_name
            remove_log_stream_name_key true
            auto_create_stream true
            <buffer>
              flush_interval 5
              chunk_limit_size 2m
              queued_chunks_limit_size 32
              retry_forever true
            </buffer>
          </match>
        </label>
    ---
    apiVersion: apps/v1
    kind: DaemonSet
    metadata:
      name: fluentd-cloudwatch
      namespace: amazon-cloudwatch
    spec:
      selector:
        matchLabels:
          k8s-app: fluentd-cloudwatch
      template:
        metadata:
          labels:
            k8s-app: fluentd-cloudwatch
          annotations:
            configHash: 8915de4cf9c3551a8dc74c0137a3e83569d28c71044b0359c2578d2e0461825
        spec:
          serviceAccountName: fluentd
          terminationGracePeriodSeconds: 30
          # Because the image's entrypoint requires to write on /fluentd/etc but we mount configmap there which is read-only,
          # this initContainers workaround or other is needed.
          # See https://github.com/fluent/fluentd-kubernetes-daemonset/issues/90
          initContainers:
            - name: copy-fluentd-config
              image: busybox
              command: ['sh', '-c', 'cp /config-volume/..data/* /fluentd/etc']
              volumeMounts:
                - name: config-volume
                  mountPath: /config-volume
                - name: fluentdconf
                  mountPath: /fluentd/etc
            - name: update-log-driver
              image: busybox
              command: ['sh','-c','']
          containers:
            - name: fluentd-cloudwatch
              image: fluent/fluentd-kubernetes-daemonset:v1.7.3-debian-cloudwatch-1.0
              env:
                - name: REGION
                  valueFrom:
                    configMapKeyRef:
                      name: cluster-info
                      key: logs.region
                - name: CLUSTER_NAME
                  valueFrom:
                    configMapKeyRef:
                      name: cluster-info
                      key: cluster.name
                - name: CI_VERSION
                  value: "k8s/1.1.0"
              resources:
                limits:
                  memory: 400Mi
                requests:
                  cpu: 100m
                  memory: 200Mi
              volumeMounts:
                - name: config-volume
                  mountPath: /config-volume
                - name: fluentdconf
                  mountPath: /fluentd/etc
                - name: varlog
                  mountPath: /var/log
                - name: varlibdockercontainers
                  mountPath: /var/lib/docker/containers
                  readOnly: true
                - name: runlogjournal
                  mountPath: /run/log/journal
                  readOnly: true
                - name: dmesg
                  mountPath: /var/log/dmesg
                  readOnly: true
          volumes:
            - name: config-volume
              configMap:
                name: fluentd-config
            - name: fluentdconf
              emptyDir: {}
            - name: varlog
              hostPath:
                path: /var/log
            - name: varlibdockercontainers
              hostPath:
                path: /var/lib/docker/containers
            - name: runlogjournal
              hostPath:
                path: /run/log/journal
            - name: dmesg
              hostPath:
                path: /var/log/dmesg
    ```


+ ## 2.2 API서비스

  + ### 2.2.1 Frism 연계
    KB카드의 EKS 배포를 Frism과 연계 하기 위해 아래와 같은 Script가 존재한다.  
    Frism에서 소스 배포를 하면 기존 Frism 체계와 동일하게 소스를 /fscm 하위 특정 디렉토리로 Copy 하고, 아래와 같은 연계 Script를 호출해 준다.  

    해당 스크립트는 카피된 소스들을 git에 반영해주는 내용이며 git에 반영된 소스들에 의해 pipeline이 자동 수행 된다.  

    ```bash
    #BuildServer /fscm/script/
    #####Frism Parameter#####
    #$1=CM_NUM $2=YYYYMMDDi $3=env
    #########################

    #####SET Variable
    APPLICATION_CODE=ube
    APPLICATION_NAME=ube-api
    DATE=`date +%Y%m%d`

    #####Set branch
    if [ $3 == "dev" ]
    then
      BRANCH_NAME="develop"
    elif [ $3 == "stg" ]
    then
      BRANCH_NAME="stage"
    elif [ $3 == "prd" ]
    then
      BRANCH_NAME="master"
    fi

    echo "##### Start Gitlab Pipeline #####" >> /fscm/logs/$3/${APPLICATION_NAME}_$DATE.log  2>&1;
    echo "BranchName: ${BRANCH_NAME}" >> /fscm/logs/$3/${APPLICATION_NAME}_$DATE.log  2>&1;

    #####ChnageDirectory
    cd /fsgitlab/gitlab/repository/$3/${APPLICATION_NAME} >> /fscm/logs/$3/${APPLICATION_NAME}_$DATE.log  2>&1;
    sudo -u cicdadm git checkout ${BRANCH_NAME} >> /fscm/logs/$3/${APPLICATION_NAME}_$DATE.log  2>&1;

    #####Git Pull
    sudo -u cicdadm git fetch --all >> /fscm/logs/$3/${APPLICATION_NAME}_$DATE.log  2>&1;
    sudo -u cicdadm git reset --hard origin/${BRANCH_NAME} >> /fscm/logs/$3/${APPLICATION_NAME}_$DATE.log  2>&1;
    sudo -u cicdadm git pull origin ${BRANCH_NAME} >> /fscm/logs/$3/${APPLICATION_NAME}_$DATE.log  2>&1;

    if [ $? -eq 0 ]
    then
      echo "Success Command git pull" >> /fscm/logs/$3/${APPLICATION_NAME}_$DATE.log  2>&1;

      #####FileCopy
      sudo cp -fR /fscm/${APPLICATION_CODE}/${APPLICATION_CODE}_$3/$1/${APPLICATION_NAME}/ /fsgitlab/gitlab/repository/$3/ >> /fscm/logs/$3/${APPLICATION_NAME}_$DATE.log  2>&1;
      sudo chown -R cicdadm:grubd /fsgitlab/gitlab/repository/$3/ >> /fscm/logs/$3/${APPLICATION_NAME}_$DATE.log  2>&1;
      echo "Success Command file copy" >> /fscm/logs/$3/${APPLICATION_NAME}_$DATE.log  2>&1;

      #####GitPush
      sudo -u cicdadm git add --all >> /fscm/logs/$3/${APPLICATION_NAME}_$DATE.log  2>&1;
      sudo -u cicdadm git commit -m "Commit from Frism - cicdadm" >> /fscm/logs/$3/${APPLICATION_NAME}_$DATE.log  2>&1;
      sudo -u cicdadm git push origin ${BRANCH_NAME} >> /fscm/logs/$3/${APPLICATION_NAME}_$DATE.log  2>&1;

      if [ $? -eq 0 ]
      then
        echo "Success Command git push" >> /fscm/logs/$3/${APPLICATION_NAME}_$DATE.log  2>&1;
      else
        echo "Fail Command git push" >> /fscm/logs/$3/${APPLICATION_NAME}_$DATE.log  2>&1;
      fi
    elif [ $? -eq 1 ]
    then
      echo "Fail Command git pull" >> /fscm/logs/$3/${APPLICATION_NAME}_$DATE.log  2>&1;
    fi
    ```

  + ### 2.2.2 API서비스 파이프라인
    Frism 배포 후 자동 수행 되는 파이프라인은 총 4단계로 구성 된다.  
    Build, Package, Deploy, Deploycheck 로 나뉘어져 있으며 각 단계에서는 gradle build, docker build, eks deploy, eks deploy check 를 수행 한다.  
    ```yaml
    image: ${ACCOUNT_ID}.dkr.ecr.ap-northeast-2.amazonaws.com/common:docker-stable
    variables:
      DOCKER_DRIVER: overlay2

      APPLICATION_GROUP: ${CI_PROJECT_GROUP}
      APPLICATION_NAME: ${CI_PROJECT_NAME}
      APPLICATION_PORT: 11010
      ENVIRONMENT: ${CI_ENVIRONMENT}
      AWS_REGION: "ap-northeast-2"
      # Jennifer
      JENNIFER_MANAGER_IP: 10.95.252.10
      JENNIFER_MANAGER_PORT: 5000
      # DockerBuild
      REGISTRY_URL: ${ACCOUNT_ID}.dkr.ecr.ap-northeast-2.amazonaws.com
      
    stages:
      - build
      - package
      - deploy
      - deploycheck

    .assume-role-template: &assume_role
      - if [ "${CI_COMMIT_BRANCH}" == "develop" ]; then
          export ACCOUNT_NO=${DEV_ACCOUNT_NO};
          export ASSUME_ROLE_NAME=${DEV_ASSUME_ROLE_NAME};
          export ROLE_SESSION_NAME=${DEV_ROLE_SESSION_NAME};
        elif [ "${CI_COMMIT_BRANCH}" == "stage" ]; then
          export ACCOUNT_NO=${STG_ACCOUNT_NO};
          export ASSUME_ROLE_NAME=${STG_ASSUME_ROLE_NAME};
          export ROLE_SESSION_NAME=${STG_ROLE_SESSION_NAME};
        elif [ "${CI_COMMIT_BRANCH}" == "master" ]; then
          export ACCOUNT_NO=${PRD_ACCOUNT_NO};
          export ASSUME_ROLE_NAME=${PRD_ASSUME_ROLE_NAME};
          export ROLE_SESSION_NAME=${PRD_ROLE_SESSION_NAME};
        fi
      - echo ${ENVIRONMENT}

      - ASSUME_ROLE_CREDENTIALS=$(aws sts assume-role --role-arn arn:aws:iam::${ACCOUNT_NO}:role/${ASSUME_ROLE_NAME} --role-session-name ${ROLE_SESSION_NAME})
      - export AWS_ACCESS_KEY_ID=$(echo $ASSUME_ROLE_CREDENTIALS | jq .Credentials.AccessKeyId | sed 's/"//g')
      - export AWS_SECRET_ACCESS_KEY=$(echo $ASSUME_ROLE_CREDENTIALS | jq .Credentials.SecretAccessKey | sed 's/"//g')
      - export AWS_SESSION_TOKEN=$(echo $ASSUME_ROLE_CREDENTIALS | jq .Credentials.SessionToken | sed 's/"//g')
      - $(aws ecr get-login --no-include-email --region ${AWS_REGION})


    gradle-build:
      image: ${REGISTRY_URL}/common:gradle6.5-jdk8
      stage: build
      script:
        # Env
        - if [ "${CI_COMMIT_BRANCH}" == "develop" ]; then
              export ENVIRONMENT="dev";
          elif [ "${CI_COMMIT_BRANCH}" == "stage" ]; then
              export ENVIRONMENT="stg";
          elif [ "${CI_COMMIT_BRANCH}" == "master" ]; then
              export ENVIRONMENT="prd";
          fi
        - echo ${ENVIRONMENT}
        ## File Change ##
        # Build
        - cp env/${ENVIRONMENT}/build.gradle build.gradle
        # Tomcat
        - cp env/${ENVIRONMENT}/context.xml context.xml
        - cp env/${ENVIRONMENT}/server.xml server.xml
        # Devon
        - rm -rf src/main/webapp/WEB-INF/devonhome/conf
        - rm -rf src/main/webapp/WEB-INF/devonhome-batch/conf 
        - cp -r env/${ENVIRONMENT}/devonhome/conf src/main/webapp/WEB-INF/devonhome/conf
        - cp -r env/${ENVIRONMENT}/devonhome-batch/conf src/main/webapp/WEB-INF/devonhome-batch/conf 

        # Build
        - gradle clean build

      artifacts:
        when: always
        paths:
          - build/
        expire_in: 1 days

      only:
        - develop
        - stage
        - master


    docker-build:
      stage: package
      script:
        # Env
        - if [ "${CI_COMMIT_BRANCH}" == "develop" ]; then
              export ENVIRONMENT="dev";
              export JENNIFER_DOMAIN_ID="1011";
          elif [ "${CI_COMMIT_BRANCH}" == "stage" ]; then
              export ENVIRONMENT="stg";
              export JENNIFER_DOMAIN_ID="1011";
          elif [ "${CI_COMMIT_BRANCH}" == "master" ]; then
              export ENVIRONMENT="prd";
              export JENNIFER_DOMAIN_ID="1012";
          fi
        - echo ${ENVIRONMENT}
        
        # Change Variables
        - sed -i "s/<APPLICATION_NAME>/${CI_PROJECT_NAME}/g" env/${ENVIRONMENT}/Dockerfile
        - sed -i "s/<APPLICATION_PORT>/${APPLICATION_PORT}/g" env/${ENVIRONMENT}/Dockerfile
        - sed -i "s/<ENVIRONMENT>/${ENVIRONMENT}/g" env/${ENVIRONMENT}/Dockerfile

        - sed -i "s/<JENNIFER_MANAGER_IP>/${JENNIFER_MANAGER_IP}/g" env/${ENVIRONMENT}/Dockerfile
        - sed -i "s/<JENNIFER_MANAGER_PORT>/${JENNIFER_MANAGER_PORT}/g" env/${ENVIRONMENT}/Dockerfile
        - sed -i "s/<JENNIFER_DOMAIN_ID>/${JENNIFER_DOMAIN_ID}/g" env/${ENVIRONMENT}/Dockerfile
        
        - sed -i "s/<APPLICATION_NAME>/${APPLICATION_NAME}/g" env/${ENVIRONMENT}/server.xml
        - sed -i "s/<APPLICATION_PORT>/${APPLICATION_PORT}/g" env/${ENVIRONMENT}/server.xml
        
        # Change Files
        - cp env/${ENVIRONMENT}/Dockerfile Dockerfile

        # AWS ECR Login
        - $(aws ecr get-login --no-include-email --region ${AWS_REGION})

        ### Docker Build
        - docker build -t ${APPLICATION_NAME}:${CI_COMMIT_SHORT_SHA} .
        - docker tag ${APPLICATION_NAME}:${CI_COMMIT_SHORT_SHA} ${REGISTRY_URL}/${ENVIRONMENT}-${APPLICATION_NAME}:${CI_COMMIT_SHORT_SHA}
        - docker tag ${APPLICATION_NAME}:${CI_COMMIT_SHORT_SHA} ${REGISTRY_URL}/${ENVIRONMENT}-${APPLICATION_NAME}:latest

        ### Docker Push into ECR
        - $(aws ecr get-login --no-include-email --region ${AWS_REGION})
        - docker push ${REGISTRY_URL}/${ENVIRONMENT}-${APPLICATION_NAME}:${CI_COMMIT_SHORT_SHA}
        - docker push ${REGISTRY_URL}/${ENVIRONMENT}-${APPLICATION_NAME}:latest

        ### Docker Delete Images
        - docker rmi ${APPLICATION_NAME}:${CI_COMMIT_SHORT_SHA}
        - docker rmi ${REGISTRY_URL}/${ENVIRONMENT}-${APPLICATION_NAME}:${CI_COMMIT_SHORT_SHA}
        - docker rmi ${REGISTRY_URL}/${ENVIRONMENT}-${APPLICATION_NAME}:latest

      only:
        - develop
        - stage
        - master
      

    # Deploy
    deploy:
      stage: deploy
      image: ${REGISTRY_URL}/common:helm-deploy
      script:
        # Env
        - if [ "${CI_COMMIT_BRANCH}" == "develop" ]; then
              export ENVIRONMENT="dev";
              export EKS_CLUSTER_NAME="mydata-cluster-dev";
              export HELM_VALUES_FILE="values-develop.yaml";
              export KUBECONFIG=~/.kube/dev-config;
          elif [ "${CI_COMMIT_BRANCH}" == "stage" ]; then
              export ENVIRONMENT="stg";
              export EKS_CLUSTER_NAME="mydata-cluster-stg";
              export HELM_VALUES_FILE="values-stage.yaml";
              export KUBECONFIG=~/.kube/stg-config;
          elif [ "${CI_COMMIT_BRANCH}" == "master" ]; then
              export ENVIRONMENT="prd";
              export EKS_CLUSTER_NAME="mydata-cluster-prd";
              export HELM_VALUES_FILE="values-master.yaml";
              export KUBECONFIG=~/.kube/prd-config;
          fi
          
        - echo ${ENVIRONMENT}

        #- aws eks update-kubeconfig --name ${EKS_CLUSTER_NAME} --kubeconfig ./kubeconfig_cli --region ${AWS_REGION}
        #- export KUBECONFIG=./kubeconfig_cli

        ## Change Variables
        - sed -i "s/<REGISTRY_URL>/${REGISTRY_URL}/g" helm/${HELM_VALUES_FILE}
        - sed -i "s/<ENVIRONMENT>/${ENVIRONMENT}/g" helm/${HELM_VALUES_FILE}
        - sed -i "s/<APPLICATION_NAME>/${CI_PROJECT_NAME}/g" helm/${HELM_VALUES_FILE}
        - sed -i "s/<APPLICATION_PORT>/${APPLICATION_PORT}/g" helm/${HELM_VALUES_FILE}
        - sed -i "s/<CI_COMMIT_SHORT_SHA>/${CI_COMMIT_SHORT_SHA}/g" helm/${HELM_VALUES_FILE}
        - sed -i "s/<APPLICATION_GROUP>/${APPLICATION_GROUP}/g" helm/${HELM_VALUES_FILE}
        
        - sed -i "s/<EKS_INGRESS_SUBNET>/${EKS_INGRESS_SUBNET}/g" helm/${HELM_VALUES_FILE}
        - sed -i "s/<EKS_INGRESS_SG>/${EKS_INGRESS_SG}/g" helm/${HELM_VALUES_FILE}
        
        - sed -i "s/<APPLICATION_NAME>/${CI_PROJECT_NAME}/g" helm/Chart.yaml
        - sed -i "s/<ENVIRONMENT>/${ENVIRONMENT}/g" helm/Chart.yaml

        - helm upgrade -i --debug ${APPLICATION_NAME}-${ENVIRONMENT} ./helm/ -f ./helm/${HELM_VALUES_FILE}
        -n ${APPLICATION_NAME}
        

      only:
        - develop
        - stage
        - master

    # Deploy-Check 
    deploycheck:
      stage: deploycheck
      image: ${REGISTRY_URL}/common:helm-deploy
      script:
        # Env
        - if [ "${CI_COMMIT_BRANCH}" == "develop" ]; then
              export ENVIRONMENT="dev";
              export EKS_CLUSTER_NAME="MyDataAPICluster";
              export KUBECONFIG=~/.kube/dev-config;
          elif [ "${CI_COMMIT_BRANCH}" == "stage" ]; then
              export ENVIRONMENT="stg";
              export EKS_CLUSTER_NAME="mydata-cluster-stg";
              export KUBECONFIG=~/.kube/stg-config;
          elif [ "${CI_COMMIT_BRANCH}" == "master" ]; then
              export ENVIRONMENT="prd";
              export EKS_CLUSTER_NAME="mydata-cluster-prd";
              export KUBECONFIG=~/.kube/prd-config;
          fi

        - echo ${ENVIRONMENT}
        #- aws eks update-kubeconfig --name ${EKS_CLUSTER_NAME} --kubeconfig ./kubeconfig_cli --region ${AWS_REGION}
        #- export KUBECONFIG=./kubeconfig_cli
        
        # Check Status
        - kubectl rollout status deployment ${APPLICATION_NAME}-${ENVIRONMENT}-deployment -n ${APPLICATION_NAME}

      only:
        - develop
        - stage
        - master
    ```
    
  + ### 2.2.3 API서비스 Helm 배포  
    API 서비스는 Helm을 통해 배포 한다. templates 하위에는 deployment.yaml, hpa.yaml, ingress.yaml, service.yaml 이 존재하며 아래의 명령어를 통해 templates하위 자원들을 배포한다.  
    ```bash
    helm upgrade -i --debug ${APPLICATION_NAME}-${ENVIRONMENT} ./helm/ -f ./helm/${HELM_VALUES_FILE} -n ${APPLICATION_NAME}
    ```
    helm/values-${ENVIRONMENT}.yaml 파일에서 모든 values 값을 가지고 있으며 몇 몇 중복되는 값들의 경우 변수 처리를 하고 gitlab-ci.yml 파일에서 변경한다.  

    실제 배포되는 자원들의 값을 변경하고 싶을 때는 values.yaml 파일을 수정하도록 한다.  
    
  + #### 2.2.4 API서비스 Values.yaml  
    Values 값에는 EKS에 배포되는 모든 설정 및 값들이 들어 있다. 
    Values에 들어가는 값들은 각 서비스별 yaml파일에 맞게 순서대로 정리를 해놨다. 따라서 Deployment.yaml 파일에 있는 값을 변경하고 싶다면 주석에 deployment.yaml을 찾아 안에 해당하는 values 값들을 수정해주면 된다.      

    `SAMPLE values-develop.yaml`
    ```yaml
    ############ Common ############
    ############ Deployment.yaml ############
    deployment:
      strategy:
        type: RollingUpdate
        rollingUpdate:
          maxSurge: 1
          maxUnavailable: 0

    replicaCount: 1

    imagePullSecrets: []

    volumes:
      - name: ba-scp-volume
        hostPath:
          path: /fsutil/scp/ba_scp
          type: Directory

    volumeMounts:
      - mountPath: /fsutil/scp/ba_scp
        name: ba-scp-volume

    image:
      repository: "<REGISTRY_URL>/<ENVIRONMENT>-<APPLICATION_NAME>"
      tag: "<CI_COMMIT_SHORT_SHA>"
      pullPolicy: Always

    containerPort: <APPLICATION_PORT>

    resources:
      requests:
        cpu: 800m
        memory: 1536Mi
      limits:
        cpu: 1600m
        memory: 1536Mi

    nameOverride: ""
    fullnameOverride: ""

    nodeSelector: {}

    tolerations: []

    affinity:
      nodeAffinity:
        requiredDuringSchedulingIgnoredDuringExecution:
            nodeSelectorTerms:
            - matchExpressions:
              - key: node-group-type
                operator: In
                values:
                - <APPLICATION_GROUP>
      podAffinity:
        preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 99
            podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: "app.kubernetes.io/name"
                  operator: In
                  values:
                  - <APPLICATION_NAME>-<ENVIRONMENT>
              topologyKey: "kubernetes.io/hostname"
          - weight: 100
            podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: "app.kubernetes.io/name"
                  operator: In
                  values:
                  - <APPLICATION_NAME>-<ENVIRONMENT>
              topologyKey: "failure-domain.beta.kubernetes.io/zone"


    ############ service.yaml ############
    service:
      type: ClusterIP
      port: <APPLICATION_PORT>


    ############ ingress.yaml ############
    ingress:
      enabled: true
      annotations:
        kubernetes.io/ingress.class: alb
        alb.ingress.kubernetes.io/group.name: <APPLICATION_GROUP>-<ENVIRONMENT>
        alb.ingress.kubernetes.io/subnets: SEOUL-MyData-DEV-A-WEB-DHCP-SBN, SEOUL-MyData-DEV-C-WEB-DHCP-SBN
        alb.ingress.kubernetes.io/scheme : internal
        alb.ingress.kubernetes.io/security-groups: SEOUL-MyData-DEV-EKSALB-OPENBANKING-SG
        alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}]'
        alb.ingress.kubernetes.io/healthcheck-path: /<APPLICATION_NAME>/
        alb.ingress.kubernetes.io/healthcheck-interval-seconds: '15'
        alb.ingress.kubernetes.io/healthcheck-timeout-seconds: '10'
        alb.ingress.kubernetes.io/healthy-threshold-count: '2'
        alb.ingress.kubernetes.io/unhealthy-threshold-count: '4'
        alb.ingress.kubernetes.io/success-codes: '200'
        alb.ingress.kubernetes.io/target-type: ip
      rules:
        - http:
            paths:
            - path: /<APPLICATION_NAME>
              pathType: Prefix
              backend:
                service:
                  name: <APPLICATION_NAME>-<ENVIRONMENT>-service
                  port:
                    number: <APPLICATION_PORT>
      tls: []
      

    ############ hpa.yaml ############
    autoscaling:
      enabled: true
      minReplicas: 1
      maxReplicas: 4
      targetCPUUtilizationPercentage: 70
      targetMemoryUtilizationPercentage: 80

    ############ serviceaccount.yaml ############
    serviceAccount:
      # Specifies whether a service account should be created
      create: false
      # Annotations to add to the service account
      annotations: {}
      # The name of the service account to use.
      # If not set and create is true, a name is generated using the fullname template
      name:   
    ```

  + #### 2.2.5 API서비스 수작업 배포 및 변경  
  
    수작업 배포를 원할 경우에는 gitlab 서비스에 접속하여 CI/CD - Pipeline에서 배포된 소스의 deploy 단계에 들어가서 retry를 누른다.  
    Repository -> CI/CD -> Pipeline -> Deploy (Stages의 3번째 단계) -> Retry

    만약 비지니스 로직으로 인한 장애 발생 시 에는 rollback의 실행을 누르면 바로 이 전 버전의 백업 태그로 돌악나다.  
    
    
    
+ ## 2.3 Logging
  + ### 2.3.1 Fluentd 배포  
    FluentD 배포의 경우 사전구성단계의 2.1.4 fluentd-cloudwatch 를 참고  
    
  + ### 2.3.2 ElasticSearch 구성  
    *ElasticSearch 구성 문서 참고

  + ### 2.3.3 Kibana 구성  
    *Kibana 운영 환경 설정 가이드 문서 참고
