import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { workspaceAPI } from '../api/workspace';
import { taskAPI } from '../api/task';

export default function Sidebar({ collapsed, onToggle }) {
  const [workspaces, setWorkspaces] = useState([]);
  const [expandedWorkspaces, setExpandedWorkspaces] = useState(new Set());
  const [expandedTasks, setExpandedTasks] = useState(new Set());
  const [workspaceTasks, setWorkspaceTasks] = useState({});
  const [taskTodos, setTaskTodos] = useState({});
  const navigate = useNavigate();
  const location = useLocation();

  // 워크스페이스 목록 조회
  useEffect(() => {
    workspaceAPI.getJoinedWorkspaces()
      .then(data => {
        if (Array.isArray(data)) {
          setWorkspaces(data);
        } else {
          console.error('Workspaces data is not an array:', data);
          setWorkspaces([]);
        }
      })
      .catch(err => {
        console.error('Failed to fetch workspaces:', err);
        setWorkspaces([]);
      });
  }, []);

  // 페이지 이동 시 펼쳐진 워크스페이스/태스크 데이터 새로고침
  useEffect(() => {
    const refreshExpandedData = async () => {
      // 펼쳐진 워크스페이스의 Task 목록 새로고침
      for (const workspaceId of expandedWorkspaces) {
        try {
          const { content: tasks } = await taskAPI.getByWorkspace(workspaceId);
          setWorkspaceTasks(prev => ({
            ...prev,
            [workspaceId]: Array.isArray(tasks) ? tasks : []
          }));
        } catch (error) {
          console.error('Failed to refresh tasks:', error);
        }
      }

      // 펼쳐진 Task의 Todo 목록 새로고침
      for (const taskId of expandedTasks) {
        try {
          const todos = await taskAPI.getTodos(taskId);
          setTaskTodos(prev => ({
            ...prev,
            [taskId]: Array.isArray(todos) ? todos : []
          }));
        } catch (error) {
          console.error('Failed to refresh todos:', error);
        }
      }
    };

    refreshExpandedData();
  }, [location.pathname]);

  // 워크스페이스 펼치기/접기
  const toggleWorkspace = async (workspaceId) => {
    const newExpanded = new Set(expandedWorkspaces);

    if (newExpanded.has(workspaceId)) {
      newExpanded.delete(workspaceId);
    } else {
      newExpanded.add(workspaceId);

      // Task 목록 로드 (항상 최신 데이터 로드)
      try {
        const { content: tasks } = await taskAPI.getByWorkspace(workspaceId);
        setWorkspaceTasks(prev => ({
          ...prev,
          [workspaceId]: Array.isArray(tasks) ? tasks : []
        }));
      } catch (error) {
        console.error('Failed to load tasks:', error);
      }
    }

    setExpandedWorkspaces(newExpanded);
  };

  // Task 펼치기/접기
  const toggleTask = async (taskId) => {
    const newExpanded = new Set(expandedTasks);

    if (newExpanded.has(taskId)) {
      newExpanded.delete(taskId);
    } else {
      newExpanded.add(taskId);

      // Todo 목록 로드 (항상 최신 데이터 로드)
      try {
        const todos = await taskAPI.getTodos(taskId);
        setTaskTodos(prev => ({
          ...prev,
          [taskId]: Array.isArray(todos) ? todos : []
        }));
      } catch (error) {
        console.error('Failed to load todos:', error);
      }
    }

    setExpandedTasks(newExpanded);
  };

  const getTodoStatusIcon = (status) => {
    switch (status) {
      case 'CONFIRMED':
        return (
          <svg className="w-3.5 h-3.5 text-green-600" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
          </svg>
        );
      case 'WAITING_REVIEW':
        return (
          <svg className="w-3.5 h-3.5 text-yellow-600" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z" clipRule="evenodd" />
          </svg>
        );
      default:
        return (
          <svg className="w-3.5 h-3.5 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm0-2a6 6 0 100-12 6 6 0 000 12z" clipRule="evenodd" />
          </svg>
        );
    }
  };

  if (collapsed) {
    return (
      <div className="w-16 border-r border-gray-200 bg-gray-50 flex flex-col items-center py-4">
        <button
          onClick={onToggle}
          className="p-2 hover:bg-gray-200 rounded"
        >
          →
        </button>
      </div>
    );
  }

  return (
    <div className="w-64 border-r border-gray-200 bg-white flex flex-col overflow-hidden">
      {/* Header */}
      <div className="px-4 py-3 border-b border-gray-200 flex items-center justify-between">
        <h2 className="font-semibold text-sm text-gray-700">워크스페이스</h2>
        <button
          onClick={onToggle}
          className="p-1.5 hover:bg-gray-100 rounded text-gray-500 transition-colors"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
        </button>
      </div>

      {/* Navigation Tree */}
      <div className="flex-1 overflow-y-auto py-2">
        {workspaces.length === 0 ? (
          <div className="px-4 py-8 text-center text-sm text-gray-400">
            워크스페이스가 없습니다
          </div>
        ) : (
          workspaces.map((workspace) => (
            <div key={workspace.id} className="mb-0.5">
              {/* Workspace */}
              <div className="flex items-center px-2">
                <button
                  onClick={() => toggleWorkspace(workspace.id)}
                  className="p-1 hover:bg-gray-100 rounded transition-colors flex-shrink-0"
                >
                  <svg
                    className={`w-3 h-3 text-gray-500 transition-transform ${
                      expandedWorkspaces.has(workspace.id) ? 'rotate-90' : ''
                    }`}
                    fill="currentColor"
                    viewBox="0 0 20 20"
                  >
                    <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
                  </svg>
                </button>
                <button
                  onClick={() => navigate(`/workspace/${workspace.id}`)}
                  className={`flex-1 text-left px-2 py-1.5 rounded text-sm transition-colors flex items-center gap-2 ${
                    location.pathname === `/workspace/${workspace.id}`
                      ? 'bg-gray-100 text-gray-900 font-medium'
                      : 'text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  <svg className="w-4 h-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" />
                  </svg>
                  <span className="truncate">{workspace.workspaceName}</span>
                </button>
              </div>

              {/* Tasks */}
              {expandedWorkspaces.has(workspace.id) && workspaceTasks[workspace.id] && (
                <div className="ml-4 mt-0.5 border-l-2 border-gray-200 pl-2">
                  {workspaceTasks[workspace.id].map((task) => (
                    <div key={task.id} className="mb-0.5">
                      <div className="flex items-center px-2">
                        <button
                          onClick={() => toggleTask(task.id)}
                          className="p-1 hover:bg-gray-100 rounded transition-colors flex-shrink-0"
                        >
                          <svg
                            className={`w-3 h-3 text-gray-500 transition-transform ${
                              expandedTasks.has(task.id) ? 'rotate-90' : ''
                            }`}
                            fill="currentColor"
                            viewBox="0 0 20 20"
                          >
                            <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
                          </svg>
                        </button>
                        <button
                          onClick={() => navigate(`/task/${task.id}`)}
                          className={`flex-1 text-left px-2 py-1 rounded text-sm transition-colors flex items-center gap-2 ${
                            location.pathname === `/task/${task.id}`
                              ? 'bg-gray-100 text-gray-900 font-medium'
                              : 'text-gray-600 hover:bg-gray-50'
                          }`}
                        >
                          <svg className="w-4 h-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                          </svg>
                          <span className="truncate">{task.title}</span>
                        </button>
                      </div>

                      {/* Todos */}
                      {expandedTasks.has(task.id) && taskTodos[task.id] && (
                        <div className="ml-4 mt-0.5 border-l-2 border-gray-200 pl-2">
                          {taskTodos[task.id].map((todo) => (
                            <button
                              key={todo.id}
                              onClick={() => navigate(`/todo/${todo.id}`)}
                              className="w-full text-left px-2 py-1 rounded text-xs hover:bg-gray-50 text-gray-600 flex items-center gap-2 transition-colors"
                            >
                              {getTodoStatusIcon(todo.status)}
                              <span className="truncate">{todo.title}</span>
                            </button>
                          ))}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}
