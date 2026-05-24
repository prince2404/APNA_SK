import axiosInstance from './axiosInstance';
import { API_PATHS } from '@/constants/apiPaths';

export const healthCardApi = {
  getHealthCards: (params) => axiosInstance.get(API_PATHS.HEALTH_CARDS, { params }),
  getHealthCardByNumber: (cardNumber) => axiosInstance.get(`${API_PATHS.HEALTH_CARDS}/number/${cardNumber}`),
  getHealthCardByPatientId: (patientId) => axiosInstance.get(`${API_PATHS.HEALTH_CARDS}/patient/${patientId}`),
  issueHealthCard: (data) => axiosInstance.post(API_PATHS.HEALTH_CARDS, data),
  addFamilyMember: (cardId, data) => axiosInstance.post(`${API_PATHS.HEALTH_CARDS}/${cardId}/members`, data),
  removeFamilyMember: (cardId, memberId) => axiosInstance.delete(`${API_PATHS.HEALTH_CARDS}/${cardId}/members/${memberId}`),
};
