package nova.mjs.domain.calendar.repository;

import nova.mjs.domain.calendar.entity.MjuCalendar;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MjuCalendarRepository extends JpaRepository<MjuCalendar, Long> {

    Page<MjuCalendar> findByYear(int year, Pageable pageable);
}