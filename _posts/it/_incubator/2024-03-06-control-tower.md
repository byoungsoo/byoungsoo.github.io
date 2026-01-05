---
layout: post
title: "Template"
author: "Bys"
category: incubator
date: 2023-01-01 01:00:00
tags: incubator
published: false
---

# [CT](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/clusters.html)  


- AccountFactory: 계정을 배포 (Service Catalog)

- AFC(AccountFactory for Customization): 계정을 배포할 때 혹은 업데이트할 때 계정에 리소스를 배포 (Service Catalog), 한 번예 1 계정

- CFCT(Custmization for ControlTower): 계정이 배포되고 나서 해당 계정들에 리소스를 배포 (Service Catalog X), 
  - https://docs.aws.amazon.com/ko_kr/controltower/latest/userguide/cfct-template.html
  - https://github.com/aws-solutions/aws-control-tower-customizations/blob/main/customizations-for-aws-control-tower.template
    - customizations-for-aws-control-tower.template 배포하면 자동으로 아키텍처가 구성이 됨

- AFT(AccountFactory for Terraform): 계정을 배포할 때 Terraform 이용