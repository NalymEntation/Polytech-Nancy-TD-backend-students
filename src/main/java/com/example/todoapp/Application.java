package com.example.todoapp;

import com.example.todoapp.presentation.TaskController;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws Exception {
        log.info("Initialisation de l'application...");

        TaskController taskController = new TaskController();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/tasks", taskController::handleTasks);
        server.setExecutor(null);
        server.start();

        log.info("Serveur HTTP démarré sur http://localhost:8080");
    }
}