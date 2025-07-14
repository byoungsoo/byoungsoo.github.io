---
layout: post
title: "MLOps - Kubeflow Lab"
author: "Bys"
category: ml
date: 2025-07-13 01:00:00
tags: mlops kubeflow
---


# [Kubeflow](https://www.kubeflow.org/docs/started/architecture/)  

## Install
Kubeflow + Keycloak, Spark, MLflow 아키텍처. Spark와 MLflow 는 이미 설치가 되어 있으므로 Keycloak과 Kubeflow 설치만 진행한다.  

### [Install Keycloak(Official)](https://github.com/keycloak/keycloak-quickstarts/blob/main/kubernetes/keycloak.yaml)

```bash
wget -O keycloak.yaml https://raw.githubusercontent.com/keycloak/keycloak-quickstarts/refs/heads/main/kubernetes/keycloak.yaml

kubectl create ns keycloak
kubectl apply -f keycloak.yaml -n keycloak
```

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: keycloak
  namespace: keycloak
  annotations:
      alb.ingress.kubernetes.io/scheme: internet-facing
      alb.ingress.kubernetes.io/target-type: ip
      alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}, {"HTTPS":443}]'
      alb.ingress.kubernetes.io/ssl-redirect: '443'
      alb.ingress.kubernetes.io/healthcheck-port: traffic-port
      alb.ingress.kubernetes.io/healthcheck-path: /
      alb.ingress.kubernetes.io/success-codes: '200'
spec:
  ingressClassName: "mlops-ingress-class"
  rules:
    - host: keycloak.bys.asia
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: keycloak
                port:
                  number: 8080
```

```
User: admin
Password: admin
```

## [Setup Keycloak](https://www.keycloak.org/docs/latest/server_admin/index.html#_oidc_clients)

1. Create realm
- Manage realms > Create realm > Realm name: mlops
2. [Create client](https://www.keycloak.org/docs/latest/server_admin/index.html#_oidc_clients)  
![create-client-01.png](/assets/it/ml/kubeflow-lab/create-client-01.png){: width="90%" height="auto"}  
![create-client-02.png](/assets/it/ml/kubeflow-lab/create-client-02.png){: width="90%" height="auto"}  
![create-client-03.png](/assets/it/ml/kubeflow-lab/create-client-03.png){: width="90%" height="auto"}  
- Valid redirect URIs: https://kubeflow.bys.asia/dex/callback
- Web origins - https://kubeflow.bys.asia
![client-secret.png](/assets/it/ml/kubeflow-lab/client-secret.png){: width="90%" height="auto"}  
3. Create User and set password
`Must verified E-mail`. If you don't, you got 500 Internal Server Error after login successfully.


---


### [Install Kubeflow(Official)](https://www.kubeflow.org/docs/started/installing-kubeflow/) 
Kubeflow 를 지원하는 여러 배포판이 존재하지만 그 중 deployKF 를 사용한다.  

 
`Install ArgoCD`  
```
git clone -b main https://github.com/deployKF/deployKF.git ./deploykf
chmod +x ./deploykf/argocd-plugin/install_argocd.sh
bash ./deploykf/argocd-plugin/install_argocd.sh
```

`argocd Ingress`  
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: argocd
  namespace: argocd
  annotations:
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}, {"HTTPS":443}]'
    alb.ingress.kubernetes.io/ssl-redirect: '443'
    alb.ingress.kubernetes.io/backend-protocol: HTTPS
    alb.ingress.kubernetes.io/healthcheck-port: traffic-port
    alb.ingress.kubernetes.io/healthcheck-path: /
    alb.ingress.kubernetes.io/success-codes: '200'
spec:
  ingressClassName: "mlops-ingress-class"
  rules:
    - host: argocd.bys.asia
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: argocd-server
                port:
                  number: 443
```


```bash
## Password
USER=admin
PASSWORD=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d)
echo ${USER} ${PASSWORD}
```


`Create Key for argo<->gtihub`  
```bash
ssh-keygen -t ed25519 
# mlops_demo

# Pub key
github
```

`Connect Repositories`  
Argocd ssh connect repositories with private key


`app-of-apps.yaml`
```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: deploykf-app-of-apps
  namespace: argocd
  labels:
    app.kubernetes.io/name: deploykf-app-of-apps
    app.kubernetes.io/part-of: deploykf
spec:

  ## NOTE: if not "default", you MUST ALSO set the `argocd.project` value
  project: "default"

  source:
    ## source git repo configuration
    ##  - we use the 'deploykf/deploykf' repo so we can read its 'sample-values.yaml'
    ##    file, but you may use any repo (even one with no files)
    ##
    repoURL: "https://gitlab.bys.asia/bys/argocd-values.git"
    targetRevision: main
    path: "."

    ## plugin configuration
    ##
    plugin:
      name: "deploykf"
      parameters:

        ## the deployKF generator version
        ##  - available versions: https://github.com/deployKF/deployKF/releases
        ##
        - name: "source_version"
          string: "0.1.5"

        ## paths to values files within the `repoURL` repository
        ##  - the values in these files are merged, with later files taking precedence
        ##  - we strongly recommend using 'sample-values.yaml' as the base of your values
        ##    so you can easily upgrade to newer versions of deployKF
        ##
        - name: "values_files"
          array:
            - "./dev-ap2-eks-demo/deploykf/values.yaml"

  destination:
    server: "https://kubernetes.default.svc"
    namespace: "argocd"
  syncPolicy:
    automated:
      prune: false
      selfHeal: false
    syncOptions:
      - CreateNamespace=false
```




`Create ACM`
```bash
# 도메인에 대한 ACM 신규 생성, 호스트 존은 동일하게 유지 =
*.bys.asia
*.kubeflow.bys.asia
```


ACM 수정
`If kubeflow apply in app-of-apps values file`
```yaml
apiVersion: networking.k8s.io/v1
kind: IngressClass
metadata:
  name: default-ingress-class
spec:
  controller: eks.amazonaws.com/alb
  parameters:
    apiGroup: eks.amazonaws.com
    kind: IngressClassParams
    # Use the name of the IngressClassParams set in the previous step
    name: default-ingress-class-params
---
apiVersion: eks.amazonaws.com/v1
kind: IngressClassParams
metadata:
  name: default-ingress-class-params
spec:
  certificateARNs:
  - "arn:aws:acm:ap-northeast-2:558846430793:certificate/062abadf-0e7d-4ae7-b4c7-f58976d199d6"
```

`values.yaml`  
```yaml
deploykf_core:

  deploykf_auth: ## 수정
  deploykf_istio_gateway: ## 수정 
  deploykf_profiles_generator: ## 수정 
```

`Deploy Kubeflow`  
```bash
kubectl apply -f app-of-apps.yaml
```


`Sync Application`  
```bash
# download the latest version of the script
curl -fL -o "sync_argocd_apps.sh" \
  "https://raw.githubusercontent.com/deployKF/deployKF/main/scripts/sync_argocd_apps.sh"

# ensure the script is executable
chmod +x ./sync_argocd_apps.sh

# ensure your kubectl context is set correctly
kubectl config current-context

# run the script
bash ./sync_argocd_apps.sh --grpc-web
```




kubeflow 소개