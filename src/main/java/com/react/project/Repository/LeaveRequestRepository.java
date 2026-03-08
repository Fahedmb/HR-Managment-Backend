// File: src/main/java/com/react/project/Repository/LeaveRequestRepository.java
package com.react.project.Repository;

import com.react.project.Model.LeaveRequest;
import com.react.project.Enumirator.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByUserId(Long userId);
    List<LeaveRequest> findByUserIdAndStatus(Long userId, LeaveStatus status);

    @Query(value = "SELECT COALESCE(SUM(" +
                   "  CASE WHEN lr.half_day = 1 THEN 1 " +
                   "       ELSE DATEDIFF(lr.end_date, lr.start_date) + 1 END" +
                   "), 0) FROM leave_request lr " +
                   "WHERE lr.user_id = :userId AND lr.status = 'APPROVED'",
           nativeQuery = true)
    int sumApprovedLeaveDays(@Param("userId") Long userId);

    @Transactional
    void deleteByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE LeaveRequest lr SET lr.approvedBy = null WHERE lr.approvedBy.id = :userId")
    void nullifyApprovedBy(@Param("userId") Long userId);
}
