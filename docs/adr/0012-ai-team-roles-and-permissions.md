# ADR-0012: AI 팀의 경계는 지침 하나에 맡기지 않고 여러 겹으로 막으며, 병합은 정현만 한다

- 날짜: 2026-11-23
- 상태: 제안됨
- 작성: 초안은 멘토 AI, 검토·승인은 정현

> **쉽게 말하면**
> 1. 팀은 넷이다. 정현(팀장, 백엔드), 멘토(claude.ai, 자문), 프론트 AI(화면), 리뷰어 AI(읽기 전용 리뷰). AI끼리는 직접 말하지 않고 정현과 GitHub을 거친다.
> 2. AI가 지킬 규칙을 `CLAUDE.md`(부탁)에만 두지 않는다. Claude Code의 권한 규칙, 우리가 쓴 훅, 운영체제의 샌드박스, GitHub의 CI가 한 겹씩 막는다.
> 3. 병합은 정현만 한다. AI는 정현의 GitHub 로그인으로 움직여서 main 보호 규칙이 AI를 구별하지 못한다. 그래서 정현의 맥에서 병합 명령을 여러 겹으로 막는다.
> 4. 프론트 AI는 `frontend/` 밖을 고칠 수 없고, 리뷰어는 아무것도 고칠 수 없다.

## 맥락

- 저장소 하나에 `backend/`, `frontend/`, `contract/`, `docs/`가 있다. 프론트 AI가 `frontend/` 밖을 고칠 위험은 지침·권한·CI 세 겹으로 막고, 세부는 환경 구축 채팅에서 정하기로 했다(ADR-0004 결정 1).
- AI의 모든 GitHub 동작은 정현의 `gh` 로그인(토큰 범위 `repo`, `workflow`)으로 실행된다. GitHub에는 AI의 push·PR·병합이 정현의 것과 똑같이 보인다.
  - main 보호(Ruleset "main 보호": PR 필수, 승인 0, 필수 검사, 예외 없음)는 "PR을 거쳤는가, 검사를 통과했는가"만 본다. 검사를 통과한 PR을 AI가 병합하는 것은 막지 못한다.
  - M2 뒤로는 main 병합이 곧 운영 배포다(ADR-0006).
- 11/3 실습(`herdr-inflearn-practice`)에서 확인한 것
  - auto mode의 Claude Code는 `git push`, `gh pr create`를 묻지 않고 실행했다. `ask` 규칙을 넣자 auto mode에서도 물었다.
  - 새 세션 + 수정 도구 차단으로 띄운 리뷰어가 [검증됨]/[의견] 형식으로 리뷰를 잘 했다. 리뷰 코멘트는 정현 이름으로 달린다.
  - 에이전트가 이슈와 저장소 상태가 다를 때 묻고, 확인하지 못한 완료 조건을 체크하지 않은 것이 좋은 동작이었다.
- Claude Code 공식 문서(2.1.318 기준)
  - 권한 규칙은 모델이 아니라 Claude Code가 강제한다. `CLAUDE.md`의 지시는 Claude가 하려는 일을 바꿀 뿐, 허용되는 일을 바꾸지 않는다.
  - Bash 규칙은 명령의 글자 모양을 맞춰 볼 뿐 그 프로그램 둘레의 보안 경계가 아니다. 같은 일을 다른 모양(다른 하위 명령, `sh -c`, 절대 경로)으로 하면 걸리지 않는다. 명령 전체를 우리 로직으로 보려면 PreToolUse 훅을, 파일·네트워크를 명령의 모양과 상관없이 막으려면 샌드박스를 쓰라고 한다.
  - Edit·Read 규칙은 Claude의 파일 도구와 Claude Code가 알아보는 일부 셸 명령에만 걸린다. node 스크립트처럼 스스로 파일을 여는 하위 프로세스는 샌드박스가 막는다. Edit deny 규칙의 경로는 샌드박스의 쓰기 금지 목록에도 자동으로 더해진다.
  - 공유 설정(`.claude/settings.json`)은 Claude를 시작한 폴더에서 읽는다.
- 저장소는 공개다. 프론트 AI가 설치하는 npm 패키지는 남이 만든 코드이고, 설치할 때 스크립트가 실행될 수 있다.

## 결정 1: 역할과 권한

