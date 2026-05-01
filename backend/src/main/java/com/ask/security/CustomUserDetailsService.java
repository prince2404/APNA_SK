package com.ask.security;

import com.ask.entity.User;
import com.ask.enums.UserStatus;
import com.ask.repository.UserPermissionRepository;
import com.ask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom UserDetailsService that loads user details from the database.
 * Used by Spring Security for authentication and by the JWT filter for token validation.
 *
 * Authorities include:
 * - ROLE_{roleName} (e.g., ROLE_SUPER_ADMIN)
 * - PERM_{module}_{action} (e.g., PERM_USERS_CREATE) for fine-grained permission checks
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;

    /**
     * Loads user by email (which is the username in this system).
     * Checks that the user exists and their account is not inactive.
     * Builds authorities from both role and individual permissions.
     *
     * @param email the user's email
     * @return UserDetails for Spring Security
     * @throws UsernameNotFoundException if user not found or inactive
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Check if user account is inactive (deactivated by admin)
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new UsernameNotFoundException("User account is deactivated: " + email);
        }

        // Build authorities list: role + individual permissions
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Add role-based authority (e.g., ROLE_SUPER_ADMIN)
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));

        // Add permission-based authorities (e.g., PERM_USERS_CREATE)
        List<String> permissionStrings = userPermissionRepository.findPermissionStringsByUserId(user.getId());
        for (String perm : permissionStrings) {
            // perm is in format "MODULE:ACTION", convert to "PERM_MODULE_ACTION"
            authorities.add(new SimpleGrantedAuthority("PERM_" + perm.replace(":", "_")));
        }

        // Build Spring Security UserDetails
        // Account is "locked" if status is LOCKED and lockedUntil is in the future
        boolean accountNonLocked = !user.isAccountLocked();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!accountNonLocked)
                .credentialsExpired(false)
                .disabled(user.getStatus() == UserStatus.INACTIVE)
                .build();
    }
}
