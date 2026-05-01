/** Frontend route path constants */
export const ROUTES = {
  // Auth
  LOGIN: '/login',
  VERIFY_OTP: '/verify-otp',
  CHANGE_PASSWORD: '/change-password',

  // Dashboard
  DASHBOARD: '/',

  // Users
  USERS: '/users',
  USER_CREATE: '/users/create',
  USER_DETAIL: '/users/:id',
  USER_EDIT: '/users/:id/edit',

  // Geography
  STATES: '/geography/states',
  DISTRICTS: '/geography/districts',
  BLOCKS: '/geography/blocks',
  STORES: '/geography/stores',

  // Sessions
  SESSIONS: '/sessions',

  // Patients
  PATIENTS: '/patients',

  // Health Cards
  HEALTH_CARDS: '/health-cards',

  // Products
  PRODUCTS: '/products',

  // Inventory
  INVENTORY: '/inventory',

  // Billing
  BILLING: '/billing',

  // Commission
  COMMISSIONS: '/commissions',

  // Notifications
  NOTIFICATIONS: '/notifications',

  // Reports
  REPORTS: '/reports',

  // Profile
  PROFILE: '/profile',

  // Settings
  SETTINGS: '/settings',

  // Error pages
  FORBIDDEN: '/403',
  NOT_FOUND: '/404',
  SERVER_ERROR: '/500',
};
