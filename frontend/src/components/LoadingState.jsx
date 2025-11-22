export function LoadingSpinner({ message = "불러오는 중..." }) {
  return (
    <div className="flex flex-col items-center justify-center py-12">
      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      <p className="mt-4 text-gray-600">{message}</p>
    </div>
  );
}

export function SkeletonCard() {
  return (
    <div className="border rounded-lg p-4 animate-pulse">
      <div className="h-4 bg-gray-200 rounded w-3/4 mb-4"></div>
      <div className="h-3 bg-gray-200 rounded w-1/2 mb-2"></div>
      <div className="h-2 bg-gray-200 rounded w-full mb-3"></div>
      <div className="flex gap-2">
        <div className="h-6 bg-gray-200 rounded w-16"></div>
        <div className="h-6 bg-gray-200 rounded w-16"></div>
        <div className="h-6 bg-gray-200 rounded w-16"></div>
      </div>
    </div>
  );
}

export function ErrorState({ error, retry }) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-center">
      <div className="bg-red-100 rounded-full p-4 mb-4">
        <svg className="w-16 h-16 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
      </div>
      <h3 className="text-lg font-semibold text-gray-900 mb-2">
        문제가 발생했습니다
      </h3>
      <p className="text-gray-600 mb-4 max-w-md">
        {error?.message || error?.response?.data?.statusMsg || "데이터를 불러오는 중 오류가 발생했습니다."}
      </p>
      {retry && (
        <button
          onClick={retry}
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
        >
          다시 시도
        </button>
      )}
    </div>
  );
}

export function EmptyState({ icon: Icon, title, description, action }) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-center">
      {Icon && <Icon className="w-16 h-16 text-gray-400 mb-4" />}
      <h3 className="text-lg font-semibold text-gray-900 mb-2">{title}</h3>
      {description && <p className="text-gray-600 mb-4 max-w-md">{description}</p>}
      {action}
    </div>
  );
}
