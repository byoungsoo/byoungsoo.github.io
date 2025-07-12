---
layout: post
title: "EKS Karpenter를 통한 Autoscaling"
author: "Bys"
category: cloud
date: 2023-03-02 01:00:00
tags: eks karpenter autoscaling
---

# [Karpenter](https://karpenter.sh/)  
Karpenter는 기존에 사용하던 Cluster Autoscaler와 같이 Kubernetes환경에서 노드의 Auto scaling 역할을 한다. 또한 공식문서에서는 Kubernetes 클러스터에서 Karpenter를 사용하는 것은 비용과 효율성을 극적으로 개선한다고 소개한다. 

**Watching** for pods that the Kubernetes scheduler has marked as unschedulable  
**Evaluating** scheduling constraints (resource requests, nodeselectors, affinities, tolerations, and topology spread constraints) requested by the pods  
**Provisioning** nodes that meet the requirements of the pods  
**Removing** the nodes when the nodes are no longer needed  

Karpenter는 Unschedulable Pod가 있을 경우 노드를 증가시킨다. Karpenter를 사용하기 전에는 이 부분에서 CA와 다른 점이 무엇일까?를 잘 이해하지 못하였다. 하지만 직접 사용을 해보고 그 차이점을 알 수 있었다. 이 부분은 Karpenter의 동작방식에서 다시 설명한다.  

<br>

## 1. Cluster Autoscaler 한계
EKS에서 Cluster Autoscaler의 경우 MNG(Managed Node Group)와 ASG(Auto Scaling Group)을 활용하게 된다. 동작 방식은 가용공간이 부족해 pending 상태의 pod가 생길 경우, Cluster Autoscaler가 감지하고 AWS ASG에 Desired Capacity 값을 변경한다. 이 후 EC2 노드가 provisioning 되고 pod가 스케쥴된다.  

Karpenter와 비교하였을 때 아래와 같은 한계점을 가진다.  
- Pending된 파드가 생기고 나서 부터 CA -> ASG -> EC2 -> Pod스케쥴 까지 걸리는 시간이 길다. 
- ASG에서 사용하는 LT(Launch Template)에 의해 Instance Type 등이 정해져 있어 정적이다. 
- CSP에 의존적이다. 예를 들어 AWS의 경우 Kubernetes의 노드 Scale In/Out을 위해 ASG를 이용한다. 

<br>

## 2. [Install Karpenter](https://github.com/aws/karpenter-provider-aws/tree/main/charts/karpenter)   


`env`
```bash
export CLUSTER_NAME="bys-shared-eks-main"
export AWS_REGION="ap-northeast-2"
export KARPENTER_NAMESPACE="karpenter"
export AWS_PARTITION="aws" # if you are not using standard partitions, you may need to configure to aws-cn / aws-us-gov
export OIDC_ENDPOINT="$(aws eks describe-cluster --name "${CLUSTER_NAME}" --query "cluster.identity.oidc.issuer" --output text)"
export AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query 'Account' --output text)
export K8S_VERSION=1.30
export ARM_AMI_ID="$(aws ssm get-parameter --name /aws/service/eks/optimized-ami/${K8S_VERSION}/amazon-linux-2-arm64/recommended/image_id --query Parameter.Value --output text)"
export AMD_AMI_ID="$(aws ssm get-parameter --name /aws/service/eks/optimized-ami/${K8S_VERSION}/amazon-linux-2/recommended/image_id --query Parameter.Value --output text)"
export GPU_AMI_ID="$(aws ssm get-parameter --name /aws/service/eks/optimized-ami/${K8S_VERSION}/amazon-linux-2-gpu/recommended/image_id --query Parameter.Value --output text)"
```


```yaml
affinity:
  nodeAffinity:
    preferredDuringSchedulingIgnoredDuringExecution:
      - weight: 50
        preference:
          matchExpressions:
            - key: eks.amazonaws.com/nodegroup
              operator: Exists

tolerations:
  - key: CriticalAddonsOnly
    operator: Exists
  - key: system
    operator: Exists
    effect: "NoSchedule"


# -- Global Settings to configure Karpenter
settings:
  # -- Cluster name.
  clusterName: "bys-shared-eks-main"

  # -- Interruption queue is the name of the SQS queue used for processing interruption events from EC2
  # Interruption handling is disabled if not specified. Enabling interruption handling may
  # require additional permissions on the controller service account. Additional permissions are outlined in the docs.
  interruptionQueue: "bys-shared-eks-main"
```

```bash
export KARPENTER_VERSION="1.2.1"
helm upgrade -i -n karpenter karpenter-crd oci://public.ecr.aws/karpenter/karpenter-crd
helm upgrade -i -n karpenter karpenter oci://public.ecr.aws/karpenter/karpenter -f /Users/bys/workspace/kubernetes/karpenter/bys-shared-eks-main/values.yaml
```

<br>

## 3. Karpenter 구성요소

#### [Node Template](https://karpenter.sh/docs/concepts/node-templates/)  
AWS의 Launch Template이라고 생각하면 된다. 실제로 node가 생성될 때 provisioner는 아래와 같이 AWSNodeTemplate을 이용하여 Launch Template을 생성하며 해당 LT로 노드를 생성하게 된다.  

