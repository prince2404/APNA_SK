import { cn } from '@/utils/cn';
import { STATUS_COLORS } from '@/constants/appConstants';

export function StatusBadge({ status, className }) {
  const colors = STATUS_COLORS[status] || STATUS_COLORS.INACTIVE;
  return (
    <span className={cn(
      'inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium',
      colors.bg, colors.text, className
    )}>
      <span className={cn('w-1.5 h-1.5 rounded-full', colors.dot)} />
      {status}
    </span>
  );
}
