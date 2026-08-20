import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useAuthStore } from './store/authStore';
import Layout from './components/Layout';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import WorkspaceDetail from './pages/WorkspaceDetail';
import TaskDetail from './pages/TaskDetail';
import TodoDetail from './pages/TodoDetail';
import ProfilePage from './pages/ProfilePage';
import Invitations from './pages/Invitations';
import ToastContainer from './components/Toast/ToastContainer';

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
              <ProtectedRoute>
                <Layout>
                  <Dashboard />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/workspace/:workspaceId"
            element={
              <ProtectedRoute>
                <Layout>
                  <WorkspaceDetail />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/task/:taskId"
            element={
              <ProtectedRoute>
                <Layout>
                  <TaskDetail />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/todo/:todoId"
            element={
              <ProtectedRoute>
                <Layout>
                  <TodoDetail />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <Layout>
                  <ProfilePage />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/invitations"
            element={
              <ProtectedRoute>
                <Layout>
                  <Invitations />
                </Layout>
              </ProtectedRoute>
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
