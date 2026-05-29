package com.example.todoapp;

import com.example.todoapp.presentation.TaskController;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Classe principale de l'application.
 * Elle est responsable du démarrage du serveur HTTP et de la configuration du routage.
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    /**
     * Point d'entrée du programme.
     * * @param args Arguments de la ligne de commande (non utilisés ici).
     * @throws Exception Si le serveur ne parvient pas à démarrer ou à lier le port.
     */
    public static void main(String[] args) throws Exception {
        log.info("Initialisation de l'application...");

        // Initialisation de la couche Présentation (qui instancie en cascade Service et DAO)
        TaskController taskController = new TaskController();

        // Création du serveur HTTP écoutant sur le port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Routage : toutes les requêtes commençant par /tasks sont envoyées au Controller
        server.createContext("/tasks", taskController::handleTasks);

        // Utilisation de l'exécuteur par défaut du système
        server.setExecutor(null);
        server.start();

        log.info("Serveur HTTP démarré sur http://localhost:8080");
    }
}