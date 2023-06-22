---
layout: post
title: "AWS Load Balancer Controller를 통한 ELB 사용하기"
author: "Bys"
category: cloud
date: 2022-11-14 01:00:00
tags: aws eks ingress alb
---

# AWS Load Balancer Controller
AWS Load Balancer Controller는 AWS ELB를 사용하기 위해 AWS에서 개발한 [Out-of-tree controller](https://kubernetes.io/blog/2019/04/17/the-future-of-cloud-providers-in-kubernetes/#:~:text=In%2DTree%20%26%20Out%2Dof%2DTree%20Providers)이다. AWS ELB를 사용하기 위해서는 AWS load balancer controller를 먼저 설치해야 한다. AWS load balancer controller를 통해 어떻게 ALB Ingress를 생성하는지 살펴본다.  

(참고) 
- In-tree controller
  - Kubernetes 소스에서 개발되고 릴리즈되는 controller로 Kubernetes native
- Out-of-Tree controller 
  - Kubernetes가 아닌 외부에서 제공하는 controller로 Kubernetes core와 독립적  

## 1. [Install - AWS Load Balancer Controller](https://docs.aws.amazon.com/eks/latest/userguide/aws-load-balancer-controller.html)
1. Policy생성

2. Create iamserviceaccount
아래 내용을 배포하면 AWS IAM Role이 하나 생성 되면서 aws-load-balancer-controller ServiceAccount에 해당 IAM Role을 맵핑시켜준다.  
    ```bash
    export ACCOUNT_ID=`aws sts get-caller-identity | jq -r .Account`
    eksctl create iamserviceaccount \
    --cluster=ClusterName \
    --namespace=kube-system \
    --name=aws-load-balancer-controller \
    --attach-policy-arn=arn:aws:iam::$ACCOUNT_ID:policy/AWSLoadBalancerControllerIAMPolicy \
    --override-existing-serviceaccounts \
    --approve
    ```

    eksctl create iamserviceaccount \
      --cluster=my-cluster \
      --namespace=kube-system \
      --name=aws-load-balancer-controller \
      --role-name "AmazonEKSLoadBalancerControllerRole" \
      --attach-policy-arn=arn:aws:iam::111122223333:policy/AWSLoadBalancerControllerIAMPolicy \
      --approve
    ```

3. Install the TargetGroupBinding custom resource definitions
4. Helm을 통한 배포 
    ```bash
    # Private Image in private ENV
    helm upgrade -i aws-load-balancer-controller eks/aws-load-balancer-controller \
    --set clusterName=ClusterName \
    --set serviceAccount.create=false \
    --set serviceAccount.name=aws-load-balancer-controller \
    --set image.repository=222383050459.dkr.ecr.ap-northeast-2.amazonaws.com/opensource-components \
    --set image.tag=aws-load-balancer-controller-v2.4.4 \
    --set enableWaf=false \
    --set enableWafv2=false \
    --set enableShield=false \
    -n kube-system

    # Public Image in private ENV
    helm upgrade -i aws-load-balancer-controller eks/aws-load-balancer-controller \
    --set clusterName=ClusterName \
    --set serviceAccount.create=false \
    --set serviceAccount.name=aws-load-balancer-controller \
    --set image.repository=602401143452.dkr.ecr.ap-northeast-2.amazonaws.com/amazon/aws-load-balancer-controller \
    --set enableWaf=false \
    --set enableWafv2=false \
    --set enableShield=false \
    -n kube-system

    # Public Image
    helm upgrade -i aws-load-balancer-controller eks/aws-load-balancer-controller \
    --set clusterName=ClusterName \
    --set serviceAccount.create=false \
    --set serviceAccount.name=aws-load-balancer-controller \
    --set image.repository=602401143452.dkr.ecr.ap-northeast-2.amazonaws.com/amazon/aws-load-balancer-controller \
    -n kube-system
    ```


## 2. [동작 방법](https://github.com/kubernetes-sigs/aws-load-balancer-controller/blob/main/docs/how-it-works.md)

![awslbcontroller001](/assets/it/cloud/eks/awslbcontroller001.png){: width="60%" height="auto"}

1. The controller watches for ingress events from the API server. When it finds ingress resources that satisfy its requirements, it begins the creation of AWS resources.
2. An ALB (ELBv2) is created in AWS for the new ingress resource. This ALB can be internet-facing or internal. You can also specify the subnets it's created in using annotations.
3. Target Groups are created in AWS for each unique Kubernetes service described in the ingress resource.
4. Listeners are created for every port detailed in your ingress resource annotations. When no port is specified, sensible defaults (80 or 443) are used. Certificates may also be attached via annotations.
5. Rules are created for each path specified in your ingress resource. This ensures traffic to a specific path is routed to the correct Kubernetes Service.
6. Along with the above, the controller also...
    - deletes AWS components when ingress resources are removed from k8s.
    - modifies AWS components when ingress resources change in k8s.
    - assembles a list of existing ingress-related AWS components on start-up, allowing you to recover if the controller were to be restarted.


<br>

## 3. ALB Ingress Test
아래는 nginx의 간단한 예시를 통해 Deployment, Service, Ingress를 배포해본다. 


```yaml
apiVersion: apps/v1 # for versions before 1.9.0 use apps/v1beta2
kind: Deployment
metadata:
  name: nginx-deploy
  namespace: test
spec:
  selector:
    matchLabels:
      app: nginx-test
  replicas: 2 # tells deployment to run 1 pods matching the template
  template: # create pods using pod definition in this template
    metadata:
      labels:
        app: nginx-test
    spec:
      containers:
      - name: nginx-test
        image: nginx
        ports:
        - containerPort: 80
---
apiVersion: v1
kind: Service
metadata:
  name: nginx-svc
  namespace: test
  labels:
    app: nginx-test
spec:
  selector:
    app: nginx-test
  ports:
  - name: http
    port: 80
    protocol: TCP
    targetPort: 80
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: nginx-ingress
  namespace: test
  labels:
    app: nginx-test
  annotations:
    alb.ingress.kubernetes.io/group.name: nginx-group
    alb.ingress.kubernetes.io/subnets: bys-dev-sbn-az1-extelb, bys-dev-sbn-az2-extelb
    alb.ingress.kubernetes.io/scheme : internet-facing
    alb.ingress.kubernetes.io/security-groups: bys-dev-sg-alb-nginx
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 81}]'
    alb.ingress.kubernetes.io/healthcheck-path: /
    alb.ingress.kubernetes.io/healthcheck-interval-seconds: '15'
    alb.ingress.kubernetes.io/healthcheck-timeout-seconds: '10'
    alb.ingress.kubernetes.io/healthy-threshold-count: '2'
    alb.ingress.kubernetes.io/unhealthy-threshold-count: '4'
    alb.ingress.kubernetes.io/healthcheck-port: traffic-port
    alb.ingress.kubernetes.io/success-codes: 200,302
    alb.ingress.kubernetes.io/target-type: ip
