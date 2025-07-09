package nova.mjs.admin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nova.mjs.admin.DTO.AdminDTO;
import nova.mjs.util.entity.BaseEntity;
import java.util.UUID;


@Entity
@Table(name = "admin")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Admin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String adminId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String studentUnionName;

    @Column(nullable = false)
    private String department;

    private String logoImageUrl;
    private String instagramUrl;
    private String homepageUrl;
    private String introduction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Admin.Role role;


    public enum Role {
        ADMIN
    }

    public static Admin create(
            String adminId,
            String department,
            String introduction,
            String homepageUrl,
            String instagramUrl,
            String logoImageUrl,
            String studentUnionName
    ) {
        return Admin.builder()
                .adminId(adminId)
                .uuid(UUID.randomUUID())
                .password("") // 비밀번호는 나중에 별도로 설정
                .studentUnionName(studentUnionName)
                .department(department)
                .logoImageUrl(logoImageUrl)
                .instagramUrl(instagramUrl)
                .homepageUrl(homepageUrl)
                .introduction(introduction)
                .role(Role.ADMIN)
                .build();
    }


    public void update(String encodedPassword, AdminDTO dto, String logoImage) {
        this.password = encodedPassword;
        this.studentUnionName = dto.getStudentUnionName();
        this.logoImageUrl = dto.getLogoImageUrl();
        this.instagramUrl = dto.getInstagramUrl();
        this.homepageUrl = dto.getHomepageUrl();
        this.introduction = dto.getIntroduction();
        this.role = Role.ADMIN;
    }
}
