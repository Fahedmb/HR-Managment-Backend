package com.react.project.Service;

import com.react.project.DTO.TimeSheetDTO;

import java.util.List;

public interface TimeSheetService {
    TimeSheetDTO findById(Long id);
    List<TimeSheetDTO> findByUserId(Long userId);
    TimeSheetDTO create(TimeSheetDTO timeSheetDTO);
    TimeSheetDTO update(Long id, TimeSheetDTO timeSheetDTO);
    void delete(Long id);
}
