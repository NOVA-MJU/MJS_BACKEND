package nova.mjs.domain.thingo.department.directory;

import nova.mjs.domain.thingo.department.entity.enumList.College;
import nova.mjs.domain.thingo.department.entity.enumList.DepartmentName;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Component
public class DepartmentDirectoryCatalog {

    private static final String RESOURCE_PATH = "department/department_directory.psv";
    private final List<Entry> entries;

    public DepartmentDirectoryCatalog() {
        this.entries = load();
    }

    public List<Entry> entries() {
        return entries;
    }

    public Optional<Entry> find(College college, DepartmentName departmentName) {
        return entries.stream()
                .filter(entry -> entry.college() == college && entry.departmentName() == departmentName)
                .findFirst();
    }

    public Optional<Entry> resolve(String rawQuery) {
        String query = normalize(rawQuery);
        if (query.isBlank()) return Optional.empty();

        return entries.stream()
                .filter(entry -> query.contains(entry.searchLabel())
                        || matchesSuffixOmittedLabel(query, entry)
                        || (entry.departmentName() == null
                            && query.contains(normalize(entry.college().name())))
                        || (entry.departmentName() != null
                            && query.contains(normalize(entry.departmentName().name()))))
                .max(Comparator.comparingInt(entry -> matchScore(query, entry)));
    }

    private int matchScore(String query, Entry entry) {
        boolean labelMatch = query.contains(entry.searchLabel());
        if (entry.departmentName() != null && labelMatch) {
            return 100_000 + entry.searchLabel().length();
        }
        if (entry.departmentName() != null
                && query.contains(normalize(entry.departmentName().name()))) {
            return 50_000 + entry.searchLabel().length();
        }
        if (matchesSuffixOmittedLabel(query, entry)) {
            return (entry.departmentName() == null ? 750 : 75_000) + entry.searchLabel().length();
        }
        return 1_000 + entry.searchLabel().length();
    }

    private boolean matchesSuffixOmittedLabel(String query, Entry entry) {
        String baseLabel = entry.searchLabel().replaceFirst("(전공|학과|학부|대학)$", "");
        // '경영', '경제'처럼 지나치게 짧은 일반어가 학과 검색으로 오인되는 것을 막는다.
        return baseLabel.length() >= 4 && query.contains(baseLabel);
    }

    public static String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^0-9a-z가-힣]", "");
    }

    private List<Entry> load() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource(RESOURCE_PATH).getInputStream(), StandardCharsets.UTF_8))) {
            List<Entry> loaded = reader.lines()
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .map(this::parse)
                    .toList();

            Set<String> keys = new HashSet<>();
            for (Entry entry : loaded) {
                String key = entry.college() + ":" + entry.departmentName();
                if (!keys.add(key)) {
                    throw new IllegalStateException("Duplicate department directory key: " + key);
                }
            }
            return loaded;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load department directory: " + RESOURCE_PATH, e);
        }
    }

    private Entry parse(String line) {
        String[] values = line.split("\\|", -1);
        if (values.length != 7) {
            throw new IllegalStateException("Invalid department directory row: " + line);
        }
        College college = College.valueOf(values[2]);
        DepartmentName departmentName = values[3].isBlank() ? null : DepartmentName.valueOf(values[3]);
        String label = values[1].isBlank() ? values[0] : values[1];
        return new Entry(
                values[0], blankToNull(values[1]), college, departmentName,
                blankToNull(values[4]), blankToNull(values[5]), blankToNull(values[6]), normalize(label));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record Entry(
            String collegeLabel,
            String departmentLabel,
            College college,
            DepartmentName departmentName,
            String instagramUrl,
            String homepageUrl,
            String academicOfficePhone,
            String searchLabel
    ) {
        public String displayName() {
            return departmentLabel == null ? collegeLabel : departmentLabel;
        }
    }
}
