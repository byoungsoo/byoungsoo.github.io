---
layout: post
title: "Claude code 사용법"
author: "Bys"
category: ml
date: 2026-04-14 01:00:00
keywords: "claude"
tags: claude
published: true
---

이 페이지는 claude code 전체 문서를 읽으면서 개인적으로 필요한 내용만 요약 정리한 페이지다.  

# [Claude code]  
Claude Code는 터미널에서 실행되는 에이전트형 어시스턴트로 코딩에 특화되어 있지만, 커맨드 라인에서 할 수 있는 모든 작업을 도와줄 수 있다.
  - 문서 작성, 빌드 실행, 파일 검색, 주제 조사 등. 이 가이드에서는 핵심 아키텍처, 내장 기능, 효과적으로 사용하는 팁

## [Architecture](https://code.claude.com/docs/en/how-claude-code-works#the-agentic-loop)  
![claude](/assets/it/ml/claude/claude.png)


**에이전트 루프: 컨텍스트 수집 - 행동 실행 - 결과 검증**  
에이전트 루프는 두 가지 구성 요소로 구동된다: 추론하는 **모델**과 행동하는 **도구**. Claude Code는 Claude를 감싸는 에이전트 하네스 역할을 한다: 도구, 컨텍스트 관리, 실행 환경을 제공하여 언어 모델을 유능한 코딩 에이전트로 만든다.

Claude는 자율적으로 작업하지만 우리의 작업에 항상 반응한다. 언제든 중단하여 Claude의 방향을 바꾸거나, 추가 컨텍스트를 제공하거나, 다른 접근 방식을 시도하도록 요청할 수 있다.  

<br>

## 세션 작업

Claude Code는 작업하면서 대화를 로컬에 저장한다.  JSONL 파일에 기록되어 되감기, 재개, 포크가 가능하다.  
```bash
# claude code 대화 기록
~/.claude/projects/

# 세션 재개 (동일 session id)
claude --continue
claude --resume

# 원래 세션에 영향을 주지 않은 신규 세션 생성
claude --continue --fork-session
```

<br>

## 컨텍스트
Claude의 컨텍스트 윈도우에는 대화 이력, 파일 내용, 명령 출력, CLAUDE.md, 자동 메모리, 로드된 Skills, 시스템 지침이 모두 포함된다.

Claude Code는 한계에 가까워지면 자동으로 컨텍스트를 관리한다. 영구 규칙은 CLAUDE.md에 넣어야 한다.  

압축 중 보존할 내용을 제어하려면 CLAUDE.md에 "Compact Instructions" 섹션을 추가하거나 /compact를 포커스와 함께 실행할 수 있다. (예: `/compact focus on the API changes`).
```bash
# Visualize current context usage
$ /context

# Clear conversation history but keep a summary in context.
$ /compact
```

<br>

## 권한
Shift+Tab을 눌러 권한 모드를 순환 가능
- **기본**: Claude가 파일 편집과 셸 명령 전에 물어봄
- **편집 자동 승인**: Claude가 파일 편집과 mkdir, mv 같은 일반 파일시스템 명령을 물어보지 않고 실행, 다른 명령은 여전히 물어봄
- **계획 모드**: Claude가 읽기 전용 도구만 사용하여 실행 전 승인할 수 있는 계획 생성
- **자동 모드**: Claude가 백그라운드 안전 검사와 함께 모든 작업을 평가 (현재 리서치 프리뷰)

.claude/settings.json에서 특정 명령을 허용도 가능

<br>

