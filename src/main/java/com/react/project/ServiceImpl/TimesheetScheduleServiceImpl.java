// TimesheetScheduleServiceImpl.java
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
import java.util.stream.Collectors;

@Service
public class TimesheetScheduleServiceImpl implements TimesheetScheduleService {
    private final TimesheetScheduleRepository repo;
    private final UserRepository userRepo;
    private static final int TOTAL_HOURS_PER_WEEK = 40;

    public TimesheetScheduleServiceImpl(TimesheetScheduleRepository r, UserRepository u) {
        this.repo = r;
        this.userRepo = u;
    }

    @Override
    public List<TimesheetScheduleDTO> findAll() {
        return repo.findAll().stream().map(TimesheetScheduleMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public TimesheetScheduleDTO create(TimesheetScheduleDTO dto) {
        User user = userRepo.findById(dto.getUserId()).orElseThrow(() -> new UserException("User not found"));
        if (!repo.findByUserId(user.getId()).isEmpty()) throw new UserException("Already has schedule");
        int days = dto.getChosenDays().size();
        int hours = TOTAL_HOURS_PER_WEEK / days;
        TimesheetSchedule e = TimesheetScheduleMapper.toEntity(dto, user);
        e.setTotalHoursPerWeek(TOTAL_HOURS_PER_WEEK);
        e.setHoursPerDay(hours);
        e.setStatus(TimesheetStatus.PENDING);
        return TimesheetScheduleMapper.toDTO(repo.save(e));
    }

    @Override
    public TimesheetScheduleDTO update(Long id, TimesheetScheduleDTO dto) {
        TimesheetSchedule s = repo.findById(id).orElseThrow(() -> new UserException("Not found"));
        userRepo.findById(dto.getUserId()).orElseThrow(() -> new UserException("User not found"));
        int days = dto.getChosenDays().size();
        int hours = TOTAL_HOURS_PER_WEEK / days;
        s.setChosenDays(dto.getChosenDays());
        s.setStartTime(dto.getStartTime());
        s.setTotalHoursPerWeek(TOTAL_HOURS_PER_WEEK);
        s.setHoursPerDay(hours);
        if (s.getStatus() == TimesheetStatus.APPROVED) s.setStatus(TimesheetStatus.PENDING);
        return TimesheetScheduleMapper.toDTO(repo.save(s));
    }

    @Override
    public TimesheetScheduleDTO updateStatus(Long id, TimesheetScheduleDTO dto) {
        TimesheetSchedule s = repo.findById(id).orElseThrow(() -> new UserException("Not found"));
        s.setStatus(dto.getStatus());
        return TimesheetScheduleMapper.toDTO(repo.save(s));
    }

    @Override
    public TimesheetScheduleDTO requestDeletion(Long id, Long userId) {
        TimesheetSchedule s = repo.findById(id).orElseThrow(() -> new UserException("Not found"));
        if (!s.getUser().getId().equals(userId)) throw new UserException("Not allowed");
        if (s.getStatus() == TimesheetStatus.PENDING) throw new UserException("Wait for HR review");
        s.setStatus(TimesheetStatus.PENDING_DELETION);
        return TimesheetScheduleMapper.toDTO(repo.save(s));
    }

    @Override
    public Optional<TimesheetScheduleDTO> getActiveScheduleForUser(Long userId) {
        List<TimesheetSchedule> list = repo.findByUserId(userId);
        if (list.isEmpty()) return Optional.empty();
        return list.stream().max(Comparator.comparing(TimesheetSchedule::getCreatedAt)).map(TimesheetScheduleMapper::toDTO);
    }
}
