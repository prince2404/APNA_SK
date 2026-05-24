import axiosInstance from './axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';

export const hospitalApi = {
  getHospitals: (params) => axiosInstance.get(API_PATHS.HOSPITALS, { params }),
  getHospital: (id) => axiosInstance.get(`${API_PATHS.HOSPITALS}/${id}`),
  createHospital: (data) => axiosInstance.post(API_PATHS.HOSPITALS, data),
  updateHospital: (id, data) => axiosInstance.put(`${API_PATHS.HOSPITALS}/${id}`, data),
  toggleHospitalStatus: (id) => axiosInstance.patch(`${API_PATHS.HOSPITALS}/${id}/toggle-status`),
};
