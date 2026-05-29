package com.example.todoapp.service;

import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.donnee.Task;
import com.example.todoapp.dto.TaskCreationDto;
import com.example.todoapp.dto.TaskResponseDto;
import com.example.todoapp.dto.TaskUpdateDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TaskService {

    private final TaskDao dao;

    public TaskService() {
        this.dao = new TaskDao();
    }

    private void validateTaskStrings(String title, String description) {
        if (title == null || title.isBlank() || title.length() > 50) {
            throw new IllegalArgumentException("Le titre est invalide ou dépasse 50 caractères.");
        }
        if (description != null && description.length() > 255) {
            throw new IllegalArgumentException("La description dépasse 255 caractères.");
        }
    }

    private TaskResponseDto mapToDto(Task task) {
        return new TaskResponseDto(task.id(), task.title(), task.description(), task.done());
    }

    public TaskResponseDto createTask(TaskCreationDto dto) {
        validateTaskStrings(dto.title(), dto.description());

        Task newTask = new Task(null, dto.title(), dto.description(), false);
        Task savedTask = dao.insert(newTask);

        return mapToDto(savedTask);
    }

    public Optional<TaskResponseDto> getTaskById(int id) {
        return dao.findById(id).map(this::mapToDto);
    }

    public List<TaskResponseDto> getAllTasks() {
        return dao.findall().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public Optional<TaskResponseDto> updateTask(int id, TaskUpdateDto dto) {
        validateTaskStrings(dto.title(), dto.description());

        Task updatedTaskData = new Task(id, dto.title(), dto.description(), dto.done());
        return dao.modif(id, updatedTaskData).map(this::mapToDto);
    }

    public boolean deleteTask(int id) {
        return dao.remove(id) == 1;
    }

    public boolean deleteAllTasks() {
        return dao.remove_all().isEmpty();
    }

    public int countTasks() {
        return dao.count();
    }
}