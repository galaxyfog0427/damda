# ADR-0003: 기반 스택은 board에서 가져가고, Java 25 · Spring Boot 4.1 · MySQL 8.4 · Node 24로 시작한다
 
- 날짜: 2026-11-05
- 상태: 승인 (2026-11-05, 정현)
- 작성: 초안은 멘토 AI, 검토·승인은 정현
> **쉽게 말하면**: board에서 써 본 도구는 그대로 쓰고, 버전만 지금 기준으로 올린다. Java는 가장 최신 장기 지원판(25), Spring Boot는 지금 가장 최신 안정판(4.1), MySQL은 board와 같은 8.4, 프론트를 빌드할 때 쓰는 Node는 24다. 가상 스레드는 처음엔 끄고, 부하 테스트에서 켜고 끈 결과를 비교해 "우리 시스템에서 효과가 있는지"를 숫자로 확인한다.
 
## 맥락
 
- board에서 Spring Boot 4, JPA, Flyway, Testcontainers, Docker, GitHub Actions를 이미 검증했다. 이번 프로젝트의 학습 시간은 새 영역(배포, 관측성, SPA 인증, 부하 테스트)에 써야 한다.
- 개발은 11/9에 시작하고, v3까지 1월 이후로 이어진다. 지원 기간(2027년)까지 보안 패치를 받을 수 있는 버전이어야 한다.
- 킥오프에서 Java 21 이상으로 올려 가상 스레드를 켜고 끄며 비교하는 실험을 하기로 했다.
## 결정
 
### 1. board에서 그대로 가져가는 것
 
Java(언어), Spring MVC, Spring Security, JPA + Spring Data JPA + QueryDSL(OpenFeign 포크), Flyway(`ddl-auto=validate`), Testcontainers, JUnit 5 + Mockito + MockMvc, Gradle(Groovy DSL), Docker + Compose, GitHub Actions(CI)
 
- 다시 토론하지 않는다. 바꿀 이유가 생기면 그때 새 ADR을 쓴다.
- board에서 바뀌는 것: 화면은 Thymeleaf 대신 React SPA(기획 2의 3번), Redis는 쓸 곳을 다시 정한다(8번), Kafka는 v2(13번), 로컬 S3 대역(LocalStack)은 9번에서 다시 정한다.
- 언어는 Java를 유지한다. 지원 목록 조사에서 도전·주력 층 공고의 절반 넘게 Kotlin을 필수로 요구하면 다시 판단한다(progress-summary 13번의 조건 그대로).
### 2. 버전
 
| 항목 | 결정 | 버린 선택지와 이유 |
| --- | --- | --- |
| Java | **25 (LTS)** | 21: 가상 스레드가 `synchronized` 안에서 운반 스레드에 고정(pinning)되는 문제가 남아 있다(JDK 24의 JEP 491에서 해결). 17: board 버전이지만 가상 스레드가 없다 |
| Spring Boot | **4.1.x 최신 패치**로 시작 | 4.0: 무료 지원이 2026년 12월 말에 끝난다. 4.2: 11월 정식 출시 예정이라 개발 시작 시점에는 아직 RC다. 새 마이너 버전의 .0은 피한다 |
| MySQL | **8.4 (LTS)** | 9.7 LTS: 더 새롭고 지원도 더 길지만, 도구(Flyway, Testcontainers, Hibernate)와의 조합이 board만큼 검증되지 않았고, 새 기능(새 옵티마이저, JSON 기능)이 필요하지 않다 |
| Node (프론트 빌드용) | **24 (LTS)** | 26: 10/28에 LTS가 된 지 얼마 안 됐고, 오래된 API를 지워 간접 의존성이 깨질 수 있다. 24도 2028년 4월까지 지원된다 |
 
### 3. 가상 스레드
 
- v1은 기본값(끔)으로 시작한다. 드롭 부하 테스트에서 켜고 끈 결과를 비교한다(실험 계획은 기획 2의 12번).
- 예측: 드롭은 DB 커넥션 풀이 한계라 가상 스레드를 켜도 처리량이 크게 늘지 않는다. 차이가 나는 곳은 느린 외부 호출(메일, v3 챗봇)일 것이다. 예측이 틀려도 그대로 기록한다.
## 결과
 
- **Spring Boot 업그레이드**: 4.2는 첫 패치(4.2.1)가 나온 뒤, v1이 끝나고 올린다. 운영 중에 마이너 버전을 올려 보는 연습이 된다. 업그레이드 PR은 Dependabot이 열게 한다. 4.1의 무료 지원은 2027년 중반까지라, 올리지 않아도 v3까지는 지원 안에 있다.
- **MySQL 업그레이드**: 8.4에서 9.7(다음 LTS)로 가는 업그레이드는 나중의 운영 연습 후보다. board에서 8.0.43 → 8.4.13으로 올린 경험의 연장이다. LTS를 건너뛰는 업그레이드는 지원되지 않으니, 9.7 다음 LTS로 바로 갈 수는 없다.
- **첫날 확인할 것**
  - 프로젝트는 Spring Initializr로 만든다. Boot 4는 기능별 스타터가 필요해서 board에서 여러 번 막혔다(Flyway, 세션, Kafka).
  - Java 25로 빌드가 되는지 확인한다. QueryDSL 같은 애너테이션 처리기도 포함하고, Gradle 래퍼가 Java 25를 지원하는지도 본다.
  - Docker 기반 이미지(temurin 25)와 CI의 Java 버전을 25로 맞춘다.
- **기반 이미지와 CI**: 백엔드는 temurin 25, 프론트는 Node 24로 고정한다. "latest"는 쓰지 않는다. 다시 빌드할 때 메이저 버전이 몰래 바뀌는 것을 막기 위해서다.
## 참고
 
- Spring Boot 지원 기간: https://www.danvega.dev/blog/spring-boot-end-of-life
- Spring Boot 4.2 일정: https://www.herodevs.com/blog-posts/spring-boot-versions-eol-dates-and-latest-releases-april-2026
- JEP 491과 JDK 25: https://infoq.com/articles/virtual-threads-after-jdk24
- MySQL 지원 기간: https://endoflife.date/mysql
- Node.js 24·26: https://fireup.pro/news/nodejs-26-lts-whats-new-upgrade
- Java 버전 사용 현황(Perforce 2025): https://www.i-programmer.info/news/80-java/17904-high-level-of-ai-adoption-for-java-development.html
## 정현의 판단 (대화에서 옮김)
 
> 정현의 말을 대화에서 옮겼다. 읽기 쉽게 조금 다듬었지만 뜻은 바꾸지 않았고, 정현이 말하지 않은 이유는 넣지 않았다.
 
- **질문** (11/5): "자바 25, 즉 가상 스레드 테스트를 해서 면접에 이점이 있을까요? 실무에서 도움이 될까요? 실무에서는 아직 25 혹은 21도 많이 안 쓰지 않을까 싶어서입니다."
- **질문** (11/5): "가상 스레드를 도입하면 시스템의 이점이 큰가요?"
- **판단** (11/5): 멘토의 설명을 듣고 25를 유지하는 것으로 승인했다. 설명의 요지는 세 가지였다.
  - 실무 사용은 17이 가장 많고 21이 다음이다.
  - 버전 간 코드 차이는 거의 없다.
  - 우리 드롭은 커넥션 풀이 한계라 가상 스레드의 효과가 작을 것이고, 실험의 가치는 효과가 있는 곳과 없는 곳을 측정으로 설명하는 데 있다.