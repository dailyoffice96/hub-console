import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: 'https://sm-console-production-6596.up.railway.app',
  withCredentials: true,
    headers: {
      'Cache-Control': 'no-cache',
    },
});

export default axiosInstance;