## 우리는 이런 서비스를 만들고 있어요
> **학교 정보가 흩어져 있어, 필요한 공지를 제때 못 본 적 있으신가요?**  
> 공지·학사일정·학과 소식·커뮤니티를 앱/사이트마다 따로 찾아다니고 있진 않나요?  
> 학생 입장에서 “지금 필요한 정보”를 한 번에 빠르게 얻고 싶지 않으셨나요?  


### 🎓 **Thingo**는 **명지대학교 학생이 실제로 사용하는 통합 캠퍼스 플랫폼**이에요

**Thingo는 학생의 실제 사용 흐름을 기준으로 설계된 서비스**입니다.  
학교생활에서 자주 쓰는 기능(공지, 일정, 검색, 커뮤니티, 학과별 정보)을 하나로 연결하고, 흩어진 데이터를 검색 가능한 구조로 바꿔 **정보 접근 속도**를 높였습니다.

우리는 “실사용자”라는 표현보다 더 분명하게, **학생을 사용자로 두고 직접 운영하는 팀**임을 중요하게 생각합니다.  
학생이 매일 믿고 쓸 수 있도록 보안·안정성·검색 품질을 함께 개선합니다.

> 🔥 **Thingo 하나로, 학생이 필요한 캠퍼스 정보를 더 빠르고 정확하게 찾을 수 있어요.**  
> 정보 탐색부터 참여(커뮤니티), 그리고 학과별 정보 확인까지 끊기지 않는 경험을 만드는 것이 우리의 방향입니다.

---

## 🧭 우리가 말하는 서비스 방향

### 1) DDD 지향 아키텍처
- `member`, `community`, `notice`, `department`, `mentorship` 등 **도메인 중심 패키징**으로 경계를 나눴습니다.
- 각 도메인에 `controller / service / repository / exception / dto`를 배치해 책임이 명확합니다.
- 변경이 생겨도 해당 도메인 내부에서 해결되도록 구조화해 유지보수 비용을 줄였습니다.

### 2) 인터페이스 중심 + CQRS(읽기/쓰기 분리)
- `CommandService` / `QueryService`를 분리해 쓰기와 조회의 책임을 나눴습니다.
- `Service interface + Impl` 구조를 기본으로 가져가서 구현 교체/테스트/확장에 유리합니다.
- 외부 모듈이 구현체가 아닌 인터페이스에 의존하도록 설계해 결합도를 낮췄습니다.

### 3) AOP/공통관심사 분리로 운영 품질 강화
- AOP로 서비스 계층 실행시간을 공통 로깅해 병목 지점을 빠르게 찾을 수 있게 했습니다.
- 인증, 예외 처리, 응답 포맷, 로깅(MDC Trace ID) 같은 횡단 관심사를 중앙화했습니다.
- 기능 코드에 운영 코드를 덕지덕지 넣지 않고, 아키텍처 레벨에서 관리를 분리했습니다.

### 4) 왜 우리와 함께해야 할까요?
- 우리는 “실사용자”라는 추상적인 표현 대신, **학생을 사용자로 두고 직접 운영하는 팀**입니다.
- 그래서 기능 추가보다 먼저, 보안/안정성/검색 품질/운영성을 함께 봅니다.
- 백엔드 코드를 쓰는 데서 끝나지 않고, 실제 학생 경험 개선까지 책임집니다.

---

## 👥 Contributors

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/hyunbin1">
        <img src="https://github.com/hyunbin1.png" width="100px" alt="hyunbin1"/><br/>
        <sub><b>hyunbin1</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/sink0324">
        <img src="https://github.com/sink0324.png" width="100px" alt="sink0324"/><br/>
        <sub><b>sink0324</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/Namtaera">
        <img src="https://github.com/Namtaera.png" width="100px" alt="Namtaera"/><br/>
        <sub><b>Namtaera</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/miink0">
        <img src="https://github.com/miink0.png" width="100px" alt="miink0"/><br/>
        <sub><b>miink0</b></sub>
      </a>
    </td>
  </tr>
</table>

---

## 🔍 주요 기능 안내
> 현재 1–4번까지는 개발이 완료되었고, 5번은 아직 기획 중입니다.

### 1. 🏠 학생 생활 포털 기능
- **메인페이지**  
  Thingo의 모든 기능에 빠르게 접근할 수 있는 포털로, 사용자의 주요 활동과 공지를 한눈에 확인할 수 있도록 구성되어 있습니다.
- **자유게시판(커뮤니티)**  
  학생 간 소통 공간으로, 질문·정보 공유·일상 이야기 등 다양한 주제를 자유롭게 나눌 수 있습니다.
- **댓글 및 좋아요**  
  게시글과 댓글에 의견을 표현해 참여와 소통을 활성화합니다.
- **확성기**  
  여러 사용자에게 중요한 소식을 빠르게 전달할 수 있는 공지 전용 커뮤니케이션 기능입니다.
- **마이페이지**  
  나의 활동 기록, 북마크, 설정 등을 관리할 수 있는 개인 맞춤형 공간입니다.

