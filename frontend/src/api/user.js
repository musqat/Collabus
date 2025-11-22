import client from './client';

export const userAPI = {
  // 닉네임 변경
  updateNickname: async (userId, nickname) => {
    const response = await client.patch(`/api/users/nickname/${userId}`, {
      nickname
    });
    return response.data;
  },

  // 비밀번호 변경
  updatePassword: async (userId, password) => {
    const response = await client.patch(`/api/users/password/${userId}`, {
      password
    });
    return response.data;
  },

  // 계정 삭제
  deleteAccount: async (email) => {
    const response = await client.delete(`/api/users/delete?email=${encodeURIComponent(email)}`);
    return response.data;
  },

  // 닉네임으로 유저 검색
  searchByNickname: async (keyword) => {
    const response = await client.get(`/api/users/search?keyword=${encodeURIComponent(keyword)}`);
    return response.data.data;
  },
};
