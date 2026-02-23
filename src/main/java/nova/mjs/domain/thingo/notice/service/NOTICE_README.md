# Notice 크롤링 비즈니스 로직 가이드

이 문서는 `NoticeCrawlingService`의 핵심 동작과 운영 시 주의사항을 정리한 문서입니다.

---

## 1) 크롤링 흐름 요약

1. `fetchAllNotices()`
   - 학교 공지 + 학과 공지를 각각 그룹 단위로 순회합니다.
2. `crawlSingleCategory(category, path)`
   - 카테고리별로 목록 페이지를 순회하며 row를 처리합니다.
3. `processRow(...)`
   - 날짜/제목 정규화
   - 오래된 공지 중단 판단
   - 링크(enc) 생성
   - 중복/재게시 교체 판단
   - 신규 공지 본문 크롤링
   - 저장 버퍼(`toSave`)에 적재
4. `saveNotices(...)`
   - 크롤링 완료 후 `saveAll`로 일괄 저장합니다.
5. `cleanupRecentNotices(...)`
   - 최근 범위 내에서 목록에 없는 공지 정리(cleanup)를 수행합니다.

---

## 2) 설계 의도

- **네트워크 I/O와 트랜잭션 분리**
  - 크롤링(HTTP/파싱)은 트랜잭션 밖에서 수행합니다.
  - DB 반영(save/cleanup)만 짧은 별도 트랜잭션으로 처리합니다.
- **실패 격리**
  - 카테고리 하나가 실패해도 다른 카테고리는 계속 진행합니다.
- **불필요한 I/O 최소화**
  - 중복 판단을 먼저 수행하고, 신규/의미 있는 공지만 상세 페이지를 요청합니다.

---

## 3) 중복/교체/정리 규칙

### A. 완전 중복 차단
- 조건: 동일 `category` + 동일 `link(enc)`
- 처리: 저장하지 않고 스킵

### B. 동일 제목 재게시 교체
- 조건: 최근 1개월 내 동일 title 존재 + link(enc) 상이
- 처리: 기존 row 삭제 후 신규 row 저장 대상으로 교체

### C. 최근 데이터 정리(cleanup)
- 조건: 최근 범위(1개월) 내에서 크롤링 목록에 없는 링크
- 처리: DB에서 제거

> cleanup은 운영 안전성을 위해 최근 범위로만 제한합니다.

---

## 4) `rule` 카테고리 예외 정책 (중요)

`rule` 타입은 운영자가 직접 입력/보정하는 데이터가 존재할 수 있어,
**cleanup 대상에서 제외**합니다.

- 적용 지점: `cleanupRecentNotices(...)`
- 동작: `category == "rule"`이면 cleanup을 수행하지 않고 skip
- 목적: 크롤링 시 수동 입력 데이터가 자동 삭제되는 문제 방지

즉, `rule` 카테고리는 크롤링 저장은 수행하되,
"최근 목록에 없음" 기준 자동 삭제는 하지 않습니다.

---

## 5) 운영 체크리스트

- `DUPLICATE_WINDOW_MONTHS` 조정 시
  - 재게시 교체 범위와 cleanup 범위가 함께 바뀌므로 영향도 확인 필요
- 신규 카테고리 추가 시
  - `NoticeUrlRegistry`에 등록
  - 수동 데이터 보호가 필요하면 cleanup 제외 목록에 카테고리 추가
- 크롤링 품질 이슈 발생 시
  - 날짜/제목 파싱 실패 row 로그 확인
  - 목록 셀렉터 변경 여부 확인

---

## 6) 관련 클래스

- `NoticeCrawlingService`
- `NoticeCrawlHelper`
- `NoticeUrlRegistry`
- `NoticeRepository`

