package com.react.project.Controller;

import com.react.project.DTO.MeetingDTO;
import com.react.project.Service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @GetMapping
    public List<MeetingDTO> getAll() {
        return meetingService.findAll();
    }

    @GetMapping("/{id}")
    public MeetingDTO getById(@PathVariable Long id) {
        return meetingService.findById(id);
    }

    /** All meetings where the user is organizer OR attendee */
    @GetMapping("/user/{userId}")
    public List<MeetingDTO> getForUser(@PathVariable Long userId) {
        return meetingService.findForUser(userId);
    }

    @GetMapping("/organizer/{organizerId}")
    public List<MeetingDTO> getByOrganizer(@PathVariable Long organizerId) {
        return meetingService.findOrganizedBy(organizerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('HR')")
    public MeetingDTO create(@RequestBody MeetingDTO dto) {
        return meetingService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR')")
    public MeetingDTO update(@PathVariable Long id, @RequestBody MeetingDTO dto) {
        return meetingService.update(id, dto);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('HR')")
    public MeetingDTO cancel(@PathVariable Long id) {
        return meetingService.cancel(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('HR')")
    public void delete(@PathVariable Long id) {
        meetingService.delete(id);
    }
}
