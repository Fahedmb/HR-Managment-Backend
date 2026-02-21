package com.react.project.DTO;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDTO {
    private Long id;
    private String name;
    private String description;
    private Long projectId;
    private String projectName;
    private Long teamLeaderId;
    private String teamLeaderName;
    private List<TeamMemberDTO> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
