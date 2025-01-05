package nova.mjs.weeklyMenu.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.util.entity.BaseEntity;

import java.util.List;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "weekly_menu")
public class WeeklyMenu extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String date; // 날짜 정보

    @ElementCollection
    @CollectionTable(name = "meal_details", joinColumns = @JoinColumn(name = "menu_id"))
    @MapKeyColumn(name = "meal_type")
    @Column(name = "menu_content")
    private Map<String, List<String>> meals; // 조식, 중식, 석식 정보
}
