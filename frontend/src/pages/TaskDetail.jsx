import { useState, useEffect } from 'react';
import { errorMessage } from '../api/errorMessage';
import { showToast } from '../store/toastStore';
import { useParams, useNavigate } from 'react-router-dom';
import { useTask, useTaskMembers, useTaskProgress } from '../hooks/useTask';
import usePageParam from '../hooks/usePageParam';
import useSortParam from '../hooks/useSortParam';
import SortSelect from '../components/SortSelect';
import Pagination from '../components/Pagination';
import { useTodos } from '../hooks/useTodo';
import { workspaceAPI } from '../api/workspace';
import { taskAPI } from '../api/task';
import { todoAPI } from '../api/todo';
import { useAuthStore } from '../store/authStore';
import { useQueryClient } from '@tanstack/react-query';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';

const TODO_SORT_OPTIONS = [
  { value: 'dueDate,asc', label: '마감일 빠른 순' },
  { value: 'dueDate,desc', label: '마감일 늦은 순' },
  { value: 'status,asc', label: '상태순' },
  { value: 'title,asc', label: '제목순' },
  { value: 'assignee.displayName,asc', label: '담당자순' },
];

export default function TaskDetail() {
  const { taskId } = useParams();
  const navigate = useNavigate();
  const { task, isLoading: taskLoading } = useTask(taskId);
  const [todoPage, setTodoPage] = usePageParam();
  const [todoSort, setTodoSort] = useSortParam(TODO_SORT_OPTIONS[0].value);
  const {
    todos,
    totalPages: todoTotalPages,
    isLoading: todosLoading,
    createTodo,
    completeTodo,
    confirmTodo,
  } = useTodos(taskId, { page: todoPage, sort: todoSort });
  const currentUser = useAuthStore((state) => state.user);
  const queryClient = useQueryClient();

  const [memberPage, setMemberPage] = usePageParam('memberPage');
  const { progress } = useTaskProgress(taskId);
  // 담당자 선택과 역할 판정에 전체 목록이 필요해 한 번에 받는다
  const { members: taskMembers } = useTaskMembers(taskId, { size: 100 });
  const {
    members: memberPageItems,
    totalPages: memberTotalPages,
  } = useTaskMembers(taskId, { page: memberPage });

  const [activeTab, setActiveTab] = useState('todos'); // 'todos' or 'members'
  const [showModal, setShowModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showTaskEditModal, setShowTaskEditModal] = useState(false);
  const [showAddMemberModal, setShowAddMemberModal] = useState(false);
  const [workspaceMembers, setWorkspaceMembers] = useState([]);
  const [selectedMember, setSelectedMember] = useState(null);
  const [editingTodo, setEditingTodo] = useState(null);
  const [editingTask, setEditingTask] = useState(null);
  const [workspaceRole, setWorkspaceRole] = useState(null);
  const [newTodo, setNewTodo] = useState({
    title: '',
    description: '',
    assigneeId: '',
    dueDate: ''
  });

  // 서버 집계값을 파이 차트 형식으로 옮긴다
  const progressData = [
    { name: '완료', value: progress.confirmed, color: '#10b981' },
    { name: '검수대기', value: progress.waitingReview, color: '#f59e0b' },
    { name: '진행중', value: progress.inProgress, color: '#3b82f6' }
  ].filter(item => item.value > 0);

  // 워크스페이스 멤버 조회 및 권한 확인
  useEffect(() => {
    if (task?.workspaceId) {
      workspaceAPI.getMembers(task.workspaceId, 0, 100)
        .then(data => {
          if (Array.isArray(data)) {
            setWorkspaceMembers(data);
            // 현재 사용자의 워크스페이스 역할 확인
            const currentUserMember = data.find(m => m.userId === currentUser?.id);
            setWorkspaceRole(currentUserMember?.role || null);
          } else {
            console.error('Members data is not an array:', data);
            setWorkspaceMembers([]);
            setWorkspaceRole(null);
          }
        })
        .catch(err => {
          console.error('Failed to fetch workspace members:', err);
          setWorkspaceMembers([]);
          setWorkspaceRole(null);
        });
    }
  }, [task?.workspaceId, currentUser?.id]);

  const handleCreate = (e) => {
    e.preventDefault();

    if (!newTodo.title || !newTodo.dueDate) {
      showToast.warning('제목, 마감일은 필수입니다.');
      return;
    }

    createTodo({
      taskId,
      assigneeId: newTodo.assigneeId ? parseInt(newTodo.assigneeId) : null,
      title: newTodo.title,
      description: newTodo.description,
      dueDate: newTodo.dueDate ? newTodo.dueDate.split('T')[0] : newTodo.dueDate
    });

    setNewTodo({
      title: '',
      description: '',
      assigneeId: '',
      dueDate: ''
    });
    setShowModal(false);
  };

  const getStatusConfig = (status) => {
    const configs = {
      IN_PROGRESS: { label: '진행중', bg: 'bg-blue-50', text: 'text-blue-700', border: 'border-blue-200' },
      WAITING_REVIEW: { label: '검수대기', bg: 'bg-yellow-50', text: 'text-yellow-700', border: 'border-yellow-200' },
      CONFIRMED: { label: '완료', bg: 'bg-green-50', text: 'text-green-700', border: 'border-green-200' }
    };
    return configs[status] || configs.IN_PROGRESS;
  };

  const handleAddMember = async () => {
    if (!selectedMember) {
      showToast.warning('추가할 멤버를 선택하세요.');
      return;
    }

    try {
      await taskAPI.addMember(taskId, selectedMember.userId);
      showToast.success('멤버가 추가되었습니다.');
      setShowAddMemberModal(false);
      setSelectedMember(null);

      queryClient.invalidateQueries({ queryKey: ['task-members', taskId] });
    } catch (error) {
      showToast.error(errorMessage(error, '멤버 추가 실패'));
    }
  };

  // 워크스페이스 멤버 중 아직 Task에 추가되지 않은 멤버들
  const availableMembers = workspaceMembers.filter(
    wm => !taskMembers.some(tm => tm.userId === wm.userId)
  );

  const handleRemoveTaskMember = async (userId) => {
    if (!confirm('정말 이 멤버를 제거하시겠습니까?')) {
      return;
    }

    try {
      await taskAPI.removeMember(taskId, userId);
      showToast.success('멤버가 제거되었습니다.');

      queryClient.invalidateQueries({ queryKey: ['task-members', taskId] });
    } catch (error) {
      showToast.error(errorMessage(error, '멤버 제거 실패'));
    }
  };

  const currentUserTaskRole = taskMembers.find(m => m.userId === currentUser?.id)?.role;
  const isTaskManager = currentUserTaskRole === 'MANAGER';

  const handleChangeAssignee = async (todoId, newAssigneeId) => {
    try {
      await todoAPI.changeAssignee(todoId, newAssigneeId);
      showToast.success('담당자가 변경되었습니다.');

      // Reload todos
      window.location.reload();
    } catch (error) {
      showToast.error(errorMessage(error, '담당자 변경 실패'));
    }
  };

  const handleEditTodo = (todo) => {
    setEditingTodo({
      id: todo.id,
      title: todo.title,
      description: todo.description || '',
      dueDate: todo.dueDate ? todo.dueDate.substring(0, 16) : ''
    });
    setShowEditModal(true);
  };

  const handleUpdateTodo = async (e) => {
    e.preventDefault();

    try {
      await todoAPI.update(editingTodo.id, editingTodo.title, editingTodo.description, editingTodo.dueDate);
      showToast.success('Todo가 수정되었습니다.');
      setShowEditModal(false);
      setEditingTodo(null);

      // Reload todos
      window.location.reload();
    } catch (error) {
      showToast.error(errorMessage(error, 'Todo 수정 실패'));
    }
  };

  const handleDeleteTodo = async (todoId) => {
    if (!confirm('정말 이 Todo를 삭제하시겠습니까?')) {
      return;
    }

    try {
      await todoAPI.delete(todoId);
      showToast.success('Todo가 삭제되었습니다.');

      // Reload todos
      window.location.reload();
    } catch (error) {
      showToast.error(errorMessage(error, 'Todo 삭제 실패'));
    }
  };

  const handleEditTask = () => {
    setEditingTask({
      title: task.title,
      description: task.description || '',
      dueDate: task.dueDate || ''
    });
    setShowTaskEditModal(true);
  };

  const handleUpdateTask = async (e) => {
    e.preventDefault();
    try {
      const dueDateOnly = editingTask.dueDate ? editingTask.dueDate.split('T')[0] : editingTask.dueDate;
      await taskAPI.update(taskId, editingTask.title, editingTask.description, dueDateOnly);
      showToast.success('Task가 수정되었습니다.');
      setShowTaskEditModal(false);
      setEditingTask(null);
      window.location.reload();
    } catch (error) {
      showToast.error(errorMessage(error, 'Task 수정 실패'));
    }
  };

  if (taskLoading || todosLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-white">
        <div className="text-gray-400">로딩 중...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white">
      {/* Header */}
      <div className="border-b border-gray-200">
        <div className="max-w-6xl mx-auto px-8 py-6">
          <div className="flex justify-between items-start mb-4">
            <button
              onClick={() => navigate(-1)}
              className="text-gray-400 hover:text-gray-600 text-sm flex items-center gap-1"
            >
              <span>←</span>
              <span>뒤로</span>
            </button>

            {(task?.managerId === currentUser?.id || workspaceRole === 'MASTER' || workspaceRole === 'MANAGER') && (
              <button
                onClick={handleEditTask}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition text-sm font-medium"
              >
                작업 수정
              </button>
            )}
          </div>

          <h1 className="text-4xl font-bold text-gray-900 mb-2">
            {task?.title}
          </h1>

          {task?.description && (
            <p className="text-gray-600 text-lg mb-4">{task.description}</p>
          )}

          <div className="flex gap-6 text-sm text-gray-500">
            <div className="flex items-center gap-2">
              <span className="text-gray-400">Manager</span>
              <span className="text-gray-700">{task?.managerDisplayName}</span>
            </div>
            <div className="flex items-center gap-2">
              <span className="text-gray-400">마감일</span>
              <span className="text-gray-700">{task?.dueDate}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200">
        <div className="max-w-6xl mx-auto px-8">
          <nav className="flex gap-8">
            <button
              onClick={() => setActiveTab('todos')}
              className={`py-4 border-b-2 font-medium text-sm transition-colors ${
                activeTab === 'todos'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              Todos
            </button>
            <button
              onClick={() => setActiveTab('members')}
              className={`py-4 border-b-2 font-medium text-sm transition-colors ${
                activeTab === 'members'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              Members
            </button>
          </nav>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-6xl mx-auto px-8 py-8">
        {activeTab === 'todos' ? (
          <>
            {/* Progress Chart Section */}
            {todos && todos.length > 0 && (
              <div className="mb-8 bg-gray-50 p-6 rounded-lg border border-gray-200">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Todo 진행률</h3>
                <div className="flex items-center gap-8">
                  <div style={{ width: 200, height: 200 }}>
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie
                          data={progressData}
                          cx="50%"
                          cy="50%"
                          innerRadius={60}
                          outerRadius={80}
                          paddingAngle={2}
                          dataKey="value"
                        >
                          {progressData.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={entry.color} />
                          ))}
                        </Pie>
                        <Tooltip />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                  <div className="flex-1">
                    <div className="space-y-2">
                      {progressData.map((item) => (
                        <div key={item.name} className="flex items-center gap-3">
                          <div
                            className="w-4 h-4 rounded"
                            style={{ backgroundColor: item.color }}
                          />
                          <span className="text-sm text-gray-700">{item.name}</span>
                          <span className="text-sm font-medium text-gray-900">
                            {item.value}개
                          </span>
                        </div>
                      ))}
                    </div>
                    <div className="mt-4 text-sm text-gray-500">
                      총 {todos.length}개의 Todo
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Todo List Header */}
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-semibold text-gray-900">Todo</h2>
              <div className="flex items-center gap-3">
              <SortSelect value={todoSort} options={TODO_SORT_OPTIONS} onChange={setTodoSort} />
              {(isTaskManager || workspaceRole === 'MASTER' || workspaceRole === 'MANAGER') && (
                <button
                  onClick={() => setShowModal(true)}
                  className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition text-sm font-medium"
                >
                  <span>+</span>
                  <span>Todo 추가</span>
                </button>
              )}
              </div>
            </div>

            {/* Todo Table */}
            {todos && todos.length > 0 ? (
              <div className="border border-gray-200 rounded-lg overflow-hidden">
                <table className="w-full">
                  <thead className="bg-gray-50 border-b border-gray-200">
                    <tr className="text-left text-sm text-gray-600">
                      <th className="px-4 py-3 font-medium w-1/4">제목</th>
                      <th className="px-4 py-3 font-medium w-1/6">담당자</th>
                      <th className="px-4 py-3 font-medium w-1/6">상태</th>
                      <th className="px-4 py-3 font-medium w-1/6">마감일</th>
                      <th className="px-4 py-3 font-medium w-1/4">액션</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200">
                    {todos.map((todo) => {
                      const statusConfig = getStatusConfig(todo.status);
                      return (
                        <tr key={todo.id} className="hover:bg-gray-50 transition">
                          <td className="px-4 py-3">
                            <div>
                              <button
                                onClick={() => navigate(`/todo/${todo.id}`)}
                                className="font-medium text-gray-900 hover:text-blue-600 text-left transition"
                              >
                                {todo.title}
                              </button>
                              {todo.description && (
                                <div className="text-sm text-gray-500 mt-1">{todo.description}</div>
                              )}
                            </div>
                          </td>
                          <td className="px-4 py-3">
                            {isTaskManager ? (
                              <select
                                value={todo.assigneeId}
                                onChange={(e) => handleChangeAssignee(todo.id, parseInt(e.target.value))}
                                className="text-sm px-2 py-1 border border-gray-300 rounded bg-white text-gray-700 hover:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
                              >
                                {Array.isArray(taskMembers) && taskMembers.map((member) => (
                                  <option key={member.userId} value={member.userId}>
                                    {member.displayName}
                                  </option>
                                ))}
                              </select>
                            ) : (
                              <span className="text-sm text-gray-700">{todo.assigneeDisplayName}</span>
                            )}
                          </td>
                          <td className="px-4 py-3">
                            <span className={`inline-flex items-center px-2.5 py-0.5 rounded-md text-xs font-medium border ${statusConfig.bg} ${statusConfig.text} ${statusConfig.border}`}>
                              {statusConfig.label}
                            </span>
                          </td>
                          <td className="px-4 py-3">
                            <span className="text-sm text-gray-600">
                              {new Date(todo.dueDate).toLocaleDateString('ko-KR')}
                            </span>
                          </td>
                          <td className="px-4 py-3">
                            <div className="flex gap-2">
                              {todo.status === 'IN_PROGRESS' && todo.assigneeId === currentUser?.id && (
                                <button
                                  onClick={() => completeTodo(todo.id)}
                                  className="text-xs px-3 py-1 rounded bg-yellow-100 text-yellow-700 hover:bg-yellow-200 transition"
                                >
                                  작업완료
                                </button>
                              )}
                              {todo.status === 'WAITING_REVIEW' && isTaskManager && (
                                <button
                                  onClick={() => confirmTodo(todo.id)}
                                  className="text-xs px-3 py-1 rounded bg-green-100 text-green-700 hover:bg-green-200 transition"
                                >
                                  승인
                                </button>
                              )}
                              {isTaskManager && (
                                <>
                                  <button
                                    onClick={() => handleEditTodo(todo)}
                                    className="text-xs px-3 py-1 rounded bg-blue-100 text-blue-700 hover:bg-blue-200 transition"
                                  >
                                    수정
                                  </button>
                                  <button
                                    onClick={() => handleDeleteTodo(todo.id)}
                                    className="text-xs px-3 py-1 rounded bg-red-100 text-red-700 hover:bg-red-200 transition"
                                  >
                                    삭제
                                  </button>
                                </>
                              )}
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>

                <Pagination page={todoPage} totalPages={todoTotalPages} onChange={setTodoPage} />

              </div>
            ) : (
              <div className="text-center py-12 border border-gray-200 rounded-lg">
                <p className="text-gray-400 mb-4">아직 Todo가 없습니다</p>
                {(isTaskManager || workspaceRole === 'MASTER' || workspaceRole === 'MANAGER') && (
                  <button
                    onClick={() => setShowModal(true)}
                    className="text-blue-600 hover:text-blue-700 text-sm"
                  >
                    첫 Todo를 추가해보세요
                  </button>
                )}
              </div>
            )}
          </>
        ) : activeTab === 'members' ? (
          <>
            {/* Members View */}
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-semibold text-gray-900">Members</h2>
              <div className="flex items-center gap-4">
                <div className="text-sm text-gray-500">{taskMembers.length}명</div>
                {isTaskManager && (
                  <button
                    onClick={() => setShowAddMemberModal(true)}
                    className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition text-sm font-medium"
                  >
                    <span>+</span>
                    <span>멤버 추가</span>
                  </button>
                )}
              </div>
            </div>

            <div className="border border-gray-200 rounded-lg overflow-hidden">
              <table className="w-full">
                <thead className="bg-gray-50 border-b border-gray-200">
                  <tr className="text-left text-sm text-gray-600">
                    <th className="px-4 py-3 font-medium">이름</th>
                    <th className="px-4 py-3 font-medium">역할</th>
                    {isTaskManager && <th className="px-4 py-3 font-medium">액션</th>}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {memberPageItems.map((member) => (
                    <tr key={member.userId} className="hover:bg-gray-50 transition">
                      <td className="px-4 py-3">
                        <div className="flex items-center gap-3">
                          <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-medium text-sm">
                            {member.displayName?.charAt(0).toUpperCase()}
                          </div>
                          <span className="text-gray-900">
                            {member.displayName}
                            {member.userId === currentUser?.id && (
                              <span className="ml-2 text-xs text-gray-500">(나)</span>
                            )}
                          </span>
                        </div>
                      </td>
                      <td className="px-4 py-3">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          member.role === 'MANAGER'
                            ? 'bg-purple-100 text-purple-800'
                            : 'bg-gray-100 text-gray-800'
                        }`}>
                          {member.role}
                        </span>
                      </td>
                      {isTaskManager && (
                        <td className="px-4 py-3">
                          {member.role !== 'MANAGER' && (
                            <button
                              onClick={() => handleRemoveTaskMember(member.userId)}
                              className="text-xs px-3 py-1 rounded bg-red-100 text-red-700 hover:bg-red-200 transition"
                            >
                              제거
                            </button>
                          )}
                        </td>
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>

              <Pagination page={memberPage} totalPages={memberTotalPages} onChange={setMemberPage} />
            </div>
          </>
        ) : null}
      </div>

      {/* Create Todo Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-lg w-full">
            <div className="border-b border-gray-200 px-6 py-4 flex justify-between items-center">
              <h3 className="text-xl font-semibold text-gray-900">Todo 추가</h3>
              <button
                onClick={() => setShowModal(false)}
                className="text-gray-400 hover:text-gray-600 text-2xl leading-none"
              >
                ×
              </button>
            </div>

            <form onSubmit={handleCreate} className="p-6 space-y-5">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  제목 <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={newTodo.title}
                  onChange={(e) => setNewTodo({ ...newTodo, title: e.target.value })}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="제목을 입력하세요"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  설명
                </label>
                <textarea
                  value={newTodo.description}
                  onChange={(e) => setNewTodo({ ...newTodo, description: e.target.value })}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="설명 (선택)"
                  rows="3"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  담당자 <span className="text-xs text-gray-500">(미지정 시 본인)</span>
                </label>
                <select
                  value={newTodo.assigneeId}
                  onChange={(e) => setNewTodo({ ...newTodo, assigneeId: e.target.value })}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">미정 (본인)</option>
                  {Array.isArray(taskMembers) && taskMembers.map((member) => (
                    <option key={member.userId} value={member.userId}>
                      {member.displayName}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  마감일 <span className="text-red-500">*</span>
                </label>
                <input
                  type="datetime-local"
                  value={newTodo.dueDate}
                  onChange={(e) => setNewTodo({ ...newTodo, dueDate: e.target.value })}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  required
                />
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  type="submit"
                  className="flex-1 px-4 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-medium"
                >
                  생성
                </button>
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="flex-1 px-4 py-2.5 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium"
                >
                  취소
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Add Member Modal */}
      {showAddMemberModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-lg w-full">
            <div className="border-b border-gray-200 px-6 py-4 flex justify-between items-center">
              <h3 className="text-xl font-semibold text-gray-900">멤버 추가</h3>
              <button
                onClick={() => {
                  setShowAddMemberModal(false);
                  setSelectedMember(null);
                }}
                className="text-gray-400 hover:text-gray-600 text-2xl leading-none"
              >
                ×
              </button>
            </div>

            <div className="p-6 space-y-5">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  워크스페이스 멤버 선택
                </label>
                {availableMembers.length > 0 ? (
                  <div className="border border-gray-300 rounded-lg p-3 max-h-60 overflow-y-auto">
                    <div className="space-y-2">
                      {availableMembers.map((member) => (
                        <label
                          key={member.userId}
                          className={`flex items-center gap-3 py-2 px-3 rounded-lg cursor-pointer transition ${
                            selectedMember?.userId === member.userId
                              ? 'bg-blue-50 border border-blue-300'
                              : 'hover:bg-gray-50'
                          }`}
                        >
                          <input
                            type="radio"
                            name="member"
                            checked={selectedMember?.userId === member.userId}
                            onChange={() => setSelectedMember(member)}
                            className="w-4 h-4 text-blue-600"
                          />
                          <div className="flex items-center gap-3 flex-1">
                            <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-medium text-sm">
                              {member.displayName?.charAt(0).toUpperCase()}
                            </div>
                            <div>
                              <div className="text-sm font-medium text-gray-900">{member.displayName}</div>
                              <div className="text-xs text-gray-500">{member.role}</div>
                            </div>
                          </div>
                        </label>
                      ))}
                    </div>
                  </div>
                ) : (
                  <div className="text-center py-8 text-gray-500">
                    추가할 수 있는 워크스페이스 멤버가 없습니다.
                  </div>
                )}
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  onClick={handleAddMember}
                  disabled={!selectedMember}
                  className="flex-1 px-4 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-medium disabled:bg-gray-300 disabled:cursor-not-allowed"
                >
                  추가
                </button>
                <button
                  onClick={() => {
                    setShowAddMemberModal(false);
                    setSelectedMember(null);
                  }}
                  className="flex-1 px-4 py-2.5 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium"
                >
                  취소
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Edit Todo Modal */}
      {showEditModal && editingTodo && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-lg w-full">
            <div className="border-b border-gray-200 px-6 py-4 flex justify-between items-center">
              <h3 className="text-xl font-semibold text-gray-900">Todo 수정</h3>
              <button
                onClick={() => {
                  setShowEditModal(false);
                  setEditingTodo(null);
                }}
                className="text-gray-400 hover:text-gray-600 text-2xl leading-none"
              >
                ×
              </button>
            </div>

            <form onSubmit={handleUpdateTodo} className="p-6 space-y-5">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  제목 <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={editingTodo.title}
                  onChange={(e) => setEditingTodo({ ...editingTodo, title: e.target.value })}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="제목을 입력하세요"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  설명
                </label>
                <textarea
                  value={editingTodo.description}
                  onChange={(e) => setEditingTodo({ ...editingTodo, description: e.target.value })}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="설명 (선택)"
                  rows="3"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  마감일 <span className="text-red-500">*</span>
                </label>
                <input
                  type="datetime-local"
                  value={editingTodo.dueDate}
                  onChange={(e) => setEditingTodo({ ...editingTodo, dueDate: e.target.value })}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  required
                />
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  type="submit"
                  className="flex-1 px-4 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-medium"
                >
                  수정
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowEditModal(false);
                    setEditingTodo(null);
                  }}
                  className="flex-1 px-4 py-2.5 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium"
                >
                  취소
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Task Edit Modal */}
      {showTaskEditModal && editingTask && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-lg w-full">
            <div className="border-b border-gray-200 px-6 py-4 flex justify-between items-center">
              <h3 className="text-xl font-semibold text-gray-900">작업 수정</h3>
              <button
                onClick={() => {
                  setShowTaskEditModal(false);
                  setEditingTask(null);
                }}
                className="text-gray-400 hover:text-gray-600 text-2xl leading-none"
              >
                ×
              </button>
            </div>

            <form onSubmit={handleUpdateTask} className="p-6 space-y-5">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  제목 <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={editingTask.title}
                  onChange={(e) => setEditingTask({ ...editingTask, title: e.target.value })}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="작업 제목"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  설명
                </label>
                <textarea
                  value={editingTask.description}
                  onChange={(e) => setEditingTask({ ...editingTask, description: e.target.value })}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="설명 (선택)"
                  rows="3"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  마감일 <span className="text-red-500">*</span>
                </label>
                <input
                  type="date"
                  value={editingTask.dueDate}
                  onChange={(e) => setEditingTask({ ...editingTask, dueDate: e.target.value })}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  required
                />
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  type="submit"
                  className="flex-1 px-4 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-medium"
                >
                  수정
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowTaskEditModal(false);
                    setEditingTask(null);
                  }}
                  className="flex-1 px-4 py-2.5 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium"
                >
                  취소
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
