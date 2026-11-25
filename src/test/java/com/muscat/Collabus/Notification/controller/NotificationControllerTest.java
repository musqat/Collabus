package com.muscat.Collabus.Notification.controller;

import static com.muscat.Collabus.support.LoginUser.login;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.muscat.Collabus.Notification.service.NotificationService;
import com.muscat.Collabus.support.ControllerSliceTest;
import com.muscat.Collabus.support.LoginUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ControllerSliceTest(NotificationController.class)
@DisplayName("알림 컨트롤러")
class NotificationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private NotificationService notificationService;

  @Test
  @DisplayName("최근 알림 개수를 안 주면 10개, 주면 그 값으로 넘긴다")
  void recentLimit() throws Exception {
    mockMvc.perform(get("/api/notifications/recent").with(login()))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/notifications/recent").with(login()).param("limit", "3"))
        .andExpect(status().isOk());

    verify(notificationService).getRecentNotifications(LoginUser.ID, 10);
    verify(notificationService).getRecentNotifications(LoginUser.ID, 3);
  }

  @Test
  @DisplayName("모두 읽음은 로그인 사용자 것만 처리한다")
  void readAll() throws Exception {
    mockMvc.perform(patch("/api/notifications/read-all").with(login()))
        .andExpect(status().isOk());

    verify(notificationService).markAllAsRead(LoginUser.ID);
  }

  @Test
  @DisplayName("로그인하지 않으면 401 이다")
  void unauthenticated() throws Exception {
    mockMvc.perform(get("/api/notifications/unread/count"))
        .andExpect(status().isUnauthorized());

    verifyNoInteractions(notificationService);
  }
}
