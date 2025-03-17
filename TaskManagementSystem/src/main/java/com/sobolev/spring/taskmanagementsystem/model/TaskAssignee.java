package com.sobolev.spring.taskmanagementsystem.model;

import com.sobolev.spring.taskmanagementsystem.model.embedded.TaskAssigneeId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "task_assignees")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignee {
    @EmbeddedId
    private TaskAssigneeId id;

    @ManyToOne
    @MapsId("taskId")
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "assigned_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date assignedAt;
}
