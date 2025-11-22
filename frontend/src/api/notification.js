import client from './client';

export const notificationAPI = {
  // 모든 알림 조회
  getAll: async () => {
    const response = await client.get('/notifications');
    return response.data.data;
  },

  // 읽지 않은 알림 조회
  getUnread: async () => {
    const response = await client.get('/notifications/unread');
    return response.data.data;
  },

  // 읽지 않은 알림 개수 조회
  getUnreadCount: async () => {
    const response = await client.get('/notifications/unread/count');
    return response.data.data;
  },

  // 최근 알림 N개 조회
  getRecent: async (limit = 10) => {
    const response = await client.get(`/notifications/recent?limit=${limit}`);
    return response.data.data;
  },

  // 알림 읽음 처리
  markAsRead: async (notificationId) => {
    const response = await client.patch(`/notifications/${notificationId}/read`);
    return response.data;
  },

  // 모든 알림 읽음 처리
  markAllAsRead: async () => {
    const response = await client.patch('/notifications/read-all');
    return response.data;
  },

  // 알림 삭제
  delete: async (notificationId) => {
    const response = await client.delete(`/notifications/${notificationId}`);
    return response.data;
  },
};
