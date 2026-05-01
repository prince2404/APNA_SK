import { Navigate } from 'react-router-dom';
import { usePermission } from '@/hooks/usePermission';
import { ROUTES } from '@/constants/routePaths';

/**
 * Guards a route by requiring a specific permission or role.
 * Renders 403 page if the user lacks access.
 */
export function RoleGuard({ children, permission, roles }) {
  const { hasPermission, hasAnyRole, isSuperAdmin } = usePermission();

  if (isSuperAdmin) return children;
  if (permission && hasPermission(permission)) return children;
  if (roles && hasAnyRole(...roles)) return children;

  return <Navigate to={ROUTES.FORBIDDEN} replace />;
}
