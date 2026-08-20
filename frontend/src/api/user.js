import client from './client';

// 경로는 client 의 baseURL(VITE_API_BASE_URL, 기본 '/api') 기준 상대 경로다.
export const userAPI = {
  // 닉네임 변경
  updateNickname: async (userId, nickname) => {
    const response = await client.patch(`/users/nickname/${userId}`, {
      nickname
    });
    return response.data;
  },

  // 비밀번호 변경
  updatePassword: async (userId, currentPassword, password) => {
    const response = await client.patch(`/users/password/${userId}`, {
      currentPassword,
      password
    });
    return response.data;
  },

  // 계정 삭제
  deleteAccount: async (email) => {
    const response = await client.delete(`/users/delete?email=${encodeURIComponent(email)}`);
    return response.data;
  },

  // 닉네임으로 유저 검색
  searchByNickname: async (keyword) => {
    const response = await client.get(`/users/search?keyword=${encodeURIComponent(keyword)}`);
    return response.data;
  },
};
