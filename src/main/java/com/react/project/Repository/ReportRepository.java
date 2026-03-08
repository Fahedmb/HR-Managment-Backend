package com.react.project.Repository;

import com.react.project.Model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByGeneratedById(Long generatedById);

    @Modifying
    @Transactional
    @Query("UPDATE Report r SET r.generatedBy = null WHERE r.generatedBy.id = :userId")
    void nullifyGeneratedBy(@Param("userId") Long userId);
}
