import { useState } from 'react';
import { useAuthStore } from '../store/authStore';
import { useProfile } from '../hooks/useProfile';
import { showToast } from '../store/toastStore';

export default function ProfilePage() {
  const user = useAuthStore((state) => state.user);

  // 닉네임 변경
  const [newNickname, setNewNickname] = useState('');

  // 비밀번호 변경
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  // 계정 삭제
  const [deleteConfirm, setDeleteConfirm] = useState('');
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const {
    changeNickname, isNicknameLoading,
    changePassword, isPasswordLoading,
    deleteAccount, isDeleteLoading,
  } = useProfile(user);

  const handleNicknameChange = (e) => {
    e.preventDefault();
    if (!newNickname.trim()) {
      showToast.error('닉네임을 입력해주세요');
      return;
    }
    changeNickname(newNickname);
  };

  const handlePasswordChange = (e) => {
    e.preventDefault();

    if (!currentPassword || !newPassword || !confirmPassword) {
      showToast.error('모든 필드를 입력해주세요');
      return;
    }

    if (newPassword !== confirmPassword) {
      showToast.error('비밀번호가 일치하지 않습니다');
      return;
    }

    // 서버 정책과 동일: 8자 이상, 영문 + 숫자 포함
    if (!/^(?=.*[A-Za-z])(?=.*\d).{8,}$/.test(newPassword)) {
      showToast.error('비밀번호는 8자 이상이며 영문과 숫자를 포함해야 합니다');
      return;
    }

    changePassword({ currentPassword, newPassword }, {
      onSuccess: () => {
        setCurrentPassword('');
        setNewPassword('');
        setConfirmPassword('');
      },
    });
  };

  const handleDeleteAccount = () => {
    if (deleteConfirm !== user.email) {
      showToast.error('이메일이 일치하지 않습니다');
      return;
    }
    deleteAccount(undefined, { onSettled: () => setShowDeleteModal(false) });
  };

  if (!user) {
    return (
      <div className="p-8">
        <div className="text-center text-gray-500">로그인이 필요합니다</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-3xl mx-auto px-4">
        {/* 헤더 */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">프로필 설정</h1>
          <p className="text-gray-600 mt-2">계정 정보를 관리하세요</p>
        </div>

        {/* 현재 정보 */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">현재 정보</h2>
          <div className="space-y-3">
            <div>
              <span className="text-sm text-gray-500">이메일</span>
              <p className="text-gray-900 font-medium">{user.email}</p>
            </div>
            <div>
              <span className="text-sm text-gray-500">닉네임</span>
              <p className="text-gray-900 font-medium">{user.nickname}</p>
            </div>
            <div>
              <span className="text-sm text-gray-500">DisplayName</span>
              <p className="text-gray-900 font-medium">{user.displayName}</p>
            </div>
          </div>
        </div>

        {/* 닉네임 변경 */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">닉네임 변경</h2>
          <form onSubmit={handleNicknameChange}>
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                새 닉네임
              </label>
              <input
                type="text"
                value={newNickname}
                onChange={(e) => setNewNickname(e.target.value)}
                placeholder="새로운 닉네임을 입력하세요"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <p className="text-sm text-gray-500 mt-2">
                닉네임을 변경하면 자동으로 로그아웃됩니다.
              </p>
            </div>
            <button
              type="submit"
              disabled={isNicknameLoading}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition disabled:bg-gray-400"
            >
              {isNicknameLoading ? '변경 중...' : '닉네임 변경'}
            </button>
          </form>
        </div>

        {/* 비밀번호 변경 */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <h2 className="text-xl font-semibold text-gray-900 mb-4">비밀번호 변경</h2>
          <form onSubmit={handlePasswordChange}>
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                현재 비밀번호
              </label>
              <input
                type="password"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                placeholder="현재 비밀번호"
                autoComplete="current-password"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                새 비밀번호
              </label>
              <input
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="새 비밀번호"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                비밀번호 확인
              </label>
              <input
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="비밀번호 확인"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <button
              type="submit"
              disabled={isPasswordLoading}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition disabled:bg-gray-400"
            >
              {isPasswordLoading ? '변경 중...' : '비밀번호 변경'}
            </button>
          </form>
        </div>

        {/* 계정 삭제 */}
        <div className="bg-white rounded-lg shadow-sm p-6 border-2 border-red-200">
          <h2 className="text-xl font-semibold text-red-600 mb-4">위험 구역</h2>
          <p className="text-gray-600 mb-4">
            계정을 삭제하면 모든 데이터가 영구적으로 삭제됩니다. 이 작업은 되돌릴 수 없습니다.
          </p>
          <button
            onClick={() => setShowDeleteModal(true)}
            className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
          >
            계정 삭제
          </button>
        </div>

        {/* 계정 삭제 확인 모달 */}
        {showDeleteModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4">
              <h3 className="text-xl font-bold text-gray-900 mb-4">계정 삭제 확인</h3>
              <p className="text-gray-600 mb-4">
                정말로 계정을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.
              </p>
              <p className="text-gray-600 mb-4">
                계속하려면 이메일 주소 <strong>{user.email}</strong>을 입력하세요:
              </p>
              <input
                type="text"
                value={deleteConfirm}
                onChange={(e) => setDeleteConfirm(e.target.value)}
                placeholder="이메일 입력"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500 mb-4"
              />
              <div className="flex gap-2">
                <button
                  onClick={() => {
                    setShowDeleteModal(false);
                    setDeleteConfirm('');
                  }}
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 transition"
                >
                  취소
                </button>
                <button
                  onClick={handleDeleteAccount}
                  disabled={isDeleteLoading || deleteConfirm !== user.email}
                  className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition disabled:bg-gray-400"
                >
                  {isDeleteLoading ? '삭제 중...' : '삭제'}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
