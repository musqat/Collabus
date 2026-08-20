import { useState } from 'react';
import { showToast } from '../store/toastStore';
import { Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import AuthShowcase from '../components/Auth/AuthShowcase';

const FIELD_CLASS =
  'w-full px-3.5 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent';

export default function Register() {
  const [email, setEmail] = useState('');
  const [nickname, setNickname] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const { register, isLoading } = useAuth();

  const handleSubmit = (e) => {
    e.preventDefault();

    if (password !== confirmPassword) {
      showToast.warning('비밀번호가 일치하지 않습니다.');
      return;
    }

    register({ email, nickname, password });
  };

  return (
    <div className="min-h-screen grid lg:grid-cols-[minmax(0,1fr)_minmax(0,480px)] bg-white">
      <AuthShowcase />

      <div className="flex flex-col justify-center px-8 py-12 sm:px-12">
        <div className="w-full max-w-sm mx-auto">
          <span className="lg:hidden text-sm tracking-[0.14em] text-gray-900">COLLABUS</span>

          <h1 className="mt-8 lg:mt-0 text-2xl font-semibold text-gray-900">회원가입</h1>
          <p className="mt-1 text-sm text-gray-500">계정을 만들면 워크스페이스를 시작할 수 있습니다.</p>

          <form onSubmit={handleSubmit} className="mt-8">
            <label htmlFor="email" className="block text-sm text-gray-600 mb-1.5">
              이메일
            </label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className={FIELD_CLASS}
              placeholder="name@company.com"
              required
            />

            <label htmlFor="nickname" className="block text-sm text-gray-600 mb-1.5 mt-4">
              닉네임
            </label>
            <input
              id="nickname"
              type="text"
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
              className={FIELD_CLASS}
              placeholder="닉네임"
              required
            />
            <p className="text-xs text-gray-400 mt-1.5">
              자동으로 #태그가 붙습니다 (예: {nickname || '닉네임'}#1234)
            </p>

            <label htmlFor="password" className="block text-sm text-gray-600 mb-1.5 mt-4">
              비밀번호
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className={FIELD_CLASS}
              placeholder="••••••••"
              required
            />
            <p className="text-xs text-gray-400 mt-1.5">8자 이상, 영문과 숫자를 포함합니다.</p>

            <label htmlFor="confirmPassword" className="block text-sm text-gray-600 mb-1.5 mt-4">
              비밀번호 확인
            </label>
            <input
              id="confirmPassword"
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className={FIELD_CLASS}
              placeholder="••••••••"
              required
            />

            <button
              type="submit"
              disabled={isLoading}
              className="w-full mt-6 bg-brand-600 text-white py-2.5 rounded-lg text-sm font-medium hover:bg-brand-700 transition disabled:bg-gray-300 disabled:cursor-not-allowed"
            >
              {isLoading ? '가입 중...' : '회원가입'}
            </button>
          </form>

          <p className="mt-8 text-sm text-gray-500">
            이미 계정이 있으신가요?{' '}
            <Link to="/login" className="text-brand-700 hover:text-brand-800 font-medium">
              로그인
            </Link>
          </p>
        </div>
      </div>

    </div>
  );
}
