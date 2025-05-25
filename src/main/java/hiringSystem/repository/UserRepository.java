/**
 * UserRepository.java
 * This interface extends JpaRepository to provide CRUD operations for the UserProfile entity.
 */

package hiringSystem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import hiringSystem.model.UserProfile;
import hiringSystem.model.UserRole;

@Repository
public interface UserRepository extends JpaRepository<UserProfile, Long> {

    UserProfile findByUserId(Long userId);

    Optional<UserProfile> findByUser(UserRole user);
}