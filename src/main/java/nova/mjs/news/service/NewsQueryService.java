package nova.mjs.news.service;

import nova.mjs.news.DTO.NewsResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 뉴스 조회 서비스 인터페이스
 * CQRS 패턴의 Query 부분을 담당
 */
public interface NewsQueryService {
    
    /**
     * 카테고리별 뉴스 조회 (페이지네이션)
     */
    Page<NewsResponseDTO> getNewsByCategory(String category, Pageable pageable);
}