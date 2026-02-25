# 시기별 추천 검색어 API 상세 명세

## 1. 개요

프론트 화면에서 `"OO 시기별 추천 검색어"` 형태의 UI를 구성할 때,
- `OO`에 들어갈 **시기 텍스트**와
- 해당 시기에서 노출할 **추천 검색어 5개**를
한 번에 조회하기 위한 API입니다.

> 기본 컨셉
> - 종류명(예: 개강/시험/종강/방학/인기 검색어)은 API 응답 `period`로 고정 노출
> - 검색어는 API 응답 `keywords` 배열로 순서대로 노출

---

## 2. 엔드포인트

- **Method**: `GET`
- **Path**: `/api/v1/search/suggest/seasonal`
- **Auth**: 없음

### Query Parameter

| 이름 | 타입 | 필수 | 기본값 | 설명 |
|---|---|---|---|---|
| `month` | Integer | N | 서버 현재 월 | 조회 기준 월(1~12) |

- `month` 미전달 시 서버 `LocalDate.now().getMonthValue()` 사용
- `month`가 1~12 범위를 벗어나면 400 에러

---

## 3. 월별 시기 매핑 규칙

### 3.1 운영 시기

| 시기 텍스트 (`period`) | 월 | 데이터 소스 |
|---|---|---|
| `개강` | 3, 9 | `popular_search_keyword` 테이블 |
| `시험` | 4, 10 | `popular_search_keyword` 테이블 |
| `종강` | 6, 12 | `popular_search_keyword` 테이블 |
| `방학` | 1, 2, 7, 8 | `popular_search_keyword` 테이블 |

### 3.2 미운영 시기

| 시기 텍스트 (`period`) | 월 | 데이터 소스 |
|---|---|---|
| `인기 검색어` | 5, 11 | Redis 실시간 검색어 Top5 (`RealtimeKeywordService`) |

---

## 4. 응답 스키마

```json
{
  "status": "API 요청 성공",
  "data": {
    "period": "개강",
    "keywords": ["수강신청", "정정기간", "수업계획서", "강의실", "학사일정"]
  },
  "timestamp": "2026-02-22T10:00:00.000000"
}
```

### data 필드

| 필드 | 타입 | 설명 |
|---|---|---|
| `period` | String | 화면 고정 텍스트. `개강`, `시험`, `종강`, `방학`, `인기 검색어` 중 하나 |
| `keywords` | Array<String> | 추천 검색어 리스트(최대 5개) |

---

## 5. 오류 응답

### 400 Bad Request

`month`가 1~12 범위를 벗어나면 아래 형태로 응답됩니다.

```json
{
  "status": 400,
  "message": "month는 1~12 사이여야 합니다.",
  "timestamp": "2026-02-22T10:00:00.000000"
}
```

---

## 6. 데이터 모델

### 6.1 테이블: `popular_search_keyword`

| 컬럼명 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `popular_search_keyword_id` | BIGINT | PK, AUTO_INCREMENT | 식별자 |
| `period` | VARCHAR(20) | NOT NULL | 시기 enum 문자열 |
| `keyword` | VARCHAR(100) | NOT NULL | 추천 검색어 |
| `display_order` | INT | NOT NULL | 노출 순서(오름차순) |

### 6.2 period enum 값

| enum 값 | 의미 |
|---|---|
| `ENROLLMENT` | 개강 |
| `EXAM` | 시험 |
| `SEMESTER_END` | 종강 |
| `VACATION` | 방학 |

### 6.3 조회 규칙

- 시기별 조회 시 `display_order ASC, id ASC` 정렬
- 최대 5건 반환
- 데이터가 5건 미만이면 있는 만큼만 반환

---

## 7. 사용 예시

### 7.1 개강 시기 조회

요청:

```http
GET /api/v1/search/suggest/seasonal?month=3
```

응답 요약:

- `period = "개강"`
- `keywords = DB의 ENROLLMENT 상위 5건`

### 7.2 미운영 시기 조회

요청:

```http
GET /api/v1/search/suggest/seasonal?month=11
```

응답 요약:

- `period = "인기 검색어"`
- `keywords = Redis 실시간 검색어 Top5`

### 7.3 month 미전달

요청:

```http
GET /api/v1/search/suggest/seasonal
```

응답 요약:

- 서버 현재 월 기준으로 자동 매핑

---

## 8. 프론트 연동 가이드

1. 타이틀 렌더링
   - `"${period} 시기별 추천 검색어"` 형태로 표시
2. 검색어 렌더링
   - `keywords` 배열 순서대로 최대 5개 표시
3. 예외 처리
   - 400 응답 시 기본 문구/기본 검색어 fallback 처리 권장

---

## 9. 백엔드 구현 포인트

- Controller
  - `SuggestController#getSeasonalSuggestions(month)`
- Service
  - `SeasonalSuggestService`
  - 월 유효성 검증(1~12)
  - 미운영 월(5,11) 분기
  - 시기 매핑 후 DB 조회
- Repository
  - `findTop5ByPeriodOrderByDisplayOrderAscIdAsc`
- Entity
  - `PopularSearchKeyword`

