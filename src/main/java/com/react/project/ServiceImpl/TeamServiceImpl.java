package com.react.project.ServiceImpl;

import com.react.project.DTO.TeamDTO;
import com.react.project.DTO.TeamMemberDTO;
import com.react.project.Enumirator.NotificationType;
import com.react.project.Enumirator.TeamMemberRole;
import com.react.project.Exception.UserException;
import com.react.project.Model.Project;
import com.react.project.Model.Team;
import com.react.project.Model.TeamMember;
import com.react.project.Model.User;
import com.react.project.Repository.ProjectRepository;
import com.react.project.Repository.TeamMemberRepository;
import com.react.project.Repository.TeamRepository;
import com.react.project.Repository.UserRepository;
import com.react.project.Service.NotificationService;
import com.react.project.Service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public List<TeamDTO> findAll() {
        return teamRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public TeamDTO findById(Long id) {
        return toDTO(teamRepository.findById(id).orElseThrow(() -> new UserException("Team not found")));
    }

    @Override
    public List<TeamDTO> findByProject(Long projectId) {
        return teamRepository.findByProjectId(projectId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<TeamDTO> findByLeader(Long leaderId) {
        return teamRepository.findByTeamLeaderId(leaderId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public TeamDTO create(TeamDTO dto, Long createdByHrId) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new UserException("Project not found"));
        Team team = Team.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .project(project)
                .build();
        if (dto.getTeamLeaderId() != null) {
            User leader = userRepository.findById(dto.getTeamLeaderId())
                    .orElseThrow(() -> new UserException("Leader not found"));
            team.setTeamLeader(leader);
        }
        Team saved = teamRepository.save(team);
        return toDTO(saved);
    }

    @Override
    public TeamDTO update(Long id, TeamDTO dto) {
        Team team = teamRepository.findById(id).orElseThrow(() -> new UserException("Team not found"));
        team.setName(dto.getName());
        team.setDescription(dto.getDescription());
        return toDTO(teamRepository.save(team));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        teamRepository.deleteById(id);
    }

    @Override
    public TeamMemberDTO addMember(Long teamId, Long userId) {
        if (teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new UserException("User is already a member of this team");
        }
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new UserException("Team not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new UserException("User not found"));
        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .role(TeamMemberRole.MEMBER)
                .build();
        TeamMember saved = teamMemberRepository.save(member);
        notificationService.create(userId,
                "You have been added to team '" + team.getName() + "' in project '" + team.getProject().getName() + "'.",
                NotificationType.TEAM_MEMBER_ADDED);
        return toMemberDTO(saved);
    }

    @Override
    @Transactional
    public void removeMember(Long teamId, Long userId) {
        TeamMember member = teamMemberRepository.findByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new UserException("Member not found"));
        Team team = member.getTeam();
        teamMemberRepository.delete(member);
        notificationService.create(userId,
                "You have been removed from team '" + team.getName() + "'.",
                NotificationType.TEAM_MEMBER_REMOVED);
    }

    @Override
    public List<TeamMemberDTO> getMembers(Long teamId) {
        return teamMemberRepository.findByTeamId(teamId).stream()
                .map(this::toMemberDTO).collect(Collectors.toList());
    }

    @Override
    public TeamDTO assignLeader(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new UserException("Team not found"));
        User leader = userRepository.findById(userId).orElseThrow(() -> new UserException("User not found"));
        // Ensure the leader is a member
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            TeamMember m = TeamMember.builder().team(team).user(leader).role(TeamMemberRole.LEADER).build();
            teamMemberRepository.save(m);
        } else {
            TeamMember m = teamMemberRepository.findByTeamIdAndUserId(teamId, userId).get();
            m.setRole(TeamMemberRole.LEADER);
            teamMemberRepository.save(m);
        }
        team.setTeamLeader(leader);
        return toDTO(teamRepository.save(team));
    }

    private TeamDTO toDTO(Team t) {
        return TeamDTO.builder()
                .id(t.getId())
                .name(t.getName())
                .description(t.getDescription())
                .projectId(t.getProject().getId())
                .projectName(t.getProject().getName())
                .teamLeaderId(t.getTeamLeader() != null ? t.getTeamLeader().getId() : null)
                .teamLeaderName(t.getTeamLeader() != null
                        ? t.getTeamLeader().getFirstName() + " " + t.getTeamLeader().getLastName()
                        : null)
                .members(t.getMembers().stream().map(this::toMemberDTO).collect(Collectors.toList()))
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private TeamMemberDTO toMemberDTO(TeamMember m) {
        return TeamMemberDTO.builder()
                .id(m.getId())
                .teamId(m.getTeam().getId())
                .teamName(m.getTeam().getName())
                .userId(m.getUser().getId())
                .firstName(m.getUser().getFirstName())
                .lastName(m.getUser().getLastName())
                .email(m.getUser().getEmail())
                .position(m.getUser().getPosition())
                .role(m.getRole())
                .joinedAt(m.getJoinedAt())
                .build();
    }
}
