/**
 * BiasRepository.java
 * This interface extends JpaRepository to provide CRUD operations for the Bias entity.
 */
package hiringSystem.repository;

import hiringSystem.model.Bias;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BiasRepository extends JpaRepository<Bias, Long> {

    Optional<Bias> findByApplicationId(Long applicationId);
}