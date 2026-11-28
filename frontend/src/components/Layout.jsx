import { useEffect, useState } from 'react';
import ErrorBoundary from './ErrorBoundary';
import Sidebar from './Sidebar';
import NotificationBell from './Notification/NotificationBell';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useAuthStore } from '../store/authStore';

export default function Layout({ children }) {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);
  const [mobileNavOpen, setMobileNavOpen] = useState(false);
  const { logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const currentUser = useAuthStore((state) => state.user);

  // 화면을 옮기면 좁은 화면의 사이드바 서랍을 닫음
  useEffect(() => {
    setMobileNavOpen(false);
  }, [location.pathname]);

  return (
    <div className="h-screen flex overflow-hidden bg-white">
      {/* 넓은 화면의 사이드바 */}
      <div className="hidden md:flex">
        <Sidebar collapsed={sidebarCollapsed} onToggle={() => setSidebarCollapsed(!sidebarCollapsed)} />
      </div>

      {/* 좁은 화면의 사이드바 서랍 */}
      {mobileNavOpen && (
        <div className="fixed inset-0 z-40 flex md:hidden">
          <div className="absolute inset-0 bg-black/30" onClick={() => setMobileNavOpen(false)} />
          <div className="relative flex h-full">
            <Sidebar collapsed={false} onToggle={() => setMobileNavOpen(false)} />
          </div>
        </div>
      )}

      {/* Main Content */}
      <div className="flex-1 min-w-0 flex flex-col overflow-hidden">
        {/* Top Header */}
        <header className="border-b border-gray-200 bg-white">
          <div className="px-4 md:px-6 py-3 flex items-center justify-between gap-2">
            <div className="flex items-center gap-2 md:gap-4">
              <button
                type="button"
                onClick={() => setMobileNavOpen(true)}
                aria-label="메뉴 열기"
                className="md:hidden p-2 -ml-2 rounded-lg text-gray-600 hover:bg-gray-100"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                </svg>
              </button>
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
                  aria-label="사용자 메뉴"
                  aria-expanded={showUserMenu}
                  className="flex items-center gap-3 px-2 sm:px-3 py-2 rounded-lg hover:bg-gray-100 transition"
                >
                  {/* User Avatar */}
                  <div className="w-9 h-9 rounded-full bg-gradient-to-br from-brand-600 to-brand-700 flex items-center justify-center text-white font-bold text-sm shadow-sm">
                    {currentUser?.nickname?.charAt(0)?.toUpperCase() || currentUser?.email?.charAt(0)?.toUpperCase() || 'U'}
                  </div>
                  {/* User Info */}
                  <div className="hidden sm:block text-left">
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
