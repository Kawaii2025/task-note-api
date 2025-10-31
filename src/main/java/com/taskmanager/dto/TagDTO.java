package com.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

public class TagDTO {

    @Data
    public static class CreateTagRequest {
        @NotBlank(message = "Tag name is required")
        private String name;

        @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color must be valid hex format")
        private String color = "#3b82f6";
    }

    @Data
    public static class UpdateTagRequest {
        private String name;

        @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color must be valid hex format")
        private String color;
    }
}