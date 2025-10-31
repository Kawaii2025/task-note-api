package com.taskmanager.service;

import com.taskmanager.dto.TaskDTO;
import com.taskmanager.entity.Tag;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TagRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<Task> getAllTasks() {
        User user = getCurrentUser();
        return taskRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Task> getTasksByStatus(Boolean completed) {
        User user = getCurrentUser();
        return taskRepository.findByUserAndCompletedOrderByCreatedAtDesc(user, completed);
    }

    public List<Task> searchTasks(String search) {
        User user = getCurrentUser();
        return taskRepository.searchTasks(user, search);
    }

    public Task getTaskById(UUID id) {
        User user = getCurrentUser();
        return taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    @Transactional
    public Task createTask(TaskDTO.CreateTaskRequest request) {
        User user = getCurrentUser();

        Task task = new Task();
        task.setUser(user);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());

        // Handle tags
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (UUID tagId : request.getTagIds()) {
                Tag tag = tagRepository.findByIdAndUser(tagId, user)
                        .orElseThrow(() -> new RuntimeException("Tag not found: " + tagId));
                tags.add(tag);
            }
            task.setTags(tags);
        }

        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(UUID id, TaskDTO.UpdateTaskRequest request) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getCompleted() != null) {
            task.setCompleted(request.getCompleted());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }

        // Update tags
        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>();
            for (UUID tagId : request.getTagIds()) {
                Tag tag = tagRepository.findByIdAndUser(tagId, user)
                        .orElseThrow(() -> new RuntimeException("Tag not found: " + tagId));
                tags.add(tag);
            }
            task.setTags(tags);
        }

        return taskRepository.save(task);
    }

    @Transactional
    public Task toggleTaskCompletion(UUID id) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setCompleted(!task.getCompleted());
        return taskRepository.save(task);
    }

    public void deleteTask(UUID id) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        taskRepository.delete(task);
    }
}