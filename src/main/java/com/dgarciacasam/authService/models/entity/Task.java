package com.dgarciacasam.authService.models.entity;

import java.time.LocalDate;

import com.dgarciacasam.authService.models.TaskStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "tasks")
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String color;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;
}