## [확장](https://code.claude.com/docs/en/features-overview#match-features-to-your-goal)  
- **CLAUDE.md** — 매 세션마다 Claude가 보는 영구 컨텍스트를 추가. 200줄 이하로 유지하고, 커지면 참조 콘텐츠를 Skills로 옮기거나 .claude/rules/ 파일로 분리  
- **Skills** — 재사용 가능한 지식과 호출 가능한 워크플로를 추가
- **MCP** — Claude를 외부 서비스와 도구에 연결
- **Subagent** — 격리된 컨텍스트에서 자체 루프를 실행하고 요약을 반환. 세션 내에서 실행되고 결과를 메인 컨텍스트에 보고  
- **Agent teams** — 공유 작업과 peer-to-peer 메시징으로 여러 독립 세션을 조율. 
- **Hooks** — 루프 외부에서 결정론적 스크립트로 실행
- **Plugins** / **marketplaces** — 이러한 기능을 패키징하고 배포

Skills가 가장 유연한 확장이다. Skill은 지식, 워크플로, 지침을 담은 마크다운 파일이다. /deploy 같은 명령으로 호출하거나, 관련이 있을 때 Claude가 자동으로 로드할 수 있다. Skills는 현재 대화에서 실행하거나 서브에이전트를 통해 격리된 컨텍스트에서 실행할 수 있다.

| 기능 | 하는 일 | 사용 시점 | 예시 |
|---|---|---|---|
| CLAUDE.md | 매 대화마다 로드되는 영구 컨텍스트 | 프로젝트 규칙, "항상 X 하기" 규칙 | "npm 대신 pnpm 사용. 커밋 전 테스트 실행." |
| Skill | Claude가 사용할 수 있는 지침, 지식, 워크플로 | 재사용 가능한 콘텐츠, 참조 문서, 반복 작업 | /deploy로 배포 체크리스트 실행; 엔드포인트 패턴이 있는 API 문서 Skill |
| Subagent | 격리된 컨텍스트를 실행하고 요약된 결과를 반환 | 컨텍스트 격리, 병렬 작업, 전문 워커 | 많은 파일을 읽지만 핵심 발견만 반환하는 조사 작업 |
| Agent teams | 여러 독립 Claude Code 세션을 조율 | 병렬 조사, 새 기능 개발, 경쟁 가설로 디버깅 | 보안, 성능, 테스트를 동시에 검토하는 리뷰어 생성 |
| MCP | 외부 서비스에 연결 | 외부 데이터 또는 작업 | 데이터베이스 쿼리, Slack 게시, 브라우저 제어 |
| Hook | 이벤트에 실행되는 결정론적 스크립트 | 예측 가능한 자동화, LLM 미관여 | 파일 편집 후 매번 ESLint 실행 |


![claude-extension.png](/assets/it/ml/claude/claude-extension.png)

<br>

## [구조](https://code.claude.com/docs/en/claude-directory#ce-claude-json)
Claude Code가 CLAUDE.md, settings.json, hooks, skills, commands, subagents, rules, 자동 메모리를 읽는 위치.
- project/.claude
- ~/.claude

`project/.claude`는 git에 커밋되어 팀과 공유되어야 하고, `~/.claude` 파일은 모든 프로젝트에 적용되는 개인설정이다.  

**보이지 않는 것**   

| 파일 | 위치 | 용도 |
|---|---|---|
| managed-settings.json | 시스템 수준, OS에 따라 다름 | 오버라이드할 수 없는 기업 강제 설정. 서버 관리 설정 참조. |
| CLAUDE.local.md | 프로젝트 루트 | 이 프로젝트에 대한 개인 선호 설정, CLAUDE.md와 함께 로드됨. 수동으로 생성하고 .gitignore에 추가. |
| 설치된 플러그인 | ~/.claude/plugins/ | 복제된 마켓플레이스, 설치된 플러그인 버전, 플러그인별 데이터. claude plugin 명령으로 관리. 고아 버전은 플러그인 업데이트 또는 제거 후 7일 뒤 삭제. |

**파일 참조**   

