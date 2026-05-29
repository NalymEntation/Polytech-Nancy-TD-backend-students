package com.example.todoapp.service;

import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.donnee.Task;

import java.util.List;
import java.util.Optional;

public class TaskService {

    private final TaskDao dao;

    public TaskService() {
        // Initialisation de l'accès aux données
        this.dao = new TaskDao();
    }

    public Task createTask(Task task) {
        return dao.save(task);
    }

    public Optional<Task> getTaskById(int id) {
        return dao.findById(id);
    }

    public List<Task> getAllTasks() {
        return dao.findall();
    }

    public boolean deleteTask(int id) {
        // Retourne true si la suppression a fonctionné (1 ligne affectée)
        return dao.remove(id) == 1;
    }

    public Optional<Task> updateTask(int id, Task task) {
        return dao.modif(id, task);
    }

    public boolean deleteAllTasks() {
        List<Task> remaining = dao.remove_all();
        return remaining.isEmpty();
    }

    public int countTasks() {
        return dao.count();
    }
}