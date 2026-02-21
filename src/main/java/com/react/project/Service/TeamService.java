package com.react.project.Service;

import com.react.project.DTO.TeamDTO;
import com.react.project.DTO.TeamMemberDTO;

import java.util.List;

public interface TeamService {
    List<TeamDTO> findAll();
    TeamDTO findById(Long id);
    List<TeamDTO> findByProject(Long projectId);
    List<TeamDTO> findByLeader(Long leaderId);
    TeamDTO create(TeamDTO dto, Long createdByHrId);
    TeamDTO update(Long id, TeamDTO dto);
    void delete(Long id);

    TeamMemberDTO addMember(Long teamId, Long userId);
    void removeMember(Long teamId, Long userId);
    List<TeamMemberDTO> getMembers(Long teamId);
    TeamDTO assignLeader(Long teamId, Long userId);
}
