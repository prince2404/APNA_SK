import axiosInstance from './axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';

export const sessionApi = {
  getSessions: (params) => axiosInstance.get(API_PATHS.SESSIONS, { params }),
  revokeSession: (id) => axiosInstance.patch(`${API_PATHS.SESSIONS}/${id}/revoke`),
};
