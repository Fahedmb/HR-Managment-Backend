package com.react.project.ServiceImpl;

import com.react.project.DTO.TaskCommentDTO;
import com.react.project.DTO.TaskDTO;
import com.react.project.Enumirator.NotificationType;
import com.react.project.Enumirator.TaskStatus;
import com.react.project.Exception.UserException;
import com.react.project.Model.*;
import com.react.project.Repository.*;
import com.react.project.Service.NotificationService;
import com.react.project.Service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskCommentRepository commentRepository;
    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public List<TaskDTO> findAll() {
        return taskRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public TaskDTO findById(Long id) {
        return toDTO(taskRepository.findById(id).orElseThrow(() -> new UserException("Task not found")));
    }

    @Override
    public List<TaskDTO> findByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<TaskDTO> findByTeam(Long teamId) {
        return taskRepository.findByTeamId(teamId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<TaskDTO> findByAssignee(Long userId) {
        return taskRepository.findByAssignedToId(userId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public TaskDTO create(TaskDTO dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new UserException("Project not found"));
        User creator = userRepository.findById(dto.getCreatedById())
                .orElseThrow(() -> new UserException("Creator not found"));

        Task task = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .project(project)
                .createdBy(creator)
                .status(dto.getStatus() != null ? dto.getStatus() : TaskStatus.TODO)
                .priority(dto.getPriority())
                .deadline(dto.getDeadline())
                .estimatedHours(dto.getEstimatedHours())
                .build();

        if (dto.getTeamId() != null) {
            task.setTeam(teamRepository.findById(dto.getTeamId()).orElse(null));
        }
        if (dto.getAssignedToId() != null) {
            User assignee = userRepository.findById(dto.getAssignedToId())
                    .orElseThrow(() -> new UserException("Assignee not found"));
            task.setAssignedTo(assignee);
        }

        Task saved = taskRepository.save(task);

        if (saved.getAssignedTo() != null) {
            notificationService.create(
                    saved.getAssignedTo().getId(),
                    "You have been assigned a new task: '" + saved.getTitle() + "' in project '" + project.getName() + "'.",
                    NotificationType.TASK_ASSIGNED);
        }
        return toDTO(saved);
    }

    @Override
    public TaskDTO update(Long id, TaskDTO dto) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new UserException("Task not found"));

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setPriority(dto.getPriority());
        task.setDeadline(dto.getDeadline());
        task.setEstimatedHours(dto.getEstimatedHours());
        task.setActualHours(dto.getActualHours());

        if (dto.getAssignedToId() != null) {
            User newAssignee = userRepository.findById(dto.getAssignedToId())
                    .orElseThrow(() -> new UserException("Assignee not found"));
            boolean reassigned = task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(newAssignee.getId());
            task.setAssignedTo(newAssignee);
            if (reassigned) {
                notificationService.create(newAssignee.getId(),
                        "You have been assigned task: '" + task.getTitle() + "'.",
                        NotificationType.TASK_ASSIGNED);
            }
        }
        return toDTO(taskRepository.save(task));
    }

    @Override
    public TaskDTO updateStatus(Long id, TaskStatus status, Long updatedByUserId) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new UserException("Task not found"));
        TaskStatus old = task.getStatus();
        task.setStatus(status);
        Task saved = taskRepository.save(task);
        // Notify creator and assignee
        if (saved.getCreatedBy() != null && !saved.getCreatedBy().getId().equals(updatedByUserId)) {
            notificationService.create(saved.getCreatedBy().getId(),
                    "Task '" + saved.getTitle() + "' status changed from " + old + " to " + status + ".",
                    NotificationType.TASK_STATUS_CHANGED);
        }
        if (saved.getAssignedTo() != null && !saved.getAssignedTo().getId().equals(updatedByUserId)) {
            notificationService.create(saved.getAssignedTo().getId(),
                    "Task '" + saved.getTitle() + "' status changed to " + status + ".",
                    NotificationType.TASK_STATUS_CHANGED);
        }
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public TaskCommentDTO addComment(Long taskId, Long authorId, String content) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new UserException("Task not found"));
        User author = userRepository.findById(authorId).orElseThrow(() -> new UserException("User not found"));
        TaskComment comment = TaskComment.builder()
                .task(task)
                .author(author)
                .content(content)
                .build();
        TaskComment saved = commentRepository.save(comment);
        // Notify assignee and creator if they are different from commenter
        if (task.getAssignedTo() != null && !task.getAssignedTo().getId().equals(authorId)) {
            notificationService.create(task.getAssignedTo().getId(),
                    author.getFirstName() + " commented on task '" + task.getTitle() + "'.",
                    NotificationType.TASK_COMMENTED);
        }
        if (task.getCreatedBy() != null && !task.getCreatedBy().getId().equals(authorId)) {
            notificationService.create(task.getCreatedBy().getId(),
                    author.getFirstName() + " commented on task '" + task.getTitle() + "'.",
                    NotificationType.TASK_COMMENTED);
        }
        return toCommentDTO(saved);
    }

    @Override
    public List<TaskCommentDTO> getComments(Long taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream().map(this::toCommentDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    private TaskDTO toDTO(Task t) {
        return TaskDTO.builder()
                .id(t.getId())
                .title(t.getTitle())
                .description(t.getDescription())
                .projectId(t.getProject().getId())
                .projectName(t.getProject().getName())
                .teamId(t.getTeam() != null ? t.getTeam().getId() : null)
                .teamName(t.getTeam() != null ? t.getTeam().getName() : null)
                .assignedToId(t.getAssignedTo() != null ? t.getAssignedTo().getId() : null)
                .assignedToName(t.getAssignedTo() != null
                        ? t.getAssignedTo().getFirstName() + " " + t.getAssignedTo().getLastName() : null)
                .createdById(t.getCreatedBy().getId())
                .createdByName(t.getCreatedBy().getFirstName() + " " + t.getCreatedBy().getLastName())
                .status(t.getStatus())
                .priority(t.getPriority())
                .deadline(t.getDeadline())
                .estimatedHours(t.getEstimatedHours())
                .actualHours(t.getActualHours())
                .comments(t.getComments().stream().map(this::toCommentDTO).collect(Collectors.toList()))
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private TaskCommentDTO toCommentDTO(TaskComment c) {
        return TaskCommentDTO.builder()
                .id(c.getId())
                .taskId(c.getTask().getId())
                .authorId(c.getAuthor().getId())
                .authorName(c.getAuthor().getFirstName() + " " + c.getAuthor().getLastName())
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
