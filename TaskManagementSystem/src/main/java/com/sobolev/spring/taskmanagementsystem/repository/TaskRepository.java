package com.sobolev.spring.taskmanagementsystem.repository;

import com.sobolev.spring.taskmanagementsystem.model.Task;
import com.sobolev.spring.taskmanagementsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findAllByAssignees(User assignedUser);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignees WHERE t.id = :id")
    Optional<Task> findByIdWithAssignees(@Param("id") Integer id);

    @Query("SELECT DISTINCT t FROM Task t " +
            "LEFT JOIN FETCH t.assignees " +
            "LEFT JOIN FETCH t.author " +
            "WHERE t IN (SELECT ta.task FROM TaskAssignee ta WHERE ta.user = :user)")
    List<Task> findTasksByAssigneeWithDetails(@Param("user") User user);
}
