import axiosInstance from './axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';

export const reportApi = {
  // Sales Report
  getSalesReport: (params) => axiosInstance.get(`${API_PATHS.REPORTS}/sales`, { params }),
  exportSalesReport: (params) => axiosInstance.get(`${API_PATHS.REPORTS}/sales/export`, { params, responseType: 'blob' }),

  // Stock Report
  getStockReport: (params) => axiosInstance.get(`${API_PATHS.REPORTS}/stock`, { params }),
  exportStockReport: (params) => axiosInstance.get(`${API_PATHS.REPORTS}/stock/export`, { params, responseType: 'blob' }),

  // Commission Report
  getCommissionReport: (params) => axiosInstance.get(`${API_PATHS.REPORTS}/commission`, { params }),
  exportCommissionReport: (params) => axiosInstance.get(`${API_PATHS.REPORTS}/commission/export`, { params, responseType: 'blob' }),

  // Patient Report
  getPatientReport: (params) => axiosInstance.get(`${API_PATHS.REPORTS}/patient`, { params }),
  exportPatientReport: (params) => axiosInstance.get(`${API_PATHS.REPORTS}/patient/export`, { params, responseType: 'blob' }),

  // Bill Report
  getBillReport: (params) => axiosInstance.get(`${API_PATHS.REPORTS}/bill`, { params }),
  exportBillReport: (params) => axiosInstance.get(`${API_PATHS.REPORTS}/bill/export`, { params, responseType: 'blob' }),

  // Expiry Report
  getExpiryReport: (params) => axiosInstance.get(`${API_PATHS.REPORTS}/expiry`, { params }),
  exportExpiryReport: (params) => axiosInstance.get(`${API_PATHS.REPORTS}/expiry/export`, { params, responseType: 'blob' }),

  // User Activity Report
  getUserActivityReport: (params) => axiosInstance.get(`${API_PATHS.REPORTS}/activity`, { params }),
  exportUserActivityReport: (params) => axiosInstance.get(`${API_PATHS.REPORTS}/activity/export`, { params, responseType: 'blob' }),

  // Revenue Report
  getRevenueReport: (params) => axiosInstance.get(`${API_PATHS.REPORTS}/revenue`, { params }),
  exportRevenueReport: (params) => axiosInstance.get(`${API_PATHS.REPORTS}/revenue/export`, { params, responseType: 'blob' }),

  // Low Stock Report
  getLowStockReport: (params) => axiosInstance.get(`${API_PATHS.REPORTS}/low-stock`, { params }),
  exportLowStockReport: (params) => axiosInstance.get(`${API_PATHS.REPORTS}/low-stock/export`, { params, responseType: 'blob' }),
};
