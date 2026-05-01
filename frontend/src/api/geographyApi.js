import axiosInstance from './axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';

export const geographyApi = {
  // States
  getStates: (params) => axiosInstance.get(API_PATHS.STATES, { params }),
  getActiveStates: (params) => axiosInstance.get(`${API_PATHS.STATES}/active`, { params }),
  getState: (id) => axiosInstance.get(`${API_PATHS.STATES}/${id}`),
  createState: (data) => axiosInstance.post(API_PATHS.STATES, data),
  updateState: (id, data) => axiosInstance.put(`${API_PATHS.STATES}/${id}`, data),
  toggleState: (id) => axiosInstance.patch(`${API_PATHS.STATES}/${id}/toggle`),

  // Districts
  getDistricts: (params) => axiosInstance.get(API_PATHS.DISTRICTS, { params }),
  getActiveDistricts: (params) => axiosInstance.get(`${API_PATHS.DISTRICTS}/active`, { params }),
  getDistrict: (id) => axiosInstance.get(`${API_PATHS.DISTRICTS}/${id}`),
  createDistrict: (data) => axiosInstance.post(API_PATHS.DISTRICTS, data),
  updateDistrict: (id, data) => axiosInstance.put(`${API_PATHS.DISTRICTS}/${id}`, data),
  toggleDistrict: (id) => axiosInstance.patch(`${API_PATHS.DISTRICTS}/${id}/toggle`),

  // Blocks
  getBlocks: (params) => axiosInstance.get(API_PATHS.BLOCKS, { params }),
  getActiveBlocks: (params) => axiosInstance.get(`${API_PATHS.BLOCKS}/active`, { params }),
  getBlock: (id) => axiosInstance.get(`${API_PATHS.BLOCKS}/${id}`),
  createBlock: (data) => axiosInstance.post(API_PATHS.BLOCKS, data),
  updateBlock: (id, data) => axiosInstance.put(`${API_PATHS.BLOCKS}/${id}`, data),
  toggleBlock: (id) => axiosInstance.patch(`${API_PATHS.BLOCKS}/${id}/toggle`),

  // Stores
  getStores: (params) => axiosInstance.get(API_PATHS.STORES, { params }),
  getStore: (id) => axiosInstance.get(`${API_PATHS.STORES}/${id}`),
  createStore: (data) => axiosInstance.post(API_PATHS.STORES, data),
  updateStore: (id, data) => axiosInstance.put(`${API_PATHS.STORES}/${id}`, data),
  toggleStore: (id) => axiosInstance.patch(`${API_PATHS.STORES}/${id}/toggle`),
};
