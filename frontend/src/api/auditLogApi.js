import axiosInstance from './axiosInstance';

export const getAuditLogs = (params) =>
  axiosInstance.get('/api/auditLogs', { params });

export const downloadAuditLog = () => {
    return axiosInstance.get('/api/auditLogs/excel', {responseType: 'blob'});
}

export const analyzeSuspicious = () =>
  axiosInstance.get('/api/auditLogs/analyze');