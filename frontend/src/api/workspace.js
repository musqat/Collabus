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

  // 내 워크스페이스 목록
  getMyWorkspaces: async () => {
    const { data } = await apiClient.get('/workspaces/my');
    return data;
  },

  // 참여 중인 워크스페이스 목록
  getJoinedWorkspaces: async () => {
    const { data } = await apiClient.get('/workspaces/joined');
    return data; // WorkspaceController는 ResponseDto 없이 직접 반환
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
    return data.data.content;
  },

  // 초대
  invite: async (workspaceId, userId, role) => {
    const { data } = await apiClient.post(`/workspaces/${workspaceId}/invites`, {
      userId,
      role
    });
    return data;
  },

  // 받은 초대 목록
  getMyInvites: async () => {
    const { data } = await apiClient.get('/workspaces/invites/me');
    return data;
  },

  // 초대 수락
  acceptInvite: async (inviteId) => {
    const { data } = await apiClient.post(`/workspaces/invites/${inviteId}/accept`);
    return data;
  },

  // 초대 거절
  rejectInvite: async (inviteId) => {
    const { data } = await apiClient.post(`/workspaces/invites/${inviteId}/reject`);
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

  // 내가 받은 초대 목록 (alias for getMyInvites)
  getMyInvitations: async () => {
    const { data } = await apiClient.get('/workspaces/invites/me');
    return data.data; // ResponseDto unwrapping
  },

  // 초대 수락 (alias for acceptInvite)
  acceptInvitation: async (inviteId) => {
    const { data } = await apiClient.post(`/workspaces/invites/${inviteId}/accept`);
    return data;
  },

  // 초대 거절 (alias for rejectInvite)
  rejectInvitation: async (inviteId) => {
    const { data } = await apiClient.post(`/workspaces/invites/${inviteId}/reject`);
    return data;
  }
};
