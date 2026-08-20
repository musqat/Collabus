import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import AuthShowcase from '../components/Auth/AuthShowcase';

const DEMO_EMAIL = 'user1@test.com';
const DEMO_PASSWORD = 'password';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const { login, isLoading } = useAuth();

  const handleSubmit = (e) => {
    e.preventDefault();
    login({ email, password });
  };

  // 처음 온 사람이 계정을 몰라 되돌아가지 않도록 바로 들어가게 한다
  const handleDemo = () => {
    setEmail(DEMO_EMAIL);
    setPassword(DEMO_PASSWORD);
    login({ email: DEMO_EMAIL, password: DEMO_PASSWORD });
  };

  return (
    <div className="min-h-screen grid lg:grid-cols-[minmax(0,1fr)_minmax(0,480px)] bg-white">
      <AuthShowcase />

      <div className="flex flex-col justify-center px-8 py-12 sm:px-12">
        <div className="w-full max-w-sm mx-auto">
          <span className="lg:hidden text-sm tracking-[0.14em] text-gray-900">COLLABUS</span>

          <h1 className="mt-8 lg:mt-0 text-2xl font-semibold text-gray-900">로그인</h1>
          <p className="mt-1 text-sm text-gray-500">계속하려면 계정 정보를 입력하세요.</p>

          <form onSubmit={handleSubmit} className="mt-8">
            <label htmlFor="email" className="block text-sm text-gray-600 mb-1.5">
              이메일
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-3.5 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent"
              placeholder="name@company.com"
              required
            />

            <label htmlFor="password" className="block text-sm text-gray-600 mb-1.5 mt-4">
              비밀번호
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-3.5 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent"
              placeholder="••••••••"
              required
            />

            <button
              type="submit"
              disabled={isLoading}
              className="w-full mt-6 bg-brand-600 text-white py-2.5 rounded-lg text-sm font-medium hover:bg-brand-700 transition disabled:bg-gray-300 disabled:cursor-not-allowed"
            >
              {isLoading ? '로그인 중...' : '로그인'}
            </button>
          </form>

          <button
            type="button"
            onClick={handleDemo}
            disabled={isLoading}
            className="w-full mt-2 border border-gray-300 text-gray-700 py-2.5 rounded-lg text-sm hover:bg-gray-50 transition disabled:opacity-50"
          >
            데모 계정으로 둘러보기
          </button>

          <p className="mt-8 text-sm text-gray-500">
            계정이 없으신가요?{' '}
            <Link to="/register" className="text-brand-700 hover:text-brand-800 font-medium">
              회원가입
            </Link>
          </p>
        </div>
      </div>

    </div>
  );
}
