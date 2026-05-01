import { ShieldX } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/common/Button';
import { ROUTES } from '@/constants/routePaths';

export default function ForbiddenPage() {
  const navigate = useNavigate();
  return (
    <div className="min-h-screen flex items-center justify-center p-6 bg-surface-50">
      <div className="text-center max-w-md">
        <div className="w-20 h-20 rounded-3xl bg-danger-100 flex items-center justify-center mx-auto mb-6">
          <ShieldX className="w-10 h-10 text-danger-600" />
        </div>
        <h1 className="text-6xl font-bold text-surface-300 mb-2">403</h1>
        <h2 className="text-xl font-bold text-surface-900 mb-2">Access Denied</h2>
        <p className="text-surface-500 mb-8">You don't have permission to access this resource. Contact your administrator if you need access.</p>
        <Button onClick={() => navigate(ROUTES.DASHBOARD)}>Go to Dashboard</Button>
      </div>
    </div>
  );
}
