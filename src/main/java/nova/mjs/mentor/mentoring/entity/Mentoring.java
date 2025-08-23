package nova.mjs.mentor.mentoring.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.domain.member.entity.enumList.DepartmentName;
import nova.mjs.mentor.profile.entity.Mentor;
import nova.mjs.util.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Entity
@Entity
@Table(name = "mentor")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Mentoring extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false)
    private String mentoringTitle; //멘토링 제목

    @Column
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @Column(nullable = false)
    private String mentoringDescription; //멘토링 설명

    @ElementCollection
    @Column
    private List<String> subjects; //멘토링 주제

    @Column
    private int maxMentee; //최대 멘티 수

    @Column(nullable = false)
    private String availableTime; //가능한 시간대

    @Column
    @OneToMany(mappedBy = "mentoring", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ThanksMessage> thanksMessages = new ArrayList<>();


}
