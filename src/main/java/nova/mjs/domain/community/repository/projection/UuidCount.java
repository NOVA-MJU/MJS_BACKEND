package nova.mjs.domain.community.repository.projection;

import java.util.UUID;

// 조회 전용 Projection (집계 결과를 담는 가벼운 인터페이스)
public interface UuidCount {
    UUID getUuid();

    long getCnt();
}