| | 정현 | 멘토 | 프론트 AI | 리뷰어 AI |
| --- | --- | --- | --- | --- |
| 어디서 | IntelliJ, 터미널 | claude.ai 프로젝트 | herdr fe 탭, `damda-fe` worktree | herdr review 탭, `damda-review` worktree |
| 모델 | | Opus | Sonnet | Opus |
| 고칠 수 있는 곳 | 전부 | 없음(초안만 씀) | `frontend/` 안 | 없음 |
| GitHub 쓰기 | 전부 | 정현이 승인한 이슈·코멘트만 | 자기 `fe/` 브랜치 push, PR 만들기·고치기, 코멘트. 모두 정현이 허락 창에서 승인 | PR 코멘트. 정현이 허락 창에서 승인 |
| 병합 | 정현만 | 하지 않음 | 막힘 | 막힘 |

- 지시는 이슈 번호로, 결과는 PR로, 멘토에게는 요약으로 전한다. AI끼리 직접 말하지 않는다.
- 멘토 칸은 11/3 실습에서 확인한 범위 그대로다.

## 결정 2: 병합은 정현만, 네 겹으로 막는다

```
AI가 병합을 시도한다
 ① 지침     CLAUDE.md "병합하지 않는다"                          ← 부탁
 ② 훅       guard-github.sh가 명령 전체를 읽고 병합 모양과
            허용 목록 밖의 gh 명령을 막는다 (exit 2)              ← 권한 규칙보다 먼저 돈다
 ③ 권한     deny: gh pr merge, gh auth / ask: gh api
 ④ 분류기   auto mode가 요청 밖의 행동을 막는다                  ← Anthropic의 장치
```

- 훅은 `.claude/hooks/guard-github.sh`이고 Bash·Monitor 명령마다 돈다. 하는 일:
  - 병합으로 보이는 모양을 막는다: `pr merge`(`sh -c` 안, 절대 경로, 변수로 부른 gh 포함), REST의 `pulls/<번호>/merge`, GraphQL의 `mergePullRequest`, 자동 병합
  - 토큰을 꺼내는 명령(`gh auth token`, `--show-token`)을 막는다
  - gh 하위 명령은 허용 목록만 통과시킨다: `issue view·list·comment`, `pr view·diff·checks·list·status·create·edit·comment·review·ready·checkout`, `run view·list·watch`, `repo view`, 읽기만 하는 `api`. 그 밖(`auth`, `secret`, `ruleset`, `repo edit`, `workflow`, `release`, `alias`, `extension` 등)은 막는다.
- 훅이 시작하지 못하거나 오류로 끝나도 명령이 막히도록 `"onFailure": "block"`을 건다.
- 훅 파일과 설정 파일은 `.claude/` 아래라 AI가 고칠 수 없다(결정 3의 Edit deny와 샌드박스의 보호 경로).
- 이 저장소에서는 권한 검사를 끄는 `bypassPermissions` 모드를 쓰지 못하게 한다(`disableBypassPermissionsMode`).

선택지:

| | A. deny 한 줄 | B. 네 겹 (선택) | C. AI 전용 GitHub 계정 |
| --- | --- | --- | --- |
| 방법 | `gh pr merge` deny | 지침 + 훅 + 권한 + 분류기 | AI가 봇 계정으로 push·PR. main 규칙에 승인 1, 정현만 승인·병합 |
| `gh api`로 병합 | 통과 | 훅이 막음 | GitHub이 막음 |
| 감춘 모양(변수로 gh 부르기 등) | 통과 | 일부 통과 가능 | GitHub이 막음 |
| 운영할 것 | 없음 | 훅 스크립트 하나 | 계정, 토큰, 2단계 인증, 정현 자신의 PR 처리 방법 |

이유:
1. 공식 문서가 Bash 규칙을 경계로 보지 말라고 한다. 04의 3번 목록은 A를 "유일한 문"이라고 적었지만, `gh api`로 같은 병합을 하면 지나간다.
2. 훅은 명령의 글자 모양이 아니라 명령 전체를 우리 로직으로 본다. 흔한 모양과 일부러 감춘 몇 모양을 함께 잡고, 막는 이유를 Claude에게 돌려준다.
3. C가 가장 확실하지만, 운영할 계정이 늘고 승인 규칙이 정현 자신의 PR까지 막는다. M1을 미룰 만큼의 일이다.

버린 선택지:
- **A**: 경계가 되지 못한다(이유 1).
- **C (v2 후보)**: AI의 커밋과 PR이 작성자로 구별되어 CI도 브랜치 이름 대신 작성자로 프론트 AI를 알아볼 수 있다. 위험이 실제로 보이거나 AI가 늘어나면 다시 본다.

