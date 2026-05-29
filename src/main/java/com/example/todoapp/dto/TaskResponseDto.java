package com.example.todoapp.dto;

/**
 * DTO renvoyé au client lors des requêtes GET ou en réponse à une création.
 * Il masque la structure interne de la base de données pour n'exposer que ce qui est nécessaire.
 * * @param id          L'identifiant unique de la tâche.
 * @param title       Le titre de la tâche.
 * @param description La description de la tâche.
 * @param done        Le statut d'accomplissement (true si terminée).
 */
public record TaskResponseDto(Integer id, String title, String description, boolean done) {
}