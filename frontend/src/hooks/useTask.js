import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { taskAPI } from '../api/task';

export const useTasks = (workspaceId) => {
  const queryClient = useQueryClient();

  const { data: tasks, isLoading } = useQuery({
    queryKey: ['tasks', workspaceId],
    queryFn: () => taskAPI.getByWorkspace(workspaceId),
    enabled: !!workspaceId,
    refetchInterval: 30000, // 30초마다 자동 새로고침
  });

  const createMutation = useMutation({
    mutationFn: (taskData) => taskAPI.create(taskData),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', workspaceId] });
      alert('Task가 생성되었습니다.');
    },
    onError: (error) => {
      alert(error.response?.data?.message || 'Task 생성 실패');
    }
  });

  const updateMutation = useMutation({
    mutationFn: ({ taskId, title, description, dueDate }) =>
      taskAPI.update(taskId, title, description, dueDate),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', workspaceId] });
      alert('Task가 수정되었습니다.');
    },
    onError: (error) => {
      alert(error.response?.data?.message || 'Task 수정 실패');
    }
  });

  const deleteMutation = useMutation({
    mutationFn: (taskId) => taskAPI.delete(taskId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', workspaceId] });
      alert('Task가 삭제되었습니다.');
    },
    onError: (error) => {
      alert(error.response?.data?.message || 'Task 삭제 실패');
    }
  });

  return {
    tasks,
    isLoading,
    createTask: createMutation.mutate,
    updateTask: updateMutation.mutate,
    deleteTask: deleteMutation.mutate
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
