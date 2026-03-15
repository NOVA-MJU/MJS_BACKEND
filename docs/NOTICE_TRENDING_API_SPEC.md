# 공지 조회수/급상승 저장 전략 (권장안)

## 핵심 결론 (정답)

- **둘 다 저장하는 게 맞습니다.**
  - `viewCount`: 외부 사이트의 "실제 조회수"
  - `countView`: 오늘 기준 누적 증가량(급상승 계산용)
- DB를 껐다 켠다고 데이터가 자동 초기화되지는 않습니다.
  - **영속 DB(MySQL/PostgreSQL 디스크 볼륨)**를 쓰면 그대로 유지됩니다.
  - 인메모리(H2 memory)나 컨테이너 볼륨 미사용이면 초기화될 수 있습니다.

---

## 왜 2개를 같이 저장해야 하나?

1. `viewCount`만 있으면 현재 인기(절대값)는 보이지만, "오늘 갑자기 뜬" 공지를 잡기 어렵습니다.
2. `countView`(증가량)를 같이 저장하면 급상승 TOP10을 안정적으로 계산할 수 있습니다.
3. 클라이언트/운영에서
   - "실제 조회수"
   - "오늘 상승폭"
   을 분리해서 보여줄 수 있습니다.

---

## 이번 적용 정책

### Notice 저장 컬럼
- `viewCount`: 실제 조회수
- `viewCountDeltaToday`: 오늘 누적 증가량 (`countView`)
- `viewCountDeltaDate`: 증가량 기준일

### 크롤링 시 업데이트
1. 조회수 크롤링
2. 날짜가 바뀌면 `viewCountDeltaToday`를 0으로 리셋
3. `(새 조회수 - 이전 조회수)`가 양수면 누적
4. `viewCount`를 최신값으로 갱신

> 외부 사이트 보정 등으로 조회수가 감소해도 음수는 누적하지 않음.

### 급상승 API 정렬 기준
- `viewCountDeltaToday DESC`
- 동률 시 `viewCount DESC`, `date DESC`

---

## DB 재기동/초기화 관련 정리

- **정상 운영 DB**: 서버 재기동/DB 재기동해도 값 유지
- **초기화되는 경우**:
  - 인메모리 DB 사용
  - Docker 볼륨 미마운트
  - 배포 파이프라인에서 recreate 시 데이터 볼륨 삭제

### 권장 운영
- MySQL/PostgreSQL + Persistent Volume
- 정기 백업(스냅샷)
- 필요 시 조회수 이력 테이블(스냅샷) 추가로 장기 분석

---

## API

### `GET /api/v1/notices/trending?size=10`

```json
[
  {
    "title": "2026학년도 1학기 대학 재학생 등록금 최종 납부 안내",
    "date": "2026-03-06T00:00:00",
    "category": "general",
    "link": "https://www.mju.ac.kr/...",
    "viewCount": 391,
    "countView": 124,
    "countViewDate": "2026-03-07"
  }
]
```
