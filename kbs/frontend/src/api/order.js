import axios from 'axios';

export const getOrderList = (params) => {
  return axios.get('/api/order/list', { params });
};