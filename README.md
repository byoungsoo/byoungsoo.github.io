# bys Blog

🌐 **Blog URL**: [https://byoungsoo.github.io](https://byoungsoo.github.io)

## 🚀 Version
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

# 기본 서버 실행
bundle exec jekyll serve

# 최적화된 개발 서버 (추천)
./dev_serve.sh

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
- `dev_serve.sh`: 최적화된 개발 서버 (증분 빌드, 라이브 리로드)
- GitHub Pages: 정적 사이트 호스팅

### 개발 환경 최적화
- **`_config_dev.yml`**: 개발용 설정 (빠른 빌드)
- **`dev_serve.sh`**: 성능 최적화된 개발 서버
- **증분 빌드**: 변경된 파일만 재빌드로 속도 향상
- **라이브 리로드**: 파일 변경 시 자동 브라우저 새로고침


### CSS 파일 구조
블로그의 스타일링은 역할별로 분리된 CSS 파일들로 관리됩니다:

#### 📁 핵심 CSS 파일들
- **`main.css`**: 기본 스타일, 헤더, 전역 설정 (정리됨)
- **`category-style.css`**: 모든 카테고리 페이지 공통 스타일 (카드 레이아웃, 그리드)
- **`navigation-style.css`**: 네비게이션 메뉴 전용 스타일 (드롭다운, 호버 효과)
- **`contact-style.css`**: Contact 페이지 전용 스타일 (폼, 소셜 아이콘, 정리됨)
- **`tags-style.css`**: Tags 페이지 전용 스타일 (태그 카드, 포스트 리스트)
- **`footer-style.css`**: Footer 전용 스타일 (단순 Copyright, 정리됨)
- **`home-style.css`**: 홈페이지 전용 스타일 (히어로 섹션, 카테고리 쇼케이스, 통계)
- **`post-style.css`**: 포스트 페이지 전용 스타일 (모던 카드 레이아웃, 타이포그래피)
- **`book-style.css`**: 독서 페이지 전용 스타일 (독서 기록, 통계 섹션)

#### 🎯 스타일 적용 범위
- **전체 사이트**: `main.css`, `navigation-style.css`, `footer-style.css`
- **카테고리 페이지**: `category-style.css` (IT, Book 하위 모든 페이지)
- **특정 페이지**: `contact-style.css`, `tags-style.css`, `home-style.css`, `post-style.css`, `book-style.css`

#### 🔄 디자인 시스템
- **색상 테마**: 밝은 블루 그라데이션 (`#74b9ff` → `#0984e3`)
- **레이아웃**: 카드 기반 그리드 시스템 (최대 너비 1500px)
- **타이포그래피**: 일관된 폰트 크기 (제목 1.4rem, 날짜 1.1rem)
- **반응형**: 모바일 최적화 (768px 브레이크포인트)

#### 📝 수정 가이드
- **전체 색상 변경**: `navigation-style.css`, `contact-style.css`, `tags-style.css`, `home-style.css`, `post-style.css`, `book-style.css`에서 그라데이션 값 수정
- **카드 크기 조정**: `category-style.css`에서 `minmax` 값과 `max-width` 수정
- **홈페이지 레이아웃**: `home-style.css`에서 히어로 섹션, 카테고리 그리드, 통계 섹션 수정
- **포스트 페이지 스타일**: `post-style.css`에서 제목, 메타 정보, 태그, 콘텐츠 스타일 수정
- **독서 페이지 스타일**: `book-style.css`에서 독서 기록, 통계 섹션 수정
- **통계 섹션 통일**: `home-style.css`와 `book-style.css`에서 동일한 통계 스타일 사용
- **새 카테고리 추가**: `category-style.css` 스타일 자동 적용, HTML 구조만 맞추면 됨
- **폰트 크기 변경**: `category-style.css`, `tags-style.css`, `home-style.css`, `post-style.css`, `book-style.css`에서 `font-size` 값 수정

## 🏗️ 레이아웃 구조

### _layouts 폴더 구성
블로그의 페이지 레이아웃은 역할별로 분리되어 관리됩니다:

#### 📁 핵심 레이아웃 파일들
- **`default.html`**: 기본 레이아웃 (헤더, 푸터 포함)
- **`page.html`**: 일반 페이지 레이아웃 (카테고리 페이지 등)
- **`post.html`**: 포스트 페이지 레이아웃 (모던 디자인 적용)
- **`tagpage.html`**: 태그 개별 페이지 레이아웃
- **`portfolio_default.html`**, **`portfolio_page.html`**: 포트폴리오 관련 레이아웃

### _includes 폴더 구성
재사용 가능한 컴포넌트들로 구성됩니다:

#### 📁 핵심 Include 파일들
- **`head.html`**: HTML 헤드 섹션 (CSS, JS 로드)
- **`header.html`**: 네비게이션 헤더
- **`footer.html`**: 푸터 (Copyright)
- **`collecttags.html`**: 태그 수집 스크립트
- **`comments.html`**: Disqus 댓글 시스템

## 🎨 디자인 시스템

### 통계 섹션 디자인
- **흰 원 배경**: 통계 숫자를 흰 원 안에 파란색으로 표시
- **호버 효과**: 마우스 오버 시 확대 및 그림자 강화
- **일관성**: 홈페이지와 독서 페이지 통계 스타일 통일
- **반응형**: 모바일에서 적절한 크기로 자동 조정

### Favicon 및 로고
- **Favicon**: 다양한 크기 지원 (16x16, 32x32, 180x180)
- **로고**: 네비게이션 바 브랜드 로고 (`img/logo/logo.png`)
- **웹 매니페스트**: PWA 지원을 위한 `site.webmanifest`

## 📞 연락처
- **GitHub**: [@byoungsoo](https://github.com/byoungsoo)
- **Blog**: [byoungsoo.github.io](https://byoungsoo.github.io)

---

> 이 블로그는 개인 학습 목적으로 작성되었으며, 지속적으로 업데이트중입니다.