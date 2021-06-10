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

  +  ### 2.1.2 aws-load-balancer-controller  
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

  +  ### 2.1.3 cloudwatch-agent  
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

  +  ### 2.1.4 fluentd-cloudwatch  





## 2.2 사전 구성 서비스
