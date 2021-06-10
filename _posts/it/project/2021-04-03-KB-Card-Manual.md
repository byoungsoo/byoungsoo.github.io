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
  # namespace 를 명령줄 마다 계속치기 귀찮을 때
  kubectl config set-context --current --namespace=co

  # namespace 가 co인 pod 조회
  kubectl get pods --namespace co

  # namespace 가 co인 pod 조회
  # 상세조회 : -o wide
  # 실행중인 상태 : --field-selector=status.phase=Running
  kubectl get pods --namespace co -o wide --field-selector=status.phase=Running

  # 로그 조회
  kubectl logs -f co-dev-6cd76d78bd-ps84g -c co --namespace co

  # 서비스 조회
  kubectl get services --all-namespaces -o wide | grep oz

  # 디플로이먼트 조회
  kubectl get deployment --all-namespaces -o wide

  # Ingress 조회
  kubectl get ing --all-namespaces

  kubectl exec ing --all-namespaces

  # pod 상세 정보 출력
  kubectl describe pod co-dev-7b6fc4b7c7-4tt2w --namespace co

  # pod 컨테이너 접속
  kubectl exec co-dev-7b6fc4b7c7-4tt2w -c co --namespace co bash

  # 노드에 할당된 POD 등 상세정보 조회
  kubectl describe nodes ip-10-253-136-163.ap-northeast-2.compute.internal

  # POD 자원사용량 확인
  kubectl top pods -A
  ```
  ```


## 2. EKS 구성  

+ ## 2.1 사전 구성 서비스  

  + ### 2.1.1 metrics-server  
    Metric Server 어플리케이션을 클러스터에 배포하여 노드 및 파드의 Metric을 수집
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

  +  ### 2.1.3 cloudwatch-agent  

  +  ### 2.1.4 fluentd-cloudwatch  





## 2.2 사전 구성 서비스
