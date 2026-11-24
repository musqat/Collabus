import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useAuthStore } from './store/authStore';
import Login from './pages/Login';
import Register from './pages/Register';
import ToastContainer from './components/Toast/ToastContainer';
import { PageLoading } from './components/LoadingState';

// 로그인 뒤 화면은 처음 들어갈 때 로드
const Layout = lazy(() => import('./components/Layout'));
const Dashboard = lazy(() => import('./pages/Dashboard'));
const WorkspaceDetail = lazy(() => import('./pages/WorkspaceDetail'));
const TaskDetail = lazy(() => import('./pages/TaskDetail'));
const TodoDetail = lazy(() => import('./pages/TodoDetail'));
const ProfilePage = lazy(() => import('./pages/ProfilePage'));
const Invitations = lazy(() => import('./pages/Invitations'));

// React Query Client 생성
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

// Protected Route 컴포넌트
function ProtectedRoute({ children }) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated());

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

// Public Route 컴포넌트 (로그인된 상태에서는 대시보드로)
function PublicRoute({ children }) {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated());

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}

// 레이아웃과 화면을 따로 기다려 화면을 옮길 때 레이아웃은 그대로 유지
function ProtectedPage({ children }) {
  return (
    <ProtectedRoute>
      <Suspense fallback={<PageLoading />}>
        <Layout>
          <Suspense fallback={<PageLoading />}>{children}</Suspense>
        </Layout>
      </Suspense>
    </ProtectedRoute>
  );
}

function RootRedirect() {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated());

  return <Navigate to={isAuthenticated ? '/dashboard' : '/login'} replace />;
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          {/* Public Routes */}
          <Route
            path="/login"
            element={
              <PublicRoute>
                <Login />
              </PublicRoute>
            }
          />
          <Route
            path="/register"
            element={
              <PublicRoute>
                <Register />
              </PublicRoute>
            }
          />

          {/* Protected Routes with Layout */}
          <Route
            path="/dashboard"
            element={
              <ProtectedPage>
                <Dashboard />
              </ProtectedPage>
            }
          />
          <Route
            path="/workspace/:workspaceId"
            element={
              <ProtectedPage>
                <WorkspaceDetail />
              </ProtectedPage>
            }
          />
          <Route
            path="/task/:taskId"
            element={
              <ProtectedPage>
                <TaskDetail />
              </ProtectedPage>
            }
          />
          <Route
            path="/todo/:todoId"
            element={
              <ProtectedPage>
                <TodoDetail />
              </ProtectedPage>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedPage>
                <ProfilePage />
              </ProtectedPage>
            }
          />
          <Route
            path="/invitations"
            element={
              <ProtectedPage>
                <Invitations />
              </ProtectedPage>
            }
          />

          {/* Default Route */}
          {/* 로그인 여부에 따라 한 번만 보낸다. 대시보드를 거쳤다 튕기지 않는다 */}
          <Route path="/" element={<RootRedirect />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
        <ToastContainer />
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
