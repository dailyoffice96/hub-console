import axiosInstance from './axiosInstance';

export const login = (loginId, password) =>
  axiosInstance.post('/login', new URLSearchParams ({ loginId, password }),
  {headers: { 'Content-Type': 'application/x-www-form-urlencoded' }}
  );

export const logout = () =>
  axiosInstance.post('/logout');

export const changeMyPassword = (currentPassword, newPassword) =>
  axiosInstance.put('/api/me/password', { currentPassword, newPassword });