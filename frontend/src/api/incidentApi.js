import axiosInstance from './axiosInstance';

export const getIncidents = (params) =>
  axiosInstance.get('/api/incidents', { params });

export const getIncidentDetail = (id) =>
  axiosInstance.get(/api/incidents/);

export const updateIncidentStatus = (id, status) =>
  axiosInstance.patch(/api/incidents//status, { status });
