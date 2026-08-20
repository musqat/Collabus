import { useState } from 'react';
import ErrorBoundary from './ErrorBoundary';
import Sidebar from './Sidebar';
import NotificationBell from './Notification/NotificationBell';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useAuthStore } from '../store/authStore';

export default function Layout({ children }) {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const { logout } = useAuth();
  const navigate = useNavigate();
  const currentUser = useAuthStore((state) => state.user);

  return (
    <div className="h-screen flex overflow-hidden bg-white">
      {/* Sidebar */}
      <Sidebar collapsed={sidebarCollapsed} onToggle={() => setSidebarCollapsed(!sidebarCollapsed)} />

      {/* Main Content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Top Header */}
        <header className="border-b border-gray-200 bg-white">
          <div className="px-6 py-3 flex items-center justify-between">
            <div className="flex items-center gap-4">
              <button
                onClick={() => navigate('/dashboard')}
                className="text-xl font-bold text-gray-900 hover:text-gray-700"
              >
                Collabus
              </button>
            </div>
            <div className="flex items-center gap-3">
              <NotificationBell />

              {/* User Menu */}
              <div className="relative">
                <button
                  onClick={() => setShowUserMenu(!showUserMenu)}
                  className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-100 transition"
                >
                  {/* User Avatar */}
                  <div className="w-9 h-9 rounded-full bg-gradient-to-br from-brand-600 to-brand-700 flex items-center justify-center text-white font-bold text-sm shadow-sm">
                    {currentUser?.nickname?.charAt(0)?.toUpperCase() || currentUser?.email?.charAt(0)?.toUpperCase() || 'U'}
                  </div>
                  {/* User Info */}
                  <div className="text-left">
                    <div className="text-sm font-semibold text-gray-900">
                      {currentUser?.nickname || 'User'}
                      <span className="ml-1 text-xs font-normal text-gray-500">
                        #{currentUser?.displayName?.split('#')[1] || '0000'}
                      </span>
                    </div>
                    <div className="text-xs text-gray-500">
                      {currentUser?.email}
                    </div>
                  </div>
                  {/* Dropdown Icon */}
                  <svg
                    className={`w-4 h-4 text-gray-500 transition-transform ${showUserMenu ? 'rotate-180' : ''}`}
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                  </svg>
                </button>

                {/* Dropdown Menu */}
                {showUserMenu && (
                  <>
                    {/* Backdrop to close menu */}
                    <div
                      className="fixed inset-0 z-10"
                      onClick={() => setShowUserMenu(false)}
                    />
                    <div className="absolute right-0 mt-2 w-64 bg-white rounded-lg shadow-lg border border-gray-200 py-1 z-20">
                      <div className="px-4 py-3 border-b border-gray-200">
                        <div className="text-sm font-semibold text-gray-900">
                          {currentUser?.displayName || `${currentUser?.nickname}#0000`}
                        </div>
                        <div className="text-xs text-gray-500 mt-1">
                          {currentUser?.email}
                        </div>
                      </div>
                      <button
                        onClick={() => {
                          navigate('/profile');
                          setShowUserMenu(false);
                        }}
                        className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition"
                      >
                        프로필 설정
                      </button>
                      <button
                        onClick={() => {
                          navigate('/invitations');
                          setShowUserMenu(false);
                        }}
                        className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 transition"
                      >
                        받은 초대
                      </button>
                      <button
                        onClick={() => {
                          logout();
                          setShowUserMenu(false);
                        }}
                        className="w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition"
                      >
                        로그아웃
                      </button>
                    </div>
                  </>
                )}
              </div>
            </div>
          </div>
        </header>

        {/* Content Area */}
        <main className="flex-1 overflow-auto">
          {/* 본문이 터져도 사이드바와 헤더는 남는다 */}
          <ErrorBoundary>{children}</ErrorBoundary>
        </main>
      </div>
    </div>
  );
}
