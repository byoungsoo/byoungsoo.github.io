---
layout: post
title: "EKS iptables 동작방식"
author: "Bys"
category: incubator
date: 2023-01-01 01:00:00
tags: eks node iptables
---

# [iptables]()


## 1. EKS 노드의 iptables

1. Launch Template

`InstanceRole`  
```yaml
```

`Userdata`  
```bash
#!/bin/bash
cat <<'EOF' >> /etc/ecs/ecs.config
ECS_CLUSTER=bys-dev-ecs-main
EOF
```



2. AutoScaling 그룹

3. ECS 클러스터 생성




<br><br><br>

> Ref: https://docs.aws.amazon.com/ko_kr/AmazonECS/latest/developerguide/instance_IAM_role.htmleiifccvjrkfbhvkkljedgtubttghheulnvichvntunic
> 