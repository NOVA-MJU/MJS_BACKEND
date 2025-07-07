package nova.mjs.news.service;

import nova.mjs.news.DTO.NewsResponseDTO;

import java.util.List;

/**
 * 뉴스 변경 서비스 인터페이스
 * CQRS 패턴의 Command 부분을 담당
 */
public interface NewsCommandService {
    
    /**
     * 뉴스 크롤링 및 저장
     */
    List<NewsResponseDTO> crawlAndSaveNews(String category);
    
    /**
     * 뉴스 삭제
     */
    void deleteAllNews(String category);
}