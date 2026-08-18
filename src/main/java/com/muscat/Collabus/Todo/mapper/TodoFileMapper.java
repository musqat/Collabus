package com.muscat.Collabus.Todo.mapper;

import com.muscat.Collabus.Todo.entity.TodoFile;
import com.muscat.Collabus.Todo.model.TodoFileDto;
import com.muscat.Collabus.User.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TodoFileMapper {

  private static final String DOWNLOAD_PATH = "/api/todo/files/%d/download";

  public TodoFileDto mapToDto(TodoFile todoFile) {
    User uploader = todoFile.getUploader();

    return TodoFileDto.builder()
        .id(todoFile.getId())
        // 엔티티의 fileUrl 은 서버 절대 경로이므로 클라이언트에는 다운로드 API 경로만 노출
        .downloadUrl(String.format(DOWNLOAD_PATH, todoFile.getId()))
        .originalFileName(todoFile.getOriginalName())
        .uploaderId(uploader.getId())
        .uploaderDisplayName(uploader.getDisplayName())
        .uploadedAt(todoFile.getCreatedAt())
        .build();
  }
}
