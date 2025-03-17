package com.sobolev.spring.taskmanagementsystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "roles")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(unique = true, nullable = false, length = 100, name = "name")
    private String name;
}
