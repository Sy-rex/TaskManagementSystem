package com.sobolev.spring.taskmanagementsystem.model.embedded;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssigneeId implements Serializable {
    private Integer taskId;
    private Integer userId;
}
