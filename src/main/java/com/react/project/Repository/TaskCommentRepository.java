package com.react.project.Repository;

import com.react.project.Model.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    List<TaskComment> findByTaskIdOrderByCreatedAtAsc(Long taskId);

    @Modifying
    @Transactional
    @Query("UPDATE TaskComment tc SET tc.author = null WHERE tc.author.id = :userId")
    void nullifyAuthor(@Param("userId") Long userId);
}
