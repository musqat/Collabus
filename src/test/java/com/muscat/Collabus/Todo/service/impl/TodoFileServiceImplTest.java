package com.muscat.Collabus.Todo.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import com.muscat.Collabus.common.util.SortGuard;
import com.muscat.Collabus.common.dto.PageResponseDto;
import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.Todo.entity.TodoFile;
import com.muscat.Collabus.Todo.entity.TodoWork;
import com.muscat.Collabus.Todo.mapper.TodoFileMapper;
import com.muscat.Collabus.Todo.model.TodoFileDto;
import com.muscat.Collabus.Todo.repository.TodoFileRepository;
import com.muscat.Collabus.Todo.repository.TodoWorkRepository;
import com.muscat.Collabus.Todo.service.TodoFileService;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.common.exception.ResourceNotFoundException;
import com.muscat.Collabus.common.util.FileUtil;
import com.muscat.Collabus.common.util.ParticipantUtil;
import com.muscat.Collabus.enums.role.SystemRole;
import com.muscat.Collabus.enums.status.TodoStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 API 는 원래 권한 검증이 전혀 없어 로그인만 하면 임의의 workId 로 접근할 수 있었다.
 * 회귀를 막기 위해 인가 경로를 중심으로 검증한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TodoFileService 단위 테스트")
class TodoFileServiceImplTest {

  private static final Long TASK_ID = 1L;
  private static final Long WORK_ID = 10L;
  private static final Long FILE_ID = 100L;
  private static final Long OWNER_ID = 1L;
  private static final Long OUTSIDER_ID = 99L;

  @Mock
  private SortGuard sortGuard;


  @Mock
  private TodoWorkRepository todoWorkRepository;

  @Mock
  private TodoFileRepository todoFileRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private TodoFileMapper todoFileMapper;

  @Mock
  private FileUtil fileUtil;

  @Mock
  private ParticipantUtil participantUtil;

  @Mock
  private MultipartFile multipartFile;

  @InjectMocks
  private TodoFileServiceImpl todoFileService;

  private User owner;
  private TodoWork work;
  private TodoFile file;

  @BeforeEach
  void setUp() {
    owner = User.builder()
        .id(OWNER_ID)
        .email("owner@example.com")
        .nickname("owner")
        .password("encoded")
        .tag("0001")
        .displayName("owner#0001")
        .role(SystemRole.USER)
        .build();

    Workspace workspace = Workspace.builder()
        .id(1L)
        .workspaceName("ws")
        .description("desc")
        .founder(owner)
        .build();

    Task task = Task.builder()
        .id(TASK_ID)
        .workspace(workspace)
        .taskManager(owner)
        .title("task")
        .dueDate(LocalDate.now().plusDays(30))
        .build();

    Todo todo = Todo.builder()
        .id(1L)
        .task(task)
        .assignee(owner)
        .title("todo")
        .dueDate(LocalDate.now().plusDays(7))
        .status(TodoStatus.IN_PROGRESS)
        .build();

    work = TodoWork.builder()
        .id(WORK_ID)
        .todo(todo)
        .author(owner)
        .title("work")
        .content("content")
        .build();

    file = TodoFile.builder()
        .id(FILE_ID)
        .work(work)
        .uploader(owner)
        .fileUrl("/app/uploads/uuid_report.pdf")
        .originalName("report.pdf")
        .build();
  }

  private void denyParticipation(Long userId) {
    doThrow(new AccessDeniedException("사용자가 태스크 참여자가 아닙니다."))
        .when(participantUtil).validateTaskParticipant(TASK_ID, userId);
  }

  @Test
  @DisplayName("업로드 실패 - Task 참여자가 아니면 접근 불가")
  void createFile_Fail_NotParticipant() {
    when(todoWorkRepository.findById(WORK_ID)).thenReturn(Optional.of(work));
    denyParticipation(OUTSIDER_ID);

    assertThatThrownBy(() -> todoFileService.createFile(WORK_ID, OUTSIDER_ID, multipartFile))
        .isInstanceOf(AccessDeniedException.class);

    // 권한 검증이 파일 저장보다 먼저 일어나야 한다
    verify(fileUtil, never()).saveFile(any());
    verify(todoFileRepository, never()).save(any());
  }

  @Test
  @DisplayName("업로드 성공 - Task 참여자")
  void createFile_Success() {
    when(todoWorkRepository.findById(WORK_ID)).thenReturn(Optional.of(work));
    when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
    when(fileUtil.saveFile(multipartFile)).thenReturn("/app/uploads/uuid_report.pdf");
    when(multipartFile.getOriginalFilename()).thenReturn("report.pdf");
    when(todoFileRepository.save(any(TodoFile.class))).thenReturn(file);
    when(todoFileMapper.mapToDto(file)).thenReturn(TodoFileDto.builder()
        .id(FILE_ID)
        .downloadUrl("/api/todo/files/100/download")
        .originalFileName("report.pdf")
        .build());

    TodoFileDto result = todoFileService.createFile(WORK_ID, OWNER_ID, multipartFile);

    assertThat(result.getOriginalFileName()).isEqualTo("report.pdf");
    // 서버 저장 경로가 아니라 다운로드 API 경로만 노출해야 한다
    assertThat(result.getDownloadUrl()).isEqualTo("/api/todo/files/100/download");
    verify(participantUtil, times(1)).validateTaskParticipant(TASK_ID, OWNER_ID);
  }

