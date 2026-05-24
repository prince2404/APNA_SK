package com.ask.repository;

import com.ask.entity.User;
import com.ask.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Repository for User entity operations.
 */
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    java.util.List<User> findByRoleName(String roleName);

    java.util.List<User> findByRoleNameIn(java.util.List<String> roleNames);
}
