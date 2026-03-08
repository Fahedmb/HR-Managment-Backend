// File: src/main/java/com/react/project/Controller/TimeSheetController.java
package com.react.project.Controller;

import com.react.project.DTO.TimeSheetDTO;
import com.react.project.Service.TimeSheetService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/time-sheets")
public class TimeSheetController {
    private final TimeSheetService service;

    public TimeSheetController(TimeSheetService s) {
        this.service = s;
    }

    @GetMapping
    public List<TimeSheetDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TimeSheetDTO getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/user/{uid}")
    public List<TimeSheetDTO> getByUser(@PathVariable Long uid) {
        return service.findByUserId(uid);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimeSheetDTO create(@RequestBody TimeSheetDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public TimeSheetDTO update(@PathVariable Long id, @RequestBody TimeSheetDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /**
     * HR approves a timesheet entry.
     * Body: { "approverId": 1 }
     */
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('HR')")
    public TimeSheetDTO approve(@PathVariable Long id,
                                @RequestBody Map<String, Long> body) {
        return service.approve(id, body.get("approverId"));
    }

    /**
     * HR rejects a timesheet entry.
     * Body: { "approverId": 1 }
     */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('HR')")
    public TimeSheetDTO reject(@PathVariable Long id,
                               @RequestBody Map<String, Long> body) {
        return service.reject(id, body.get("approverId"));
    }
}

