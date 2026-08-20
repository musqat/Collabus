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
    return data;
  },

  // Task의 Todo 목록 (페이지 응답)
  getByTask: async (taskId, status = null, page = 0, size = 20, sort = null) => {
    const { data } = await apiClient.get('/todo', {
      params: { taskId, status, page, size, sort: sort || undefined }
    });
    return data;
  },

  // Todo 상세
  getById: async (todoId) => {
    const { data } = await apiClient.get(`/todo/${todoId}`);
    return data;
  },

  // Todo 수정
  update: async (todoId, title, description, dueDate) => {
    const { data } = await apiClient.patch(`/todo/${todoId}`, {
      title,
      description,
      dueDate
    });
    return data;
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
    return data;
  },

  // 담당자 변경
  changeAssignee: async (todoId, userId) => {
    const { data } = await apiClient.patch(`/todo/${todoId}/assignee/${userId}`);
    return data;
  },

  // 댓글 관련
  // Todo 의 작업 내용 목록 (페이지 응답)
  getWorks: async (todoId, page = 0, size = 20) => {
    const { data } = await apiClient.get('/todo/works', {
      params: { todoId, page, size }
    });
    return data;
  },

  // 작업 내용에 붙은 파일 목록 (페이지 응답)
  getFilesByWork: async (workId, page = 0, size = 20) => {
    const { data } = await apiClient.get(`/todo/files/work/${workId}`, {
      params: { page, size }
    });
    return data;
  },

  // 작업 내용 등록
  createWork: async (todoId, title, content) => {
    const { data } = await apiClient.post('/todo/works', { title, content }, {
      params: { todoId }
    });
    return data;
  },

  // 작업 내용 수정
  updateWork: async (workId, title, content) => {
    const { data } = await apiClient.patch(`/todo/works/${workId}`, { title, content });
    return data;
  },

  // 작업 내용 삭제
  deleteWork: async (workId) => {
    const { data } = await apiClient.delete(`/todo/works/${workId}`);
    return data;
  },

  // 작업 내용에 파일 첨부
  uploadFile: async (workId, file) => {
    const formData = new FormData();
    formData.append('file', file);
    const { data } = await apiClient.post('/todo/files', formData, {
      params: { workId },
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    return data;
  },

  // 파일 내려받기. 인증이 필요하므로 blob 으로 받는다
  downloadFile: async (fileId) => {
    const response = await apiClient.get(`/todo/files/${fileId}/download`, {
      responseType: 'blob'
    });
    return response.data;
  },

  // 파일 삭제
  deleteFile: async (fileId) => {
    const { data } = await apiClient.delete(`/todo/files/${fileId}`);
    return data;
  },

  // Todo 의 댓글 목록 (페이지 응답)
  getComments: async (todoId, page = 0, size = 20) => {
    const { data } = await apiClient.get('/todo/comments', {
      params: { todoId, page, size }
    });
    return data;
  },

  createComment: async (todoId, content) => {
    const { data } = await apiClient.post('/todo/comments', { content }, {
      params: { todoId }
    });
    return data;
  },

  updateComment: async (commentId, content) => {
    const { data } = await apiClient.patch(`/todo/comments/${commentId}`, { content });
    return data;
  },

  deleteComment: async (commentId) => {
    const { data } = await apiClient.delete(`/todo/comments/${commentId}`);
    return data;
  }
};
