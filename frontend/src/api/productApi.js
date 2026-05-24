import axiosInstance from './axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';

export const productApi = {
  getCategories: () => axiosInstance.get(API_PATHS.PRODUCT_CATEGORIES),
  createCategory: (data) => axiosInstance.post(API_PATHS.PRODUCT_CATEGORIES, data),
  toggleCategory: (id) => axiosInstance.patch(`${API_PATHS.PRODUCT_CATEGORIES}/${id}/toggle`),

  getProducts: (params) => axiosInstance.get(API_PATHS.PRODUCTS, { params }),
  getProduct: (id) => axiosInstance.get(`${API_PATHS.PRODUCTS}/${id}`),
  createProduct: (data) => axiosInstance.post(API_PATHS.PRODUCTS, data),
  updateProduct: (id, data) => axiosInstance.put(`${API_PATHS.PRODUCTS}/${id}`, data),
  toggleProduct: (id) => axiosInstance.patch(`${API_PATHS.PRODUCTS}/${id}/toggle`),
};
