package nova.mjs.mentor.mentoring.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.util.entity.BaseEntity;

import java.util.UUID;


// Entity
@Entity
@Table(name = "mentoringrequestcontent")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MentoringRequestContent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column
    private int estimatedTime; //예상시간

    @Column
    private String mentoringPurposeAndQuestion; //멘토링 목적 및 질문사항

    @Column
    private String preferredTimeSlot; //희망 시간대
}
