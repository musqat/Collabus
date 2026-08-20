import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { showToast } from '../store/toastStore';
import { errorMessage } from '../api/errorMessage';
import { userAPI } from '../api/user';
import { useAuthStore } from '../store/authStore';

/**
 * 내 계정 변경. 닉네임과 계정 삭제는 세션을 끊으므로 안내 후 로그인으로 보낸다.
 */
export const useProfile = (user) => {
  const navigate = useNavigate();
  const logout = useAuthStore((state) => state.logout);

  const leaveAfterNotice = () => {
    setTimeout(() => {
      logout();
      navigate('/login');
    }, 1500);
  };

  const nicknameMutation = useMutation({
    mutationFn: (nickname) => userAPI.updateNickname(user.id, nickname),
    onSuccess: () => {
      showToast.success('닉네임이 변경되었습니다. 다시 로그인해주세요.');
      leaveAfterNotice();
    },
    onError: (error) => showToast.error(errorMessage(error, '닉네임 변경에 실패했습니다')),
  });

  const passwordMutation = useMutation({
    mutationFn: ({ currentPassword, newPassword }) =>
      userAPI.updatePassword(user.id, currentPassword, newPassword),
    onSuccess: () => showToast.success('비밀번호가 변경되었습니다'),
    onError: (error) => showToast.error(errorMessage(error, '비밀번호 변경에 실패했습니다')),
  });

  const deleteMutation = useMutation({
    mutationFn: () => userAPI.deleteAccount(user.email),
    onSuccess: () => {
      showToast.success('계정이 삭제되었습니다');
      leaveAfterNotice();
    },
    onError: (error) => showToast.error(errorMessage(error, '계정 삭제에 실패했습니다')),
  });

  return {
    changeNickname: nicknameMutation.mutate,
    isNicknameLoading: nicknameMutation.isPending,
    changePassword: passwordMutation.mutate,
    isPasswordLoading: passwordMutation.isPending,
    deleteAccount: deleteMutation.mutate,
    isDeleteLoading: deleteMutation.isPending,
  };
};
