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

// 화면 코드를 받는 동안의 자리 표시
export function PageLoading() {
  return (
    <div className="min-h-[50vh] flex items-center justify-center">
      <div className="text-xl text-gray-600">로딩 중...</div>
    </div>
  );
}
