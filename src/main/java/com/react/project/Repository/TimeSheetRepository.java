package com.react.project.Repository;

import com.react.project.Model.TimeSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TimeSheetRepository extends JpaRepository<TimeSheet, Long> {
    List<TimeSheet> findByUserId(Long userId);

    @Transactional
    void deleteByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE TimeSheet t SET t.approvedBy = null WHERE t.approvedBy.id = :userId")
    void nullifyApprovedBy(@Param("userId") Long userId);
}
