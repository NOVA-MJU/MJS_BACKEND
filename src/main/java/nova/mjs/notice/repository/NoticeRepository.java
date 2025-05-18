package nova.mjs.notice.repository;

import nova.mjs.notice.dto.NoticeResponseDto;
import nova.mjs.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    @Query("""
    SELECT new nova.mjs.notice.dto.NoticeResponseDto(n.title, n.date, n.category, n.link)
    FROM Notice n
    WHERE n.category = :category
    """)
    Page<NoticeResponseDto> findNoticesByCategory(
            @Param("category") String category,
            Pageable pageable
    );

    @Query("""
    SELECT new nova.mjs.notice.dto.NoticeResponseDto(n.title, n.date, n.category, n.link)
    FROM Notice n
    WHERE n.category = :category
    AND n.date BETWEEN :start AND :end
    """)
    Page<NoticeResponseDto> findNoticesByCategoryAndDateRange(
            @Param("category") String category,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            Pageable pageable
    );



    // 중복 여부 확인용: 날짜, 카테고리, 링크가 모두 같은지, 근데 링크는 중복 못알아 먹더라
    boolean existsByDateAndCategoryAndLink(LocalDateTime date, String category, String link);

    // 중복 여부 확인용: 제목, 카테고리, 날짜 모두 같은지
    boolean existsByDateAndCategoryAndTitle(LocalDateTime date, String category, String title);

}
