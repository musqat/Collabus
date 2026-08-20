import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import {
  formatDate,
  formatDueDate,
  formatRelativeTime,
  getDaysUntil,
  getDueDateColor,
} from './dateUtils';

// 경계가 오늘 기준으로 갈리므로 시계를 고정한다
const NOW = new Date('2026-08-20T12:00:00');

beforeEach(() => {
  vi.useFakeTimers();
  vi.setSystemTime(NOW);
});

afterEach(() => {
  vi.useRealTimers();
});

describe('formatDate', () => {
  it('빈 값은 빈 문자열이 된다', () => {
    expect(formatDate(null)).toBe('');
    expect(formatDate(undefined)).toBe('');
    expect(formatDate('')).toBe('');
  });

  it('날짜를 한국어 표기로 바꾼다', () => {
    expect(formatDate('2026-08-20')).toBe('2026년 8월 20일');
  });

  it('읽을 수 없는 값은 Invalid Date 대신 빈 문자열이 된다', () => {
    expect(formatDate('어제')).toBe('');
    expect(formatDate('2026-13-45')).toBe('');
  });
});

describe('getDaysUntil', () => {
  it('빈 값은 null 이 된다', () => {
    expect(getDaysUntil(null)).toBeNull();
  });

  it('오늘은 0 이다', () => {
    expect(getDaysUntil('2026-08-20')).toBe(0);
  });

  it('내일은 1, 어제는 -1 이다', () => {
    expect(getDaysUntil('2026-08-21')).toBe(1);
    expect(getDaysUntil('2026-08-19')).toBe(-1);
  });

  it('시각이 달라도 날짜만 본다', () => {
    expect(getDaysUntil('2026-08-20T23:59:59')).toBe(0);
    expect(getDaysUntil('2026-08-21T00:00:01')).toBe(1);
  });

  it('달을 넘겨도 센다', () => {
    expect(getDaysUntil('2026-09-01')).toBe(12);
  });
});

describe('getDueDateColor', () => {
  it('빈 값은 기본 색이다', () => {
    expect(getDueDateColor(null)).toBe('text-gray-600');
  });

  it('지난 마감은 빨강이다', () => {
    expect(getDueDateColor('2026-08-19')).toBe('text-red-600 font-semibold');
  });

  it('3일 이하로 남으면 주황이다', () => {
    expect(getDueDateColor('2026-08-20')).toBe('text-orange-600 font-semibold');
    expect(getDueDateColor('2026-08-23')).toBe('text-orange-600 font-semibold');
  });

  it('4일부터 7일까지는 노랑이다', () => {
    expect(getDueDateColor('2026-08-24')).toBe('text-yellow-600');
    expect(getDueDateColor('2026-08-27')).toBe('text-yellow-600');
  });

  it('8일 이상 남으면 기본 색이다', () => {
    expect(getDueDateColor('2026-08-28')).toBe('text-gray-600');
  });
});

describe('formatDueDate', () => {
  it('빈 값은 빈 문자열이 된다', () => {
    expect(formatDueDate(null)).toBe('');
  });

  it('오늘과 내일은 따로 말한다', () => {
    expect(formatDueDate('2026-08-20')).toBe('오늘 마감');
    expect(formatDueDate('2026-08-21')).toBe('내일 마감');
  });

  it('이틀 뒤부터는 남은 날짜를 센다', () => {
    expect(formatDueDate('2026-08-22')).toBe('2일 남음');
  });

  it('지난 마감은 지난 날짜를 센다', () => {
    expect(formatDueDate('2026-08-19')).toBe('1일 지남');
    expect(formatDueDate('2026-08-10')).toBe('10일 지남');
  });
});

describe('formatRelativeTime', () => {
  it('빈 값은 빈 문자열이 된다', () => {
    expect(formatRelativeTime(null)).toBe('');
  });

  it('1분 미만은 방금 전이다', () => {
    expect(formatRelativeTime('2026-08-20T11:59:01')).toBe('방금 전');
  });

  it('1분부터 분 단위로 센다', () => {
    expect(formatRelativeTime('2026-08-20T11:59:00')).toBe('1분 전');
    expect(formatRelativeTime('2026-08-20T11:01:00')).toBe('59분 전');
  });

  it('1시간부터 시간 단위로 센다', () => {
    expect(formatRelativeTime('2026-08-20T11:00:00')).toBe('1시간 전');
    expect(formatRelativeTime('2026-08-19T13:00:00')).toBe('23시간 전');
  });

  it('하루부터 일 단위로 센다', () => {
    expect(formatRelativeTime('2026-08-19T12:00:00')).toBe('1일 전');
    expect(formatRelativeTime('2026-08-13T12:00:01')).toBe('6일 전');
  });

  it('일주일이 지나면 날짜로 보여준다', () => {
    expect(formatRelativeTime('2026-08-13T12:00:00')).toBe('2026년 8월 13일');
  });

  it('시계 차이만큼 앞선 값은 방금 전으로 둔다', () => {
    expect(formatRelativeTime('2026-08-20T12:00:30')).toBe('방금 전');
    expect(formatRelativeTime('2026-08-20T12:01:00')).toBe('방금 전');
  });

  it('그보다 먼 미래는 날짜로 보여준다', () => {
    expect(formatRelativeTime('2026-08-21T12:00:00')).toBe('2026년 8월 21일');
  });

  it('읽을 수 없는 값은 빈 문자열이 된다', () => {
    expect(formatRelativeTime('어제')).toBe('');
  });
});
