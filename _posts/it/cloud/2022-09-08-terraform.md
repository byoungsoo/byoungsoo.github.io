---
layout: post
title: "Terraform 사용법, Terraform을 통해 VPC 구성하기"
author: "Bys"
category: cloud
date: 2022-09-08 01:00:00
tags: terraform vpc
---

---
Created at: 2022-09-08 01:00:00

---


Terraform 사용하기  

#### 사전지식  

**- HCLHashicorp Configuration Language**  
테라폼에서 사용하는 설정 언어. 테라폼에서 모든 설정과 리소스 선언은 HCL을 사용하며 HCL 파일의 확장자는 .tf를 사용한다.  
variable을 사용할 수 있고 .tfvars 파일을 이용하여 변수에 값을 주입하는 것이 가능하다.  

**- 계획Plan**  
테라폼 프로젝트 디렉터리 아래의 모든 .tf 파일의 내용을 실제로 적용 가능한지 확인하는 작업을 계획이라고 한다. 테라폼은 이를 terraform plan 명령어로 제공하며, 이 명령어를 실행하면 어떤 리소스가 생성되고, 수정되고, 삭제될지 계획을 보여준다.

**- 적용Apply**  
테라폼 프로젝트 디렉터리 아래의 모든 .tf 파일의 내용대로 리소스를 생성, 수정, 삭제하는 일을 적용이라고 한다.  


#### 1. 시작하기  
간단하게 Terraform을 적용해보기로 한다. 이번의 경우는 AWS 계정을 생성 한 후 기본적인 수준의 Landing Zone 구성을 Terraform을 통해 진행해 보려고 한다.  
[SourceCode](https://github.com/byoungsoo/terraform.git) <- 소스코드를 적당한 위치에 클론을 받고 아래의 커맨드를 수행하면 aws configure로 설정되어있는 account에 vpc리소스들이 전개된다.  

```
cd terraform/vpc
terraform init
terraform plan -var-file="variable.tfvars"
terraform apply -var-file="variable.tfvars"
```
<br>

#### 2. tf파일 확인  
vpc 폴더 밑으로 생성 된 tf파일의 내용은 아래와 같다.  
`vpc.tf`, `route.tf`, `gateway.tf`, `variable.tf`, `variable.tfvars`  

resource는 terraform block중 가장 중요하다. 하나의 resource block은 provider의 리소스를 describe하고 있기 때문이다. 
아래 예시는 aws의 vpc를 생성하기 위한 resource block이다. 'aws_vpc'는 aws vpc 리소스에 대한 자원을 생성하기 위해 정해져있는 이름이다. [aws_vpc](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/vpc) 
그리고 'bys_vpc'는 이 리소스에 대해 user가 저장한 name이다. 그리고 각각의 속성은 terraform의 aws_vpc문서를 보면 나와있다.  

`vpc.tf`  
```json
resource "aws_vpc" "bys_vpc" { 
  cidr_block  = var.cidr_blocks["vpc"]
  instance_tenancy = "default" 
  enable_dns_support                = true 
  enable_dns_hostnames              = true 
  assign_generated_ipv6_cidr_block  = false 

  tags = { 
          "Name" = "${var.project_code}-${var.account}-vpc" 
        }
}
```

cidr_block의 값으로는 변수에서 사용하고 있다. 변수의 선언은 variable.tf파일에 되어있으며 해당 변수에 대한 값의 주입은 variable.tfvars 파일에 되어있다. 

`variable.tf`  
```json
# vpc/subnet
variable "project_code" {}
variable "account" {}

variable "cidr_blocks" {
  type = map 
}
```

`variable.tfvars`  
```json
## vpc/subnet
project_code = "bys"
account = "shared"

cidr_blocks = {
  vpc = "10.10.0.0/16"
  subnet_az1_dmz = "10.10.1.0/24"
  subnet_az2_dmz = "10.10.2.0/24"

  subnet_az1_extelb = "10.10.11.0/24"
  subnet_az2_extelb = "10.10.12.0/24"

  subnet_az1_app = "10.10.21.0/24"
  subnet_az2_app = "10.10.22.0/24"

  subnet_az1_con = "10.10.31.0/24"
  subnet_az2_con = "10.10.32.0/24"

  subnet_az1_intelb = "10.10.41.0/24"
  subnet_az2_intelb = "10.10.42.0/24"

  subnet_az1_db = "10.10.41.0/24"
  subnet_az2_db = "10.10.42.0/24"
}
```

<br>

  
#### 3. terraform 적용하기    
vpc 폴더 밑으로 해당 tf 파일들을 모두 작성하였다면 현재 vpc 폴더 밑으로는 `vpc.tf`, `route.tf`, `gateway.tf`, `variable.tf`, `variable.tfvars`파일 총 5개의 파일이 존재한다.  
terminal을 열고 아래의 명령을 수행한다.
```bash 
terraform init
terraform plan -var-file="variable.tfvars"
terraform apply -var-file="variable.tfvars"
```

정상적으로 적용이 되었다면 AWS Console에 들어가서 자원이 모두 생성된 것을 확인 할 수 있다.  
해당 내용은 가장 기본적인 수준의 VPC를 구성해본 것이므로 추가적인 내용이나 오류가 있다면 terraform 공식 홈페이지의 내용을 확인하여 tf파일의 내용을 수정하여 init, plan, apply의 단계를 걸치면 된다.  


#### 4. terraform 디버깅
terraform을 실행하고 나서 오류가 발생하면 상세 로그를 디버깅 할 수 있다. TF_LOG 변수를 TRACE레벨로 변경하고 terraform을 실행하면 된다.  
```bash
export TF_LOG=TRACE
terraform apply -no-color 2>&1 | tee apply.log
```



---
Updated at: 2022-09-08 01:00:00

---

#### 10. 리팩토링을 고민한 이유
AWS Account 및 리전에 테라폼을 통해 배포를 하다보니 여러가지 문제점이 발생했다. 폴더레벨 및 Terraform 워크스페이스에 대한 고민을 하기 시작한 것이다. 또한, 반복되는 코드 등에 대해 Refactoring 작업을 진행했다.  
코드는 Github 에서 관리.


#### 11. Refactoring 코드
1. 전체적인 모듈화를 진행하지는 않았다.
2. Workspace는 어카운트별 리소스별로 나누었다. 
3. Workspace간 의존성은 최대한 VPC 리소스만 의존하도록 한다. 
4. 너무 복잡한 코드는 인프라 관리를 어렵게할 수 있다. 
5. EKS 서비스와 같이 모듈을 가져다 사용할 수 있는 서비스는 모듈을 사용하여 클러스터별로 관리한다. 



<br><br><br>


---

## 📚 References

[1] **Variable**  
https://www.terraform.io/language/values/variables  

[2] **Terraform Debugging**  
https://support.hashicorp.com/hc/en-us/articles/360001113727-Enabling-trace-level-logs-in-Terraform-CLI-Cloud-or-Enterprise  

[3] **For each**  
https://developer.hashicorp.com/terraform/language/meta-arguments/count#when-to-use-for_each-instead-of-count
