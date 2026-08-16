package nova.mjs.domain.thingo.department.profile;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.DepartmentProfile;
import nova.mjs.domain.thingo.department.repository.DepartmentProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepartmentProfilePersistenceService {

    private final DepartmentProfileRepository profileRepository;

    @Transactional
    public void save(Department department, OfficialDepartmentProfileCrawler.CrawlResult result) {
        DepartmentProfile profile = profileRepository.findByDepartment(department)
                .orElseGet(() -> DepartmentProfile.create(department));
        profile.updateOfficialContent(
                result.introduction(), result.educationalGoals(), result.careerPaths(),
                result.majorCurriculum(), result.introductionSourceUrl(),
                result.educationalGoalsSourceUrl(), result.careerPathsSourceUrl(),
                result.majorCurriculumSourceUrl(), result.status(), result.message(),
                result.verifiedAt());
        profileRepository.save(profile);
    }
}
