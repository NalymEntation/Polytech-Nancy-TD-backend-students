package com.example.todoapp.presentation;

import com.example.todoapp.dao.JsonUtils;
import com.example.todoapp.dto.ErrorResponseDto;
import com.example.todoapp.dto.TaskCreationDto;
import com.example.todoapp.dto.TaskResponseDto;
import com.example.todoapp.dto.TaskUpdateDto;
import com.example.todoapp.service.TaskService;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

/**
 * Couche Présentation (Controller).
 * Gère le routage HTTP, l'extraction du JSON, la validation des entrées (400 Bad Request),
 * et la gestion globale des erreurs (500 Internal Server Error).
 */
public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    // Expression régulière pour extraire l'ID numérique à la fin de l'URL (ex: /tasks/12)
    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");

    private final TaskService taskService;

    public TaskController() {
        this.taskService = new TaskService();
    }

    /**
     * Valide les contraintes de taille et de format des entrées de l'utilisateur.
     * S'assure que les règles du contrat d'interface (DTO) sont respectées.
     * * @param title       Le titre fourni.
     * @param description La description fournie.
     * @return Un {@link ErrorResponseDto} s'il y a une erreur, ou null si tout est valide.
     */
    private ErrorResponseDto validateTaskInput(String title, String description) {
        if (title == null || title.isBlank()) {
            return new ErrorResponseDto("title", "Le titre est obligatoire et ne peut pas être vide.");
        }
        if (title.length() > 50) {
            return new ErrorResponseDto("title", "La taille maximale du titre est de 50 caractères.");
        }
        if (description != null && description.length() > 255) {
            return new ErrorResponseDto("description", "La taille maximale de la description est de 255 caractères.");
        }
        return null;
    }

    /**
     * Méthode principale de gestion des requêtes HTTP.
     * Redirige le traitement selon le verbe HTTP (GET, POST, etc.) et le chemin de l'URL.
     * * @param exchange Le contexte de la requête et de la réponse HTTP.
     * @throws IOException En cas de problème de lecture/écriture des flux réseau.
     */
    public void handleTasks(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        Matcher m = ID_PATH.matcher(path);

        try {
            // === Endpoint : Créer une tâche ===
            if ("POST".equals(method) && "/tasks".equals(path)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
                TaskCreationDto input = JsonUtils.deserialize(body, TaskCreationDto.class);

                // Vérification de la validité des données
                ErrorResponseDto validationError = validateTaskInput(input.title(), input.description());
                if (validationError != null) {
                    sendResponse(exchange, 400, JsonUtils.serialize(validationError));
                    return;
                }

                TaskResponseDto createdTask = taskService.createTask(input);

                // Code 201 (Created) + En-tête Location pointant vers la nouvelle ressource
                exchange.getResponseHeaders().add("Location", "/tasks/" + createdTask.id());
                sendResponse(exchange, 201, JsonUtils.serialize(createdTask));
                return;
            }

            // === Endpoint : Lire une tâche par ID ===
            if ("GET".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1)); // Extraction du groupe capturé par la Regex
                Optional<TaskResponseDto> task = taskService.getTaskById(id);

                if (task.isPresent()) {
                    sendResponse(exchange, 200, JsonUtils.serialize(task.get()));
                } else {
                    sendResponse(exchange, 404, null); // Code 404 (Not Found)
                }
                return;
            }

            // === Endpoint : Lire toutes les tâches ===
            if ("GET".equals(method) && "/tasks".equals(path)) {
                List<TaskResponseDto> tasks = taskService.getAllTasks();
                sendResponse(exchange, 200, JsonUtils.serialize(tasks));
                return;
            }

            // === Endpoint : Supprimer une tâche ===
            if ("DELETE".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1));
                if (taskService.deleteTask(id)) {
                    sendResponse(exchange, 204, null); // Code 204 (No Content) pour une suppression réussie
                } else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }

            // === Endpoint : Mettre à jour une tâche ===
            if ("PUT".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1));
                String body = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
                TaskUpdateDto input = JsonUtils.deserialize(body, TaskUpdateDto.class);

                ErrorResponseDto validationError = validateTaskInput(input.title(), input.description());
                if (validationError != null) {
                    sendResponse(exchange, 400, JsonUtils.serialize(validationError));
                    return;
                }

                Optional<TaskResponseDto> updatedTask = taskService.updateTask(id, input);
                if (updatedTask.isPresent()) {
                    sendResponse(exchange, 204, null);
                } else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }

            // === Endpoint : Supprimer toutes les tâches ===
            if ("DELETE".equals(method) && "/tasks".equals(path)) {
                if (taskService.deleteAllTasks()) {
                    sendResponse(exchange, 204, null);
                } else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }

            // === Endpoint : Compter les tâches ===
            if ("GET".equals(method) && "/tasks/count".equals(path)) {
                int count = taskService.countTasks();
                sendResponse(exchange, 200, JsonUtils.serialize(count));
                return;
            }

            // Si aucune combinaison Verbe/URL ne correspond, on retourne 404
            sendResponse(exchange, 404, null);

        } catch (Exception e) {
            // === Gestion Globale des Erreurs ===
            // Capture toutes les exceptions inattendues (ex: JSON malformé, base de données corrompue)
            log.error("Erreur inattendue du serveur : ", e);
            sendResponse(exchange, 500, "{\"error\": \"Erreur interne du serveur\"}");
        }
    }

    /**
     * Formate et envoie la réponse HTTP au client.
     * * @param exchange Le contexte de communication.
     * @param status   Le code de statut HTTP (200, 201, 204, 400, 404, 500).
     * @param json     Le corps de la réponse formaté en JSON (ou null s'il n'y a pas de corps).
     * @throws IOException En cas de problème d'écriture réseau.
     */
    private void sendResponse(HttpExchange exchange, int status, String json) throws IOException {
        if (nonNull(json)) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            byte[] bytes = json.getBytes(UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            // Un contenu vide est signalé avec la taille -1 dans les versions récentes de Java
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        }
    }
}