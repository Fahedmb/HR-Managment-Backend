package com.react.project.ServiceImpl;

import com.react.project.Model.PerformanceEvaluation;
import com.react.project.Model.User;
import com.react.project.DTO.PerformanceEvaluationDTO;
import com.react.project.Mapper.PerformanceEvaluationMapper;
import com.react.project.Repository.PerformanceEvaluationRepository;
import com.react.project.Repository.UserRepository;
import com.react.project.Service.PerformanceEvaluationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerformanceEvaluationServiceImpl implements PerformanceEvaluationService {
    private final PerformanceEvaluationRepository evaluationRepository;
    private final UserRepository userRepository;

    public PerformanceEvaluationServiceImpl(PerformanceEvaluationRepository evaluationRepository, UserRepository userRepository) {
        this.evaluationRepository = evaluationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public PerformanceEvaluationDTO findById(Long id) {
        PerformanceEvaluation evaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));
        return PerformanceEvaluationMapper.toDTO(evaluation);
    }

    @Override
    public List<PerformanceEvaluationDTO> findByUserId(Long userId) {
        return evaluationRepository.findByUserId(userId)
                .stream()
                .map(PerformanceEvaluationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PerformanceEvaluationDTO create(PerformanceEvaluationDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        User evaluator = userRepository.findById(dto.getEvaluatorId())
                .orElseThrow(() -> new RuntimeException("Evaluator not found"));
        PerformanceEvaluation evaluation = PerformanceEvaluationMapper.toEntity(dto, user, evaluator);
        PerformanceEvaluation savedEvaluation = evaluationRepository.save(evaluation);
        return PerformanceEvaluationMapper.toDTO(savedEvaluation);
    }

    @Override
    public PerformanceEvaluationDTO update(Long id, PerformanceEvaluationDTO dto) {
        PerformanceEvaluation existingEvaluation = evaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));
        existingEvaluation.setEvaluationDate(dto.getEvaluationDate());
        existingEvaluation.setScore(dto.getScore());
        existingEvaluation.setComments(dto.getComments());
        PerformanceEvaluation updatedEvaluation = evaluationRepository.save(existingEvaluation);
        return PerformanceEvaluationMapper.toDTO(updatedEvaluation);
    }

    @Override
    public void delete(Long id) {
        evaluationRepository.deleteById(id);
    }
}
