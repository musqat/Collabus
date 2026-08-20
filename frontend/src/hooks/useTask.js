import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { showToast } from '../store/toastStore';
import { errorMessage } from '../api/errorMessage';
import { taskAPI } from '../api/task';

export const useTasks = (workspaceId, { page = 0, keyword = '', sort } = {}) => {
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ['tasks', workspaceId, page, keyword, sort],
    queryFn: () => taskAPI.getByWorkspace(workspaceId, { page, keyword, sort }),
    enabled: !!workspaceId,
    // 페이지를 넘기는 동안 이전 페이지를 그대로 보여준다
    placeholderData: keepPreviousData,
    refetchInterval: 30000, // 30초마다 자동 새로고침
  });

  const createMutation = useMutation({
    mutationFn: (taskData) => taskAPI.create(taskData),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', workspaceId] });
      queryClient.invalidateQueries({ queryKey: ['workspace-progress', workspaceId] });
      showToast.success('Task가 생성되었습니다.');
    },
    onError: (error) => {
      showToast.error(errorMessage(error, 'Task 생성 실패'));
    }
  });

  const updateMutation = useMutation({
    mutationFn: ({ taskId, title, description, dueDate }) =>
      taskAPI.update(taskId, title, description, dueDate),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', workspaceId] });
      showToast.success('Task가 수정되었습니다.');
    },
    onError: (error) => {
      showToast.error(errorMessage(error, 'Task 수정 실패'));
    }
  });

  const deleteMutation = useMutation({
    mutationFn: (taskId) => taskAPI.delete(taskId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', workspaceId] });
      queryClient.invalidateQueries({ queryKey: ['workspace-progress', workspaceId] });
      showToast.success('Task가 삭제되었습니다.');
    },
    onError: (error) => {
      showToast.error(errorMessage(error, 'Task 삭제 실패'));
    }
  });

  return {
    tasks: data?.content ?? [],
    page: data?.page ?? 0,
    totalPages: data?.totalPages ?? 0,
    totalElements: data?.totalElements ?? 0,
    isLoading,
    createTask: createMutation.mutate,
    updateTask: updateMutation.mutate,
    deleteTask: deleteMutation.mutate
  };
};

// Task 참여자 목록. page 로 페이지를 넘긴다
export const useTaskMembers = (taskId, { page = 0, size = 20 } = {}) => {
  const { data, isLoading } = useQuery({
    queryKey: ['task-members', taskId, page, size],
    queryFn: () => taskAPI.getMembers(taskId, { page, size }),
    enabled: !!taskId,
    // 페이지를 넘기는 동안 이전 페이지를 그대로 보여준다
    placeholderData: keepPreviousData,
  });

  return {
    members: data?.content ?? [],
    page: data?.page ?? 0,
    totalPages: data?.totalPages ?? 0,
    totalElements: data?.totalElements ?? 0,
    isLoading,
  };
};

// Task 의 Todo 를 상태별로 센 값을 받는다
export const useTaskProgress = (taskId) => {
  const { data } = useQuery({
    queryKey: ['task-progress', taskId],
    queryFn: () => taskAPI.getTaskProgress(taskId),
    enabled: !!taskId,
  });

  return { progress: data ?? { total: 0, inProgress: 0, waitingReview: 0, confirmed: 0 } };
};

// 볼 수 있는 Task 의 Todo 를 상태별로 센 값을 받는다
export const useWorkspaceProgress = (workspaceId) => {
  const { data, isLoading } = useQuery({
    queryKey: ['workspace-progress', workspaceId],
    queryFn: () => taskAPI.getWorkspaceProgress(workspaceId),
    enabled: !!workspaceId,
  });

  return {
    progress: data ?? { total: 0, inProgress: 0, waitingReview: 0, confirmed: 0 },
    isLoading,
  };
};

export const useTask = (taskId) => {
  const { data: task, isLoading } = useQuery({
    queryKey: ['task', taskId],
    queryFn: () => taskAPI.getById(taskId),
    enabled: !!taskId,
    refetchInterval: 30000, // 30초마다 자동 새로고침
  });

  return { task, isLoading };
};
