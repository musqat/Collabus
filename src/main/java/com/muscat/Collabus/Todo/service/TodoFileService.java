package com.muscat.Collabus.Todo.service;

import com.muscat.Collabus.Todo.model.TodoFileDto;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface TodoFileService {

  TodoFileDto createFile(Long workId, Long userId, MultipartFile file);

  List<TodoFileDto> getFilesByWorkId(Long workId);

  TodoFileDto updateFile(Long fileId, Long userId, MultipartFile newFile);


  void deleteFile(Long fileId, Long userId);

}
