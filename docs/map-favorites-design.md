# 명지도 즐겨찾기(그룹) 기능 설계서

> 상태: **설계/논의용 초안 (v1)** — 구현 전 데이터 저장 구조 확정을 위한 문서
> 대상 화면: 05-1(즐겨찾기 리스트), 05-1-1(그룹 상세), 05-5-1-1(그룹명 수정), 05-5-1-2(그룹 삭제),
> 05-5-3(새 그룹 추가), 그룹 선택 바텀시트(특정 장소 → 즐겨찾기)

---

## 1. 배경 / 현재 구조

현재 즐겨찾기는 **그룹 개념이 없는 단순 토글** 구조다.

| 엔티티 | 테이블 | 단위 | 관련 API/용도 |
|---|---|---|---|
| `PinFavorite` | `map_pin_favorite` | (member, pin) 유일 | `POST /api/v1/map/favorites` 토글, 칩 목록 상단 정렬(`findFavoritePinIds`), 마이페이지 집계(`countByMember`) |
| `BusFavorite` | `bus_favorite` | (member, arsId, routeName) 유일 | 버스 도착 정보 상단 정렬 |

신규 설계는 여기에 **그룹(폴더) 계층**과 **장소별 메모**를 추가한다. 즉:

- 한 회원은 여러 개의 **그룹**을 만든다. (그룹명 + 색상)
- 하나의 장소(핀)는 **여러 그룹에 동시에** 담길 수 있다. (다중 선택)
- 그룹에 담긴 각 장소는 **메모(최대 30자)** 를 가질 수 있다.
- `내 장소`, `버스` 두 그룹은 **시스템 기본 제공**이며 정렬과 무관하게 항상 상단 고정.

---

## 2. 핵심 설계 결정 (논의 필요 지점 ★)

### ★A. 기존 `PinFavorite`와 신규 그룹 모델의 관계

신규 그룹 모델에서 "이 장소가 즐겨찾기됨"의 의미가 "**어떤 그룹엔가 담겨 있음**"으로 바뀐다.
→ 기존 `PinFavorite`(member,pin 플랫 토글)와 이중 소스가 되면 안 된다.

**권장안(Option A): `PinFavorite`를 그룹 모델로 흡수.**
- `내 장소`를 회원별 **기본 시스템 그룹**으로 두고, 기존 `PinFavorite` 행을 이 그룹의 멤버십으로 마이그레이션.
- 지도 칩/검색/상세의 "별 채워짐" 여부 = 해당 핀이 **회원의 어떤 place 그룹에든 담겨 있는지**로 판정.
- 기존 `POST /api/v1/map/favorites` 토글 API의 동작 변경(아래 §5.4 참고).

Option B(기존 `PinFavorite` 유지 + 그룹 별도)는 "즐겨찾기됨" 판정이 두 곳에 나뉘어 정합성 관리가 어려우므로 비권장.

### ★B. `버스` 시스템 그룹의 데이터 소스

버스 즐겨찾기는 핀이 아니라 (arsId, routeName)이라 `FavoritePlace`(핀 멤버십)로 담을 수 없다.
→ **`버스`는 DB에 저장하지 않는 가상 그룹.** 그룹 리스트 응답에만 `내 장소` 다음(상단 고정)으로 끼워 넣는다.
`id=null`, `type=SYSTEM_BUS`, `placeCount`=회원의 `BusFavorite` 노선 총합(정류장 A/B 무관, 한쪽만 담아도 그만큼).
프론트는 이 항목을 탭하면 그룹 상세가 아니라 **기존 버스 도착정보 화면**으로 이동한다.
(버스 노선 조회/토글은 기존 `/bus/arrivals`, `/bus/favorites` 재사용 — 즐겨찾기 노선 상단 정렬 이미 구현됨.)

### ★C. 메모의 소속 단위

그룹 삭제 스펙: *"해당 그룹 데이터 및 그룹 내 저장되어 있던 장소, 메모 리스트 삭제"*
→ 메모는 **(그룹, 핀) 멤버십에 종속**되는 것으로 본다. 즉 `FavoritePlace.memo`.
바텀시트에서 한 장소를 여러 그룹에 넣으며 메모 1개를 입력하면 → 선택된 각 멤버십에 동일 메모 기록.

