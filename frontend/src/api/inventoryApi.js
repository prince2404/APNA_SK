import axiosInstance from './axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';

export const inventoryApi = {
  // Central Warehouse
  receiveCentralStock: (data) => axiosInstance.post(`${API_PATHS.STOCK_CENTRAL}/receipt`, data),
  getCentralStock: (params) => axiosInstance.get(API_PATHS.STOCK_CENTRAL, { params }),

  // Transfer Orders
  createTransferOrder: (data) => axiosInstance.post(API_PATHS.TRANSFER_ORDERS, data),
  getTransferOrders: (params) => axiosInstance.get(API_PATHS.TRANSFER_ORDERS, { params }),
  getTransferOrder: (id) => axiosInstance.get(`${API_PATHS.TRANSFER_ORDERS}/${id}`),
  confirmTransferReceipt: (id) => axiosInstance.patch(`${API_PATHS.TRANSFER_ORDERS}/${id}/confirm`),
  cancelTransferOrder: (id) => axiosInstance.patch(`${API_PATHS.TRANSFER_ORDERS}/${id}/cancel`),

  // Store Stock
  getStoreStock: (params) => axiosInstance.get(API_PATHS.STOCK_STORE, { params }),
  getLowStockAlerts: (params) => axiosInstance.get(`${API_PATHS.STOCK_STORE}/alerts/low`, { params }),
  getExpiringStockAlerts: (params) => axiosInstance.get(`${API_PATHS.STOCK_STORE}/alerts/expiry`, { params }),

  // Stock Adjustments
  adjustStock: (data) => axiosInstance.post(API_PATHS.STOCK_ADJUSTMENTS, data),
  getStockAdjustments: (params) => axiosInstance.get(API_PATHS.STOCK_ADJUSTMENTS, { params }),

  // Stock Requests
  createStockRequest: (data) => axiosInstance.post(API_PATHS.STOCK_REQUESTS, data),
  getStockRequests: (params) => axiosInstance.get(API_PATHS.STOCK_REQUESTS, { params }),
  reviewStockRequest: (id, data) => axiosInstance.patch(`${API_PATHS.STOCK_REQUESTS}/${id}/review`, data),
};
