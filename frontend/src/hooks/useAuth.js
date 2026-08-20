import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { authAPI } from '../api/auth';
import { useAuthStore } from '../store/authStore';
import { showToast } from '../store/toastStore';

export const useAuth = () => {
  const navigate = useNavigate();
  const { setAuth, clearAuth } = useAuthStore();

  const loginMutation = useMutation({
    mutationFn: ({ email, password }) => authAPI.login(email, password),
    onSuccess: (user) => {
      setAuth(user, user.accessToken, user.refreshToken);
      navigate('/dashboard');
    },
    onError: (error) => {
      showToast.error(error.response?.data?.statusMsg || '로그인에 실패했습니다.');
    },
  });

  const registerMutation = useMutation({
    mutationFn: ({ email, nickname, password }) =>
      authAPI.register(email, nickname, password),
    onSuccess: () => {
      showToast.success('회원가입 성공! 로그인해주세요.');
      navigate('/login');
    },
    onError: (error) => {
      showToast.error(error.response?.data?.statusMsg || '회원가입에 실패했습니다.');
    },
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
    isLoading: loginMutation.isPending || registerMutation.isPending,
  };
};