### ★D. `내 장소` 자동 포함 여부 / 별 토글 진입점

스펙 10: 특정 장소의 [즐겨찾기] 클릭 → **'그룹 선택 바텀시트' 진입** (직접 토글 아님).
→ 지도/상세의 별 버튼은 이제 "바텀시트 열기". 저장 시 선택된 그룹 집합으로 멤버십을 replace.
논의: 바텀시트 진입 시 `내 장소`를 기본 체크로 둘지(모든 즐겨찾기는 최소 내 장소에 포함) vs. 완전 자유 선택.

---

## 3. 데이터 저장 구조 (권장안 기준)

### 3.1 `FavoriteGroup` — 즐겨찾기 그룹

테이블: `map_favorite_group`

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `map_favorite_group_id` | BIGINT PK | auto | |
| `member_id` | BIGINT FK | not null | 소유 회원 |
| `name` | VARCHAR(12) | not null | 그룹명 (1~12자, 공백포함) |
| `color` | VARCHAR(20) | not null | `FavoriteGroupColor` enum, 기본 `BLUE` |
| `type` | VARCHAR(20) | not null | `SYSTEM_MY_PLACES` / `USER` (저장되는 값). `SYSTEM_BUS`는 가상 응답 마커로만 사용 |
| `created_at`,`updated_at` | (BaseEntity) | | 최신순 정렬 기준 = created_at |

- **저장되는 시스템 그룹은 `내 장소` 하나뿐**이며, 회원 최초 접근 시 lazy 생성한다.
- `버스`는 저장하지 않고 그룹 리스트 응답에만 가상 항목으로 노출한다(§★B).
- 시스템 그룹은 **수정/삭제 불가** (요청 시 예외).
- 상단 고정 순서(`내 장소`→`버스`)는 정렬 로직에서 처리(별도 sort_order 컬럼 불필요).

### 3.2 `FavoritePlace` — 그룹 내 장소 멤버십

테이블: `map_favorite_place`

| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| `map_favorite_place_id` | BIGINT PK | auto | |
| `group_id` | BIGINT FK | not null | 소속 그룹 |
| `pin_id` | BIGINT FK | not null | 대상 핀(건물/장소) |
| `memo` | VARCHAR(30) | nullable | 장소 메모 (최대 30자) |
| `created_at`,`updated_at` | (BaseEntity) | | **장소 추가순** 정렬 기준 = created_at |

- 유니크 제약: `uk_map_favorite_place_group_pin (group_id, pin_id)` — 같은 그룹 내 중복 방지.
- 그룹 삭제 시 하위 `FavoritePlace` 전부 삭제(cascade / 명시 삭제).
- "회원이 이 핀을 즐겨찾기했는가" = `member의 group들 중 group_id ∈ ... AND pin_id = ?` 존재 여부.

### 3.3 버스

- 신규 테이블 없음. 기존 `BusFavorite` 재사용.
- `버스` 시스템 그룹의 개수/내용은 `BusFavorite`에서 조회.

### 3.4 `FavoriteGroupColor` (enum, 팔레트 10색)

목업 팔레트 기준(순서대로): `CORAL, RED, ORANGE, AMBER, LIME, GREEN, SKY, BLUE(기본), PURPLE, GRAY`
- 실제 hex는 프론트 팔레트와 1:1 매핑(백엔드는 enum 이름만 저장, hex는 프론트/공용 상수).
- 기본값 `BLUE`(띵고 색).

### 3.5 ERD (텍스트)

```
Member 1 ──< FavoriteGroup 1 ──< FavoritePlace >── 1 Pin
                  │
                  └ type = SYSTEM_MY_PLACES | SYSTEM_BUS | USER

Member 1 ──< BusFavorite        (버스 시스템 그룹의 내용물)
```

---

## 4. 정렬 규칙

