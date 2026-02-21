package com.react.project.Service;

import com.react.project.DTO.ProjectDTO;

import java.util.List;

public interface ProjectService {
    List<ProjectDTO> findAll();
    ProjectDTO findById(Long id);
    List<ProjectDTO> findByDepartment(String department);
    List<ProjectDTO> findByCreatedBy(Long userId);
    ProjectDTO create(ProjectDTO dto, Long createdById);
    ProjectDTO update(Long id, ProjectDTO dto);
    ProjectDTO updateStatus(Long id, ProjectDTO dto);
    void delete(Long id);
}
