package nova.mjs.domain.thingo.map.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * '그룹 선택 바텀시트' 저장 요청.
 *
 * 이 핀이 속할 그룹 집합을 replace 한다. groupIds 에 포함된 그룹에는 담고(없으면 추가),
 * 빠진 그룹에서는 제거한다. groupIds 가 비면 전 그룹에서 제거(=즐겨찾기 해제).
 * memo(최대 30자)는 선택된 각 그룹 멤버십에 동일하게 반영된다.
 */
@Getter
@NoArgsConstructor
public class PinFavoriteSaveRequest {
    private List<Long> groupIds;
    private String memo;
}
