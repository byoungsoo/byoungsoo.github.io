---
slug: claude-code
author: Bys
categories:
- ml
category: ml
date: '2026-04-14 01:00:00'
draft: false
keywords: claude
tags:
- claude
title: Claude code 사용법
description: "Claude Code 사용법 정리 - 터미널에서 실행되는 에이전트형 AI 어시스턴트의 핵심 아키텍처, 내장 기능, 효과적 활용 팁"
---


### 이 페이지는 claude code 전체 문서를 읽으면서 개인적으로 필요한 내용만 요약 정리한 페이지다.  


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
Claude의 컨텍스트 윈도우에는 대화 이력, 파일 내용, 명령 출력, CLAUDE.md, 자동 메모리, 로드된 Skills, 시스템 지침이 모두 포함된다. Subagent가 자체 별도 context window에서 조사를 처리한 것에 대해서는 컨텍스트에 남지 않는다. 요약과 작은 메타데이터 정보만 돌아온다.  
Claude Code는 한계에 가까워지면 자동으로 컨텍스트를 관리한다. 영구 규칙은 CLAUDE.md에 넣어야 한다.  

압축 중 보존할 내용을 제어하려면 CLAUDE.md에 "Compact Instructions" 섹션을 추가하거나 /compact를 포커스와 함께 실행할 수 있다. (예: `/compact focus on the API changes`).
```bash
# Visualize current context usage
$ /context

# Clear conversation history but keep a summary in context.
$ /compact
```

/compact 명령어를 통해 긴 세션이 압축되면, claude code는 context window 를 맞추기 위해 대화 이력을 요약한다.  

#### 메모리 시스템
CLAUDE.md 파일로 Claude에게 영구 지침(persistent instructions)을 제공하고, 자동 메모리(auto memory)로 Claude가 학습 내용을 자동으로 축적하도록 한다.  

| | CLAUDE.md 파일 | 자동 메모리 |
|---|---|---|
| 작성자 | You | Claude |
| 포함 내용 | Instructions and rules | Learnings and patterns |
| 범위 | Project, user, or org | Per working tree |
| 로드 대상 | Every session | Every session (first 200 lines or 25KB) |
| 용도 | Coding standards, workflows, project architecture | Build commands, debugging insights, preferences Claude discovers |


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

## CLAUDE.md 파일
CLAUDE.md 파일은 무엇인가 다시 설명이 필요한 내용들을 적어두는 공간으로 활용한다: 
  - Claude가 같은 실수를 두 번째 할 때
  - 코드 리뷰에서 Claude가 이 코드베이스에 대해 알았어야 할 것을 잡았을 때
  - 지난 세션에서 입력한 것과 같은 수정이나 설명을 채팅에 다시 입력할 때
  - 새 팀원이 생산적이 되려면 같은 컨텍스트가 필요할 때

CLAUDE.md 파일은 여러 위치에 있을 수 있으며, 각각 다른 범위를 가진다. 지침을 작성할 때는 "코드를 적절히 Format 하시오" 보다는 "2칸 들여쓰기를 사용하시오" 와 같이 검증할 수 있을 만큼 구체적인 지침을 작성해야한다. CLAUDE.md 파일은 @path/to/import 구문을 사용하여 추가 파일을 임포트할 수 있다.  

`foo/bar/` 에서 Claude code 는 `foo/bar/CLAUDE.md`, `foo/CLAUDE.md` 및 모든 `CLAUDE.local.md` 파일의 지시사항을 로드한다. `CLAUDE.local.md` 파일은 `CLAUDE.md` 뒤에 append 되며 모든 파일은 overriding 되지 않고, context에 추가 된다.  

CLAUDE.md 파일의 블록 수준 HTML 주석(<!-- 주석 -->)은 Cluade의 컨텍스트에 주입되기전에 삭제되므로 Human간 정보처리를 위해 사용할 수 있다.  

--add-dir 플래그는 Claude에게 주 작업 디렉토리 외부의 추가 디렉토리에 대한 접근 권한을 부여한다. 기본적으로 이러한 추가 디렉토리의 CLAUDE.md 파일은 로드되지 않으며, 추가디렉토리의 메모리 파일을 로드하기 위해서는 `CLAUDE_CODE_ADDITIONAL_DIRECTORIES_CLAUDE_MD=1` 환경변수를 설정해야 한다.  

