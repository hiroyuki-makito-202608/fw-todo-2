package com.example.todoapp;

import java.util.List;
import java.time.LocalDate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TodoService {
    private final TodoMapper todoMapper;

    public TodoService(TodoMapper todoMapper) {
        this.todoMapper = todoMapper;
    }

    public List<Todo> search(String keyword, String category, String order, LocalDate from, LocalDate to) {
        return todoMapper.search(keyword, category, order, from, to);
    }

    public Todo findById(Long id) {
        return todoMapper.findById(id);
    }

    public void create(Todo todo) {
        todoMapper.insert(todo);
        log.info("Todo created id={}", todo.getId());
    }

    public void update(Todo todo) {
        todoMapper.update(todo);
        log.info("Todo updated id={}", todo.getId());
    }

    public void updateProvidedFields(Todo todo, boolean updateTitle, boolean updateDetail,
            boolean updateCategory, boolean updatePriority, boolean updateDueDate) {
        if (!(updateTitle || updateDetail || updateCategory || updatePriority || updateDueDate)) {
            return;
        }
        todoMapper.updateProvidedFields(todo, updateTitle, updateDetail, updateCategory,
                updatePriority, updateDueDate);
        log.info("Todo selected fields updated id={}", todo.getId());
    }

    public void setCompleted(Long id, Boolean completed) {
        todoMapper.updateCompleted(id, completed);
        log.info("Todo completion updated id={} completed={}", id, completed);
    }

    public void delete(Long id) {
        todoMapper.deleteById(id);
        log.info("Todo deleted id={}", id);
    }
}
