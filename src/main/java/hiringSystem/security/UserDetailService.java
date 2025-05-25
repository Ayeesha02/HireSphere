/**
 * UserDetailService.java
 * This class implements UserDetailsService to load user-specific data.
 * It retrieves user information from the database and constructs a UserDetails object.
 * This is used for authentication and authorization in the Spring Security framework.
 */

package hiringSystem.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import hiringSystem.model.UserRole;
import hiringSystem.repository.UserRoleRepository;

@Service
public class UserDetailService implements UserDetailsService {

    @Autowired
    private UserRoleRepository userRoleRepository;

    /**
     * Load user by email
     * 
     * @param email the email of the user
     * @return UserDetails object containing user information
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserRole userRole = userRoleRepository.findByEmail(email);
        System.out.println(
                "Loaded user: " + (userRole != null ? userRole.getEmail() + ", " + userRole.getPassword() : "null"));
        if (userRole == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return User.withUsername(userRole.getEmail())
                .password(userRole.getPassword())
                .roles(userRole.getRole().toUpperCase())
                .build();
    }
}