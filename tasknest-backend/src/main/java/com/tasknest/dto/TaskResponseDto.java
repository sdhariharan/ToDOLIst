package com.tasknest.dto;

import com.tasknest.entity.TaskPriority;
import com.tasknest.entity.TaskStatus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Response payload representing a task")
public class TaskResponseDto {

    @Schema(description = "Unique identifier of the task", example = "1")
    private Long id;

    @Schema(description = "Title of the task", example = "Complete sprint planning")
    private String title;

    @Schema(description = "Detailed description of the task", example = "Review backlog items and assign story points")
    private String description;

    @Schema(description = "Current status of the task", example = "PENDING")
    private TaskStatus status;

    @Schema(description = "Priority level of the task", example = "HIGH")
    private TaskPriority priority;

    @Schema(description = "Due date and time of the task", example = "2026-10-15T18:00:00")
    private LocalDateTime dueDate;

    @Schema(description = "Timestamp when the task was created", example = "2026-09-28T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the task was last updated", example = "2026-09-28T14:30:00")
    private LocalDateTime updatedAt;

    public TaskResponseDto() {
    }

    public TaskResponseDto(Long id, String title, String description, TaskStatus status,
                           TaskPriority priority, LocalDateTime dueDate,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
