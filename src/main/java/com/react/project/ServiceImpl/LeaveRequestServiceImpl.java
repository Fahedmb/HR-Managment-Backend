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
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;

    private static final int MAX_DAYS_PER_YEAR = 30;

    public LeaveRequestServiceImpl(LeaveRequestRepository leaveRequestRepository, UserRepository userRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    public LeaveRequestDTO findById(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));
        return LeaveRequestMapper.toDTO(leaveRequest);
    }

    @Override
    public List<LeaveRequestDTO> findByUserId(Long userId) {
        return leaveRequestRepository.findByUserId(userId)
                .stream()
                .map(LeaveRequestMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LeaveRequestDTO create(LeaveRequestDTO leaveRequestDTO) {
        User user = userRepository.findById(leaveRequestDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        LeaveRequest leaveRequest = LeaveRequestMapper.toEntity(leaveRequestDTO, user);
        LeaveRequest savedLeaveRequest = leaveRequestRepository.save(leaveRequest);
        return LeaveRequestMapper.toDTO(savedLeaveRequest);
    }

    @Override
    public LeaveRequestDTO update(Long id, LeaveRequestDTO leaveRequestDTO) {
        LeaveRequest existingRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        boolean wasApprovedBefore = existingRequest.getStatus() == LeaveStatus.APPROVED;

        existingRequest.setStartDate(leaveRequestDTO.getStartDate());
        existingRequest.setEndDate(leaveRequestDTO.getEndDate());
        existingRequest.setType(leaveRequestDTO.getType());
        existingRequest.setStatus(leaveRequestDTO.getStatus());
        existingRequest.setReason(leaveRequestDTO.getReason());
        LeaveRequest updatedRequest = leaveRequestRepository.save(existingRequest);

        if (!wasApprovedBefore && leaveRequestDTO.getStatus() == LeaveStatus.APPROVED) {
            User user = updatedRequest.getUser();
            recalculateUsedDaysThisYear(user);
        }

        return LeaveRequestMapper.toDTO(updatedRequest);
    }

    @Override
    public void delete(Long id) {
        leaveRequestRepository.deleteById(id);
    }

    @Override
    public LeaveBalanceDTO getLeaveBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));
        int usedDaysThisYear = user.getUsedDaysThisYear();
        int remaining = MAX_DAYS_PER_YEAR - usedDaysThisYear;
        return new LeaveBalanceDTO(MAX_DAYS_PER_YEAR, usedDaysThisYear, Math.max(remaining, 0));
    }

    private void recalculateUsedDaysThisYear(User user) {
        int currentYear = LocalDate.now().getYear();
        List<LeaveRequest> requests = leaveRequestRepository.findByUserId(user.getId());

        int totalUsed = 0;
        for (LeaveRequest lr : requests) {
            if (lr.getStatus() == LeaveStatus.APPROVED && lr.getStartDate().getYear() == currentYear) {
                long days = ChronoUnit.DAYS.between(lr.getStartDate(), lr.getEndDate()) + 1;
                totalUsed += days;
            }
        }

        user.setUsedDaysThisYear(totalUsed);
        userRepository.save(user);
    }
}
