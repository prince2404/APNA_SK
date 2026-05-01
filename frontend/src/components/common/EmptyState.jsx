import { Inbox } from 'lucide-react';
import { cn } from '@/utils/cn';

export function EmptyState({ icon: Icon = Inbox, title = 'No data found', description, action, className }) {
  return (
    <div className={cn('flex flex-col items-center justify-center py-16 text-center', className)}>
      <div className="w-14 h-14 rounded-2xl bg-surface-100 flex items-center justify-center mb-4">
        <Icon className="w-7 h-7 text-surface-400" />
      </div>
      <h3 className="text-base font-semibold text-surface-700 mb-1">{title}</h3>
      {description && <p className="text-sm text-surface-500 max-w-sm">{description}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}
