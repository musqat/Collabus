package com.muscat.Collabus.Todo.controller;

import static com.muscat.Collabus.support.LoginUser.login;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.muscat.Collabus.Todo.service.TodoFileService;
import com.muscat.Collabus.Todo.service.TodoFileService.DownloadedFile;
import com.muscat.Collabus.support.ControllerSliceTest;
import com.muscat.Collabus.support.LoginUser;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ControllerSliceTest(TodoFileController.class)
@DisplayName("첨부파일 컨트롤러")
class TodoFileControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TodoFileService todoFileService;

  @Test
  @DisplayName("다운로드는 화면에 열지 않고 내려받게 하며 한글 파일명을 그대로 담는다")
  void downloadAsAttachment() throws Exception {
    when(todoFileService.downloadFile(1L, LoginUser.ID))
        .thenReturn(new DownloadedFile(new ByteArrayResource(new byte[]{1, 2}), "보고서.svg"));

    mockMvc.perform(get("/api/todo/files/1/download").with(login()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
        .andExpect(header().string("Content-Disposition", Matchers.startsWith("attachment")))
        .andExpect(header().string("Content-Disposition",
            Matchers.containsString("filename*=UTF-8''%EB%B3%B4%EA%B3%A0%EC%84%9C.svg")));
  }

  @Test
  @DisplayName("파일 없이 업로드하면 400 이고 서비스까지 가지 않는다")
  void uploadWithoutFile() throws Exception {
    mockMvc.perform(multipart("/api/todo/files").with(login()).param("workId", "1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("필수 요청 파트가 없습니다: file"));

    verifyNoInteractions(todoFileService);
  }
}
