package nova.mjs.domain.thingo.semantic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 검색과 알림이 함께 사용하는 공지 Topic·별칭·계층의 단일 원천.
 * 검색 순위나 알림 발송 정책은 이 컴포넌트에 두지 않는다.
 */
@Component
public class TopicCatalog {

    private static final String RESOURCE_PATH = "semantic/notice_topic_catalog.json";

    private final int version;
    private final List<TopicDefinition> topics;
    private final List<TopicAliasDefinition> aliases;
    private final Map<String, TopicDefinition> topicById;

    public TopicCatalog() {
        CatalogFile file = load();
        this.version = file.version();
        this.topics = file.topics() == null ? List.of() : List.copyOf(file.topics());
        this.aliases = file.aliases() == null ? List.of() : List.copyOf(file.aliases());
        this.topicById = topics.stream().collect(Collectors.toUnmodifiableMap(
                TopicDefinition::topicId, Function.identity()));
        validate();
    }

    public int version() {
        return version;
    }

    public Optional<TopicDefinition> find(String topicId) {
        return Optional.ofNullable(topicById.get(topicId));
    }

    public boolean isGroup(String topicId) {
        return topics.stream().anyMatch(topic -> topicId.equals(topic.parentTopicId()) && topic.enabled());
    }

    public List<ResolvedTopic> resolve(String rawText) {
        return resolve(rawText, Integer.MIN_VALUE);
    }

    /**
     * 본문 보조 분류처럼 고신뢰 별칭만 허용해야 할 때 최소 우선순위를 적용한다.
     * 검색 질의와 제목 분류는 기존 {@link #resolve(String)}를 사용한다.
     */
    public List<ResolvedTopic> resolve(String rawText, int minimumPriority) {
        String inputKey = SemanticTextNormalizer.lookupKey(rawText);
        if (inputKey.isBlank()) {
            return List.of();
        }

        Map<String, ResolvedTopic> bestByTopic = aliases.stream()
                .filter(alias -> alias.priority() >= minimumPriority)
                .filter(alias -> matchesAlias(rawText, inputKey, alias.alias()))
                .map(alias -> new ResolvedTopic(topicById.get(alias.topicId()), alias.alias(), alias.priority()))
                .filter(match -> match.topic() != null && match.topic().enabled())
                .collect(Collectors.toMap(
                        match -> match.topic().topicId(),
                        Function.identity(),
                        (left, right) -> left.priority() >= right.priority() ? left : right));

        Set<String> mostSpecificTopicIds = bestByTopic.keySet().stream()
                .filter(candidate -> bestByTopic.keySet().stream()
                        .noneMatch(other -> !candidate.equals(other) && isDescendant(other, candidate)))
                .collect(Collectors.toSet());

        return bestByTopic.values().stream()
                .filter(match -> mostSpecificTopicIds.contains(match.topic().topicId()))
                .sorted(Comparator.comparingInt(ResolvedTopic::priority).reversed()
                        .thenComparing(match -> match.topic().topicId()))
                .toList();
    }

    public List<TopicDefinition> autocomplete(String query, int limit, boolean subscribableOnly) {
        String key = SemanticTextNormalizer.lookupKey(query);
        if (key.isBlank() || limit <= 0) {
            return List.of();
        }

        Set<String> matchedIds = new LinkedHashSet<>();
        topics.stream()
                .filter(TopicDefinition::enabled)
                .filter(topic -> !subscribableOnly || topic.subscribable())
                .filter(topic -> SemanticTextNormalizer.lookupKey(topic.displayName()).contains(key))
                .forEach(topic -> matchedIds.add(topic.topicId()));
        aliases.stream()
                .filter(alias -> SemanticTextNormalizer.lookupKey(alias.alias()).contains(key))
                .sorted(Comparator.comparingInt(TopicAliasDefinition::priority).reversed())
                .forEach(alias -> matchedIds.add(alias.topicId()));

        return matchedIds.stream()
                .map(topicById::get)
                .filter(topic -> topic != null && topic.enabled())
                .filter(topic -> !subscribableOnly || topic.subscribable())
                .sorted(Comparator.comparing((TopicDefinition topic) -> topic.parentTopicId() != null)
                        .thenComparing(TopicDefinition::displayName))
                .limit(limit)
                .toList();
    }

    /** 상위 Topic 구독은 모든 활성 하위 Topic을 포함한다. */
    public Set<String> inclusiveTopicIds(String topicId) {
        if (!topicById.containsKey(topicId)) {
            return Set.of();
        }
        LinkedHashSet<String> result = new LinkedHashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(topicId);
        while (!queue.isEmpty()) {
            String current = queue.removeFirst();
            if (!result.add(current)) {
                continue;
            }
            topics.stream()
                    .filter(TopicDefinition::enabled)
                    .filter(topic -> current.equals(topic.parentTopicId()))
                    .map(TopicDefinition::topicId)
                    .forEach(queue::addLast);
        }
        return Set.copyOf(result);
    }

    /** 분류 결과에는 직접 Topic과 모든 상위 Topic을 함께 저장한다. */
    public Set<String> withAncestors(Set<String> directTopicIds) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String directId : directTopicIds) {
            String current = directId;
            while (current != null && result.add(current)) {
                TopicDefinition topic = topicById.get(current);
                current = topic == null ? null : topic.parentTopicId();
            }
        }
        return Set.copyOf(result);
    }

    private void validate() {
        List<String> errors = new ArrayList<>();
        for (TopicDefinition topic : topics) {
            if (topic.parentTopicId() != null && !topicById.containsKey(topic.parentTopicId())) {
                errors.add(topic.topicId() + " -> missing parent " + topic.parentTopicId());
            }
        }
        for (TopicAliasDefinition alias : aliases) {
            if (!topicById.containsKey(alias.topicId())) {
                errors.add(alias.alias() + " -> missing topic " + alias.topicId());
            }
            if (SemanticTextNormalizer.lookupKey(alias.alias()).length() < 2) {
                errors.add(alias.alias() + " -> one-letter aliases are not allowed");
            }
        }
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Invalid topic catalog: " + String.join(", ", errors));
        }
    }

    private boolean isDescendant(String candidateId, String ancestorId) {
        TopicDefinition candidate = topicById.get(candidateId);
        String parentId = candidate == null ? null : candidate.parentTopicId();
        while (parentId != null) {
            if (ancestorId.equals(parentId)) {
                return true;
            }
            TopicDefinition parent = topicById.get(parentId);
            parentId = parent == null ? null : parent.parentTopicId();
        }
        return false;
    }

    private boolean matchesAlias(String rawText, String inputKey, String alias) {
        String aliasKey = SemanticTextNormalizer.lookupKey(alias);
        if (aliasKey.length() < 2) {
            return false;
        }
        if (alias.matches("[A-Za-z]{2,6}")) {
            Pattern acronym = Pattern.compile(
                    "(?i)(?<![A-Za-z0-9])" + Pattern.quote(alias) + "(?![A-Za-z0-9])");
            return acronym.matcher(rawText == null ? "" : rawText).find();
        }
        return inputKey.contains(aliasKey);
    }

    private CatalogFile load() {
        try (InputStream input = TopicCatalog.class.getClassLoader().getResourceAsStream(RESOURCE_PATH)) {
            if (input == null) {
                throw new IllegalStateException("Topic catalog not found: " + RESOURCE_PATH);
            }
            return new ObjectMapper().readValue(input, CatalogFile.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load topic catalog", e);
        }
    }

    private record CatalogFile(int version,
                               List<TopicDefinition> topics,
                               List<TopicAliasDefinition> aliases) {
    }
}
