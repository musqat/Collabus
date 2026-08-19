import { useEffect, useState } from 'react';

/**
 * 값이 delay 동안 바뀌지 않으면 그 값을 넘긴다.
 */
export default function useDebouncedValue(value, delay = 300) {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(timer);
  }, [value, delay]);

  return debounced;
}
