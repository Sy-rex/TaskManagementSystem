package com.sobolev.spring.taskmanagementsystem.model;

import com.sobolev.spring.taskmanagementsystem.util.TaskPriority;
import com.sobolev.spring.taskmanagementsystem.util.TaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,name = "status")
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,name = "priority")
    private TaskPriority priority;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskAssignee> assignees = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskComment> comments = new ArrayList<>();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }
}
