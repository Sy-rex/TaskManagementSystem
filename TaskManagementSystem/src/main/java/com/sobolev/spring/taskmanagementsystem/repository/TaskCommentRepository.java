package com.sobolev.spring.taskmanagementsystem.repository;

import com.sobolev.spring.taskmanagementsystem.model.TaskComment;
import org.hibernate.id.IntegralDataTypeHolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Integer> {
    List<TaskComment> findByTaskIdOrderByCreatedAtDesc(Integer taskId);

    @Query("SELECT tc FROM TaskComment tc JOIN FETCH tc.user WHERE tc.task.id = :taskId")
    List<TaskComment> findByTaskIdWithUser(@Param("taskId") Integer taskId);
}
