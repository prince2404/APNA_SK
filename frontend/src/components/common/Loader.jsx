import { Loader2 } from 'lucide-react';

export function Loader({ text = 'Loading...', fullScreen = false }) {
  const content = (
    <div className="flex flex-col items-center justify-center gap-3">
      <Loader2 className="w-8 h-8 text-primary-600 animate-spin" />
      <p className="text-sm text-surface-500 font-medium">{text}</p>
    </div>
  );

  if (fullScreen) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-white/80 backdrop-blur-sm">
        {content}
      </div>
    );
  }

  return <div className="flex items-center justify-center py-12">{content}</div>;
}
