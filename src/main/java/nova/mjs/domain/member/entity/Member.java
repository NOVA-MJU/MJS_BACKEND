package nova.mjs.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.admin.account.entity.StudentCouncilAdmin;
import nova.mjs.domain.member.DTO.MemberDTO;
import nova.mjs.domain.member.entity.enumList.College;
import nova.mjs.domain.member.entity.enumList.DepartmentName;
import nova.mjs.util.entity.BaseEntity;

import java.util.UUID;

// Entity
@Entity
@Table(name = "member")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Enumerated(EnumType.STRING) // `role` 필드 추가
    @Column(nullable = false)
    private Role role;  // Role enum 타입으로 설정

    @Column(nullable = false)
    private String name;

    @Column
    private String profileImageUrl;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private StudentCouncilAdmin studentCouncilProfile; // 있는 경우만 연결됨

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "department_name", nullable = false)
    private DepartmentName departmentName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private College college;

    private Integer studentNumber;

    public enum Gender {
        MALE, FEMALE, OTHERS;

        public static Gender fromString(String value) {
            try {
                return Gender.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new IllegalArgumentException("Invalid gender value: " + value);
            }
        }
    }

    public enum Role {
        USER, ADMIN, DEVELOPER
    }

    public static Member create(MemberDTO.MemberRegistrationRequestDTO memberDTO, String encodePassword) {
        return Member.builder()
                .uuid(UUID.randomUUID()) // UUID 자동 생성
                .name(memberDTO.getName())
                .email(memberDTO.getEmail())
                .password(encodePassword)
                .gender(Gender.fromString(memberDTO.getGender())) // 대소문자 변환
                .nickname(memberDTO.getNickname())
                .departmentName(memberDTO.getDepartmentName())
                .college(memberDTO.getCollege())
                .studentNumber(memberDTO.getStudentNumber())
                .role(Role.USER)
                .build();
    }

    public static Member createStudentCouncilInitProfile(MemberDTO.StudentCouncilRegistrationRequestDTO requestDTO) {
        // 초기 관리자 회원가입

        return Member.builder()
                .uuid(UUID.randomUUID())
                .email(requestDTO.getEmail())
                .role(Role.ADMIN)
                .build();
    }

    public void update(MemberDTO memberDTO) {
        this.name = getOrDefault(memberDTO.getName(), this.name);
        this.email = getOrDefault(memberDTO.getEmail(), this.email);
        this.nickname = getOrDefault(memberDTO.getNickname(), this.nickname);
        this.departmentName = getOrDefault(memberDTO.getDepartmentName(), this.departmentName);
        this.college = getOrDefault(memberDTO.getCollege(), this.college);
        this.studentNumber = getOrDefault(memberDTO.getStudentNumber(), this.studentNumber);
        this.gender = memberDTO.getGender() != null ? Gender.fromString(memberDTO.getGender()) : this.gender;
    }

    public void updatePassword(String encodedNewPassword) {
        this.password = encodedNewPassword;
    }

    private <T> T getOrDefault(T newValue, T currentValue) {
        return newValue != null ? newValue : currentValue;
    }
}
