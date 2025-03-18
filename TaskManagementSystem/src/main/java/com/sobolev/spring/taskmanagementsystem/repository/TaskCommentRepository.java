package com.sobolev.spring.taskmanagementsystem.repository;

import com.sobolev.spring.taskmanagementsystem.model.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Integer> {
    List<TaskComment> findByTaskIdOrderByCreatedAtDesc(Integer taskId);

    void deleteByTaskId(Integer taskId);
}
