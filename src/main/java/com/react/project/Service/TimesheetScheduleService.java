package com.react.project.Service;

import com.react.project.DTO.TimesheetScheduleDTO;

import java.util.Optional;

public interface TimesheetScheduleService {
    TimesheetScheduleDTO create(TimesheetScheduleDTO dto);
    TimesheetScheduleDTO update(Long id, TimesheetScheduleDTO dto);
    TimesheetScheduleDTO updateStatus(Long id, TimesheetScheduleDTO dto);
    TimesheetScheduleDTO requestDeletion(Long id, Long userId);
    Optional<TimesheetScheduleDTO> getActiveScheduleForUser(Long userId);
}