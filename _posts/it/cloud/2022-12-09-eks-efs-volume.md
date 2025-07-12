---
layout: post
title: "EFS CSI Driver를 통한 EFS PersistentVolume 사용하기"
author: "Bys"
category: cloud
date: 2022-12-09 01:00:00
tags: kubernetes eks efs csi controller
---

# EFS CSI driver
EFS CSI driver는 Amazon EKS 클러스터가 영구 볼륨을 위해 Amazon EFS 볼륨의 수명 주기를 관리할 수 있게 해주는 CSI 인터페이스를 제공한다. 

## 1. [Install](https://docs.aws.amazon.com/eks/latest/userguide/efs-csi.html)
1. Policy 생성
2. ServiceAccount 생성 및 IAM Role생성 / 매핑 
3. aws-efs-csi-driver 배포 

## 2. EFS CSI driver의 동작방식
EFS CSI driver는 Pod가 EFS스토리지에 TLS암호화를 통해 접근할 수 있도록 mount와 stunnel 프로세스의 조합으로 동작한다.
- NFS스토리지는 노드의 127.0.0.1 주소의 random-port로 마운트된다.
- stunnel은 TLS를 사용하지 않는 TCP 서비스에 TLS 암호화하여 Proxy하도록 하는 서비스다. 
- stunnel은 127.0.0.1:20149 에서 fs-057778ed087bb0e63.efs.ap-northeast-2.amazonaws.com:2049로 트래픽을 proxy하도록 설정되어있다. 

자세한 내용은 테스트를 통해 설명하며 테스트를 위해 아래의 내용을 배포한다.  
1. `PVC & Pod 배포`  
    ```yaml
    apiVersion: v1
    kind: PersistentVolumeClaim
    metadata:
      name: netutil-efs-claim1
      namespace: test
    spec:
      accessModes:
        - ReadWriteMany
      storageClassName: efs-eks-v122
      resources:
        requests:
          storage: 5Gi
    ---
    apiVersion: v1
    kind: Pod
    metadata:
      name: netutils-efs-1
      namespace: test
    spec:
      containers:
        - name: netutil-efs-1
          image: public.ecr.aws/w0m8q0d5/common:bys-netutil
          command: ["/bin/sh"]
          args: ["-c", "while true; do echo $(date -u) >> /data/out1; sleep 5; done"]
          volumeMounts:
            - name: persistent-storage
              mountPath: /data
      volumes:
        - name: persistent-storage
          persistentVolumeClaim:
            claimName: netutil-efs-claim1
    ```

2. `워커노드에 접속`
    ```bash
    # pvc는 아래와 같이 127.0.0.1:20149 주소로 mount 
    $ mount | grep efs
    127.0.0.1:/ on /var/lib/kubelet/pods/83fc554d-70bb-4919-9a34-2242214a08d2/volumes/kubernetes.io~csi/pvc-4395bf6c-0756-4c8b-bdca-85f300ce3cac/mount type nfs4 (rw,relatime,vers=4.1,rsize=1048576,wsize=1048576,namlen=255,hard,noresvport,proto=tcp,port=20149,timeo=600,retrans=2,sec=sys,clientaddr=127.0.0.1,local_lock=none,addr=127.0.0.1)

    # stunnel 프로세스는 아래와 같이 127.0.0.1주소의 20149 포트를 LISTEN
    $ netstat -anop | grep stunnel
    tcp        0      0 127.0.0.1:20149         0.0.0.0:*               LISTEN      17177/stunnel        off (0.00/0/0)

    # 아래와 같이 stunnel의 구성파일 확인 
    $ ps -ef | grep stunnel
    root     17177 28085  0 12:42 ?        00:00:00 /usr/bin/stunnel /var/run/efs/stunnel-config.fs-11112222333344445.var.lib.kubelet.pods.83fc554d-70bb-4919-9a34-2242214a08d2.volumes.kubernetes.io~csi.pvc-4395bf6c-0756-4c8b-bdca-85f300ce3cac.mount.20149

    $ cat /var/run/efs/stunnel-config.fs-11112222333344445.var.lib.kubelet.pods.83fc554d-70bb-4919-9a34-2242214a08d2.volumes.kubernetes.io~csi.pvc-4395bf6c-0756-4c8b-bdca-85f300ce3cac.mount.20149
    fips = no
    foreground = yes
    socket = l:SO_REUSEADDR=yes
    socket = a:SO_BINDTODEVICE=lo
    [efs]
    client = yes
    accept = 127.0.0.1:20149
    connect = fs-11112222333344445.efs.ap-northeast-2.amazonaws.com:2049
    sslVersion = TLSv1.2
    renegotiation = no
    TIMEOUTbusy = 20
    TIMEOUTclose = 0
    TIMEOUTidle = 70
    delay = yes
    verify = 2
    CAfile = /etc/amazon/efs/efs-utils.crt
    cert = /var/run/efs/fs-11112222333344445.var.lib.kubelet.pods.83fc554d-70bb-4919-9a34-2242214a08d2.volumes.kubernetes.io~csi.pvc-4395bf6c-0756-4c8b-bdca-85f300ce3cac.mount.20149+/certificate.pem
    key = /etc/amazon/efs/privateKey.pem
    checkHost = fs-11112222333344445.efs.ap-northeast-2.amazonaws.com
    ```
    즉, NFS스토리지는 127.0.0.1:20149 주소로 mount가 되며 stunnel 프로세스는 127.0.0.1:20149의 데이터를 fs-11112222333344445.efs.ap-northeast-2.amazonaws.com:2049로 암호화 하여 proxy하는 역할을 한다.  

