package com.example.todoapp;

import com.example.todoapp.dao.JsonUtils;
import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.donnee.Task;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

import com.example.todoapp.presentation.TaskController;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Main class of the application. Managing routing and HTTP layer setup.
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        // Initialisation de la couche Controller (qui initialise elle-même le reste)
        TaskController taskController = new TaskController();

        // Création du serveur HTTP
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Attribution des requêtes /tasks à notre Controller
        server.createContext("/tasks", taskController::handleTasks);

        server.setExecutor(null);
        server.start();

        log.info("HTTP server started on http://localhost:8080");
    }
}