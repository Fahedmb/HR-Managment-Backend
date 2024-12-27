package com.react.project.Controller;

import com.react.project.DTO.LeaveRequestDTO;
import com.react.project.Service.LeaveRequestService;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {
    private final LeaveRequestService service;

    public LeaveRequestController(LeaveRequestService service) {
        this.service = service;
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

    
}
