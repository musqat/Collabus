import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { showToast } from '../store/toastStore';
import { errorMessage } from '../api/errorMessage';
import { todoAPI } from '../api/todo';

/**
 * Todo 상세 화면이 쓰는 조회와 변경을 모은다.
 * 성공 시 무효화할 키와 실패 문구를 한곳에 두어 화면에서 try/catch 를 없앤다.
 */

export const useTodo = (todoId) => {
  const { data: todo, isLoading } = useQuery({
    queryKey: ['todo', todoId],
    queryFn: () => todoAPI.getById(todoId),
    enabled: !!todoId,
  });

  return { todo, isLoading };
};

export const useTodoWorks = (todoId, page = 0, { enabled = true } = {}) => {
  const queryClient = useQueryClient();
  const queryKey = ['todo-works', todoId];

  const { data: workPage } = useQuery({
    queryKey: [...queryKey, page],
    queryFn: () => todoAPI.getWorks(todoId, page, 20),
    enabled: enabled && !!todoId,
    placeholderData: keepPreviousData,
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey });

  const createMutation = useMutation({
    mutationFn: ({ title, content }) => todoAPI.createWork(todoId, title, content),
    onSuccess: () => {
      invalidate();
      showToast.success('작업 내용이 등록되었습니다.');
    },
    onError: (error) => showToast.error(errorMessage(error, '작업 내용 등록 실패')),
  });

  const updateMutation = useMutation({
    mutationFn: ({ workId, title, content }) => todoAPI.updateWork(workId, title, content),
    onSuccess: () => {
      invalidate();
      showToast.success('작업 내용이 수정되었습니다.');
    },
    onError: (error) => showToast.error(errorMessage(error, '작업 내용 수정 실패')),
  });

  const deleteMutation = useMutation({
    mutationFn: (workId) => todoAPI.deleteWork(workId),
    onSuccess: () => {
      invalidate();
      showToast.success('작업 내용이 삭제되었습니다.');
    },
    onError: (error) => showToast.error(errorMessage(error, '작업 내용 삭제 실패')),
  });

  return {
    works: workPage?.content ?? [],
    totalPages: workPage?.totalPages ?? 0,
    createWork: createMutation.mutate,
    updateWork: updateMutation.mutate,
    deleteWork: deleteMutation.mutate,
  };
};

export const useTodoComments = (todoId, page = 0, { enabled = true } = {}) => {
  const queryClient = useQueryClient();
  const queryKey = ['todo-comments', todoId];

  const { data: commentPage } = useQuery({
    queryKey: [...queryKey, page],
    queryFn: () => todoAPI.getComments(todoId, page, 20),
    enabled: enabled && !!todoId,
    placeholderData: keepPreviousData,
  });

  const invalidate = () => queryClient.invalidateQueries({ queryKey });

  const createMutation = useMutation({
    mutationFn: (content) => todoAPI.createComment(todoId, content),
    onSuccess: invalidate,
    onError: (error) => showToast.error(errorMessage(error, '댓글 작성 실패')),
  });

  const updateMutation = useMutation({
    mutationFn: ({ commentId, content }) => todoAPI.updateComment(commentId, content),
    onSuccess: invalidate,
    onError: (error) => showToast.error(errorMessage(error, '댓글 수정 실패')),
  });

  const deleteMutation = useMutation({
    mutationFn: (commentId) => todoAPI.deleteComment(commentId),
    onSuccess: invalidate,
    onError: (error) => showToast.error(errorMessage(error, '댓글 삭제 실패')),
  });

  return {
    comments: commentPage?.content ?? [],
    totalPages: commentPage?.totalPages ?? 0,
    createComment: createMutation.mutate,
    updateComment: updateMutation.mutate,
    deleteComment: deleteMutation.mutate,
  };
};

/**
 * 작업 내용에 붙은 파일. 목록은 펼친 작업만 가져온다.
 */
export const useWorkFiles = () => {
  const queryClient = useQueryClient();

  const invalidate = (workId) =>
    queryClient.invalidateQueries({ queryKey: ['work-files', workId] });

  const fetchFiles = (workId) =>
    queryClient.fetchQuery({
      queryKey: ['work-files', workId],
      queryFn: () => todoAPI.getFilesByWork(workId, 0, 20),
    });

  const uploadMutation = useMutation({
    mutationFn: ({ workId, file }) => todoAPI.uploadFile(workId, file),
    onSuccess: (_data, { workId }) => {
      invalidate(workId);
      showToast.success('파일이 업로드되었습니다.');
    },
    onError: (error) => showToast.error(errorMessage(error, '파일 업로드 실패')),
  });

  const deleteMutation = useMutation({
    mutationFn: ({ fileId }) => todoAPI.deleteFile(fileId),
    onSuccess: (_data, { workId }) => invalidate(workId),
    onError: (error) => showToast.error(errorMessage(error, '파일 삭제 실패')),
  });

  const downloadMutation = useMutation({
    mutationFn: (fileId) => todoAPI.downloadFile(fileId),
    onError: (error) => showToast.error(errorMessage(error, '파일 다운로드 실패')),
  });

  return {
    fetchFiles,
    uploadFile: uploadMutation.mutate,
    deleteFile: deleteMutation.mutate,
    downloadFile: downloadMutation.mutate,
  };
};
