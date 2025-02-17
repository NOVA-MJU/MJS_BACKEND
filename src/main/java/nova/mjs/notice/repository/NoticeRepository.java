package nova.mjs.notice.repository;

import nova.mjs.notice.dto.NoticeResponseDto;
import nova.mjs.notice.entity.Notice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Query("SELECT new nova.mjs.notice.dto.NoticeResponseDto(n.title, n.date, n.category, n.link) " +
            "FROM Notice n " +
            "WHERE n.category = :category AND (:year IS NULL OR n.date LIKE CONCAT(:year, '%'))")
    List<NoticeResponseDto> findNoticesByCategoryAndYear(
            @Param("category") String category,
            @Param("year") Integer year,
            Pageable pageable);
}
