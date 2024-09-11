---
layout: post
title: "kubernetes CSI Driver"
author: "Bys"
category: k8s
date: 2024-07-30 01:00:00
tags: kubernetes node
---

# [Kubernetes Volume](https://kubernetes.io/docs/concepts/storage/volumes/)

### [Container Storage Interface(CSI)](https://github.com/container-storage-interface/spec/blob/master/spec.md)
Storage Provider가 플러그인을 한 번만 개발하면 여러 Container Orchestration에서 동작할 수 있도록 하는 표준 Container Storage Interface에 대한 정의 

- 용어
  - CO - `Container Orchestration` system.
  - SP - `Storage Provider`, the vendor of a CSI plugin implementation.
  - Plugin - Aka “plugin implementation”, a gRPC endpoint that `implements the CSI Services`.
  - Volume - `A unit of storage` that will be made available inside of a CO-managed container, via the CSI.

- 목표
  - CSI를 구현하는 모든 CO에서 "그냥 작동하는(just works)" 하나의 CSI 호환 플러그인을 SP 작성자가 작성할 수 있도록 합니다.
  - Define API (RPCs) that enable
    - Dynamic `provisioning` and `deprovisioning` of a volume
    - `Attaching` or `detaching` a volume from a node
    - `Mounting/unmounting` a volume from a node
    - Consumption of both `block and mountable volumes`
    - `Local storage providers` (e.g., device mapper, lvm)
    - Creating and deleting a `snapshot` (source of the snapshot is a volume)
    - `Provisioning` a new volume `from a snapshot`

<br>

#### A CO interacts with an Plugin through RPCs. Each SP MUST provide:
- Controller Plugin: 어디에서나 실행될 수 있는 CSI RPC를 제공하는 gRPC 엔드포인트. 즉, 컨트롤러는 쿠버네티스 컨테이너에서 사용할 볼륨을 스토리지 서버에서 생성 및 삭제하는 걸 담당한다.  
  > A gRPC endpoint serving CSI RPCs that MAY be run anywhere.
        
- Node Plugin: SP 프로비저닝 볼륨이 publish 되는 노드에서 반드시 실행되어야 하는 CSI RPC를 제공하는 gRPC 엔드포인트. 즉, 노드서버는 파드가 배포될 노드에서 스토리지 볼륨에 마운트할 수 있게 환경을 만드는 걸 담당한다.  
  > A gRPC endpoint serving CSI RPCs that MUST be run on the Node whereupon an SP-provisioned volume will be published.

<br>

#### [How to work](https://kubernetes.io/blog/2019/01/15/container-storage-interface-ga/#how-to-write-a-csi-driver)

![k8s-workflow-csi-driver](/assets/it/k8s/k8s/k8s-workflow-csi-driver.png){: .lightbox-image  width="100%" height="auto" }