### 2. 📅 학교 생활 정보 기능
- **오늘의 학사 일정**  
  오늘 진행되는 학사 일정을 요약 제공해 필요한 정보를 놓치지 않도록 도와드립니다.
- **학사일정**  
  연간 학사 일정을 월별로 정리해 쉽게 검색·확인할 수 있습니다.
- **학과 일정**  
  각 학과 학생회가 제공하는 일정·행사·회의 정보를 종합 안내합니다.
- **공지사항** *(일반 / 학사 / 장학·학자금 / 진로·취업·창업 / 학생활동 / 학칙개정)*  
  교내 부서·학과·기관 공지사항을 자동 수집해 카테고리별로 깔끔하게 정리합니다.
- **학교 실시간 날씨(서대문구)**  
  캠퍼스 위치 지역의 실시간 날씨를 제공해 외출이나 수업 준비에 도움을 줍니다.
- **식단 안내**  
  교내 식당의 메뉴와 운영 시간을 매일 업데이트합니다.

### 3. 🎓 학과별 정보·운영 기능
- **학과별 정보성 서비스**  
  전공 팁, 교수님 스타일, 수강 신청 전략 등을 학생회에서 직접 운영합니다.
- **학과별 서비스 어드민**  
  학회 구성원이 일정·정보 게시를 관리할 수 있는 전용 관리자 기능입니다.

### 4. 🔎 검색 및 탐색 기능
- **통합 검색**  
  공지사항, 게시글, 학사일정, 뉴스 등 다양한 정보를 키워드 기반으로 빠르게 탐색합니다.
- **검색 결과 페이지**  
  콘텐츠 유형별로 결과를 분류해 원하는 정보를 더욱 쉽게 찾을 수 있습니다.
- **실시간 검색 순위**  
  현재 가장 많이 검색되는 키워드를 집계해 캠퍼스 관심사를 파악할 수 있습니다.

### 5. 💼 진로 및 커리어 연계 서비스 *(기획 중)*
- **취업정보**  
  채용·공모전·진로 자료를 모아 제공해 체계적인 취업 준비를 돕습니다.
- **졸업 멘토관**  
  취업 후기, 실무 경험, 인터뷰 팁 등 졸업생의 생생한 조언을 제공합니다.


## 메인 페이지(미리보기)

<div align="center">
  <img src="https://github.com/user-attachments/assets/25b56314-cb27-45b3-9d2f-a620877cd996" width="358" alt="Thingo 메인 화면 프리뷰"/>
</div>


## 🗓️ 앞으로의 계획
- **멘토관**  
  일자리센터 멘토 등록 후 일정에 맞춘 예약 시스템 구축(후기·가격 포함).
- **취업 가이드**  
  일자리센터의 성공 사례 데이터를 기반으로 13개 직무 가이드 공개 예정.


## 🤝 Thingo가 드리고 싶은 약속
> 저희는 **학생의 목소리를 가장 우선**으로 생각합니다.  
> 작고 불편한 점도 놓치지 않고, 꾸준히 개선해 나가겠습니다.  
> 여러분의 **학교생활이 조금 더 편하고 의미 있게** 바뀔 수 있도록 함께하겠습니다.


## 🏗️ 백엔드 아키텍처 & 개발 강점

### 🧩 1) 실제 코드 구조로 설명하는 DDD 설계
- 프로젝트는 도메인(학생/커뮤니티/공지/학과/멘토링) 단위로 분리되어 있습니다.
- 도메인 내부에서도 `controller → service → repository` 흐름과 DTO/Exception을 분리해 SRP를 지켰습니다.
- 공통 영역(`util`, `config`)은 인증·응답·예외·인프라 역할만 담당해 도메인 오염을 줄였습니다.

### 🧱 2) 인터페이스 기반 설계 + CQRS 스타일
- `MemberQueryService`, `MemberCommandService`처럼 조회/명령 인터페이스를 분리해 책임을 명확히 했습니다.
- 관리자/멘토링/학과 기능도 `*Service` 인터페이스와 `*Impl` 구현체 패턴을 일관되게 사용합니다.
- 이 구조 덕분에 기능 확장 시 기존 코드 침범을 줄이고, 테스트 포인트도 명확해집니다.

### 🔐 3) 보안과 신뢰성
- Spring Security + JWT 기반 무상태 인증 구조를 적용했습니다.
- Access/Refresh 토큰, 재발급, 로그아웃, 블랙리스트를 분리해 토큰 수명주기를 관리합니다.
- `GlobalExceptionHandler`와 표준 응답 포맷으로 실패 케이스를 일관되게 처리합니다.

### 🔎 4) 검색 아키텍처(Elasticsearch + 이벤트 기반 인덱싱)
- 통합 검색은 `SearchIntentResolver`로 의도를 해석하고, `SearchRankingPolicy`로 랭킹 규칙을 조합합니다.
- JPA EntityListener/이벤트 발행 후 `@TransactionalEventListener(AFTER_COMMIT)`에서 인덱스 반영해 정합성을 높였습니다.
- 도메인 인덱스 + 통합 인덱스 fan-out 방식으로 검색 확장성과 운영성을 함께 확보했습니다.

