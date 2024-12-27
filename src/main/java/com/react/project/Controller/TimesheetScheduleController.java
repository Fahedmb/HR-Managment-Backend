// TimesheetScheduleController.java
package com.react.project.Controller;

import com.react.project.DTO.TimesheetScheduleDTO;
import com.react.project.Enumirator.TimesheetStatus;
import com.react.project.Service.TimesheetScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/timesheet-schedules")
public class TimesheetScheduleController {
    private final TimesheetScheduleService s;
    public TimesheetScheduleController(TimesheetScheduleService svc){this.s=svc;}

    @GetMapping
    public List<TimesheetScheduleDTO> getAll() { return s.findAll(); }

    @GetMapping("/user/{userId}")
    public TimesheetScheduleDTO getActive(@PathVariable Long userId) {
        Optional<TimesheetScheduleDTO> o = s.getActiveScheduleForUser(userId);
        return o.orElse(null);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimesheetScheduleDTO create(@RequestBody TimesheetScheduleDTO dto) {
        return s.create(dto);
    }

    @PutMapping("/{id}")
    public TimesheetScheduleDTO updateSchedule(@PathVariable Long id, @RequestBody TimesheetScheduleDTO dto) {
        return s.update(id, dto);
    }

    @PutMapping("/{id}/approve")
    public TimesheetScheduleDTO approve(@PathVariable Long id) {
        TimesheetScheduleDTO d = new TimesheetScheduleDTO();
        d.setStatus(TimesheetStatus.APPROVED);
        return s.updateStatus(id, d);
    }

    @PutMapping("/{id}/delete-request")
    public TimesheetScheduleDTO requestDeletion(@PathVariable Long id, @RequestParam Long userId) {
        return s.requestDeletion(id, userId);
    }

    @DeleteMapping("/{id}/finalize-delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finalizeDelete(@PathVariable Long id) {
        s.updateStatus(id, TimesheetScheduleDTO.builder().status(TimesheetStatus.PENDING_DELETION).build());
    }
}
