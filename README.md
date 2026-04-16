# bys Blog

🌐 **Blog URL**: [https://byoungsoo.github.io](https://byoungsoo.github.io)

## 🚀 Version
- **v2.8**: Jekyll → Hugo 마이그레이션, SEO 최적화 (GA4, OG태그, JSON-LD, sitemap)
- **v2.7**: 블로그 컨텐츠 정리 및 오타/문법 수정
- **v2.0**: 밝은 파란색 느낌의 깔끔한 신규 스타일 적용
- **v1.0**: 기존 Jekyll 테마 스타일 활용

## 📚 주요 카테고리

### ☁️ Cloud & Infrastructure
- **AWS**: EKS, EC2, S3, Lambda, IAM 등 AWS 서비스 활용
- **OpenStack**: Heat, Aodh 등 오픈스택 기술
- **Terraform**: Infrastructure as Code
- **CDK**: AWS Cloud Development Kit

### ☸️ Kubernetes & Container
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
- **LLM Agents**: OpenAI Agents SDK, CrewAI, LangGraph

### 🛠️ DevOps & Tools
- **CI/CD**: GitLab, Jenkins, ArgoCD
- **Monitoring**: Prometheus, Grafana, EFK Stack, CloudWatch
- **Storage**: MinIO, PostgreSQL
- **Security**: PKI, HTTPS, SSH

### 📖 Books & Learning
- **Clean Architecture**: 클린 아키텍처 학습
- **Spring Framework**: 스프링 프레임워크 심화
- **Design Patterns**: 디자인 패턴 학습
- **Linux**: 리눅스 시스템 관리

## 🏗️ 프로젝트 구조

```
├── content/                # 블로그 포스트 (Hugo)
│   ├── cloud/              # 클라우드 기술
│   ├── k8s/                # 쿠버네티스
│   ├── ml/                 # MLOps & AI
│   ├── solution/           # 솔루션 구축
│   ├── command/            # 명령어 정리
│   ├── etc/                # 기타 기술
│   ├── dev/                # 개발 관련 도서
│   └── os/                 # 운영체제 관련
├── layouts/                # Hugo 레이아웃
│   ├── _default/           # baseof, single, list
│   └── partials/           # head, header, footer
├── static/                 # 정적 파일 (CSS, JS, 이미지)
│   ├── css/                # 스타일시트
│   ├── js/                 # 자바스크립트
│   ├── assets/             # 포스트 이미지
│   └── robots.txt          # 크롤링 설정
├── .github/workflows/      # GitHub Actions 배포
└── hugo.toml               # Hugo 설정
```

## 🚀 개발 환경

### 기술 스택
- **Static Site Generator**: Hugo 0.156+
- **Theme**: Custom theme (Bootstrap 기반)
- **Hosting**: GitHub Pages
- **Deployment**: GitHub Actions (hugo.yml)

### 로컬 개발 설정

```bash
# Hugo 설치 (Mac)
brew install hugo

# 개발 서버 실행
./start_hugo.sh

# 개발 서버 중지
./stop_hugo.sh

# 개발 서버 재시작
./restart_hugo.sh

# 직접 실행
hugo server -D --port 1313
```

### 빌드
```bash
hugo build
```

## 📝 글쓰기 가이드

### 포스트 작성 형식
```yaml
---
title: "제목"
author: "Bys"
categories: ["cloud"]
date: 2026-03-10
tags: ["eks", "backup", "aws"]
description: "포스트 내용을 요약한 150자 이내의 설명 (SEO용)"
keywords: ["eks", "backup", "aws"]
draft: false
---

내용...
```

### 카테고리별 디렉토리
- `content/cloud/`: AWS, OpenStack 등 클라우드 기술
- `content/k8s/`: 쿠버네티스 관련 기술
- `content/ml/`: MLOps, AI/ML 관련 기술
- `content/solution/`: 솔루션 구축 및 설정
- `content/command/`: 명령어 및 CLI 도구
- `content/etc/`: 기타 IT 기술

## 🔧 유지보수

### 주요 스크립트
- `start_hugo.sh`: Hugo 개발 서버 시작 (백그라운드, 포트 1313)
- `stop_hugo.sh`: Hugo 개발 서버 중지
- `restart_hugo.sh`: Hugo 개발 서버 재시작
- `commit.sh`: Git 커밋 및 푸시 자동화

### SEO 설정
- **GA4**: `hugo.toml`의 `ga_tracking_id = "G-Y4MJ6RFLWJ"`
- **메타태그**: `layouts/partials/head.html` (OG, Twitter Card, JSON-LD, 캐노니컬)
- **sitemap**: Hugo 자동 생성 (`/sitemap.xml`)
- **robots.txt**: `static/robots.txt`

### CSS 파일 구조
블로그의 스타일링은 역할별로 분리된 CSS 파일들로 관리됩니다:

#### 📁 핵심 CSS 파일들
- **`main.css`**: 기본 스타일, 헤더, 전역 설정
- **`category-style.css`**: 모든 카테고리 페이지 공통 스타일 (카드 레이아웃, 그리드)
- **`navigation-style.css`**: 네비게이션 메뉴 전용 스타일 (드롭다운, 호버 효과)
- **`contact-style.css`**: Contact 페이지 전용 스타일
- **`tags-style.css`**: Tags 페이지 전용 스타일
- **`footer-style.css`**: Footer 전용 스타일
- **`home-style.css`**: 홈페이지 전용 스타일 (히어로 섹션, 카테고리 쇼케이스)
- **`post-style.css`**: 포스트 페이지 전용 스타일
- **`book-style.css`**: 독서 페이지 전용 스타일

#### 🎯 디자인 시스템
- **색상 테마**: 밝은 블루 그라데이션 (`#74b9ff` → `#0984e3`)
- **레이아웃**: 카드 기반 그리드 시스템 (최대 너비 1500px)
- **타이포그래피**: 일관된 폰트 크기 (제목 1.4rem, 날짜 1.1rem)
- **반응형**: 모바일 최적화 (768px 브레이크포인트)

## 🏗️ 레이아웃 구조

### layouts 폴더 구성
- **`_default/baseof.html`**: HTML 뼈대 (헤더, 푸터 포함)
- **`_default/single.html`**: 포스트 상세 페이지
- **`_default/list.html`**: 포스트 목록 페이지
- **`partials/head.html`**: HTML 헤드 섹션 (CSS, JS, SEO 메타태그)
- **`partials/header.html`**: 네비게이션 헤더
- **`partials/footer.html`**: 푸터
- **`category-page.html`**: 카테고리별 포스트 목록
- **`tags-page.html`**: 전체 태그 목록
- **`book-page.html`**: 독서 기록 페이지

## 📞 연락처
- **GitHub**: [@byoungsoo](https://github.com/byoungsoo)
- **Blog**: [byoungsoo.github.io](https://byoungsoo.github.io)

---

> 이 블로그는 개인 학습 목적으로 작성되었으며, 지속적으로 업데이트중입니다.
