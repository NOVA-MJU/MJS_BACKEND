package nova.mjs.domain.thingo.ElasticSearch.Repository;

import nova.mjs.domain.thingo.ElasticSearch.Entity.PopularSearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PopularSearchKeywordRepository extends JpaRepository<PopularSearchKeyword, Long> {
    List<PopularSearchKeyword> findTop5ByPeriodOrderByDisplayOrderAscIdAsc(PopularSearchKeyword.SeasonalPeriod period);
}
