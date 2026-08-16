package nova.mjs.domain.thingo.department.search;

import lombok.RequiredArgsConstructor;
import nova.mjs.domain.thingo.department.directory.DepartmentDirectoryCatalog;
import nova.mjs.domain.thingo.department.entity.Department;
import nova.mjs.domain.thingo.department.entity.DepartmentNotice;
import nova.mjs.domain.thingo.department.entity.DepartmentProfile;
import nova.mjs.domain.thingo.department.entity.DepartmentSchedule;
import nova.mjs.domain.thingo.department.repository.DepartmentNoticeRepository;
import nova.mjs.domain.thingo.department.repository.DepartmentProfileRepository;
import nova.mjs.domain.thingo.department.repository.DepartmentRepository;
import nova.mjs.domain.thingo.department.repository.DepartmentScheduleRepository;
import nova.mjs.domain.thingo.search.academic.AcademicGuideCatalog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static nova.mjs.domain.thingo.department.search.DepartmentAiSearchDTO.BlockType;
import static nova.mjs.domain.thingo.department.search.DepartmentAiSearchDTO.Category;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentAiSearchService {

    private static final int DEFAULT_LIMIT = 5;

    private final DepartmentDirectoryCatalog directoryCatalog;
    private final DepartmentRepository departmentRepository;
    private final DepartmentProfileRepository profileRepository;
    private final DepartmentNoticeRepository noticeRepository;
    private final DepartmentScheduleRepository scheduleRepository;
    private final AcademicGuideCatalog academicGuideCatalog;

    public DepartmentAiSearchDTO.Response search(String query, Category requestedCategory) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("query는 필수입니다.");
        }

        Category category = resolveCategory(query, requestedCategory);
        DepartmentDirectoryCatalog.Entry entry = directoryCatalog.resolve(query).orElse(null);
        if (entry == null) {
            return DepartmentAiSearchDTO.Response.builder()
                    .query(query)
                    .category(category)
                    .blocks(List.of(textBlock("검색 결과", "질문에서 단과대 또는 학과를 식별하지 못했습니다.")))
                    .build();
        }

        Department department = findDepartment(entry);
        DepartmentProfile profile = department == null
                ? null : profileRepository.findByDepartment(department).orElse(null);
        List<DepartmentAiSearchDTO.Block> blocks = switch (category) {
            case BASIC -> basicBlocks(entry, department, profile);
            case FOUNDATION -> foundationBlocks(entry);
            case MAJOR -> majorBlocks(entry, profile);
            case EVENT -> eventBlocks(entry, department);
            case AUTO -> throw new IllegalStateException("AUTO category must be resolved");
        };

        return DepartmentAiSearchDTO.Response.builder()
                .query(query)
                .category(category)
                .entity(toEntityRef(entry))
                .blocks(blocks)
                .build();
    }

    private List<DepartmentAiSearchDTO.Block> basicBlocks(
            DepartmentDirectoryCatalog.Entry entry,
            Department department,
            DepartmentProfile profile
    ) {
        String phone = department == null ? entry.academicOfficePhone() : department.getAcademicOfficePhone();
        String instagram = department == null ? entry.instagramUrl() : department.getInstagramUrl();
        String homepage = department == null ? entry.homepageUrl() : department.getHomepageUrl();

        List<DepartmentAiSearchDTO.Block> blocks = new ArrayList<>();
        blocks.add(DepartmentAiSearchDTO.Block.builder()
                .type(BlockType.PROFILE_CARD)
                .title(entry.displayName())
                .profile(DepartmentAiSearchDTO.ProfileCard.builder()
                        .academicOfficePhone(phone)
                        .instagramUrl(instagram)
                        .homepageUrl(homepage)
                        .collectionStatus(profile == null ? "PENDING" : profile.getCollectionStatus())
                        .verifiedAt(profile == null ? null : profile.getVerifiedAt())
                        .build())
                .build());

        if (profile != null && hasText(profile.getIntroduction())) {
            blocks.add(textBlock("소개", profile.getIntroduction()));
        } else {
            blocks.add(textBlock("소개", "공식 학과 소개를 수집 중입니다. 연락처와 공식 링크는 바로 이용할 수 있습니다."));
        }
        blocks.add(sourceBlock(profile, homepage));
        return blocks;
    }

    private List<DepartmentAiSearchDTO.Block> foundationBlocks(DepartmentDirectoryCatalog.Entry entry) {
        String departmentLabel = entry.displayName();
        List<DepartmentAiSearchDTO.Item> items = academicGuideCatalog.documents().stream()
                .filter(document -> DepartmentDirectoryCatalog.normalize(document.content())
                        .contains(DepartmentDirectoryCatalog.normalize(departmentLabel)))
                .limit(DEFAULT_LIMIT)
                .map(document -> DepartmentAiSearchDTO.Item.builder()
                        .title(document.title())
                        .description(snippet(document.content()))
                        .url(document.link())
                        .date(document.instant() == null ? null : LocalDateTime.ofInstant(
                                document.instant(), ZoneId.systemDefault()))
                        .sourceType("ACADEMIC_GUIDE")
                        .build())
                .toList();

        String message = items.isEmpty()
                ? "해당 학과에 적용되는 학문기초교양 자료를 찾지 못했습니다."
                : "적용 학번과 학과 범위를 확인한 학사안내 자료입니다.";
        return List.of(
                textBlock("학문기초교양", message),
                DepartmentAiSearchDTO.Block.builder()
                        .type(BlockType.COURSE_LIST)
                        .title("관련 학사안내")
                        .items(items)
                        .build());
    }

    private List<DepartmentAiSearchDTO.Block> majorBlocks(
            DepartmentDirectoryCatalog.Entry entry,
            DepartmentProfile profile
    ) {
        if (profile == null) {
            return List.of(textBlock("전공 검색", "공식 전공·교과과정 정보를 수집 중입니다."));
        }

        List<DepartmentAiSearchDTO.Block> blocks = new ArrayList<>();
        if (hasText(profile.getEducationalGoals())) {
            blocks.add(textBlock("교육 목표", profile.getEducationalGoals()));
        }
        if (hasText(profile.getMajorCurriculum())) {
            blocks.add(textBlock("전공 교과과정", profile.getMajorCurriculum()));
        }
        if (hasText(profile.getCareerPaths())) {
            blocks.add(textBlock("졸업 후 진로", profile.getCareerPaths()));
        }
        if (blocks.isEmpty()) {
            blocks.add(textBlock("전공 검색", entry.displayName() + "의 공식 전공 정보를 수집 중입니다."));
        }
        blocks.add(sourceBlock(profile, entry.homepageUrl()));
        return blocks;
    }

    private List<DepartmentAiSearchDTO.Block> eventBlocks(
            DepartmentDirectoryCatalog.Entry entry,
            Department department
    ) {
        if (department == null) {
            return List.of(textBlock("학과 이벤트", "학과 데이터가 아직 동기화되지 않았습니다."));
        }

        Page<DepartmentNotice> noticePage = entry.departmentName() == null
                ? noticeRepository.findByDepartmentOrderByDateDesc(department, PageRequest.of(0, DEFAULT_LIMIT))
                : noticeRepository.findCollegeAndDepartmentLevelNotices(
                        entry.college(), entry.departmentName(), PageRequest.of(0, DEFAULT_LIMIT));
        List<DepartmentAiSearchDTO.Item> items = new ArrayList<>(noticePage.stream()
                .map(notice -> DepartmentAiSearchDTO.Item.builder()
                        .title(notice.getTitle())
                        .url(notice.getLink())
                        .date(notice.getDate())
                        .sourceType("DEPARTMENT_NOTICE")
                        .build())
                .toList());

        scheduleRepository.findByDepartment(department).stream()
                .sorted(Comparator.comparing(DepartmentSchedule::getStartDate).reversed())
                .limit(DEFAULT_LIMIT)
                .map(schedule -> DepartmentAiSearchDTO.Item.builder()
                        .title(schedule.getTitle())
                        .description(schedule.getContent())
                        .date(schedule.getStartDate().atStartOfDay())
                        .sourceType("DEPARTMENT_SCHEDULE")
                        .build())
                .forEach(items::add);
        items.sort(Comparator.comparing(
                DepartmentAiSearchDTO.Item::getDate,
                Comparator.nullsLast(Comparator.reverseOrder())));
        if (items.size() > DEFAULT_LIMIT) items = new ArrayList<>(items.subList(0, DEFAULT_LIMIT));

        return List.of(DepartmentAiSearchDTO.Block.builder()
                .type(BlockType.EVENT_LIST)
                .title(entry.displayName() + " 최신 소식")
                .items(items)
                .build());
    }

    private DepartmentAiSearchDTO.Block sourceBlock(DepartmentProfile profile, String homepage) {
        Map<String, String> sources = new LinkedHashMap<>();
        putSource(sources, "공식 홈페이지", homepage);
        if (profile != null) {
            putSource(sources, "소개", profile.getIntroductionSourceUrl());
            putSource(sources, "교육 목표", profile.getEducationalGoalsSourceUrl());
            putSource(sources, "진로", profile.getCareerPathsSourceUrl());
            putSource(sources, "교과과정", profile.getMajorCurriculumSourceUrl());
        }
        List<DepartmentAiSearchDTO.Item> items = sources.entrySet().stream()
                .map(source -> DepartmentAiSearchDTO.Item.builder()
                        .title(source.getKey())
                        .url(source.getValue())
                        .sourceType("OFFICIAL_DEPARTMENT_SITE")
                        .build())
                .toList();
        return DepartmentAiSearchDTO.Block.builder()
                .type(BlockType.SOURCE_LIST)
                .title("공식 출처")
                .items(items)
                .build();
    }

    private void putSource(Map<String, String> sources, String title, String url) {
        if (hasText(url)) sources.putIfAbsent(title, url);
    }

    private Department findDepartment(DepartmentDirectoryCatalog.Entry entry) {
        return entry.departmentName() == null
                ? departmentRepository.findCollegeLevelDepartment(entry.college()).orElse(null)
                : departmentRepository.findByCollegeAndDepartmentName(
                        entry.college(), entry.departmentName()).orElse(null);
    }

    private Category resolveCategory(String query, Category requested) {
        if (requested != null && requested != Category.AUTO) return requested;
        String normalized = DepartmentDirectoryCatalog.normalize(query);
        String intent = directoryCatalog.resolve(query)
                .map(entry -> normalized.replace(
                        DepartmentDirectoryCatalog.normalize(entry.displayName()), ""))
                .orElse(normalized);
        if (containsAny(intent, "학문기초교양", "기초교양", "학문기초")) return Category.FOUNDATION;
        if (containsAny(intent, "행사", "이벤트", "공지", "일정", "설명회")) return Category.EVENT;
        if (containsAny(intent, "교과", "교과목", "전공과목", "교육목표", "교육과정", "커리큘럼", "진로")) {
            return Category.MAJOR;
        }
        return Category.BASIC;
    }

    private boolean containsAny(String value, String... candidates) {
        for (String candidate : candidates) {
            if (value.contains(DepartmentDirectoryCatalog.normalize(candidate))) return true;
        }
        return false;
    }

    private DepartmentAiSearchDTO.EntityRef toEntityRef(DepartmentDirectoryCatalog.Entry entry) {
        String id = entry.departmentName() == null
                ? "COLLEGE:" + entry.college()
                : "DEPARTMENT:" + entry.college() + ":" + entry.departmentName();
        return DepartmentAiSearchDTO.EntityRef.builder()
                .id(id)
                .college(entry.college())
                .departmentName(entry.departmentName())
                .collegeLabel(entry.collegeLabel())
                .departmentLabel(entry.departmentLabel())
                .displayName(entry.displayName())
                .build();
    }

    private DepartmentAiSearchDTO.Block textBlock(String title, String text) {
        return DepartmentAiSearchDTO.Block.builder()
                .type(BlockType.TEXT_ANSWER)
                .title(title)
                .text(text)
                .build();
    }

    private String snippet(String content) {
        if (content == null) return null;
        String normalized = content.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 500 ? normalized : normalized.substring(0, 500) + "…";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
