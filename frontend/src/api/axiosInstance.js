import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: 'http://localhost:9000',
  withCredentials: true,
    headers: {
      'Cache-Control': 'no-cache',
    },
});

// 인증 안 된 상태로 보호된 API(/api/**)를 호출하면 백엔드가 401 + JSON을 돌려준다.
// 매 api/*.js 모듈마다 개별적으로 처리하지 않도록 여기서 한 번에 로그인 페이지로 보낸다.
// 로그인 페이지("/") 자체에서 발생한 401(예: 로그인 실패, 로그인 전 systemSettings 조회)까지
// 리다이렉트해버리면 새로고침이 반복될 수 있어서, 이미 로그인 페이지에 있을 때는 건드리지 않는다.
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && window.location.pathname !== '/') {
      window.location.href = '/';
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