```log
Mar 2, 2023 @ 11:02:17.349	2023-03-02T02:02:17.349342382Z stdout F 2023-03-02T02:02:17.349Z	INFO	controller.provisioner.cloudprovider	launched new instance	{"commit": "8c27519-dirty", "provisioner": "karpenter-default", "id": "i-0eb1db912a728a778", "hostname": "ip-10-20-11-158.ap-northeast-2.compute.internal", "instance-type": "c5a.large", "zone": "ap-northeast-2c", "capacity-type": "on-demand"}

Mar 2, 2023 @ 11:02:15.524	2023-03-02T02:02:15.523833797Z stdout F 2023-03-02T02:02:15.523Z	DEBUG	controller.provisioner.cloudprovider	created launch template	{"commit": "8c27519-dirty", "provisioner": "karpenter-default", "launch-template-name": "Karpenter-bys-dev-eks-main-6867569097314650987", "launch-template-id": "lt-08e6437c11ffda1e9"}

Mar 2, 2023 @ 11:02:15.367	2023-03-02T02:02:15.366983828Z stdout F 2023-03-02T02:02:15.366Z	DEBUG	controller.provisioner.cloudprovider	discovered new ami	{"commit": "8c27519-dirty", "provisioner": "karpenter-default", "ami": "ami-068b80883c7cb4771", "query": "/aws/service/eks/optimized-ami/1.24/amazon-linux-2/recommended/image_id"}

Mar 2, 2023 @ 11:02:15.323	2023-03-02T02:02:15.323702355Z stdout F 2023-03-02T02:02:15.323Z	DEBUG	controller.provisioner.cloudprovider	discovered kubernetes version	{"commit": "8c27519-dirty", "provisioner": "karpenter-default", "kubernetes-version": "1.24"}

Mar 2, 2023 @ 11:02:15.133	2023-03-02T02:02:15.133040873Z stdout F 2023-03-02T02:02:15.132Z	INFO	controller.provisioner	computed new node(s) to fit pod(s)	{"commit": "8c27519-dirty", "nodes": 1, "pods": 1}

Mar 2, 2023 @ 11:02:15.133	2023-03-02T02:02:15.133369996Z stdout F 2023-03-02T02:02:15.132Z	INFO	controller.provisioner	launching machine with 1 pods requesting {"cpu":"1475m","memory":"1686Mi","pods":"6"} from types m4.large, c5n.9xlarge, m5.24xlarge, r5n.24xlarge, m5d.4xlarge and 182 other(s)	{"commit": "8c27519-dirty", "provisioner": "karpenter-default"}
```

`default_nodetemplate.yaml`  
```yaml
apiVersion: karpenter.k8s.aws/v1alpha1
kind: AWSNodeTemplate
metadata:
  name: karpenter-default
spec:
  # required, discovers tagged subnets to attach to instances
  subnetSelector:
    karpenter.sh/discovery: "bys-dev-eks-main"

  # required, discovers tagged security groups to attach to instances
  securityGroupSelector:
    karpenter.sh/discovery: "bys-dev-eks-main"

  # optional, overrides the node's identity from global settings
  instanceProfile: KarpenterInstanceProfile

  # optional, resolves a default ami and userdata
  amiFamily: AL2

  # optional, configures storage devices for the instance
  blockDeviceMappings:
  - deviceName: /dev/xvda
    ebs:
      volumeSize: 30Gi
      volumeType: gp3
      iops: 3000
      encrypted: true
      kmsKeyID: "arn:aws:kms:ap-northeast-2:558846430793:key/6f29df3a-fb94-48e4-b359-a88fb74ce78d"
      deleteOnTermination: true
      throughput: 125

  # optional, configures detailed monitoring for the instance
  detailedMonitoring: true

  # optional, propagates tags to underlying EC2 resources
  tags:
    Name: karpenter-eks-main/karpenter-default
    auto-delete: "no"
```

#### [Provisioner](https://karpenter.sh/docs/concepts/provisioners/)  
Karpenter Controller의 provisioner는 실제로 AWS API를 호출하여 노드를 provisioning 하는 역할을 담당한다. 이 때 provisioner는 노드의 제약사항 및 해당 노드에서 실행할 수 있는 파드에 대한 제약 사항을 설정할 수 있으며 또한 추가적인 Kubelet args를 설정할 수 있다.  
Karpenter에서는 여러개의 provisioner를 사용할 수 있다. 
- Karpenter는 provisioner에 taint 설정이 존재하고 pod에 toleration설정이 없으면 해당 provisioner를 사용하지 않는다.  
- Pod가 여러 provisioner에 중복으로 매치되지 않도록 상호배타적으로 설정하는 것이좋다. 만약 여러개의 provisioner가 매치되면 weight이 높은 provisioner를 사용한다.  

