import { useState, useEffect } from 'react';
import { showToast } from '../store/toastStore';
import { useParams, useNavigate } from 'react-router-dom';
import {
  useTodo,
  useTodoComments,
  useTodoWorks,
  useWorkFiles,
} from '../hooks/useTodoDetail';
import usePageParam from '../hooks/usePageParam';
import Pagination from '../components/Pagination';
import { useAuthStore } from '../store/authStore';

export default function TodoDetail() {
  const { todoId } = useParams();
  const navigate = useNavigate();
  const currentUser = useAuthStore((state) => state.user);

  const [activeTab, setActiveTab] = useState('work'); // 'work', 'comments'

  const [newWork, setNewWork] = useState({ title: '', content: '' });
  const [editingWork, setEditingWork] = useState(null);
  const [newComment, setNewComment] = useState('');
  const [editingComment, setEditingComment] = useState(null);

  // 펼친 작업의 파일 목록만 들고 있는다 (workId별)
  const [workFiles, setWorkFiles] = useState({});

  const [workPage, setWorkPage] = usePageParam();
  const [commentPage, setCommentPage] = usePageParam('commentPage');

  const { todo, isLoading: loading } = useTodo(todoId);
  const {
    works, totalPages: workTotalPages, createWork, updateWork, deleteWork,
  } = useTodoWorks(todoId, workPage, { enabled: activeTab === 'work' });
  const {
    comments, totalPages: commentTotalPages, createComment, updateComment, deleteComment,
  } = useTodoComments(todoId, commentPage, { enabled: activeTab === 'comments' });
  const { fetchFiles, uploadFile, deleteFile, downloadFile } = useWorkFiles();

  // 조회에 실패하면 머무를 이유가 없다
  useEffect(() => {
    if (!loading && todoId && todo === undefined) {
      showToast.error('Todo를 불러올 수 없습니다.');
      navigate(-1);
    }
  }, [loading, todo, todoId, navigate]);

  const loadFilesForWork = async (workId) => {
    const page = await fetchFiles(workId);
    setWorkFiles((prev) => ({ ...prev, [workId]: page?.content ?? [] }));
  };

  // Work 관련 핸들러
  const handleCreateWork = (e) => {
    e.preventDefault();
    if (!newWork.title.trim()) {
      showToast.warning('제목을 입력하세요.');
      return;
    }
    createWork(
      { title: newWork.title, content: newWork.content },
      { onSuccess: () => setNewWork({ title: '', content: '' }) }
    );
  };

  const handleUpdateWork = (e) => {
    e.preventDefault();
    updateWork(
      { workId: editingWork.id, title: editingWork.title, content: editingWork.content },
      { onSuccess: () => setEditingWork(null) }
    );
  };

  const handleDeleteWork = (workId) => {
    if (!confirm('작업 내용을 삭제하시겠습니까?')) return;
    deleteWork(workId);
  };

  // Comment 관련 핸들러
  const handleCreateComment = (e) => {
    e.preventDefault();
    if (!newComment.trim()) {
      showToast.warning('댓글을 입력하세요.');
      return;
    }
    createComment(newComment, { onSuccess: () => setNewComment('') });
  };

  const handleUpdateComment = (commentId, content) => {
    updateComment({ commentId, content }, { onSuccess: () => setEditingComment(null) });
  };

  const handleDeleteComment = (commentId) => {
    if (!confirm('댓글을 삭제하시겠습니까?')) return;
    deleteComment(commentId);
  };

  // File 관련 핸들러
  const handleFileUpload = (workId, e) => {
    const file = e.target.files[0];
    if (!file) return;

    const input = e.target;
    uploadFile({ workId, file }, {
      onSuccess: () => {
        loadFilesForWork(workId);
        input.value = '';
      },
    });
  };

  // 다운로드 API는 인증이 필요하므로 <a download> 대신 blob 으로 받아 저장한다
  const handleDownloadFile = (file) => {
    downloadFile(file.id, {
      onSuccess: (blob) => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = file.originalFileName;
        document.body.appendChild(link);
        link.click();
        link.remove();
        URL.revokeObjectURL(url);
      },
    });
  };

  const handleDeleteFile = (workId, fileId) => {
    if (!confirm('파일을 삭제하시겠습니까?')) return;
    deleteFile({ workId, fileId }, { onSuccess: () => loadFilesForWork(workId) });
  };

  const getStatusConfig = (status) => {
    const configs = {
      IN_PROGRESS: { label: '진행중', bg: 'bg-blue-50', text: 'text-blue-700', border: 'border-blue-200' },
      WAITING_REVIEW: { label: '검수대기', bg: 'bg-yellow-50', text: 'text-yellow-700', border: 'border-yellow-200' },
      CONFIRMED: { label: '완료', bg: 'bg-green-50', text: 'text-green-700', border: 'border-green-200' }
    };
    return configs[status] || configs.IN_PROGRESS;
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-white">
        <div className="text-gray-400">로딩 중...</div>
      </div>
    );
  }

  const statusConfig = getStatusConfig(todo?.status);

  return (
    <div className="min-h-screen bg-white">
      {/* Header */}
      <div className="border-b border-gray-200">
        <div className="max-w-6xl mx-auto px-8 py-6">
          <button
            onClick={() => navigate(-1)}
            className="text-gray-400 hover:text-gray-600 mb-4 text-sm flex items-center gap-1"
          >
            <span>←</span>
            <span>뒤로</span>
          </button>

          <div className="flex items-start justify-between">
            <div className="flex-1">
              <h1 className="text-4xl font-bold text-gray-900 mb-4">
                {todo?.title}
              </h1>

              {todo?.description && (
                <p className="text-gray-600 text-lg mb-4">{todo.description}</p>
              )}

              <div className="flex gap-6 text-sm text-gray-600">
                <div className="flex items-center gap-2">
                  <span className="text-gray-400">담당자</span>
                  <span className="text-gray-700">{todo?.assigneeDisplayName}</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-gray-400">마감일</span>
                  <span className="text-gray-700">{todo?.dueDate}</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-gray-400">상태</span>
                  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-md text-xs font-medium border ${statusConfig.bg} ${statusConfig.text} ${statusConfig.border}`}>
                    {statusConfig.label}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200">
        <div className="max-w-6xl mx-auto px-8">
          <nav className="flex gap-8">
            <button
              onClick={() => setActiveTab('work')}
              className={`py-4 border-b-2 font-medium text-sm transition-colors ${
                activeTab === 'work'
                  ? 'border-brand-600 text-brand-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              작업 내용
            </button>
            <button
              onClick={() => setActiveTab('comments')}
              className={`py-4 border-b-2 font-medium text-sm transition-colors ${
                activeTab === 'comments'
                  ? 'border-brand-600 text-brand-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              댓글
            </button>
          </nav>
        </div>
      </div>

      {/* Content */}
      <div className="max-w-6xl mx-auto px-8 py-8">
        {/* Work Tab */}
        {activeTab === 'work' && (
          <div className="space-y-6">
            {/* Work 작성 폼 - 담당자만 표시 */}
            {todo?.assigneeId === currentUser?.id && (
              <div className="bg-gray-50 p-6 rounded-lg border border-gray-200">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">작업 내용 등록</h3>
                <form onSubmit={editingWork ? handleUpdateWork : handleCreateWork} className="space-y-4">
                  <div>
                    <input
                      type="text"
                      value={editingWork ? editingWork.title : newWork.title}
                      onChange={(e) => editingWork
                        ? setEditingWork({ ...editingWork, title: e.target.value })
                        : setNewWork({ ...newWork, title: e.target.value })
                      }
                      placeholder="작업 제목"
                      className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500"
                      required
                    />
                  </div>
                  <div>
                    <textarea
                      value={editingWork ? editingWork.content : newWork.content}
                      onChange={(e) => editingWork
                        ? setEditingWork({ ...editingWork, content: e.target.value })
                        : setNewWork({ ...newWork, content: e.target.value })
                      }
                      placeholder="작업 내용을 상세히 작성하세요..."
                      className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 min-h-32"
                      rows="4"
                    />
                  </div>
                  <div className="flex gap-2">
                    <button
                      type="submit"
                      className="px-4 py-2 bg-brand-600 text-white rounded-lg hover:bg-brand-700 transition font-medium"
                    >
                      {editingWork ? '수정' : '등록'}
                    </button>
                    {editingWork && (
                      <button
                        type="button"
                        onClick={() => setEditingWork(null)}
                        className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium"
                      >
                        취소
                      </button>
                    )}
                  </div>
                </form>
              </div>
            )}

            {/* Work 목록 */}
            <div className="space-y-4">
              {works.length > 0 ? (
                works.map((work) => {
                  const workFileList = workFiles[work.id] || [];
                  return (
                    <div key={work.id} className="bg-white p-6 rounded-lg border border-gray-200">
                      <div className="flex justify-between items-start mb-3">
                        <h4 className="text-lg font-semibold text-gray-900">{work.title}</h4>
                        {work.authorId === currentUser?.id && (
                          <div className="flex gap-2">
                            <button
                              onClick={() => setEditingWork(work)}
                              className="text-sm px-3 py-1 rounded bg-brand-100 text-brand-700 hover:bg-brand-200 transition"
                            >
                              수정
                            </button>
                            <button
                              onClick={() => handleDeleteWork(work.id)}
                              className="text-sm px-3 py-1 rounded bg-red-100 text-red-700 hover:bg-red-200 transition"
                            >
                              삭제
                            </button>
                          </div>
                        )}
                      </div>
                      <p className="text-gray-600 whitespace-pre-wrap mb-3">{work.content}</p>
                      <div className="text-xs text-gray-400 mb-4">
                        {work.authorDisplayName} · {new Date(work.createdAt).toLocaleString('ko-KR')}
                      </div>

                      {/* 파일 섹션 */}
                      <div className="mt-4 pt-4 border-t border-gray-200">
                        <div className="flex items-center justify-between mb-3">
                          <h5 className="text-sm font-semibold text-gray-700">첨부 파일</h5>
                          {work.authorId === currentUser?.id && (
                            <label className="text-xs px-3 py-1 rounded bg-brand-100 text-brand-700 hover:bg-brand-200 transition cursor-pointer">
                              파일 추가
                              <input
                                type="file"
                                onChange={(e) => handleFileUpload(work.id, e)}
                                onFocus={() => !workFileList.length && loadFilesForWork(work.id)}
                                className="hidden"
                              />
                            </label>
                          )}
                        </div>

                        {workFileList.length === 0 && !workFiles[work.id] && (
                          <button
                            onClick={() => loadFilesForWork(work.id)}
                            className="text-xs text-gray-500 hover:text-gray-700"
                          >
                            파일 목록 보기
                          </button>
                        )}

                        {workFileList.length > 0 ? (
                          <div className="space-y-2">
                            {workFileList.map((file) => (
                              <div key={file.id} className="flex items-center justify-between bg-gray-50 p-3 rounded-lg">
                                <div className="flex items-center gap-2">
                                  <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                                  </svg>
                                  <div>
                                    <div className="text-xs font-medium text-gray-900">{file.originalFileName}</div>
                                    <div className="text-xs text-gray-400">
                                      {file.uploaderDisplayName} · {new Date(file.uploadedAt).toLocaleString('ko-KR')}
                                    </div>
                                  </div>
                                </div>
                                <div className="flex gap-2">
                                  <button
                                    onClick={() => handleDownloadFile(file)}
                                    className="text-xs px-2 py-1 rounded bg-brand-100 text-brand-700 hover:bg-brand-200 transition"
                                  >
                                    다운로드
                                  </button>
                                  {file.uploaderId === currentUser?.id && (
                                    <button
                                      onClick={() => handleDeleteFile(work.id, file.id)}
                                      className="text-xs px-2 py-1 rounded bg-red-100 text-red-700 hover:bg-red-200 transition"
                                    >
                                      삭제
                                    </button>
                                  )}
                                </div>
                              </div>
                            ))}
                          </div>
                        ) : workFiles[work.id] !== undefined && (
                          <p className="text-xs text-gray-400">첨부된 파일이 없습니다</p>
                        )}
                      </div>
                    </div>
                  );
                })
              ) : (
                <div className="text-center py-12 border border-gray-200 rounded-lg">
                  <p className="text-gray-400">작업 내용이 없습니다</p>
                </div>
              )}
            </div>

            <Pagination page={workPage} totalPages={workTotalPages} onChange={setWorkPage} />
          </div>
        )}

        {/* Comments Tab */}
        {activeTab === 'comments' && (
          <div className="space-y-6">
            {/* 댓글 작성 폼 */}
            <div className="bg-gray-50 p-6 rounded-lg border border-gray-200">
              <form onSubmit={handleCreateComment} className="space-y-4">
                <textarea
                  value={newComment}
                  onChange={(e) => setNewComment(e.target.value)}
                  placeholder="댓글을 입력하세요..."
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500"
                  rows="3"
                />
                <button
                  type="submit"
                  className="px-4 py-2 bg-brand-600 text-white rounded-lg hover:bg-brand-700 transition font-medium"
                >
                  댓글 작성
                </button>
              </form>
            </div>

            {/* 댓글 목록 */}
            <div className="space-y-4">
              {comments.length > 0 ? (
                comments.map((comment) => (
                  <div key={comment.id} className="bg-white p-4 rounded-lg border border-gray-200">
                    {editingComment?.id === comment.id ? (
                      <form onSubmit={(e) => {
                        e.preventDefault();
                        handleUpdateComment(comment.id, editingComment.content);
                      }} className="space-y-2">
                        <textarea
                          value={editingComment.content}
                          onChange={(e) => setEditingComment({ ...editingComment, content: e.target.value })}
                          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500"
                          rows="2"
                        />
                        <div className="flex gap-2">
                          <button type="submit" className="text-sm px-3 py-1 rounded bg-brand-100 text-brand-700 hover:bg-brand-200 transition">
                            저장
                          </button>
                          <button type="button" onClick={() => setEditingComment(null)} className="text-sm px-3 py-1 rounded bg-gray-100 text-gray-700 hover:bg-gray-200 transition">
                            취소
                          </button>
                        </div>
                      </form>
                    ) : (
                      <>
                        <div className="flex justify-between items-start mb-2">
                          <div className="text-sm font-medium text-gray-900">{comment.authorDisplayName}</div>
                          {comment.authorId === currentUser?.id && (
                            <div className="flex gap-2">
                              <button
                                onClick={() => setEditingComment(comment)}
                                className="text-xs px-2 py-1 rounded bg-brand-100 text-brand-700 hover:bg-brand-200 transition"
                              >
                                수정
                              </button>
                              <button
                                onClick={() => handleDeleteComment(comment.id)}
                                className="text-xs px-2 py-1 rounded bg-red-100 text-red-700 hover:bg-red-200 transition"
                              >
                                삭제
                              </button>
                            </div>
                          )}
                        </div>
                        <p className="text-gray-600 whitespace-pre-wrap mb-2">{comment.content}</p>
                        <div className="text-xs text-gray-400">
                          {new Date(comment.createdAt).toLocaleString('ko-KR')}
                        </div>
                      </>
                    )}
                  </div>
                ))
              ) : (
                <div className="text-center py-12 border border-gray-200 rounded-lg">
                  <p className="text-gray-400">댓글이 없습니다</p>
                </div>
              )}
            </div>

            <Pagination page={commentPage} totalPages={commentTotalPages} onChange={setCommentPage} />
          </div>
        )}
      </div>
    </div>
  );
}
