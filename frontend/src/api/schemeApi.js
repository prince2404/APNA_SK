import axiosInstance from './axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';

export const schemeApi = {
  getSchemes: () => axiosInstance.get(API_PATHS.SCHEMES),
  createScheme: (data) => axiosInstance.post(API_PATHS.SCHEMES, data),
  toggleSchemeStatus: (id) => axiosInstance.patch(`${API_PATHS.SCHEMES}/${id}/toggle-status`),
};