`default_provisioner`  
```yaml
apiVersion: karpenter.sh/v1alpha5
kind: Provisioner
metadata:
  name: karpenter-default
spec:
  providerRef:
    name: karpenter-default

  #weight: 50
  #taints:
  #  - key: example.com/special-taint
  #    effect: NoSchedule

  requirements:
    - key: karpenter.k8s.aws/instance-category
      operator: In
      values: ["c", "m", "r", "t"]
    - key: karpenter.k8s.aws/instance-generation
      operator: Gt
      values: ["3"]
    - key: "karpenter.k8s.aws/instance-hypervisor"
      operator: In
      values: ["nitro"]
      requirements:
    - key: kubernetes.io/os
      operator: In
      values: ["linux"]
    - key: "kubernetes.io/arch"
      operator: In
      values: ["arm64", "amd64"]
    - key: "karpenter.sh/capacity-type" # If not included, the webhook for the AWS cloud provider will default to on-demand
      operator: In
      values: ["spot", "on-demand"]
    - key: "topology.kubernetes.io/zone"
      operator: In
      values: ["ap-northeast-2a", "ap-northeast-2c"]

  # Resource limits constrain the total size of the cluster.
  # Limits prevent Karpenter from creating new instances once the limit is exceeded.
  limits:
    resources:
      cpu: "1000"
      memory: 1000Gi

  # If omitted, the feature is disabled and nodes will never expire.  If set to less time than it requires for a node
  # to become ready, the node may expire before any pods successfully start.
  ttlSecondsUntilExpired: 2592000 # 30 Days = 60 * 60 * 24 * 30 Seconds;

  # If omitted, the feature is disabled, nodes will never scale down due to low utilization
  ttlSecondsAfterEmpty: 60

  # Karpenter provides the ability to specify a few additional Kubelet args.
  # These are all optional and provide support for additional customization and use cases.
  kubeletConfiguration:
    containerRuntime: containerd
    evictionMaxPodGracePeriod: 60
```


#### [Machine](https://github.com/aws/karpenter/blob/main/designs/node-ownership.md#kubernetes-crd-object-store-machine-crd)  
Karpenter는 provisioning loop의 완료단계에서 Machine을 생성한다. Provisioner는 Machine을 소유하고 Machine은 Kubernetes 노드 오브젝트와 Cloud Provider의 노드를 소유한다. Machine은 노드와 매핑되는데 노드의 `spec.providerID` 값과 Machine의 `status.providerID` 값과 매핑된다. 

> Karpenter will no longer create node objects or launch instances as part of the provisioning loop, but, instead, will create Machine CRs at the completion of the provisioning loop. This machine CR will then be picked up by a separate controller that will launch capacity based on the requirements passed from the provisioning loop and will resolve the static values from the **CreateFleet** response into its status. After the instance is launched, the kubelet starts, and the node joins the cluster, machines will be mapped to nodes using the `spec.providerID` of the Node and the `status.providerID` of the Machine.

![karpenter-node-ownership](/assets/it/cloud/eks/karpenter-node-ownership.png){: width="40%" height="auto"}  

<br>

## 4. [Provisioning 동작방법]()  
![karpenter001](/assets/it/cloud/eks/karpenter001.png){: width="80%" height="auto"}

1. CRD인 provisioner와 awsnodetemplate을 정의하며 Provisioner는 awsnodetemplate을 참조한다. 
2. Kubernetes karpenter는 pods가 실패하거나 다른 노드로 다시 스케쥴링이 될 때 워커 노드 수를 자동으로 조정한다. 
3. Karpenter에서는 ASG를 관리하지 않고 Karpenter -> EC2 Fleet API를 바로 호출하며 Just in time. Not ready 상태에서도 이미 pod가 스케쥴 됨.

- Karpenter Workflow
  1. launching machine with 1 pods requesting
  2. computed new node(s) to fit pod(s)
  3. discovered kubernetes version
  4. discovered new ami
  5. created launch template
  6. launched new instance


- Current Instance & Node Creation Flow  
Karpenter currently performs the following operations when it detect pending pods and launches capacity to the cluster:
1. VM 인스턴스 생성을 위해 EC2 API로 CreateFleet API를 수행한다. > Performs a CreateFleet request against EC2 APIs which leads to VM Instance Creation
2. Kubernetes API로 /core/v1/node Create를 수행한다. > Performs a Create against the /core/v1/node Kubernetes API
3. If there is a conflict in this Create call to the Node object, Karpenter assumes that Kubelet already created the node and does not add the Karpenter-managed node labels onto the node
4. Reconciles the Node termination finalizer onto the node as soon as the node watch receives an Add/Update event for the node
5. Reconciles the Provisioner ownerReference onto the node as soon as the node watch receives an Add/Update event for the node
    ```yaml
    $ k get nodes ip-10-20-11-140.ap-northeast-2.compute.internal --show-managed-fields -o yaml

    apiVersion: v1
    kind: Node
    metadata:
      annotations:
        alpha.kubernetes.io/provided-node-ip: 10.20.11.140
        csi.volume.kubernetes.io/nodeid: '{"ebs.csi.aws.com":"i-04226997c81ddee46","efs.csi.aws.com":"i-04226997c81ddee46"}'
        karpenter.sh/managed-by: bys-dev-eks-main
        karpenter/provisioner.name: karpenter-default
        node.alpha.kubernetes.io/ttl: "0"
        volumes.kubernetes.io/controller-managed-attach-detach: "true"
      creationTimestamp: "2023-08-03T08:43:09Z"
      finalizers:
      - karpenter.sh/termination
    .....생략
      ownerReferences:
      - apiVersion: karpenter.sh/v1alpha5
        blockOwnerDeletion: true
        kind: Machine
        name: default-prv-6dsjg
        uid: 37177659-2f4e-4b76-beac-71067d904471
    ```



