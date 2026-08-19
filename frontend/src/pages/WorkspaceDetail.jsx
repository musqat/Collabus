import { useEffect, useRef, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { useWorkspace } from '../hooks/useWorkspace';
import { useTasks, useWorkspaceProgress } from '../hooks/useTask';
import usePageParam from '../hooks/usePageParam';
import useDebouncedValue from '../hooks/useDebouncedValue';
import Pagination from '../components/Pagination';
import { workspaceAPI } from '../api/workspace';
import { authAPI } from '../api/auth';
import { useAuthStore } from '../store/authStore';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';
import TaskCard from '../components/Task/TaskCard';
import { EmptyState } from '../components/LoadingState';
import { useQuery } from '@tanstack/react-query';

export default function WorkspaceDetail() {
  const { workspaceId } = useParams();
  const navigate = useNavigate();
  const { workspace, isLoading: workspaceLoading } = useWorkspace(workspaceId);
  const [page, setPage] = usePageParam();
  const [taskSearchText, setTaskSearchText] = useState('');
  // 입력이 300ms 멈추면 그 값을 서버에 넘긴다
  const keyword = useDebouncedValue(taskSearchText, 300);

  const {
    tasks,
    totalPages,
    totalElements,
    isLoading: tasksLoading,
    createTask,
  } = useTasks(workspaceId, { page, keyword });
  const { progress } = useWorkspaceProgress(workspaceId);

  const currentUser = useAuthStore((state) => state.user);

  const [activeTab, setActiveTab] = useState('tasks'); // 'tasks' or 'members'
  const [showModal, setShowModal] = useState(false);
  const [showInviteModal, setShowInviteModal] = useState(false);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [dueDate, setDueDate] = useState('');
  const [managerId, setManagerId] = useState('');
  const [selectedMemberIds, setSelectedMemberIds] = useState([]);
  const [members, setMembers] = useState([]);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [selectedInvitee, setSelectedInvitee] = useState(null);
  const [showWorkspaceEditModal, setShowWorkspaceEditModal] = useState(false);
  const [editingWorkspace, setEditingWorkspace] = useState(null);

  // 워크스페이스 멤버 로드
  useEffect(() => {
    if (workspaceId) {
      workspaceAPI.getMembers(workspaceId)
        .then(data => {
          if (Array.isArray(data)) {
            setMembers(data);
          } else {
            console.error('Members data is not an array:', data);
            setMembers([]);
          }
        })
        .catch(err => {
          console.error('Failed to fetch members:', err);
          setMembers([]);
        });
    }
  }, [workspaceId]);

  // 검색어가 바뀌면 첫 페이지로 옮긴다. 첫 렌더는 건너뛰어 URL 의 page 를 살린다
  const previousKeyword = useRef(keyword);
  useEffect(() => {
    if (previousKeyword.current !== keyword) {
      previousKeyword.current = keyword;
      setPage(0);
    }
  }, [keyword, setPage]);

  // 서버 집계값을 파이 차트 형식으로 옮긴다
  const progressData = [
    { name: '완료', value: progress.confirmed, color: '#10b981' },
    { name: '검수대기', value: progress.waitingReview, color: '#f59e0b' },
    { name: '진행중', value: progress.inProgress, color: '#3b82f6' }
  ].filter(item => item.value > 0);

  const handleCreate = (e) => {
    e.preventDefault();

    const taskData = {
      workspaceId,
      title,
      description,
      dueDate: dueDate ? dueDate.split('T')[0] : dueDate, // LocalDate 형식으로 변환
      managerId: managerId ? parseInt(managerId) : null,
      memberIds: selectedMemberIds.length > 0 ? selectedMemberIds.map(id => parseInt(id)) : null
    };

    createTask(taskData);

    setTitle('');
    setDescription('');
    setDueDate('');
    setManagerId('');
    setSelectedMemberIds([]);
    setShowModal(false);
  };

  const handleMemberToggle = (memberId) => {
    setSelectedMemberIds(prev => {
      if (prev.includes(memberId)) {
        return prev.filter(id => id !== memberId);
      } else {
        return [...prev, memberId];
      }
    });
  };

  const handleSearch = async () => {
    if (searchKeyword.trim()) {
      try {
        const results = await authAPI.searchUsers(searchKeyword);
        // 본인과 이미 멤버인 사용자 제외
        const filteredResults = (results || []).filter(user => {
          // 본인 제외
          if (user.id === currentUser?.id) return false;
          // 이미 워크스페이스 멤버인 사용자 제외
          if (members.some(member => member.userId === user.id)) return false;
          return true;
        });
        setSearchResults(filteredResults);
      } catch (error) {
        console.error('Search failed:', error);
        setSearchResults([]);
      }
    }
  };

  const handleInvite = async () => {
    if (!selectedInvitee) {
      alert('초대할 사용자를 선택하세요.');
      return;
    }

    try {
      await workspaceAPI.invite(workspaceId, selectedInvitee.id, 'MEMBER');
      alert('초대가 완료되었습니다.');
      setShowInviteModal(false);
      setSearchKeyword('');
      setSearchResults([]);
      setSelectedInvitee(null);
    } catch (error) {
      alert(error.response?.data?.message || error.response?.data?.statusMsg || '초대 실패');
    }
  };

  const handleRemoveMember = async (userId) => {
    if (!confirm('정말 이 멤버를 제거하시겠습니까?')) {
      return;
    }

    try {
      await workspaceAPI.removeMember(workspaceId, userId);
      alert('멤버가 제거되었습니다.');
      // Reload members
      const data = await workspaceAPI.getMembers(workspaceId);
      if (Array.isArray(data)) {
        setMembers(data);
      }
    } catch (error) {
      alert(error.response?.data?.statusMsg || '멤버 제거 실패');
    }
  };

  const handleRoleChange = async (userId, newRole) => {
    try {
      await workspaceAPI.updateMemberRole(workspaceId, userId, newRole);
      alert('멤버 역할이 변경되었습니다.');
      // Reload members
      const data = await workspaceAPI.getMembers(workspaceId);
      if (Array.isArray(data)) {
        setMembers(data);
      }
    } catch (error) {
      alert(error.response?.data?.statusMsg || '역할 변경 실패');
    }
  };

  const currentUserRole = members.find(m => m.userId === currentUser?.id)?.role;
  const isMasterOrManager = currentUserRole === 'MASTER' || currentUserRole === 'MANAGER';

  const handleEditWorkspace = () => {
    setEditingWorkspace({
      workspaceName: workspace.workspaceName,
      description: workspace.description || ''
    });
    setShowWorkspaceEditModal(true);
  };

  const handleUpdateWorkspace = async (e) => {
    e.preventDefault();
    try {
      await workspaceAPI.update(workspaceId, editingWorkspace.workspaceName, editingWorkspace.description);
      alert('Workspace가 수정되었습니다.');
      setShowWorkspaceEditModal(false);
      setEditingWorkspace(null);
      window.location.reload();
    } catch (error) {
      alert(error.response?.data?.statusMsg || 'Workspace 수정 실패');
    }
  };

  if (workspaceLoading || tasksLoading) {
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
              onClick={() => navigate('/')}
              className="text-gray-400 hover:text-gray-600 text-sm flex items-center gap-1"
            >
              <span>←</span>
              <span>대시보드로</span>
            </button>

            {isMasterOrManager && (
              <button
                onClick={handleEditWorkspace}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition text-sm font-medium"
              >
                Workspace 수정
              </button>
            )}
          </div>

          <h1 className="text-4xl font-bold text-gray-900 mb-2">
            {workspace?.workspaceName}
          </h1>
          {workspace?.description && (
            <p className="text-gray-600 text-lg mb-4">{workspace.description}</p>
          )}
          <div className="text-sm text-gray-500">
            <span className="text-gray-400">Founder:</span>{' '}
            <span className="text-gray-700">{workspace?.founderDisplayName}</span>
          </div>
        </div>
      </div>

      {/* 통계 카드 */}
      <div className="border-b border-gray-200 bg-gray-50">
        <div className="max-w-6xl mx-auto px-8 py-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
              <div className="text-sm text-blue-600 font-medium mb-1">총 Task</div>
              <div className="text-3xl font-bold text-blue-900">{totalElements}</div>
            </div>
            <div className="bg-green-50 border border-green-200 rounded-lg p-6">
              <div className="text-sm text-green-600 font-medium mb-1">총 Todo</div>
              <div className="text-3xl font-bold text-green-900">{progress.total}</div>
            </div>
            <div className="bg-purple-50 border border-purple-200 rounded-lg p-6">
              <div className="text-sm text-purple-600 font-medium mb-1">완료된 Todo</div>
              <div className="text-3xl font-bold text-purple-900">{progress.confirmed}</div>
              {progress.total > 0 && (
                <div className="text-sm text-purple-600 mt-1">
                  {Math.round((progress.confirmed / progress.total) * 100)}% 완료
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-gray-200">
        <div className="max-w-6xl mx-auto px-8">
          <nav className="flex gap-8">
            <button
              onClick={() => setActiveTab('tasks')}
              className={`py-4 border-b-2 font-medium text-sm transition-colors ${
                activeTab === 'tasks'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              Tasks
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
        {activeTab === 'tasks' ? (
          <>
            {/* Progress Chart */}
            {tasks && tasks.length > 0 && progressData.length > 0 && (
              <div className="mb-8 bg-gray-50 p-6 rounded-lg border border-gray-200">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Task 진행률</h3>
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
                      총 {tasks.length}개의 Task · {progress.total}개의 Todo
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Task List Header */}
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-semibold text-gray-900">Task</h2>
              {isMasterOrManager && (
                <button
                  onClick={() => setShowModal(true)}
                  className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition text-sm font-medium"
                >
                  <span>+</span>
                  <span>Task 추가</span>
                </button>
              )}
            </div>

            {/* 검색 */}
            <div className="mb-6 flex flex-col sm:flex-row gap-3">
              {/* 검색 입력 */}
              <div className="flex-1">
                <input
                  type="text"
                  placeholder="Task 검색..."
                  value={taskSearchText}
                  onChange={(e) => setTaskSearchText(e.target.value)}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              {/* 검색 결과 개수 */}
              {keyword && (
                <div className="flex items-center text-sm text-gray-600 px-2">
                  {totalElements}개 결과
                </div>
              )}
            </div>

            {/* Task Grid */}
            {tasks && tasks.length > 0 ? (
              <>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {tasks.map((task) => (
                    <TaskCard key={task.id} task={task} />
                  ))}
                </div>
                <Pagination page={page} totalPages={totalPages} onChange={setPage} />
              </>
            ) : (
              <div className="text-center py-12 border border-gray-200 rounded-lg">
                {tasks && tasks.length > 0 ? (
                  <div>
                    <p className="text-gray-400 mb-4">검색 결과가 없습니다</p>
                    <button
                      onClick={() => setTaskSearchText('')}
                      className="text-blue-600 hover:text-blue-700 text-sm"
                    >
                      검색 초기화
                    </button>
                  </div>
                ) : (
                  <div>
                    <p className="text-gray-400 mb-4">아직 Task가 없습니다</p>
                    {isMasterOrManager && (
                      <button
                        onClick={() => setShowModal(true)}
                        className="text-blue-600 hover:text-blue-700 text-sm"
                      >
                        첫 Task를 추가해보세요
                      </button>
                    )}
                  </div>
                )}
              </div>
            )}
          </>
        ) : (
          <>
            {/* Members View */}
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-semibold text-gray-900">Members</h2>
              <div className="flex items-center gap-4">
                <div className="text-sm text-gray-500">{members.length}명</div>
                {isMasterOrManager && (
                  <button
                    onClick={() => setShowInviteModal(true)}
                    className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition text-sm font-medium"
                  >
                    <span>+</span>
                    <span>멤버 초대</span>
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
                    {isMasterOrManager && <th className="px-4 py-3 font-medium">액션</th>}
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {members.map((member) => (
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
                        {currentUserRole === 'MASTER' && member.userId !== currentUser?.id && member.role !== 'MASTER' ? (
                          <select
                            value={member.role}
                            onChange={(e) => handleRoleChange(member.userId, e.target.value)}
                            className={`px-2.5 py-0.5 rounded-full text-xs font-medium border-0 cursor-pointer ${
                              member.role === 'MANAGER'
                                ? 'bg-blue-100 text-blue-800'
                                : 'bg-gray-100 text-gray-800'
                            }`}
                          >
                            <option value="MANAGER">MANAGER</option>
                            <option value="MEMBER">MEMBER</option>
                          </select>
                        ) : (
                          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                            member.role === 'MASTER'
                              ? 'bg-purple-100 text-purple-800'
                              : member.role === 'MANAGER'
                              ? 'bg-blue-100 text-blue-800'
                              : 'bg-gray-100 text-gray-800'
                          }`}>
                            {member.role}
                          </span>
                        )}
                      </td>
                      {isMasterOrManager && (
                        <td className="px-4 py-3">
                          {member.userId !== currentUser?.id && (
                            <button
                              onClick={() => handleRemoveMember(member.userId)}
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
            </div>
          </>
        )}
      </div>

      {/* Create Task Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center">
              <h3 className="text-xl font-semibold text-gray-900">Task 추가</h3>
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
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="Task 제목을 입력하세요"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  설명
                </label>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="설명을 입력하세요 (선택)"
                  rows="3"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  마감일 <span className="text-red-500">*</span>
                </label>
                <input
                  type="date"
                  value={dueDate}
                  onChange={(e) => setDueDate(e.target.value)}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  담당자
                </label>
                <select
                  value={managerId}
                  onChange={(e) => setManagerId(e.target.value)}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="">미정</option>
                  {Array.isArray(members) && members.map((member) => (
                    <option key={member.userId} value={member.userId}>
                      {member.displayName}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  추가 팀원
                </label>
                <div className="border border-gray-300 rounded-lg p-3 max-h-40 overflow-y-auto bg-gray-50">
                  {Array.isArray(members) && members.length > 0 ? (
                    <div className="space-y-2">
                      {members.map((member) => (
                        <label
                          key={member.userId}
                          className="flex items-center gap-3 py-2 px-3 hover:bg-white rounded-lg cursor-pointer transition"
                        >
                          <input
                            type="checkbox"
                            checked={selectedMemberIds.includes(member.userId)}
                            onChange={() => handleMemberToggle(member.userId)}
                            className="w-4 h-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                          />
                          <span className="text-sm text-gray-700">{member.displayName}</span>
                        </label>
                      ))}
                    </div>
                  ) : (
                    <div className="text-sm text-gray-400 text-center py-4">
                      팀원이 없습니다
                    </div>
                  )}
                </div>
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

      {/* Invite Member Modal */}
      {showInviteModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-lg w-full">
            <div className="border-b border-gray-200 px-6 py-4 flex justify-between items-center">
              <h3 className="text-xl font-semibold text-gray-900">멤버 초대</h3>
              <button
                onClick={() => {
                  setShowInviteModal(false);
                  setSearchKeyword('');
                  setSearchResults([]);
                  setSelectedInvitee(null);
                }}
                className="text-gray-400 hover:text-gray-600 text-2xl leading-none"
              >
                ×
              </button>
            </div>

            <div className="p-6 space-y-5">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  사용자 검색
                </label>
                <div className="flex gap-2">
                  <input
                    type="text"
                    value={searchKeyword}
                    onChange={(e) => setSearchKeyword(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
                    className="flex-1 px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="이메일 또는 닉네임으로 검색"
                  />
                  <button
                    onClick={handleSearch}
                    className="px-4 py-2.5 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition font-medium"
                  >
                    검색
                  </button>
                </div>
              </div>

              {searchResults.length > 0 && (
                <div className="border border-gray-300 rounded-lg p-3 max-h-60 overflow-y-auto">
                  <div className="space-y-2">
                    {searchResults.map((user) => (
                      <label
                        key={user.id}
                        className={`flex items-center gap-3 py-2 px-3 rounded-lg cursor-pointer transition ${
                          selectedInvitee?.id === user.id
                            ? 'bg-blue-50 border border-blue-300'
                            : 'hover:bg-gray-50'
                        }`}
                      >
                        <input
                          type="radio"
                          name="invitee"
                          checked={selectedInvitee?.id === user.id}
                          onChange={() => setSelectedInvitee(user)}
                          className="w-4 h-4 text-blue-600"
                        />
                        <div className="flex-1">
                          <div className="text-sm font-medium text-gray-900">{user.nickname}</div>
                          <div className="text-xs text-gray-500">{user.displayName}</div>
                        </div>
                      </label>
                    ))}
                  </div>
                </div>
              )}

              <div className="flex gap-3 pt-4">
                <button
                  onClick={handleInvite}
                  className="flex-1 px-4 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition font-medium"
                >
                  초대
                </button>
                <button
                  onClick={() => {
                    setShowInviteModal(false);
                    setSearchKeyword('');
                    setSearchResults([]);
                    setSelectedInvitee(null);
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

      {/* Edit Workspace Modal */}
      {showWorkspaceEditModal && editingWorkspace && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-lg w-full">
            <div className="border-b border-gray-200 px-6 py-4 flex justify-between items-center">
              <h3 className="text-xl font-semibold text-gray-900">Workspace 수정</h3>
              <button
                onClick={() => {
                  setShowWorkspaceEditModal(false);
                  setEditingWorkspace(null);
                }}
                className="text-gray-400 hover:text-gray-600 text-2xl leading-none"
              >
                ×
              </button>
            </div>

            <form onSubmit={handleUpdateWorkspace} className="p-6 space-y-5">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Workspace 이름 <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={editingWorkspace.workspaceName}
                  onChange={(e) => setEditingWorkspace({ ...editingWorkspace, workspaceName: e.target.value })}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="Workspace 이름"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  설명
                </label>
                <textarea
                  value={editingWorkspace.description}
                  onChange={(e) => setEditingWorkspace({ ...editingWorkspace, description: e.target.value })}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="설명 (선택)"
                  rows="3"
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
                    setShowWorkspaceEditModal(false);
                    setEditingWorkspace(null);
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
