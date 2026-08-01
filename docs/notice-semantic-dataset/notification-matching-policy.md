# Topic 기반 알림 매칭 정책 v1

## 입력과 출력

입력은 공지의 `directTopicIds`/`expandedTopicIds`, 사용자가 명시적으로 선택한 Topic과 필터,
신규·중복·활성 상태다. 출력은 각 단계의 결과와 최종 제외 사유를 분리한다.

```text
topicMatched
explicitFilterMatched
isNewContent
isDuplicate
subscriptionEnabled
eligible
exclusionReason
```

## Topic 계층

- 상위 Topic 구독은 모든 활성 하위 Topic을 포함한다.
- 하위 Topic 구독은 부모나 형제 Topic으로 확장하지 않는다.
- 공지는 직접 Topic뿐 아니라 부모를 포함한 `expandedTopicIds`를 실행 시 계산하거나 저장한다.
- 여러 구독이 같은 공지를 매칭해도 회원·공지 단위로 한 번만 발송한다.

## 넓은 구독 보존

사용자가 `GRADUATION`을 선택하면 졸업요건, 졸업유예, 조기졸업, 학위수여식,
졸업생 취업지원, 졸업작품·연주를 포함한 모든 활성 하위 Topic을 받는다. 시스템이 사용자의 선택을
임의로 좁히지 않는다.

## Audience와 Campus

- 사용자 필터가 없으면 공지의 대상·캠퍼스 값으로 범위를 줄이지 않는다.
- 공지 라벨이 `[]`이면 미지정이므로 필터 불일치로 제외하지 않는다.
- 공지가 `ALL`이면 특정 캠퍼스/대상 사용자를 포함한다.
- 사용자와 공지 양쪽에 명시적 값이 있고 교집합이 없을 때만 제외한다.

## 중복과 상태

1. Topic 계층 매칭
2. 사용자 명시 필터
3. 구독 활성화
4. 신규 콘텐츠 여부
5. 회원·공지 dedup
6. 발송 대상 확정

재분류나 검색 재색인은 신규 공지를 만들지 않는다. 같은 원본 공지의 수정본은 정책상 변경 알림을 별도로
지원하기 전까지 신규 발송하지 않는다.

## 제외 사유 코드

```text
NO_TOPIC_MATCH
EXPLICIT_AUDIENCE_MISMATCH
EXPLICIT_CAMPUS_MISMATCH
SUBSCRIPTION_DISABLED
NOT_NEW_CONTENT
DUPLICATE_MEMBER_NOTICE
```

분류 데이터셋에는 이 코드를 넣지 않는다. 알림 매칭 평가셋에서만 사용한다.
