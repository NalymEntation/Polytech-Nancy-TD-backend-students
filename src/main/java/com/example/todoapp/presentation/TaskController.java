package com.example.todoapp.presentation;

import com.example.todoapp.dao.JsonUtils;
import com.example.todoapp.dto.TaskCreationDto;
import com.example.todoapp.dto.TaskResponseDto;
import com.example.todoapp.dto.TaskUpdateDto;
import com.example.todoapp.service.TaskService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

public class TaskController {

    private static final Pattern ID_PATH = Pattern.compile("^/tasks/([0-9]+)$");
    private final TaskService taskService;

    public TaskController() {
        this.taskService = new TaskService();
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

                if (!tasks.isEmpty()) {
                    sendResponse(exchange, 200, JsonUtils.serialize(tasks));
                } else {
                    sendResponse(exchange, 404, null); // Ou 200 avec tableau vide []
                }
                return;
            }

            // DELETE /tasks/{id}
            if ("DELETE".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1));
                boolean deleted = taskService.deleteTask(id);

                if (deleted) {
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
                boolean allDeleted = taskService.deleteAllTasks();
                if (allDeleted) {
                    sendResponse(exchange, 204, null);
                } else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }

            // GET /tasks/count
            if ("GET".equals(method) && "/tasks/count".equals(path)) {
                int count = taskService.countTasks();
                if (count != 0) {
                    sendResponse(exchange, 200, JsonUtils.serialize(count));
                } else {
                    sendResponse(exchange, 404, JsonUtils.serialize(0));
                }
                return;
            }

            // Si aucune route ne correspond
            sendResponse(exchange, 404, null);

        } catch (IllegalArgumentException e) {
            // Règle de validation métier non respectée (ex: titre > 50 chars)
            sendResponse(exchange, 400, "{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            // Erreur serveur globale ou problème de parsing JSON
            e.printStackTrace();
            sendResponse(exchange, 500, null);
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
            exchange.sendResponseHeaders(status, -1); // -1 indique qu'il n'y a pas de body en Java 11+
            exchange.close();
        }
    }
}