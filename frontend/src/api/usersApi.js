import axiosInstance from './axiosInstance';

// 백틱 필요 없음 (변수가 안에 안 들어감)
export const getUser = (params) =>
  axiosInstance.get('/api/users', { params });

// 백틱 필요함 (변수 id가 안에 들어감)
export const getUserDetail = (id) =>
  axiosInstance.get(`/api/users/${id}`);

//회원의 개인정보 수정
export const updateUser = (id, data) =>
  axiosInstance.put(`/api/users/${id}`, data);

//회원을 즉시 휴면 상태로 전환 (조건 없이 즉시 처리됨)
export const dormantUser = (id) =>
  axiosInstance.put(`/api/users/${id}/dormant`);

//휴면 상태를 다시 정상(ACTIVE)으로 되돌림
export const activateUser = (id) =>
  axiosInstance.put(`/api/users/${id}/activate`);

//회원 상태 통계
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


