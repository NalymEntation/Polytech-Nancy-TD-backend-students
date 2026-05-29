package com.example.todoapp.dto;

/**
 * DTO standardisé pour renvoyer des messages d'erreur clairs au client (ex: Erreur 400).
 * * @param field   Le nom du champ qui a provoqué l'erreur (ex: "title").
 * @param message La description de l'erreur (ex: "Le titre est obligatoire").
 */
public record ErrorResponseDto(String field, String message) {
}