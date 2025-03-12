package nova.mjs.news.repository;

import nova.mjs.news.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    //카테고리 기준으로 탐색
    List<News> findByCategory(News.Category category);

    //기사 제목을 기준으로 존재 여부 확인
    boolean existsByTitle(String title);

    //링크를 기준으로 중복 여부 확인
    boolean existsByLink(String link);

    //카테고리 기준으로 존재 여부 확인
    boolean existsByCategory(News.Category category);

    //카테고리를 기준으로 삭제
    void deleteByCategory(News.Category category);
}
