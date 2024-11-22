package com.react.project.ServiceImpl;

import com.react.project.Model.TimeSheet;
import com.react.project.Model.User;
import com.react.project.DTO.TimeSheetDTO;
import com.react.project.Mapper.TimeSheetMapper;
import com.react.project.Repository.TimeSheetRepository;
import com.react.project.Repository.UserRepository;
import com.react.project.Service.TimeSheetService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimeSheetServiceImpl implements TimeSheetService {
    private final TimeSheetRepository timeSheetRepository;
    private final UserRepository userRepository;

    public TimeSheetServiceImpl(TimeSheetRepository timeSheetRepository, UserRepository userRepository) {
        this.timeSheetRepository = timeSheetRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TimeSheetDTO findById(Long id) {
        TimeSheet timeSheet = timeSheetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TimeSheet not found"));
        return TimeSheetMapper.toDTO(timeSheet);
    }

    @Override
    public List<TimeSheetDTO> findByUserId(Long userId) {
        return timeSheetRepository.findByUserId(userId)
                .stream()
                .map(TimeSheetMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TimeSheetDTO create(TimeSheetDTO timeSheetDTO) {
        User user = userRepository.findById(timeSheetDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        User approvedBy = null;
        if (timeSheetDTO.getApprovedById() != null) {
            approvedBy = userRepository.findById(timeSheetDTO.getApprovedById())
                    .orElseThrow(() -> new RuntimeException("Approver not found"));
        }
        TimeSheet timeSheet = TimeSheetMapper.toEntity(timeSheetDTO, user, approvedBy);
        TimeSheet savedTimeSheet = timeSheetRepository.save(timeSheet);
        return TimeSheetMapper.toDTO(savedTimeSheet);
    }

    @Override
    public TimeSheetDTO update(Long id, TimeSheetDTO timeSheetDTO) {
        TimeSheet existingTimeSheet = timeSheetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TimeSheet not found"));
        existingTimeSheet.setDate(timeSheetDTO.getDate());
        existingTimeSheet.setHoursWorked(timeSheetDTO.getHoursWorked());
        existingTimeSheet.setStatus(timeSheetDTO.getStatus());
        TimeSheet updatedTimeSheet = timeSheetRepository.save(existingTimeSheet);
        return TimeSheetMapper.toDTO(updatedTimeSheet);
    }

    @Override
    public void delete(Long id) {
        timeSheetRepository.deleteById(id);
    }
}
