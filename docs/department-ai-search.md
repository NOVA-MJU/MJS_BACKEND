# 학과 AI 검색 및 공식 홈페이지 수집

## 호환성 원칙

- 기존 프론트가 사용하는 `/api/v1/search/**`, `/api/v1/departments/info`, 일정·공지 API의 요청과 응답은 변경하지 않는다.
- 학과 AI 검색은 공개 엔드포인트 `GET /api/v1/departments/ai-search`로 제공한다.
- 기존 `/api/v1/ai/departments/search`도 내부 호환을 위해 유지하지만, 운영 nginx의 `/api/v1/ai/*` 라우팅과 충돌하므로 외부 연동에서는 사용하지 않는다.
- 응답은 `PROFILE_CARD`, `TEXT_ANSWER`, `COURSE_LIST`, `EVENT_LIST`, `SOURCE_LIST` 블록으로 유형화한다.

## 데이터 흐름

1. `department_directory.psv`의 7개 단과대와 46개 학부·학과·전공을 `department` 테이블에 멱등 동기화한다.
2. 공식 홈페이지 안에서 학과/전공 상세, 교육목표, 진로, 교과과정 링크를 찾는다.
3. 추출 본문과 각 원문 URL, 검증 시각, 수집 상태를 `department_profiles`에 저장한다.
4. 사용자 검색 시에는 외부 홈페이지를 호출하지 않고 DB와 학사안내 인덱스만 조회한다.
5. 이벤트 검색은 기존 학과 공지와 학과 일정 데이터를 합쳐 최신순으로 반환한다.

## 카테고리

- `BASIC`: 소개, 교학팀 전화, 인스타그램, 공식 홈페이지
- `FOUNDATION`: 적용 학번·학과 범위를 포함한 학문기초교양 자료
- `MAJOR`: 교육목표, 전공 교과과정, 졸업 후 진로
- `EVENT`: 학과·단과대 공지와 학과 일정
- `AUTO`: 질문의 핵심 의도를 위 네 종류 중 하나로 분류

## 운영 동기화

```yaml
mju:
  department-profile:
    sync-on-startup: true
```

전체 동기화는 홈페이지 수집을 수행하므로 검색 요청 경로와 분리한다. 공개 HTTP 동기화 API는 두지 않으며, 운영자가 위 설정을 명시적으로 활성화한 배포에서만 한 번 실행한 뒤 다시 끈다. 공식 사이트가 제공하지 않는 항목은 억지로 생성하지 않고 `PARTIAL`로, 접속 자체가 불가능하면 `FAILED`로 저장한다.

## 검색 요청 예시

```http
GET /api/v1/departments/ai-search?query=데이터사이언스전공%20소개&category=AUTO
```

학과 약칭과 과거·관용 명칭도 같은 학과로 정규화한다. 예를 들어 `응소`,
`응용소프트웨어학과`, `응용소프트웨어전공`은 모두 응용소프트웨어전공의
기본 소개로 연결된다.

기계 판독용 전체 예시는 `/openapi/department-ai-search.json`에서 확인한다.
