package com.react.project.Repository;

import com.react.project.Enumirator.TaskStatus;
import com.react.project.Model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    List<Task> findByTeamId(Long teamId);
    List<Task> findByAssignedToId(Long userId);
    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);
    List<Task> findByAssignedToIdAndStatus(Long userId, TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.deadline <= :date AND t.status NOT IN (com.react.project.Enumirator.TaskStatus.DONE)")
    List<Task> findOverdueTasks(LocalDate date);

    @Query("SELECT t FROM Task t WHERE t.deadline BETWEEN :from AND :to AND t.status NOT IN (com.react.project.Enumirator.TaskStatus.DONE)")
    List<Task> findTasksDueBetween(LocalDate from, LocalDate to);

    long countByProjectIdAndStatus(Long projectId, TaskStatus status);
}
