package nova.mjs.news.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nova.mjs.news.DTO.NewsResponseDTO;
import nova.mjs.news.entity.News;
import nova.mjs.news.exception.NewsNotFoundException;
import nova.mjs.news.repository.NewsRepository;
import nova.mjs.util.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 뉴스 조회 서비스 구현체
 * CQRS 패턴의 Query 부분을 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NewsQueryServiceImpl implements NewsQueryService {

    private final NewsRepository newsRepository;

    @Override
    public Page<NewsResponseDTO> getNewsByCategory(String category, Pageable pageable) {
        log.info("'{}' 카테고리 뉴스 조회 요청", category);

        News.Category categoryEnum;

        try {
            categoryEnum = News.Category.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("잘못된 카테고리 입력: " + category);
        }

        Page<News> newsPage = newsRepository.findByCategory(categoryEnum, pageable);

        if (newsPage.isEmpty()) {
            log.warn("'{}' 카테고리 뉴스 없음", category);
            throw new NewsNotFoundException("해당 카테고리에서 기사를 찾을 수 없습니다.", ErrorCode.NEWS_NOT_FOUND);
        }
        return newsPage.map(NewsResponseDTO::fromEntity);
    }
}