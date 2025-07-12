package nova.mjs.admin.account.entity;   // ← 패키지도 admin.account → studentcouncil 로 변경

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nova.mjs.domain.department.entity.Department;
import nova.mjs.domain.member.entity.Member;

/**
 * 학과별 학생회(관리자) 엔티티
 *
 * - Member 를 상속(JOINED)하여 공통 정보‧인증 처리
 * - 학과당 1개의 학생회만 존재하도록 department_id UNIQUE
 */
@Entity
@Table(name = "student_council")           // 테이블 이름도 명확히
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentCouncilAdmin{

    @Id
    private Long id;

    @OneToOne
    @MapsId
    private Member member;

    /** 학생회 공식 연락 이메일 */
    @Column(name = "contact_email", nullable = false, unique = true)
    private String contactEmail;

    /** 담당 학과 (학과당 학생회 1개) */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "department_id", nullable = false, unique = true)
    private Department department;

    /* --------------------- 비즈니스 메서드 --------------------- */

    /** 연락 이메일 변경 */
    public void updateContactEmail(String newEmail) {
        this.contactEmail = newEmail;
    }

    /** 담당 학과 교체 */
    public void changeDepartment(Department newDept) {
        this.department = newDept;
    }
}
