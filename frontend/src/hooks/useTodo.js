import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { todoAPI } from '../api/todo';

export const useTodos = (taskId, { status = null, page = 0, sort } = {}) => {
  const queryClient = useQueryClient();

  const { data: todoPage, isLoading } = useQuery({
    queryKey: ['todos', taskId, status, page, sort],
    queryFn: () => todoAPI.getByTask(taskId, status, page, 20, sort),
    enabled: !!taskId,
    // 페이지를 넘기는 동안 이전 페이지를 그대로 보여준다
    placeholderData: keepPreviousData,
    refetchInterval: 30000, // 30초마다 자동 새로고침
  });

  const todos = todoPage?.content ?? [];

  const createMutation = useMutation({
    mutationFn: ({ taskId, assigneeId, title, description, dueDate }) =>
      todoAPI.create(taskId, assigneeId, title, description, dueDate),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['todos', taskId] });
      queryClient.invalidateQueries({ queryKey: ['task-progress', taskId] });
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
      queryClient.invalidateQueries({ queryKey: ['task-progress', taskId] });
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
      queryClient.invalidateQueries({ queryKey: ['task-progress', taskId] });
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
      queryClient.invalidateQueries({ queryKey: ['task-progress', taskId] });
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
      queryClient.invalidateQueries({ queryKey: ['task-progress', taskId] });
      alert('Todo가 최종 승인되었습니다.');
    },
    onError: (error) => {
      alert(error.response?.data?.message || 'Todo 승인 실패');
    }
  });

  return {
    todos,
    page: todoPage?.page ?? 0,
    totalPages: todoPage?.totalPages ?? 0,
    totalElements: todoPage?.totalElements ?? 0,
    isLoading,
    createTodo: createMutation.mutate,
    updateTodo: updateMutation.mutate,
    deleteTodo: deleteMutation.mutate,
    completeTodo: completeMutation.mutate,
    confirmTodo: confirmMutation.mutate
  };
};
