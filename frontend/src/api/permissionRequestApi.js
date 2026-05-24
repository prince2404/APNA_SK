import axiosInstance from './axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';

export const permissionRequestApi = {
  createRequest: (data) => axiosInstance.post(API_PATHS.PERMISSION_REQUESTS, data),
  getRequests: (params) => axiosInstance.get(API_PATHS.PERMISSION_REQUESTS, { params }),
  reviewRequest: (id, data) => axiosInstance.post(`${API_PATHS.PERMISSION_REQUESTS}/${id}/review`, data),
};
