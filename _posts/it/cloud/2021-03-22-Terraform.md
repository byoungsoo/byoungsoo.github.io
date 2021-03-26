---
layout: post
title: "Terraform 사용법, Terraform을 통해 VPC 구성하기"
author: "Bys"
category: cloud
date: 2021-03-22 01:00:00
tags: terraform vpc
---


Terraform 사용하기  

#### 사전지식  

**- HCLHashicorp Configuration Language**  
테라폼에서 사용하는 설정 언어. 테라폼에서 모든 설정과 리소스 선언은 HCL을 사용하며 HCL 파일의 확장자는 .tf를 사용한다.


**- 계획Plan**  
테라폼 프로젝트 디렉터리 아래의 모든 .tf 파일의 내용을 실제로 적용 가능한지 확인하는 작업을 계획이라고 한다. 테라폼은 이를 terraform plan 명령어로 제공하며, 이 명령어를 실행하면 어떤 리소스가 생성되고, 수정되고, 삭제될지 계획을 보여준다.

**- 적용Apply**  
테라폼 프로젝트 디렉터리 아래의 모든 .tf 파일의 내용대로 리소스를 생성, 수정, 삭제하는 일을 적용이라고 한다.  

>참고: https://www.44bits.io/ko/post/terraform_introduction_infrastrucute_as_code#%ED%85%8C%EB%9D%BC%ED%8F%BC-%EB%B2%84%EC%A0%84-%EA%B4%80%EB%A6%AC


#### 1. 시작하기  
간단하게 Terraform을 적용해보기로 한다. 적당한 위치에 폴더를 만든다.  
이번의 경우는 AWS 계정을 생성 한 후 기본적인 수준의 Landing Zone 구성을 Terraform을 통해 진행해 보려고 한다.  
적당한 위치에 다음과 같은 C:\dev\terraform_0.14.8\terraform\vpc 폴더 구조를 생성하고 vpc 폴더 밑으로 아래와 같이 5개의 파일을 구성했다.  
tf 파일은 용도에 맞게 적당히 합쳐도, 나누어도 무방하다.  

`provider.tf`
`vpc.tf`
`gateway.tf`
`security.tf`
`route.tf`  
  

#### 2. tf파일 만들기  
vpc 폴더 밑으로 생성 된 tf파일의 내용은 아래와 같다.  

`provider.tf`  

provider.tf는 AWS계정의 IAM Security credential의 Access Keys 파일의 정보이다.  
Terraform 수행 계정이 된다.  
```js
provider "aws" {
access_key = "**********************"
secret_key = "*******************************"
region     = "ap-northeast-2"
}
```

`vpc.tf`  

vpc.tf의 경우 vpc 및 subnet의 resource 정보가 담겨있다.   
vpc생성 시 "smp_dev_vpc" 와 같이 이름을 설정하여 다른 resource 구성 시 aws_vpc.smp_dev_vpc.id 와 같이 변수 사용하여 추가적인 resource를 구성 할 수 있다.  

Subnet은 Public인 DMZ, Private인 APP, DB, EKS로 나누어진다.  
모든 통신은 DMZ망을 통해서 Private인 AP서버 DB서버 등으로 들어올 예정이다.  
사용자는 DMZ에 Bastion Host를 생성하여 SSH접속을 시도한다.  

