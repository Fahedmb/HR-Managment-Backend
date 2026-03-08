package com.react.project.ServiceImpl;

import com.react.project.DTO.LeaveRequestDTO;
import com.react.project.Enumirator.LeaveStatus;
import com.react.project.Enumirator.NotificationType;
import com.react.project.Exception.UserException;
import com.react.project.Model.LeaveRequest;
import com.react.project.Model.User;
import com.react.project.Repository.LeaveRequestRepository;
import com.react.project.Service.EmailService;
import com.react.project.Service.LeaveRequestService;
import com.react.project.Service.NotificationService;
import com.react.project.Service.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    private static final int TOTAL_LEAVE_DAYS = 30;

    // ── Read ───────────────────────────────────────────────────────────

    @Override
    public List<LeaveRequestDTO> findAll() {
        return leaveRequestRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LeaveRequestDTO findById(Long id) {
        return convertToDTO(leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave Request not found")));
    }

    @Override
    public List<LeaveRequestDTO> findByUserId(Long userId) {
        return leaveRequestRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ── Balance helper ─────────────────────────────────────────────────

    /** Computes remaining leave days using the corrected SQL query. */
    @Override
    public int getLeaveBalance(Long userId) {
        int used = leaveRequestRepository.sumApprovedLeaveDays(userId);
        return TOTAL_LEAVE_DAYS - used;
    }

    /** Days requested by the given DTO (1 for half-day, inclusive day-count otherwise). */
    private int daysRequested(LeaveRequestDTO dto) {
        if (Boolean.TRUE.equals(dto.getHalfDay())) return 1;
        return (int) ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
    }

    private int daysOf(LeaveRequest lr) {
        if (Boolean.TRUE.equals(lr.getHalfDay())) return 1;
        return (int) ChronoUnit.DAYS.between(lr.getStartDate(), lr.getEndDate()) + 1;
    }

    // ── Create ─────────────────────────────────────────────────────────

    @Override
    public LeaveRequestDTO create(LeaveRequestDTO dto) throws MessagingException {
        int requested = daysRequested(dto);
        int balance   = getLeaveBalance(dto.getUserId());

        if (requested > balance) {
            throw new UserException(
                "Insufficient leave balance. Requested: " + requested +
                " day(s), Available: " + balance + " day(s).");
        }

        LeaveRequest lr = new LeaveRequest();
        lr.setUser(userService.getUserEntityById(dto.getUserId()));
        lr.setStartDate(dto.getStartDate());
        lr.setEndDate(dto.getEndDate());
        lr.setType(dto.getType());
        lr.setStatus(LeaveStatus.PENDING);
        lr.setReason(dto.getReason());
        lr.setHalfDay(dto.getHalfDay() != null && dto.getHalfDay());
        leaveRequestRepository.save(lr);

        LeaveRequestDTO saved = convertToDTO(lr);

        try {
            emailService.sendEmail(saved.getUserEmail(), "Leave Request Submitted",
                    "leaveRequestEmail", Map.of(
                            "username",       saved.getUsername(),
                            "leaveRequestId", saved.getId(),
                            "startDate",      saved.getStartDate(),
                            "endDate",        saved.getEndDate(),
                            "reason",         saved.getReason()
                    ));
        } catch (Exception ignored) { /* email failure must not break the API */ }

        notificationService.create(lr.getUser().getId(),
                "Your leave request from " + lr.getStartDate() + " to " + lr.getEndDate() + " has been submitted.",
                NotificationType.LEAVE_REQUEST_SUBMITTED);

        return saved;
    }

    // ── Update status (HR action) ──────────────────────────────────────

    @Override
    public LeaveRequestDTO updateStatus(Long id, LeaveRequestDTO dto) throws MessagingException {
        LeaveRequest lr = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave Request not found"));

        // Guard: before approving, make sure the user still has enough balance
        if (dto.getStatus() == LeaveStatus.APPROVED) {
            int days    = daysOf(lr);
            int balance = getLeaveBalance(lr.getUser().getId());
            if (days > balance) {
                throw new UserException(
                    "Cannot approve: insufficient leave balance. Requested: " + days +
                    " day(s), Available: " + balance + " day(s).");
            }
        }

        // If a previously-approved request is now being rejected, restore the days
        if (lr.getStatus() == LeaveStatus.APPROVED && dto.getStatus() == LeaveStatus.REJECTED) {
            userService.decrementUsedLeaveDays(lr.getUser().getId(), daysOf(lr));
        }

        lr.setStatus(dto.getStatus());

        if (dto.getApprovedById() != null) {
            lr.setApprovedBy(userService.getUserEntityById(dto.getApprovedById()));
        }
        if (dto.getApproverComment() != null) {
            lr.setApproverComment(dto.getApproverComment());
        }

        leaveRequestRepository.save(lr);

        if (dto.getStatus() == LeaveStatus.APPROVED) {
            userService.incrementUsedLeaveDays(lr.getUser().getId(), daysOf(lr));
        }

        LeaveRequestDTO updated = convertToDTO(lr);

        String templateName = null;
        String subject = null;
        NotificationType notifType = null;

        if (dto.getStatus() == LeaveStatus.APPROVED) {
            templateName = "approvalEmail";
            subject = "Your Leave Request Has Been Approved";
            notifType = NotificationType.LEAVE_REQUEST_APPROVED;
        } else if (dto.getStatus() == LeaveStatus.REJECTED) {
            templateName = "rejectionEmail";
            subject = "Your Leave Request Has Been Rejected";
            notifType = NotificationType.LEAVE_REQUEST_REJECTED;
        }

        if (templateName != null) {
            try {
                emailService.sendEmail(updated.getUserEmail(), subject, templateName, Map.of(
                        "username",   updated.getUsername(),
                        "requestId",  updated.getId(),
                        "type",       "Leave Request",
                        "date",       updated.getStartDate()
                ));
            } catch (Exception ignored) { /* email failure must not break the API */ }
            notificationService.create(lr.getUser().getId(), subject, notifType);
        }

        return updated;
    }

    // ── Update (employee edits pending request) ────────────────────────

    @Override
    public LeaveRequestDTO update(Long id, LeaveRequestDTO dto) throws MessagingException {
        LeaveRequest lr = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave Request not found"));

        // Only allow edits while still pending
        if (lr.getStatus() != LeaveStatus.PENDING) {
            throw new UserException("Only PENDING requests can be edited.");
        }

        int requested = daysRequested(dto);
        int balance   = getLeaveBalance(lr.getUser().getId());
        if (requested > balance) {
            throw new UserException(
                "Insufficient leave balance. Requested: " + requested +
                " day(s), Available: " + balance + " day(s).");
        }

        lr.setStartDate(dto.getStartDate());
        lr.setEndDate(dto.getEndDate());
        lr.setType(dto.getType());
        lr.setReason(dto.getReason());
        lr.setHalfDay(dto.getHalfDay() != null && dto.getHalfDay());
        leaveRequestRepository.save(lr);
        return convertToDTO(lr);
    }

    // ── Cancel (employee withdraws) ────────────────────────────────────

    @Override
    public LeaveRequestDTO cancel(Long id, String reason) throws MessagingException {
        LeaveRequest lr = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave Request not found"));

        // If the request was already approved, give the days back
        if (lr.getStatus() == LeaveStatus.APPROVED) {
            userService.decrementUsedLeaveDays(lr.getUser().getId(), daysOf(lr));
        }

        lr.setStatus(LeaveStatus.CANCELLED);
        lr.setCancellationReason(reason);
        leaveRequestRepository.save(lr);

        notificationService.create(lr.getUser().getId(),
                "Your leave request has been cancelled.", NotificationType.LEAVE_REQUEST_CANCELLED);
        return convertToDTO(lr);
    }

    // ── Balance ────────────────────────────────────────────────────────

    @Override
    public void delete(Long id) {
        leaveRequestRepository.deleteById(id);
    }

    // ── Mapping ────────────────────────────────────────────────────────

    private LeaveRequestDTO convertToDTO(LeaveRequest lr) {
        User user = lr.getUser();
        long daysCount = lr.getStartDate() != null && lr.getEndDate() != null
                ? ChronoUnit.DAYS.between(lr.getStartDate(), lr.getEndDate()) + 1 : 0;
        if (Boolean.TRUE.equals(lr.getHalfDay())) daysCount = 1;

        LeaveRequestDTO.LeaveRequestDTOBuilder builder = LeaveRequestDTO.builder()
                .id(lr.getId())
                .userId(user.getId())
                .userEmail(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .startDate(lr.getStartDate())
                .endDate(lr.getEndDate())
                .type(lr.getType())
                .status(lr.getStatus())
                .reason(lr.getReason())
                .halfDay(lr.getHalfDay())
                .daysCount((int) daysCount)
                .cancellationReason(lr.getCancellationReason())
                .approverComment(lr.getApproverComment())
                .createdAt(lr.getCreatedAt())
                .updatedAt(lr.getUpdatedAt());

        if (lr.getApprovedBy() != null) {
            builder.approvedById(lr.getApprovedBy().getId())
                   .approvedByName(lr.getApprovedBy().getFirstName() + " " + lr.getApprovedBy().getLastName());
        }

        return builder.build();
    }
}
