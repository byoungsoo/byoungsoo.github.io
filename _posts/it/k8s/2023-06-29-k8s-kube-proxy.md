---
layout: post
title: "Kubernetes kube-proxy 알아보기"
author: "Bys"
category: k8s
date: 2023-06-29 01:00:00
tags: eks node iptables
---

# [kube-proxy](https://kubernetes.io/docs/concepts/overview/components/#kube-proxy)  
- kube-proxy는 워커노드의 컴포넌트로 동작하며 Network proxy 역할을 수행한다. 이는 Kubernetes의 서비스 개념을 구체화한 것이다. 
  > kube-proxy is a network proxy that runs on each node in your cluster, implementing part of the Kubernetes Service concept.
- kube-proxy는 노드의 network rule(ex. iptables rule)을 관리한다. 이러한 network rule들은 파드로의 In/Out 네트워크 통신을 허용한다.  
  > kube-proxy maintains network rules on nodes. These network rules allow network communication to your Pods from network sessions inside or outside of your cluster.
- kube-proxy는 OS의 패킷 필터링 계층을 사용하고 그렇지 않으면 kube-proxy가 트래픽을 전달한다.  
  > kube-proxy uses the operating system packet filtering layer if there is one and it's available. Otherwise, kube-proxy forwards the traffic itself

<br>

## Kube-proxy의 동작 방식

