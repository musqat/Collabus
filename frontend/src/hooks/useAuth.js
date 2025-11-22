import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { authAPI } from '../api/auth';
import { useAuthStore } from '../store/authStore';

export const useAuth = () => {
  const navigate = useNavigate();
  const { setAuth, clearAuth } = useAuthStore();

  const loginMutation = useMutation({
    mutationFn: ({ email, password }) => authAPI.login(email, password),
    onSuccess: (data) => {
      // 백엔드가 data.data 구조로 반환
      const userData = data.data || data;
      setAuth(userData, userData.accessToken, userData.refreshToken);
      navigate('/dashboard');
    },
    onError: (error) => {
      alert(error.response?.data?.statusMsg || '로그인 실패');
    }
  });

  const registerMutation = useMutation({
    mutationFn: ({ email, nickname, password }) =>
      authAPI.register(email, nickname, password),
    onSuccess: () => {
      alert('회원가입 성공! 로그인해주세요.');
      navigate('/login');
    },
    onError: (error) => {
      alert(error.response?.data?.statusMsg || '회원가입 실패');
    }
  });

  const logout = async () => {
    try {
      await authAPI.logout();
    } catch (error) {
      console.error('로그아웃 에러:', error);
    } finally {
      clearAuth();
      navigate('/login');
    }
  };

  return {
    login: loginMutation.mutate,
    register: registerMutation.mutate,
    logout,
    isLoading: loginMutation.isPending || registerMutation.isPending
  };
};