**그룹 리스트(05-1)** — 기본 `최신순`
- `최신순`: 그룹 `created_at` DESC
- `가나다순`: 그룹 `name` ASC (한글 로케일)
- `장소 추가순`: 그룹에 마지막으로 장소가 추가된 시각(하위 `FavoritePlace.created_at` MAX) DESC
- **시스템 그룹(`내 장소`,`버스`)은 정렬과 무관하게 항상 상단 고정.**

**그룹 상세(05-1-1)** — 기본 `장소 추가순`
- `장소 추가순`: `FavoritePlace.created_at` DESC (해당 그룹 기준)
- `가나다순`: 핀 `name` ASC

---

## 5. API 설계 (초안)

베이스: `/api/v1/map/favorites` · 전부 `isAuthenticated()`

### 5.1 그룹
| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/groups?sort=latest\|name\|place_added` | 그룹 리스트 (시스템 그룹 상단 고정). 각 항목: id, name, color, type, placeCount |
| POST | `/groups` `{name, color}` | 그룹 생성 (name 1~12자 검증) |
| PATCH | `/groups/{groupId}` `{name, color}` | 그룹명/색상 수정 (시스템 그룹 불가) |
| DELETE | `/groups/{groupId}` | 그룹 + 하위 멤버십/메모 삭제 (시스템 그룹 불가) |

### 5.2 그룹 상세(장소 목록)
| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/groups/{groupId}/places?sort=place_added\|name&lat=&lng=` | 그룹 내 장소 카드 목록. 카드: 카테고리 아이콘, 이름, 강의실코드, 운영상태, 거리(lat/lng 있을 때), memo, favorite=true. 비었으면 빈 배열(프론트 '아직 저장된 장소가 없어요' 표시). `버스` 그룹이면 버스 즐겨찾기 목록 반환 |

> UX 문구: 그룹 상세 빈 상태 = "아직 저장된 장소가 없어요" / 새 그룹 생성 입력 힌트 = "설정하실 그룹명을 입력해 주세요" / 그룹명 수정 = 기존 이름 prefill.

### 5.3 특정 장소 → 그룹 선택 바텀시트
| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/pins/{pinId}/groups` | 바텀시트용: 회원 그룹 목록 + 각 그룹에 이 핀 포함여부(selected) + 기존 memo |
| PUT | `/pins/{pinId}` `{groupIds:[...], memo}` | 이 핀의 소속 그룹 집합을 replace + memo 반영. groupIds 비면 전 그룹에서 제거(=즐겨찾기 해제) |

### 5.4 개별 해제 / 기존 토글 처리 (★논의)
- 그룹 상세의 별 토글: `DELETE /groups/{groupId}/places/{pinId}` (그룹에서만 제거).
  - UI: 해제해도 페이지 이탈 전까지 카드 유지(프론트 처리) — 백엔드는 즉시 삭제.
- 기존 `POST /api/v1/map/favorites`(플랫 토글): §2-★A 결정에 따라
  (a) 폐기하고 바텀시트 PUT로 대체, 또는 (b) "내 장소" 토글로 동작 유지.

### 5.5 검증/에러코드(신규 예정)
- `FAVORITE_GROUP_NOT_FOUND` (404)
- `FAVORITE_GROUP_NAME_INVALID` (400, 1~12자)
- `FAVORITE_GROUP_SYSTEM_MODIFY_NOT_ALLOWED` (400, 시스템 그룹 수정/삭제)
- `FAVORITE_MEMO_TOO_LONG` (400, 30자 초과)
- `FAVORITE_PLACE_NOT_FOUND` (404)

---

## 6. 클래스 구조 (패키지: `domain.thingo.map`)

```
entity/
  FavoriteGroup.java        (신규)
  FavoritePlace.java        (신규)
  FavoriteGroupColor.java   (신규, enum)
  FavoriteGroupType.java    (신규, enum: SYSTEM_MY_PLACES/SYSTEM_BUS/USER)
  PinFavorite.java          (★A안 채택 시 FavoritePlace로 흡수/제거 검토)
repository/
  FavoriteGroupRepository.java
  FavoritePlaceRepository.java
