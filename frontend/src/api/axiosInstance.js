import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: '',
  withCredentials: true,
    headers: {
      'Cache-Control': 'no-cache',
    },
});

export default axiosInstance;