/**
 * 서버가 내려준 실패 사유를 꺼낸다.
 * GlobalExceptionHandler 는 message 로, 컨트롤러가 직접 만든 응답은 statusMsg 로 내려준다.
 * 둘 다 없으면 fallback 을 쓴다.
 */
export function errorMessage(error, fallback) {
  const body = error?.response?.data;
  return body?.message || body?.statusMsg || fallback;
}

// 다른 사람이 먼저 고쳐 저장이 거절된 응답인지
export function isEditConflict(error) {
  return error?.response?.status === 409;
}
