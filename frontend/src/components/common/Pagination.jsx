import { ChevronLeft, ChevronRight } from 'lucide-react';
import { cn } from '@/utils/cn';

export function Pagination({ page, totalPages, totalElements, onPageChange }) {
  if (totalPages <= 1) return null;

  const pages = [];
  const maxVisible = 5;
  let start = Math.max(0, page - Math.floor(maxVisible / 2));
  let end = Math.min(totalPages, start + maxVisible);
  if (end - start < maxVisible) start = Math.max(0, end - maxVisible);

  for (let i = start; i < end; i++) pages.push(i);

  return (
    <div className="flex items-center justify-between px-1 py-3">
      <p className="text-sm text-surface-500">
        Showing page <span className="font-medium text-surface-700">{page + 1}</span> of{' '}
        <span className="font-medium text-surface-700">{totalPages}</span>
        {totalElements != null && (
          <> · <span className="font-medium text-surface-700">{totalElements}</span> total</>
        )}
      </p>
      <div className="flex items-center gap-1">
        <button
          onClick={() => onPageChange(page - 1)}
          disabled={page === 0}
          className="p-1.5 rounded-lg text-surface-500 hover:bg-surface-100 disabled:opacity-40 disabled:cursor-not-allowed transition-colors cursor-pointer"
        >
          <ChevronLeft className="w-4 h-4" />
        </button>
        {pages.map((p) => (
          <button
            key={p}
            onClick={() => onPageChange(p)}
            className={cn(
              'min-w-[36px] h-9 px-2 rounded-lg text-sm font-medium transition-colors cursor-pointer',
              p === page
                ? 'bg-primary-600 text-white shadow-sm'
                : 'text-surface-600 hover:bg-surface-100'
            )}
          >
            {p + 1}
          </button>
        ))}
        <button
          onClick={() => onPageChange(page + 1)}
          disabled={page >= totalPages - 1}
          className="p-1.5 rounded-lg text-surface-500 hover:bg-surface-100 disabled:opacity-40 disabled:cursor-not-allowed transition-colors cursor-pointer"
        >
          <ChevronRight className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}
