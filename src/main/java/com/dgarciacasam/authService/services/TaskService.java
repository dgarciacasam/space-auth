package com.dgarciacasam.authService.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.dgarciacasam.authService.models.UserPrincipal;
import com.dgarciacasam.authService.models.entity.Task;
import com.dgarciacasam.authService.models.entity.User;
import com.dgarciacasam.authService.repositories.TaskRepository;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    public List<Task> getTasks() {
        Long currentUserId = getCurrentUserId();
        return taskRepository.findByAssignedUser_id(currentUserId);
    }

    public List<Task> getTasksByUser(Long userId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null || !currentUserId.equals(userId)) {
            return List.of();
        }
        return taskRepository.findByAssignedUser_id(currentUserId);
    }

    public Task getTask(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public Task createTask(Task task) {
        Long currentUserId = getCurrentUserId();
        User owner = new User();
        owner.setId(currentUserId);
        task.setAssignedUser(owner);
        return taskRepository.save(task);
    }

    public Task updateTask(Long id, Task task) {
        Task existingTask = taskRepository.findById(id).orElse(null);
        if (existingTask == null) {
            return null;
        }
        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setStatus(task.getStatus());
        existingTask.setColor(task.getColor());
        existingTask.setDate(task.getDate());
        return taskRepository.save(existingTask);
    }

    public void deleteTask(Long id) {
        Task existingTask = taskRepository.findById(id).orElse(null);
        if (existingTask != null) {
            taskRepository.deleteById(id);
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return null;
        }
        return ((UserPrincipal) authentication.getPrincipal()).getId();
    }
}
