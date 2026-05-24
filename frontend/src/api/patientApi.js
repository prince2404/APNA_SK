import axiosInstance from './axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';

export const patientApi = {
  getPatients: (params) => axiosInstance.get(API_PATHS.PATIENTS, { params }),
  getPatient: (id) => axiosInstance.get(`${API_PATHS.PATIENTS}/${id}`),
  registerPatient: (data) => axiosInstance.post(API_PATHS.PATIENTS, data),
  updatePatient: (id, data) => axiosInstance.put(`${API_PATHS.PATIENTS}/${id}`, data),
  bulkUpload: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return axiosInstance.post(`${API_PATHS.PATIENTS}/bulk-upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
};