```js
#Naming Rule
#ProjectCode-Account-Resource-{att1}-{zone}

// vpc
resource "aws_vpc" "smp_dev_vpc" { 
  cidr_block  = "10.20.0.0/16" 
  instance_tenancy = "default" 
  enable_dns_support                = true 
  enable_dns_hostnames              = true 
  enable_classiclink                = false 
  enable_classiclink_dns_support    = false 
  assign_generated_ipv6_cidr_block  = false 

  tags = { 
          "Name"      = "SMP-DEV-VPC" 
        }
}

// public subnets
resource "aws_subnet" "smp_dev_sbn_az1_dmz" {
  vpc_id = aws_vpc.smp_dev_vpc.id
  cidr_block = "10.20.1.0/24"
  map_public_ip_on_launch = false
  availability_zone = "ap-northeast-2a"
  tags = {
    Name = "SMP-DEV-SBN-AZ1-DMZ"
  }
}

resource "aws_subnet" "smp_dev_sbn_az2_dmz" {
  vpc_id = aws_vpc.smp_dev_vpc.id
  cidr_block = "10.20.2.0/24"
  map_public_ip_on_launch = true
  availability_zone = "ap-northeast-2c"
  tags = {
    Name = "SMP-DEV-SBN-AZ2-DMZ"
  }
}

// private subnets
resource "aws_subnet" "smp_dev_sbn_az1_app" {
  vpc_id = aws_vpc.smp_dev_vpc.id
  cidr_block = "10.20.10.0/24"
  availability_zone = "ap-northeast-2a"
  tags = {
    Name = "SMP-DEV-SBN-AZ1-APP"
  }
}

resource "aws_subnet" "smp_dev_sbn_az2_app" {
  vpc_id = aws_vpc.smp_dev_vpc.id
  cidr_block = "10.20.11.0/24"
  availability_zone = "ap-northeast-2c"
  tags = {
    Name = "SMP-DEV-SBN-AZ2-APP"
  }
}

// private subnets
resource "aws_subnet" "smp_dev_sbn_az1_elb" {
  vpc_id = aws_vpc.smp_dev_vpc.id
  cidr_block = "10.20.12.0/24"
  availability_zone = "ap-northeast-2a"
  tags = {
    Name = "SMP-DEV-SBN-AZ1-ELB"
  }
}

resource "aws_subnet" "smp_dev_sbn_az2_elb" {
  vpc_id = aws_vpc.smp_dev_vpc.id
  cidr_block = "10.20.13.0/24"
  availability_zone = "ap-northeast-2c"
  tags = {
    Name = "SMP-DEV-SBN-AZ2-ELB"
  }
}

resource "aws_subnet" "smp_dev_sbn_az1_eks" {
  vpc_id = aws_vpc.smp_dev_vpc.id
  cidr_block = "10.20.14.0/24"
  availability_zone = "ap-northeast-2a"
  tags = {
    Name = "SMP-DEV-SBN-AZ1-EKS"
  }
}

resource "aws_subnet" "smp_dev_sbn_az2_eks" {
  vpc_id = aws_vpc.smp_dev_vpc.id
  cidr_block = "10.20.15.0/24"
  availability_zone = "ap-northeast-2c"
  tags = {
    Name = "SMP-DEV-SBN-AZ2-EKS"
  }
}


resource "aws_subnet" "smp_dev_sbn_az1_db" {
  vpc_id = aws_vpc.smp_dev_vpc.id
  cidr_block = "10.20.16.0/24"
  availability_zone = "ap-northeast-2a"
  tags = {
    Name = "SMP-DEV-SBN-AZ1-DB"
  }
}

resource "aws_subnet" "smp_dev_sbn_az2_db" {
  vpc_id = aws_vpc.smp_dev_vpc.id
  cidr_block = "10.20.17.0/24"
  availability_zone = "ap-northeast-2c"
  tags = {
    Name = "SMP-DEV-SBN-AZ2-DB"
  }
}
```


`gateway.tf`  

gateway.tf는 Internet Gateway, NAT ateway를 구성한다.   
```js
#ProjectCode-Account-Resource-{att1}-{zone}
// igw
resource "aws_internet_gateway" "smp_dev_igw" {
  vpc_id = aws_vpc.smp_dev_vpc.id

  tags = {
    Name = "SMP-DEV-IGW"
  }
}

// eip for NAT
resource "aws_eip" "smp_dev_eip_nat" {
  vpc = true
  depends_on = [aws_internet_gateway.smp_dev_igw]

  tags = {
    Name = "IGW"
  }
}

// NAT gateway
resource "aws_nat_gateway" "smp_dev_nat" {
  allocation_id = aws_eip.smp_dev_eip_nat.id
  subnet_id = aws_subnet.smp_dev_sbn_az1_dmz.id
  depends_on = [aws_internet_gateway.smp_dev_igw]
}
```


`security.tf`  

security.tf는 network acl 및 security group에 대한 설정 정보를 구성한다.  
```js
//network acl default
resource "aws_default_network_acl" "SMP-DEV-NACL-DEFAULT" {
  default_network_acl_id = aws_vpc.smp_dev_vpc.default_network_acl_id 
  
  subnet_ids = [
    aws_subnet.smp_dev_sbn_az1_dmz.id,
    aws_subnet.smp_dev_sbn_az2_dmz.id,
    
    aws_subnet.smp_dev_sbn_az1_app.id,
    aws_subnet.smp_dev_sbn_az2_app.id,

    aws_subnet.smp_dev_sbn_az1_eks.id,
    aws_subnet.smp_dev_sbn_az2_eks.id,

    aws_subnet.smp_dev_sbn_az1_db.id,
    aws_subnet.smp_dev_sbn_az2_db.id    
  ]

  ingress {
    protocol   = -1
    rule_no    = 100
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 0
  }

  egress {
    protocol   = -1
    rule_no    = 100
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 0
  }

  tags = {
    Name = "default"
  }
}


// default security group
resource "aws_default_security_group" "smp_dev_sg_deafult" {
  vpc_id = aws_vpc.smp_dev_vpc.id

  ingress {
    protocol  = -1
    self      = true
    from_port = 0
    to_port   = 0
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "SMP-DEV-SG-Default"
  }
}


// Basiton Host
resource "aws_security_group" "smp_dev_sg_bastion" {
  name = "SMP-DEV-SG-Bastion"
  description = "Security group for bastion instance"
  vpc_id = aws_vpc.smp_dev_vpc.id

  ingress {
    from_port = 22
    to_port = 22
    protocol = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port = 0
    to_port = 0
    protocol = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "SMP-DEV-SG-Bastion"
  }
}
```


