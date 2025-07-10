package nova.mjs.admin.account.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @Column(nullable = false)
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

    public void updateInfo(String department, String studentUnionName, String homepageUrl, String instagramUrl, String introduction, String logoImageUrl) {
        this.department = department;
        this.studentUnionName = studentUnionName;
        this.homepageUrl = homepageUrl;
        this.instagramUrl = instagramUrl;
        this.introduction = introduction;
        this.logoImageUrl = logoImageUrl;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
