import apiClient from './client';

export const workspaceAPI = {
  // 워크스페이스 생성
  create: async (workspaceName, description) => {
    const { data } = await apiClient.post('/workspaces', {
      workspaceName,
      description
    });
    return data;
  },

  // 참여 중인 워크스페이스 목록. content 와 함께 page, totalPages 를 넘긴다
  getJoinedWorkspaces: async ({ page = 0, size = 20, sort } = {}) => {
    const { data } = await apiClient.get('/workspaces/joined',
      { params: { page, size, sort: sort || undefined } });
    return data;
  },

  // 워크스페이스 상세
  getById: async (id) => {
    const { data } = await apiClient.get(`/workspaces/${id}`);
    return data;
  },

  // 워크스페이스 수정
  update: async (id, workspaceName, description) => {
    const { data } = await apiClient.put(`/workspaces/${id}`, {
      workspaceName,
      description
    });
    return data;
  },

  // 워크스페이스 삭제
  delete: async (id) => {
    const { data } = await apiClient.delete(`/workspaces/${id}`);
    return data;
  },

  // 멤버 목록
  getMembers: async (workspaceId, page = 0, size = 20) => {
    const { data } = await apiClient.get(`/workspaces/${workspaceId}/users`, {
      params: { page, size }
    });
    return data.content;
  },

  // 초대
  invite: async (workspaceId, userId, role) => {
    const { data } = await apiClient.post(`/workspaces/${workspaceId}/invites`, {
      userId,
      role
    });
    return data;
  },

  // 멤버 제거
  removeMember: async (workspaceId, userId) => {
    const { data } = await apiClient.delete(`/workspaces/${workspaceId}/users/${userId}`);
    return data;
  },

  // 멤버 역할 변경
  updateMemberRole: async (workspaceId, userId, role) => {
    const { data } = await apiClient.put(`/workspaces/${workspaceId}/users/${userId}/role`, null, {
      params: { newRole: role }
    });
    return data;
  },

  // 받은 초대 목록. content 와 함께 page, totalPages 를 그대로 넘긴다
  getMyInvitations: async (page = 0, size = 20) => {
    const { data } = await apiClient.get('/workspaces/invites/me', {
      params: { page, size }
    });
    return data;
  },

  // 초대 수락
  acceptInvitation: async (inviteId) => {
    const { data } = await apiClient.post(`/workspaces/invites/${inviteId}/accept`);
    return data;
  },

  // 초대 거절
  rejectInvitation: async (inviteId) => {
    const { data } = await apiClient.post(`/workspaces/invites/${inviteId}/reject`);
    return data;
  }
};
