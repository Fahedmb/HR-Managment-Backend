package com.react.project.ServiceImpl;

import com.react.project.Model.Report;
import com.react.project.Model.User;
import com.react.project.DTO.ReportDTO;
import com.react.project.Mapper.ReportMapper;
import com.react.project.Repository.ReportRepository;
import com.react.project.Repository.UserRepository;
import com.react.project.Service.ReportService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public ReportServiceImpl(ReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<ReportDTO> findByGeneratedById(Long generatedById) {
        return reportRepository.findByGeneratedById(generatedById)
                .stream()
                .map(ReportMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReportDTO create(ReportDTO reportDTO) {
        User generatedBy = userRepository.findById(reportDTO.getGeneratedById())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Report report = ReportMapper.toEntity(reportDTO, generatedBy);
        Report savedReport = reportRepository.save(report);
        return ReportMapper.toDTO(savedReport);
    }

    @Override
    public void delete(Long id) {
        reportRepository.deleteById(id);
    }
}
