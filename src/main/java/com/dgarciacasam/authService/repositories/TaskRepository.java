package com.dgarciacasam.authService.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dgarciacasam.authService.models.TaskStatus;
import com.dgarciacasam.authService.models.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Task findByTitle(String title);

    Task findByStatus(Enum<TaskStatus> status);

    List<Task> findByAssignedUser_id(Long userId);
}
