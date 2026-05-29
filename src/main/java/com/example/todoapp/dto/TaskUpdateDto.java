package com.example.todoapp.dto;

/**
 * DTO utilisé pour la mise à jour complète d'une tâche existante (PUT).
 * * @param title       Le nouveau titre.
 * @param description La nouvelle description.
 * @param done        Le nouveau statut d'accomplissement.
 */
public record TaskUpdateDto(String title, String description, boolean done) {
}