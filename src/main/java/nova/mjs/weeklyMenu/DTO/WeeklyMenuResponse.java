package nova.mjs.weeklyMenu.DTO;

import lombok.Builder;
import lombok.Data;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.weeklyMenu.entity.WeeklyMenu;
import nova.mjs.weeklyMenu.entity.enumList.MenuCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class WeeklyMenuResponse {
    private String date; //날짜 정보
    private MenuCategory menuCategory; //메뉴 카테고리 정보
    private List<String> meals; //메뉴 정보

    // 엔티티에서 DTO로 변환
    public static WeeklyMenuResponse fromEntity(WeeklyMenu entity) {
        return WeeklyMenuResponse.builder()
                .date(entity.getDate())
                .menuCategory(entity.getMenuCategory())
                .meals(entity.getMeals())
                .build();
    }
}
