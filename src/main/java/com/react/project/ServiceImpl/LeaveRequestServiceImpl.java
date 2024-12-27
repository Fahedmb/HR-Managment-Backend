package com.react.project.ServiceImpl;

import com.react.project.DTO.LeaveRequestDTO;
import com.react.project.DTO.UserDTO;
import com.react.project.Enumirator.LeaveStatus;
import com.react.project.Model.LeaveRequest;
import com.react.project.Repository.LeaveRequestRepository;
import com.react.project.Service.EmailService;
import com.react.project.Service.LeaveRequestService;
import com.react.project.Service.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserService userService;
    private final EmailService emailService;

    @Override
    public List<LeaveRequestDTO> findAll() {
        return leaveRequestRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LeaveRequestDTO findById(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave Request not found"));
        return convertToDTO(leaveRequest);
    }

    @Override
    public List<LeaveRequestDTO> findByUserId(Long userId) {
        return leaveRequestRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LeaveRequestDTO create(LeaveRequestDTO dto) throws MessagingException {
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setUser(userService.getUserEntityById(dto.getUserId()));
        leaveRequest.setStartDate(dto.getStartDate());
        leaveRequest.setEndDate(dto.getEndDate());
        leaveRequest.setType(dto.getType());
        leaveRequest.setStatus(leaveRequest.getStatus() != null ? leaveRequest.getStatus() : LeaveStatus.PENDING);
        leaveRequest.setReason(dto.getReason());
        leaveRequestRepository.save(leaveRequest);

        LeaveRequestDTO savedDto = convertToDTO(leaveRequest);

        // Prepare email data
        Map<String, Object> templateModel = Map.of(
                "username", savedDto.getUsername(),
                "leaveRequestId", savedDto.getId(),
                "startDate", savedDto.getStartDate(),
                "endDate", savedDto.getEndDate(),
                "reason", savedDto.getReason()
        );

        // Send email
        emailService.sendEmail(
                savedDto.getUserEmail(),
                "Leave Request Submitted",
                "leaveRequestEmail",
                templateModel
        );

        return savedDto;
    }

    @Override
    public LeaveRequestDTO updateStatus(Long id, LeaveRequestDTO dto) throws MessagingException {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave Request not found"));

        leaveRequest.setStatus(dto.getStatus());
        leaveRequestRepository.save(leaveRequest);

        LeaveRequestDTO updatedDto = convertToDTO(leaveRequest);

        // Determine email template and subject based on status
        String templateName = "";
        String subject = "";

        if (updatedDto.getStatus() == LeaveStatus.APPROVED) {
            templateName = "approvalEmail";
            subject = "Your Leave Request Has Been Approved";
        } else if (updatedDto.getStatus() == LeaveStatus.REJECTED) {
            templateName = "rejectionEmail";
            subject = "Your Leave Request Has Been Rejected";
        }

        if (!templateName.isEmpty()) {
            Map<String, Object> templateModel = Map.of(
                    "username", updatedDto.getUsername(),
                    "requestId", updatedDto.getId(),
                    "type", "Leave Request",
                    "date", updatedDto.getStartDate() // Adjust as needed
            );

            // Send email
            emailService.sendEmail(
                    updatedDto.getUserEmail(),
                    subject,
                    templateName,
                    templateModel
            );
        }

        return updatedDto;
    }

    @Override
    public LeaveRequestDTO update(Long id, LeaveRequestDTO dto) throws MessagingException {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave Request not found"));

        leaveRequest.setStartDate(dto.getStartDate());
        leaveRequest.setEndDate(dto.getEndDate());
        leaveRequest.setType(dto.getType());
        leaveRequest.setStatus(dto.getStatus());
        leaveRequest.setReason(dto.getReason());
        leaveRequestRepository.save(leaveRequest);

        LeaveRequestDTO updatedDto = convertToDTO(leaveRequest);

        // Optionally send email if needed
        // Example: Notify user about update
        Map<String, Object> templateModel = Map.of(
                "username", updatedDto.getUsername(),
                "leaveRequestId", updatedDto.getId(),
                "startDate", updatedDto.getStartDate(),
                "endDate", updatedDto.getEndDate(),
                "reason", updatedDto.getReason(),
                "status", updatedDto.getStatus()
        );

        emailService.sendEmail(
                updatedDto.getUserEmail(),
                "Your Leave Request Has Been Updated",
                "leaveRequestUpdateEmail", // Create this template if needed
                templateModel
        );

        return updatedDto;
    }

    @Override
    public int getLeaveBalance(Long userId) {
        // Implement logic to calculate leave balance
        // For example:
        UserDTO user = userService.getUserById(userId);
        // Assume each employee has 20 leave days per year
        return 20 - leaveRequestRepository.findApprovedLeavesByUserId(userId);
    }

    @Override
    public void delete(Long id) {
        leaveRequestRepository.deleteById(id);
    }

    private LeaveRequestDTO convertToDTO(LeaveRequest leaveRequest) {
        return LeaveRequestDTO.builder()
                .id(leaveRequest.getId())
                .userId(leaveRequest.getUser().getId())
                .userEmail(leaveRequest.getUser().getEmail())
                .username(leaveRequest.getUser().getUsername())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .type(leaveRequest.getType())
                .status(leaveRequest.getStatus())
                .reason(leaveRequest.getReason())
                .createdAt(leaveRequest.getCreatedAt())
                .updatedAt(leaveRequest.getUpdatedAt())
                .build();
    }
}
