import { forwardRef } from 'react';
import { cn } from '@/utils/cn';

export const Input = forwardRef(({ label, error, className, id, ...props }, ref) => {
  const inputId = id || label?.toLowerCase().replace(/\s+/g, '-');
  return (
    <div className="space-y-1.5">
      {label && (
        <label htmlFor={inputId} className="block text-sm font-medium text-surface-700">
          {label}
        </label>
      )}
      <input
        ref={ref}
        id={inputId}
        className={cn(
          'w-full px-3.5 py-2.5 text-sm rounded-lg border transition-colors duration-150',
          'bg-white text-surface-900 placeholder:text-surface-400',
          'focus:outline-none focus:ring-2 focus:ring-primary-500/40 focus:border-primary-500',
          error ? 'border-danger-500 focus:ring-danger-500/40 focus:border-danger-500' : 'border-surface-300',
          className
        )}
        {...props}
      />
      {error && <p className="text-xs text-danger-600 mt-1">{error}</p>}
    </div>
  );
});

Input.displayName = 'Input';