- [external-provisioner(csi-provisioner)](https://kubernetes-csi.github.io/docs/external-provisioner.html#csi-external-provisioner)
  1. CSI external-provisioner 컨테이너는 PersistentVolumeClaim 리소스를 Watch 한다. 
  2. PersistentVolume 리소스가 생성되면 CreateVolume API를 호출한다. 
  3. Volume이 정상적으로 생성되면 Kubernetes PersistentVolume 리소스를 생성한다.  
  4. PV의 기본 ReclaimPolicy는 Delete이며 PersistentVolumeClaim 리소스를 삭제하면 csi-provisioner는 PV를 삭제한다.  

- [external-attacher(csi-attacher)](https://kubernetes-csi.github.io/docs/external-attacher.html)
  1. CSI external-attacher 컨테이너는 VolumeAttachment 리소스를 Watch 한다.  
  2. VolumeAttachment 리소스가 생성/삭제 되면 [`ControllerPublish`/`ControllerUnpublish` API를 호출하여 노드에 대한 Volume Attach/Detach 를 수행한다.](https://github.com/kubernetes-csi/external-attacher/blob/master/README.md#csi-attacher)
     - 쿠버네티스에서 attach 용어는 3rd party 볼륨을 노드에 attachment 하는 것을 의미한다. 클라우드 환경에서 노드에서 코드를 실행하지 않고도 클라우드 API가 노드에 볼륨을 첨부할 수 있는 것은 일반적입니다. CSI 용어로 이것은 `ControllerPublish` 호출에 해당한다.  
     - Detach는 역순으로 동작하며 노드에서 볼륨을 Detach 하는 것을 의미한다.  CSI 용어로 `ControllerUnpublish` 호출에 해당한다.  
     - 노드에서 실행되는 코드가 수행하는 Attach/Detach 작업(예: iSCSI 또는 파이버 채널 볼륨 연결)이 아니다. 이러한 작업은 일반적으로 `NodeStage` 및 `NodeUnstage` CSI 호출 중에 수행되며 external-attacher 가 수행하지 않는다.  

- [Attach/Deatch Controller](https://github.com/kubernetes/kubernetes/issues/20262)
  1. 파드가 노드에 스케쥴링 되는 것을 Watch 한다.  
  2. 볼륨이 필요한 파드가 노드에 스케쥴링 되면 VolumeAttachment 리소스를 생성한다.  

- [node-driver-registrar(csi-driver-registrar)](https://kubernetes-csi.github.io/docs/node-driver-registrar.html#csi-node-driver-registrar)
  1. CSI node-driver-registrar 는 NodeGetInfo API를 통해 CSI endpoint 로 부터 driver 정보를 가져와 노드의 kubelet에 등록하는 사이드카 컨테이너이며, kubelet 플러그인 등록 메커니즘을 사용하여 노드에 등록한다.  
    > [Device Plugins](https://kubernetes.io/docs/concepts/extend-kubernetes/compute-storage-net/device-plugins/#device-plugin-registration)
    장치 공급업체는 쿠버네티스 자체에 대한 코드를 사용자 정의하는 대신 수동으로 또는 데몬셋으로 배포하는 디바이스 플러그인을 구현할 수 있습니다. 대상 장치에는 GPU, 고성능 NIC, FPGA, InfiniBand 어댑터 및 공급업체별 초기화 및 설정이 필요할 수 있는 기타 유사한 컴퓨팅 리소스가 포함됩니다.
    ```golang
    service Registration {
      rpc Register(RegisterRequest) returns (Empty) {}
    }
    ```
    장치 플러그인은 이 gRPC 서비스를 통해 kubelet에 자신을 등록할 수 있습니다. 등록에 성공하면, 장치 플러그인은 관리하는 장치 목록을 kubelet에 전송하고, 그러면 kubelet은 kubelet 노드 상태 업데이트의 일부로 해당 리소스를 API 서버에 알리는 작업을 담당합니다. 
  2. Kubelet은 CSI 드라이버에 대해 CSI `NodeGetInfo`, `NodeStageVolume`, `NodePublishVolume` 호출을 직접 실행한다.  
    - Kubelet directly issues CSI `NodeGetInfo`, `NodeStageVolume`, and `NodePublishVolume` calls against CSI drivers.
  3. `NodeStageVolume`은 디스크를 파티션 및 포맷하고 노드의 global directory에 마운트 하는 데 사용됩니다. `NodePublishVolume`은 global directory를 컨테이너 디렉토리에 마운트하기 위해 사용합니다.  
    - [`NodePublishVolume` vs `NodeStageVolume`](https://github.com/kubernetes-csi/docs/issues/24#issuecomment-408342071)
    - 결론적으로 `ControllerPublishVolume` 에 의해서 볼륨이 노드에 Attach 되고, `NodeStageVolume` 에 의해서 노드의 global directory에 의해 마운트되며(Mount a device to a path), `NodePublishVolume`에 의해 global directory가 container directory로 마운트 된다(Bind mount: mount a path to a different path (instead of mounting a device to a path)).  
    
      ```bash
        CreateVolume +------------+ DeleteVolume
      +------------->|  CREATED   +--------------+
      |              +---+----^---+              |
      |       Controller |    | Controller       v
      +++         Publish |    | Unpublish       +++
      |X|          Volume |    | Volume          | |
      +-+             +---v----+---+             +-+
                      | NODE_READY |
                      +---+----^---+
                    Node |    | Node
                    Stage |    | Unstage
                  Volume |    | Volume
                      +---v----+---+
                      |  VOL_READY |
                      +---+----^---+
                    Node |    | Node
                  Publish |    | Unpublish
                  Volume |    | Volume
                      +---v----+---+
                      | PUBLISHED  |
                      +------------+
      
      #######################################
      $ mount
      ......
      /dev/nvme1n1 on /var/lib/kubelet/plugins/kubernetes.io/csi/ebs.csi.aws.com/4db2ca9d899c4423fec9632a48d0b5e7eca83a3a8661f0603e466bf62e31f809/globalmount type ext4 (rw,relatime)
      /dev/nvme1n1 on /var/lib/kubelet/pods/46afeb2c-fcd8-4abc-837b-47e8cfbc64af/volumes/kubernetes.io~csi/pvc-666a79ae-5375-44cb-bc97-e6d9d6d2bdfe/mount type ext4 (rw,relatime)
      ```


- [snapshot-controller](https://kubernetes-csi.github.io/docs/snapshot-controller.html), [external-snapshotter(csi-snapshotter)](https://kubernetes-csi.github.io/docs/external-snapshotter.html), 
  1. Snapshot-controller 는 VolumeSnapshots 리소스를 Watch 한다.  
  2. Snapshot-controller 는 VolumeSnapshots 리소스가 생성되면 VolumeSnapshotContents 리소스를 생성한다.  
  3. csi-snapshotter는 VolumeSnapshotContents 리소스를 Watch 한다.  
  4. csi-snapshotter는 VolumeSnapshotContents 리소스가 생성되면 CreateSnapshot API를 호출한다.  
  5. 사용자는 PersistentVolumeClaim 리소스를 생성할 때 datasource로 Snapshot을 사용하여 PVC를 생성할 수 있다.  



<br>

#### EBS/EFS CSI Driver의 구성요소 
```bash
## EFS CSI Controller
- efs-plugin
- csi-provisioner
- liveness-probe

## EFS CSI Node
- efs-plugin
- csi-driver-registrar
- liveness-probe


## EBS CSI Controller
- ebs-plugin
- csi-provisioner
- csi-attacher
- csi-resizer
- liveness-probe
- csi-snapshotter (CSI Snapshot controller와 같이 사용, Addon 설치)

## EBS CSI Node
- ebs-plugin
- node-driver-registrar
- liveness-probe
```



#### [CSI Volume Plugins in Kubernetes Design Doc](https://github.com/kubernetes/design-proposals-archive/blob/main/storage/container-storage-interface.md)


#### [Leader & Lease]
Leader 선출에 의해 동작하는 애플리케이션은 lease 오브젝트를 가지고 있다.  

```bash
# Lease objects
$ kubectl get leases -A
NAMESPACE         NAME                                                      HOLDER                                                                                   AGE
......
kube-system       ebs-csi-aws-com                                           1725345475903-4152-ebs-csi-aws-com                                                       664d
kube-system       efs-csi-aws-com                                           1725345522671-8163-efs-csi-aws-com                                                       648d
......

# Describe leases
$ kubectl describe lease ebs-csi-aws-com -n kube-system
Name:         ebs-csi-aws-com
Namespace:    kube-system
Labels:       <none>
Annotations:  <none>
API Version:  coordination.k8s.io/v1
Kind:         Lease
Metadata:
  Creation Timestamp:  2022-11-09T02:12:09Z
  Resource Version:    380944711
  UID:                 e8e8f63a-8556-408b-a1f2-628bdd3e2ca6
Spec:
  Acquire Time:            2024-09-03T06:39:03.304069Z
  Holder Identity:         1725345475903-4152-ebs-csi-aws-com
  Lease Duration Seconds:  15
  Lease Transitions:       130
  Renew Time:              2024-09-04T01:41:37.828660Z
Events:                    <none>

# Describe Rolebinding  
$ kubectl describe rolebinding ebs-csi-leases-rolebinding -n kube-system
Name:         ebs-csi-leases-rolebinding
Labels:       app.kubernetes.io/component=csi-driver
              app.kubernetes.io/managed-by=EKS
              app.kubernetes.io/name=aws-ebs-csi-driver
              app.kubernetes.io/version=1.34.0
Annotations:  <none>
Role:
  Kind:  Role
  Name:  ebs-csi-leases-role
Subjects:
  Kind            Name                   Namespace
  ----            ----                   ---------
  ServiceAccount  ebs-csi-controller-sa  kube-system

# Describe Role
$ kubectl describe role ebs-csi-leases-role -n kube-system
Name:         ebs-csi-leases-role
Labels:       app.kubernetes.io/component=csi-driver
              app.kubernetes.io/managed-by=EKS
              app.kubernetes.io/name=aws-ebs-csi-driver
              app.kubernetes.io/version=1.34.0
Annotations:  <none>
PolicyRule:
  Resources                   Non-Resource URLs  Resource Names  Verbs
  ---------                   -----------------  --------------  -----
  leases.coordination.k8s.io  []                 []              [get watch list delete update create]
```



Lease 오브젝트를 업데이트하는 주체는 리더다. 
```json
{
  "kind": "Event",
  "apiVersion": "audit.k8s.io/v1",
  "level": "Metadata",
  "auditID": "6b69f448-d54b-4c91-aee7-8adb4bca75ce",
  "stage": "ResponseComplete",
  "requestURI": "/apis/coordination.k8s.io/v1/namespaces/kube-system/leases/ebs-csi-aws-com",
  "verb": "update",
  "user": {
    "username": "system:serviceaccount:kube-system:ebs-csi-controller-sa",
    "uid": "04b51e47-d1db-4c65-a501-7a6eb6a6772a",
    "groups": [
      "system:serviceaccounts",
      "system:serviceaccounts:kube-system",
      "system:authenticated"
    ],
    "extra": {
      "authentication.kubernetes.io/credential-id": [
        "JTI=c5d82535-2f29-40ec-8b93-47b5d997ef41"
      ],
      "authentication.kubernetes.io/node-name": [
        "ip-10-20-129-248.ap-northeast-2.compute.internal"
      ],
      "authentication.kubernetes.io/node-uid": [
        "79f64dda-e2a8-4fa4-9ccf-b89c1d586f03"
      ],
      "authentication.kubernetes.io/pod-name": [
        "ebs-csi-controller-67988b76d8-f46gv"
      ],
      "authentication.kubernetes.io/pod-uid": [
        "d703f4ee-8654-416c-ad8d-73b918c2980f"
      ]
    }
  },
  "sourceIPs": [
    "10.20.130.198"
  ],
  "userAgent": "csi-provisioner/v0.0.0 (linux/amd64) kubernetes/$Format",

}
```

Lease 오브젝트를 주기적으로 업데이트하여 리더의 health를 확인하고 만약 리더가 health 체크를 하지 못하는 경우 새로운 리더를 선출한다.  

Leader가 아닌 파드의 로그
```bash
$ kubectl logs -f ebs-csi-controller-67988b76d8-ptmzv -c ebs-plugin  -n kube-system

I0903 06:38:44.006320       1 leaderelection.go:250] attempting to acquire leader lease kube-system/ebs-csi-aws-com...
```
[참고](https://github.com/kubernetes-sigs/aws-ebs-csi-driver/blob/master/docs/design.md#restarts)



-------------------------

<br><br><br>

> References

- CSI Spec - https://github.com/container-storage-interface/spec/blob/master/spec.md#rpc-interface
- CSI external-provisioner - https://kubernetes-csi.github.io/docs/external-provisioner.html#csi-external-provisioner
- CSI external-attacher - https://kubernetes-csi.github.io/docs/external-attacher.html#csi-external-attacher
- Plugin Registration Service - https://github.com/kubernetes/kubernetes/blob/master/pkg/kubelet/pluginmanager/pluginwatcher/README.md
- device-plugin-registration - https://kubernetes.io/docs/concepts/extend-kubernetes/compute-storage-net/device-plugins/#device-plugin-registration
- Plugin Registration Service - https://github.com/kubernetes/kubernetes/blob/master/pkg/kubelet/pluginmanager/pluginwatcher/README.md
- How to write a Container Storage Interface (CSI) plugin - https://arslan.io/2018/06/21/how-to-write-a-container-storage-interface-csi-plugin/
- Leader - https://github.com/kubernetes-sigs/aws-ebs-csi-driver/blob/master/docs/design.md#restarts