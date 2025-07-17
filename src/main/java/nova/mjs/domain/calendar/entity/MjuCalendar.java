package nova.mjs.domain.calendar.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.domain.calendar.dto.MjuCalendarDTO;

import java.time.LocalDate;

@Entity
@Table(name = "mju_calendar")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MjuCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int year;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(length = 1000)
    private String description;

    /** DTO를 받아 엔티티를 생성하는 정적 팩토리 메서드 */
    public static MjuCalendar create(MjuCalendarDTO dto) {
        return MjuCalendar.builder()
                .year(dto.year())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .description(dto.description())
                .build();
    }
}
