import axiosInstance from './axiosInstance';

export const getAdmin = (params) =>
    axiosInstance.get('/api/admins', {params});

export const getAdminStats = () =>
   axiosInstance.get('/api/admins/stats');

export const unlockAdmin = (id) =>
    axiosInstance.put(`/api/admins/${id}/unlock`);

export const deleteAdmin = (id) =>
    axiosInstance.delete(`/api/admins/${id}`);