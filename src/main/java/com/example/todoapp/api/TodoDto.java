package com.example.todoapp.api;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.todoapp.Todo;

public record TodoDto(
        Long id,
        String title,
        String detail,
        String category,
        Integer priority,
        LocalDate dueDate,
        Boolean completed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static TodoDto from(Todo todo) {
        return new TodoDto(
                todo.getId(),
                todo.getTitle(),
                todo.getDetail(),
                todo.getCategory(),
                todo.getPriority(),
                todo.getDueDate(),
                todo.getCompleted(),
                todo.getCreatedAt(),
                todo.getUpdatedAt());
    }
}
