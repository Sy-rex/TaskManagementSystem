package com.sobolev.spring.taskmanagementsystem.repository;

import com.sobolev.spring.taskmanagementsystem.model.Task;
import com.sobolev.spring.taskmanagementsystem.model.TaskAssignee;
import com.sobolev.spring.taskmanagementsystem.model.User;
import com.sobolev.spring.taskmanagementsystem.model.embedded.TaskAssigneeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskAssigneeRepository extends JpaRepository<TaskAssignee, TaskAssigneeId> {
    List<TaskAssignee> findAllByUser(User user);

    @Query("SELECT ta.task FROM TaskAssignee ta WHERE ta.user = :user")
    List<Task> findTasksByUser(@Param("user") User user);
}
