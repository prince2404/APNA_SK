import { useEffect } from 'react';
import { X, CheckCircle, AlertCircle, AlertTriangle, Info } from 'lucide-react';
import { useNotificationStore } from '@/store/useNotificationStore';
import { cn } from '@/utils/cn';

const icons = {
  success: CheckCircle,
  error: AlertCircle,
  warning: AlertTriangle,
  info: Info,
};

const styles = {
  success: 'bg-emerald-50 border-emerald-200 text-emerald-800',
  error: 'bg-rose-50 border-rose-200 text-rose-800',
  warning: 'bg-amber-50 border-amber-200 text-amber-800',
  info: 'bg-blue-50 border-blue-200 text-blue-800',
};

const iconStyles = {
  success: 'text-emerald-500',
  error: 'text-rose-500',
  warning: 'text-amber-500',
  info: 'text-blue-500',
};

export function ToastContainer() {
  const toasts = useNotificationStore((s) => s.toasts);
  const removeToast = useNotificationStore((s) => s.removeToast);

  if (!toasts.length) return null;

  return (
    <div className="fixed top-4 right-4 z-[100] space-y-2 max-w-sm w-full">
      {toasts.map((toast) => {
        const Icon = icons[toast.type] || Info;
        return (
          <div
            key={toast.id}
            className={cn(
              'flex items-start gap-3 p-4 rounded-xl border shadow-lg animate-slide-in',
              styles[toast.type]
            )}
          >
            <Icon className={cn('w-5 h-5 mt-0.5 shrink-0', iconStyles[toast.type])} />
            <p className="text-sm font-medium flex-1">{toast.message}</p>
            <button onClick={() => removeToast(toast.id)} className="p-0.5 hover:opacity-70 cursor-pointer">
              <X className="w-4 h-4" />
            </button>
          </div>
        );
      })}
    </div>
  );
}