### ⚡ 5) 성능·메모리·운영 관점
- Redis ZSet/List로 실시간 검색어를 집계하고 TTL 기반 정리 스케줄러로 메모리 증가를 제어합니다.
- AOP 실행시간 로깅 + MDC Trace ID로 요청 단위 추적과 병목 분석이 가능합니다.
- 즉, 기능 개발과 운영 관측성을 동시에 가져가는 백엔드 구조입니다.

### ✅ 현재 백엔드에서 동작 중인 핵심 기능(학생 체감 기준)
- 회원가입/로그인/JWT 재발급/비밀번호 복구/프로필 관리
- 커뮤니티 게시글·댓글·대댓글·좋아요
- 학교 공지/학사일정/학과 일정/학생회 공지 조회 및 관리
- 통합 검색 + 자동완성 + 실시간 검색어 Top10
- 주간 식단/날씨/캘린더/뉴스/방송국 데이터 연동 및 조회
- 멘토링 프로그램/신청/승인·거절/내 신청 조회

## 🚀 핵심 기술 스택

### ⚙️ Language & Framework  
<table>
  <tr>
    <td><img src="https://img.shields.io/badge/Java-17-007396?style=flat&logo=java&logoColor=white" height="20"/></td>
    <td><b>Java 17</b><br/>백엔드 전반을 구성하는 주력 언어</td>
  </tr>
  <tr>
    <td><img src="https://img.shields.io/badge/Spring_Boot-3.2.1-6DB33F?style=flat&logo=spring-boot&logoColor=white" height="20"/></td>
    <td><b>Spring Boot</b><br/>REST API 및 비즈니스 로직 구현</td>
  </tr>
  <tr>
    <td><img src="https://img.shields.io/badge/Spring_Security-Auth-6DB33F?style=flat&logo=spring-security&logoColor=white" height="20"/></td>
    <td><b>Spring Security</b><br/>JWT 기반 인증·인가 처리</td>
  </tr>
  <tr>
    <td><img src="https://img.shields.io/badge/JPA-Hibernate-6DB33F?style=flat&logo=hibernate&logoColor=white" height="20"/></td>
    <td><b>JPA (Hibernate)</b><br/>ORM 기반 객체–관계 매핑</td>
  </tr>
</table>

### 🗄️ Database  
<table>
  <tr>
    <td><img src="https://img.shields.io/badge/PostgreSQL-17-4169E1?style=flat&logo=postgresql&logoColor=white" height="20"/></td>
    <td><b>PostgreSQL 17</b><br/>관계형 데이터베이스, RDS와 연동</td>
  </tr>
</table>

### ☁️ Cloud & Infra (AWS)  
<table>
  <tr>
    <td><img src="https://img.shields.io/badge/EC2-t3.medium-FF9900?style=flat&logo=amazon-ec2&logoColor=white" height="20"/></td>
    <td><b>AWS EC2</b><br/>Spring 서버가 배포된 인스턴스 (30 GiB EBS)</td>
  </tr>
  <tr>
    <td><img src="https://img.shields.io/badge/S3-Static_Storage-569A31?style=flat&logo=amazons3&logoColor=white" height="20"/></td>
    <td><b>AWS S3</b><br/>이미지 및 정적 자산 저장소</td>
  </tr>
  <tr>
    <td><img src="https://img.shields.io/badge/CloudFront-CDN-232F3E?style=flat&logo=amazonaws&logoColor=white" height="20"/></td>
    <td><b>AWS CloudFront</b><br/>정적 파일 전송 최적화를 위한 CDN</td>
  </tr>
</table>

### 🐳 DevOps & Deployment  
<table>
  <tr>
    <td><img src="https://img.shields.io/badge/Docker-Container-2496ED?style=flat&logo=docker&logoColor=white" height="20"/></td>
    <td><b>Docker</b><br/>백엔드 환경 컨테이너화 및 배포 자동화</td>
  </tr>
  <tr>
    <td><img src="https://img.shields.io/badge/GitHub_Actions-CI/CD-2088FF?style=flat&logo=githubactions&logoColor=white" height="20"/></td>
    <td><b>GitHub Actions</b><br/>CI/CD 자동화 파이프라인 구축</td>
  </tr>
</table>

### 🧰 Tools & Collaboration  
<table>
  <tr>
    <td><img src="https://img.shields.io/badge/IntelliJ_IDEA-IDE-000000?style=flat&logo=intellijidea&logoColor=white" height="20"/></td>
    <td><b>IntelliJ IDEA</b><br/>주 개발 IDE</td>
  </tr>
  <tr>
    <td><img src="https://img.shields.io/badge/Postman-API_Test-FF6C37?style=flat&logo=postman&logoColor=white" height="20"/></td>
    <td><b>Postman</b><br/>REST API 테스트 및 문서화</td>
  </tr>
  <tr>
    <td><img src="https://img.shields.io/badge/Discord-Community-5865F2?style=flat&logo=discord&logoColor=white" height="20"/></td>
    <td><b>Discord</b><br/>실시간 협업 및 커뮤니케이션</td>
  </tr>
</table>
