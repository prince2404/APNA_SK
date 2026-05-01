import { create } from 'zustand';

export const useNotificationStore = create((set) => ({
  toasts: [],

  addToast: (toast) => {
    const id = Date.now() + Math.random();
    const newToast = { id, duration: 4000, ...toast };
    set((s) => ({ toasts: [...s.toasts, newToast] }));

    setTimeout(() => {
      set((s) => ({ toasts: s.toasts.filter((t) => t.id !== id) }));
    }, newToast.duration);
  },

  removeToast: (id) => {
    set((s) => ({ toasts: s.toasts.filter((t) => t.id !== id) }));
  },

  success: (message) => {
    const { addToast } = useNotificationStore.getState();
    addToast({ type: 'success', message });
  },

  error: (message) => {
    const { addToast } = useNotificationStore.getState();
    addToast({ type: 'error', message, duration: 6000 });
  },

  warning: (message) => {
    const { addToast } = useNotificationStore.getState();
    addToast({ type: 'warning', message });
  },

  info: (message) => {
    const { addToast } = useNotificationStore.getState();
    addToast({ type: 'info', message });
  },
}));

/** Shorthand toast helper */
export const toast = {
  success: (msg) => useNotificationStore.getState().success(msg),
  error: (msg) => useNotificationStore.getState().error(msg),
  warning: (msg) => useNotificationStore.getState().warning(msg),
  info: (msg) => useNotificationStore.getState().info(msg),
};
