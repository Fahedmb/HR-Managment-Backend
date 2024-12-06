package com.react.project.Repository;

import com.react.project.Model.TimesheetSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimesheetScheduleRepository extends JpaRepository<TimesheetSchedule, Long> {
    List<TimesheetSchedule> findByUserId(Long userId);
}
