package com.example.todoapp;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TodoMapper {
    List<Todo> findAll();

    int insert(Todo todo);

    Todo findById(@Param("id") Long id);

    int update(Todo todo);

    int deleteById(@Param("id") Long id);
}
