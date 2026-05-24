import axiosInstance from './axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';

export const profileApi = {
  getProfile: () => axiosInstance.get(API_PATHS.PROFILE),
  updateProfile: (data) => axiosInstance.put(API_PATHS.PROFILE, data),
  uploadPhoto: (formData) => axiosInstance.post(`${API_PATHS.PROFILE}/photo`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),
  getPhoto: () => axiosInstance.get(`${API_PATHS.PROFILE}/photo`, { responseType: 'blob' }),
  submitKyc: (formData) => axiosInstance.post(`${API_PATHS.PROFILE}/kyc`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),
  getKycDocument: () => axiosInstance.get(`${API_PATHS.PROFILE}/kyc/document`, { responseType: 'blob' }),
};
