package com.sobolev.spring.taskmanagementsystem.repository;

import com.sobolev.spring.taskmanagementsystem.model.TaskAssignee;
import com.sobolev.spring.taskmanagementsystem.model.embedded.TaskAssigneeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TaskAssigneeRepository extends JpaRepository<TaskAssignee, TaskAssigneeId> {
    void deleteByTaskId(Integer taskId);
}
