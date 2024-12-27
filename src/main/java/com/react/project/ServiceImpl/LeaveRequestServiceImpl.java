// LeaveRequestServiceImpl.java
package com.react.project.ServiceImpl;

import com.react.project.DTO.LeaveBalanceDTO;
import com.react.project.DTO.LeaveRequestDTO;
import com.react.project.Enumirator.LeaveStatus;
import com.react.project.Exception.UserException;
import com.react.project.Mapper.LeaveRequestMapper;
import com.react.project.Model.LeaveRequest;
import com.react.project.Model.User;
import com.react.project.Repository.LeaveRequestRepository;
import com.react.project.Repository.UserRepository;
import com.react.project.Service.LeaveRequestService;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {
    private final LeaveRequestRepository repo;
    private final UserRepository userRepo;
    private static final int MAX_DAYS_PER_YEAR = 30;

    public LeaveRequestServiceImpl(LeaveRequestRepository r, UserRepository u) {
        this.repo = r;
        this.userRepo = u;
    }

    @Override
    public List<LeaveRequestDTO> findAll() {
        return repo.findAll().stream().map(LeaveRequestMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public LeaveRequestDTO findById(Long id) {
        LeaveRequest lr = repo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        return LeaveRequestMapper.toDTO(lr);
    }

    @Override
    public List<LeaveRequestDTO> findByUserId(Long userId) {
        return repo.findByUserId(userId).stream().map(LeaveRequestMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public LeaveRequestDTO create(LeaveRequestDTO dto) {
        User user = userRepo.findById(dto.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        LeaveRequest entity = LeaveRequestMapper.toEntity(dto, user);
        return LeaveRequestMapper.toDTO(repo.save(entity));
    }

    @Override
    public LeaveRequestDTO update(Long id, LeaveRequestDTO dto) {
        LeaveRequest existing = repo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        boolean wasApproved = existing.getStatus() == LeaveStatus.APPROVED;
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setType(dto.getType());
        existing.setStatus(dto.getStatus());
        existing.setReason(dto.getReason());
        LeaveRequest updated = repo.save(existing);
        if (!wasApproved && dto.getStatus() == LeaveStatus.APPROVED) {
            recalc(updated.getUser());
        }
        return LeaveRequestMapper.toDTO(updated);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public LeaveBalanceDTO getLeaveBalance(Long userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new UserException("User not found"));
        int used = user.getUsedDaysThisYear();
        int remain = MAX_DAYS_PER_YEAR - used;
        return new LeaveBalanceDTO(MAX_DAYS_PER_YEAR, used, Math.max(remain, 0));
    }

    private void recalc(User user) {
        int year = LocalDate.now().getYear();
        List<LeaveRequest> requests = repo.findByUserId(user.getId());
        int totalUsed = 0;
        for (LeaveRequest lr : requests) {
            if (lr.getStatus() == LeaveStatus.APPROVED && lr.getStartDate().getYear() == year) {
                long days = ChronoUnit.DAYS.between(lr.getStartDate(), lr.getEndDate()) + 1;
                totalUsed += days;
            }
        }
        user.setUsedDaysThisYear(totalUsed);
        userRepo.save(user);
    }
}
