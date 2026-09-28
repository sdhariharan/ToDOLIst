package com.tasknest.service.impl;

import com.tasknest.dto.TaskPageResponseDto;
import com.tasknest.dto.TaskRequestDto;
import com.tasknest.dto.TaskResponseDto;
import com.tasknest.entity.Task;
import com.tasknest.entity.TaskPriority;
import com.tasknest.entity.TaskStatus;
import com.tasknest.exception.ResourceNotFoundException;
import com.tasknest.mapper.TaskMapper;
import com.tasknest.repository.TaskRepository;
import com.tasknest.service.TaskService;
import com.tasknest.specification.TaskSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "title", "priority", "status", "dueDate", "createdAt", "updatedAt"
    );

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public TaskServiceImpl(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    @Transactional
    public TaskResponseDto createTask(TaskRequestDto request) {
        Task task = taskMapper.toEntity(request);
        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponseDto(savedTask);
    }

    @Override
    public List<TaskResponseDto> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(taskMapper::toResponseDto)
                .toList();
    }

    @Override
    public TaskPageResponseDto getTasks(TaskStatus status, TaskPriority priority,
                                        LocalDateTime dueDateFrom, LocalDateTime dueDateTo,
                                        int page, int size, String sortBy, String direction) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be negative");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        if (sortBy == null || !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sortBy field: " + sortBy + ". Allowed fields: " + ALLOWED_SORT_FIELDS);
        }
        if (direction == null || (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc"))) {
            throw new IllegalArgumentException("Invalid sort direction: " + direction + ". Allowed values: asc, desc");
        }

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Specification<Task> spec = TaskSpecification.filterBy(status, priority, dueDateFrom, dueDateTo);
        Page<Task> taskPage = taskRepository.findAll(spec, pageable);

        List<TaskResponseDto> content = taskPage.getContent()
                .stream()
                .map(taskMapper::toResponseDto)
                .toList();

        return new TaskPageResponseDto(
                content,
                taskPage.getNumber(),
                taskPage.getSize(),
                taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.isFirst(),
                taskPage.isLast()
        );
    }

    @Override
    public TaskResponseDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return taskMapper.toResponseDto(task);
    }

    @Override
    @Transactional
    public TaskResponseDto updateTask(Long id, TaskRequestDto request) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        existingTask.setTitle(request.getTitle());
        existingTask.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            existingTask.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            existingTask.setPriority(request.getPriority());
        }
        existingTask.setDueDate(request.getDueDate());

        Task updatedTask = taskRepository.save(existingTask);
        return taskMapper.toResponseDto(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }
}
