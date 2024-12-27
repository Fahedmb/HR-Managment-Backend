// File: src/main/java/com/react/project/Repository/LeaveRequestRepository.java
package com.react.project.Repository;

import com.react.project.Model.LeaveRequest;
import com.react.project.Enumirator.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByUserId(Long userId);
    List<LeaveRequest> findByUserIdAndStatus(Long userId, LeaveStatus status);

    @Query("SELECT SUM(L.endDate - L.startDate) FROM LeaveRequest L WHERE L.user.id = :userId AND L.status = 'APPROVED'")
    Integer sumApprovedLeaveDays(Long userId);
}
