import { cn } from '@/utils/cn';

export function PageHeader({ title, description, children, className }) {
  return (
    <div className={cn('flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6', className)}>
      <div>
        <h1 className="text-2xl font-bold text-surface-900 tracking-tight">{title}</h1>
        {description && <p className="mt-1 text-sm text-surface-500">{description}</p>}
      </div>
      {children && <div className="flex items-center gap-3 shrink-0">{children}</div>}
    </div>
  );
}
