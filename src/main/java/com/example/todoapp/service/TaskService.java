package com.example.todoapp.service;

import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.donnee.Task;
import com.example.todoapp.dto.TaskCreationDto;
import com.example.todoapp.dto.TaskResponseDto;
import com.example.todoapp.dto.TaskUpdateDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Couche Métier (Service) chargée d'appliquer les règles de gestion.
 * Elle fait le pont entre le Controller (qui gère HTTP) et le DAO (qui gère SQL).
 * Elle transforme également les modèles de données (Task) en objets de transfert (DTO).
 */
public class TaskService {

    private final TaskDao dao;

    public TaskService() {
        this.dao = new TaskDao();
    }

    /**
     * Convertit une entité interne {@link Task} en un DTO destiné au client {@link TaskResponseDto}.
     * * @param task L'entité à convertir.
     * @return Le DTO formaté.
     */
    private TaskResponseDto mapToDto(Task task) {
        return new TaskResponseDto(task.id(), task.title(), task.description(), task.done());
    }

    /**
     * Crée une nouvelle tâche à partir des données reçues du client.
     * * @param dto Les données de création envoyées par le client.
     * @return La tâche créée formatée en DTO.
     */
    public TaskResponseDto createTask(TaskCreationDto dto) {
        // Règle métier : une tâche nouvellement créée est toujours 'non terminée' (false)
        Task newTask = new Task(null, dto.title(), dto.description(), false);
        Task savedTask = dao.insert(newTask);
        return mapToDto(savedTask);
    }

    /**
     * Récupère une tâche spécifique par son ID.
     * * @param id L'identifiant de la tâche.
     * @return Un Optional contenant le DTO de la tâche, ou vide si introuvable.
     */
    public Optional<TaskResponseDto> getTaskById(int id) {
        return dao.findById(id).map(this::mapToDto);
    }

    /**
     * Récupère toutes les tâches enregistrées.
     * * @return Une liste de DTOs représentant l'ensemble des tâches.
     */
    public List<TaskResponseDto> getAllTasks() {
        return dao.findall().stream()
                .map(this::mapToDto) // Conversion de chaque entité en DTO
                .collect(Collectors.toList());
    }

    /**
     * Met à jour les informations d'une tâche existante.
     * * @param id  L'identifiant de la tâche à modifier.
     * @param dto Les nouvelles informations envoyées par le client.
     * @return Un Optional contenant la tâche mise à jour, ou vide si l'ID n'existe pas.
     */
    public Optional<TaskResponseDto> updateTask(int id, TaskUpdateDto dto) {
        Task updatedTaskData = new Task(id, dto.title(), dto.description(), dto.done());
        return dao.modif(id, updatedTaskData).map(this::mapToDto);
    }

    /**
     * Supprime une tâche par son ID.
     * * @param id L'identifiant de la tâche à supprimer.
     * @return true si la tâche a bien été supprimée, false sinon (ex: ID inexistant).
     */
    public boolean deleteTask(int id) {
        return dao.remove(id) == 1; // 1 correspond au nombre de lignes affectées par SQL
    }

    /**
     * Supprime toutes les tâches de la base de données.
     * * @return true si la base est bien vide après l'opération.
     */
    public boolean deleteAllTasks() {
        return dao.remove_all().isEmpty();
    }

    /**
     * Renvoie le nombre total de tâches.
     * * @return Le nombre entier de tâches.
     */
    public int countTasks() {
        return dao.count();
    }
}