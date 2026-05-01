import axiosInstance from './axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';

export const authApi = {
  login: (data) => axiosInstance.post(API_PATHS.AUTH_LOGIN, data),
  verifyOtp: (data) => axiosInstance.post(API_PATHS.AUTH_VERIFY_OTP, data),
  resendOtp: (data) => axiosInstance.post(API_PATHS.AUTH_RESEND_OTP, data),
  refresh: (data) => axiosInstance.post(API_PATHS.AUTH_REFRESH, data),
  logout: (data) => axiosInstance.post(API_PATHS.AUTH_LOGOUT, data),
  changePassword: (data) => axiosInstance.post(API_PATHS.AUTH_CHANGE_PASSWORD, data),
};
