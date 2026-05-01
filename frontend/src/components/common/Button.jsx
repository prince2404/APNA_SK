import { cn } from '@/utils/cn';
import { Loader2 } from 'lucide-react';

const variants = {
  primary: 'bg-primary-600 text-white hover:bg-primary-700 active:bg-primary-800 shadow-sm',
  secondary: 'bg-surface-100 text-surface-700 hover:bg-surface-200 active:bg-surface-300 border border-surface-300',
  danger: 'bg-danger-600 text-white hover:bg-danger-700 active:bg-danger-700',
  ghost: 'text-surface-600 hover:bg-surface-100 active:bg-surface-200',
  outline: 'border border-primary-600 text-primary-600 hover:bg-primary-50 active:bg-primary-100',
};

const sizes = {
  sm: 'px-3 py-1.5 text-sm gap-1.5',
  md: 'px-4 py-2 text-sm gap-2',
  lg: 'px-5 py-2.5 text-base gap-2',
};

export function Button({
  children,
  variant = 'primary',
  size = 'md',
  loading = false,
  disabled = false,
  className,
  ...props
}) {
  return (
    <button
      className={cn(
        'inline-flex items-center justify-center font-medium rounded-lg transition-all duration-150 focus-ring disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer',
        variants[variant],
        sizes[size],
        className
      )}
      disabled={disabled || loading}
      {...props}
    >
      {loading && <Loader2 className="w-4 h-4 animate-spin" />}
      {children}
    </button>
  );
}
