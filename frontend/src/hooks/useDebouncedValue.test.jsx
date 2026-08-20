import { act, renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import useDebouncedValue from './useDebouncedValue';

beforeEach(() => {
  vi.useFakeTimers();
});

afterEach(() => {
  vi.useRealTimers();
});

describe('useDebouncedValue', () => {
  it('처음에는 받은 값을 그대로 낸다', () => {
    const { result } = renderHook(() => useDebouncedValue('검토'));
    expect(result.current).toBe('검토');
  });

  it('delay 가 지나기 전에는 이전 값을 유지한다', () => {
    const { result, rerender } = renderHook(({ value }) => useDebouncedValue(value), {
      initialProps: { value: '검' },
    });

    rerender({ value: '검토' });
    act(() => vi.advanceTimersByTime(299));

    expect(result.current).toBe('검');
  });

  it('delay 가 지나면 새 값을 낸다', () => {
    const { result, rerender } = renderHook(({ value }) => useDebouncedValue(value), {
      initialProps: { value: '검' },
    });

    rerender({ value: '검토' });
    act(() => vi.advanceTimersByTime(300));

    expect(result.current).toBe('검토');
  });

  it('타이머가 도는 중에 값이 또 바뀌면 처음부터 다시 센다', () => {
    const { result, rerender } = renderHook(({ value }) => useDebouncedValue(value), {
      initialProps: { value: '검' },
    });

    rerender({ value: '검토' });
    act(() => vi.advanceTimersByTime(200));
    rerender({ value: '검토중' });
    act(() => vi.advanceTimersByTime(200));

    // 마지막 변경 이후 200ms 밖에 지나지 않았다
    expect(result.current).toBe('검');

    act(() => vi.advanceTimersByTime(100));
    expect(result.current).toBe('검토중');
  });

  it('delay 를 바꿔 넘길 수 있다', () => {
    const { result, rerender } = renderHook(({ value }) => useDebouncedValue(value, 1000), {
      initialProps: { value: '검' },
    });

    rerender({ value: '검토' });
    act(() => vi.advanceTimersByTime(999));
    expect(result.current).toBe('검');

    act(() => vi.advanceTimersByTime(1));
    expect(result.current).toBe('검토');
  });

  it('빈 문자열로 지워도 delay 뒤에 반영된다', () => {
    const { result, rerender } = renderHook(({ value }) => useDebouncedValue(value), {
      initialProps: { value: '검토' },
    });

    rerender({ value: '' });
    act(() => vi.advanceTimersByTime(300));

    expect(result.current).toBe('');
  });
});
