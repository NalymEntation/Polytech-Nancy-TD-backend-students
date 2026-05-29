package com.example.todoapp;

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
        log.info("Initialisation de l'application...");

        // Initialisation de la couche Controller (qui initialise le Service et le DAO)
        TaskController taskController = new TaskController();

        // Création du serveur HTTP
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Attribution des requêtes /tasks à notre Controller
        server.createContext("/tasks", taskController::handleTasks);

        server.setExecutor(null);
        server.start();

        log.info("Serveur HTTP démarré sur http://localhost:8080");
    }
}