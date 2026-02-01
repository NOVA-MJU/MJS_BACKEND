package nova.mjs.domain.thingo.calendar.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.domain.thingo.calendar.dto.MjuCalendarDTO;
import nova.mjs.domain.mentorship.ElasticSearch.EntityListner.MjuCalendarEntityListener;

import java.time.LocalDate;

@Entity
@Table(name = "mju_calendar")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(MjuCalendarEntityListener.class)
public class MjuCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int year;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(length = 1000)
    private String description;

    /** DTO를 받아 엔티티를 생성하는 정적 팩토리 메서드 */
    public static MjuCalendar create(MjuCalendarDTO dto) {
        return MjuCalendar.builder()
                .year(dto.getYear())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .description(dto.getDescription())
                .build();
    }
}
