package nova.mjs.mentor.mentoring.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.util.entity.BaseEntity;

import java.util.UUID;


// Entity
@Entity
@Table(name = "thanksmessage")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ThanksMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentoring_id")
    private Mentoring mentoring; //한 멘토링에 여러 감사 메시지
}
