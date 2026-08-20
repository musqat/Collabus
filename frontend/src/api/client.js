import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

// Axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request 인터셉터 - JWT 토큰 자동 추가
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 재발급은 한 번만 수행하고, 그 사이 401을 받은 요청들은 큐에서 대기시킨다.
// (Refresh Token Rotation 때문에 동시에 여러 번 재발급하면 서로를 무효화시킨다)
let isRefreshing = false;
let pendingRequests = [];

const resolveQueue = (token) => {
  pendingRequests.forEach(({ resolve }) => resolve(token));
  pendingRequests = [];
};

const rejectQueue = (error) => {
  pendingRequests.forEach(({ reject }) => reject(error));
  pendingRequests = [];
};

const clearSession = () => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
};

// 재발급 자체가 401이면 재시도할 수 없다
const isAuthEndpoint = (url = '') =>
  url.includes('/token/refresh') || url.includes('/users/login');

// Response 인터셉터 - 토큰 만료 처리
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (
      error.response?.status !== 401 ||
      !originalRequest ||
      originalRequest._retry ||
      isAuthEndpoint(originalRequest.url)
    ) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    // 이미 다른 요청이 재발급 중이면 완료될 때까지 기다렸다가 새 토큰으로 재시도
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        pendingRequests.push({ resolve, reject });
      }).then((token) => {
        originalRequest.headers.Authorization = `Bearer ${token}`;
        return apiClient(originalRequest);
      });
    }

    isRefreshing = true;

    try {
      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        throw error;
      }

      const { data } = await axios.post(`${API_BASE_URL}/token/refresh`, {
        refreshToken
      });

      const newAccessToken = data.data.accessToken;
      localStorage.setItem('accessToken', newAccessToken);
      localStorage.setItem('refreshToken', data.data.refreshToken);

      resolveQueue(newAccessToken);

      // 원래 요청 재시도
      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
      return apiClient(originalRequest);
    } catch (refreshError) {
      // Refresh Token 만료 — 대기 중인 요청까지 모두 실패시키고 로그아웃
      rejectQueue(refreshError);
      clearSession();
      window.location.href = '/login';
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  }
);

// ResponseDto 를 unwrap 한다.
// axios 의 응답 본문과 ResponseDto 의 payload 가 둘 다 data 라 호출부가 data.data 를 써야 했다.
// 재발급 후 재시도된 응답이 이 체인을 다시 타므로, ResponseDto 일 때만 unwrap 해 두 번 하지 않는다.
apiClient.interceptors.response.use((response) => {
  const body = response.data;
  if (body && typeof body === 'object' && 'statusCode' in body) {
    response.data = body.data;
  }
  return response;
});

export default apiClient;
