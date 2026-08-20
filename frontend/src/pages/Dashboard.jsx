import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useWorkspaces } from '../hooks/useWorkspace';
import usePageParam from '../hooks/usePageParam';
import useSortParam from '../hooks/useSortParam';
import SortSelect from '../components/SortSelect';
import Pagination from '../components/Pagination';

const WORKSPACE_SORT_OPTIONS = [
  { value: 'createdAt,desc', label: '최근 생성순' },
  { value: 'createdAt,asc', label: '오래된 순' },
  { value: 'workspaceName,asc', label: '이름순' },
];

export default function Dashboard() {
  const [page, setPage] = usePageParam();
  const [sort, setSort] = useSortParam(WORKSPACE_SORT_OPTIONS[0].value);
  const { workspaces, totalPages, isLoading, createWorkspace } = useWorkspaces({ page, sort });
  const [showModal, setShowModal] = useState(false);
  const [workspaceName, setWorkspaceName] = useState('');
  const [description, setDescription] = useState('');

  const handleCreate = (e) => {
    e.preventDefault();
    createWorkspace({ workspaceName, description });
    setWorkspaceName('');
    setDescription('');
    setShowModal(false);
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-xl text-gray-600">로딩 중...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white">
      <div className="max-w-6xl mx-auto px-8 py-8">
        <div className="flex justify-between items-center mb-8">
          <h2 className="text-3xl font-bold text-gray-800">대시보드</h2>
          <button
            onClick={() => setShowModal(true)}
            className="flex items-center gap-2 bg-brand-600 text-white px-5 py-2.5 rounded-lg hover:bg-brand-700 transition font-medium"
          >
            <span>+</span>
            <span>새 워크스페이스</span>
          </button>
        </div>

        {/* 워크스페이스 목록 */}
        <div className="flex items-center justify-between mb-6">
          <h3 className="text-2xl font-bold text-gray-800">워크스페이스</h3>
          <SortSelect value={sort} options={WORKSPACE_SORT_OPTIONS} onChange={setSort} />
        </div>
        {workspaces && workspaces.length > 0 ? (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {workspaces.map((workspace) => (
              <Link
                key={workspace.id}
                to={`/workspace/${workspace.id}`}
                className="bg-white p-6 rounded-lg border border-gray-200 hover:border-brand-300 hover:shadow-lg transition transform hover:-translate-y-1"
              >
                <h3 className="text-xl font-bold text-gray-800 mb-2">
                  {workspace.workspaceName}
                </h3>
                <p className="text-gray-600 mb-4 line-clamp-2">
                  {workspace.description || '설명 없음'}
                </p>
                <div className="text-sm text-gray-500">
                  Created by {workspace.founderDisplayName}
                </div>
              </Link>
            ))}
            </div>

            <Pagination page={page} totalPages={totalPages} onChange={setPage} />
          </>
        ) : (
          <div className="text-center py-16 border border-gray-200 rounded-lg bg-gray-50">
            <p className="text-gray-500 text-lg mb-4">
              아직 워크스페이스가 없습니다
            </p>
            <button
              onClick={() => setShowModal(true)}
              className="text-brand-600 hover:text-brand-700 font-medium"
            >
              첫 워크스페이스를 만들어보세요
            </button>
          </div>
        )}
      </div>

      {/* Create Workspace Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-lg w-full">
            <div className="border-b border-gray-200 px-6 py-4 flex justify-between items-center">
              <h3 className="text-xl font-semibold text-gray-900">새 워크스페이스 만들기</h3>
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
                  워크스페이스 이름 <span className="text-red-500">*</span>
                </label>
                <input
                  type="text"
                  value={workspaceName}
                  onChange={(e) => setWorkspaceName(e.target.value)}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                  placeholder="예: 마케팅팀"
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
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent"
                  placeholder="워크스페이스 설명"
                  rows="3"
                />
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  type="submit"
                  className="flex-1 px-4 py-2.5 bg-brand-600 text-white rounded-lg hover:bg-brand-700 transition font-medium"
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
    </div>
  );
}
