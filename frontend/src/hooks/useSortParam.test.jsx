import { act, renderHook } from '@testing-library/react';
import { MemoryRouter, useLocation } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import useSortParam from './useSortParam';

const DEFAULT_SORT = 'createdAt,desc';

function renderSortParam(initialUrl, defaultSort = DEFAULT_SORT, key) {
  return renderHook(
    () => ({ param: useSortParam(defaultSort, key), location: useLocation() }),
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

describe('useSortParam', () => {
  it('쿼리가 없으면 기본 정렬이다', () => {
    const { result } = renderSortParam('/tasks');
    expect(result.current.param[0]).toBe(DEFAULT_SORT);
  });

  it('쿼리가 있으면 그 값을 쓴다', () => {
    const { result } = renderSortParam('/tasks?sort=dueDate,asc');
    expect(result.current.param[0]).toBe('dueDate,asc');
  });

  it('정렬을 바꾸면 URL 에 싣는다', () => {
    const { result } = renderSortParam('/tasks');

    act(() => result.current.param[1]('title,asc'));

    expect(result.current.location.search).toBe('?sort=title%2Casc');
    expect(result.current.param[0]).toBe('title,asc');
  });

  it('기본값으로 되돌리면 쿼리를 지운다', () => {
    const { result } = renderSortParam('/tasks?sort=dueDate,asc');

    act(() => result.current.param[1](DEFAULT_SORT));

    expect(result.current.location.search).toBe('');
    expect(result.current.param[0]).toBe(DEFAULT_SORT);
  });

  it('빈 값을 주면 쿼리를 지운다', () => {
    const { result } = renderSortParam('/tasks?sort=dueDate,asc');

    act(() => result.current.param[1](null));

    expect(result.current.location.search).toBe('');
  });

  it('정렬이 바뀌면 페이지를 첫 장으로 되돌린다', () => {
    const { result } = renderSortParam('/tasks?page=5&sort=dueDate,asc');

    act(() => result.current.param[1]('title,asc'));

    const params = new URLSearchParams(result.current.location.search);
    expect(params.has('page')).toBe(false);
    expect(params.get('sort')).toBe('title,asc');
  });

  it('기본값으로 되돌릴 때도 페이지를 되돌린다', () => {
    const { result } = renderSortParam('/tasks?page=5&sort=dueDate,asc');

    act(() => result.current.param[1](DEFAULT_SORT));

    expect(result.current.location.search).toBe('');
  });

  it('다른 쿼리는 건드리지 않는다', () => {
    const { result } = renderSortParam('/tasks?keyword=검토');

    act(() => result.current.param[1]('title,asc'));

    const params = new URLSearchParams(result.current.location.search);
    expect(params.get('keyword')).toBe('검토');
  });

  it('key 를 주면 그 쿼리 이름을 쓴다', () => {
    const { result } = renderSortParam('/todo/1?todoSort=dueDate,asc', DEFAULT_SORT, 'todoSort');
    expect(result.current.param[0]).toBe('dueDate,asc');

    act(() => result.current.param[1]('title,asc'));

    const params = new URLSearchParams(result.current.location.search);
    expect(params.get('todoSort')).toBe('title,asc');
  });
});
