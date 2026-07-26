import axiosInstance from './axiosInstance';

export const getInquiries = (params) =>
  axiosInstance.get('/api/inquiries', { params });

export const getInquiryDetail = (id) =>
  axiosInstance.get(/api/inquiries/);

export const updateInquiryStatus = (id, status) =>
  axiosInstance.patch(/api/inquiries//status, { status });
