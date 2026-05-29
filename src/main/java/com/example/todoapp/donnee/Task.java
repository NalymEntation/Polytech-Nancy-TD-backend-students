package com.example.todoapp.donnee;

/**
 * Task model for Database representation.
 */
public record Task(Integer id, String title, String description, boolean done) {
}