import { describe, expect, it } from 'vitest';
import { errorMessage } from './errorMessage';

const FALLBACK = '요청에 실패했습니다';

describe('errorMessage', () => {
  it('GlobalExceptionHandler 가 낸 message 를 쓴다', () => {
    const error = { response: { data: { message: 'Task 를 볼 권한이 없습니다.' } } };

    expect(errorMessage(error, FALLBACK)).toBe('Task 를 볼 권한이 없습니다.');
  });

  it('컨트롤러가 낸 statusMsg 도 쓴다', () => {
    const error = { response: { data: { statusMsg: '해당 이메일이 존재하지 않습니다.' } } };

    expect(errorMessage(error, FALLBACK)).toBe('해당 이메일이 존재하지 않습니다.');
  });

  it('둘 다 있으면 message 를 먼저 쓴다', () => {
    const error = { response: { data: { message: '앞', statusMsg: '뒤' } } };

    expect(errorMessage(error, FALLBACK)).toBe('앞');
  });

  it('본문이 비어 있으면 기본 문구를 쓴다', () => {
    expect(errorMessage({ response: { data: {} } }, FALLBACK)).toBe(FALLBACK);
    expect(errorMessage({ response: {} }, FALLBACK)).toBe(FALLBACK);
  });

  it('응답 자체가 없어도 터지지 않는다', () => {
    expect(errorMessage(new Error('Network Error'), FALLBACK)).toBe(FALLBACK);
    expect(errorMessage(undefined, FALLBACK)).toBe(FALLBACK);
    expect(errorMessage(null, FALLBACK)).toBe(FALLBACK);
  });

  it('빈 문자열은 기본 문구로 넘긴다', () => {
    const error = { response: { data: { message: '', statusMsg: '' } } };

    expect(errorMessage(error, FALLBACK)).toBe(FALLBACK);
  });
});
