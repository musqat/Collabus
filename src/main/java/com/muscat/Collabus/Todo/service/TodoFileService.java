package com.muscat.Collabus.Todo.service;

import com.muscat.Collabus.Todo.model.TodoFileDto;
import java.util.List;
import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface TodoFileService {

  // 첨부 파일을 올린다
  TodoFileDto createFile(Long workId, Long userId, MultipartFile file);

  // 작업 내용의 첨부 파일 목록
  PageResponseDto<TodoFileDto> getFilesByWorkId(Long workId, Long userId, Pageable pageable);

  // 첨부 파일을 교체한다
  TodoFileDto updateFile(Long fileId, Long userId, MultipartFile newFile);

  // 첨부 파일을 삭제한다
  void deleteFile(Long fileId, Long userId);

  // 첨부 파일을 내려받는다
  DownloadedFile downloadFile(Long fileId, Long userId);

  record DownloadedFile(org.springframework.core.io.Resource resource, String originalName) {

  }
}
