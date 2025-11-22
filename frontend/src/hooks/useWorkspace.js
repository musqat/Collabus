import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { workspaceAPI } from '../api/workspace';

export const useWorkspaces = () => {
  const queryClient = useQueryClient();

  const { data: workspaces, isLoading } = useQuery({
    queryKey: ['workspaces'],
    queryFn: workspaceAPI.getJoinedWorkspaces,
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
    workspaces,
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
