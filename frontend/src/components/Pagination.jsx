/**
 * 번호 방식 페이지네이션.
 */
const WINDOW = 5;

export default function Pagination({ page, totalPages, onChange }) {
  if (totalPages <= 1) {
    return null;
  }

  // 현재 페이지를 가운데 두고 WINDOW 개를 보여준다. 양 끝에서는 안쪽으로 민다
  const start = Math.max(0, Math.min(page - Math.floor(WINDOW / 2), totalPages - WINDOW));
  const numbers = Array.from(
    { length: Math.min(WINDOW, totalPages) },
    (_, i) => start + i
  );

  const base = 'min-w-9 h-9 px-3 rounded-lg border text-sm font-medium transition';
  const idle = 'border-gray-300 text-gray-700 hover:bg-gray-50';
  const active = 'border-blue-600 bg-blue-600 text-white';
  const disabled = 'disabled:opacity-40 disabled:cursor-not-allowed disabled:hover:bg-white';

  return (
    <nav className="flex items-center justify-center gap-1 pt-4" aria-label="페이지">
      <button
        onClick={() => onChange(page - 1)}
        disabled={page === 0}
        className={`${base} ${idle} ${disabled}`}
      >
        이전
      </button>

      {numbers.map((n) => (
        <button
          key={n}
          onClick={() => onChange(n)}
          aria-current={n === page ? 'page' : undefined}
          className={`${base} ${n === page ? active : idle}`}
        >
          {n + 1}
        </button>
      ))}

      <button
        onClick={() => onChange(page + 1)}
        disabled={page >= totalPages - 1}
        className={`${base} ${idle} ${disabled}`}
      >
        다음
      </button>
    </nav>
  );
}
