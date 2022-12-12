---
layout: post
title: "EKS에 EFS CSI Driver 설치 및 사용하기"
author: "Bys"
category: container
date: 2023-12-09 01:00:00
tags: kubernetes eks efs csi
---

# EFS CSI driver
EFS CSI driver는 Amazon EKS 클러스터가 영구 볼륨을 위해 Amazon EFS 볼륨의 수명 주기를 관리할 수 있게 해주는 CSI 인터페이스를 제공한다. 

## 1. [Install](https://docs.aws.amazon.com/eks/latest/userguide/efs-csi.html)
1. Policy 생성
2. ServiceAccount 생성 및 IAM Role생성 / 매핑 
3. aws-efs-csi-driver 배포 


## 2. Dynamic Provisioning 동작방법

![storage001](/assets/it/container/eks/storage001.png){: width="50%" height="auto"}
  
1. [StorageClass](https://kubernetes.io/docs/concepts/storage/storage-classes/)를 정의한다. StorageClass는 provisioner, parameters, reclaimPolicy 필드를 포함하는데 이는 dynamic provisioning을 통해 PV를 생성 할 때 사용된다. 
   - provisioner는 PV를 provisioning할 때 어떤 볼륨 plugin을 사용할지 결정한다. 
   - Parameters는 사용되는 provisioner에 따라 허용되는 값이 달라진다. EBS CSI driver에서는 'efs.csi.aws.com' provisioner를 사용하며 provisioningMode, fileSystemId, directoryPerms등을 허용한다. 자세한 내용은 링크 참고 [Parameters](https://github.com/kubernetes-sigs/aws-efs-csi-driver#storage-class-parameters-for-dynamic-provisioning).
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

## 3. [Dynamic Provisioning Test](https://github.com/kubernetes-sigs/aws-efs-csi-driver/tree/5e1fcd3e915d62d3b091c6de780ff9e6816f3a7b/examples/kubernetes)

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

## 5. 볼륨의 Mount 살펴보기 




<br><br><br>

> Ref: https://docs.aws.amazon.com/eks/latest/userguide/efs-csi.html  
> Ref: https://github.com/kubernetes-sigs/aws-efs-csi-driver  