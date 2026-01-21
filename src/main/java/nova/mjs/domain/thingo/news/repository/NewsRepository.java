package nova.mjs.domain.thingo.news.repository;

import nova.mjs.domain.thingo.news.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    //카테고리 기준으로 탐색
    Page<News> findByCategory(News.Category category, Pageable pageable);

    //링크를 기준으로 중복 여부 확인
    boolean existsByLink(String link);

    //기사 인덱스를 기준으로 중복 여부 확인
    boolean existsByNewsIndex(Long newsIndex);

    //카테고리 기준으로 존재 여부 확인
    boolean existsByCategory(News.Category category);

    //카테고리를 기준으로 삭제
    void deleteByCategory(News.Category category);
}
