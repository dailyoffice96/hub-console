import axiosInstance from './axiosInstance';

export const getAuditLogs = (params) =>
  axiosInstance.get('/api/auditLogs', { params });