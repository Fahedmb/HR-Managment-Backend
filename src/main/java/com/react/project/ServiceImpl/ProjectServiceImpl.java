package com.react.project.ServiceImpl;

import com.react.project.DTO.ProjectDTO;
import com.react.project.DTO.TeamDTO;
import com.react.project.Enumirator.NotificationType;
import com.react.project.Enumirator.ProjectStatus;
import com.react.project.Enumirator.TaskStatus;
import com.react.project.Exception.UserException;
import com.react.project.Model.Project;
import com.react.project.Model.User;
import com.react.project.Repository.ProjectRepository;
import com.react.project.Repository.TaskRepository;
import com.react.project.Repository.TeamMemberRepository;
import com.react.project.Repository.UserRepository;
import com.react.project.Service.NotificationService;
import com.react.project.Service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final NotificationService notificationService;

    @Override
    public List<ProjectDTO> findAll() {
        return projectRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ProjectDTO findById(Long id) {
        return toDTO(projectRepository.findById(id)
                .orElseThrow(() -> new UserException("Project not found")));
    }

    @Override
    public List<ProjectDTO> findByDepartment(String department) {
        return projectRepository.findByDepartment(department).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProjectDTO> findByCreatedBy(Long userId) {
        return projectRepository.findByCreatedById(userId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ProjectDTO create(ProjectDTO dto, Long createdById) {
        User creator = userRepository.findById(createdById)
                .orElseThrow(() -> new UserException("User not found"));
        Project project = Project.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .department(dto.getDepartment())
                .status(dto.getStatus() != null ? dto.getStatus() : ProjectStatus.PLANNING)
                .startDate(dto.getStartDate())
                .deadline(dto.getDeadline())
                .createdBy(creator)
                .build();
        return toDTO(projectRepository.save(project));
    }

    @Override
    public ProjectDTO update(Long id, ProjectDTO dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new UserException("Project not found"));
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setDepartment(dto.getDepartment());
        project.setStartDate(dto.getStartDate());
        project.setDeadline(dto.getDeadline());
        Project saved = projectRepository.save(project);

        // Notify all team members
        saved.getTeams().forEach(team ->
            team.getMembers().forEach(m ->
                notificationService.create(m.getUser().getId(),
                        "Project '" + saved.getName() + "' has been updated.",
                        NotificationType.PROJECT_UPDATED)
            )
        );
        return toDTO(saved);
    }

    @Override
    public ProjectDTO updateStatus(Long id, ProjectDTO dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new UserException("Project not found"));
        project.setStatus(dto.getStatus());
        Project saved = projectRepository.save(project);
        saved.getTeams().forEach(team ->
            team.getMembers().forEach(m ->
                notificationService.create(m.getUser().getId(),
                        "Project '" + saved.getName() + "' status changed to " + saved.getStatus(),
                        NotificationType.PROJECT_UPDATED)
            )
        );
        return toDTO(saved);
    }

    @Override
    public void delete(Long id) {
        projectRepository.deleteById(id);
    }

    public ProjectDTO toDTO(Project p) {
        long total = taskRepository.findByProjectId(p.getId()).size();
        long completed = taskRepository.countByProjectIdAndStatus(p.getId(), TaskStatus.DONE);
        return ProjectDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .department(p.getDepartment())
                .status(p.getStatus())
                .startDate(p.getStartDate())
                .deadline(p.getDeadline())
                .createdById(p.getCreatedBy().getId())
                .createdByName(p.getCreatedBy().getFirstName() + " " + p.getCreatedBy().getLastName())
                .totalTasks((int) total)
                .completedTasks((int) completed)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
