---
slug: parallel-cluster
author: Bys
categories:
- cloud
date: '2026-06-08 01:00:00'
tags:
- aws
- parallel-cluster
- slurm
title: Parallel Cluster
---

# [Parallel Cluster](https://docs.aws.amazon.com/parallelcluster/latest/ug/what-is-aws-parallelcluster.html)  
AWS Parallel Cluster는 AWS에서 지원하는 오픈 소스 클러스터 관리 도구로 HPC(High-performance Computing)을 배포하고 관리하는데 도움을 준다. 이 도구는 AWS Batch 및 Slurm 스케줄러와 함께 사용할 수 있는데 이 문서에서는 Slurm 을 통해 사용하는 방법과 내부 동작에 대해 살펴본다.  


## 1. Architecture

![architecture](/assets/it/cloud/parallel-cluster/architecture.png)


## 2. Overview

Parallel Cluster는 AWS 콘솔에 서비스로 존재하지 않는다. Parallel Cluster 서비스는 일반적으로 pcluster CLI를 통해 구성하며, pcluster를 통해 생성하면 위 아키텍처와 같이 `Head Node`가 생성된다. 

`Head Node`에는 Slurm이 구성되어 있으며, Slurm을 통해 잡을 제출하고 관리한다. Slurm 은 `Power Saving(ResumeProgram/SuspendProgram)` 인터페이스를 제공하는데 이를 구현하면 compute resources를 시작하고 중단할 수 있게 되어 있다. Parallel Cluster 서비스는 이 Slurm Power Saving 인터페이스(ResumeProgram/SuspendProgram)를 구현해서 EC2 API를 호출하는 방식이다.  

---

1. ParallelCluster 서비스를 이용하기 위해 pcluster cli를 설치하고 구성하면 `Head Node`가 생성된다.
2. `Head Node`에 접속하면 Slurm이 설치되어 있으며, 설정파일(/opt/slurm/etc/slurm.conf)을 확인해보면 아래와 같이 ResumeProgram, SuspendProgram 에 대한 구현이 되어 있음을 알 수 있다.  
    ```bash
    # CLOUD CONFIGS OPTIONS
    SuspendProgram=/opt/parallelcluster/scripts/slurm/slurm_suspend
    ResumeProgram=/opt/parallelcluster/scripts/slurm/slurm_resume
    ResumeFailProgram=/opt/parallelcluster/scripts/slurm/slurm_suspend
    ......
    ```
3. 스크립트를 따라 구현체를 살펴보면, 결국 Python을 통해 EC2 인스턴스를 실행하고 중지하는 프로세스가 포함되어 있다.  
4. 결국 Slurm과 Computing을 관리하는 스크립트의 조합이 결국 ParallelCluster 라고 이해할 수 있다.  


## 3. Install