service/
  FavoriteGroupService.java      (그룹 CRUD, 시스템 그룹 보장)
  FavoritePlaceService.java      (멤버십 replace/조회, 메모)
controller/
  MapFavoriteController.java      (기존 확장) 또는 MapFavoriteGroupController(신규)
dto/
  FavoriteGroupResponse, FavoriteGroupCreateRequest, FavoriteGroupUpdateRequest,
  FavoritePlaceCardResponse, PinGroupSelectionResponse, PinFavoritePutRequest ...
```

---

## 7. 논의 결과 (확정) 및 구현 반영

| # | 항목 | 결정 |
|---|---|---|
| ★A | 기존 `PinFavorite` | **`내 장소` 그룹으로 흡수.** 기동 시 1회성 마이그레이션(`FavoriteMigrationRunner`)으로 기존 행을 `내 장소` `FavoritePlace`로 이관(멱등). 읽기(`favoritePinIds`)·마이페이지 집계·레거시 토글 모두 그룹 모델로 재연결 |
| ★B | `버스` 그룹 | **가상 그룹(미저장).** DB에 행을 만들지 않고 그룹 리스트 응답에만 `내 장소` 다음에 삽입(id=null, type=SYSTEM_BUS). 개수는 `BusFavorite` count. 탭 시 버스 도착정보 화면으로 이동 (기존 bus API 재사용) |
| ★C | 메모 단위 | **(그룹, 핀) 멤버십 종속** = `FavoritePlace.memo`. 같은 장소도 그룹마다 다른 메모 가능 |
| ★D | 별 클릭 | **그룹 선택 바텀시트 진입 + `내 장소` 기본 체크.** 기본 체크는 프론트 프리셋(백엔드는 전달된 groupIds를 그대로 반영). 레거시 `POST /favorites` 토글은 `내 장소` 편입/해제로 호환 유지 |
| 5 | 시스템 그룹 생성 시점 | **최초 접근 시 lazy 생성**(`FavoriteGroupProvisioner`). 가입 플로우 미변경 |
| 6 | 개수 상한 | 이번 구현에서는 미설정(무제한). 필요 시 후속 |

### 구현 산출물 (엔티티/서비스/컨트롤러)
- entity: `FavoriteGroup`, `FavoritePlace`, `FavoriteGroupColor`, `FavoriteGroupType`
- repository: `FavoriteGroupRepository`, `FavoritePlaceRepository`, (`BusFavoriteRepository.countByMember` 추가)
- service: `FavoriteGroupService`, `FavoritePlaceService`, `FavoriteGroupProvisioner`, `FavoriteMigrationRunner`, (`PinFavoriteService` 위임 전환)
- controller: `MapFavoriteGroupController` (신규), `MapFavoriteController` (레거시 토글 유지)
- 재연결: `MapPinService`/`MapSearchService`의 즐겨찾기 판정, `ProfileService` 마이페이지 집계 → `FavoritePlace` 기준
- 에러코드: `FAVORITE_GROUP_NOT_FOUND`, `FAVORITE_GROUP_FORBIDDEN`, `FAVORITE_GROUP_NAME_INVALID`, `FAVORITE_GROUP_SYSTEM_MODIFY_NOT_ALLOWED`, `FAVORITE_MEMO_TOO_LONG`

### API 문서
- OpenAPI 스펙(`src/main/resources/static/openapi/map.json`)에 즐겨찾기 그룹 API 6종 경로 + 스키마 7종 반영, `버스` 가상 그룹 규칙 명시 (버전 v1.3.0)

### 남은 후속 과제
- `버스` 그룹 처리 확정됨: **가상 그룹으로 리스트에만 노출, 탭 시 기존 버스 도착정보 화면**으로 이동(별도 상세 엔드포인트 없음)
- 안정화 후 레거시 `map_pin_favorite` 테이블/`PinFavorite` 정리
- 단위/통합 테스트 (현 원격 환경은 jitpack(KOMORAN) 차단으로 빌드 불가 → 테스트는 로컬/CI에서 추가 필요)
