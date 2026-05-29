package com.example.todoapp.dto;

/**
 * DTO (Data Transfer Object) utilisé pour la création d'une nouvelle tâche (POST).
 * Il ne contient que les champs que l'utilisateur a le droit d'envoyer.
 * * @param title       Le titre de la tâche (max 50 caractères).
 * @param description La description détaillée de la tâche (max 255 caractères).
 */
public record TaskCreationDto(String title, String description) {
}