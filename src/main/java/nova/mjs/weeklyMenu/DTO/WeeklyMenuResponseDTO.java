package nova.mjs.weeklyMenu.DTO;

import lombok.Builder;
import lombok.Data;
import nova.mjs.weeklyMenu.entity.WeeklyMenu;
import nova.mjs.weeklyMenu.entity.enumList.MenuCategory;

import java.util.List;

@Data
@Builder
public class WeeklyMenuResponseDTO {
    private String date; //날짜 정보
    private MenuCategory menuCategory; //메뉴 카테고리 정보
    private List<String> meals; //메뉴 정보

    // 엔티티에서 DTO로 변환
    public static WeeklyMenuResponseDTO fromEntity(WeeklyMenu weeklyMenu) {
        return WeeklyMenuResponseDTO.builder()
                .date(weeklyMenu.getDate())
                .menuCategory(weeklyMenu.getMenuCategory())
                .meals(weeklyMenu.getMeals())
                .build();
    }
    public static List<WeeklyMenuResponseDTO> fromEntityToList(List<WeeklyMenu> weeklyMenu) {
        return weeklyMenu.stream()
                .map(WeeklyMenuResponseDTO::fromEntity) // 각 엔티티를 DTO로 변환
                .toList(); // 리스트로 변환
    }
}
