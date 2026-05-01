import { useState, useEffect, useRef } from 'react';

/**
 * Debounce a value by a given delay.
 * @param {*} value - The value to debounce
 * @param {number} delay - Debounce delay in ms (default 400)
 */
export function useDebounce(value, delay = 400) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const handler = setTimeout(() => setDebouncedValue(value), delay);
    return () => clearTimeout(handler);
  }, [value, delay]);

  return debouncedValue;
}
