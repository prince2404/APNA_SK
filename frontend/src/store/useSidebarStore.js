import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useSidebarStore = create(
  persist(
    (set) => ({
      isCollapsed: false,
      isMobileOpen: false,

      toggle: () => set((s) => ({ isCollapsed: !s.isCollapsed })),
      collapse: () => set({ isCollapsed: true }),
      expand: () => set({ isCollapsed: false }),
      toggleMobile: () => set((s) => ({ isMobileOpen: !s.isMobileOpen })),
      closeMobile: () => set({ isMobileOpen: false }),
    }),
    {
      name: 'ask-sidebar',
      partialize: (state) => ({ isCollapsed: state.isCollapsed }),
    }
  )
);
