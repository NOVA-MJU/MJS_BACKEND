# 키워드 알림 API (06-2-3 키워드 알림 설정)

내가 등록한 키워드 또는 자동완성에서 선택한 표준 Topic에 맞는 **새 글이 올라오면 푸시(FCM)로 알려주는** 기능이다.
Topic을 선택하면 공지 분류 계층으로, 선택하지 않으면 기존 제목 키워드 방식으로 매칭한다.

> 모든 응답은 공통 래퍼로 감싼다: `{ "status": "...", "data": <실제값>, "timestamp": "..." }`
> 추천 키워드를 빼면 전부 **로그인 필요**(헤더 `Authorization: Bearer <accessToken>`).

---

## 쓰는 순서 (앱 기준)

1. 로그인 → accessToken 확보
2. 앱이 FCM 토큰 발급 → `POST /api/v1/device-tokens` 로 등록 (이게 있어야 푸시가 감)
3. 입력 중 `GET /api/v1/alarm-topics/autocomplete`로 표준 Topic 후보 표시
4. 사용자가 키워드와 선택한 `topicId` 등록 → `POST /api/v1/keyword-alarms`
5. 새 글/학식이 올라오면 서버가 알아서 매칭 → 푸시 발송
6. 앱에서 `GET /api/v1/notifications` 로 알림함 표시, 누르면 읽음 처리

---

## 카테고리 / 플랫폼 값

| 카테고리(`categories`) | 뜻 | 매칭 방식 |
| --- | --- | --- |
| `NOTICE` | 공지사항 | 키워드가 **글 제목**에 있으면 알림 |
| `MJU_CALENDAR` | 학사일정 | 키워드가 **제목**에 있으면 알림 |
| `COMMUNITY` | 게시판 | 키워드가 **제목**에 있으면 알림 |
| `CAFETERIA` | 학식 | 키워드 무관, **새 학식 올라오면** 구독자 전원 알림 |

| 플랫폼(`platform`) | 뜻 |
| --- | --- |
| `ANDROID` / `IOS` / `WEB` | 토큰이 발급된 기기 종류 |

> `topicId`가 없으면 **제목 기준 + 접두 일치**다. `topicId`가 있으면 분류된 Topic 계층으로 매칭한다.

## 0. 알림 Topic 자동완성

```http
GET /api/v1/alarm-topics/autocomplete?query=졸업&limit=8
```

응답의 `topicId`를 저장 요청에 사용한다. `type=GROUP`인 항목은 활성 하위 Topic 전체를 포함한다.
사용자가 후보를 선택하지 않으면 `topicId`를 보내지 않고 자유 키워드로 등록할 수 있다.

---

## 1. 키워드 등록

```
POST /api/v1/keyword-alarms
```

요청 본문:
```json
{
  "keyword": "졸업",
  "topicId": "GRADUATION",
  "categories": ["NOTICE"]
}
```

| 필드 | 타입 | 규칙 |
| --- | --- | --- |
| `keyword` | string | **공백 제외 1~5글자** (띄어쓰기 불가) |
| `topicId` | string/null | 자동완성 항목을 선택한 경우 응답의 ID, 미선택 시 생략 |
| `categories` | string[] | 위 카테고리 값, **1개 이상** |

검증 실패 시 `400` + 메시지 `"올바른 형식의 키워드를 입력해 주세요."`
이미 등록한 키워드면 `400` + `"이미 등록된 키워드입니다."`

성공 응답(`data`):
```json
{
  "id": 12,
  "keyword": "졸업",
  "topicId": "GRADUATION",
  "categories": ["NOTICE"],
  "enabled": true,
  "createdAt": "2026-06-30T19:40:00"
}
```

---

## 2. 내 키워드 목록

```
GET /api/v1/keyword-alarms
```

`data` 는 위 1번 응답(객체)의 배열. 최신 등록순.

---

## 3. 카테고리 수정

```
PATCH /api/v1/keyword-alarms/{id}
```

```json
{
  "keyword": "졸업",
  "topicId": "GRADUATION",
  "categories": ["NOTICE"]
}
```

응답은 수정된 구독 객체. 남의 구독이면 `404` `"키워드 알림 구독을 찾을 수 없습니다."`

---

## 4. 키워드 삭제

```
DELETE /api/v1/keyword-alarms/{id}
```

성공 시 `data` 없음. 남의 구독이면 `404`.

---

## 5. 추천 키워드 (로그인 불필요, 고정값)

```
GET /api/v1/keyword-alarms/recommended
```

