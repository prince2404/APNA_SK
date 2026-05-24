import axiosInstance from './axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';

export const notificationApi = {
  getNotifications: (params) => axiosInstance.get(API_PATHS.NOTIFICATIONS, { params }),
  getUnreadCount: () => axiosInstance.get(`${API_PATHS.NOTIFICATIONS}/unread-count`),
  markAsRead: (id) => axiosInstance.patch(`${API_PATHS.NOTIFICATIONS}/${id}/read`),
  markAllAsRead: () => axiosInstance.patch(`${API_PATHS.NOTIFICATIONS}/read-all`),
};
