package com.react.project.Controller;

import com.react.project.DTO.PerformanceEvaluationDTO;
import com.react.project.Service.PerformanceEvaluationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/performance-evaluations")
public class PerformanceEvaluationController {

    private final PerformanceEvaluationService performanceEvaluationService;

    public PerformanceEvaluationController(PerformanceEvaluationService performanceEvaluationService) {
        this.performanceEvaluationService = performanceEvaluationService;
    }

    @GetMapping("/{id}")
    public PerformanceEvaluationDTO getPerformanceEvaluationById(@PathVariable Long id) {
        return performanceEvaluationService.findById(id);
    }

    @GetMapping("/user/{userId}")
    public List<PerformanceEvaluationDTO> getPerformanceEvaluationsByUserId(@PathVariable Long userId) {
        return performanceEvaluationService.findByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PerformanceEvaluationDTO createPerformanceEvaluation(@RequestBody PerformanceEvaluationDTO dto) {
        return performanceEvaluationService.create(dto);
    }

    @PutMapping("/{id}")
    public PerformanceEvaluationDTO updatePerformanceEvaluation(@PathVariable Long id, @RequestBody PerformanceEvaluationDTO dto) {
        return performanceEvaluationService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePerformanceEvaluation(@PathVariable Long id) {
        performanceEvaluationService.delete(id);
    }
}
