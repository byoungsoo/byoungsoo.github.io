---
layout: post
title: "MLOps - PostgreSQL"
author: "Bys"
category: ml
date: 2025-02-07 01:00:00
tags: mlops postgresql
---

## MLOps

### [PostgreSQL]

### [Install PostgreSQL(Bitnami Version)](https://github.com/bitnami/charts/tree/main/bitnami/postgresql) 

`values.yaml`
```yaml
auth:
  enablePostgresUser: true
  postgresPassword: "postgres"
  username: "postgres"
  password: "postgres"
  database: ""
primary:
  persistence:
    size: 15Gi
```

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update bitnami

helm upgrade -i postgresql bitnami/postgresql -n postgresql -f /Users/bys/workspace/kubernetes/mlops/postgresql/values.yaml
helm delete postgresql -n postgresql

# PostgreSQL can be accessed via port 5432 on the following DNS names from within your cluster:
postgresql.postgresql.svc.cluster.local - Read/Write connection

# To get the password for "postgres" run:
export POSTGRES_PASSWORD=$(kubectl get secret --namespace postgresql postgresql -o jsonpath="{.data.postgres-password}" | base64 -d)

kubectl exec -it postgresql-0 -n postgresql -- psql -U postgres
```