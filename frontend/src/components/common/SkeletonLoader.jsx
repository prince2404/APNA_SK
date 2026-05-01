import { cn } from '@/utils/cn';

export function SkeletonLoader({ className, rows = 1 }) {
  return (
    <div className="space-y-3 animate-pulse-soft">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className={cn('h-4 bg-surface-200 rounded-md', className)} />
      ))}
    </div>
  );
}

export function TableSkeleton({ columns = 4, rows = 5 }) {
  return (
    <div className="space-y-3 animate-pulse-soft">
      <div className="grid gap-4" style={{ gridTemplateColumns: `repeat(${columns}, 1fr)` }}>
        {Array.from({ length: columns }).map((_, i) => (
          <div key={i} className="h-4 bg-surface-200 rounded-md" />
        ))}
      </div>
      {Array.from({ length: rows }).map((_, r) => (
        <div key={r} className="grid gap-4" style={{ gridTemplateColumns: `repeat(${columns}, 1fr)` }}>
          {Array.from({ length: columns }).map((_, c) => (
            <div key={c} className="h-4 bg-surface-100 rounded-md" />
          ))}
        </div>
      ))}
    </div>
  );
}
