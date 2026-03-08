package com.react.project.Repository;

import com.react.project.Model.TimesheetSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TimesheetScheduleRepository extends JpaRepository<TimesheetSchedule, Long> {
    List<TimesheetSchedule> findByUserId(Long userId);

    @Transactional
    void deleteByUserId(Long userId);
}
