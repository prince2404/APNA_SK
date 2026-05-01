/** Application-wide constants */
export const APP = {
  NAME: import.meta.env.VITE_APP_NAME || 'Apna Swasthya Kendra',
  SHORT_NAME: import.meta.env.VITE_APP_SHORT_NAME || 'ASK',
  DEFAULT_PAGE_SIZE: 20,
  MAX_PAGE_SIZE: 100,
  DEBOUNCE_MS: 400,
  TOAST_DURATION_MS: 4000,
};

/** Status badge colors */
export const STATUS_COLORS = {
  ACTIVE: { bg: 'bg-emerald-100', text: 'text-emerald-700', dot: 'bg-emerald-500' },
  INACTIVE: { bg: 'bg-slate-100', text: 'text-slate-600', dot: 'bg-slate-400' },
  LOCKED: { bg: 'bg-rose-100', text: 'text-rose-700', dot: 'bg-rose-500' },
  PENDING: { bg: 'bg-amber-100', text: 'text-amber-700', dot: 'bg-amber-500' },
  VERIFIED: { bg: 'bg-emerald-100', text: 'text-emerald-700', dot: 'bg-emerald-500' },
  REJECTED: { bg: 'bg-rose-100', text: 'text-rose-700', dot: 'bg-rose-500' },
  APPROVED: { bg: 'bg-emerald-100', text: 'text-emerald-700', dot: 'bg-emerald-500' },
};

/** Gender options */
export const GENDERS = [
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' },
  { value: 'OTHER', label: 'Other' },
];
