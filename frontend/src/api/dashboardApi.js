import axiosInstance from './axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';

export const dashboardApi = {
  getDashboardData: () => axiosInstance.get(API_PATHS.DASHBOARD),
};
