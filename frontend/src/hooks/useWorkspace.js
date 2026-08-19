import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { workspaceAPI } from '../api/workspace';

export const useWorkspaces = ({ page = 0 } = {}) => {
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ['workspaces', page],
    queryFn: () => workspaceAPI.getJoinedWorkspaces({ page }),
    // 페이지를 넘기는 동안 이전 페이지를 그대로 보여준다
    placeholderData: keepPreviousData,
    refetchInterval: 30000, // 30초마다 자동 새로고침
  });

  const createMutation = useMutation({
    mutationFn: ({ workspaceName, description }) =>
      workspaceAPI.create(workspaceName, description),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workspaces'] });
      alert('워크스페이스가 생성되었습니다.');
    },
    onError: (error) => {
      alert(error.response?.data?.message || '워크스페이스 생성 실패');
    }
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, workspaceName, description }) =>
      workspaceAPI.update(id, workspaceName, description),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workspaces'] });
      alert('워크스페이스가 수정되었습니다.');
    },
    onError: (error) => {
      alert(error.response?.data?.message || '워크스페이스 수정 실패');
    }
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => workspaceAPI.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workspaces'] });
      alert('워크스페이스가 삭제되었습니다.');
    },
    onError: (error) => {
      alert(error.response?.data?.message || '워크스페이스 삭제 실패');
    }
  });

  return {
    workspaces: data?.content ?? [],
    page: data?.page ?? 0,
    totalPages: data?.totalPages ?? 0,
    isLoading,
    createWorkspace: createMutation.mutate,
    updateWorkspace: updateMutation.mutate,
    deleteWorkspace: deleteMutation.mutate
  };
};

export const useWorkspace = (workspaceId) => {
  const { data: workspace, isLoading } = useQuery({
    queryKey: ['workspace', workspaceId],
    queryFn: () => workspaceAPI.getById(workspaceId),
    enabled: !!workspaceId,
    refetchInterval: 30000, // 30초마다 자동 새로고침
  });

  return { workspace, isLoading };
};
