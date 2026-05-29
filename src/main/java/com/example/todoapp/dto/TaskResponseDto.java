package com.example.todoapp.dto;

public record TaskResponseDto(Integer id, String title, String description, boolean done) {
}