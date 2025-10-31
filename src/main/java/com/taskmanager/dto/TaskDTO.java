package com.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public class TaskDTO {

    @Data
    public static class CreateTaskRequest {
        @NotBlank(message = "Title is required")
        private String title;
        private String description;
        private String priority = "medium";
        private LocalDate dueDate;
        private Set<UUID> tagIds;
    }

    @Data
    public static class UpdateTaskRequest {
        private String title;
        private String description;
        private String priority;
        private Boolean completed;
        private LocalDate dueDate;
        private Set<UUID> tagIds;
    }
}