// TimeSheetService.java
package com.react.project.Service;
import com.react.project.DTO.TimeSheetDTO;
import java.util.List;

public interface TimeSheetService {
    List<TimeSheetDTO> findAll();
    TimeSheetDTO findById(Long id);
    List<TimeSheetDTO> findByUserId(Long userId);
    TimeSheetDTO create(TimeSheetDTO dto);
    TimeSheetDTO update(Long id, TimeSheetDTO dto);
    void delete(Long id);
    /** HR approves a timesheet entry; approverId is the HR user performing the action */
    TimeSheetDTO approve(Long id, Long approverId);
    /** HR rejects a timesheet entry */
    TimeSheetDTO reject(Long id, Long approverId);
}
