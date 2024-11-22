package com.react.project.Controller;

import com.react.project.DTO.LeaveRequestDTO;
import com.react.project.Service.LeaveRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @GetMapping("/{id}")
    public LeaveRequestDTO getLeaveRequestById(@PathVariable Long id) {
        return leaveRequestService.findById(id);
    }

    @GetMapping("/user/{userId}")
    public List<LeaveRequestDTO> getLeaveRequestsByUserId(@PathVariable Long userId) {
        return leaveRequestService.findByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveRequestDTO createLeaveRequest(@RequestBody LeaveRequestDTO leaveRequestDTO) {
        return leaveRequestService.create(leaveRequestDTO);
    }

    @PutMapping("/{id}")
    public LeaveRequestDTO updateLeaveRequest(@PathVariable Long id, @RequestBody LeaveRequestDTO leaveRequestDTO) {
        return leaveRequestService.update(id, leaveRequestDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLeaveRequest(@PathVariable Long id) {
        leaveRequestService.delete(id);
    }
}
