package nova.mjs.domain.thingo.department.profile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 공개 HTTP 크롤링 엔드포인트 대신 운영자가 배포 설정으로 명시적으로 켤 때만 실행한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "mju.department-profile.sync-on-startup",
        havingValue = "true")
public class DepartmentProfileStartupSyncRunner implements ApplicationRunner {

    private final DepartmentProfileSyncService syncService;

    @Override
    public void run(ApplicationArguments args) {
        DepartmentProfileSyncService.SyncResponse response = syncService.sync(null, null);
        log.info("Department profile startup sync complete. total={}, complete={}, partial={}, failed={}",
                response.total(), response.complete(), response.partial(), response.failed());
    }
}
