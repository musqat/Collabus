export function formatDate(dateString) {
  if (!dateString) return '';
  return new Date(dateString).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

export function formatDateTime(dateString) {
  if (!dateString) return '';
  return new Date(dateString).toLocaleString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function formatRelativeTime(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString);
  const now = new Date();
  const seconds = Math.floor((now - date) / 1000);

  if (seconds < 60) return '방금 전';
  if (seconds < 3600) return `${Math.floor(seconds / 60)}분 전`;
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}시간 전`;
  if (seconds < 604800) return `${Math.floor(seconds / 86400)}일 전`;
  return formatDate(dateString);
}

export function getDaysUntil(dateString) {
  if (!dateString) return null;
  const target = new Date(dateString);
  const now = new Date();
  now.setHours(0, 0, 0, 0);
  target.setHours(0, 0, 0, 0);
  return Math.ceil((target - now) / (1000 * 60 * 60 * 24));
}

export function getDueDateColor(dueDate) {
  if (!dueDate) return 'text-gray-600';
  const daysLeft = getDaysUntil(dueDate);
  if (daysLeft < 0) return 'text-red-600 font-semibold'; // 지남
  if (daysLeft <= 3) return 'text-orange-600 font-semibold'; // 임박
  if (daysLeft <= 7) return 'text-yellow-600'; // 주의
  return 'text-gray-600'; // 여유
}

export function formatDueDate(dueDate) {
  if (!dueDate) return '';
  const daysLeft = getDaysUntil(dueDate);

  if (daysLeft < 0) return `${Math.abs(daysLeft)}일 지남`;
  if (daysLeft === 0) return '오늘 마감';
  if (daysLeft === 1) return '내일 마감';
  return `${daysLeft}일 남음`;
}
