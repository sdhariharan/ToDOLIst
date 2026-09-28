package com.tasknest.controller;

import com.tasknest.exception.GlobalExceptionHandler;
import com.tasknest.exception.ResourceNotFoundException;
import com.tasknest.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TaskControllerExceptionHandlingTest {

    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getNonexistentTask_returnsHttp404() throws Exception {
        when(taskService.getTaskById(999L))
                .thenThrow(new ResourceNotFoundException("Task not found with id: 999"));

        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task not found with id: 999"))
                .andExpect(jsonPath("$.path").value("/api/tasks/999"));
    }

    @Test
    void createInvalidTask_returnsHttp400WithFieldErrors() throws Exception {
        String invalidPayload = """
                {
                    "title": ""
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/tasks"))
                .andExpect(jsonPath("$.validationErrors.title").exists());
    }

    @Test
    void unexpectedError_returnsHttp500() throws Exception {
        when(taskService.getTaskById(1L))
                .thenThrow(new RuntimeException("Database connectivity drop"));

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.path").value("/api/tasks/1"));
    }

    @Test
    void getAllTasks_returnsPaginatedResponse() throws Exception {
        com.tasknest.dto.TaskPageResponseDto pageResponse = new com.tasknest.dto.TaskPageResponseDto(
                java.util.List.of(), 0, 10, 0L, 0, true, true
        );
        when(taskService.getTasks(null, null, null, null, 0, 10, "createdAt", "desc"))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void getAllTasks_withInvalidDirection_returnsHttp400() throws Exception {
        when(taskService.getTasks(null, null, null, null, 0, 10, "createdAt", "side"))
                .thenThrow(new IllegalArgumentException("Invalid direction: side. Allowed values: asc, desc"));

        mockMvc.perform(get("/api/tasks?direction=side"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid direction: side. Allowed values: asc, desc"));
    }

    @Test
    void getAllTasks_withInvalidStatusEnum_returnsHttp400() throws Exception {
        mockMvc.perform(get("/api/tasks?status=INVALID_STATUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