```bash
CLAUDE_CODE_ADDITIONAL_DIRECTORIES_CLAUDE_MD=1 claude --add-dir ../shared-config
```
이렇게 하면 CLAUDE.md, .claude/CLAUDE.md, .claude/rules/*.md, CLAUDE.local.md 파일들을 추가디렉토리로부터 로드한다.  


<br>

## 권한

| 모드 | 확인 없이 실행되는 것 | 적합한 용도 |
|---|---|---|
| `default` | Reads only | 시작 단계, 민감한 작업 |
| `acceptEdits` | Reads, file edits, and common filesystem commands (mkdir, touch, mv, cp, etc.) | 검토 중인 코드 반복 작업 |
| `plan` | Reads only | 변경하기 전에 코드베이스 탐색 |
| `auto` | Everything, with background safety checks | 긴 작업, 프롬프트 피로 감소 |
| `dontAsk` | Only pre-approved tools | Locked-down CI 와 scripts |
| `bypassPermissions` | Everything except protected paths | 격리된 컨테이너 및 VM 전용 |

Shift+Tab을 눌러 권한 모드를 순환 가능. 세션 중: Shift+Tab을 눌러 default -> acceptEdits -> plan을 순환할 수 있따. 모든 모드가 기본 순환에 포함되는 것은 아니다:
  - auto: 계정이 auto 모드 요구사항을 충족할 때 나타남
  - bypassPermissions: --permission-mode bypassPermissions, --dangerously-skip-permissions, 또는 --allow-dangerously-skip-permissions로 시작한 후 나타남; --allow- 변형은 활성화하지 않고 순환에 모드를 추가함
  - dontAsk: 순환에 나타나지 않음; --permission-mode dontAsk로 설정

```bash
claude --permission-mode acceptEdits
claude --permission-mode plan
claude --permission-mode dontAsk
claude --permission-mode bypassPermissions
```

#### acceptEdits 모드로 파일 편집 자동 승인  
acceptEdits 모드는 Claude가 프롬프트 없이 작업 디렉토리에서 파일을 생성하고 편집할 수 있게 한다. 파일 편집 외에도 acceptEdits 모드는 일반 파일시스템 Bash 명령어를 자동 승인합니다: mkdir, touch, rm, rmdir, mv, cp, sed.

#### plan 모드로 편집 전 분석
Plan 모드는 Claude에게 변경 사항을 만들지 않고 조사하고 제안하도록 지시한다. Claude는 파일을 읽고, 탐색을 위해 셸 명령어를 실행하고, 계획을 작성하지만, 소스를 편집하지 않는다. 

#### auto 모드로 프롬프트 제거하기
Auto 모드는 Claude가 권한 프롬프트 없이 실행할 수 있게 한다. Auto 모드는 research preview 단계이다. 프롬프트를 줄이지만 안전을 보장하지는 않는다. 일반적인 방향을 신뢰하는 작업에 사용하고, 민감한 작업에 대한 검토의 대체물로 사용하지 않아야 한다.  

#### dontAsk 모드로 사전 승인된 도구만 허용
dontAsk 모드는 prompt가 필요한 모든 도구 호출을 자동 거부한다. permissions.allow 규칙과 일치하는 작업 및 읽기 전용 Bash 명령어만 실행할 수 있다.
```json
{
  "permissions": {
    "allow": [
      "Bash(npm run *)",
      "Bash(git commit *)",
      "Bash(git * main)",
      "Bash(* --version)",
      "Bash(* --help *)"
    ],
    "deny": [
      "Bash(git push *)"
    ]
  }
}
```

#### bypassPermissions 모드로 모든 검사 건너뛰기
bypassPermissions 모드는 권한 프롬프트와 안전 검사를 비활성화하여 도구 호출이 즉시 실행된다. 보호된 경로에 대한 쓰기만 여전히 프롬프트가 표시된다. 이 모드는 인터넷 접근이 없는 컨테이너, VM, devcontainer와 같은 격리된 환경에서만 사용해야한다. Claude Code가 호스트 시스템을 손상시킬 수 없는 곳에서만 사용한다.  

#### 보호된 경로
소수의 경로에 대한 쓰기는 모든 모드에서 절대 자동 승인되지 않습니다. 이는 저장소 상태와 Claude 자체 설정의 실수로 인한 손상을 방지합니다. default, acceptEdits, plan, bypassPermissions에서는 이러한 쓰기가 프롬프트를
표시하고; auto에서는 분류기로 라우팅되며; dontAsk에서는 거부됩니다.

**보호된 디렉토리:**
- .git
- .vscode
- .idea
- .husky
- .claude (단, Claude가 일상적으로 콘텐츠를 생성하는 .claude/commands, .claude/agents, .claude/skills, .claude/worktrees는 제외)

**보호된 파일:**  
- .gitconfig, .gitmodules
- .bashrc, .bash_profile, .zshrc, .zprofile, .profile
- .ripgreprc
- .mcp.json, .claude.json


<br>

## Rules
대규모 프로젝트에서는 .cluade/rules/ 디렉토리를 사용하여 지시사항을 여러 파일로 구성할 수 있다. 이렇게 하면 지시사항(instructions)이 모듈화되어 팀이 유지 관리하기 쉬워집니다. 규칙은 특정 파일 경로에 대한 범위를 지정할 수도 있어 필요할 때만 컨텍스트에 로드되므로 노이즈를 줄이고 컨텍스트 공간을 절역가능하다. 

```
your-project/
├── .claude/
│   ├── CLAUDE.md           # Main project instructions
│   └── rules/
│       ├── code-style.md   # Code style guidelines
│       ├── testing.md      # Testing conventions
│       └── security.md     # Security requirements
```

```
---
paths:
  - "src/**/*.{ts,tsx}"
  - "lib/**/*.ts"
  - "tests/**/*.test.ts"
---

# API Development Rules

- All API endpoints must include input validation
- Use the standard error response format
- Include OpenAPI documentation comments
```

- `paths` 필드가 없는 규칙은 .claude/CLAUDE.md와 동일한 우선순위로 실행 시 로드된다.  
- .claude/rules/ 디렉토리는 심볼릭 링크를 지원하므로, shared rules 를 여러 프로젝트에서 링크할 수 있다.  
- ~/.claude/rules/의 개인 규칙은 machine의 모든 프로젝트에 적용된다.  



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