<br>

## 3. Dynamic Provisioning 동작방법

![storage001](/assets/it/cloud/eks/storage001.png){: width="50%" height="auto"}
  
1. [StorageClass](https://kubernetes.io/docs/concepts/storage/storage-classes/)를 정의한다. StorageClass는 provisioner, parameters, reclaimPolicy 필드를 포함하는데 이는 dynamic provisioning을 통해 PV를 생성 할 때 사용된다. 
   - provisioner는 PV를 provisioning할 때 어떤 볼륨 plugin을 사용할지 결정한다. 
   - Parameters는 사용되는 provisioner에 따라 허용되는 값이 달라진다. EFS CSI driver에서는 'efs.csi.aws.com' provisioner를 사용하며 provisioningMode, fileSystemId, directoryPerms등을 허용한다. 자세한 내용은 링크 참고 [Parameters](https://github.com/kubernetes-sigs/aws-efs-csi-driver#storage-class-parameters-for-dynamic-provisioning).
   - StorageClass에 의해 동적으로 생성 된 PV는 reclaimPolicy필드를 갖는데 'Delete' or 'Retain' 값을 갖는다. 

2. Developer는 생성되어있는 cluster에서 StorageClass들을 리스트하여 각각의 정의를 보고 어떤 볼륨을 생성할지 결정한다. 그리고 원하는 StorageClass의 볼륨을 생성하기 위한 요청을 진행하게 되는데 이 내용을 담은 것이 PVC다. 

3. PVC를 요청하게 되면 provisioner는 요청을 받아들여 PV를 생성하게 된다. 이 요청을 처리하는 것은 실제로 efs-csi-controller의 csi-provisioner 컨테이너에서 처리한다. 따라서, 아래와 같이 efs-csi-controller의 csi-provisioner 컨테이너 로그를 확인한 상태로 PVC를 생성하면 PV가 생성되는 로그를 확인할 수 있다.  
    ```bash
    $ kubectl logs -f -l app=efs-csi-controller -c csi-provisioner -n kube-system

    I1208 16:38:37.078570       1 controller.go:1332] provision "default/efs-claim-1" class "efs-sc": started
    I1208 16:38:37.079182       1 event.go:282] Event(v1.ObjectReference{Kind:"PersistentVolumeClaim", Namespace:"default", Name:"efs-claim-1", UID:"bb393459-1f2b-494a-b408-a0f5bf252ea0", APIVersion:"v1", ResourceVersion:"16216171", FieldPath:""}): type: 'Normal' reason: 'Provisioning' External provisioner is provisioning volume for claim "default/efs-claim-1"
    I1208 16:38:37.263884       1 controller.go:838] successfully created PV pvc-bb393459-1f2b-494a-b408-a0f5bf252ea0 for PVC efs-claim-1 and csi volume name fs-0821cfc3a8bee7b2d::fsap-0cfd2a2c616b08d8d
    I1208 16:38:37.263922       1 controller.go:1439] provision "default/efs-claim-1" class "efs-sc": volume "pvc-bb393459-1f2b-494a-b408-a0f5bf252ea0" provisioned
    I1208 16:38:37.263981       1 controller.go:1456] provision "default/efs-claim-1" class "efs-sc": succeeded
    I1208 16:38:37.271453       1 event.go:282] Event(v1.ObjectReference{Kind:"PersistentVolumeClaim", Namespace:"default", Name:"efs-claim-1", UID:"bb393459-1f2b-494a-b408-a0f5bf252ea0", APIVersion:"v1", ResourceVersion:"16216171", FieldPath:""}): type: 'Normal' reason: 'ProvisioningSucceeded' Successfully provisioned volume pvc-bb393459-1f2b-494a-b408-a0f5bf252ea0
    ```

4. 볼륨 바인딩과 dynamic provisioning을 통한 PV가 생성되는 시점은 StorageClass의 volumeBindingMode와 값과 관련이 있다. 'Immediate' 모드에서는 PVC가 생성되는 시점에 즉시 volume binding과 dynamic provisioning을 통한 PV가 생성된다. 'WaitForFirstConsumer' 모드에서는 binding과 PV의 provisioning의 시점을 Pod가 PVC를 사용하기 전까지 지연시킨다.  
아래 테스트에서 PVC를 배포하면 바로 PV가 생성되며 binding상태가 된다.  

## 4. [Dynamic Provisioning Test](https://github.com/kubernetes-sigs/aws-efs-csi-driver/tree/5e1fcd3e915d62d3b091c6de780ff9e6816f3a7b/examples/kubernetes)

`sc1.yaml`  
```yaml
kind: StorageClass
apiVersion: storage.k8s.io/v1
metadata:
  name: efs-sc-1
provisioner: efs.csi.aws.com
mountOptions:
  - tls
volumeBindingMode: Immediate # Default
reclaimPolicy: Delete # Default
parameters:
  provisioningMode: efs-ap
  fileSystemId: fs-09a658ebf884ec043
  directoryPerms: "700"
  uid: "1000"
  gid: "1000"
  gidRangeStart: "1000" # optional
  gidRangeEnd: "2000" # optional
  basePath: "/dynamic_provisioning" # optional
```

`pvc1.yaml`  
```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: efs-claim-1
spec:
  accessModes:
    - ReadWriteMany
  storageClassName: efs-sc-1
  resources:
    requests:
      storage: 5Gi
```

`pod1.yaml`  
```yaml
apiVersion: v1
kind: Pod
metadata:
  name: efs-app-1
spec:
  containers:
    - name: app
      image: centos
      command: ["/bin/sh"]
      args: ["-c", "while true; do echo $(date -u) >> /data/out1; sleep 5; done"]
      volumeMounts:
        - name: persistent-storage
          mountPath: /data
  volumes:
    - name: persistent-storage
      persistentVolumeClaim:
        claimName: efs-claim-1
```

<br>

StorageClass, PVC를 배포하면 PV가 즉시 생성되며 PVC의 상태가 Bound상태로 변경된다. Pod를 배포하며 볼륨을 마운트한다.  
```bash
$ k get sc
NAME              PROVISIONER             RECLAIMPOLICY   VOLUMEBINDINGMODE      ALLOWVOLUMEEXPANSION   AGE
efs-sc            efs.csi.aws.com         Delete          Immediate              false                  7d13h

$ k get pvc
NAME                   STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS      AGE
efs-claim-1            Bound    pvc-bb393459-1f2b-494a-b408-a0f5bf252ea0   5Gi        RWX            efs-sc            10m

$ k get po -o wide
NAME         READY   STATUS    RESTARTS   AGE    IP            NODE                                              NOMINATED NODE   READINESS GATES
efs-app-1    1/1     Running   0          8s     10.20.11.47   ip-10-20-11-227.ap-northeast-2.compute.internal   <none>           <none>
```

<br>


## 5. TroubleShooting
EKS에서 EFS를 PVC로 사용 할 때 발생할 수 있는 여러가지 문제들...

#### 1. Pod가 ContainerCreating 또는 Terminating 단계에서 진행되지 않고 멈춤  
- 원인  
  - /proc/mounts에 대한 파일 시스템의 읽기 정합성이 깨져 발생. EFS CSI driver는 mount point가 제거된 것으로 생각하고 mount point와 일치하는 stunnel process를 정리해버림. 
  - stunnel process가 닫혔기 때문에 응답이 오지 않음

- 증상
  ```
  $ dmesg -T
  nfs: server 127.0.0.1 not responding, timed out
  nfs: server 127.0.0.1 not responding, timed out
  ```

- 해결
  - EFS CSI node 재기동
  - EC2 재기동

- Github  
[Github Issue](https://github.com/kubernetes-sigs/aws-efs-csi-driver/issues/616)  


#### 2. Pod가 ContainerCreating 또는 Terminating 단계에서 진행되지 않고 멈춤  
- 원인  
EFS CSI driver v1.3.6 버전에서 발견할 수 있는 현상으로 현재 원인을 확인하지 못함 

- 증상
  ```bash
  $ dmesg -T
  nfs: server 127.0.0.1 not responding, still trying
  nfs: server 127.0.0.1 not responding, still trying
  ```

- 해결
  - EFS CSI driver v1.3.8로 업그레이드
  - EFS CSI node 재기동 or EC2 재기동

- Github  
[Github Issue](https://github.com/kubernetes-sigs/aws-efs-csi-driver/issues/616)  


## 6. For more subjects
1. How to setup cross-account EKS - EFS mount
  - https://aws.amazon.com/ko/blogs/storage/mount-amazon-efs-file-systems-cross-account-from-amazon-eks/



---

## 📚 References
[1] **Use elastic file system storage with Amazon EFS**
- https://docs.aws.amazon.com/eks/latest/userguide/efs-csi.html  

[2] **aws-efs-csi-driver**  
- https://github.com/kubernetes-sigs/aws-efs-csi-driver  