설정은 [AWS 공식문서](https://docs.aws.amazon.com/parallelcluster/latest/ug/cluster-configuration-file-v3.html)를 참고해서 진행할 수 있다.  
`cluster-config.yaml`  
```bash
Region: ap-northeast-2
Image:
  Os: alinux2023
HeadNode:
  InstanceType: c7i.xlarge
  Networking:
    SubnetId: subnet-0ea5be4984975e8ed
  Ssh:
    KeyName: bys-dev-an2-key
Scheduling:
  Scheduler: slurm
  SlurmQueues:
  - Name: queue1
    CustomActions:
      OnNodeConfigured:
        Script: s3://bys-dev-s3-temp/install_mpi4py.sh
    ComputeResources:
    - Name: pc-c7ilarge
      Instances:
      - InstanceType: c7i.large
      MinCount: 0
      MaxCount: 10
    Networking:
      SubnetIds:
      - subnet-0bbd4c134a3589aee
```

```bash
pcluster create-cluster --cluster-name test-cluster --cluster-configuration cluster-config.yaml
```

pcluster 커맨드를 통해 클러스터를 관리할 수 있다. 

AWS ParallelCluster 서비스의 경우 AWS 콘솔에서 제공되는 메뉴가 없지만, [CloudFormation stack](https://docs.aws.amazon.com/parallelcluster/latest/ug/install-pcui-v3.html)을 배포해서 클러스터 관리화면도 만들 수 있다. 

![pcluster-ui](/assets/it/cloud/parallel-cluster/pcluster-ui.png)



## 4. Test
MPI 병렬 프로그램 테스트

`mpi_hello.py`  
```python
from mpi4py import MPI

comm = MPI.COMM_WORLD
rank = comm.Get_rank()
size = comm.Get_size()

print(f"Hello from rank {rank} of {size} on {MPI.Get_processor_name()}")

local_value = rank * 10
total = comm.reduce(local_value, op=MPI.SUM, root=0)

if rank == 0:
    print(f"Sum of all ranks: {total}")
```


`submit_mpi.sh`  
```
#!/bin/bash
#SBATCH --job-name=mpi_test
#SBATCH --partition=queue1
#SBATCH --nodes=2
#SBATCH --ntasks-per-node=2
#SBATCH --time=00:05:00
#SBATCH --output=mpi_test_%j.out

export PATH=/opt/amazon/openmpi/bin:$PATH
export LD_LIBRARY_PATH=/opt/amazon/openmpi/lib64:$LD_LIBRARY_PATH

mpirun python3 mpi_hello.py
```

1. mpi_hello.py 프로그램을 2개의 노드에서 각 2개의 태스크로 실행 
2. srun이 각 프로세스에 rank를 부여
3. 각 로컬에서 local_value 값 계산
4. comm.reduce() 호출 시 4개 프로세스가 네트워크로 통신하여 rank 0로 합산 결과 전달

```bash
   노드1 (queue1-dy-pc-c7ilarge-1)        노드2 (queue1-dy-pc-c7ilarge-2)
   ┌─────────────────────────────┐        ┌─────────────────────────────┐
   │  rank 0       rank 1        │        │  rank 2       rank 3        │
   │  local=0      local=10      │        │  local=20     local=30      │
   └─────────────────────────────┘        └─────────────────────────────┘
            │           │                          │           │
            └───────────┴──────────┬───────────────┘           │
                                   │  네트워크 통신 (MPI)         │
                                   ├───────────────────────────┘
                                   ▼
                            rank 0에서 reduce
                            0 + 10 + 20 + 30 = 60

```


```bash
# Submit job
sbatch submit_mpi.sh

# Check job
squeue
```

결과를 보면 정상 동작한 것을 볼 수 있다.  
```bash
$ cat mpi_test_13.out
Hello from rank 0 of 4 on queue1-dy-pc-c7ilarge-3
Hello from rank 1 of 4 on queue1-dy-pc-c7ilarge-3
Hello from rank 3 of 4 on queue1-dy-pc-c7ilarge-4
Hello from rank 2 of 4 on queue1-dy-pc-c7ilarge-4
Sum of all ranks: 60
```


## 5. Summary
Slurm 과 AWS 클라우드 컴퓨팅을 이용해 HPC(High-Performance Computing)를 배포하고 관리할 수 있도록 한 서비스 중 하나가 AWS ParallelCluster 서비스로 볼 수 있다. 

---

## 📚 References

[1] Cluster configuration file
- https://docs.aws.amazon.com/parallelcluster/latest/ug/cluster-configuration-file-v3.html

[2] AWS ParallelCluster Blog
- https://aws.amazon.com/ko/blogs/machine-learning/building-an-interactive-and-scalable-ml-research-environment-using-aws-parallelcluster/

[3] UI Install
- https://docs.aws.amazon.com/parallelcluster/latest/ug/install-pcui-v3.html

- https://t0b2aeok22.execute-api.ap-northeast-2.amazonaws.com/pcui/clusters