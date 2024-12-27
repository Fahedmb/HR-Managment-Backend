// LeaveRequestService.java
package com.react.project.Service;

import com.react.project.DTO.LeaveBalanceDTO;
import com.react.project.DTO.LeaveRequestDTO;
import java.util.List;

public interface LeaveRequestService {
    List<LeaveRequestDTO> findAll();
    LeaveRequestDTO findById(Long id);
    List<LeaveRequestDTO> findByUserId(Long userId);
    LeaveRequestDTO create(LeaveRequestDTO dto);
    LeaveRequestDTO update(Long id, LeaveRequestDTO dto);
    void delete(Long id);
    LeaveBalanceDTO getLeaveBalance(Long userId);
}
