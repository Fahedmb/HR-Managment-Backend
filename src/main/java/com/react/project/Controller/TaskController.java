package com.react.project.Controller;

import com.react.project.DTO.TaskCommentDTO;
import com.react.project.DTO.TaskDTO;
import com.react.project.Enumirator.TaskStatus;
import com.react.project.Service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public List<TaskDTO> getAll() {
        return taskService.findAll();
    }

    @GetMapping("/{id}")
    public TaskDTO getById(@PathVariable Long id) {
        return taskService.findById(id);
    }

    @GetMapping("/project/{projectId}")
    public List<TaskDTO> getByProject(@PathVariable Long projectId) {
        return taskService.findByProject(projectId);
    }

    @GetMapping("/team/{teamId}")
    public List<TaskDTO> getByTeam(@PathVariable Long teamId) {
        return taskService.findByTeam(teamId);
    }

    @GetMapping("/assignee/{userId}")
    public List<TaskDTO> getByAssignee(@PathVariable Long userId) {
        return taskService.findByAssignee(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO create(@RequestBody TaskDTO dto) {
        return taskService.create(dto);
    }

    @PutMapping("/{id}")
    public TaskDTO update(@PathVariable Long id, @RequestBody TaskDTO dto) {
        return taskService.update(id, dto);
    }

    @PatchMapping("/{id}/status")
    public TaskDTO updateStatus(@PathVariable Long id,
                                @RequestBody Map<String, String> body,
                                @RequestParam Long updatedByUserId) {
        return taskService.updateStatus(id, TaskStatus.valueOf(body.get("status")), updatedByUserId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }

    // ── Comments ───────────────────────────────────────────────
    @GetMapping("/{taskId}/comments")
    public List<TaskCommentDTO> getComments(@PathVariable Long taskId) {
        return taskService.getComments(taskId);
    }

    @PostMapping("/{taskId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskCommentDTO addComment(@PathVariable Long taskId,
                                     @RequestParam Long authorId,
                                     @RequestBody Map<String, String> body) {
        return taskService.addComment(taskId, authorId, body.get("content"));
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        taskService.deleteComment(commentId);
    }
}
