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
이번의 경우는 AWS 계정을 생성 한 후 가장 기본적인 수준의 Landing Zone 구성을 Terraform을 통해 진행해 보려고 한다.  
적당한 위치에 다음과 같은 C:\dev\terraform_0.14.8\terraform\vpc 폴더 구조를 생성하고 vpc 폴더 밑으로 아래와 같이 4개의 파일을 구성했다.  
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
```json
provider "aws" {
access_key = "**********************"
secret_key = "*******************************"
region     = "ap-northeast-2"
}
```

`vpc.tf`  
vpc.tf의 경우 vpc 및 subnet의 resource 정보가 담겨있다.   
vpc생성 시 "smp_dev" 이름을 설정하여 subnet에 구성 시 ${aws_vpc.smp_dev.id} 라는 변수 값을 통해 해당 vpc내 subnet을 구성 할 수 있다.  
```json
// vpc
resource "aws_vpc" "smp_dev" { 
  cidr_block  = "10.10.0.0/16" 
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

// private subnets
resource "aws_subnet" "smp_dev_public_subnet1" {
  vpc_id = "${aws_vpc.smp_dev.id}"
  cidr_block = "10.10.1.0/24"
  map_public_ip_on_launch = false
  availability_zone = "ap-northeast-2a"
  tags = {
    Name = "smp_dev_public_subnet1"
  }
}

resource "aws_subnet" "smp_dev_public_subnet2" {
  vpc_id = "${aws_vpc.smp_dev.id}"
  cidr_block = "10.10.2.0/24"
  map_public_ip_on_launch = true
  availability_zone = "ap-northeast-2c"
  tags = {
    Name = "smp_dev_public_subnet2"
  }
}

// private subnets
resource "aws_subnet" "smp_dev_private_subnet1" {
  vpc_id = "${aws_vpc.smp_dev.id}"
  cidr_block = "10.10.3.0/24"
  availability_zone = "ap-northeast-2a"
  tags = {
    Name = "smp_dev_private_subnet1"
  }
}

resource "aws_subnet" "smp_dev_private_subnet2" {
  vpc_id = "${aws_vpc.smp_dev.id}"
  cidr_block = "10.10.11.0/24"
  availability_zone = "ap-northeast-2c"
  tags = {
    Name = "smp_dev_private_subnet2"
  }
}
```


`gateway.tf`  
gateway.tf는 Internet Gateway, NAT Gateway를 구성한다.   
```json
// igw
resource "aws_internet_gateway" "smp_dev_igw" {
  vpc_id = "${aws_vpc.smp_dev.id}"

  tags = {
    Name = "main"
  }
}

// eip for NAT
resource "aws_eip" "smp_dev_nat_eip" {
  vpc = true
  depends_on = ["aws_internet_gateway.smp_dev_igw"]
}

// NAT gateway
resource "aws_nat_gateway" "smp_dev_nat" {
  allocation_id = "${aws_eip.smp_dev_nat_eip.id}"
  subnet_id = "${aws_subnet.smp_dev_public_subnet1.id}"
  depends_on = ["aws_internet_gateway.smp_dev_igw"]
}
```


`security.tf`  
security.tf는 network acl 및 security group에 대한 설정 정보를 구성한다.  
```json
//network acl default
resource "aws_default_network_acl" "smp_dev_default" {
  default_network_acl_id = "${aws_vpc.smp_dev.default_network_acl_id}"

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


// network acl for public subnets
resource "aws_network_acl" "smp_dev_public" {
  vpc_id = "${aws_vpc.smp_dev.id}"
  subnet_ids = [
    "${aws_subnet.smp_dev_public_subnet1.id}",
    "${aws_subnet.smp_dev_public_subnet2.id}",
  ]

  tags = {
    Name = "public"
  }
}


// default security group
resource "aws_default_security_group" "smp_dev_default" {
  vpc_id = "${aws_vpc.smp_dev.id}"

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
    Name = "default"
  }
}


// Basiton Host
resource "aws_security_group" "smp_dev_bastion" {
  name = "sg_bastion"
  description = "Security group for bastion instance"
  vpc_id = "${aws_vpc.smp_dev.id}"

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
    Name = "sg_bastion"
  }
}
```


`route.tf`  
route.tf는 subnet 간 routing 테이블을 설정한다.  
```json
resource "aws_default_route_table" "smp_dev" {
  default_route_table_id = "${aws_vpc.smp_dev.default_route_table_id}"

  tags = {
    Name = "default"
  }
}


// route to internet
resource "aws_route" "smp_dev_internet_access" {
  route_table_id = "${aws_vpc.smp_dev.main_route_table_id}"
  destination_cidr_block = "0.0.0.0/0"
  gateway_id = "${aws_internet_gateway.smp_dev_igw.id}"
}


// private route table
resource "aws_route_table" "smp_dev_private_route_table" {
  vpc_id = "${aws_vpc.smp_dev.id}"
  
  tags = {
    Name = "private"
  }
}

resource "aws_route" "private_route" {
  route_table_id = "${aws_route_table.smp_dev_private_route_table.id}"
  destination_cidr_block = "0.0.0.0/0"
  nat_gateway_id = "${aws_nat_gateway.smp_dev_nat.id}"
}



// associate subnets to route tables
resource "aws_route_table_association" "smp_dev_public_subnet1_association" {
  subnet_id = "${aws_subnet.smp_dev_public_subnet1.id}"
  route_table_id = "${aws_vpc.smp_dev.main_route_table_id}"
}

resource "aws_route_table_association" "smp_dev_public_subnet2_association" {
  subnet_id = "${aws_subnet.smp_dev_public_subnet2.id}"
  route_table_id = "${aws_vpc.smp_dev.main_route_table_id}"
}

resource "aws_route_table_association" "smp_dev_private_subnet1_association" {
  subnet_id = "${aws_subnet.smp_dev_private_subnet1.id}"
  route_table_id = "${aws_route_table.smp_dev_private_route_table.id}"
}

resource "aws_route_table_association" "smp_dev_private_subnet2_association" {
  subnet_id = "${aws_subnet.smp_dev_private_subnet2.id}"
  route_table_id = "${aws_route_table.smp_dev_private_route_table.id}"
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


