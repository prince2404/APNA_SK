import axiosInstance from './axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';

export const billApi = {
  getBills: (params) => axiosInstance.get(API_PATHS.BILLS, { params }),
  getBillByNumber: (billNumber) => axiosInstance.get(`${API_PATHS.BILLS}/number/${billNumber}`),
  getBill: (id) => axiosInstance.get(`${API_PATHS.BILLS}/${id}`),
  createBill: (data) => axiosInstance.post(API_PATHS.BILLS, data),
  cancelBill: (id, cancelReason) => axiosInstance.patch(`${API_PATHS.BILLS}/${id}/cancel`, null, { params: { cancelReason } }),
  getBillPdfUrl: (id) => `${axiosInstance.defaults.baseURL || ''}${API_PATHS.BILLS}/${id}/pdf`,
  downloadBillPdf: (id) => axiosInstance.get(`${API_PATHS.BILLS}/${id}/pdf`, { responseType: 'blob' }),
};
