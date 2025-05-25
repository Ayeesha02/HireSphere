/**
 * UserRoleRepository.java
 * This interface extends JpaRepository to provide CRUD operations for the UserRole entity.
 */

package hiringSystem.repository;

import hiringSystem.model.UserRole;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    UserRole findByEmail(String email);

}