package com.react.project.Controller;

import com.react.project.DTO.TimeSheetDTO;
import com.react.project.Service.TimeSheetService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/time-sheets")
public class TimeSheetController {

    private final TimeSheetService timeSheetService;

    public TimeSheetController(TimeSheetService timeSheetService) {
        this.timeSheetService = timeSheetService;
    }

    @GetMapping("/{id}")
    public TimeSheetDTO getTimeSheetById(@PathVariable Long id) {
        return timeSheetService.findById(id);
    }

    @GetMapping("/user/{userId}")
    public List<TimeSheetDTO> getTimeSheetsByUserId(@PathVariable Long userId) {
        return timeSheetService.findByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimeSheetDTO createTimeSheet(@RequestBody TimeSheetDTO timeSheetDTO) {
        return timeSheetService.create(timeSheetDTO);
    }

    @PutMapping("/{id}")
    public TimeSheetDTO updateTimeSheet(@PathVariable Long id, @RequestBody TimeSheetDTO timeSheetDTO) {
        return timeSheetService.update(id, timeSheetDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTimeSheet(@PathVariable Long id) {
        timeSheetService.delete(id);
    }
}

