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
  VERIFICATION_QUEUE: '/users/verification-queue',
  PERMISSION_REQUESTS: '/users/permission-requests',

  // Geography
  STATES: '/geography/states',
  DISTRICTS: '/geography/districts',
  BLOCKS: '/geography/blocks',
  STORES: '/geography/stores',

  // Sessions
  SESSIONS: '/sessions',

  // Patients
  PATIENTS: '/patients',
  PATIENTS_BULK_UPLOAD: '/patients/bulk-upload',
  HOSPITALS: '/hospitals',

  // Health Cards
  HEALTH_CARDS: '/health-cards',

  // Products
  PRODUCTS: '/products',

  // Inventory
  INVENTORY: '/inventory',

  // Billing
  BILLING: '/billing',
  SCHEMES: '/schemes',
  INVOICES: '/billing/invoices',

  // Commission
  COMMISSIONS: '/commissions',

  // Messaging
  MESSAGES: '/messages',
  MESSAGE_TEMPLATES: '/messages/templates',
  MESSAGE_HISTORY: '/messages/history',

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
