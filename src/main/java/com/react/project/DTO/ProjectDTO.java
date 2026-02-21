package com.react.project.DTO;

import com.react.project.Enumirator.ProjectStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private String department;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate deadline;
    private Long createdById;
    private String createdByName;
    private List<TeamDTO> teams;
    private Integer totalTasks;
    private Integer completedTasks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
