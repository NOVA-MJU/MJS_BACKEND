package nova.mjs.weeklyMenu.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.util.entity.BaseEntity;
import nova.mjs.weeklyMenu.entity.enumList.MenuCategory;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter //vs. @data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "weekly_menu")
public class WeeklyMenu extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String date; // 날짜 정보

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuCategory menuCategory;

    @ElementCollection
    @CollectionTable(name = "meal_details", joinColumns = @JoinColumn(name = "id"))
    private List<String> meals = new ArrayList<>(); // 메뉴

    public static WeeklyMenu create(String date, MenuCategory menuCategory, List<String> meals) {
        return WeeklyMenu.builder()
                .date(date)
                .menuCategory(menuCategory)
                .meals(meals)
                .build();
    }

}
