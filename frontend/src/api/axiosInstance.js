import axios from 'axios';
import { useAuthStore } from '@/store/useAuthStore';
import { API_PATHS } from '@/constants/apiPaths';

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api';

const axiosInstance = axios.create({
  baseURL: API_BASE,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

/** Request interceptor — attach access token */
axiosInstance.interceptors.request.use(
  (config) => {
    const { accessToken } = useAuthStore.getState();
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

/** Response interceptor — handle 401 token refresh */
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error);
    else resolve(token);
  });
  failedQueue = [];
};

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const isSessionRevoked = error.response?.data?.errorCode === 'SESSION_REVOKED';

    // If 401 Unauthorized
    if (error.response?.status === 401) {
      // 1. If it's a login credentials error, do not redirect/logout
      if (error.response?.data?.errorCode === 'INVALID_CREDENTIALS') {
        return Promise.reject(error);
      }

      // 2. If session is explicitly revoked, logout and redirect immediately
      if (isSessionRevoked) {
        useAuthStore.getState().logout();
        localStorage.setItem(
          'logout_message',
          'You have been logged out because your account was accessed from another device.'
        );
        window.location.href = '/login';
        return Promise.reject(error);
      }

      // 3. Otherwise, try to refresh the token (normal access token expiry)
      if (!originalRequest._retry) {
        // Don't retry other auth endpoints
        if (originalRequest.url?.includes('/auth/')) {
          return Promise.reject(error);
        }

        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject });
          }).then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return axiosInstance(originalRequest);
          });
        }

        originalRequest._retry = true;
        isRefreshing = true;

        try {
          const { refreshToken } = useAuthStore.getState();
          if (!refreshToken) {
            useAuthStore.getState().logout();
            localStorage.setItem(
              'logout_message',
              'You have been logged out because your account was accessed from another device.'
            );
            window.location.href = '/login';
            return Promise.reject(error);
          }

          const response = await axios.post(`${API_BASE}${API_PATHS.AUTH_REFRESH}`, {
            refreshToken,
          });

          const { accessToken: newAccessToken } = response.data.data;
          useAuthStore.getState().setTokens(newAccessToken, refreshToken);

          processQueue(null, newAccessToken);
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
          return axiosInstance(originalRequest);
        } catch (refreshError) {
          processQueue(refreshError);
          useAuthStore.getState().logout();
          localStorage.setItem(
            'logout_message',
            'You have been logged out because your account was accessed from another device.'
          );
          window.location.href = '/login';
          return Promise.reject(refreshError);
        } finally {
          isRefreshing = false;
        }
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
