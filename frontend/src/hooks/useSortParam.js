import { useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';

/**
 * 정렬 조건을 URL 쿼리로 읽고 쓴다.
 */
export default function useSortParam(defaultSort, key = 'sort') {
  const [searchParams, setSearchParams] = useSearchParams();
  const sort = searchParams.get(key) ?? defaultSort;

  const setSort = useCallback(
    (next) => {
      setSearchParams(
        (current) => {
          const updated = new URLSearchParams(current);
          if (!next || next === defaultSort) {
            updated.delete(key);
          } else {
            updated.set(key, next);
          }
          // 정렬이 바뀌면 순서가 달라지므로 첫 페이지부터 본다
          updated.delete('page');
          return updated;
        },
        { replace: true }
      );
    },
    [defaultSort, key, setSearchParams]
  );

  return [sort, setSort];
}
