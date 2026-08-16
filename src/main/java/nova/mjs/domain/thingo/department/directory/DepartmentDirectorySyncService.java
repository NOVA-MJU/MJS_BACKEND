package nova.mjs.domain.thingo.department.directory;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepartmentDirectorySyncService {

    private final DepartmentDirectoryCatalog catalog;
    private final DepartmentRepository departmentRepository;

    @Transactional
    public SyncResult syncAll() {
        departmentRepository.alignIdSequence();
        int created = 0;
        int updated = 0;

        for (DepartmentDirectoryCatalog.Entry entry : catalog.entries()) {
            Department department = entry.departmentName() == null
                    ? departmentRepository.findCollegeLevelDepartment(entry.college()).orElse(null)
                    : departmentRepository.findByCollegeAndDepartmentName(
                            entry.college(), entry.departmentName()).orElse(null);

            if (department == null) {
                department = Department.builder()
                        .college(entry.college())
                        .departmentName(entry.departmentName())
                        .academicOfficePhone(entry.academicOfficePhone())
                        .instagramUrl(entry.instagramUrl())
                        .homepageUrl(entry.homepageUrl())
                        .build();
                departmentRepository.save(department);
                created++;
            } else {
                department.syncDirectoryInfo(
                        entry.academicOfficePhone(), entry.instagramUrl(), entry.homepageUrl());
                updated++;
            }
        }
        return new SyncResult(catalog.entries().size(), created, updated);
    }

    public record SyncResult(int total, int created, int updated) {
    }
}
