import axiosInstance from './axiosInstance';

export const getMembers = (params) =>
  axiosInstance.get('/api/members', { params });

export const getMemberDetail = (id) =>
  axiosInstance.get(/api/members/);

export const updateMember = (id, data) =>
  axiosInstance.put(/api/members/, data);
