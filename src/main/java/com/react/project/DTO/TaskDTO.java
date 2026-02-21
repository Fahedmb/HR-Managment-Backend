package com.react.project.DTO;

import com.react.project.Enumirator.TaskPriority;
import com.react.project.Enumirator.TaskStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private Long projectId;
    private String projectName;
    private Long teamId;
    private String teamName;
    private Long assignedToId;
    private String assignedToName;
    private Long createdById;
    private String createdByName;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate deadline;
    private Double estimatedHours;
    private Double actualHours;
    private List<TaskCommentDTO> comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
