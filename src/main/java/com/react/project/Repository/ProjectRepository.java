package com.react.project.Repository;

import com.react.project.Enumirator.ProjectStatus;
import com.react.project.Model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByDepartment(String department);
    List<Project> findByStatus(ProjectStatus status);
    List<Project> findByCreatedById(Long userId);
}
