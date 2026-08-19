package com.muscat.Collabus.Todo.service.impl;

import com.muscat.Collabus.common.exception.BusinessException;
import org.springframework.data.domain.Pageable;
import com.muscat.Collabus.common.util.SortGuard;
import com.muscat.Collabus.common.dto.PageResponseDto;
import com.muscat.Collabus.Todo.entity.TodoFile;
import com.muscat.Collabus.Todo.entity.TodoWork;
import com.muscat.Collabus.Todo.mapper.TodoFileMapper;
import com.muscat.Collabus.Todo.model.TodoFileDto;
import com.muscat.Collabus.Todo.repository.TodoFileRepository;
import com.muscat.Collabus.Todo.repository.TodoWorkRepository;
import com.muscat.Collabus.Todo.service.TodoFileService;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.common.util.FileUtil;
import com.muscat.Collabus.common.util.ParticipantUtil;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.enums.response.TodoResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoFileServiceImpl implements TodoFileService {

    private final SortGuard sortGuard;

    private final TodoWorkRepository todoWorkRepository;
    private final TodoFileRepository todoFileRepository;
    private final UserRepository userRepository;
    private final TodoFileMapper todoFileMapper;
    private final FileUtil fileUtil;
    private final ParticipantUtil participantUtil;

    @Override
    @Transactional
    public TodoFileDto createFile(Long workId, Long userId, MultipartFile file) {
        TodoWork work = findWork(workId);
        validateParticipant(work, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(CommonResponse.USER_NOT_FOUND));

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
    @Transactional(readOnly = true)
    public PageResponseDto<TodoFileDto> getFilesByWorkId(Long workId, Long userId,
                                                         Pageable pageable) {
        validateParticipant(findWork(workId), userId);

        return PageResponseDto.of(
                todoFileRepository.findAllByWorkId(workId,
                        sortGuard.apply(pageable, TodoFile.class)),
                todoFileMapper::mapToDto);
    }

    // 이전 파일은 커밋 후에 지운다
    @Override
    @Transactional
    public TodoFileDto updateFile(Long fileId, Long userId, MultipartFile newFile) {
        TodoFile file = findFile(fileId);
        validateUploader(file, userId);

        // 새 파일 저장이 실패하면 기존 파일이 남도록 저장을 먼저 수행한다
        String newFileUrl = fileUtil.saveFile(newFile);
        fileUtil.deleteFile(file.getFileUrl());

        file.replaceFile(newFileUrl, newFile.getOriginalFilename());

        return todoFileMapper.mapToDto(file);
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId, Long userId) {
        TodoFile file = findFile(fileId);
        validateUploader(file, userId);

        fileUtil.deleteFile(file.getFileUrl());
        todoFileRepository.delete(file);
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadedFile downloadFile(Long fileId, Long userId) {
        TodoFile file = findFile(fileId);
        validateParticipant(file.getWork(), userId);

        return new DownloadedFile(fileUtil.loadFile(file.getFileUrl()), file.getOriginalName());
    }

    private TodoWork findWork(Long workId) {
        return todoWorkRepository.findById(workId)
                .orElseThrow(() -> new BusinessException(TodoResponse.TODO_WORK_NOT_FOUND));
    }

    private TodoFile findFile(Long fileId) {
        return todoFileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(TodoResponse.FILE_NOT_FOUND));
    }

    // 파일은 Todo → Task 에 속하므로 해당 Task 참여자만 접근할 수 있다
    private void validateParticipant(TodoWork work, Long userId) {
        participantUtil.validateTaskParticipant(work.getTodo().getTask().getId(), userId);
    }

    // 파일 수정·삭제는 업로더 본인만 가능하다
    private void validateUploader(TodoFile file, Long userId) {
        if (!file.getUploader().getId().equals(userId)) {
            throw new BusinessException(TodoResponse.UNAUTHORIZED_TODO_WORK);
        }
    }
}
