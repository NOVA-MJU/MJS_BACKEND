package nova.mjs.domain.broadcast.repository;

import nova.mjs.domain.broadcast.entity.Broadcast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BroadcastRepository extends JpaRepository<Broadcast, Long> {
    Optional<Broadcast> findByVideoId(String videoId);
    List<Broadcast> findAll();
    Page<Broadcast> findAllByOrderByPublishedAtDesc(Pageable pageable);
}