spec:
  ingressClassName: alb
  rules:
  - http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: nginx-svc
            port:
              number: 80
```

위 와 같이 배포를 하고 나면 아래와 같이 pod, svc, ingress가 생성된 것을 확인 할 수 있으며 추가적으로 TargetGroupBinding이라는 리소스도 생성된다.  
```bash
$ k get pod,svc,targetgroupbinding,ing -n test

NAME                                READY   STATUS    RESTARTS   AGE
pod/nginx-deploy-7f8f66d685-lglmm   1/1     Running   0          89m
pod/nginx-deploy-7f8f66d685-llg4l   1/1     Running   0          89m

NAME                TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)   AGE
service/nginx-svc   ClusterIP   172.20.86.145   <none>        80/TCP    89m

NAME                           SERVICE-NAME   SERVICE-PORT   TARGET-TYPE   AGE
k8s-test-nginxsvc-c3ca7df12f   nginx-svc      80             ip            66m

NAME                                      CLASS   HOSTS   ADDRESS                                                            PORTS   AGE
ingress.networking.k8s.io/nginx-ingress   alb     *       k8s-v1dev-0ee906a5cb-1817684647.ap-northeast-2.elb.amazonaws.com   80      62m
```

<br>

## 4. [ALB Ingress Annotation](https://kubernetes-sigs.github.io/aws-load-balancer-controller/v2.4/guide/ingress/annotations/)
Annotation을 통해 다양한 ALB 설정이 가능하며 group.name을 통해 single alb설정도 가능하다. Multi-cluster에서의 single alb설정에 대한 문의가 있었지만, group.name을 통한 single alb의 scope에 대해 살펴보면 multi-cluster가 아닌 단일 cluster에서만 가능하다.  
```yaml
annotations:
    # group.name의 특징은 group.name이 같으면 single alb로 설정이 가능하다는 것이다. 
    # 다만 test 결과 동일한 Cluster 내에서만 single alb로 설정이 가능하다. 다른 클러스터에서 group.name을 동일하게 주고 생성해도 id값이 다른 alb가 별도로 생성되게 된다.  
    # ingress1번 배포를 하고 ingress2 배포 시 특정 schme같은 설정을 변경하면 ingress2번 배포시 오류가 발생한다. 
    alb.ingress.kubernetes.io/group.name: nginx-group
