import { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/useAuthStore';
import { authApi } from '@/api/authApi';
import { toast } from '@/store/useNotificationStore';
import { ROUTES } from '@/constants/routePaths';

/**
 * Custom auth hook wrapping the auth store with navigation-aware actions.
 */
export function useAuth() {
  const navigate = useNavigate();
  const {
    accessToken,
    refreshToken,
    user,
    isAuthenticated,
    requiresTwoFactor,
    requiresPasswordChange,
    twoFactorChallengeToken,
    twoFactorEmail,
    setLoginResponse,
    clearTwoFactor,
    clearPasswordChange,
    logout: storeLogout,
  } = useAuthStore();

  const login = useCallback(async (email, password) => {
    const response = await authApi.login({ email, password });
    const data = response.data.data;
    setLoginResponse(data);

    if (data.requiresTwoFactor) {
      navigate(ROUTES.VERIFY_OTP);
    } else if (data.requiresPasswordChange) {
      navigate(ROUTES.CHANGE_PASSWORD);
    } else {
      navigate(ROUTES.DASHBOARD);
      toast.success('Welcome back!');
    }
    return data;
  }, [navigate, setLoginResponse]);

  const verifyOtp = useCallback(async (otp) => {
    const response = await authApi.verifyOtp({
      email: twoFactorEmail,
      otp,
      challengeToken: twoFactorChallengeToken,
    });
    const data = response.data.data;
    setLoginResponse(data);

    if (data.requiresPasswordChange) {
      navigate(ROUTES.CHANGE_PASSWORD);
    } else {
      navigate(ROUTES.DASHBOARD);
      toast.success('Welcome back!');
    }
    return data;
  }, [navigate, setLoginResponse, twoFactorEmail, twoFactorChallengeToken]);

  const resendOtp = useCallback(async () => {
    await authApi.resendOtp({
      email: twoFactorEmail,
      challengeToken: twoFactorChallengeToken,
    });
    toast.success('OTP resent to your email');
  }, [twoFactorEmail, twoFactorChallengeToken]);

  const changePassword = useCallback(async (currentPassword, newPassword, confirmPassword) => {
    await authApi.changePassword({ currentPassword, newPassword, confirmPassword });
    clearPasswordChange();
    navigate(ROUTES.DASHBOARD);
    toast.success('Password changed successfully');
  }, [navigate, clearPasswordChange]);

  const logout = useCallback(async () => {
    try {
      if (refreshToken) {
        await authApi.logout({ refreshToken });
      }
    } catch {
      // Ignore errors during logout
    } finally {
      storeLogout();
      navigate(ROUTES.LOGIN);
    }
  }, [refreshToken, storeLogout, navigate]);

  return {
    user,
    isAuthenticated,
    requiresTwoFactor,
    requiresPasswordChange,
    accessToken,
    login,
    verifyOtp,
    resendOtp,
    changePassword,
    logout,
  };
}
