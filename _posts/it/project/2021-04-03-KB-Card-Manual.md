---
layout: post
title: "Project - KBCard - EKS Manual"
author: "Bys"
category: project
date: 2021-04-03 01:00:00
tags: project issue
---

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
    ```yaml
    # create amazon-cloudwatch namespace
    apiVersion: v1
    kind: Namespace
    metadata:
      name: amazon-cloudwatch
      labels:
        name: amazon-cloudwatch
        
    # create cwagent service account and role binding
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
      
    # create configmap for cwagent config
    apiVersion: v1
    data:
      # Configuration is in Json format. No matter what configure change you make,
      # please keep the Json blob valid.
      cwagentconfig.json: |
        {
          "logs": {
            "metrics_collected": {
              "kubernetes": {
                "cluster_name": "{{cluster_name}}",
                "metrics_collection_interval": 60
              }
            },
            "force_flush_interval": 5
          }
        }
    kind: ConfigMap
    metadata:
      name: cwagentconfig
      namespace: amazon-cloudwatch
      
    # deploy cwagent as daemonset
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


## 2.2 사전 구성 서비스

+ ### 2.2.1 API서비스 (UBE, UBD, UBF)
  API 서비스는 Helm을 통해 배포 하며 