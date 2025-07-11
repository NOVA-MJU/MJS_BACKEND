package nova.mjs.domain.weeklyMenu.repository;


import nova.mjs.domain.weeklyMenu.entity.WeeklyMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklyMenuRepository extends JpaRepository<WeeklyMenu, Long> {
}