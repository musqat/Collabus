package com.muscat.Collabus.Todo.repository;


import com.muscat.Collabus.Todo.entity.TodoFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoFileRepository extends JpaRepository<TodoFile, Long> {

  List<TodoFile> findAllByWorkId(Long workId);
}
