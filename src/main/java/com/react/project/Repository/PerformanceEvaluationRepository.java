package com.react.project.Repository;

import com.react.project.Model.PerformanceEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PerformanceEvaluationRepository extends JpaRepository<PerformanceEvaluation, Long> {
    List<PerformanceEvaluation> findByUserId(Long userId);

    @Transactional
    void deleteByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE PerformanceEvaluation pe SET pe.evaluator = null WHERE pe.evaluator.id = :userId")
    void nullifyEvaluator(@Param("userId") Long userId);
}
