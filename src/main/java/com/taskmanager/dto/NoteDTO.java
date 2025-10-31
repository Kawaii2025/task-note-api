package com.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

public class NoteDTO {

    @Data
    public static class CreateNoteRequest {
        @NotBlank(message = "Title is required")
        private String title;
        private String content;
        private Set<UUID> tagIds;
    }

    @Data
    public static class UpdateNoteRequest {
        private String title;
        private String content;
        private Boolean isPinned;
        private Set<UUID> tagIds;
    }
}