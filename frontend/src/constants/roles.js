/** Role name constants matching backend RoleConstants.java */
export const ROLES = {
  SUPER_ADMIN: 'SUPER_ADMIN',
  SYSTEM_ADMIN: 'SYSTEM_ADMIN',
  STATE_ADMIN: 'STATE_ADMIN',
  DISTRICT_ADMIN: 'DISTRICT_ADMIN',
  BLOCK_ADMIN: 'BLOCK_ADMIN',
  RECEPTIONIST: 'RECEPTIONIST',
  VOLUNTEER: 'VOLUNTEER',
  PHARMACIST: 'PHARMACIST',
};

/** Role display names */
export const ROLE_DISPLAY_NAMES = {
  [ROLES.SUPER_ADMIN]: 'Super Admin',
  [ROLES.SYSTEM_ADMIN]: 'System Admin',
  [ROLES.STATE_ADMIN]: 'State Admin',
  [ROLES.DISTRICT_ADMIN]: 'District Admin',
  [ROLES.BLOCK_ADMIN]: 'Block Admin',
  [ROLES.RECEPTIONIST]: 'Receptionist',
  [ROLES.VOLUNTEER]: 'Volunteer',
  [ROLES.PHARMACIST]: 'Pharmacist',
};

/** Hierarchy levels (lower = higher authority) */
export const ROLE_HIERARCHY = {
  [ROLES.SUPER_ADMIN]: 1,
  [ROLES.SYSTEM_ADMIN]: 2,
  [ROLES.STATE_ADMIN]: 3,
  [ROLES.DISTRICT_ADMIN]: 4,
  [ROLES.BLOCK_ADMIN]: 5,
  [ROLES.PHARMACIST]: 6,
  [ROLES.RECEPTIONIST]: 7,
  [ROLES.VOLUNTEER]: 8,
};

/** Platform-wide roles that see all geography */
export const PLATFORM_ROLES = [ROLES.SUPER_ADMIN, ROLES.SYSTEM_ADMIN];

/** Check if a role has platform-wide scope */
export const hasPlatformScope = (roleName) => PLATFORM_ROLES.includes(roleName);
