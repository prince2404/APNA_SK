import { create } from 'zustand';
import { persist } from 'zustand/middleware';

/**
 * Auth store — persists tokens and user profile in localStorage.
 * Used by axiosInstance interceptors and auth hooks.
 */
export const useAuthStore = create(
  persist(
    (set, get) => ({
      // State
      accessToken: null,
      refreshToken: null,
      user: null,
      isAuthenticated: false,
      requiresTwoFactor: false,
      requiresPasswordChange: false,
      twoFactorChallengeToken: null,
      twoFactorEmail: null,

      // Actions
      setLoginResponse: (response) => {
        if (response.requiresTwoFactor) {
          set({
            requiresTwoFactor: true,
            twoFactorChallengeToken: response.twoFactorChallengeToken,
            twoFactorEmail: response.user?.email,
            user: response.user,
          });
          return;
        }

        if (response.requiresPasswordChange) {
          set({
            accessToken: response.accessToken,
            requiresPasswordChange: true,
            requiresTwoFactor: false,
            twoFactorChallengeToken: null,
            user: response.user,
          });
          return;
        }

        set({
          accessToken: response.accessToken,
          refreshToken: response.refreshToken,
          user: response.user,
          isAuthenticated: true,
          requiresTwoFactor: false,
          requiresPasswordChange: false,
          twoFactorChallengeToken: null,
          twoFactorEmail: null,
        });
      },

      setTokens: (accessToken, refreshToken) => {
        set({ accessToken, refreshToken });
      },

      clearTwoFactor: () => {
        set({
          requiresTwoFactor: false,
          twoFactorChallengeToken: null,
          twoFactorEmail: null,
        });
      },

      clearPasswordChange: () => {
        set({ requiresPasswordChange: false });
      },

      logout: () => {
        set({
          accessToken: null,
          refreshToken: null,
          user: null,
          isAuthenticated: false,
          requiresTwoFactor: false,
          requiresPasswordChange: false,
          twoFactorChallengeToken: null,
          twoFactorEmail: null,
        });
      },
    }),
    {
      name: 'ask-auth',
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);
