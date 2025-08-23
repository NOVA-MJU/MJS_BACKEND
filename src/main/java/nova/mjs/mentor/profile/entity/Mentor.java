package nova.mjs.mentor.profile.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.domain.member.entity.enumList.DepartmentName;
import nova.mjs.mentor.mentoring.entity.Mentoring;
import nova.mjs.util.entity.BaseEntity;

import java.util.List;
import java.util.UUID;

// Entity
@Entity
@Table(name = "mentor")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Mentor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false)
    private String name; //실명

    @Column(nullable = false)
    private String email; //email

    @Column(nullable = false)
    private String password; //비밀번호

    @Column(nullable = false)
    private String phoneNumber; //연락처

    @Enumerated(EnumType.STRING)
    @Column(name = "department_name", nullable = false)
    private DepartmentName departmentName; //졸업 전공

    @Column(nullable = false)
    private int year; //졸업 연도

    @Column
    private String workSpace; //현재 직장

    @Column(nullable = false)
    private String job; //직무 -> 밑에 category랑 중복? enum으로 정리?

    @Column(nullable = false)
    private String career; //총 경력 -> enum 으로 변경

    @Column(nullable = false)
    private String category; //직무 카테고리 -> enum으로 변경

    @Column
    private String jobDescription; //회사 및 업무 소개

    @Column
    private String growthProcess; //공부 방법 및 성장 과정 -> 굳이 필요할까?

    @ElementCollection
    @Column(nullable = false)
    private List<String> skills; //보유스킬 -> enum으로?

    //멘토링 정보는 멘토링 엔티티에
    @Column
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentoring_id", nullable = false)
    private List<Mentoring> mentoring;

    @Column(nullable = false)
    private boolean termsAndConditions; //이용약관

    @Column(nullable = false)
    private boolean consentVerified; //사실 여부 확인

    public enum Role {
        USER, ADMIN, OPERATOR
    } //role 추가



}
