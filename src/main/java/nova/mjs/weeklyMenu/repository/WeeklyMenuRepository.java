package nova.mjs.weeklyMenu.repository;


import nova.mjs.weeklyMenu.entity.WeeklyMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklyMenuRepository extends JpaRepository<WeeklyMenu, Long> {

    @Modifying
    @Query(value = "TRUNCATE TABLE weekly_menu RESTART IDENTITY CASCADE", nativeQuery = true)
    void truncateAllWithCascade();

}