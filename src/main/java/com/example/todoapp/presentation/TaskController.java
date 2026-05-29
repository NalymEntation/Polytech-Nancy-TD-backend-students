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

public class TaskController {

    private static final Logger log = LoggerFactory.getLogger(TaskController.class);
    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");
    private final TaskService taskService;

    public TaskController() {
        this.taskService = new TaskService();
    }

    // Validation des règles métier (400)
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

    public void handleTasks(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        Matcher m = ID_PATH.matcher(path);

        try {
            // POST /tasks
            if ("POST".equals(method) && "/tasks".equals(path)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
                TaskCreationDto input = JsonUtils.deserialize(body, TaskCreationDto.class);

                ErrorResponseDto validationError = validateTaskInput(input.title(), input.description());
                if (validationError != null) {
                    sendResponse(exchange, 400, JsonUtils.serialize(validationError));
                    return;
                }

                TaskResponseDto createdTask = taskService.createTask(input);
                exchange.getResponseHeaders().add("Location", "/tasks/" + createdTask.id());
                sendResponse(exchange, 201, JsonUtils.serialize(createdTask));
                return;
            }

            // GET /tasks/{id}
            if ("GET".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1));
                Optional<TaskResponseDto> task = taskService.getTaskById(id);
                if (task.isPresent()) {
                    sendResponse(exchange, 200, JsonUtils.serialize(task.get()));
                } else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }

            // GET /tasks
            if ("GET".equals(method) && "/tasks".equals(path)) {
                List<TaskResponseDto> tasks = taskService.getAllTasks();
                sendResponse(exchange, 200, JsonUtils.serialize(tasks));
                return;
            }

            // DELETE /tasks/{id}
            if ("DELETE".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1));
                if (taskService.deleteTask(id)) {
                    sendResponse(exchange, 204, null);
                } else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }

            // PUT /tasks/{id}
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

            // DELETE /tasks
            if ("DELETE".equals(method) && "/tasks".equals(path)) {
                if (taskService.deleteAllTasks()) {
                    sendResponse(exchange, 204, null);
                } else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }

            // GET /tasks/count
            if ("GET".equals(method) && "/tasks/count".equals(path)) {
                int count = taskService.countTasks();
                sendResponse(exchange, 200, JsonUtils.serialize(count));
                return;
            }

            sendResponse(exchange, 404, null);

        } catch (Exception e) {
            // Étape 4 : Gestion globale des erreurs inattendues (Code 500)
            log.error("Erreur inattendue du serveur : ", e);
            sendResponse(exchange, 500, "{\"error\": \"Erreur interne du serveur\"}");
        }
    }

    private void sendResponse(HttpExchange exchange, int status, String json) throws IOException {
        if (nonNull(json)) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            byte[] bytes = json.getBytes(UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else {
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        }
    }
}