`route.tf`  
route.tf는 subnet 간 routing 테이블을 설정한다.  
```js
//default route table
resource "aws_default_route_table" "smp_dev_rtb_default" {
  default_route_table_id = aws_vpc.smp_dev_vpc.default_route_table_id

  tags = {
    Name = "SMP-DEV-RTB-PUBLIC"
  }
}

// route to internet
resource "aws_route" "smp_dev_rt_public" {
  route_table_id = aws_vpc.smp_dev_vpc.main_route_table_id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id = aws_internet_gateway.smp_dev_igw.id
}


// private route table
resource "aws_route_table" "smp_dev_rtb_private" {
  vpc_id = aws_vpc.smp_dev_vpc.id
  
  tags = {
    Name = "SMP-DEV-RTB-PRIVATE"
  }
}

resource "aws_route" "smp_dev_rt_private" {
  route_table_id = aws_route_table.smp_dev_rtb_private.id
  destination_cidr_block = "0.0.0.0/0"
  nat_gateway_id = aws_nat_gateway.smp_dev_nat.id
}



// associate subnets to route tables
resource "aws_route_table_association" "smp_dev_sbn_az1_dmz_association" {
  subnet_id = aws_subnet.smp_dev_sbn_az1_dmz.id
  route_table_id = aws_vpc.smp_dev_vpc.main_route_table_id
}

resource "aws_route_table_association" "smp_dev_sbn_az2_dmz_association" {
  subnet_id = aws_subnet.smp_dev_sbn_az2_dmz.id
  route_table_id = aws_vpc.smp_dev_vpc.main_route_table_id
}

resource "aws_route_table_association" "smp_dev_sbn_az1_app_association" {
  subnet_id = aws_subnet.smp_dev_sbn_az1_app.id
  route_table_id = aws_route_table.smp_dev_rtb_private.id
}

resource "aws_route_table_association" "smp_dev_sbn_az2_app_association" {
  subnet_id = aws_subnet.smp_dev_sbn_az2_app.id
  route_table_id = aws_route_table.smp_dev_rtb_private.id
}

resource "aws_route_table_association" "smp_dev_sbn_az1_elb_association" {
  subnet_id = aws_subnet.smp_dev_sbn_az1_elb.id
  route_table_id = aws_route_table.smp_dev_rtb_private.id
}

resource "aws_route_table_association" "smp_dev_sbn_az2_elb_association" {
  subnet_id = aws_subnet.smp_dev_sbn_az2_elb.id
  route_table_id = aws_route_table.smp_dev_rtb_private.id
}

resource "aws_route_table_association" "smp_dev_sbn_az1_eks_association" {
  subnet_id = aws_subnet.smp_dev_sbn_az1_eks.id
  route_table_id = aws_route_table.smp_dev_rtb_private.id
}

resource "aws_route_table_association" "smp_dev_sbn_az2_eks_association" {
  subnet_id = aws_subnet.smp_dev_sbn_az2_eks.id
  route_table_id = aws_route_table.smp_dev_rtb_private.id
}

resource "aws_route_table_association" "smp_dev_sbn_az1_db_association" {
  subnet_id = aws_subnet.smp_dev_sbn_az1_db.id
  route_table_id = aws_route_table.smp_dev_rtb_private.id
}

resource "aws_route_table_association" "smp_dev_sbn_az2_db_association" {
  subnet_id = aws_subnet.smp_dev_sbn_az2_db.id
  route_table_id = aws_route_table.smp_dev_rtb_private.id
}
```


  
#### 3. terraform 적용하기    
vpc 폴더 밑으로 해당 tf 파일들을 모두 작성하였다면 현재 vpc 폴더 밑으로는 provider.tf, vpc.tf, gateway.tf, security.tf, route.tf 파일 총 5개의 파일이 존재한다.  

cmd창을 열고 아래의 명령을 수행한다.
```cmd
cd C:\dev\terraform_0.14.8\terraform\vpc

C:\dev\terraform_0.14.8\terraform\vpc> terraform init
...
...
C:\dev\terraform_0.14.8\terraform\vpc> terraform plan
...
...
C:\dev\terraform_0.14.8\terraform\vpc> terraform apply
```

정상적으로 적용이 되었다면 AWS Console에 들어가서 자원이 모두 생성된 것을 확인 할 수 있다.  
해당 내용은 가장 기본적인 수준의 VPC를 구성해본 것이므로 추가적인 내용이나 오류가 있다면 terraform 공식 홈페이지의 내용을 확인하여 tf파일의 내용을 수정하여 init, plan, apply의 단계를 걸치면 된다.  


