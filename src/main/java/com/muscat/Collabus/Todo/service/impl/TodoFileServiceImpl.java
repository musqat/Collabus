package com.muscat.Collabus.Todo.service.impl;

import com.muscat.Collabus.Todo.entity.TodoFile;
import com.muscat.Collabus.Todo.entity.TodoWork;
import com.muscat.Collabus.Todo.mapper.TodoFileMapper;
import com.muscat.Collabus.Todo.model.TodoFileDto;
import com.muscat.Collabus.Todo.repository.TodoFileRepository;
import com.muscat.Collabus.Todo.repository.TodoWorkRepository;
import com.muscat.Collabus.Todo.service.TodoFileService;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.common.exception.ResourceNotFoundException;
import com.muscat.Collabus.common.util.FileUtil;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.enums.response.TodoResponse;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class TodoFileServiceImpl implements TodoFileService {

  private final TodoWorkRepository todoWorkRepository;
  private final TodoFileRepository todoFileRepository;
  private final UserRepository userRepository;
  private final TodoFileMapper todoFileMapper;
  private final FileUtil fileUtil;

  @Override
  @Transactional
  public TodoFileDto createFile(Long workId, Long userId, MultipartFile file) {
    TodoWork work = todoWorkRepository.findById(workId)
        .orElseThrow(() -> new ResourceNotFoundException(TodoResponse.TODO_WORK_NOT_FOUND));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(CommonResponse.USER_NOT_FOUND));

    String fileUrl = fileUtil.saveFile(file);

    TodoFile saved = todoFileRepository.save(TodoFile.builder()
        .work(work)
        .uploader(user)
        .fileUrl(fileUrl)
        .originalName(file.getOriginalFilename())
        .build());

    return todoFileMapper.mapToDto(saved);
  }

  @Override
  @Transactional
  public List<TodoFileDto> getFilesByWorkId(Long workId) {
    return todoFileRepository.findAllByWorkId(workId).stream()
        .map(todoFileMapper::mapToDto)
        .toList();
  }

  @Override
  @Transactional
  public TodoFileDto updateFile(Long fileId, Long userId, MultipartFile newFile) {
    TodoFile file = todoFileRepository.findById(fileId)
        .orElseThrow(() -> new ResourceNotFoundException(TodoResponse.FILE_NOT_FOUND));

    if (!file.getUploader().getId().equals(userId)) {
      throw new AccessDeniedException(CommonResponse.UNAUTHORIZED.getMessage());
    }

    fileUtil.deleteFile(file.getFileUrl());
    String newFileUrl = fileUtil.saveFile(newFile);

    file.setFileUrl(newFileUrl);
    file.setOriginalName(newFile.getOriginalFilename());

    return todoFileMapper.mapToDto(todoFileRepository.save(file));
  }

  @Override
  @Transactional
  public void deleteFile(Long fileId, Long userId) {
    TodoFile file = todoFileRepository.findById(fileId)
        .orElseThrow(() -> new ResourceNotFoundException(TodoResponse.FILE_NOT_FOUND));

    if (!file.getUploader().getId().equals(userId)) {
      throw new AccessDeniedException(CommonResponse.UNAUTHORIZED.getMessage());
    }

    fileUtil.deleteFile(file.getFileUrl());
    todoFileRepository.delete(file);
  }
}
