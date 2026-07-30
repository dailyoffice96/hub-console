import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: 'http://localhost:9000',
  withCredentials: true, // "쿠키도 같이 보내라"
/*  1. 로그인 성공 → 서버가 세션 쿠키를 발급해서 브라우저에게 줌
  2. 이후 요청마다, 브라우저가 그 쿠키를 자동으로 담아서 보내야 함
  3. 서버가 그 쿠키를 보고 "아, 이 사람 로그인된 사람이구나" 확인*/
});

export default axiosInstance;
