// File: src/main/java/com/react/project/Controller/TimeSheetController.java
package com.react.project.Controller;

import com.react.project.DTO.TimeSheetDTO;
import com.react.project.Service.TimeSheetService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/time-sheets")
public class TimeSheetController {
    private final TimeSheetService service;

    public TimeSheetController(TimeSheetService s) {
        this.service = s;
    }

    @GetMapping
    public List<TimeSheetDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TimeSheetDTO getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/user/{uid}")
    public List<TimeSheetDTO> getByUser(@PathVariable Long uid) {
        return service.findByUserId(uid);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimeSheetDTO create(@RequestBody TimeSheetDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public TimeSheetDTO update(@PathVariable Long id, @RequestBody TimeSheetDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
