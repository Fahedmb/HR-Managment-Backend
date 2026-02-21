package com.react.project.DTO;

import com.react.project.Enumirator.TeamMemberRole;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberDTO {
    private Long id;
    private Long teamId;
    private String teamName;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String position;
    private TeamMemberRole role;
    private LocalDateTime joinedAt;
}
