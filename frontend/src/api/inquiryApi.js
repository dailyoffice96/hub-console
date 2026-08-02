import axiosInstance from './axiosInstance';

export const getInquiry = (params) =>
  axiosInstance.get('/api/inquiries', { params });

export const getInquiryDetail = (id) =>
  axiosInstance.get(`/api/inquiries/${id}`);

export const createComment = (id, content) =>
  axiosInstance.post(`/api/inquiries/${id}/comments`, { content }, {
    headers: { 'Content-Type': 'application/json' }
  });

export const updateStatus = (id, status) =>
  axiosInstance.put(`/api/inquiries/${id}/status`, { status }, {
    headers: { 'Content-Type': 'application/json' }
  });

export const assignInquiry = (id, adminId) =>
  axiosInstance.put(`/api/inquiries/${id}/assign`, { adminId }, {
    headers: { 'Content-Type': 'application/json' }
  });

export const getInquiryStats = () =>
  axiosInstance.get('/api/inquiries/stats');