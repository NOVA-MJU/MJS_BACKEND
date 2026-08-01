# 공지 의미 분류 데이터셋 v1

이 디렉터리는 검색 순위가 아니라 공지 자체의 의미를 평가하기 위한 명세다. 기존
`notice_topic_catalog.json`을 단일 Topic 원천으로 사용하며 별도 Topic ID나 별칭 사전을 만들지 않는다.

## 산출물

- `schema.json`: 공지 의미 정답 레코드의 JSON Schema
- `labeling-guide.md`: Topic, Event Type, Audience, Campus, Deadline 라벨링 기준
- `notification-matching-policy.md`: 분류 결과와 사용자 구독을 결합하는 실행 정책
- `catalog-gap-analysis.md`: 현재 Catalog의 실제 데이터 커버리지와 보완 후보
- `completion-spec.md`: 구현·데이터·검증·운영 적용 상태를 정리한 완료 명세
- `global-program-taxonomy.md`: 실제 명지대 공지에서 확인한 해외·국제 프로그램 분류 기준
- `global-program-source-catalog-v1.jsonl`: 공식 원문 URL이 포함된 해외 프로그램 근거 35건
- `src/test/resources/semantic/notice-semantic-gold-v1.jsonl`: 실제 공지 기반 검수 표본
- `src/test/resources/semantic/alias-normalization-eval-v1.jsonl`: 별칭·기호 정규화 평가셋
- `src/test/resources/semantic/notification-matching-eval-v1.jsonl`: Topic 계층 및 중복 매칭 평가셋
- `src/test/resources/semantic/boundary-cases-v1.jsonl`: 오탐·경계 사례 평가셋
- `src/test/resources/semantic/global-program-cases-v1.jsonl`: 실제 해외 프로그램명·변형·오탐 50건
- `scripts/semantic/split_notice_semantic_dataset.py`: 그룹·시간 기반 누수 방지 분할
- `scripts/semantic/evaluate_notice_semantics.py`: 다중 라벨·계층·속성 평가

## 2026-08-01 운영 DB 읽기 전용 조사

- `unified_search_index`: 전체 4,296건, 활성 4,288건
- 활성 `NOTICE`: 2,671건
- 현 Catalog/본문 전체 규칙 기준 미분류: 1,535건(57.5%)
- 직접 Topic이 2개 이상인 문서: 533건(20.0%)
- 제목에서 Topic이 확인되는 문서: 449건(16.8%)
- 제목에는 없고 본문에서만 Topic이 잡히는 문서: 687건(25.7%)
- 대상 표현 추출 가능 후보: 852건(31.9%)
- 캠퍼스 표현 추출 가능 후보: 706건(26.4%)
- 기존 `valid_until` 존재: 1,413건(52.9%)
- 해외·국제 후보 제목: 151건. 반복 고유명과 구체 표현을 검수해 Catalog v3에 반영
- Catalog v3: Topic 34개, 별칭 122개

본문 전용 Topic 비율은 recall로 해석하면 안 된다. 예를 들어 일반 해외취업 설명회 본문에
`졸업생 및 지역청년`이 대상 중 하나로 등장했다는 이유만으로 공지 Topic을 `GRADUATION`으로 분류하는
오탐이 확인됐다. v1 데이터셋은 제목 중심 정답과 본문 근거가 필요한 예외를 분리한다.

## 라벨과 실행 정책의 경계

정답 데이터는 공지 의미만 기록한다. 사용자 구독 여부, 알림 활성화, 신규 여부, 중복 발송,
FCM 성공 여부는 공지 정답 라벨이 아니다. 이 값들은 `notification-matching-eval-v1.jsonl`에서
별도의 실행 정책 입력과 결과로 평가한다.

## 재현 순서

```powershell
python scripts/semantic/split_notice_semantic_dataset.py `
  src/test/resources/semantic/notice-semantic-gold-v1.jsonl `
  build/semantic-eval/split

python scripts/semantic/evaluate_notice_semantics.py `
  --gold src/test/resources/semantic/notice-semantic-gold-v1.jsonl `
  --predictions build/semantic-eval/predictions.jsonl `
  --catalog src/main/resources/semantic/notice_topic_catalog.json
```

운영 DB와 원본 공지는 읽기 전용으로만 사용하며, 표본의 `content`는 평가에 필요한 근거 구간만 보관한다.
