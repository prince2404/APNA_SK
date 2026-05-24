import axiosInstance from './axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';

export const commissionApi = {
  getConfigs: () => axiosInstance.get(`${API_PATHS.COMMISSIONS}/config`),
  updateConfig: (data) => axiosInstance.put(`${API_PATHS.COMMISSIONS}/config`, data),
  getCommissions: (params) => axiosInstance.get(API_PATHS.COMMISSIONS, { params }),
  getCommissionSummary: (params) => axiosInstance.get(`${API_PATHS.COMMISSIONS}/summary`, { params }),
};
