package nova.mjs.weeklyMenu.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.community.entity.CommunityBoard;
import nova.mjs.community.entity.enumList.CommunityCategory;
import nova.mjs.util.entity.BaseEntity;
import nova.mjs.weeklyMenu.entity.enumList.MenuCategory;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter //vs. @data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "weekly_menu")
public class WeeklyMenu extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private long id;

    private String date; // 날짜 정보

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuCategory menuCategory;

    @ElementCollection
    @CollectionTable(name = "meal_details", joinColumns = @JoinColumn(name = "menu_id"))
    @Column(name = "menu_content")
    @Builder.Default //new를 하지 않으면, 위에서 annotation 오류가 발생함
    private List<String> meals = new ArrayList<>(); // 메뉴

    public static WeeklyMenu create(String date, MenuCategory menuCategory, List<String> meals) {
        WeeklyMenu menu = WeeklyMenu.builder()
                .date(date) //id는 안 하는 이유?
                .menuCategory(menuCategory)
                .build();
        menu.meals.addAll(meals != null ? meals : new ArrayList<>()); // null일 경우 빈 리스트 추가
        return menu;
    }
}
