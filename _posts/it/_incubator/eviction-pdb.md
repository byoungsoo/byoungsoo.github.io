

## 1. Pod eviction과 PDB
Eviction API 호출 시 Terminating이 되고 나서 신규 파드가 생성 됨
```bash
TOKEN=$(aws eks get-token --cluster-name bys-dev-eks-main | jq -r ".status.token")

curl -v -H 'Content-type: application/json' https://A8845D3F0E5C385227204D33B8635ABC.sk1.ap-northeast-2.eks.amazonaws.com/api/v1/namespaces/default/pods/nginx-7c5464d485-cvrg4/eviction -d @eviction.json --header "Authorization: Bearer $TOKEN" --insecure
{
  "apiVersion": "policy/v1",
  "kind": "Eviction",
  "metadata": {
    "name": "nginx-7c5464d485-cvrg4",
    "namespace": "default"
  }
}

curl -v -H 'Content-type: application/json' https://A8845D3F0E5C385227204D33B8635ABC.sk1.ap-northeast-2.eks.amazonaws.com/api/v1/namespaces/default/pods/nginx-7c5464d485-bwf98/eviction --header "Authorization: Bearer $TOKEN" --insecure \
--data '{
  "apiVersion": "policy/v1",
  "kind": "Eviction",
  "metadata": {
    "name": "nginx-7c5464d485-bwf98",
    "namespace": "default"
  }
}'

nginx-7c5464d485-v7tkh         1/1     Running   0          89s
nginx-7c5464d485-v7tkh         1/1     Terminating   0          89s
nginx-7c5464d485-v7tkh         1/1     Terminating   0          89s
nginx-7c5464d485-cvrg4         0/1     Pending       0          0s
nginx-7c5464d485-cvrg4         0/1     Pending       0          0s
nginx-7c5464d485-cvrg4         0/1     ContainerCreating   0          0s
nginx-7c5464d485-v7tkh         0/1     Terminating         0          90s
nginx-7c5464d485-v7tkh         0/1     Terminating         0          90s
nginx-7c5464d485-v7tkh         0/1     Terminating         0          90s
nginx-7c5464d485-v7tkh         0/1     Terminating         0          90s
nginx-7c5464d485-cvrg4         1/1     Running             0          8s
```



`deployment.yaml`

```yaml
apiVersion: apps/v1 # for versions before 1.9.0 use apps/v1beta2
kind: Deployment
metadata:
  name: nginx
spec:
  selector:
    matchLabels:
      app: nginx
  replicas: 1 # tells deployment to run 1 pods matching the template
  template: # create pods using pod definition in this template
    metadata:
      labels:
        app: nginx
    spec:
      containers:
      - name: nginx
        image: nginx
        ports:
        - containerPort: 80
```


PDB를 배포하면 Eviction이 실패함

`pdb.yaml`
```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: nginx-pdb
spec:
  minAvailable: 1
  selector:
    matchLabels:
      app: nginx
```


```json
{
  "kind": "Status",
  "apiVersion": "v1",
  "metadata": {},
  "status": "Failure",
  "message": "Cannot evict pod as it would violate the pod's disruption budget.",
  "reason": "TooManyRequests",
  "details": {
    "causes": [
      {
        "reason": "DisruptionBudget",
        "message": "The disruption budget nginx-pdb needs 1 healthy pods and has 1 currently"
      }
    ]
  },
  "code": 429
* Connection #0 to host A8845D3F0E5C385227204D33B8635ABC.sk1.ap-northeast-2.eks.amazonaws.com left intact
}
```