---
layout: post
title: "EBS CSI driver를 통한 EBS PersistentVolume 사용하기"
author: "Bys"
category: cloud
date: 2022-12-09 01:00:00
tags: kubernetes eks ebs csi controller
---

## EBS CSI driver
EBS CSI driver는 Amazon EKS 클러스터가 영구 볼륨을 위해 Amazon EBS 볼륨의 수명 주기를 관리할 수 있게 해주는 CSI 인터페이스를 제공한다. 
> The Amazon Elastic Block Store (Amazon EBS) Container Storage Interface (CSI) driver allows Amazon Elastic Kubernetes Service (Amazon EKS) clusters to manage the lifecycle of Amazon EBS volumes for persistent volumes.  
[Amazon EBS CSI driver](https://docs.aws.amazon.com/eks/latest/userguide/ebs-csi.html)

EKS v1.23 부터는 In-tree controller는 deprecated 되고 EBS CSI driver를 사용하여야 한다.  

### 1. [IAM Role과 ServiceAccount생성하기](https://docs.aws.amazon.com/eks/latest/userguide/csi-iam-role.html)
The Amazon EBS CSI plugin requires IAM permissions to make calls to AWS APIs on your behalf. 

```bash
eksctl create iamserviceaccount \
  --name ebs-csi-controller-sa \
  --namespace kube-system \
  --cluster bys-dev-eks-main \
  --attach-policy-arn arn:aws:iam::aws:policy/service-role/AmazonEBSCSIDriverPolicy \
  --approve \
  --role-name AmazonEKS_EBS_CSI_DriverRole
```

<br>


### 2. [Installation, self-managed](https://github.com/kubernetes-sigs/aws-ebs-csi-driver/blob/master/docs/install.md)
설치 가이드에 따라 EBS CSI Driver를 설치하고 나면 Controller가 Deployment로 배포가 되고, EBS CSI Driver daemonSet이 설치된다. 

```bash
# kubectl
kubectl apply -k "github.com/kubernetes-sigs/aws-ebs-csi-driver/deploy/kubernetes/overlays/stable/?ref=release-1.13"
kubectl delete -k "github.com/kubernetes-sigs/aws-ebs-csi-driver/deploy/kubernetes/overlays/stable/?ref=release-1.13"

# Helm
helm repo add aws-ebs-csi-driver https://kubernetes-sigs.github.io/aws-ebs-csi-driver
helm repo update
helm upgrade --install aws-ebs-csi-driver --namespace kube-system aws-ebs-csi-driver/aws-ebs-csi-driver -f values.yaml
```

<br>

### 3. Dynamic Provisioning 동작방법

![storage001](/assets/it/cloud/eks/storage001.png){: width="80%" height="auto"}

1. [StorageClass](https://kubernetes.io/docs/concepts/storage/storage-classes/)를 정의한다. StorageClass는 provisioner, parameters, reclaimPolicy 필드를 포함하는데 이는 dynamic provisioning을 통해 PV를 생성 할 때 사용된다. 
   - provisioner는 PV를 provisioning할 때 어떤 볼륨 plugin을 사용할지 결정한다. 
   - Parameters는 사용되는 provisioner에 따라 허용되는 값이 달라진다. EBS CSI driver에서는 'ebs.csi.aws.com' provisioner를 사용하며 [Parameters](https://github.com/kubernetes-sigs/aws-ebs-csi-driver/blob/master/docs/parameters.md)값은 type에 따라 gp2, gp3, io1 등의 종류를 결정할 수 있고, iops 옵션, encrypted 옵션 등을 설정할 수 있다.  
   - StorageClass에 의해 동적으로 생성 된 PV는 reclaimPolicy필드를 갖는데 'Delete' or 'Retain' 값을 갖는다. 

2. Developer는 생성되어있는 cluster에서 StorageClass들을 리스트하여 각각의 정의를 보고 어떤 볼륨을 생성할지 결정한다. 그리고 원하는 StorageClass의 볼륨을 생성하기 위한 요청을 진행하게 되는데 이 내용을 담은 것이 PVC다. 

3. PVC를 요청하게 되면 Provisioner는 요청을 받아들여 PV를 생성하게 된다. 이 요청을 처리하는 것은 실제로 ebs-csi-controller에서 진행한다. 따라서, 아래와 같이 ebs-csi-controller로그를 확인한 상태로 PVC를 생성하면 PV가 생성되는 로그를 확인할 수 있다.  
    ```bash
    kubectl logs -f -l app=ebs-csi-controller -n kube-system
    I1208 14:25:15.016041       1 cloud.go:670] Waiting for volume "vol-07435d5e6e23bbc3b" state: actual=attaching, desired=attached
    I1208 14:25:58.977160       1 cloud.go:670] Waiting for volume "vol-024cb9263523da2f8" state: actual=detaching, desired=detached
    I1208 14:26:00.058710       1 cloud.go:670] Waiting for volume "vol-024cb9263523da2f8" state: actual=detaching, desired=detached
    I1208 14:26:01.911935       1 cloud.go:670] Waiting for volume "vol-024cb9263523da2f8" state: actual=detaching, desired=detached
    I1208 14:27:57.399082       1 cloud.go:670] Waiting for volume "vol-07435d5e6e23bbc3b" state: actual=detaching, desired=detached
    I1208 14:27:58.463509       1 cloud.go:670] Waiting for volume "vol-07435d5e6e23bbc3b" state: actual=detaching, desired=detached
    I1208 14:28:00.302592       1 cloud.go:670] Waiting for volume "vol-07435d5e6e23bbc3b" state: actual=detaching, desired=detached
    ```

4. 볼륨 바인딩과 dynamic provisioning을 통한 PV가 생성되는 시점은 StorageClass의 volumeBindingMode와 값과 관련이 있다. 'Immediate' 모드에서는 PVC가 생성되는 시점에 즉시 volume binding과 dynamic provisioning을 통한 PV가 생성된다. 'WaitForFirstConsumer' 모드에서는 binding과 PV의 provisioning의 시점을 Pod가 PVC를 사용하기 전까지 지연시킨다.  
아래 테스트에서 manifest를 SC -> PVC -> Pod 순으로 배포중 PVC까지 생성하고 PVC의 상태를 살펴보면 pending상태임을 알 수 있다. 그리고 Pod를 생성하면 그 때서야 binding과 dynamic provisioning이 발생하는 것을 알 수 있다. 이 것은 StorageClass의 volumeBindingMode 모드가 WaitForFirstConsumer 값을 가지기 때문이다.  

<br>

### 4. [Dynamic Provisioning Test](https://docs.aws.amazon.com/eks/latest/userguide/ebs-sample-app.html)
```yaml
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: ebs-sc-gp3
provisioner: ebs.csi.aws.com
volumeBindingMode: WaitForFirstConsumer
reclaimPolicy: Delete # Default
parameters:
  type: gp3
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: ebs-gp3-pvc
spec:
  accessModes:
    - ReadWriteOnce
  storageClassName: ebs-sc-gp3
  resources:
    requests:
      storage: 10Gi
---
apiVersion: v1
kind: Pod
metadata:
  name: app
spec:
  containers:
  - name: app
    image: centos
    command: ["/bin/sh"]
    args: ["-c", "while true; do echo $(date -u) >> /data/out.txt; sleep 5; done"]
    volumeMounts:
    - name: persistent-storage
      mountPath: /data
  volumes:
  - name: persistent-storage
    persistentVolumeClaim:
      claimName: ebs-gp3-pvc
```

<br>

StorageClass, PVC까지만 배포를 하고 나면 PVC의 상태는 아직 pending 중이다. 이 후 Pod를 정상 배포 하면 PVC의 상태가 Bound상태로 변경 되었고, PV를 보면 StorageClass에서 정의한 볼륨이 생성 된 것을 알 수 있다.  

```bash
$ k get sc
NAME              PROVISIONER             RECLAIMPOLICY   VOLUMEBINDINGMODE      ALLOWVOLUMEEXPANSION   AGE
ebs-sc-gp3        ebs.csi.aws.com         Delete          WaitForFirstConsumer   false                  25m

$ k get pvc
NAME                   STATUS        VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS      AGE
ebs-gp3-pvc            Pending                                                                            ebs-sc-gp3        10m

$ k get po app -o wide
NAME   READY   STATUS    RESTARTS   AGE     IP            NODE                                              NOMINATED NODE   READINESS GATES
app    1/1     Running   0          4m48s   10.20.11.25   ip-10-20-11-227.ap-northeast-2.compute.internal   <none>           <none>

$ k get pvc
NAME                   STATUS        VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS      AGE
ebs-gp3-pvc            Bound         pvc-fbd333d4-f74e-4dcc-b60b-d5d79a73b0ed   10Gi       RWO            ebs-sc-gp3        11m

$ get pv
NAME                                       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM                                 STORAGECLASS      REASON   AGE
pvc-fbd333d4-f74e-4dcc-b60b-d5d79a73b0ed   10Gi       RWO            Delete           Bound    default/ebs-gp3-pvc                   ebs-sc-gp3                 5s
```

<br>

### 5. 볼륨의 Mount 살펴보기 

##### Pod가 생성된 노드(10.20.11.227)로 접속

EC2에서 lsblk 명령어로 리눅스 디바이스 정보를 확인해보면, nvme1n1 이름으로 ext4 파일시스템 타입이 /var/lib/kubelet/pod 하위 경로에 마운트 된 것을 확인 할 수 있다.  
```
$ lsblk -f
NAME          FSTYPE LABEL UUID                                 MOUNTPOINT
nvme0n1
├─nvme0n1p1   xfs    /     1569e1c8-ad2a-4099-b3be-8ccd6ac8e7e9 /
└─nvme0n1p128
nvme1n1       ext4         e853ae12-34a1-4399-84f5-fde227b683c2 /var/lib/kubelet/pods/48c42e3a-073d-46b9-9a18-73679e9eaf31/volumes/kubernetes.io~csi/pvc-fbd333d4-f74e-4dcc-b60b-d5d79a73b0ed/mount

$ lsblk
NAME          MAJ:MIN RM SIZE RO TYPE MOUNTPOINT
nvme0n1       259:0    0  80G  0 disk
├─nvme0n1p1   259:1    0  80G  0 part /
└─nvme0n1p128 259:2    0   1M  0 part
nvme1n1       259:3    0  10G  0 disk /var/lib/kubelet/pods/48c42e3a-073d-46b9-9a18-73679e9eaf31/volumes/kubernetes.io~csi/pvc-fbd333d4-f74e-4dcc-b60b-d5d79a73b0ed/mount
```

해당 경로에 이동해보면 실제 EBS볼륨이 EC2인스턴스에 우선 마운트 된 것을 확인 할 수 있다.  
```
$ cd /var/lib/kubelet/pods/48c42e3a-073d-46b9-9a18-73679e9eaf31/volumes/kubernetes.io~csi/pvc-fbd333d4-f74e-4dcc-b60b-d5d79a73b0ed/mount
$ ll
total 20
drwx------ 2 root root 16384 Dec  8 14:49 lost+found
-rw-r--r-- 1 root root  1932 Dec  8 14:55 out.txt
```

##### Pod로 접속 
Pod에서 lsblk 명령어로 확인해보면 nvme1n1이름으로 /data 경로에 마운트 된 것을 확인 할 수 있다.  
```
lsblk
NAME          MAJ:MIN RM SIZE RO TYPE MOUNTPOINT
nvme0n1       259:0    0  80G  0 disk
|-nvme0n1p1   259:1    0  80G  0 part /etc/resolv.conf
`-nvme0n1p128 259:2    0   1M  0 part
nvme1n1       259:3    0  10G  0 disk /data
```

해당 경로로 이동해서 데이터를 확인  
```
$ cd /data
$ ls -l
total 20
drwx------ 2 root root 16384 Dec  8 14:49 lost+found
-rw-r--r-- 1 root root  3864 Dec  8 15:01 out.txt
```

##### Pod삭제
1. Pod를 삭제하면 /var/lib/kubelet/pods 하위 경로인 48c42e3a-073d-46b9-9a18-73679e9eaf31 디렉토리가 삭제된다.  


<br><br><br>

> Ref: [https://docs.aws.amazon.com/eks/latest/userguide/ebs-csi.html](https://docs.aws.amazon.com/eks/latest/userguide/ebs-csi.html)  
> Ref: [https://docs.aws.amazon.com/eks/latest/userguide/csi-iam-role.html](https://docs.aws.amazon.com/eks/latest/userguide/csi-iam-role.html)  
> Ref: [https://github.com/kubernetes-sigs/aws-ebs-csi-driver/tree/master/docs#set-up-driver-permission](https://github.com/kubernetes-sigs/aws-ebs-csi-driver/tree/master/docs#set-up-driver-permission)  
> Ref: [https://github.com/kubernetes-sigs/aws-ebs-csi-driver/blob/master/docs/install.md](https://github.com/kubernetes-sigs/aws-ebs-csi-driver/blob/master/docs/install.md)  
> Ref: [https://hyperconnect.github.io/2021/07/05/ebs-csi-gp3-support.html](https://hyperconnect.github.io/2021/07/05/ebs-csi-gp3-support.html)  