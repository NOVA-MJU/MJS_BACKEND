package nova.mjs.domain.mentorship.communication.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.domain.mentorship.communication.event.ChatMessageEvent;
import nova.mjs.domain.mentorship.communication.service.ChatMessagePersistenceService;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRedisStreamConsumer {

    //consumer는 redis stream에 쌓인 메시지를 읽어서 처리하는 작업자 - producer : 메시지를 넣는 쪽 / consumer : 메시지를 꺼내 처리하는 쪽
    //consumer : 새로 들어온 메시지 읽기 / 이전에 읽었지만 ACK 못한 pending 메시지 복구 처리
    //pending : consumer가 읽었지만 아직 ACK 되진 않은 메시지 - 유실 방지를 위함
    //pending 먼저 확인 -> 없으면 새 메시지 읽기(새로운 stream 레코드 가져옴) -> record를 메시지 이벤트 객체로 변환 -> mongo batch upsert -> 성공한 것만 ACK
    //mongo 반영이 성공한 뒤에만 ACK

    private final StringRedisTemplate stringRedisTemplate;
    private final ChatMessagePersistenceService chatMessagePersistenceService;

    @Scheduled(fixedDelay = 300)
    public void consume() {
        // 1. pending 먼저 복구 시도
        List<MapRecord<String, Object, Object>> pendingRecords = stringRedisTemplate.opsForStream().read(
                Consumer.from(
                        ChatRedisStreamConstants.GROUP_NAME,
                        ChatRedisStreamConstants.CONSUMER_NAME
                ),
                StreamReadOptions.empty().count(ChatRedisStreamConstants.BATCH_SIZE),
                StreamOffset.create(
                        ChatRedisStreamConstants.STREAM_KEY,
                        ReadOffset.from("0")
                )
        );

        if (pendingRecords != null && !pendingRecords.isEmpty()) {
            processRecords(pendingRecords);
            return;
        }

        // 2. 새 메시지 읽기
        List<MapRecord<String, Object, Object>> newRecords = stringRedisTemplate.opsForStream().read(
                Consumer.from(
                        ChatRedisStreamConstants.GROUP_NAME,
                        ChatRedisStreamConstants.CONSUMER_NAME
                ),
                StreamReadOptions.empty()
                        .count(ChatRedisStreamConstants.BATCH_SIZE)
                        .block(Duration.ofMillis(200)),
                StreamOffset.create(
                        ChatRedisStreamConstants.STREAM_KEY,
                        ReadOffset.lastConsumed()
                )
        );

        if (newRecords == null || newRecords.isEmpty()) {
            return;
        }

        processRecords(newRecords);
    }

    private void processRecords(List<MapRecord<String, Object, Object>> records) {
        List<ChatMessageEvent> events = new ArrayList<>();
        List<RecordId> ackTargets = new ArrayList<>();

        for (MapRecord<String, Object, Object> record : records) {
            try {
                ChatMessageEvent event = toEvent(record.getValue());
                if (event == null) {
                    ackTargets.add(record.getId());
                    continue;
                }

                events.add(event);
                ackTargets.add(record.getId());
            } catch (Exception e) {
                log.error("Redis Stream 레코드 파싱 실패. recordId={}", record.getId(), e);
                // 파싱 실패 레코드는 바로 ACK 하지 않음
                // 운영 단계에서는 DLQ 이동을 권장
            }
        }

        if (events.isEmpty()) {
            return;
        }

        // Mongo 반영 성공 후에만 ACK
        chatMessagePersistenceService.persistBatch(events);

        if (!ackTargets.isEmpty()) {
            stringRedisTemplate.opsForStream().acknowledge(
                    ChatRedisStreamConstants.STREAM_KEY,
                    ChatRedisStreamConstants.GROUP_NAME,
                    ackTargets.toArray(new RecordId[0])
            );
        }
    }

    private ChatMessageEvent toEvent(Map<Object, Object> value) {
        Object messageId = value.get("messageId");
        if (messageId == null) {
            // stream init control message 등은 무시
            return null;
        }

        return ChatMessageEvent.builder()
                .messageId((String) messageId)
                .chatUuid(UUID.fromString((String) value.get("chatUuid")))
                .senderUuid(UUID.fromString((String) value.get("senderUuid")))
                .content((String) value.get("content"))
                .sentAt(LocalDateTime.parse((String) value.get("sentAt")))
                .build();
    }
}