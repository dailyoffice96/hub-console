import axiosInstance from './axiosInstance';

export const getDailyStats = () =>
  axiosInstance.get('/api/dailyStats');
