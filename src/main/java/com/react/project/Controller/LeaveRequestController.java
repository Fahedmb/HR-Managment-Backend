// LeaveRequestController.java
package com.react.project.Controller;

import com.react.project.DTO.LeaveBalanceDTO;
import com.react.project.DTO.LeaveRequestDTO;
import com.react.project.Service.LeaveRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {
    private final LeaveRequestService service;
    public LeaveRequestController(LeaveRequestService s) { this.service = s; }

    @GetMapping
    public List<LeaveRequestDTO> getAll() { return service.findAll(); }

    @GetMapping("/{id}")
    public LeaveRequestDTO getById(@PathVariable Long id) { return service.findById(id); }

    @GetMapping("/user/{userId}")
    public List<LeaveRequestDTO> getByUserId(@PathVariable Long userId) { return service.findByUserId(userId); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveRequestDTO create(@RequestBody LeaveRequestDTO dto) { return service.create(dto); }

    @PutMapping("/{id}")
    public LeaveRequestDTO update(@PathVariable Long id, @RequestBody LeaveRequestDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }

    @GetMapping("/balance/{userId}")
    public LeaveBalanceDTO getBalance(@PathVariable Long userId) { return service.getLeaveBalance(userId); }
}
