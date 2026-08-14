import axiosInstance from './axiosInstance';

export const login = (loginId, password) =>
  axiosInstance.post('/login', new URLSearchParams ({ loginId, password }),
  {headers: { 'Content-Type': 'application/x-www-form-urlencoded' }}
  );

export const logout = () =>
  axiosInstance.post('/logout');

//본인 비밀번호 변경 (로그인한 사람 본인만, 현재 비밀번호 확인 필요)
export const changeMyPassword = (currentPassword, newPassword) =>
  axiosInstance.put('/api/me/password', { currentPassword, newPassword });