```json
["수강신청", "기숙사", "졸업", "국가근로", "해외탐방", "해외봉사"]
```

---

## 6. 기기 토큰 등록 (푸시 받으려면 필수)

```
POST /api/v1/device-tokens
```

```json
{ "fcmToken": "e1l_IePh...(앱이 발급)", "platform": "ANDROID" }
```

- 같은 토큰을 다시 보내면 **갱신**(멱등). 다른 계정에서 같은 기기면 현재 계정으로 재배정.
- 성공 시 `data` 없음.

## 7. 기기 토큰 삭제 (로그아웃 시)

```
DELETE /api/v1/device-tokens?fcmToken=<토큰>
```

---

## 8. 알림함 (받은 알림 목록)

메인 화면의 알림 아이콘 빨간 점만 판단할 때는 목록 대신 아래 경량 API를 호출한다.

```
GET /api/v1/notifications/unread-status
```

응답 `data`:

```json
{
  "unreadCount": 3,
  "hasUnread": true
}
```

`hasUnread=true`면 빨간 점을 표시하고, `false`면 숨긴다. 로그인 직후나 메인 화면
진입 시 호출하며 알림 상세 화면에서는 아래 목록 API의 동일 필드를 사용한다.

```
GET /api/v1/notifications?page=0&size=20
```

`data.content`는 **미읽음 최신순 → 읽음 최신순**으로 정렬된다.
`data.unreadCount`는 페이지와 무관한 전체 미읽음 수이고, `data.hasUnread`로
화면의 `모두 읽음` 버튼 활성화 여부를 바로 판단할 수 있다.

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `id` | number | 알림 id |
| `matchedKeyword` | string | 기존 클라이언트 호환용 걸린 키워드 |
| `keyword` | string\|null | 키워드 알림에만 노출(학식 방송형은 `null`) |
| `type` | string | 키워드/학식: `NOTICE` / `MJU_CALENDAR` / `COMMUNITY` / `WEEKLY_MENU`, 활동: `COMMUNITY_LIKE` / `COMMUNITY_COMMENT` / `REVIEW_LIKE` |
| `categoryCode` | string | `NOTICE` / `MJU_CALENDAR` / `COMMUNITY` / `CAFETERIA` |
| `category` | string | 화면 표시용 `공지사항` / `학사일정` / `게시판` / `학식` |
| `title` | string | 글 제목(학식은 안내 문구) |
| `link` | string\|null | 원문 링크 |
| `read` | boolean | 읽음 여부 |
| `sentAt` | date-time | 기존 클라이언트 호환용 ISO-8601 발송 시각 |
| `timestamp` | number | Unix epoch milliseconds. 상대/절대 표기는 프론트에서 계산 |

## 9. 알림 읽음 처리

```
PATCH /api/v1/notifications/{id}/read     # 단건 (갱신된 read=true 알림 반환)
PATCH /api/v1/notifications/read-all      # 전체 (updatedCount, unreadCount=0, hasUnread=false)
```

---

## 동작 메모 (헷갈리기 쉬운 부분)

- **키워드 푸시 시점**: 콘텐츠가 **처음 올라올 때만** 발송한다(수정으로는 재알림 안 함).
- **활동 인앱 알림**: 내 게시글 좋아요/댓글, 내 명지도 리뷰 좋아요가 알림함에 쌓인다. 본인 활동은 제외한다.
- **좋아요 집계**: 같은 게시글/리뷰의 좋아요는 알림 1건을 최신 인원·개수로 갱신하고 다시 미읽음 처리한다.
- **중복 방지**: 한 글이 한 사람의 키워드 여러 개에 걸려도 **알림은 1건**.
- **학식**: 크롤링은 같은 주를 여러 번 돌리지만, **메뉴 내용이 실제 바뀐 경우에만** 1건 발송.
- **푸시 안 와도 알림함은 쌓임**: 기기 토큰이 없거나 FCM 미설정이어도 `GET /notifications` 에는 기록된다.
- **에러 형식**: 실패는 `{ status, error, message }` 형태로 내려온다(전역 핸들러).

---

## 예시 (curl)

```bash
# 키워드 등록
curl -X POST https://<host>/api/v1/keyword-alarms \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"keyword":"장학","categories":["NOTICE"]}'

# 기기 토큰 등록
curl -X POST https://<host>/api/v1/device-tokens \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"fcmToken":"<APP_FCM_TOKEN>","platform":"ANDROID"}'

# 알림함
curl https://<host>/api/v1/notifications?page=0&size=20 \
  -H "Authorization: Bearer $TOKEN"
```
