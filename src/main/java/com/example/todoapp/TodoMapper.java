package com.example.todoapp;

import java.util.List;
import java.time.LocalDate;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoMapper {
    List<Todo> search(@Param("keyword") String keyword, @Param("category") String category,
            @Param("order") String order, @Param("from") LocalDate from, @Param("to") LocalDate to);

    int insert(Todo todo);

    Todo findById(@Param("id") Long id);

    int update(Todo todo);

    int updateProvidedFields(@Param("todo") Todo todo,
            @Param("updateTitle") boolean updateTitle,
            @Param("updateDetail") boolean updateDetail,
            @Param("updateCategory") boolean updateCategory,
            @Param("updatePriority") boolean updatePriority,
            @Param("updateDueDate") boolean updateDueDate);

    int updateCompleted(@Param("id") Long id, @Param("completed") Boolean completed);

    int deleteById(@Param("id") Long id);
}
