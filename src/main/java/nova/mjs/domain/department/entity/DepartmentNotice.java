package nova.mjs.domain.department.entity;

import jakarta.persistence.*;
import lombok.*;
import nova.mjs.util.ElasticSearch.EntityListner.DepartmentEntityListener;
import nova.mjs.util.entity.BaseEntity;

import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(DepartmentEntityListener.class)
@Table(name = "department_notices")
public class DepartmentNotice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;


    @Column(nullable = false)
    private String title;

    @Column
    private String content;

}