import { cn } from '@/utils/cn';

export function Badge({ children, variant = 'default', className }) {
  const variants = {
    default: 'bg-surface-100 text-surface-700',
    primary: 'bg-primary-100 text-primary-700',
    accent: 'bg-accent-100 text-accent-700',
    danger: 'bg-danger-100 text-danger-700',
    warning: 'bg-warning-100 text-warning-700',
    success: 'bg-success-50 text-success-600',
  };

  return (
    <span className={cn(
      'inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium',
      variants[variant], className
    )}>
      {children}
    </span>
  );
}
