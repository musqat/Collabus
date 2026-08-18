import apiClient from './client';

export const todoAPI = {
  // Todo 생성
  create: async (taskId, assigneeId, title, description, dueDate) => {
    const { data } = await apiClient.post('/todo', {
      taskId,
      assigneeId,
      title,
      description,
      dueDate
    });
    return data.data;
  },

  // Task의 Todo 목록 (페이지 응답)
  getByTask: async (taskId, status = null, page = 0, size = 20) => {
    const { data } = await apiClient.get('/todo', {
      params: { taskId, status, page, size }
    });
    return data.data;
  },

  // Todo 상세
  getById: async (todoId) => {
    const { data } = await apiClient.get(`/todo/${todoId}`);
    return data.data;
  },

  // Todo 수정
  update: async (todoId, title, description, dueDate) => {
    const { data } = await apiClient.patch(`/todo/${todoId}`, {
      title,
      description,
      dueDate
    });
    return data.data;
  },

  // Todo 삭제
  delete: async (todoId) => {
    const { data } = await apiClient.delete(`/todo/${todoId}`);
    return data;
  },

  // 완료 처리
  complete: async (todoId) => {
    const { data } = await apiClient.patch(`/todo/${todoId}/complete`);
    return data;
  },

  // 최종 확인
  confirm: async (todoId) => {
    const { data } = await apiClient.patch(`/todo/${todoId}/confirm`);
    return data.data;
  },

  // 담당자 변경
  changeAssignee: async (todoId, userId) => {
    const { data } = await apiClient.patch(`/todo/${todoId}/assignee/${userId}`);
    return data;
  },

  // 댓글 관련
  getComments: async (todoId, page = 0, size = 20) => {
    const { data } = await apiClient.get('/todo/comments', {
      params: { todoId, page, size }
    });
    return data.data.content;
  },

  createComment: async (todoId, content) => {
    const { data } = await apiClient.post('/todo/comments', { content }, {
      params: { todoId }
    });
    return data.data;
  },

  updateComment: async (commentId, content) => {
    const { data } = await apiClient.patch(`/todo/comments/${commentId}`, { content });
    return data.data;
  },

  deleteComment: async (commentId) => {
    const { data } = await apiClient.delete(`/todo/comments/${commentId}`);
    return data;
  }
};
