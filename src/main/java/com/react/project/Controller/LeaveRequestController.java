// File: src/main/java/com/react/project/Controller/LeaveRequestController.java
package com.react.project.Controller;

import com.react.project.DTO.LeaveRequestDTO;
import com.react.project.Service.LeaveRequestService;
import com.react.project.Service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {
    private final LeaveRequestService service;
    private final UserService userService;

    public LeaveRequestController(LeaveRequestService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    @GetMapping
    public List<LeaveRequestDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public LeaveRequestDTO getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/user/{userId}")
    public List<LeaveRequestDTO> getByUserId(@PathVariable Long userId) {
        return service.findByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveRequestDTO create(@RequestBody LeaveRequestDTO dto) throws MessagingException {
        return service.create(dto);
    }

    @PutMapping("/{id}/status")
    public LeaveRequestDTO updateStatus(@PathVariable Long id, @RequestBody LeaveRequestDTO dto) throws MessagingException {
        return service.updateStatus(id, dto);
    }

    @PutMapping("/{id}")
    public LeaveRequestDTO update(@PathVariable Long id, @RequestBody LeaveRequestDTO dto) throws MessagingException {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // New Endpoint for Leave Balance
    @GetMapping("/balance")
    public int getLeaveBalance(Authentication authentication) {
        String username = authentication.getName();
        Long userId = userService.getUserByEmail(username).getId();
        return service.getLeaveBalance(userId);
    }
}
