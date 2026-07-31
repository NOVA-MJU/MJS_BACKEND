# 공지 검색 의미 계층 설계 및 적용 기록

## 결론

검색 API와 알림 API는 합치지 않는다. 검색 순위와 알림 발송은 서로 다른 정책을 유지하고,
두 기능은 `Topic Catalog`, 별칭 정규화, 공지 분류 결과만 공유한다.

이번 작업은 다음 6단계로 구성한다.

1. 현행 검색·공지 DB·키워드 알림 구조 조사
2. 공통 Topic Catalog와 별칭/계층 설계
3. 검색 질의의 Topic·Event Type·Search Intent 분리
4. 공지 인덱싱 메타데이터와 기존 공지 재분류 경계 추가
5. 알림 Topic 자동완성 API 추가
6. 회귀 테스트, 운영 마이그레이션 및 품질 평가

## 1. 현행 구조 분석

- PostgreSQL 검색은 Komoran 토큰의 OR 후보군을 먼저 만들고 제목, 최신성, 인기도 등을 가중해 정렬한다.
- 짧은 일반어가 후보군을 과도하게 넓히기 때문에 `기숙사 신청`, `국장 신청`, `축제 일정` 같은 질의에서
  핵심 주제보다 `신청`, `일정`이 강하게 작용할 수 있었다.
- 기존 검색 요약은 별도 의미 요약이 아니라 동일 tsquery로 만든 `ts_headline` 발췌문이다.
  따라서 검색 후보가 잘못되면 요약도 함께 부정확해진다.
- `intent_lexicon.json`, `search_ranking_policy.json`은 현재 PostgreSQL 검색 실행 경로에 직접 연결되어 있지 않고,
  일부 가중치는 Java/SQL에 고정되어 있었다.
- 키워드 알림은 최대 5자 자유 키워드와 카테고리를 저장하고 신규 문서 제목 토큰을 prefix로 매칭한다.
  검색 인덱스의 순위 정책과는 별도 경로다.
- 운영 데이터 조사 시 통합 인덱스는 4,296건(활성 4,288건)이었고, 원본 `department_notice` 388건이
  동기화 대상에서 빠져 있었다. 검색 알고리즘과 별개로 데이터 수집 범위를 보완해야 한다.
- 공지 `valid_until`은 약 절반만 존재하고 본문의 가장 늦은 날짜를 잡는 사례가 있어,
  마감일은 분류 신뢰도와 함께 품질 점검이 필요하다.

## 2. 공통 Topic Catalog

`src/main/resources/semantic/notice_topic_catalog.json`을 단일 원천으로 둔다.

- Topic: 공지가 무엇에 관한 것인지
- Event Type: 신청, 모집, 변경, 연장, 결과 등 공지의 행동/상태
- Search Intent: 신청 방법, 신청 기간, 결과 조회, 대상 조건 등 사용자의 탐색 목적

`searchable`과 `subscribable`은 분리한다. 상위 Topic 선택은 모든 활성 하위 Topic을 포함하고,
하위 Topic 선택은 형제 Topic으로 확장하지 않는다. 하위 Topic 별칭이 매칭되면 문자열에 포함된 부모 별칭은
중복 제거하지만, 공지 분류 저장 시에는 조회/구독을 위해 부모 ID를 다시 함께 저장한다.

별칭 비교 전에 NFKC 정규화와 공백, `/`, `·`, `ㆍ`, `․`, `.`, `&`, `-`, 괄호 등의 구분기호 제거를 적용한다.
한 글자 별칭은 Catalog 검증 단계에서 거부한다.

## 3. 검색 실행 정책

등록된 Topic 질의는 다음 방식으로 처리한다.

- 핵심 Topic의 표준 검색어를 후보 검색의 필수 조건으로 사용한다.
- `신청`, `기간` 같은 수식어는 핵심 Topic을 대체하지 않고 순위 및 커버리지 신호로만 사용한다.
- 질문형 표현(`언제`, `언제까지야`, `알려줘`)은 후보를 넓히지 않는다.
- Event Type과 Search Intent는 내부 질의 계획에 별도로 기록한다.
- Catalog에 없는 일반 검색어는 기존 Komoran OR/AND 동작을 유지한다.

Learned sparse retrieval은 지금 즉시 기본 검색기로 넣지 않는다. 현재 문제의 큰 부분은 누락 데이터,
약어/기호 정규화, 지나치게 넓은 OR 후보군, 마감일 품질이다. 먼저 실제 질의와 정답 문서가 포함된 query bank를
축적하고 Recall@K, NDCG@K, 최신 유효 공지 비율을 비교한 뒤 2단계 후보 생성기로 실험하는 편이 안전하다.

## 4. 공지 분류 및 인덱스 필드

`unified_search_index`에 다음 내부 필드를 추가한다.

- `direct_topic_ids` JSONB: 공지에서 직접 판정한 가장 구체적인 Topic
- `topic_ids` JSONB: 직접 Topic과 부모 Topic을 모두 포함한 검색·알림 매칭용 값
- `event_type`
- `audiences` JSONB
- `campuses` JSONB
- `classification_version`
- `classification_source`
- `classification_confidence`

마감일은 기존 `valid_until`을 재사용한다. 미지정 대상/캠퍼스를 임의로 `ALL`로 채우지 않는다.
현재 1차 분류기는 보수적인 규칙 기반이며, 이후 모델 분류를 쓰더라도 `classification_source=MODEL`과 버전을
남길 수 있다.

`NoticeSemanticReclassificationService.reclassifyAll()`은 기존 인덱스를 200건 단위로 다시 분류하며
처리/분류/미분류 수와 실패 ID를 반환한다. Catalog 변경 후 재분류하고, 필요 시 기존 검색 벡터 재생성 작업을
이어 실행한다.

## 5. 자동완성 API

신규 API만 추가한다.

```http
GET /api/v1/alarm-topics/autocomplete?query=졸업&limit=8
```

게시글이 아니라 구독 가능한 표준 Topic을 반환한다. 사용자가 이후 구독할 값은 입력 문자열이 아닌 `topicId`다.
기존 `/api/v1/search/**`, `/api/v1/keyword-alarms/**`, `/api/v1/notifications/**`의 요청·응답 DTO는 변경하지 않았다.

## 6. 운영 적용 및 후속 작업

권장 적용 순서는 스키마 추가 → 애플리케이션 배포 → 기존 공지 재분류 → 검색 벡터 재생성 → query bank 평가다.
새 컬럼은 기존 행이 있는 운영 DB에서도 추가될 수 있도록 nullable로 매핑하고, 초기화 SQL에서 JSON 기본값을
설정하고 NULL을 빈 배열로 보정한다.

아직 구현하지 않은 알림 전용 작업은 다음과 같다.

- `topicId` 기반 사용자 구독 저장 모델과 생성/해지 API
- 신규 공지의 Topic과 구독 Topic 계층을 비교하는 매칭
- 명시적 대상/캠퍼스 필터
- 중복 발송 방지 및 활성화 정책과의 연결
- 재분류 실행을 위한 운영자 배치/관리 API
- 실패/미분류 결과의 영속 로그와 규칙 변경 전후 품질 대시보드

이 후속 작업도 기존 자유 키워드 알림 API를 교체하지 않고 병행 도입한 뒤 마이그레이션해야 한다.
