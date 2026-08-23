package nova.mjs.domain.thingo.map.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 새 그룹 생성 요청.
 * name: 1~12자(공백 포함). color: 팔레트 enum 이름(미지정 시 기본 BLUE).
 * 상세 검증은 서비스에서 수행한다.
 */
@Getter
@NoArgsConstructor
public class FavoriteGroupCreateRequest {
    private String name;
    private String color;
}
