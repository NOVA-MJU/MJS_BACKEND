package nova.mjs.weeklyMenu.repository;


import nova.mjs.weeklyMenu.entity.WeeklyMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklyMenuRepository extends JpaRepository<WeeklyMenu, Long> {
}