감수하는 것:
- 훅도 완전한 경계는 아니다. 일부러 감춘 명령 모양은 지나갈 수 있다. 그때는 ask 규칙의 허락 창, auto mode 분류기, 정현의 PR 확인이 남는다.
- 명령의 따옴표 안 글자에 `pr merge` 같은 낱말이 있어도 막힌다. PR 본문은 `--body-file`로 올려 이 일을 피한다.

## 결정 3: 프론트 AI의 경계, 세 겹

| 겹 | 장치 | 막는 것 |
| --- | --- | --- |
| 1. 지침 | `frontend/CLAUDE.md`, 루트 `CLAUDE.md` | 우회 금지, 계약·생성 타입 손대지 않기, 계획 7항목 |
| 2. 권한·샌드박스 | 공유 `.claude/settings.json` | 아래 목록 |
| 3. CI | `.github/workflows/ci.yml` (다음 PR) | `fe/`로 시작하는 브랜치의 PR이 `frontend/` 밖을 바꾸면 실패 |

2겹째의 내용:
- Edit deny: `backend/`, `contract/`, `docs/`, `.github/`, `.claude/`, 루트 파일(`CLAUDE.md`, `README.md`, `.gitignore`, `.nvmrc`), `frontend/CLAUDE.md`. 이 경로는 샌드박스의 쓰기 금지에도 자동으로 더해진다.
- 생성 타입 `frontend/src/api/schema.d.ts`는 deny가 아니라 ask다. deny로 두면 샌드박스 쓰기 금지에 더해져 `npm run gen:api`도 막힌다. 손으로 고친 것은 CI가 다시 만들어 보고 다르면 실패시킨다(다음 PR).
- 셸 명령은 샌드박스 안에서 돈다.
  - 쓰기: worktree 안(위 금지 경로 제외)과 npm 캐시(`~/.npm`)만
  - 읽기 금지: `~/.ssh`, `~/.config/gh`, `~/.aws`, `~/.docker`, `~/.kube`, `~/.netrc`. 환경 변수 `GH_TOKEN`·`GITHUB_TOKEN`·`NPM_TOKEN`은 샌드박스 안에서 지운다.
  - 네트워크: npm 저장소(`registry.npmjs.org`)만 미리 허용하고, GitHub 주소(`github.com`, `*.github.com`, `*.githubusercontent.com`)는 막는다.
  - 샌드박스가 뜨지 않으면 Claude Code가 시작하지 않고(`failIfUnavailable`), 막힌 명령을 샌드박스 밖에서 다시 시도하지 못한다(`allowUnsandboxedCommands: false`).
- GitHub에 닿아야 하는 `gh`, `git push`, `git fetch`, `git pull`만 샌드박스 밖에서 돈다. 대신 결정 2의 훅·권한 규칙을 거치고, 쓰는 명령은 정현이 허락 창에서 승인한다. 이 명령들은 다른 명령과 묶으면 샌드박스 안에 남으므로 단독으로 실행한다(루트 `CLAUDE.md`).
- 샌드박스 안과 밖은 임시 폴더(`$TMPDIR`)가 서로 다르다. 그래서 `gh`에 넘길 PR·코멘트 본문 파일은 worktree 안의 `.ai-tmp/`에 쓰고, 이 폴더는 `.gitignore`에 넣는다.
- 프론트 AI는 worktree 맨 위(`damda-fe`)에서 띄운다. 공유 설정은 Claude를 시작한 폴더에서 읽기 때문에, `frontend/`에서 띄우면 2겹째가 통째로 빠진다. `frontend/CLAUDE.md`는 Claude가 그 폴더의 파일을 읽을 때 붙으므로, 루트 `CLAUDE.md`에 "먼저 읽는다"를 적는다.

선택지:
- **규칙을 둘 곳**: 역할별 설정 파일을 띄울 때마다 `--settings`로 넘기기 / 공유 설정 하나(선택)
- **프론트 AI를 띄우는 폴더**: `frontend/`(작업 폴더 자체가 경계) / worktree 맨 위(선택)

