# Byoungsoo's Tech Blog

개인 학습 내용을 정리한 기술 블로그입니다. 클라우드, 쿠버네티스, MLOps, 개발 등 다양한 IT 기술에 대한 실습과 학습 내용을 기록하고 있습니다.

🌐 **Blog URL**: [https://byoungsoo.github.io](https://byoungsoo.github.io)

## 📚 주요 카테고리

### ☁️ Cloud & Infrastructure
- **AWS**: EKS, EC2, S3, Lambda, IAM 등 AWS 서비스 활용
- **OpenStack**: Heat, Aodh 등 오픈스택 기술
- **Terraform**: Infrastructure as Code
- **CDK**: AWS Cloud Development Kit

### 🚢 Kubernetes & Container
- **Kubernetes**: 클러스터 구성, 컨트롤러, 네트워킹, CSI 드라이버
- **EKS**: AWS 관리형 쿠버네티스 서비스
- **Container**: Docker, Containerd, CRI
- **Service Mesh**: Istio, App Mesh

### 🤖 MLOps & AI
- **MLflow**: 머신러닝 실험 관리
- **Jupyter**: 데이터 분석 환경
- **Spark**: 빅데이터 처리
- **Keycloak**: 인증/인가 시스템
- **Seldon Core**: 모델 서빙
- **Kubeflow**: 머신러닝 파이프라인

### 🛠️ DevOps & Tools
- **CI/CD**: GitLab, Jenkins, ArgoCD
- **Monitoring**: Prometheus, Grafana, EFK Stack
- **Storage**: MinIO, PostgreSQL
- **Security**: PKI, HTTPS, SSH

### 📖 Books & Learning
- **Clean Architecture**: 클린 아키텍처 학습
- **Spring Framework**: 스프링 프레임워크 심화
- **Design Patterns**: 디자인 패턴 학습
- **Linux**: 리눅스 시스템 관리

## 🏗️ 프로젝트 구조

```
├── _posts/                 # 블로그 포스트
│   ├── it/                 # IT 기술 관련
│   │   ├── cloud/          # 클라우드 기술
│   │   ├── k8s/            # 쿠버네티스
│   │   ├── ml/             # MLOps & AI
│   │   ├── solution/       # 솔루션 구축
│   │   ├── command/        # 명령어 정리
│   │   ├── etc/            # 기타 기술
│   │   └── _incubator/     # 작성 중인 글
│   └── book/               # 도서 학습 내용
│       ├── dev/            # 개발 관련 도서
│       └── os/             # 운영체제 관련
├── _layouts/               # Jekyll 레이아웃
├── _includes/              # Jekyll 인클루드
├── assets/                 # 이미지, CSS 등 자원
├── category/               # 카테고리 페이지
└── codeseries/             # 코드 시리즈
```

## 🚀 개발 환경

### 기술 스택
- **Static Site Generator**: Jekyll 4.3+
- **Theme**: Custom theme (기존 minimal-mistakes 기반)
- **Hosting**: GitHub Pages
- **Deployment**: Manual deployment

### 로컬 개발 설정

```bash
# 의존성 설치
bundle install

# 로컬 서버 실행
bundle exec jekyll serve

# 또는 Makefile 사용
make serve
```

### 태그 생성
```bash
# 자동 태그 파일 생성
python3 taggen.py
```

## 📝 글쓰기 가이드

### 포스트 작성 형식
```yaml
---
layout: post
title: "제목"
author: "Bys"
category: cloud  # cloud, k8s, ml, solution, etc
date: YYYY-MM-DD HH:MM:SS
tags: aws eks kubernetes
---

내용...
```

### 카테고리별 디렉토리
- `cloud`: AWS, OpenStack 등 클라우드 기술
- `k8s`: 쿠버네티스 관련 기술
- `ml`: MLOps, AI/ML 관련 기술
- `solution`: 솔루션 구축 및 설정
- `command`: 명령어 및 CLI 도구
- `etc`: 기타 IT 기술

## 🔧 유지보수

### 주요 스크립트
- `taggen.py`: 태그 페이지 자동 생성
- `Makefile`: 개발 워크플로우 자동화
- GitHub Pages: 정적 사이트 호스팅

### 개선 사항
- ✅ Jekyll 4.3+ 업그레이드
- ✅ SEO 최적화
- ✅ 보안 헤더 추가
- ✅ 태그 시스템 개선

## 📊 통계

- **총 포스트**: 100+ 개
- **주요 카테고리**: Cloud (40+), Kubernetes (20+), MLOps (15+)
- **학습 기간**: 2019년 ~ 현재
- **업데이트**: 지속적

## 📞 연락처

- **GitHub**: [@byoungsoo](https://github.com/byoungsoo)
- **Blog**: [byoungsoo.github.io](https://byoungsoo.github.io)

---

> 이 블로그는 개인 학습 목적으로 작성되었으며, 지속적으로 업데이트됩니다.