  @Test
  @DisplayName("목록 조회 실패 - Task 참여자가 아니면 접근 불가")
  void getFilesByWorkId_Fail_NotParticipant() {
    when(todoWorkRepository.findById(WORK_ID)).thenReturn(Optional.of(work));
    denyParticipation(OUTSIDER_ID);

    assertThatThrownBy(() ->
        todoFileService.getFilesByWorkId(WORK_ID, OUTSIDER_ID, PageRequest.of(0, 20)))
        .isInstanceOf(AccessDeniedException.class);

    verify(todoFileRepository, never()).findAllByWorkId(any(), any());
  }

  @Test
  @DisplayName("다운로드 실패 - Task 참여자가 아니면 접근 불가")
  void downloadFile_Fail_NotParticipant() {
    when(todoFileRepository.findById(FILE_ID)).thenReturn(Optional.of(file));
    denyParticipation(OUTSIDER_ID);

    assertThatThrownBy(() -> todoFileService.downloadFile(FILE_ID, OUTSIDER_ID))
        .isInstanceOf(AccessDeniedException.class);

    verify(fileUtil, never()).loadFile(any());
  }

  @Test
  @DisplayName("다운로드 성공 - 원본 파일명을 함께 반환")
  void downloadFile_Success() {
    when(todoFileRepository.findById(FILE_ID)).thenReturn(Optional.of(file));
    when(fileUtil.loadFile(file.getFileUrl())).thenReturn(new ByteArrayResource(new byte[]{1}));

    TodoFileService.DownloadedFile downloaded = todoFileService.downloadFile(FILE_ID, OWNER_ID);

    assertThat(downloaded.originalName()).isEqualTo("report.pdf");
    verify(participantUtil, times(1)).validateTaskParticipant(TASK_ID, OWNER_ID);
  }

  @Test
  @DisplayName("삭제 실패 - 업로더 본인이 아니면 불가")
  void deleteFile_Fail_NotUploader() {
    when(todoFileRepository.findById(FILE_ID)).thenReturn(Optional.of(file));

    assertThatThrownBy(() -> todoFileService.deleteFile(FILE_ID, OUTSIDER_ID))
        .isInstanceOf(AccessDeniedException.class);

    verify(fileUtil, never()).deleteFile(any());
    verify(todoFileRepository, never()).delete(any());
  }

  @Test
  @DisplayName("교체 시 새 파일 저장이 기존 파일 삭제보다 먼저 수행된다")
  void updateFile_SavesBeforeDeleting() {
    when(todoFileRepository.findById(FILE_ID)).thenReturn(Optional.of(file));
    when(fileUtil.saveFile(multipartFile)).thenReturn("/app/uploads/uuid_new.pdf");
    when(multipartFile.getOriginalFilename()).thenReturn("new.pdf");
    when(todoFileMapper.mapToDto(file)).thenReturn(TodoFileDto.builder().build());

    todoFileService.updateFile(FILE_ID, OWNER_ID, multipartFile);

    var inOrder = org.mockito.Mockito.inOrder(fileUtil);
    inOrder.verify(fileUtil).saveFile(multipartFile);
    inOrder.verify(fileUtil).deleteFile("/app/uploads/uuid_report.pdf");
    assertThat(file.getFileUrl()).isEqualTo("/app/uploads/uuid_new.pdf");
    assertThat(file.getOriginalName()).isEqualTo("new.pdf");
  }

  @Test
  @DisplayName("존재하지 않는 Work 는 404")
  void createFile_Fail_WorkNotFound() {
    when(todoWorkRepository.findById(WORK_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> todoFileService.createFile(WORK_ID, OWNER_ID, multipartFile))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("목록 조회 성공 - 참여자")
  void getFilesByWorkId_Success() {
    when(todoWorkRepository.findById(WORK_ID)).thenReturn(Optional.of(work));
    Pageable pageable = PageRequest.of(0, 20);
    when(sortGuard.apply(pageable, TodoFile.class)).thenReturn(pageable);
    when(todoFileRepository.findAllByWorkId(WORK_ID, pageable))
        .thenReturn(new PageImpl<>(List.of(file), pageable, 1));
    when(todoFileMapper.mapToDto(file)).thenReturn(TodoFileDto.builder().id(FILE_ID).build());

    PageResponseDto<TodoFileDto> result =
        todoFileService.getFilesByWorkId(WORK_ID, OWNER_ID, pageable);

    assertThat(result.getContent()).hasSize(1);
    verify(participantUtil, times(1)).validateTaskParticipant(TASK_ID, OWNER_ID);
  }
}
