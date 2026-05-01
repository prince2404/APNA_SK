import { ServerCrash } from 'lucide-react';
import { Button } from '@/components/common/Button';

export default function ServerErrorPage() {
  return (
    <div className="min-h-screen flex items-center justify-center p-6 bg-surface-50">
      <div className="text-center max-w-md">
        <div className="w-20 h-20 rounded-3xl bg-warning-100 flex items-center justify-center mx-auto mb-6">
          <ServerCrash className="w-10 h-10 text-warning-600" />
        </div>
        <h1 className="text-6xl font-bold text-surface-300 mb-2">500</h1>
        <h2 className="text-xl font-bold text-surface-900 mb-2">Server Error</h2>
        <p className="text-surface-500 mb-8">Something went wrong on our end. Please try again later.</p>
        <Button onClick={() => window.location.reload()}>Try Again</Button>
      </div>
    </div>
  );
}
