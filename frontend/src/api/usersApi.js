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

//회원의 휴면계정으로 전환
export const dormantUser = (id) =>
  axiosInstance.put(`/api/users/${id}/dormant`);

//회원 상태 통계
export const getUserStats = () =>
   axiosInstance.get('/api/users/stats')
