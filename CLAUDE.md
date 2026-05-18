# Project
이 프로젝트는 내가 공부하고 경험한 것들에 대해 기록하는 공간입니다. 회사일을 하면서 또는 책을 보거나 문서를 보고 개인적으로 공부하면서 배운 내용들을 기록하고 필요한 내용들에 대해 요약 정리한 기록들이 존재한다. 이 프로젝트는 현재 Hugo 를 통해 빌드되어 github.io 에 포스트 하고 있다. 내가 일을 하면서 가장 소중히 여기는 자산중 하나다.  

# Architecture
이 프로젝트는 Hugo를 통해 빌드한다. 컨텐츠는 `content` 하위 폴더에 존재하고, 이중에서도 
This project is build using Hugo. Contents are located under `content`, and static files are located under `static`. 

# Project conventions
## Rules
- Don't delete files under the `content/` without my permission.
- When user request to commit and push to git repositories, then you must use commit.sh script. it needs to add files for apply separately. (never use --no-verify options for git commit)
- All of subagents run background as a default. Only user request foreground they run foreground.