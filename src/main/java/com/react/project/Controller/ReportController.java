package com.react.project.Controller;

import com.react.project.DTO.ReportDTO;
import com.react.project.Service.ReportService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/generated-by/{generatedById}")
    public List<ReportDTO> getReportsByGeneratedById(@PathVariable Long generatedById) {
        return reportService.findByGeneratedById(generatedById);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportDTO createReport(@RequestBody ReportDTO reportDTO) {
        return reportService.create(reportDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReport(@PathVariable Long id) {
        reportService.delete(id);
    }
}
