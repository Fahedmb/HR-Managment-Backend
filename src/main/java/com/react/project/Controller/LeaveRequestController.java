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
import java.util.Map;

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

    @PatchMapping("/{id}/cancel")
    public LeaveRequestDTO cancel(@PathVariable Long id,
                                  @RequestBody(required = false) java.util.Map<String, String> body)
            throws MessagingException {
        String reason = body != null ? body.get("reason") : null;
        return service.cancel(id, reason);
    }

    // New Endpoint for Leave Balance
    @GetMapping("/balance")
    public Map<String, Object> getLeaveBalance(Authentication authentication) {
        if (authentication == null) return Map.of("total", 30, "used", 0, "balance", 30);
        String username = authentication.getName();
        Long userId = userService.getUserByEmail(username).getId();
        int balance = service.getLeaveBalance(userId);
        int used    = 30 - balance;
        return Map.of("total", 30, "used", used, "balance", balance);
    }

    // HR can query any user's leave balance
    @GetMapping("/balance/user/{userId}")
    public Map<String, Object> getLeaveBalanceForUser(@PathVariable Long userId) {
        int balance = service.getLeaveBalance(userId);
        int used    = 30 - balance;
        return Map.of("total", 30, "used", used, "balance", balance);
    }
}
