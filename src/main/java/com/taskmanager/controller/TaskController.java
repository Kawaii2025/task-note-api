package com.taskmanager.controller;

import com.taskmanager.dto.ApiResponse;
import com.taskmanager.dto.TaskDTO;
import com.taskmanager.entity.Task;
import com.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/test")
    public String test() {
        return "controller ok";
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Task>>> getAllTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        try {
            List<Task> tasks;

            if (search != null && !search.isEmpty()) {
                tasks = taskService.searchTasks(search);
            } else if (status != null) {
                Boolean completed = status.equals("completed");
                tasks = taskService.getTasksByStatus(completed);
            } else {
                tasks = taskService.getAllTasks();
            }

            return ResponseEntity.ok(ApiResponse.success(tasks));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> getTaskById(@PathVariable UUID id) {
        try {
            Task task = taskService.getTaskById(id);
            return ResponseEntity.ok(ApiResponse.success(task));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Task>> createTask(
            @Valid @RequestBody TaskDTO.CreateTaskRequest request) {
        try {
            Task task = taskService.createTask(request);
            return ResponseEntity.ok(ApiResponse.success("Task created successfully", task));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody TaskDTO.UpdateTaskRequest request) {
        try {
            Task task = taskService.updateTask(id, request);
            return ResponseEntity.ok(ApiResponse.success("Task updated successfully", task));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Task>> toggleTaskCompletion(@PathVariable UUID id) {
        try {
            Task task = taskService.toggleTaskCompletion(id);
            return ResponseEntity.ok(ApiResponse.success("Task status updated", task));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable UUID id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}