`Cloud Trail Event`  
```json
{
  "userIdentity": {
        "type": "AssumedRole",
        "principalId": "AROAYEHOXZZE4O3LUTVQB:1676620212864829436",
        "arn": "arn:aws:sts::558846430793:assumed-role/KarpenterControllerRole-bys-dev-eks-main/1676620212864829436",
        "accountId": "558846430793",
        "sessionContext": {
            "sessionIssuer": {
                "type": "Role",
                "principalId": "AROAYEHOXZZE4O3LUTVQB",
                "arn": "arn:aws:iam::558846430793:role/KarpenterControllerRole-bys-dev-eks-main",
                "accountId": "558846430793",
                "userName": "KarpenterControllerRole-bys-dev-eks-main"
            },
            "webIdFederationData": {
                "federatedProvider": "arn:aws:iam::558846430793:oidc-provider/oidc.eks.ap-northeast-2.amazonaws.com/id/~",
                "attributes": {}
            },
            "attributes": {
                "creationDate": "2023-02-17T07:50:12Z",
                "mfaAuthenticated": "false"
            }
        },
        "invokedBy": "ec2fleet.amazonaws.com"
    },
    "eventName": "RunInstances",
    "sourceIPAddress": "ec2fleet.amazonaws.com",
    "userAgent": "ec2fleet.amazonaws.com",
        "requestParameters": {
        "instancesSet": {
            "items": [
                {
                    "minCount": 1,
                    "maxCount": 1
                }
            ]
        },
        "instanceType": "c5a.xlarge",
        "blockDeviceMapping": {},
        "availabilityZone": "ap-northeast-2a",
        "monitoring": {
            "enabled": false
        },
        "subnetId": "subnet-0ea5be4984975e8ed",
        "disableApiTermination": false,
        "disableApiStop": false
}
```

```bash
# k get po -A -o wide | grep 10.46
dashboard              dashboard-v1-dev-deploy-7b49f9645b-nm2zw       0/2     ContainerCreating   0          70s     <none>         ip-10-20-10-46.ap-northeast-2.compute.internal            <none>           <none>
default                php-apache1-8445df799-g5s8p                    0/1     ContainerCreating   0          70s     <none>         ip-10-20-10-46.ap-northeast-2.compute.internal            <none>           <none>
kube-system            aws-node-8cbfj                                 1/1     Running             0          66s     10.20.10.46    ip-10-20-10-46.ap-northeast-2.compute.internal            <none>           <none>
kube-system            ebs-csi-node-9lnl6                             0/3     ContainerCreating   0          66s     <none>         ip-10-20-10-46.ap-northeast-2.compute.internal            <none>           <none>
kube-system            efs-csi-node-92hmj                             3/3     Running             0          66s     10.20.10.46    ip-10-20-10-46.ap-northeast-2.compute.internal            <none>           <none>
kube-system            kube-proxy-tbkdp                               1/1     Running             0          67s     10.20.10.46    ip-10-20-10-46.ap-northeast-2.compute.internal            <none>           <none>


# kd node ip-10-20-10-46.ap-northeast-2.compute.internal
Events:
  Type     Reason                   Age                From             Message
  ----     ------                   ----               ----             -------
  Normal   Starting                 35s                kube-proxy
  Normal   RegisteredNode           64s                node-controller  Node ip-10-20-10-46.ap-northeast-2.compute.internal event: Registered Node ip-10-20-10-46.ap-northeast-2.compute.internal in Controller
  Normal   Starting                 47s                kubelet          Starting kubelet.
  Warning  InvalidDiskCapacity      47s                kubelet          invalid capacity 0 on image filesystem
  Normal   NodeHasSufficientMemory  47s (x3 over 47s)  kubelet          Node ip-10-20-10-46.ap-northeast-2.compute.internal status is now: NodeHasSufficientMemory
  Normal   NodeHasNoDiskPressure    47s (x3 over 47s)  kubelet          Node ip-10-20-10-46.ap-northeast-2.compute.internal status is now: NodeHasNoDiskPressure
  Normal   NodeHasSufficientPID     47s (x3 over 47s)  kubelet          Node ip-10-20-10-46.ap-northeast-2.compute.internal status is now: NodeHasSufficientPID
  Normal   NodeAllocatableEnforced  47s                kubelet          Updated Node Allocatable limit across pods
  Normal   NodeReady                19s                kubelet          Node ip-10-20-10-46.ap-northeast-2.compute.internal status is now: NodeReady
```



## 5. [Deprovisioning 동작방법](https://karpenter.sh/docs/concepts/deprovisioning/)  

- finalizers  
  Karpenter는 provision된 노드에 Kubernetes finalizers를 설정한다. 

  ```yaml
  $ kubectl get nodes  -o yaml --show-managed-fields

  apiVersion: v1
  kind: Node
  metadata:
    annotations:
      alpha.kubernetes.io/provided-node-ip: 10.20.11.140
      karpenter.sh/managed-by: bys-dev-eks-main
      karpenter/provisioner.name: karpenter-default
      node.alpha.kubernetes.io/ttl: "0"
      volumes.kubernetes.io/controller-managed-attach-detach: "true"
    finalizers:
    - karpenter.sh/termination
  ```
  Karpenter sets a Kubernetes finalizer on each node it provisions. The finalizer blocks deletion of the node object while the Termination Controller cordons and drains the node, before removing the underlying machine. 
  Deprovisioning is triggered by the Deprovisioning Controller, by the user through manual deprovisioning, or through an external system that sends a delete request to the node object.

- Deprovisioning  
  Karpenter automatically discovers deprovisionable nodes and spins up replacements when needed. Karpenter deprovisions nodes by executing one automatic method at a time, in order of Expiration, Drift, Emptiness, and then Consolidation. 

