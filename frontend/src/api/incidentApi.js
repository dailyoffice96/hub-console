import axiosInstance from './axiosInstance';

export const getIncidents = (params) =>
  axiosInstance.get('/api/incidents', { params });

export const getIncidentDetail = (id) =>
  axiosInstance.get(`/api/incidents/${id}`);

export const getIncidentStats = () =>
  axiosInstance.get(`/api/incidents/stats`);

export const createIncident = (request) =>
  axiosInstance.post(`/api/incidents`, request);

export const updateIncidentStatus = (id, status, version) =>
  axiosInstance.put(`/api/incidents/${id}/status`, { status, version });