kube-proxy는 [spec.externalTrafficPolicy](https://kubernetes.io/docs/concepts/services-networking/service-traffic-policy/)의 설정에 따라서 라우팅되는 엔드포인트를 필터링 한다[1]. 서비스에는 spec.externalTrafficPolicy 필드가 존재하며 Cluster(Default 설정) 또는 Local 값을 갖으며 서비스의 externalTrafficPolicy 정책이 Local인 경우 트래픽이 랜덤으로 분산되지 않는다.  

- spec.externalTrafficPolicy: Local
externalTrafficPolicy가 Local로 설정된 서비스에 대해서 kube-proxy는 같은 노드에 있는 엔드포인트로 트래픽을 분산한다. [문서](https://kubernetes.io/blog/2022/12/30/advancements-in-kubernetes-traffic-engineering/#:~:text=healthy%20Node%2C%20when-,externalTrafficPolicy%20is%20Local,-One%20such%20scenario)를 확인하면 첫 번째 노드의 A파드에서 B서비스로 요청시 A파드가 존재하는 노드의 kube-proxy는 자신의 노드에 존재하는 1개의 B파드로만 트래픽을 전달한다. 

- spec.externalTrafficPolicy: Cluster(Default)
externalTrafficPolicy가 Cluster로 설정된 서비스에 대해서 kube-proxy는 모든 엔드포인트로 랜덤하게 트래픽을 분산한다. [문서](https://kubernetes.io/blog/2022/12/30/advancements-in-kubernetes-traffic-engineering/#:~:text=Figure%207%3A%20Service%20routing%20when%20internalTrafficPolicy%20is%20Cluster)를 확인하면 첫 번째 노드의 A파드에서 B서비스로 요청시 A파드가 존재하는 노드의 kube-proxy는 iptables를 기반으로 트래픽을 3개의 B파드로 전달한다. 


### [DeepDive] - When externalTrafficPolicy is Cluster.

1. nginx 샘플 배포  

    ```yaml
    apiVersion: apps/v1
    kind: Deployment
    metadata:
      name: nginx
      namespace: test
    spec:
      selector:
        matchLabels:
          app: nginx
      replicas: 2
      template:
        metadata:
          labels:
            app: nginx
        spec:
          containers:
          - name: nginx
            image: nginx
            ports:
            - containerPort: 80
    ---
    apiVersion: v1
    kind: Service
    metadata:
      name: nginx
      namespace: test
      labels:
        app: nginx
      annotations:
    spec:
      type: ClusterIP
      selector:
        app: nginx
      ports:
      - name: http
        port: 80
        protocol: TCP
        targetPort: 80
    ```
    해당 매니페스트를 배포 하면 서비스의 externalTrafficPolicy는 기본 설정인 Cluster 값을 갖는다.  

2. 배포 확인  

    ```bash
    $ kubectl get po -o wide -n test
    NAME                    READY   STATUS    RESTARTS   AGE   IP             NODE                                              NOMINATED NODE   READINESS GATES
    nginx-55f598f8d-25sx9   1/1     Running   0          13m   10.20.11.179   ip-10-20-11-149.ap-northeast-2.compute.internal   <none>           <none>
    nginx-55f598f8d-wcqt7   1/1     Running   0          13m   10.20.10.127   ip-10-20-10-207.ap-northeast-2.compute.internal   <none>           <none>

    $ kubectl get svc -o wide -n test
    NAME    TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)   AGE   SELECTOR
    nginx   ClusterIP   172.20.247.136   <none>        80/TCP    30m   app=nginx
    ```


3. 워커노드의 iptables 확인  

   - PREROUTING 확인 (패킷의 도착지 주소를 변경하는 과정)
     ```bash
     $ iptables -L PREROUTING -v -n -t nat

     Chain PREROUTING (policy ACCEPT 2703 packets, 245K bytes)
     pkts bytes target     prot opt in     out     source               destination
     4823K  502M KUBE-SERVICES  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* kubernetes service portals */
     ```

   - KUBE-SERVICES 확인
     KUBE-SERVICES를 KUBE-SVC-IU46S4VZFN77LK6S target에 대해서는 목적지를 172.20.247.136로 변경하며 172.20.247.136는 test네임스페이스의 nginx서비스 IP주소  
     ```bash
     $ iptables -L KUBE-SERVICES -v -n -t nat

     Chain KUBE-SERVICES (2 references)
     pkts bytes target                     prot opt in     out     source               destination
         0     0 KUBE-SVC-IU46S4VZFN77LK6S  tcp  --  *      *       0.0.0.0/0            172.20.247.136       /* test/nginx:http cluster IP */ tcp dpt:80
     1946  117K KUBE-NODEPORTS             all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* kubernetes service nodeports; NOTE: this must be the last rule in this chain */ ADDRTYPE match dst-type LOCAL
     ```


   - KUBE-SVC-IU46S4VZFN77LK6S 확인
     다시 KUBE-SVC-IU46S4VZFN77LK6S를 확인하면 (KUBE-SEP-XAAQ4E3V5J6GEKUJ, KUBE-SEP-S7XCV467AW272WRX) 두 개의 타겟이 존재하며 랜덤으로 분산이 되지만 통계 확률적으로는 0.5단위로 분산 됨  
     ```bash
     $ iptables -L KUBE-SVC-IU46S4VZFN77LK6S -v -n -t nat

     Chain KUBE-SVC-IU46S4VZFN77LK6S (1 references)
     pkts bytes target                     prot opt in     out     source               destination
         0     0 KUBE-SEP-XAAQ4E3V5J6GEKUJ  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* test/nginx:http -> 10.20.10.127:80 */ statistic mode random probability 0.50000000000
         0     0 KUBE-SEP-S7XCV467AW272WRX  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* test/nginx:http -> 10.20.11.179:80 */
     ```

   - KUBE-SEP-XAAQ4E3V5J6GEKUJ, KUBE-SEP-S7XCV467AW272WRX를 확인하면 최종적으로 172.20.247.136 서비스 IP는 (10.20.10.127, 10.20.11.179)로 목적지가 변경 됨  
     ```bash
     $ iptables -L KUBE-SEP-XAAQ4E3V5J6GEKUJ -v -n -t nat

     Chain KUBE-SEP-XAAQ4E3V5J6GEKUJ (1 references)
     pkts bytes target          prot opt in     out     source               destination
         0     0 KUBE-MARK-MASQ  all  --  *      *       10.20.10.127         0.0.0.0/0            /* test/nginx:http */
         0     0 DNAT            tcp  --  *      *       0.0.0.0/0            0.0.0.0/0            /* test/nginx:http */ tcp to:10.20.10.127:80


     $ iptables -L KUBE-SEP-S7XCV467AW272WRX -v -n -t nat

     Chain KUBE-SEP-S7XCV467AW272WRX (1 references)
     pkts bytes target          prot opt in     out     source               destination
         0     0 KUBE-MARK-MASQ  all  --  *      *       10.20.11.179         0.0.0.0/0            /* test/nginx:http */
         0     0 DNAT            tcp  --  *      *       0.0.0.0/0            0.0.0.0/0            /* test/nginx:http */ tcp to:10.20.11.179:80
     ```


4. 파드의 수를 3개로 늘린 후 KUBE-SVC-IU46S4VZFN77LK6S 확인
  - KUBE-SVC-IU46S4VZFN77LK6S를 확인하면 파드를 3개로 늘린 상황에서는 아래와 같이 트래픽이 랜덤 분산되며 통계적으로 0.3, 0.5, 0.2의 확률이다. 
  ```bash
  $ iptables -L KUBE-SVC-IU46S4VZFN77LK6S -v -n -t nat
  Chain KUBE-SVC-IU46S4VZFN77LK6S (1 references)
  pkts bytes target                     prot opt in     out     source               destination
      0     0 KUBE-SEP-XAAQ4E3V5J6GEKUJ  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* test/nginx:http -> 10.20.10.127:80 */ statistic mode random probability 0.33333333349
      0     0 KUBE-SEP-K34D3XS7VWEHKFPE  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* test/nginx:http -> 10.20.10.218:80 */ statistic mode random probability 0.50000000000
      0     0 KUBE-SEP-S7XCV467AW272WRX  all  --  *      *       0.0.0.0/0            0.0.0.0/0            /* test/nginx:http -> 10.20.11.179:80 */
  ```


<br><br><br>