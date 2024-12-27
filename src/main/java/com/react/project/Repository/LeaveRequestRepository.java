package com.react.project.Repository;

import com.react.project.Model.LeaveRequest;
import com.react.project.Enumirator.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByUserId(Long userId);
    List<LeaveRequest> findByUserIdAndStatus(Long userId, LeaveStatus status);

    default int findApprovedLeavesByUserId(Long userId) {
        return findByUserIdAndStatus(userId, LeaveStatus.APPROVED).size();
    }
}
