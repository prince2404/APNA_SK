/** API path constants mirroring the backend ApiPaths.java */
const V1 = '/v1';

export const API_PATHS = {
  // Auth
  AUTH_LOGIN: `${V1}/auth/login`,
  AUTH_REFRESH: `${V1}/auth/refresh`,
  AUTH_LOGOUT: `${V1}/auth/logout`,
  AUTH_VERIFY_OTP: `${V1}/auth/verify-otp`,
  AUTH_RESEND_OTP: `${V1}/auth/resend-otp`,
  AUTH_CHANGE_PASSWORD: `${V1}/auth/change-password`,

  // Users
  USERS: `${V1}/users`,

  // Permissions
  PERMISSIONS: `${V1}/permissions`,
  PERMISSION_REQUESTS: `${V1}/permission-requests`,

  // Geography
  STATES: `${V1}/states`,
  DISTRICTS: `${V1}/districts`,
  BLOCKS: `${V1}/blocks`,
  STORES: `${V1}/stores`,

  // Sessions
  SESSIONS: `${V1}/sessions`,

  // Patients
  PATIENTS: `${V1}/patients`,
  HOSPITALS: `${V1}/hospitals`,

  // Health Cards
  HEALTH_CARDS: `${V1}/health-cards`,

  // Products
  PRODUCT_CATEGORIES: `${V1}/product-categories`,
  PRODUCTS: `${V1}/products`,

  // Inventory
  STOCK_CENTRAL: `${V1}/stock/central`,
  TRANSFER_ORDERS: `${V1}/transfer-orders`,
  STOCK_STORE: `${V1}/stock/store`,
  STOCK_REQUESTS: `${V1}/stock-requests`,
  STOCK_ADJUSTMENTS: `${V1}/stock/adjustments`,

  // Billing
  BILLS: `${V1}/bills`,
  SCHEMES: `${V1}/schemes`,

  // Commission
  COMMISSIONS: `${V1}/commissions`,

  // Notifications
  NOTIFICATIONS: `${V1}/notifications`,

  // Messages & Templates
  MESSAGES: `${V1}/messages`,
  MESSAGE_TEMPLATES: `${V1}/message-templates`,

  // Reports
  REPORTS: `${V1}/reports`,

  // Profile
  PROFILE: `${V1}/profile`,

  // Dashboard
  DASHBOARD: `${V1}/dashboard`,

  // System Config
  SYSTEM_CONFIG: `${V1}/system-config`,
};