이유:
1. 이 저장소에서 Claude Code로 파일을 고치는 것은 프론트 AI뿐이다(정현은 IntelliJ, 리뷰어는 읽기 전용). 공유 설정 하나면 띄울 때 챙길 것이 없다.
2. 공급망 공격을 생각했다. 2025년 9월의 npm 웜 Shai-Hulud는 패키지 설치 스크립트로 개발자 PC의 토큰을 모아 GitHub API로 공개 저장소에 올렸고(CISA 경보), 같은 계열의 공격이 그 뒤로도 이어졌다. 샌드박스는 명령의 모양과 상관없이 운영체제가 막는다. GitHub 주소를 막아 두면 샌드박스 안에서 토큰을 찾아내더라도 GitHub로 내보낼 수 없다.

감수하는 것:
- 정현이 나중에 Claude Code로 문서나 백엔드를 고치려 해도 막힌다. deny는 다른 설정 파일의 allow로 풀 수 없다. 그럴 일이 생기면 이 ADR을 다시 본다.
- 새 최상위 폴더(예: `docker/`)가 생기면 Edit deny 목록에 더해야 한다. 빠뜨려도 3겹째 CI가 잡는다.
- 샌드박스 밖에서 도는 명령은 편의이지 경계가 아니다(문서). 그 명령들은 훅·권한 규칙·허락 창에 기댄다.
- 샌드박스가 막는 일(새 도메인, 포트 열기)을 프론트 AI가 해야 하면, 우회하지 않고 보고한다. 필요하면 설정을 PR로 넓힌다.

## 결정 4: `fe/` 규칙과 계약을 깨는 변경

- 프론트 AI의 브랜치는 `fe/<이슈번호>-<설명>`이다. PR과 커밋이 모두 정현 계정으로 올라가 작성자로는 구별할 수 없어서, CI는 브랜치 이름으로 프론트 AI의 PR을 알아본다.
- **계약을 깨는 백엔드 PR (M3 전)**: 쌓인 PR로 한다. ADR-0006 결정 1의 "같은 브랜치에서 프론트도 함께 고친 뒤 병합"을 `fe/` 경계를 지키며 하는 방법이다.

```
main ◀── 백엔드 PR (feat/12-…) ◀── 프론트 PR (fe/12-…, base = feat/12-…)

① 정현: 백엔드 브랜치에 계약을 깨는 변경과 계약 파일을 올린다
② 프론트 AI: 그 브랜치에서 fe/ 브랜치를 따서 프론트를 고치고, 백엔드 브랜치를 base로 PR
③ CI의 fe/ 경계 검사 통과 → 정현이 프론트 PR을 백엔드 브랜치에 병합
④ 정현: 프론트까지 담긴 백엔드 PR을 main에 병합
```

- 사이트 공개 뒤(M3~)는 ADR-0006대로 추가 → 프론트 이전 → 제거의 세 단계라 쌓인 PR이 필요 없다.

버린 선택지: **프론트 AI가 백엔드 브랜치에 직접 push**. 그 PR은 `fe/` 브랜치가 아니라 경계 검사가 걸리지 않는다.

## 결정 5: 리뷰어와 리뷰 흐름

- 정의 파일: `.claude/agents/fe-reviewer.md`(다음 PR), `be-reviewer.md`(다음 큰 백엔드 PR 전). 도구 목록에서 Edit·Write를 빼서 고칠 수단을 아예 주지 않는다. review 탭에서 `claude --agent fe-reviewer`로 띄워 세션 전체가 리뷰어가 된다.
- 모델은 Opus다. 작성자(Sonnet)와 다른 모델로 보고, 같은 대화에서 자기 작업을 리뷰시키지 않는다. PR마다 새로 시작한다(`/clear`).
- 대상: 프론트 AI의 PR은 전부(정현이 React 코드를 읽지 않는다). 백엔드는 큰 PR(계약 변경, 보안 설정, 새 기능의 로직)만. Opus는 Sonnet보다 한도를 몇 배 쓰므로 범위를 좁힌다. Pro가 모자라면 기획 3의 8번 순서대로 줄인다.
- 흐름 (인계 문서 1의 13번)
  1. 정현이 review 탭에 "PR #N 리뷰"라고 요청한다. 처음 1~2주는 손으로 하고, 자동 요청은 그 뒤에 정한다.
  2. 리뷰어가 번호 붙은 지적을 PR 코멘트로 단다. [검증됨]은 실행한 명령과 결과를 함께, [의견]은 이유와 함께. 맨 위에 `🤖 리뷰어 AI`를 붙인다(정현 이름으로 달리기 때문).
  3. 정현이 판단 코멘트를 단다("수락 1, 3 / 거절 2: 이유").
  4. 작성자가 수락한 것만 같은 브랜치에 반영한다.
  5. 리뷰어가 수락한 것만 다시 확인한다(최대 2바퀴).
  6. 정현이 브라우저와 네트워크 탭으로 확인하고 병합한다.
