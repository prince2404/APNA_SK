import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/store/useAuthStore';
import { ROUTES } from '@/constants/routePaths';

/**
 * Wraps routes that require authentication.
 * Redirects to login if not authenticated, to change-password if forced.
 */
export function ProtectedRoute({ children }) {
  const { isAuthenticated, requiresPasswordChange, requiresTwoFactor } = useAuthStore();
  const location = useLocation();

  if (!isAuthenticated) {
    if (requiresTwoFactor) return <Navigate to={ROUTES.VERIFY_OTP} replace />;
    if (requiresPasswordChange) return <Navigate to={ROUTES.CHANGE_PASSWORD} replace />;
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />;
  }

  return children;
}
