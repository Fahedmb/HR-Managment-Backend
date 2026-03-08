// TimeSheetServiceImpl.java
package com.react.project.ServiceImpl;

import com.react.project.DTO.TimeSheetDTO;
import com.react.project.Enumirator.NotificationType;
import com.react.project.Enumirator.Status;
import com.react.project.Mapper.TimeSheetMapper;
import com.react.project.Model.TimeSheet;
import com.react.project.Model.User;
import com.react.project.Repository.TimeSheetRepository;
import com.react.project.Repository.UserRepository;
import com.react.project.Service.NotificationService;
import com.react.project.Service.TimeSheetService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimeSheetServiceImpl implements TimeSheetService {
    private final TimeSheetRepository repo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    public TimeSheetServiceImpl(TimeSheetRepository r, UserRepository u, NotificationService ns) {
        this.repo = r;
        this.userRepo = u;
        this.notificationService = ns;
    }

    @Override
    public List<TimeSheetDTO> findAll() {
        return repo.findAll().stream().map(TimeSheetMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public TimeSheetDTO findById(Long id) {
        TimeSheet t = repo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        return TimeSheetMapper.toDTO(t);
    }

    @Override
    public List<TimeSheetDTO> findByUserId(Long userId) {
        return repo.findByUserId(userId).stream().map(TimeSheetMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public TimeSheetDTO create(TimeSheetDTO dto) {
        User user = userRepo.findById(dto.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        User approved = null;
        if (dto.getApprovedById() != null) {
            approved = userRepo.findById(dto.getApprovedById()).orElseThrow(() -> new RuntimeException("Approver not found"));
        }
        TimeSheet entity = TimeSheetMapper.toEntity(dto, user, approved);
        return TimeSheetMapper.toDTO(repo.save(entity));
    }

    @Override
    public TimeSheetDTO update(Long id, TimeSheetDTO dto) {
        TimeSheet t = repo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        t.setDate(dto.getDate());
        t.setHoursWorked(dto.getHoursWorked());
        t.setStatus(dto.getStatus());
        return TimeSheetMapper.toDTO(repo.save(t));
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public TimeSheetDTO approve(Long id, Long approverId) {
        TimeSheet t = repo.findById(id).orElseThrow(() -> new RuntimeException("Timesheet not found"));
        User approver = userRepo.findById(approverId).orElseThrow(() -> new RuntimeException("Approver not found"));
        t.setStatus(Status.APPROVED);
        t.setApprovedBy(approver);
        TimeSheetDTO saved = TimeSheetMapper.toDTO(repo.save(t));
        notificationService.create(
                t.getUser().getId(),
                "Your timesheet for " + t.getDate() + " has been approved.",
                NotificationType.SCHEDULE_APPROVED);
        return saved;
    }

    @Override
    public TimeSheetDTO reject(Long id, Long approverId) {
        TimeSheet t = repo.findById(id).orElseThrow(() -> new RuntimeException("Timesheet not found"));
        User approver = userRepo.findById(approverId).orElseThrow(() -> new RuntimeException("Approver not found"));
        t.setStatus(Status.REJECTED);
        t.setApprovedBy(approver);
        TimeSheetDTO saved = TimeSheetMapper.toDTO(repo.save(t));
        notificationService.create(
                t.getUser().getId(),
                "Your timesheet for " + t.getDate() + " has been rejected.",
                NotificationType.SCHEDULE_REJECTED);
        return saved;
    }
}

