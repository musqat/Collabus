import { act, renderHook } from '@testing-library/react';
import { MemoryRouter, useLocation } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import usePageParam from './usePageParam';

// 훅이 바꾼 URL 을 같이 읽으려고 location 을 함께 반환한다
function renderPageParam(initialUrl, key) {
  return renderHook(
    () => ({ param: usePageParam(key), location: useLocation() }),
    {
      wrapper: ({ children }) => (
        <MemoryRouter
          initialEntries={[initialUrl]}
          future={{ v7_startTransition: true, v7_relativeSplatPath: true }}
        >
          {children}
        </MemoryRouter>
      ),
    }
  );
}

describe('usePageParam', () => {
  it('쿼리가 없으면 첫 페이지다', () => {
    const { result } = renderPageParam('/tasks');
    expect(result.current.param[0]).toBe(0);
  });

  it('URL 은 1부터, 반환값은 0부터 센다', () => {
    const { result } = renderPageParam('/tasks?page=3');
    expect(result.current.param[0]).toBe(2);
  });

  it('숫자가 아니거나 1 미만이면 첫 페이지로 본다', () => {
    expect(renderPageParam('/tasks?page=0').result.current.param[0]).toBe(0);
    expect(renderPageParam('/tasks?page=-2').result.current.param[0]).toBe(0);
    expect(renderPageParam('/tasks?page=abc').result.current.param[0]).toBe(0);
    expect(renderPageParam('/tasks?page=1.5').result.current.param[0]).toBe(0);
  });

  it('페이지를 바꾸면 URL 에 1 을 더해 싣는다', () => {
    const { result } = renderPageParam('/tasks');

    act(() => result.current.param[1](2));

    expect(result.current.location.search).toBe('?page=3');
    expect(result.current.param[0]).toBe(2);
  });

  it('첫 페이지로 돌아가면 쿼리를 지운다', () => {
    const { result } = renderPageParam('/tasks?page=4');

    act(() => result.current.param[1](0));

    expect(result.current.location.search).toBe('');
    expect(result.current.param[0]).toBe(0);
  });

  it('다른 쿼리는 건드리지 않는다', () => {
    const { result } = renderPageParam('/tasks?sort=dueDate,asc&keyword=검토');

    act(() => result.current.param[1](1));

    const params = new URLSearchParams(result.current.location.search);
    expect(params.get('sort')).toBe('dueDate,asc');
    expect(params.get('keyword')).toBe('검토');
    expect(params.get('page')).toBe('2');
  });

  it('key 를 주면 그 쿼리 이름을 쓴다', () => {
    const { result } = renderPageParam('/todo/1?commentPage=2', 'commentPage');
    expect(result.current.param[0]).toBe(1);

    act(() => result.current.param[1](4));

    expect(result.current.location.search).toBe('?commentPage=5');
  });

  it('key 가 다르면 서로의 쿼리를 건드리지 않는다', () => {
    const { result } = renderPageParam('/todo/1?page=2&commentPage=3', 'commentPage');

    act(() => result.current.param[1](0));

    const params = new URLSearchParams(result.current.location.search);
    expect(params.get('page')).toBe('2');
    expect(params.has('commentPage')).toBe(false);
  });
});
