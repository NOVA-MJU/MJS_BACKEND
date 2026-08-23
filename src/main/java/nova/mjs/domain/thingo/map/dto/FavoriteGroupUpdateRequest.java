package nova.mjs.domain.thingo.map.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 그룹명/색상 수정 요청.
 * name: 1~12자(공백 포함). color: 팔레트 enum 이름. 시스템 그룹은 수정 불가.
 */
@Getter
@NoArgsConstructor
public class FavoriteGroupUpdateRequest {
    private String name;
    private String color;
}