<!-- To do -->
- Deprovisioning Controller 
  1. Identify a list of prioritized candidates for the deprovisioning method.
  2. For each deprovisionable node, execute a scheduling simulation with the pods on the node to find if any replacement nodes are needed.
  3. Cordon the node(s) to prevent pods from scheduling to it.
  4. Pre-spin any replacement nodes needed as calculated in Step (2), and wait for them to become ready.
  5. Delete the node(s) and wait for the Termination Controller to gracefully shutdown the node(s).
  6. Once the Termination Controller terminates the node, go back to Step (1), starting at the the first deprovisioning method again.

- Termination Controller
  1. 노드에 Cordon을 통해 신규 파드의 스케줄링 방지
  2. K8s Eviction API를 통해 Eviction 시작 (PDB 존중), 노드가 drain 되기를 기다린다. 노드가 완전히 Drain되면 3번 프로세스 시작. 
      - 만약 가디라는동안 만약 Underlying machine(AWS에서는 EC2)가 존재하지 않으면, 노드의 finalizers 필드를 제거하여 API 서버가 노드를 삭제하여 완전히 삭제될 수 있도록 한다. 
  3. CSP의 machine을 종료한다. 
  4. 노드의 finalizers 필드를 제거하여 API 서버가 노드를 삭제하여 완전히 삭제될 수 있도록 한다. 

<!-- To do -->
- Automated Methods
  1. Emptiness
     - 마지막 파드가 노드로부터 stop된 후, `ttlSecondsAfterEmpty` 설정 이 후 Karpenter가 노드 삭제 요청을 함.
  2. Expiration
  3. Consolidation
  4. Drift
  5. Interruption


## 10. [Trouble Shooting]()  

#### 1. Karpenter 노드가 있는 상황에서 데몬셋(fluent-bit) 배포
결국에는 Karpenter노드는 기존의 Daemon-set은 respect 하지만 추가되는 Daemon-set의 경우 Update가 필요하다. 

