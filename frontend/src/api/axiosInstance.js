import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: 'http://15.164.80.9:9000',
  withCredentials: true,
});

export default axiosInstance;
