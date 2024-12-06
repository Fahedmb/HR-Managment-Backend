// TimesheetScheduleController.java
package com.react.project.Controller;

import com.react.project.DTO.TimesheetScheduleDTO;
import com.react.project.Service.TimesheetScheduleService;
import com.react.project.Enumirator.TimesheetStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/timesheet-schedules")
public class TimesheetScheduleController {

    private final TimesheetScheduleService service;

    public TimesheetScheduleController(TimesheetScheduleService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimesheetScheduleDTO create(@RequestBody TimesheetScheduleDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public TimesheetScheduleDTO updateSchedule(@PathVariable Long id, @RequestBody TimesheetScheduleDTO dto) {
        return service.update(id, dto);
    }

    @PutMapping("/{id}/approve")
    public TimesheetScheduleDTO approve(@PathVariable Long id) {
        TimesheetScheduleDTO dto = new TimesheetScheduleDTO();
        dto.setStatus(TimesheetStatus.APPROVED);
        return service.updateStatus(id, dto);
    }

    @PutMapping("/{id}/delete-request")
    public TimesheetScheduleDTO requestDeletion(@PathVariable Long id, @RequestParam Long userId) {
        return service.requestDeletion(id, userId);
    }

    @GetMapping("/user/{userId}")
    public TimesheetScheduleDTO getActive(@PathVariable Long userId) {
        Optional<TimesheetScheduleDTO> schedule = service.getActiveScheduleForUser(userId);
        return schedule.orElse(null);
    }


    @DeleteMapping("/{id}/finalize-delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void finalizeDelete(@PathVariable Long id) {
        service.updateStatus(id, TimesheetScheduleDTO.builder().status(TimesheetStatus.PENDING_DELETION).build());
    }
}
