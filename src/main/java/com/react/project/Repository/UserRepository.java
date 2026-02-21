package com.react.project.Repository;

import com.react.project.Enumirator.Role;
import com.react.project.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByDepartment(String department);
    List<User> findByRole(Role role);
    long countByDepartment(String department);
    long countByRole(Role role);
}
