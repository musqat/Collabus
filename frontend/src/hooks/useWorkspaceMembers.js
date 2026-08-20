import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { showToast } from '../store/toastStore';
import { errorMessage } from '../api/errorMessage';
import { authAPI } from '../api/auth';
import { workspaceAPI } from '../api/workspace';

/**
 * 워크스페이스 멤버 목록과 초대·제거·역할 변경
 * 변경 후 목록 갱신
 */
export const useWorkspaceMembers = (workspaceId) => {
  const queryClient = useQueryClient();
  const queryKey = ['workspace-members', workspaceId];

  const { data: members = [] } = useQuery({
    queryKey,
    queryFn: () => workspaceAPI.getMembers(workspaceId),
    enabled: !!workspaceId,
    // 목록이 배열이 아니면 화면이 깨지므로 여기서 막는다
    select: (data) => (Array.isArray(data) ? data : []),
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey });

  const inviteMutation = useMutation({
    mutationFn: (userId) => workspaceAPI.invite(workspaceId, userId, 'MEMBER'),
    onSuccess: () => showToast.success('초대가 완료되었습니다.'),
    onError: (error) => showToast.error(errorMessage(error, '초대 실패')),
  });

  const removeMutation = useMutation({
    mutationFn: (userId) => workspaceAPI.removeMember(workspaceId, userId),
    onSuccess: () => {
      invalidate();
      showToast.success('멤버가 제거되었습니다.');
    },
    onError: (error) => showToast.error(errorMessage(error, '멤버 제거 실패')),
  });

  const roleMutation = useMutation({
    mutationFn: ({ userId, role }) => workspaceAPI.updateMemberRole(workspaceId, userId, role),
    onSuccess: () => {
      invalidate();
      showToast.success('멤버 역할이 변경되었습니다.');
    },
    onError: (error) => showToast.error(errorMessage(error, '역할 변경 실패')),
  });

  return {
    members,
    invite: inviteMutation.mutate,
    removeMember: removeMutation.mutate,
    changeRole: roleMutation.mutate,
  };
};

/**
 * 초대할 사용자 검색. 본인과 이미 참여한 사람은 뺀다.
 */
export const useInviteeSearch = (members, currentUserId) => {
  const searchMutation = useMutation({
    mutationFn: (keyword) => authAPI.searchUsers(keyword),
    onError: (error) => showToast.error(errorMessage(error, '사용자 검색 실패')),
  });

  const exclude = (results) =>
    (results || []).filter(
      (user) =>
        user.id !== currentUserId && !members.some((member) => member.userId === user.id)
    );

  return {
    search: (keyword, onDone) =>
      searchMutation.mutate(keyword, {
        onSuccess: (results) => onDone(exclude(results)),
        onError: () => onDone([]),
      }),
  };
};
