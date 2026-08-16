package nova.mjs.domain.thingo.department.profile;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.department.directory.DepartmentDirectoryCatalog;
import nova.mjs.domain.thingo.department.directory.DepartmentDirectorySyncService;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import nova.mjs.domain.thingo.department.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentProfileSyncService {

    private final DepartmentDirectoryCatalog catalog;
    private final DepartmentDirectorySyncService directorySyncService;
    private final DepartmentRepository departmentRepository;
    private final OfficialDepartmentProfileCrawler crawler;
    private final DepartmentProfilePersistenceService persistenceService;

    public SyncResponse sync(College college, DepartmentName departmentName) {
        if (college == null && departmentName != null) {
            throw new IllegalArgumentException("department는 college와 함께 전달해야 합니다.");
        }

        directorySyncService.syncAll();
        List<DepartmentDirectoryCatalog.Entry> targets = catalog.entries().stream()
                .filter(entry -> college == null || entry.college() == college)
                .filter(entry -> departmentName == null || entry.departmentName() == departmentName)
                .toList();
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("요청한 단과대·학과 조합이 디렉터리에 없습니다.");
        }

        List<Item> items = new ArrayList<>();
        for (DepartmentDirectoryCatalog.Entry entry : targets) {
            Department department = findDepartment(entry);
            OfficialDepartmentProfileCrawler.CrawlResult result = crawler.crawl(entry);
            persistenceService.save(department, result);
            items.add(new Item(entry.college(), entry.departmentName(), entry.displayName(),
                    result.status(), result.message(), result.verifiedAt()));
        }

        long complete = items.stream().filter(item -> "COMPLETE".equals(item.status())).count();
        long partial = items.stream().filter(item -> "PARTIAL".equals(item.status())).count();
        long failed = items.size() - complete - partial;
        return new SyncResponse(items.size(), complete, partial, failed, items);
    }

    private Department findDepartment(DepartmentDirectoryCatalog.Entry entry) {
        return entry.departmentName() == null
                ? departmentRepository.findCollegeLevelDepartment(entry.college()).orElseThrow()
                : departmentRepository.findByCollegeAndDepartmentName(
                        entry.college(), entry.departmentName()).orElseThrow();
    }

    public record SyncResponse(
            int total,
            long complete,
            long partial,
            long failed,
            List<Item> items
    ) {
    }

    public record Item(
            College college,
            DepartmentName departmentName,
            String displayName,
            String status,
            String message,
            java.time.LocalDateTime verifiedAt
    ) {
    }
}
