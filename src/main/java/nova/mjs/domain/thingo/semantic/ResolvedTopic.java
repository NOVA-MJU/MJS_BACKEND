package nova.mjs.domain.thingo.semantic;

/** 입력에서 인식한 Topic과 실제 매칭 별칭. */
public record ResolvedTopic(TopicDefinition topic, String matchedAlias, int priority) {
}
