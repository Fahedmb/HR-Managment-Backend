package com.react.project.Service;

import com.react.project.Model.LeaveRequest;
import com.react.project.DTO.LeaveRequestDTO;

import java.util.List;

public interface LeaveRequestService {
    LeaveRequestDTO findById(Long id);
    List<LeaveRequestDTO> findByUserId(Long userId);
    LeaveRequestDTO create(LeaveRequestDTO leaveRequestDTO);
    LeaveRequestDTO update(Long id, LeaveRequestDTO leaveRequestDTO);
    void delete(Long id);
}
