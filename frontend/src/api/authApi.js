import axiosInstance from './axiosInstance';

export const login = (loginId, password) =>
  //실제로 보낼 데이터 (아이디/비번을 폼 형식으로 변환)
  axiosInstance.post('/login', new URLSearchParams ({ loginId, password }),
  //"이 데이터는 form 형식(x-www-form-urlencoded)이다"라고 서버에 알려줌
  {headers: { 'Content-Type': 'application/x-www-form-urlencoded' }}
  );

export const logout = () =>
  axiosInstance.post('/logout');