| 파일 | 범위 | 커밋 | 하는 일 | 참조 |
|---|---|---|---|---|
| CLAUDE.md | Project and global | ✓ | 매 세션 로드되는 지침 | Memory |
| rules/*.md | Project and global | ✓ | 주제별 지침, 선택적으로 경로 제한 | Rules |
| settings.json | Project and global | ✓ | 권한, hooks, 환경 변수, 모델 기본값 | Settings |
| settings.local.json | Project only | | 개인 오버라이드, 자동 gitignore | Settings scopes |
| .mcp.json | Project only | ✓ | 팀 공유 MCP 서버 | MCP scopes |
| .worktreeinclude | Project only | ✓ | 새 worktree에 복사할 gitignore된 파일 | Worktrees |
| skills/<name>/SKILL.md | Project and global | ✓ | /name으로 호출하거나 자동 호출되는 재사용 가능한 프롬프트 | Skills |
| commands/*.md | Project and global | ✓ | 단일 파일 프롬프트; Skills와 같은 메커니즘 | Skills |
| output-styles/*.md | Project and global | ✓ | 커스텀 시스템 프롬프트 섹션 | Output styles |
| agents/*.md | Project and global | ✓ | 자체 프롬프트와 도구를 가진 서브에이전트 정의 | Subagents |
| agent-memory/<name>/ | Project and global | ✓ | 서브에이전트의 영구 메모리 | Persistent memory |
| ~/.claude.json | Global only | | 앱 상태, OAuth, UI 토글, 개인 MCP 서버 | Global config |
| projects/<project>/memory/ | Global only | | 자동 메모리: 세션 간 Claude의 자체 메모 | Auto memory |
| keybindings.json | Global only | | 커스텀 키보드 단축키 | Keybindings |

**로드 된 것 확인**
현재 세션에서 실제로 로드된 것을 확인하기 위한 명령어: 

| 명령 | 표시하는 것 |
|---|---|
| /context | 카테고리별 토큰 사용량: 시스템 프롬프트, 메모리 파일, Skills, MCP 도구, 메시지 |
| /memory | 로드된 CLAUDE.md와 rules 파일, 자동 메모리 항목 |
| /agents | 설정된 서브에이전트와 설정 |
| /hooks | 활성 hook 설정 |
| /mcp | 연결된 MCP 서버와 상태 |
| /skills | 프로젝트, 사용자, 플러그인 소스의 사용 가능한 Skills |
| /permissions | 현재 허용 및 거부 규칙 |
| /doctor | 설치 및 설정 진단 |

<br>

## 효율적인 사용법
#### Give Claude something to verify against  
Claude는 자체적으로 작업을 확인할 수 있을 때 더 잘 동작한다. 테스트 케이스를 포함하거나, 예상 UI의 스크린샷을 붙여넣거나, 원하는 출력을 정의하면 좋다.  
```
validateEmail을 구현해줘. 테스트 케이스: 'user@example.com' → true,
'invalid' → false, 'user@.com' → false. 테스트도 실행해줘.
```

#### Explore before implementing

복잡한 문제의 경우 조사와 코딩을 분리하세요. 계획 모드(Shift+Tab 두 번)를 사용하여 먼저 코드베이스를 분석하세요:

```
src/auth/를 읽고 세션 처리 방식을 이해해줘.
그다음 OAuth 지원 추가 계획을 만들어줘.
```

#### Delegate, don’t dictate(지시하지 말고 위임하기)  
컨텍스트와 방향을 주고, 세부 사항은 Claude를 믿으세요:
```
만료된 카드를 가진 사용자의 결제 흐름이 깨졌어.
관련 코드는 src/payments/에 있어. 조사하고 수정해줄 수 있어?
```

<br>

## 기타 커맨드
```bash
# 프로젝트용 CLAUDE.md 생성을 안내
$ /init 

# 커스텀 서브에이전트 설정을 도움
$ /agents 

# 설치 관련 일반적인 문제 진단
$ /doctor
```




---

## 📚 References

[1] **How Claude Code works**  
- https://code.claude.com/docs/en/how-claude-code-works

[2] **Extend Claude Code**  
- https://code.claude.com/docs/en/features-overview