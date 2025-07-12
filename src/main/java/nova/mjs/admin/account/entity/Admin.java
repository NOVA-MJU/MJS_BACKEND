package nova.mjs.admin.account.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nova.mjs.department.entity.Department;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "department_id", unique = true)
    private Department department;

    public enum Role {
        ADMIN
    }



    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
