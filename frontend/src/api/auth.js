import apiClient from './client';

export const authAPI = {
  // 로그인
  login: async (email, password) => {
    const { data } = await apiClient.post('/users/login', { email, password });
    return data;
  },

  // 회원가입
  register: async (email, nickname, password) => {
    const { data } = await apiClient.post('/users/register', {
      email,
      nickname,
      password
    });
    return data;
  },

  // 로그아웃
  logout: async () => {
    const { data } = await apiClient.post('/users/logout');
    localStorage.clear();
    return data;
  },

  // 닉네임으로 사용자 검색. 두 글자 이상이어야 한다
  searchUsers: async (keyword, { page = 0, size = 20 } = {}) => {
    const { data } = await apiClient.get('/users/search', {
      params: { keyword, page, size }
    });
    return data.content;
  }
};
