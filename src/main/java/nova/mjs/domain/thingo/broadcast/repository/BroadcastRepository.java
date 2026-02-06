package nova.mjs.domain.thingo.broadcast.repository;

import nova.mjs.domain.thingo.broadcast.entity.Broadcast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BroadcastRepository extends JpaRepository<Broadcast, Long> {
    Optional<Broadcast> findByVideoId(String videoId);
    Page<Broadcast> findAllByOrderByPublishedAtDesc(Pageable pageable);
    void deleteByPublishedAtBefore(LocalDateTime boundary);
    void deleteByLastSyncedAtBefore(LocalDateTime boundary);
}
