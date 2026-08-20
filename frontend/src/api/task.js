import apiClient from './client';

export const taskAPI = {
  // Task 생성
  create: async (taskData) => {
    const { data } = await apiClient.post('/tasks', taskData);
    return data;
  },

  // 워크스페이스의 Task 목록. page, size, keyword 를 넘긴다
  getByWorkspace: async (workspaceId, { page = 0, size = 20, keyword, sort } = {}) => {
    const { data } = await apiClient.get(`/tasks/workspaces/${workspaceId}/tasks`, {
      params: { page, size, keyword: keyword || undefined, sort: sort || undefined }
    });
    return data;
  },

  // 워크스페이스 진행률. total, inProgress, waitingReview, confirmed 를 받는다
  getWorkspaceProgress: async (workspaceId) => {
    const { data } = await apiClient.get(`/tasks/workspaces/${workspaceId}/progress`);
    return data;
  },

  // Task 상세
  getById: async (taskId) => {
    const { data } = await apiClient.get(`/tasks/${taskId}`);
    return data; // ResponseDto unwrapping
  },

  // Task의 Todo 목록
  // Task 의 Todo 목록. 사이드바 트리에서 쓰므로 배열만 돌려준다
  getTodos: async (taskId, { size = 100 } = {}) => {
    const { data } = await apiClient.get('/todo', {
      params: { taskId, size }
    });
    return data.content;
  },

  // Task 수정
  update: async (taskId, title, description, dueDate) => {
    const { data } = await apiClient.patch(`/tasks/${taskId}`, {
      title,
      description,
      dueDate
    });
    return data;
  },

  // Task 삭제
  delete: async (taskId) => {
    const { data } = await apiClient.delete(`/tasks/${taskId}`);
    return data;
  },

  // Task 참여자 목록. content 와 함께 page, totalPages 를 그대로 넘긴다
  getMembers: async (taskId, { page = 0, size = 20 } = {}) => {
    const { data } = await apiClient.get(`/tasks/${taskId}/members`, {
      params: { page, size }
    });
    return data;
  },

  // Task 진행률. total, inProgress, waitingReview, confirmed 를 받는다
  getTaskProgress: async (taskId) => {
    const { data } = await apiClient.get(`/tasks/${taskId}/progress`);
    return data;
  },

  // 멤버 추가
  addMember: async (taskId, targetUserId) => {
    const { data } = await apiClient.post(`/tasks/${taskId}/members`, null, {
      params: { targetUserId }
    });
    return data;
  },

  // 멤버 제거
  removeMember: async (taskId, userId) => {
    const { data } = await apiClient.delete(`/tasks/${taskId}/members/${userId}`);
    return data;
  },

  // Task Manager 변경
  changeManager: async (taskId, newManagerId) => {
    const { data } = await apiClient.patch(`/tasks/${taskId}/manager`, null, {
      params: { newManagerId }
    });
    return data;
  }
};
