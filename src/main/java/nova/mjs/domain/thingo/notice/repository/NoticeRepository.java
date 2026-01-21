package nova.mjs.domain.thingo.notice.repository;

import nova.mjs.domain.thingo.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 카테고리 기준 조회
    Page<Notice> findByCategory(String category, Pageable pageable);

    // 카테고리 + 날짜 범위 조회
    Page<Notice> findByCategoryAndDateBetween(
            String category,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    // 전체 조회 (Pageable 포함)
    Page<Notice> findAll(Pageable pageable);

    // 전체 + 날짜 범위 조회
    Page<Notice> findByDateBetween(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    // 크롤링 중 중복 여부 확인용
    boolean existsByDateAndCategoryAndTitle(
            LocalDateTime date,
            String category,
            String title
    );
}

