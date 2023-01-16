---
layout: post
title: "EKS AppMesh 적용하기"
author: "Bys"
category: cloud
date: 2022-12-15 01:00:00
tags: kubernetes eks appmesh
---

# [AppMesh](https://docs.aws.amazon.com/app-mesh/latest/userguide/what-is-app-mesh.html)


## 1. Install App Mesh Controller
[Install Dcocument AWS](https://docs.aws.amazon.com/app-mesh/latest/userguide/getting-started-kubernetes.html)  
[Install Document Github](https://github.com/aws/eks-charts/blob/master/stable/appmesh-controller/README.md#upgrade)  

1. Helm repo add 
'''
helm repo add eks https://aws.github.io/eks-charts
'''

2. Install the App Mesh CRDs
AppMesh 관련된 CRDs를 배포한다. 
```bash
kubectl apply -k "github.com/aws/eks-charts/stable/appmesh-controller//crds?ref=master"
```

3. AppMesh Controller를 위한 IRSA 셋팅

    ```bash
    kubectl create ns appmesh-system

    eksctl utils associate-iam-oidc-provider --region=ap-northeast-2 --cluster bys-dev-eks-main --approve

    eksctl create iamserviceaccount \
        --cluster bys-dev-eks-main \
        --namespace appmesh-system \
        --name appmesh-controller \
        --role-name "AWSAppMeshFullAccessRole" \
        --attach-policy-arn  arn:aws:iam::aws:policy/AWSCloudMapFullAccess,arn:aws:iam::aws:policy/AWSAppMeshFullAccess \
        --override-existing-serviceaccounts \
        --approve
    ```

4. AppMesh Controller 배포
XRay에서 Envoy에 대한 추적을 하려면 '--set tracing.enabled=true', '--set tracing.enabled=true' 두 옵션을 모두 셋팅해주어야 한다.  
>  EKS - In the App Mesh Controller configuration, include --set tracing.enabled=true and --set tracing.provider=x-ray.  

    ```bash
    helm upgrade -i appmesh-controller eks/appmesh-controller \
        --namespace appmesh-system \
        --set region=ap-northeast-2 \
        --set serviceAccount.create=false \
        --set serviceAccount.name=appmesh-controller \
        --set tracing.enabled=true \
        --set tracing.provider=x-ray \
        --set sidecar.logLevel=debug
    ```

<br>

## 2. Deploy App Mesh resources
기존 test namespace에 존재하는 application에 적용하는 과정.

### AppMesh 생성  
1. AppMesh생성 및 Envoy sidecar를 Injection 설정
[Envoy injection](https://aws.github.io/aws-app-mesh-controller-for-**k8s**/reference/injector/)
다음은 namespace를 통한 설정이다. 

    ```yaml
    apiVersion: v1
    kind: Namespace
    metadata:
      name: aws
      labels:
        mesh: bys-dev-appmesh-eks-main
        appmesh.k8s.aws/sidecarInjectorWebhook: enabled
    ---
    apiVersion: appmesh.k8s.aws/v1beta2
    kind: Mesh
    metadata:
      name: bys-dev-appmesh-eks-main
    spec:
      namespaceSelector:
        matchLabels:
          mesh: bys-dev-appmesh-eks-main
      egressFilter:
        type: DROP_ALL #ALLOW_ALL/DROP_ALL
    ```

2. ALLOW_ALL or DROP_ALL  
  우선 ALLOW_ALL을 선택하게 되면 virtualNode에서 ServiceMesh 내/외부 모든 endpoint에 대한 통신을 허용하게 한다. 반면 DROP_ALL을 선택하게 되면 virtualNode에서 ServiceMesh에 등록된 리소스에 한하여 통신이 가능하도록 한다. 
  즉, 이 설정을 하면 outgoing traffic이 envoy를 통하지 않고 'Application -> 외부'로 direct통신을 하게 되는 구조가 된다. Application내부에서 HttpCall을 통해 외부와 통신을 할 수 없다. outgoing traffic은 envoy를 통해 통신을 하게 되며 envoy입장에서는 AppMesh의 설정을 통해 알고있는 virtualService가 아닌 경우 404오류를 뱉게 된다. 따라서 DROP_ALL을 선택하게 되면 내부 통신을 위해 virtualNode에서 Backend설정이 필수이다.  
   > The first option is to set the egress filter on the mesh resource to ALLOW_ALL. This setting will allow any application service within the mesh to communicate with any destination IP address inside or outside of the mesh.
  The second option, DROP_ALL, allows egress only from virtual nodes to other defined resources in the service mesh. AWS App Mesh allows network traffic to flow from a virtual node to any service that is discoverable by a service discovery method. There are two supported options for service discovery, DNS or AWS Cloud Map.  
  
  만약  namespace에서 'appmesh.k8s.aws/sidecarInjectorWebhook: disabled' 처리가 되어있더라도 Pod의 spec에서 'appmesh.k8s.aws/sidecarInjectorWebhook: enabled' 값이 enabled 되어있다면 값을 override하여 sidecarInjection이 수행된다.  

    ```yaml
    apiVersion: v1
    kind: Namespace
    metadata:
      name: aws
      labels:
        mesh: bys-dev-appmesh-eks-main
        appmesh.k8s.aws/sidecarInjectorWebhook: disabled
    ---
    apiVersion: apps/v1
    kind: Deployment
    metadata:
      name: awssdk-dev-deploy
      namespace: aws
    spec:
      template:
        metadata:
          annotations:
            appmesh.k8s.aws/sidecarInjectorWebhook: enabled
    ```

  ![appmesh-xray001.png](/assets/it/container/eks/appmesh-xray001.png){: width="80%" height="auto"}  

  ![appmesh-xray002.png](/assets/it/container/eks/appmesh-xray002.png){: width="90%" height="auto"}  



### [Virtual Node](https://docs.aws.amazon.com/app-mesh/latest/userguide/virtual_nodes.html)

   - Virtual Node는 EKS에서 Service와 같은 검색 가능한 서비스에 대한 논리적 포인터 역할을 한다. 
       > A virtual node acts as a logical pointer to a discoverable service, such as an Amazon ECS or Kubernetes service.
   
   - 추후에 구성할 Virtual Service에서는 적어도 하나의 Virtual Node를 가져야 한다. 
   
   - Virtual Node는 Listener, Backend, Service Discovery로 구성 된다. 
       > When you create a virtual node, you must specify a service discovery method for your task group. Any inbound traffic that your virtual node expects is specified as a listener. Any virtual service that a virtual node sends outbound traffic to is specified as a backend.
   
   - Service Discovery 설정
   이번에는 DNS설정을 통해 진행하므로 실제 service의 명이 들어가야 한다.
       > DNS – Specify the DNS hostname of the actual service that the virtual node represents. The Envoy proxy is deployed in an Amazon VPC. The proxy sends name resolution requests to the DNS server that is configured for the VPC.
       > AWS Cloud Map – Specify an existing Service name and Namespace. 
   
   - Virtual Node의 inbound traffic은 listener 설정으로 구성한다.  

   - Virtual Node의 outbound traffic은 Backend로 구성한다. 여기서는 다른 Microservice가 없기 때문에 우선 backend는 설정하지 않는다.  
   
      ```yaml
      apiVersion: appmesh.k8s.aws/v1beta2
      kind: VirtualNode
      metadata:
        name: appmesh-vn-awssdk-iam-dev
        namespace: aws
      spec:
        podSelector:
          matchLabels:
            app.kubernetes.io/name: awssdk-iam-dev
        serviceDiscovery:
          dns:
            hostname: awssdk-iam-dev-svc.aws
        listeners:
          - portMapping:
              port: 10012
              protocol: http
        backends:
          - virtualService:
              virtualServiceRef:
                name: appmesh-vs-awssdk-storage-dev
        #    - virtualService:
        #        virtualServiceRef:
        #          name: awssdk-ec2-dev-svc.aws
        logging:
          accessLog:
            file:
              path: "/dev/stdout"
      ```


      `proxy-auth.json`
      IRSA 셋팅 필요
      ```json
      {
          "Version": "2012-10-17",
          "Statement": [
              {
                  "Effect": "Allow",
                  "Action": "appmesh:StreamAggregatedResources",
                  "Resource": [
                      "*"
                  ]
              }
          ]
      }
      ```

      ```bash
      aws iam create-policy --policy-name AWSAppMeshStreamAggregatedRscRole --policy-document file://proxy-auth.json
      ```
      AWSXrayWriteOnlyAccess

<br>

### [Virtual Router](https://docs.aws.amazon.com/app-mesh/latest/userguide/virtual_routers.html)

```yaml
apiVersion: appmesh.k8s.aws/v1beta2
kind: VirtualRouter
metadata:
  namespace: test
  name: awssdk-dev-appmesh-virtual-router
spec:
  listeners:
    - portMapping:
        port: 10010
        protocol: http
  routes:
    - name: awssdk-dev-appmesh-route
      httpRoute:
        match:
          prefix: /aws
        action:
          weightedTargets:
            - virtualNodeRef:
                name: awssdk-dev-appmesh-virtual-node
              weight: 1
```

### [Virtual Service](https://docs.aws.amazon.com/app-mesh/latest/userguide/virtual_services.html)

```yaml
apiVersion: appmesh.k8s.aws/v1beta2
kind: VirtualService
metadata:
  name: awssdk-dev-appmesh-virtual-service
  namespace: test
spec:
  awsName: awssdk-dev-svc.test.svc.cluster.local
  provider:
    virtualRouter:
      virtualRouterRef:
        name: awssdk-dev-appmesh-virtual-router
```


<br><br><br>

> Ref: https://docs.aws.amazon.com/app-mesh/latest/userguide/getting-started-kubernetes.html  
> Ref: https://docs.aws.amazon.com/app-mesh/latest/userguide/concepts.html  
> Ref: https://www.youtube.com/watch?v=I6aIp0AmIC0  
> Ref: https://www.youtube.com/watch?v=lOyRgNZrWA4