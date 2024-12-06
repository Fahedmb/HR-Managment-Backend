package com.react.project.ServiceImpl;

import com.react.project.DTO.TimesheetScheduleDTO;
import com.react.project.Enumirator.TimesheetStatus;
import com.react.project.Exception.UserException;
import com.react.project.Mapper.TimesheetScheduleMapper;
import com.react.project.Model.TimesheetSchedule;
import com.react.project.Model.User;
import com.react.project.Repository.TimesheetScheduleRepository;
import com.react.project.Repository.UserRepository;
import com.react.project.Service.TimesheetScheduleService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class TimesheetScheduleServiceImpl implements TimesheetScheduleService {

    private final TimesheetScheduleRepository repo;
    private final UserRepository userRepository;
    private static final int TOTAL_HOURS_PER_WEEK = 40;

    public TimesheetScheduleServiceImpl(TimesheetScheduleRepository repo, UserRepository userRepository) {
        this.repo = repo;
        this.userRepository = userRepository;
    }

    @Override
    public TimesheetScheduleDTO create(TimesheetScheduleDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UserException("User not found"));
        List<TimesheetSchedule> schedules = repo.findByUserId(user.getId());
        if (!schedules.isEmpty()) throw new UserException("User already has a schedule.");
        int daysCount = dto.getChosenDays().size();
        int hoursPerDay = TOTAL_HOURS_PER_WEEK / daysCount;
        TimesheetSchedule entity = TimesheetScheduleMapper.toEntity(dto, user);
        entity.setTotalHoursPerWeek(TOTAL_HOURS_PER_WEEK);
        entity.setHoursPerDay(hoursPerDay);
        entity.setStatus(TimesheetStatus.PENDING);
        TimesheetSchedule saved = repo.save(entity);
        return TimesheetScheduleMapper.toDTO(saved);
    }

    @Override
    public TimesheetScheduleDTO update(Long id, TimesheetScheduleDTO dto) {
        TimesheetSchedule schedule = repo.findById(id)
                .orElseThrow(() -> new UserException("Schedule not found"));
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UserException("User not found"));
        int daysCount = dto.getChosenDays().size();
        int hoursPerDay = TOTAL_HOURS_PER_WEEK / daysCount;
        schedule.setChosenDays(dto.getChosenDays());
        schedule.setStartTime(dto.getStartTime());
        schedule.setTotalHoursPerWeek(TOTAL_HOURS_PER_WEEK);
        schedule.setHoursPerDay(hoursPerDay);
        if (schedule.getStatus() == TimesheetStatus.APPROVED) {
            schedule.setStatus(TimesheetStatus.PENDING);
        }
        TimesheetSchedule updated = repo.save(schedule);
        return TimesheetScheduleMapper.toDTO(updated);
    }

    @Override
    public TimesheetScheduleDTO updateStatus(Long id, TimesheetScheduleDTO dto) {
        TimesheetSchedule schedule = repo.findById(id)
                .orElseThrow(() -> new UserException("Schedule not found"));
        schedule.setStatus(dto.getStatus());
        TimesheetSchedule updated = repo.save(schedule);
        return TimesheetScheduleMapper.toDTO(updated);
    }

    @Override
    public TimesheetScheduleDTO requestDeletion(Long id, Long userId) {
        TimesheetSchedule schedule = repo.findById(id)
                .orElseThrow(() -> new UserException("Schedule not found"));
        if (!schedule.getUser().getId().equals(userId)) throw new UserException("Not allowed.");
        if (schedule.getStatus() == TimesheetStatus.PENDING) throw new UserException("Wait for HR review.");
        schedule.setStatus(TimesheetStatus.PENDING_DELETION);
        TimesheetSchedule updated = repo.save(schedule);
        return TimesheetScheduleMapper.toDTO(updated);
    }

    @Override
    public Optional<TimesheetScheduleDTO> getActiveScheduleForUser(Long userId) {
        List<TimesheetSchedule> schedules = repo.findByUserId(userId);
        if (schedules.isEmpty()) return Optional.empty();
        return schedules.stream()
                .max(Comparator.comparing(TimesheetSchedule::getCreatedAt))
                .map(TimesheetScheduleMapper::toDTO);
    }
}
