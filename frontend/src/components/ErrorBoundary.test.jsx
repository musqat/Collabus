import { render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import ErrorBoundary from './ErrorBoundary';

function Boom() {
  throw new Error('렌더 실패');
}

function Fine() {
  return <p>정상 화면</p>;
}

beforeEach(() => {
  // React 가 경계에서 잡은 예외를 콘솔로 다시 내보낸다. 출력만 막는다
  vi.spyOn(console, 'error').mockImplementation(() => {});
});

afterEach(() => {
  vi.restoreAllMocks();
});

describe('ErrorBoundary', () => {
  it('예외가 없으면 자식을 그대로 그린다', () => {
    render(
      <ErrorBoundary>
        <Fine />
      </ErrorBoundary>
    );

    expect(screen.getByText('정상 화면')).toBeDefined();
  });

  it('렌더 중 예외가 나면 안내 화면을 대신 그린다', () => {
    render(
      <ErrorBoundary>
        <Boom />
      </ErrorBoundary>
    );

    expect(screen.getByText('화면을 그리지 못했습니다')).toBeDefined();
    expect(screen.getByRole('button', { name: '다시 시도' })).toBeDefined();
    expect(screen.getByRole('button', { name: '처음으로' })).toBeDefined();
  });

  it('예외를 콘솔에 남긴다', () => {
    render(
      <ErrorBoundary>
        <Boom />
      </ErrorBoundary>
    );

    const logged = console.error.mock.calls
      .some((args) => args.some((a) => String(a).includes('렌더링 중 예외')));
    expect(logged).toBe(true);
  });
});
