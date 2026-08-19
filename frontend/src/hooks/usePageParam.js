import { useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';

/**
 * 페이지 번호를 URL 쿼리로 관리한다.
 *
 * state 로 들고 있으면 새로고침·뒤로가기에서 첫 페이지로 돌아가고 링크 공유도 안 된다.
 * URL 은 사람이 읽으므로 1부터, 서버·컴포넌트에는 0부터 넘긴다.
 *
 * 한 화면에 목록이 둘 이상이면 key 를 다르게 제공
 */
export default function usePageParam(key = 'page') {
  const [searchParams, setSearchParams] = useSearchParams();

  const parsed = Number(searchParams.get(key));
  const page = Number.isInteger(parsed) && parsed > 0 ? parsed - 1 : 0;

  const setPage = useCallback(
    (next) => {
      setSearchParams(
        (current) => {
          // 다른 쿼리는 건드리지 않는다
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
