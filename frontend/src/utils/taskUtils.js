export function calculateTodoStats(todos) {
  if (!Array.isArray(todos)) {
    return {
      total: 0,
      inProgress: 0,
      waitingReview: 0,
      confirmed: 0,
    };
  }

  return {
    total: todos.length,
    inProgress: todos.filter(t => t.status === 'IN_PROGRESS').length,
    waitingReview: todos.filter(t => t.status === 'WAITING_REVIEW').length,
    confirmed: todos.filter(t => t.status === 'CONFIRMED').length,
  };
}

export function getTaskProgress(todos) {
  const stats = calculateTodoStats(todos);
  if (stats.total === 0) return 0;
  return Math.round((stats.confirmed / stats.total) * 100);
}
