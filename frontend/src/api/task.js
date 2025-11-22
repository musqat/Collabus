import apiClient from './client';

export const taskAPI = {
  // Task 생성
  create: async (taskData) => {
    const { data } = await apiClient.post('/tasks', taskData);
    return data;
  },

  // 워크스페이스의 Task 목록
  getByWorkspace: async (workspaceId) => {
    const { data } = await apiClient.get(`/tasks/workspaces/${workspaceId}/tasks`);
    return data.data; // ResponseDto unwrapping
  },

  // Task 상세
  getById: async (taskId) => {
    const { data } = await apiClient.get(`/tasks/${taskId}`);
    return data.data; // ResponseDto unwrapping
  },

  // Task의 Todo 목록
  getTodos: async (taskId) => {
    const { data } = await apiClient.get(`/todo`, {
      params: { taskId }
    });
    return data.data; // ResponseDto unwrapping
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

  // 멤버 목록
  getMembers: async (taskId) => {
    const { data } = await apiClient.get(`/tasks/${taskId}/members`);
    return data.data; // ResponseDto unwrapping
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
