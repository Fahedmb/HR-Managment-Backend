package com.react.project.Controller;

import com.react.project.DTO.ProjectDTO;
import com.react.project.Enumirator.ProjectStatus;
import com.react.project.Service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public List<ProjectDTO> getAll() {
        return projectService.findAll();
    }

    @GetMapping("/{id}")
    public ProjectDTO getById(@PathVariable Long id) {
        return projectService.findById(id);
    }

    @GetMapping("/department/{department}")
    public List<ProjectDTO> getByDepartment(@PathVariable String department) {
        return projectService.findByDepartment(department);
    }

    @GetMapping("/created-by/{userId}")
    public List<ProjectDTO> getByCreatedBy(@PathVariable Long userId) {
        return projectService.findByCreatedBy(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('HR')")
    public ProjectDTO create(@RequestBody ProjectDTO dto,
                             @RequestParam Long createdById) {
        return projectService.create(dto, createdById);
    }

    @PutMapping("/{id}")
    public ProjectDTO update(@PathVariable Long id, @RequestBody ProjectDTO dto) {
        return projectService.update(id, dto);
    }

    @PatchMapping("/{id}/status")
    public ProjectDTO updateStatus(@PathVariable Long id,
                                   @RequestBody Map<String, String> body) {
        ProjectDTO patch = new ProjectDTO();
        patch.setStatus(ProjectStatus.valueOf(body.get("status")));
        return projectService.updateStatus(id, patch);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('HR')")
    public void delete(@PathVariable Long id) {
        projectService.delete(id);
    }
}
