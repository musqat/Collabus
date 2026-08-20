/**
 * 로그인·회원가입 화면 왼쪽에 붙는 제품 소개.
 * 워크스페이스에서 Todo 로 좁혀지는 계층을 보여준다.
 * 좁은 화면에서는 숨긴다.
 */
export default function AuthShowcase() {
  return (
    <div className="hidden lg:flex flex-col justify-center bg-gray-50 border-r border-gray-200 px-12 py-12">
      <div className="w-full max-w-sm">
        <span className="text-sm tracking-[0.14em] text-gray-900">COLLABUS</span>

        <p className="mt-8 text-xl font-medium text-gray-900 leading-snug">
          팀의 일을 세 단계로 정리합니다
        </p>
        <p className="mt-2 text-sm text-gray-500 leading-relaxed">
          큰 흐름에서 시작해 각자 할 일까지 내려갑니다.
        </p>

        <ol className="mt-8">
          <li className="flex items-center gap-3 bg-white border border-gray-200 rounded-lg px-3.5 py-3">
            <UsersIcon />
            <span className="text-sm font-medium text-gray-900">Workspace</span>
          </li>

          <Arrow />

          <li className="flex items-center gap-3 bg-white border border-gray-200 rounded-lg px-3.5 py-3">
            <FolderIcon />
            <span className="text-sm font-medium text-gray-900">Task</span>
          </li>

          <Arrow />

          <li className="flex items-center gap-3 bg-brand-50 border border-brand-100 rounded-lg px-3.5 py-3">
            <CheckIcon />
            <span className="text-sm font-medium text-brand-800">Todo</span>
          </li>
        </ol>
      </div>
    </div>
  );
}

function Arrow() {
  return (
    <li aria-hidden="true" className="pl-[22px] py-1.5">
      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="text-gray-300">
        <path d="M6 9l6 6 6-6" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
    </li>
  );
}

function UsersIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="text-gray-500 shrink-0" aria-hidden="true">
      <circle cx="9" cy="7" r="3" />
      <path d="M3 20c0-3 2.7-5 6-5s6 2 6 5" strokeLinecap="round" />
      <path d="M16 4.5a3 3 0 010 5.8M18 20c0-2.2-.9-3.6-2-4.5" strokeLinecap="round" />
    </svg>
  );
}

function FolderIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="text-gray-500 shrink-0" aria-hidden="true">
      <path d="M3 7a2 2 0 012-2h4l2 2h8a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2V7z" strokeLinejoin="round" />
    </svg>
  );
}

function CheckIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="text-brand-700 shrink-0" aria-hidden="true">
      <circle cx="12" cy="12" r="9" />
      <path d="M8.5 12.5l2.5 2.5 4.5-5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}
