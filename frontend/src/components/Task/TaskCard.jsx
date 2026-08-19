import { useNavigate } from 'react-router-dom';
import { useTaskProgress } from '../../hooks/useTask';
import { getDueDateColor, formatDueDate } from '../../utils/dateUtils';

export default function TaskCard({ task }) {
  const navigate = useNavigate();

  const { progress: stats } = useTaskProgress(task.id);
  const progress = stats.total === 0
    ? 0
    : Math.round((stats.confirmed / stats.total) * 100);

  return (
    <div
      onClick={() => navigate(`/task/${task.id}`)}
      className="border rounded-lg p-4 hover:shadow-md transition cursor-pointer bg-white"
    >
      <h3 className="font-bold text-lg mb-2">{task.title}</h3>

      {task.description && (
        <p className="text-sm text-gray-600 mb-3 line-clamp-2">
          {task.description}
        </p>
      )}

      {/* 진행률 바 */}
      {stats.total > 0 && (
        <div className="mt-3 mb-3">
          <div className="flex justify-between text-xs text-gray-600 mb-1">
            <span>진행률</span>
            <span className="font-semibold">{progress}%</span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-2">
            <div
              className="bg-blue-600 h-2 rounded-full transition-all duration-300"
              style={{ width: `${progress}%` }}
            />
          </div>
        </div>
      )}

      {/* 상태별 아이콘 배지 */}
      <div className="flex gap-3 text-sm mb-3">
        <span className="flex items-center gap-1 text-gray-500">
          <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm0-2a6 6 0 100-12 6 6 0 000 12z" clipRule="evenodd" />
          </svg>
          {stats.inProgress}
        </span>
        <span className="flex items-center gap-1 text-yellow-600">
          <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clipRule="evenodd" />
          </svg>
          {stats.waitingReview}
        </span>
        <span className="flex items-center gap-1 text-green-600">
          <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
          </svg>
          {stats.confirmed}
        </span>
      </div>

      {/* 매니저 정보 */}
      {task.managerDisplayName && (
        <div className="text-xs text-gray-500 mb-2">
          매니저: {task.managerDisplayName}
        </div>
      )}

      {/* 마감일 강조 */}
      {task.dueDate && (
        <p className={`text-xs ${getDueDateColor(task.dueDate)}`}>
           {formatDueDate(task.dueDate)}
        </p>
      )}

      {/* Todo가 없는 경우 */}
      {stats.total === 0 && (
        <p className="text-sm text-gray-400 italic">할일이 없습니다</p>
      )}
    </div>
  );
}
