import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { todoAPI } from '../api/todo';

export const useTodos = (taskId, status = null) => {
  const queryClient = useQueryClient();

  const { data: todos, isLoading } = useQuery({
    queryKey: ['todos', taskId, status],
    queryFn: () => todoAPI.getByTask(taskId, status),
    enabled: !!taskId,
    refetchInterval: 30000, // 30초마다 자동 새로고침
  });

  const createMutation = useMutation({
    mutationFn: ({ taskId, assigneeId, title, description, dueDate }) =>
      todoAPI.create(taskId, assigneeId, title, description, dueDate),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['todos', taskId] });
      alert('Todo가 생성되었습니다.');
    },
    onError: (error) => {
      alert(error.response?.data?.message || 'Todo 생성 실패');
    }
  });

  const updateMutation = useMutation({
    mutationFn: ({ todoId, title, description, dueDate }) =>
      todoAPI.update(todoId, title, description, dueDate),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['todos', taskId] });
      alert('Todo가 수정되었습니다.');
    },
    onError: (error) => {
      alert(error.response?.data?.message || 'Todo 수정 실패');
    }
  });

  const deleteMutation = useMutation({
    mutationFn: (todoId) => todoAPI.delete(todoId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['todos', taskId] });
      alert('Todo가 삭제되었습니다.');
    },
    onError: (error) => {
      alert(error.response?.data?.message || 'Todo 삭제 실패');
    }
  });

  const completeMutation = useMutation({
    mutationFn: (todoId) => todoAPI.complete(todoId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['todos', taskId] });
      alert('Todo가 완료 처리되었습니다.');
    },
    onError: (error) => {
      alert(error.response?.data?.message || 'Todo 완료 처리 실패');
    }
  });

  const confirmMutation = useMutation({
    mutationFn: (todoId) => todoAPI.confirm(todoId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['todos', taskId] });
      alert('Todo가 최종 승인되었습니다.');
    },
    onError: (error) => {
      alert(error.response?.data?.message || 'Todo 승인 실패');
    }
  });

  return {
    todos,
    isLoading,
    createTodo: createMutation.mutate,
    updateTodo: updateMutation.mutate,
    deleteTodo: deleteMutation.mutate,
    completeTodo: completeMutation.mutate,
    confirmTodo: confirmMutation.mutate
  };
};
