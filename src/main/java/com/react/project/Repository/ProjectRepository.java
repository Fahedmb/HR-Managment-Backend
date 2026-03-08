package com.react.project.Repository;

import com.react.project.Enumirator.ProjectStatus;
import com.react.project.Model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByDepartment(String department);
    List<Project> findByStatus(ProjectStatus status);
    List<Project> findByCreatedById(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Project p SET p.createdBy = null WHERE p.createdBy.id = :userId")
    void nullifyCreatedBy(@Param("userId") Long userId);
}
