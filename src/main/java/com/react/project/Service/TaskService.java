package com.react.project.Service;

import com.react.project.DTO.TaskCommentDTO;
import com.react.project.DTO.TaskDTO;
import com.react.project.Enumirator.TaskStatus;

import java.util.List;

public interface TaskService {
    List<TaskDTO> findAll();
    TaskDTO findById(Long id);
    List<TaskDTO> findByProject(Long projectId);
    List<TaskDTO> findByTeam(Long teamId);
    List<TaskDTO> findByAssignee(Long userId);
    TaskDTO create(TaskDTO dto);
    TaskDTO update(Long id, TaskDTO dto);
    TaskDTO updateStatus(Long id, TaskStatus status, Long updatedByUserId);
    void delete(Long id);

    TaskCommentDTO addComment(Long taskId, Long authorId, String content);
    List<TaskCommentDTO> getComments(Long taskId);
    void deleteComment(Long commentId);
}
