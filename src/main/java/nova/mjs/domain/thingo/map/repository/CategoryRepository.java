package nova.mjs.domain.thingo.map.repository;

import nova.mjs.domain.thingo.map.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** 칩 코드로 단건 조회 (예: "daedong") */
    Optional<Category> findByCode(String code);

    /** 특정 칩의 하위 탭들 (예: 대동명지도 아래 한식/중식), 노출 순서대로 */
    List<Category> findByParentOrderByDisplayOrderAsc(Category parent);

    /** 카테고리 목록 API용: 그룹·상위칩을 함께 로딩(N+1 방지). 그룹/칩 정렬은 서비스에서 수행 */
    @Query("select c from Category c join fetch c.group left join fetch c.parent")
    List<Category> findAllForListing();
}
