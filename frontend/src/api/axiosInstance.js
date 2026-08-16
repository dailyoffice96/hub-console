import axios from 'axios';

// 다른 파일(예: WebSocket 연결)에서도 같은 백엔드 주소가 필요해서 export 해둔다.
// 이 값 하나만 바꾸면 되고, 다른 곳에 새 주소를 또 하드코딩할 필요 없다.
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9000';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
    headers: {
      'Cache-Control': 'no-cache',
    },
});

// 401이 오면 로그인 페이지로 보낸다. 단, 로그인 페이지("/") 자체에서 난 401까지 리다이렉트하면
// 새로고침이 반복될 수 있어서 이미 로그인 페이지에 있을 때는 건드리지 않는다.
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
