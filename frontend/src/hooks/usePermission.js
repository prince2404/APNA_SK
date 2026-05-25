import { useMemo, useCallback } from 'react';
import { useAuthStore } from '@/store/useAuthStore';
import { ROLES } from '@/constants/roles';

/**
 * Hook to check if current user has a specific permission or role.
 */
export function usePermission() {
  const user = useAuthStore((s) => s.user);

  const permissions = useMemo(() => new Set(user?.permissions || []), [user?.permissions]);
  const roleName = user?.roleName;

  /** Check if user has a specific permission code like "USERS:VIEW" */
  const hasPermission = useCallback((permCode) => {
    if (roleName === ROLES.SUPER_ADMIN) return true;
    return permissions.has(permCode);
  }, [roleName, permissions]);

  /** Check if user has any of the given permissions */
  const hasAnyPermission = useCallback((...permCodes) => {
    if (roleName === ROLES.SUPER_ADMIN) return true;
    return permCodes.some((code) => permissions.has(code));
  }, [roleName, permissions]);

  /** Check if user has a specific role */
  const hasRole = useCallback((role) => roleName === role, [roleName]);

  /** Check if user has any of the given roles */
  const hasAnyRole = useCallback((...roles) => roles.includes(roleName), [roleName]);

  /** Check if user is Super Admin */
  const isSuperAdmin = roleName === ROLES.SUPER_ADMIN;

  /** Check if user has platform-wide scope */
  const hasPlatformScope = roleName === ROLES.SUPER_ADMIN || roleName === ROLES.SYSTEM_ADMIN;

  return {
    permissions,
    roleName,
    hasPermission,
    hasAnyPermission,
    hasRole,
    hasAnyRole,
    isSuperAdmin,
    hasPlatformScope,
  };
}
