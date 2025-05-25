/**
 * AuditLogRepository.java
 * This interface extends JpaRepository to provide CRUD operations for the AuditLog entity.
 */
package hiringSystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import hiringSystem.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEmail(String email);
}
