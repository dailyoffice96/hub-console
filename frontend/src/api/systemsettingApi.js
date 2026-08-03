import axiosInstance from './axiosInstance';

export const getSystemSetting = (params) =>
    axiosInstance.get('/api/systemSettings', {params});

export const saveSystemSetting  = (request) =>
  axiosInstance.put('/api/systemSettings', request);