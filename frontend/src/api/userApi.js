import axiosInstance from './axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';

export const userApi = {
  getUsers: (params) => axiosInstance.get(API_PATHS.USERS, { params }),
  getUser: (id) => axiosInstance.get(`${API_PATHS.USERS}/${id}`),
  createUser: (data) => axiosInstance.post(API_PATHS.USERS, data),
  updateUser: (id, data) => axiosInstance.put(`${API_PATHS.USERS}/${id}`, data),
  deactivateUser: (id) => axiosInstance.patch(`${API_PATHS.USERS}/${id}/deactivate`),
  reactivateUser: (id) => axiosInstance.patch(`${API_PATHS.USERS}/${id}/reactivate`),
  assignPermissions: (id, data) => axiosInstance.put(`${API_PATHS.USERS}/${id}/permissions`, data),
  getPermissions: () => axiosInstance.get(API_PATHS.PERMISSIONS),
  getVerificationQueue: (params) => axiosInstance.get(`${API_PATHS.USERS}/verification-queue`, { params }),
  verifyUser: (id, data) => axiosInstance.post(`${API_PATHS.USERS}/${id}/verify`, data),
  getUserKycDocument: (id) => axiosInstance.get(`${API_PATHS.USERS}/${id}/kyc/document`, { responseType: 'blob' }),
};
