package com.backoffice.fitandflex.repository;

import com.backoffice.fitandflex.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.email = :email")
    Optional<User> findByEmailWithRole(@Param("email") String email);
    
    List<User> findByBranchId(Long branchId);
    List<User> findByRoleName(String roleName);
    List<User> findByActiveTrue();
    List<User> findByActiveFalse();
    
    @Query("SELECT u FROM User u WHERE u.branch.id = :branchId AND u.active = true")
    List<User> findActiveUsersByBranch(@Param("branchId") Long branchId);
    
    @Query("SELECT u FROM User u WHERE u.role.name = :roleName AND u.active = true")
    List<User> findActiveUsersByRole(@Param("roleName") String roleName);
}