MNG-ANG 노드 2개, Karpenter 노드 2개, Fargate 노드 2개의 상태에서 [fluent-bit](https://raw.githubusercontent.com/aws-samples/amazon-cloudwatch-container-insights/latest/k8s-deployment-manifest-templates/deployment-mode/daemonset/container-insights-monitoring/fluent-bit/fluent-bit.yaml) 배포를 진행했다. 
```bash
# kubectl get node --show-labels
NAME                                                      STATUS   ROLES    AGE    VERSION                LABELS
fargate-ip-10-20-10-162.ap-northeast-2.compute.internal   Ready    <none>   45d    v1.24.8-eks-a1bebd3    beta.kubernetes.io/arch=amd64,beta.kubernetes.io/os=linux,eks.amazonaws.com/compute-type=fargate,failure-domain.beta.kubernetes.io/region=ap-northeast-2,failure-domain.beta.kubernetes.io/zone=ap-northeast-2a,kubernetes.io/arch=amd64,kubernetes.io/hostname=ip-10-20-10-162.ap-northeast-2.compute.internal,kubernetes.io/os=linux,topology.kubernetes.io/region=ap-northeast-2,topology.kubernetes.io/zone=ap-northeast-2a
fargate-ip-10-20-11-87.ap-northeast-2.compute.internal    Ready    <none>   44d    v1.24.8-eks-a1bebd3    beta.kubernetes.io/arch=amd64,beta.kubernetes.io/os=linux,eks.amazonaws.com/compute-type=fargate,failure-domain.beta.kubernetes.io/region=ap-northeast-2,failure-domain.beta.kubernetes.io/zone=ap-northeast-2c,kubernetes.io/arch=amd64,kubernetes.io/hostname=ip-10-20-11-87.ap-northeast-2.compute.internal,kubernetes.io/os=linux,topology.kubernetes.io/region=ap-northeast-2,topology.kubernetes.io/zone=ap-northeast-2c
ip-10-20-10-230.ap-northeast-2.compute.internal           Ready    <none>   105m   v1.24.7-eks-fb459a0    alpha.eksctl.io/cluster-name=bys-dev-eks-main,alpha.eksctl.io/nodegroup-name=ng-v1,beta.kubernetes.io/arch=amd64,beta.kubernetes.io/instance-type=m5.large,beta.kubernetes.io/os=linux,eks.amazonaws.com/capacityType=ON_DEMAND,eks.amazonaws.com/nodegroup-image=ami-0cf0feb43fac2c960,eks.amazonaws.com/nodegroup=ng-v1,eks.amazonaws.com/sourceLaunchTemplateId=lt-0cbd44d881bbbe60f,eks.amazonaws.com/sourceLaunchTemplateVersion=1,failure-domain.beta.kubernetes.io/region=ap-northeast-2,failure-domain.beta.kubernetes.io/zone=ap-northeast-2a,k8s.io/cloud-provider-aws=32fda9a85f0c28b9f28eab77e4951bc2,kubernetes.io/arch=amd64,kubernetes.io/hostname=ip-10-20-10-230.ap-northeast-2.compute.internal,kubernetes.io/os=linux,node.kubernetes.io/instance-type=m5.large,topology.ebs.csi.aws.com/zone=ap-northeast-2a,topology.kubernetes.io/region=ap-northeast-2,topology.kubernetes.io/zone=ap-northeast-2a
ip-10-20-10-46.ap-northeast-2.compute.internal            Ready    <none>   105m   v1.24.10-eks-48e63af   beta.kubernetes.io/arch=amd64,beta.kubernetes.io/instance-type=c5a.large,beta.kubernetes.io/os=linux,failure-domain.beta.kubernetes.io/region=ap-northeast-2,failure-domain.beta.kubernetes.io/zone=ap-northeast-2a,k8s.io/cloud-provider-aws=32fda9a85f0c28b9f28eab77e4951bc2,karpenter.k8s.aws/instance-ami-id=ami-05de65e2c8743bf79,karpenter.k8s.aws/instance-category=c,karpenter.k8s.aws/instance-cpu=2,karpenter.k8s.aws/instance-encryption-in-transit-supported=true,karpenter.k8s.aws/instance-family=c5a,karpenter.k8s.aws/instance-generation=5,karpenter.k8s.aws/instance-hypervisor=nitro,karpenter.k8s.aws/instance-memory=4096,karpenter.k8s.aws/instance-pods=29,karpenter.k8s.aws/instance-size=large,karpenter.sh/capacity-type=on-demand,karpenter.sh/initialized=true,karpenter.sh/machine-name=,karpenter.sh/provisioner-name=karpenter-default,kubernetes.io/arch=amd64,kubernetes.io/hostname=ip-10-20-10-46.ap-northeast-2.compute.internal,kubernetes.io/os=linux,node.kubernetes.io/instance-type=c5a.large,topology.ebs.csi.aws.com/zone=ap-northeast-2a,topology.kubernetes.io/region=ap-northeast-2,topology.kubernetes.io/zone=ap-northeast-2a
ip-10-20-11-89.ap-northeast-2.compute.internal            Ready    <none>   107m   v1.24.7-eks-fb459a0    alpha.eksctl.io/cluster-name=bys-dev-eks-main,alpha.eksctl.io/nodegroup-name=ng-v1,beta.kubernetes.io/arch=amd64,beta.kubernetes.io/instance-type=m5.large,beta.kubernetes.io/os=linux,eks.amazonaws.com/capacityType=ON_DEMAND,eks.amazonaws.com/nodegroup-image=ami-0cf0feb43fac2c960,eks.amazonaws.com/nodegroup=ng-v1,eks.amazonaws.com/sourceLaunchTemplateId=lt-0cbd44d881bbbe60f,eks.amazonaws.com/sourceLaunchTemplateVersion=1,failure-domain.beta.kubernetes.io/region=ap-northeast-2,failure-domain.beta.kubernetes.io/zone=ap-northeast-2c,k8s.io/cloud-provider-aws=32fda9a85f0c28b9f28eab77e4951bc2,kubernetes.io/arch=amd64,kubernetes.io/hostname=ip-10-20-11-89.ap-northeast-2.compute.internal,kubernetes.io/os=linux,node.kubernetes.io/instance-type=m5.large,topology.ebs.csi.aws.com/zone=ap-northeast-2c,topology.kubernetes.io/region=ap-northeast-2,topology.kubernetes.io/zone=ap-northeast-2c
ip-10-20-11-9.ap-northeast-2.compute.internal             Ready    <none>   150m   v1.24.10-eks-48e63af   beta.kubernetes.io/arch=amd64,beta.kubernetes.io/instance-type=c5a.large,beta.kubernetes.io/os=linux,failure-domain.beta.kubernetes.io/region=ap-northeast-2,failure-domain.beta.kubernetes.io/zone=ap-northeast-2c,k8s.io/cloud-provider-aws=32fda9a85f0c28b9f28eab77e4951bc2,karpenter.k8s.aws/instance-ami-id=ami-05de65e2c8743bf79,karpenter.k8s.aws/instance-category=c,karpenter.k8s.aws/instance-cpu=2,karpenter.k8s.aws/instance-encryption-in-transit-supported=true,karpenter.k8s.aws/instance-family=c5a,karpenter.k8s.aws/instance-generation=5,karpenter.k8s.aws/instance-hypervisor=nitro,karpenter.k8s.aws/instance-memory=4096,karpenter.k8s.aws/instance-pods=29,karpenter.k8s.aws/instance-size=large,karpenter.sh/capacity-type=on-demand,karpenter.sh/initialized=true,karpenter.sh/machine-name=,karpenter.sh/provisioner-name=karpenter-default,kubernetes.io/arch=amd64,kubernetes.io/hostname=ip-10-20-11-9.ap-northeast-2.compute.internal,kubernetes.io/os=linux,node.kubernetes.io/instance-type=c5a.large,topology.ebs.csi.aws.com/zone=ap-northeast-2c,topology.kubernetes.io/region=ap-northeast-2,topology.kubernetes.io/zone=ap-northeast-2c
```

데몬셋을 배포했더니 4개의 파드가 정상 배포되지 않았다. 
```bash
# kubectl get node --show-labels
NAME                                                      STATUS   ROLES    AGE    VERSION                LABELS
fargate-ip-10-20-10-162.ap-northeast-2.compute.internal   Ready    <none>   45d    v1.24.8-eks-a1bebd3    beta.kubernetes.io/arch=amd64,beta.kubernetes.io/os=linux,eks.amazonaws.com/compute-type=fargate,failure-domain.beta.kubernetes.io/region=ap-northeast-2,failure-domain.beta.kubernetes.io/zone=ap-northeast-2a,kubernetes.io/arch=amd64,kubernetes.io/hostname=ip-10-20-10-162.ap-northeast-2.compute.internal,kubernetes.io/os=linux,topology.kubernetes.io/region=ap-northeast-2,topology.kubernetes.io/zone=ap-northeast-2a
fargate-ip-10-20-11-87.ap-northeast-2.compute.internal    Ready    <none>   44d    v1.24.8-eks-a1bebd3    beta.kubernetes.io/arch=amd64,beta.kubernetes.io/os=linux,eks.amazonaws.com/compute-type=fargate,failure-domain.beta.kubernetes.io/region=ap-northeast-2,failure-domain.beta.kubernetes.io/zone=ap-northeast-2c,kubernetes.io/arch=amd64,kubernetes.io/hostname=ip-10-20-11-87.ap-northeast-2.compute.internal,kubernetes.io/os=linux,topology.kubernetes.io/region=ap-northeast-2,topology.kubernetes.io/zone=ap-northeast-2c
ip-10-20-10-230.ap-northeast-2.compute.internal           Ready    <none>   105m   v1.24.7-eks-fb459a0    alpha.eksctl.io/cluster-name=bys-dev-eks-main,alpha.eksctl.io/nodegroup-name=ng-v1,beta.kubernetes.io/arch=amd64,beta.kubernetes.io/instance-type=m5.large,beta.kubernetes.io/os=linux,eks.amazonaws.com/capacityType=ON_DEMAND,eks.amazonaws.com/nodegroup-image=ami-0cf0feb43fac2c960,eks.amazonaws.com/nodegroup=ng-v1,eks.amazonaws.com/sourceLaunchTemplateId=lt-0cbd44d881bbbe60f,eks.amazonaws.com/sourceLaunchTemplateVersion=1,failure-domain.beta.kubernetes.io/region=ap-northeast-2,failure-domain.beta.kubernetes.io/zone=ap-northeast-2a,k8s.io/cloud-provider-aws=32fda9a85f0c28b9f28eab77e4951bc2,kubernetes.io/arch=amd64,kubernetes.io/hostname=ip-10-20-10-230.ap-northeast-2.compute.internal,kubernetes.io/os=linux,node.kubernetes.io/instance-type=m5.large,topology.ebs.csi.aws.com/zone=ap-northeast-2a,topology.kubernetes.io/region=ap-northeast-2,topology.kubernetes.io/zone=ap-northeast-2a
ip-10-20-10-46.ap-northeast-2.compute.internal            Ready    <none>   105m   v1.24.10-eks-48e63af   beta.kubernetes.io/arch=amd64,beta.kubernetes.io/instance-type=c5a.large,beta.kubernetes.io/os=linux,failure-domain.beta.kubernetes.io/region=ap-northeast-2,failure-domain.beta.kubernetes.io/zone=ap-northeast-2a,k8s.io/cloud-provider-aws=32fda9a85f0c28b9f28eab77e4951bc2,karpenter.k8s.aws/instance-ami-id=ami-05de65e2c8743bf79,karpenter.k8s.aws/instance-category=c,karpenter.k8s.aws/instance-cpu=2,karpenter.k8s.aws/instance-encryption-in-transit-supported=true,karpenter.k8s.aws/instance-family=c5a,karpenter.k8s.aws/instance-generation=5,karpenter.k8s.aws/instance-hypervisor=nitro,karpenter.k8s.aws/instance-memory=4096,karpenter.k8s.aws/instance-pods=29,karpenter.k8s.aws/instance-size=large,karpenter.sh/capacity-type=on-demand,karpenter.sh/initialized=true,karpenter.sh/machine-name=,karpenter.sh/provisioner-name=karpenter-default,kubernetes.io/arch=amd64,kubernetes.io/hostname=ip-10-20-10-46.ap-northeast-2.compute.internal,kubernetes.io/os=linux,node.kubernetes.io/instance-type=c5a.large,topology.ebs.csi.aws.com/zone=ap-northeast-2a,topology.kubernetes.io/region=ap-northeast-2,topology.kubernetes.io/zone=ap-northeast-2a
ip-10-20-11-89.ap-northeast-2.compute.internal            Ready    <none>   107m   v1.24.7-eks-fb459a0    alpha.eksctl.io/cluster-name=bys-dev-eks-main,alpha.eksctl.io/nodegroup-name=ng-v1,beta.kubernetes.io/arch=amd64,beta.kubernetes.io/instance-type=m5.large,beta.kubernetes.io/os=linux,eks.amazonaws.com/capacityType=ON_DEMAND,eks.amazonaws.com/nodegroup-image=ami-0cf0feb43fac2c960,eks.amazonaws.com/nodegroup=ng-v1,eks.amazonaws.com/sourceLaunchTemplateId=lt-0cbd44d881bbbe60f,eks.amazonaws.com/sourceLaunchTemplateVersion=1,failure-domain.beta.kubernetes.io/region=ap-northeast-2,failure-domain.beta.kubernetes.io/zone=ap-northeast-2c,k8s.io/cloud-provider-aws=32fda9a85f0c28b9f28eab77e4951bc2,kubernetes.io/arch=amd64,kubernetes.io/hostname=ip-10-20-11-89.ap-northeast-2.compute.internal,kubernetes.io/os=linux,node.kubernetes.io/instance-type=m5.large,topology.ebs.csi.aws.com/zone=ap-northeast-2c,topology.kubernetes.io/region=ap-northeast-2,topology.kubernetes.io/zone=ap-northeast-2c
ip-10-20-11-9.ap-northeast-2.compute.internal             Ready    <none>   150m   v1.24.10-eks-48e63af   beta.kubernetes.io/arch=amd64,beta.kubernetes.io/instance-type=c5a.large,beta.kubernetes.io/os=linux,failure-domain.beta.kubernetes.io/region=ap-northeast-2,failure-domain.beta.kubernetes.io/zone=ap-northeast-2c,k8s.io/cloud-provider-aws=32fda9a85f0c28b9f28eab77e4951bc2,karpenter.k8s.aws/instance-ami-id=ami-05de65e2c8743bf79,karpenter.k8s.aws/instance-category=c,karpenter.k8s.aws/instance-cpu=2,karpenter.k8s.aws/instance-encryption-in-transit-supported=true,karpenter.k8s.aws/instance-family=c5a,karpenter.k8s.aws/instance-generation=5,karpenter.k8s.aws/instance-hypervisor=nitro,karpenter.k8s.aws/instance-memory=4096,karpenter.k8s.aws/instance-pods=29,karpenter.k8s.aws/instance-size=large,karpenter.sh/capacity-type=on-demand,karpenter.sh/initialized=true,karpenter.sh/machine-name=,karpenter.sh/provisioner-name=karpenter-default,kubernetes.io/arch=amd64,kubernetes.io/hostname=ip-10-20-11-9.ap-northeast-2.compute.internal,kubernetes.io/os=linux,node.kubernetes.io/instance-type=c5a.large,topology.ebs.csi.aws.com/zone=ap-northeast-2c,topology.kubernetes.io/region=ap-northeast-2,topology.kubernetes.io/zone=ap-northeast-2c
```

Fargate 노드에는 데몬셋이 배포되지 않도록 아래의 nodeAffinity를 추가했다. 
```yaml
    spec:
      affinity:
        nodeAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
            nodeSelectorTerms:
            - matchExpressions:
              - key: eks.amazonaws.com/compute-type
                operator: NotIn
                values:
                - fargate
```

또한 Karpenter 노드의 경우에는 CPU가 부족해서 생성되지 않고 있는 상황으로 CPU Type자체를 변경해야 할 필요가 생겼다. 이번 경우에는 노드를 drain 시키고 다시 재 생성 하였다. 

<br>

#### 2. EC2 인스턴스가 기동되자마자 바로 종료되는 현상 
- Node terminates before ready on failed encrypted EBS volume

If you are using a custom launch template and an encrypted EBS volume, the IAM principal launching the node may not have sufficient permissions to use the KMS customer managed key (CMK) for the EC2 EBS root volume. This issue also applies to Block Device Mappings specified in the Provisioner. In either case, this results in the node terminating almost immediately upon creation.

To correct the problem if it occurs, you can use the approach that AWS EBS uses, which avoids adding particular roles to the KMS policy.

<br>

#### 3. Karpenter 컨트롤러가 Spot Interruption을 제대로 처리하지 못할때 확인할 것
- interruptionQueue 설정

<br>

#### 4. Karpenter 컨트롤러 Provision 실패
다음과 같은 메세지와 함께 프로비저닝이 실패했다. [소스코드](https://github.com/kubernetes-sigs/karpenter/blob/37d09a148b887a809a754a5d9703f8ef25ad492a/pkg/controllers/provisioning/scheduling/nodeclaim.go#L103C2-L103C47)를 잘 살펴보면 실패사유와 원인을 확인할 수 있지만 명확하지 않다.  

```
{"level":"ERROR","time":"2024-06-11T06:26:33.747Z","logger":"controller.provisioner","message":"Could not schedule pod, incompatible with nodepool \"test-nodepool\", daemonset overhead={\"cpu\":\"310m\",\"memory\":\"272Mi\",\"pods\":\"7\"}, no instance type satisfied resources {\"cpu\":\"3310m\",\"memory\":\"3344Mi\",\"pods\":\"8\",\"vpc.amazonaws.com/pod-eni\":\"1\"} and requirements karpenter.sh/capacity-type In [on-demand], karpenter.sh/nodepool In [test-nodepool], kubernetes.io/arch In [amd64], node.kubernetes.io/instance-type In [c7i.2xlarge c7i.4xlarge], nodepool In [test-nodepool], nodepool/test-nodepool Exists, topology.kubernetes.io/zone In [ap-northeast-2a ap-northeast-2c] (no instance type which had enough resources and the required offering met the scheduling requirements);,"commit":"c4ak371","pod":"test-nodepool/nginx-a1938a0d"}
```

이 오류가 발생한 사유는 SGP를 사용하였기 때문이다. SGP를 사용하면 requests에는 `"vpc.amazonaws.com/pod-eni": "1"` 리소스가 포함된다.  v0.32버전 에서는 Instance Type의 리소스를 확인해보면 다음과 같이 리소스가 있는 것을 알 수 있다.  
#### c7i.2xlarge  

 | Resource | Quantity |
 |--|--|
 |cpu|7910m|
 |ephemeral-storage|17Gi|
 |memory|14162Mi|
 |pods|58|

 그리고 v0.34 버전 부터는 다음과 같이 SGP 리소스가 추가되었다. 
 | Resource | Quantity |
 |--|--|
 |cpu|7910m|
 |ephemeral-storage|17Gi|
 |memory|14162Mi|
 |pods|58|
 |vpc.amazonaws.com/pod-eni|38|
 
따라서, 버전 업그레이드가 필요한 상황이다.  



---

## 📚 References

[1] **Github Issue**  
- https://github.com/aws/karpenter/issues/101

[2] **Troubleshooting**  
- https://karpenter.sh/docs/troubleshooting/
