package com.tasknest.dto;

import com.tasknest.entity.TaskPriority;
import com.tasknest.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Request payload for creating or updating a task")
public class TaskRequestDto {

    @Schema(description = "Title of the task", example = "Complete sprint planning", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 150)
    @NotBlank(message = "Title must not be null or blank")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @Schema(description = "Detailed description of the task", example = "Review backlog items and assign story points", maxLength = 2000)
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Schema(description = "Status of the task. Defaults to PENDING if omitted.", example = "PENDING")
    private TaskStatus status;

    @Schema(description = "Priority level of the task. Defaults to MEDIUM if omitted.", example = "HIGH")
    private TaskPriority priority;

    @Schema(description = "Task due date and time (must be present or future)", example = "2026-10-15T18:00:00")
    @FutureOrPresent(message = "Due date must be in the present or future")
    private LocalDateTime dueDate;

    public TaskRequestDto() {
    }

    public TaskRequestDto(String title, String description, TaskStatus status, TaskPriority priority, LocalDateTime dueDate) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
}
