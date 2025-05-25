/**
 * DashboardRepository.java 
 * This interface extends JpaRepository to provide CRUD operations for the Dashboard entity.
 */
package hiringSystem.repository;

import hiringSystem.model.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface DashboardRepository extends JpaRepository<Dashboard, Long> {

    List<Dashboard> findByRecruiterUserId(Long recruiterId);

    Optional<Dashboard> findByRecruiterUserIdAndMetricDate(Long recruiterId, Date metricDate);

    List<Dashboard> findDailyMetricsByRecruiterUserId(Long recruiterId);

    List<Dashboard> findWeeklyMetricsByRecruiterUserId(Long recruiterId);

    List<Dashboard> findMonthlyMetricsByRecruiterUserId(Long recruiterId);
}
