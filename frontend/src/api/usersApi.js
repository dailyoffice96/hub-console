import axiosInstance from './axiosInstance';

export const getUser = (params) =>
  axiosInstance.get('/api/users', { params });

export const getUserDetail = (id) =>
  axiosInstance.get(`/api/users/${id}`);

export const updateUser = (id, data) =>
  axiosInstance.put(`/api/users/${id}`, data);

// 유예 기간 없이 호출 즉시 휴면 처리된다.
export const dormantUser = (id) =>
  axiosInstance.put(`/api/users/${id}/dormant`);

export const activateUser = (id) =>
  axiosInstance.put(`/api/users/${id}/activate`);

export const getUserStats = () =>
   axiosInstance.get('/api/users/stats')

export const downloadUser = () => {
    return axiosInstance.get('/api/users/excel', {responseType: 'blob'});
}

export const uploadUser = (file) => {
    const formData = new FormData();
    formData.append('file', file);

    return axiosInstance.post('/api/users/excel/upload', formData,
    { headers: { 'Content-Type': 'multipart/form-data' }});
}