```

<br>

## 5. TargetGroupBinding 살펴보기
Ingress를 배포하고 나면 CustomResource인 TargetGroupBinding이 생성된다. TargetGroupBinding은 실제 AWS리소스인 Target Group의 ARN값과 Target으로 하는 Service Ref값을 통해 어떤 서비스를 Target Group에 등록할지 매핑해준다. 따라서 TargetGroupBinding을 통해 쿠버네티스의 서비스와 AWS의 ALB와 Target을 연결하고 있다고 생각하면 된다.  

```yaml
Name:         k8s-test-nginxsvc-c3ca7df12f
Namespace:    test
Labels:       ingress.k8s.aws/stack=v1-dev
Annotations:  <none>
API Version:  elbv2.k8s.aws/v1beta1
Kind:         TargetGroupBinding
Spec:
  Ip Address Type:  ipv4
  Networking:
    Ingress:
      From:
        Security Group:
          Group ID:  sg-07d020b78fab821d5
      Ports:
        Port:      80
        Protocol:  TCP
  Service Ref:
    Name:            nginx-svc
    Port:            80
  Target Group ARN:  arn:aws:elasticloadbalancing:ap-northeast-2:558846430793:targetgroup/k8s-test-nginxsvc-c3ca7df12f/36e4fe400a255758
  Target Type:       ip
Status:
  Observed Generation:  1
Events:                 <none>
```

<br>

## 6. IngressClass
Ingress를 살펴보면 Class가 alb로 생성된 것을 알 수 있다. Ingress의 spec을 살펴보면 'ingressClassName: alb'으로 설정된 것을 볼 수 있다. 이 Ingress를 정의할 때 ingressClass는 alb이름의 ingressClass를 사용하겠다는 의미이며 해당하는 alb IngressClass는 'controller: ingress.k8s.aws/alb'를 사용한다.
```yaml
apiVersion: networking.k8s.io/v1
kind: IngressClass
metadata:
  annotations:
    meta.helm.sh/release-name: aws-load-balancer-controller
    meta.helm.sh/release-namespace: kube-system
  creationTimestamp: "2022-10-28T06:12:53Z"
  generation: 1
  labels:
    app.kubernetes.io/instance: aws-load-balancer-controller
    app.kubernetes.io/managed-by: Helm
    app.kubernetes.io/name: aws-load-balancer-controller
    app.kubernetes.io/version: v2.4.4
    helm.sh/chart: aws-load-balancer-controller-1.4.5
  name: alb
  resourceVersion: "1356505"
  uid: 589bedac-3fa7-4511-bba4-afc41ea1761f
spec:
  controller: ingress.k8s.aws/alb
```

<br><br><br>

> Ref: https://docs.aws.amazon.com/eks/latest/userguide/aws-load-balancer-controller.html  
> Ref: https://kubernetes-sigs.github.io/aws-load-balancer-controller/v2.2/guide/ingress/annotations/  
> Ref: https://kubernetes.io/blog/2019/04/17/the-future-of-cloud-providers-in-kubernetes/#:~:text=In%2DTree%20%26%20Out%2Dof%2DTree%20Providers   
> Ref: https://docs.aws.amazon.com/ko_kr/eks/latest/userguide/aws-load-balancer-controller.html  
> Ref: https://kubernetes-sigs.github.io/aws-load-balancer-controller/v2.4/guide/ingress/annotations/  
> Ref: https://kubernetes-sigs.github.io/aws-load-balancer-controller/v2.2/guide/ingress/ingress_class/  