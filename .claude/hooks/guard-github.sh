#!/bin/bash
# guard-github.sh: Claude Code PreToolUse 훅 (ADR-0012 결정 2)
#
# Bash·Monitor 명령이 실행되기 직전에 명령 전체를 읽고,
# GitHub 병합과 허용 목록 밖의 gh 명령을 막는다.
#   exit 0 → 통과 (이어서 권한 규칙과 auto mode가 판단)
#   exit 2 → 막음 (stderr가 막힌 이유로 Claude에게 전달됨)
# 이 스크립트가 시작하지 못하거나 0·2 밖의 코드로 끝나도
# settings.json의 "onFailure": "block" 때문에 명령은 막힌다.
# macOS 기본 /bin/bash(3.2)에서 돌도록 쓴다.

export LC_ALL=C

block() {
  echo "guard-github: $1. 병합과 GitHub 설정 변경은 정현만 합니다(ADR-0012). 다른 모양으로 다시 시도하지 말고 멈춘 뒤 정현에게 보고하세요." >&2
  exit 2
}

command -v jq >/dev/null 2>&1 || block "jq가 없어 명령을 검사할 수 없음"

cmd=$(jq -r '.tool_input.command // empty') || block "훅 입력을 읽지 못함"
[ -z "$cmd" ] && exit 0

# 따옴표·괄호·연결 기호·줄바꿈을 빈칸으로 바꾼다.
# sh -c '...' 안이나 $(...) 안에 숨은 명령도 같은 줄의 낱말로 보이게 하려는 것.
norm=" $(printf '%s' "$cmd" | tr -c 'A-Za-z0-9/_.:=@,+-' ' ') "

# 1) 병합은 어떤 모양이든 막는다: gh pr merge, REST, GraphQL, 자동 병합
if printf '%s' "$norm" | grep -Eiq '[[:space:]]pr[[:space:]]+merge[[:space:]]|pulls/[0-9]+/merge|mergePullRequest|enablePullRequestAutoMerge'; then
  block "GitHub 병합 요청"
fi

# 2) 토큰을 꺼내는 명령을 막는다 (꺼낸 토큰으로 gh 밖에서 GitHub를 부르지 못하게)
if printf '%s' "$norm" | grep -Eiq '[[:space:]]auth[[:space:]]+token[[:space:]]|show-token'; then
  block "GitHub 토큰 꺼내기"
fi

# 3) gh 하위 명령은 허용 목록만 통과시킨다
set -f   # 아래 set -- 에서 * 같은 글자가 파일 이름으로 바뀌지 않게
matches=$(printf '%s' "$norm" | grep -Eo '[[:space:]/]gh[[:space:]]+[^[:space:]]+([[:space:]]+[^[:space:]]+)?')

while IFS= read -r m; do
  [ -z "$m" ] && continue
  set -- $m            # $1 = gh, $2 = 무리(pr, issue …), $3 = 동작(view, create …)
  group=$2
  action=${3:-}

  if [ "$group" = "api" ]; then
    # gh api는 읽기만: 쓰는 메서드, 값 보내기(-f·-F·--input), GraphQL을 막는다
    if printf '%s' "$norm" | grep -Eiq '[[:space:]](-X|--method)[[:space:]=]*(POST|PUT|PATCH|DELETE)[[:space:]]|[[:space:]]-X(POST|PUT|PATCH|DELETE)[[:space:]]|[[:space:]](-f|-F|--field|--raw-field|--input)[[:space:]=]|[[:space:]]graphql[[:space:]]'; then
      block "gh api로 쓰기·GraphQL 요청"
    fi
    continue
  fi

  case "$group $action" in
    "issue view"|"issue list"|"issue comment") ;;
    "pr view"|"pr diff"|"pr checks"|"pr list"|"pr status") ;;
    "pr create"|"pr edit"|"pr comment"|"pr review"|"pr ready"|"pr checkout") ;;
    "run view"|"run list"|"run watch") ;;
    "repo view") ;;
    *) block "허용 목록 밖의 gh 명령 (gh $group $action)" ;;
  esac
done <<EOF
$matches
EOF

exit 0
