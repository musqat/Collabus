package com.muscat.Collabus.Todo.service;

import com.muscat.Collabus.Todo.model.TodoFileDto;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface TodoFileService {

  TodoFileDto createFile(Long workId, Long userId, MultipartFile file);

  List<TodoFileDto> getFilesByWorkId(Long workId, Long userId);

  TodoFileDto updateFile(Long fileId, Long userId, MultipartFile newFile);

  void deleteFile(Long fileId, Long userId);

  // 다운로드용 파일 본문과 원본 파일명
  DownloadedFile downloadFile(Long fileId, Long userId);

  record DownloadedFile(org.springframework.core.io.Resource resource, String originalName) {

  }
}
