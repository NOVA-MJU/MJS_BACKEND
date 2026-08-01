# 공지 Topic 검색·알림 완료 명세

기준일: 2026-08-01
Catalog: v3 / Topic 34개 / 별칭 122개

## 사용자 동작

1. 사용자가 기존 키워드 입력창에 `졸업`, `해외`, `WELL` 등을 입력한다.
2. `GET /api/v1/alarm-topics/autocomplete`가 게시글이 아닌 표준 Topic 후보를 반환한다.
3. 사용자가 후보를 선택하면 앱은 기존 키워드 등록 요청에 선택한 `topicId`를 함께 보낸다.
4. 서버는 표시용 `keyword`와 표준 `topicId`를 함께 저장한다.
5. 신규 글은 제목 중심 규칙으로 Topic·Event Type·대상·캠퍼스를 분류한다.
6. 상위 Topic 구독은 분류 결과의 부모 Topic까지 확장해 모든 하위 Topic을 포함한다.
7. 같은 회원의 여러 구독이 한 공지에 걸려도 한 번만 발송한다.

자동완성을 선택하지 않은 요청은 `topicId`를 생략할 수 있다. 이 경우 기존 제목 접두 키워드 매칭을
그대로 사용하므로 사용자가 넓게 입력한 범위를 서버가 임의로 좁히지 않는다.

## API 계약

자동완성:

```http
GET /api/v1/alarm-topics/autocomplete?query=졸업&limit=8
```

Topic 선택 구독:

```json
{
  "keyword": "졸업",
  "topicId": "GRADUATION",
  "categories": ["NOTICE"]
}
```

`topicId`는 선택 필드다. 응답에도 저장된 `topicId`를 반환하며, 자유 키워드 구독이면 `null`이다.
존재하지 않거나 구독 불가능한 값은 `ALARM_TOPIC_INVALID`로 거부한다.

## 구현 완료 범위

- 검색과 알림이 공유하는 단일 Topic Catalog 및 별칭 정규화
- 졸/취업 같은 기호·축약 표현과 WELL/WEST/WSP/SAF 영문 경계 처리
- 해외일경험·해외취업·교환학생·해외탐방·해외연수·해외봉사·글로벌 창업 계층
- 공지의 직접 Topic과 부모 포함 Topic, Event Type, Audience, Campus 저장
- 기존 공지 일괄 재분류 서비스
- Topic 자동완성 API
- 기존 구독 모델의 선택적 `topicId` 저장 및 응답
- 신규 공지의 Topic 계층 구독 매칭, 회원·공지 단위 중복 방지, 기존 FCM 발송 연결
- 자유 키워드 API와 데이터의 하위 호환

## 데이터와 검증

- 운영 DB 활성 공지 2,671건 읽기 전용 조사
- 해외·국제 후보 제목 151건 검수
- 실제 공지 정답 63건, 별칭 평가 30건, 경계 사례 15건
- 해외 프로그램 근거 35건, 해외 프로그램 회귀 사례 50건
- Topic 계층·중복 알림 정책 사례 10건
- 데이터 JSON/JSONL 파싱, Catalog 무결성, 분류·자동완성·구독·매칭 회귀 테스트로 검증

## 배포 시 실행 항목

코드 기준 구현은 완료됐다. 실제 운영 반영에는 배포 절차로 다음을 실행한다.

1. 애플리케이션 배포 시 `keyword_subscription.topic_id`와 Topic 검색 인덱스를 생성한다.
2. Catalog v3 기준으로 기존 공지를 재분류한다.
3. 프런트엔드는 자동완성 선택 시 기존 등록 요청에 `topicId`만 추가한다.

이 세 항목은 별도 기능 개발이 아니라 배포·프런트 연결 단계이며, 기존 앱은 `topicId` 없이도 계속 동작한다.
