import { useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';

/**
 * 페이지 번호를 URL 쿼리로 읽고 쓴다.
 * URL 에는 1부터 쓰고, 반환값은 0부터 센다.
 * key 로 쿼리 이름을 바꾼다. (예: 'commentPage')
 */
export default function usePageParam(key = 'page') {
  const [searchParams, setSearchParams] = useSearchParams();

  const parsed = Number(searchParams.get(key));
  const page = Number.isInteger(parsed) && parsed > 0 ? parsed - 1 : 0;

  const setPage = useCallback(
    (next) => {
      setSearchParams(
        (current) => {
          const updated = new URLSearchParams(current);
          if (next <= 0) {
            updated.delete(key);
          } else {
            updated.set(key, String(next + 1));
          }
          return updated;
        },
        { replace: true }
      );
    },
    [key, setSearchParams]
  );

  return [page, setPage];
}
