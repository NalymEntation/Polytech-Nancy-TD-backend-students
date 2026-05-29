package com.example.todoapp.donnee;

/**
 * Modèle métier représentant l'entité Tâche telle qu'elle est stockée en base de données.
 * * @param id          Identifiant unique généré par la base de données.
 * @param title       Titre de la tâche.
 * @param description Description optionnelle de la tâche.
 * @param done        Statut de la tâche (false par défaut).
 */
public record Task(Integer id, String title, String description, boolean done) {
}