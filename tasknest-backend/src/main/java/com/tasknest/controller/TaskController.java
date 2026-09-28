package com.tasknest.controller;

import com.tasknest.dto.ErrorResponse;
import com.tasknest.dto.TaskPageResponseDto;
import com.tasknest.dto.TaskRequestDto;
import com.tasknest.dto.TaskResponseDto;
import com.tasknest.entity.TaskPriority;
import com.tasknest.entity.TaskStatus;
import com.tasknest.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "Tasks", description = "Endpoints for managing personal to-do items and tasks")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Create a new task", description = "Creates a new task and returns the persisted entity with generated ID and timestamps")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation failure",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto request) {
        TaskResponseDto createdTask = taskService.createTask(request);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all tasks with filtering, sorting, and pagination",
            description = "Retrieves tasks filtered by status, priority, and date range with database-level pagination and sorting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskPageResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid query parameter or sort field",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<TaskPageResponseDto> getAllTasks(
            @Parameter(description = "Filter by task status: PENDING, IN_PROGRESS, COMPLETED")
            @RequestParam(required = false) TaskStatus status,

            @Parameter(description = "Filter by task priority: LOW, MEDIUM, HIGH")
            @RequestParam(required = false) TaskPriority priority,

            @Parameter(description = "Filter tasks with due date greater than or equal to this timestamp (ISO-8601 format)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDateFrom,

            @Parameter(description = "Filter tasks with due date less than or equal to this timestamp (ISO-8601 format)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDateTo,

            @Parameter(description = "Zero-based page index (must not be negative, default: 0)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page (1 to 100, default: 10)")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Field to sort by: id, title, priority, status, dueDate, createdAt, updatedAt (default: createdAt)")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction: asc or desc (default: desc)")
            @RequestParam(defaultValue = "desc") String direction
    ) {
        TaskPageResponseDto response = taskService.getTasks(
                status, priority, dueDateFrom, dueDateTo, page, size, sortBy, direction
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get task by ID", description = "Retrieves a single task by its unique numeric ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found and returned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Task not found with provided ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(
            @Parameter(description = "Unique ID of the task", required = true, example = "1")
            @PathVariable Long id) {
        TaskResponseDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @Operation(summary = "Update an existing task", description = "Updates title, description, status, priority, and due date of a task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation failure",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Task not found with provided ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @Parameter(description = "Unique ID of the task to update", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDto request) {
        TaskResponseDto updatedTask = taskService.updateTask(id, request);
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(summary = "Delete a task by ID", description = "Deletes a task by its unique numeric ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully (no content returned)"),
            @ApiResponse(responseCode = "404", description = "Task not found with provided ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Unique ID of the task to delete", required = true, example = "1")
            @PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
