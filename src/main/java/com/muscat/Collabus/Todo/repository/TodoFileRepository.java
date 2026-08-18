package com.muscat.Collabus.Todo.repository;

import com.muscat.Collabus.Todo.entity.TodoFile;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoFileRepository extends JpaRepository<TodoFile, Long> {

    @EntityGraph(attributePaths = {"uploader"})
    List<TodoFile> findAllByWorkId(Long workId);


}
