package nova.mjs.domain.thingo.weeklyMenu.repository;


import nova.mjs.domain.thingo.weeklyMenu.entity.WeeklyMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklyMenuRepository extends JpaRepository<WeeklyMenu, Long> {
}