package com.react.project.Service;

import com.react.project.DTO.LeaveRequestDTO;
import jakarta.mail.MessagingException;

import java.util.List;

public interface LeaveRequestService {
    List<LeaveRequestDTO> findAll();
    LeaveRequestDTO findById(Long id);
    List<LeaveRequestDTO> findByUserId(Long userId);
    LeaveRequestDTO create(LeaveRequestDTO dto) throws MessagingException;
    LeaveRequestDTO updateStatus(Long id, LeaveRequestDTO dto) throws MessagingException;
    LeaveRequestDTO update(Long id, LeaveRequestDTO dto) throws MessagingException;
    int getLeaveBalance(Long userId);
    void delete(Long id);
}
