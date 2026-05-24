import axiosInstance from './axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';

export const messageApi = {
  // Templates CRUD
  getTemplates: () => axiosInstance.get(API_PATHS.MESSAGE_TEMPLATES),
  getTemplate: (id) => axiosInstance.get(`${API_PATHS.MESSAGE_TEMPLATES}/${id}`),
  createTemplate: (data) => axiosInstance.post(API_PATHS.MESSAGE_TEMPLATES, data),
  updateTemplate: (id, data) => axiosInstance.put(`${API_PATHS.MESSAGE_TEMPLATES}/${id}`, data),
  deleteTemplate: (id) => axiosInstance.delete(`${API_PATHS.MESSAGE_TEMPLATES}/${id}`),

  // Bulk Dispatch
  sendBulkMessage: (data) => axiosInstance.post(`${API_PATHS.MESSAGES}/send-bulk`, data),
  getBulkMessageHistory: (params) => axiosInstance.get(`${API_PATHS.MESSAGES}/history`, { params }),
};