- [검증됨] 지적의 자동 반영은 1~2주 손으로 운영하며 틀린 지적의 비율을 본 뒤 정한다.

## 결정 6: 계획 승인

- 새 이슈마다 프론트 AI는 코드를 쓰기 전에 계획 7항목(`frontend/CLAUDE.md`)을 내고 정현이 승인한다. 리뷰 반영이나 작은 수정은 계획 없이 한다.
- 정현의 판단법: 2·3번(호출하는 API, 처리하는 오류)을 계약과 대조하고, 4번(고치는 파일)으로 범위를 확인한다. 7번(계약과 맞지 않는 부분)에 무엇이 있으면 백엔드 결정이 필요하다는 신호다.

## 결과

- 이 PR(M1): 이 ADR, 루트 `CLAUDE.md`, `frontend/CLAUDE.md`, `.claude/settings.json`, `.claude/hooks/guard-github.sh`, `.nvmrc`(Node 24, ADR-0003), `.gitignore`의 `.ai-tmp/`. `damda-fe` worktree에서 망가뜨려 보기로 각 겹이 실제로 막는지 확인한다.
- 다음 PR(첫 프론트 PR 병합 전): CI의 `fe/` 경계 검사, 프론트 job(계약 파일이 바뀔 때도 돎, 생성 타입이 최신인지 확인), 필수 검사 추가, `fe-reviewer.md`, `damda-review` worktree.
- 그 일이 올 때: `be-reviewer.md`, frontend-design 플러그인 설치와 점검 기록, 리뷰 요청 자동화.

## 다시 검토할 때

- AI가 늘거나, 훅을 지나가는 병합 시도가 실제로 보이면 결정 2의 C(AI 전용 계정)를 다시 본다.
- 손으로 리뷰를 1~2주 운영한 뒤 리뷰 요청·[검증됨] 반영 자동화를 정한다.
- Claude Code의 권한·샌드박스·훅 동작이 바뀌면 망가뜨려 보기를 다시 돌린다.

## 참고

- Claude Code 권한 규칙: https://code.claude.com/docs/en/permissions
- Claude Code 샌드박스: https://code.claude.com/docs/en/sandboxing
- Claude Code 설정 파일과 우선순위: https://code.claude.com/docs/en/settings
- Claude Code 훅: https://code.claude.com/docs/en/hooks
- Claude Code 권한 모드(auto mode, 보호 경로): https://code.claude.com/docs/en/permission-modes
- Claude Code 서브에이전트: https://code.claude.com/docs/en/sub-agents
- Claude Code 에이전트 팀: https://code.claude.com/docs/en/agent-teams
- GitHub REST API, 풀 리퀘스트 병합: https://docs.github.com/en/rest/pulls/pulls#merge-a-pull-request
- CISA, npm 생태계 공급망 공격(Shai-Hulud) 경보 (2025-09-23): https://www.cisa.gov/news-events/alerts/2025/09/23/widespread-supply-chain-compromise-impacting-npm-ecosystem

## 정현의 판단 (대화에서 옮김)

> 정현의 말을 대화에서 옮겼다. 읽기 쉽게 조금 다듬었지만 뜻은 바꾸지 않았고, 정현이 말하지 않은 이유는 넣지 않았다.

- **ADR로 쓸지** (11/10): 처음 추천(ADR 없이 `CLAUDE.md`만)에 "우선 문제될 부분이 없으면 좋습니다." 멘토가 검토에서 킥오프 ⑤-5와 환경 문서가 "→ ADR"로 적어 둔 것을 찾아 ADR을 다시 제안하자 "a에 문제될 게 없다면 좋네요."
- **결정 1~6** (11/23): 멘토가 개념(부탁과 막음의 차이, 서브에이전트와 에이전트 팀, worktree)과 정할 것 아홉 가지를 추천과 이유로 설명한 뒤 "추천대로 빈틈없이 부탁드릴게요."
- **멘토가 고친 것** (11/23): 환경 구축 기록(04의 3번 목록)이 `gh pr merge` deny를 "AI 병합을 막는 유일한 문"이라고 적었다. 공식 문서를 확인해 이 말을 바로잡고 결정 2의 네 겹으로 바꿨다.
