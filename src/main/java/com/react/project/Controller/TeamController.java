package com.react.project.Controller;

import com.react.project.DTO.TeamDTO;
import com.react.project.DTO.TeamMemberDTO;
import com.react.project.Service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping
    public List<TeamDTO> getAll() {
        return teamService.findAll();
    }

    @GetMapping("/{id}")
    public TeamDTO getById(@PathVariable Long id) {
        return teamService.findById(id);
    }

    @GetMapping("/project/{projectId}")
    public List<TeamDTO> getByProject(@PathVariable Long projectId) {
        return teamService.findByProject(projectId);
    }

    @GetMapping("/leader/{leaderId}")
    public List<TeamDTO> getByLeader(@PathVariable Long leaderId) {
        return teamService.findByLeader(leaderId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('HR')")
    public TeamDTO create(@RequestBody TeamDTO dto,
                          @RequestParam Long createdByHrId) {
        return teamService.create(dto, createdByHrId);
    }

    @PutMapping("/{id}")
    public TeamDTO update(@PathVariable Long id, @RequestBody TeamDTO dto) {
        return teamService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('HR')")
    public void delete(@PathVariable Long id) {
        teamService.delete(id);
    }

    // ── Member management ──────────────────────────────────────
    @GetMapping("/{teamId}/members")
    public List<TeamMemberDTO> getMembers(@PathVariable Long teamId) {
        return teamService.getMembers(teamId);
    }

    @PostMapping("/{teamId}/members/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('HR')")
    public TeamMemberDTO addMember(@PathVariable Long teamId, @PathVariable Long userId) {
        return teamService.addMember(teamId, userId);
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('HR')")
    public void removeMember(@PathVariable Long teamId, @PathVariable Long userId) {
        teamService.removeMember(teamId, userId);
    }

    @PatchMapping("/{teamId}/leader/{userId}")
    @PreAuthorize("hasRole('HR')")
    public TeamDTO assignLeader(@PathVariable Long teamId, @PathVariable Long userId) {
        return teamService.assignLeader(teamId, userId);
    }
}
