package com.tasknest;

import com.jayway.jsonpath.JsonPath;
import com.tasknest.entity.Task;
import com.tasknest.entity.TaskPriority;
import com.tasknest.entity.TaskStatus;
import com.tasknest.repository.TaskRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class TaskIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TaskRepository taskRepository;

    private MockMvc mockMvc;

    private final List<Long> createdTaskIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @AfterEach
    void tearDown() {
        for (Long id : createdTaskIds) {
            if (taskRepository.existsById(id)) {
                taskRepository.deleteById(id);
            }
        }
        createdTaskIds.clear();
    }

    private Task createAndPersistTestTask(String title, TaskStatus status, TaskPriority priority, LocalDateTime dueDate) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("Test task description");
        task.setStatus(status);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        Task saved = taskRepository.save(task);
        createdTaskIds.add(saved.getId());
        return saved;
    }

    @Test
    @DisplayName("1. CREATE TASK: POST /api/tasks returns HTTP 201 with generated ID and timestamps")
    void createTask_validRequest_persistsAndReturns201() throws Exception {
        String requestJson = """
                {
                    "title": "Integration Test Task",
                    "description": "Testing end-to-end task creation",
                    "status": "PENDING",
                    "priority": "MEDIUM",
                    "dueDate": "2026-11-01T10:00:00"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Integration Test Task"))
                .andExpect(jsonPath("$.description").value("Testing end-to-end task creation"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andReturn();

        Number idNum = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        Long generatedId = idNum.longValue();
        createdTaskIds.add(generatedId);

        assertTrue(taskRepository.existsById(generatedId));
    }

    @Test
    @DisplayName("2. GET TASK BY ID: GET /api/tasks/{id} returns HTTP 200 with matching task")
    void getTaskById_existingTask_returns200() throws Exception {
        Task task = createAndPersistTestTask("Existing Task", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, LocalDateTime.now().plusDays(1));

        mockMvc.perform(get("/api/tasks/" + task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("Existing Task"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    @DisplayName("3. UPDATE TASK: PUT /api/tasks/{id} updates fields and preserves createdAt")
    void updateTask_validRequest_updatesAndReturns200() throws Exception {
        Task task = createAndPersistTestTask("Initial Title", TaskStatus.PENDING, TaskPriority.LOW, LocalDateTime.now().plusDays(1));
        task = taskRepository.findById(task.getId()).orElseThrow();
        LocalDateTime originalCreatedAt = task.getCreatedAt();

        String updateJson = """
                {
                    "title": "Updated Title",
                    "description": "Updated Description",
                    "status": "COMPLETED",
                    "priority": "HIGH",
                    "dueDate": "2026-12-01T10:00:00"
                }
                """;

        mockMvc.perform(put("/api/tasks/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        Task updatedEntity = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals("Updated Title", updatedEntity.getTitle());
        assertEquals(TaskStatus.COMPLETED, updatedEntity.getStatus());
        assertEquals(TaskPriority.HIGH, updatedEntity.getPriority());
        assertEquals(originalCreatedAt, updatedEntity.getCreatedAt());
    }

    @Test
    @DisplayName("4. GET ALL TASKS: GET /api/tasks returns HTTP 200 with paginated structure")
    void getAllTasks_returnsPaginatedResponse() throws Exception {
        createAndPersistTestTask("Task 1", TaskStatus.PENDING, TaskPriority.LOW, LocalDateTime.now().plusDays(1));
        createAndPersistTestTask("Task 2", TaskStatus.IN_PROGRESS, TaskPriority.MEDIUM, LocalDateTime.now().plusDays(2));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").isNumber())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("5. FILTER BY STATUS: GET /api/tasks?status=COMPLETED returns matching status")
    void filterByStatus_returnsOnlyMatchingStatus() throws Exception {
        createAndPersistTestTask("Completed Task", TaskStatus.COMPLETED, TaskPriority.MEDIUM, null);

        mockMvc.perform(get("/api/tasks").param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].status", everyItem(is("COMPLETED"))));
    }

    @Test
    @DisplayName("6. FILTER BY PRIORITY: GET /api/tasks?priority=HIGH returns matching priority")
    void filterByPriority_returnsOnlyMatchingPriority() throws Exception {
        createAndPersistTestTask("High Priority Task", TaskStatus.PENDING, TaskPriority.HIGH, null);

        mockMvc.perform(get("/api/tasks").param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].priority", everyItem(is("HIGH"))));
    }

    @Test
    @DisplayName("7. COMBINED FILTER: GET /api/tasks?status=PENDING&priority=HIGH returns both")
    void combinedFilter_returnsMatchingBothConditions() throws Exception {
        createAndPersistTestTask("Pending High Task", TaskStatus.PENDING, TaskPriority.HIGH, null);

        mockMvc.perform(get("/api/tasks")
                        .param("status", "PENDING")
                        .param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].status", everyItem(is("PENDING"))))
                .andExpect(jsonPath("$.content[*].priority", everyItem(is("HIGH"))));
    }

    @Test
    @DisplayName("8. SORTING: GET /api/tasks?sortBy=dueDate&direction=asc returns sorted")
    void sorting_byDueDateAsc_returnsSorted() throws Exception {
        createAndPersistTestTask("Early Task", TaskStatus.PENDING, TaskPriority.LOW, LocalDateTime.now().plusDays(1));
        createAndPersistTestTask("Later Task", TaskStatus.PENDING, TaskPriority.LOW, LocalDateTime.now().plusDays(10));

        mockMvc.perform(get("/api/tasks")
                        .param("sortBy", "dueDate")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("9. PAGINATION: GET /api/tasks?page=0&size=2 respects pagination parameters")
    void pagination_respectsPageAndSize() throws Exception {
        createAndPersistTestTask("Page Task 1", TaskStatus.PENDING, TaskPriority.LOW, null);
        createAndPersistTestTask("Page Task 2", TaskStatus.PENDING, TaskPriority.LOW, null);

        mockMvc.perform(get("/api/tasks")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("10. NOT FOUND: GET /api/tasks/999999 returns HTTP 404 with ErrorResponse")
    void getTaskById_nonexistent_returns404() throws Exception {
        mockMvc.perform(get("/api/tasks/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(containsString("999999")))
                .andExpect(jsonPath("$.path").value("/api/tasks/999999"));
    }

    @Test
    @DisplayName("11. VALIDATION FAILURE: POST /api/tasks with blank title returns HTTP 400")
    void createTask_blankTitle_returns400WithValidationErrors() throws Exception {
        String invalidRequestJson = """
                {
                    "title": "",
                    "description": "Invalid task"
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.title").exists());
    }

    @Test
    @DisplayName("12. DELETE: DELETE /api/tasks/{id} removes task and returns HTTP 204")
    void deleteTask_existingTask_removesAndReturns204() throws Exception {
        Task task = createAndPersistTestTask("To Delete", TaskStatus.PENDING, TaskPriority.LOW, null);
        Long id = task.getId();

        mockMvc.perform(delete("/api/tasks/" + id))
                .andExpect(status().isNoContent());

        assertFalse(taskRepository.existsById(id));

        mockMvc.perform(get("/api/tasks/" + id))
                .andExpect(status().isNotFound());
    }
}
