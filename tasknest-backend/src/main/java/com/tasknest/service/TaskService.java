package com.tasknest.service;

import com.tasknest.dto.TaskPageResponseDto;
import com.tasknest.dto.TaskRequestDto;
import com.tasknest.dto.TaskResponseDto;
import com.tasknest.entity.TaskPriority;
import com.tasknest.entity.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskService {

    TaskResponseDto createTask(TaskRequestDto request);

    List<TaskResponseDto> getAllTasks();

    TaskPageResponseDto getTasks(TaskStatus status, TaskPriority priority,
                                 LocalDateTime dueDateFrom, LocalDateTime dueDateTo,
                                 int page, int size, String sortBy, String direction);

    TaskResponseDto getTaskById(Long id);

    TaskResponseDto updateTask(Long id, TaskRequestDto request);

    void deleteTask(Long id);
}
