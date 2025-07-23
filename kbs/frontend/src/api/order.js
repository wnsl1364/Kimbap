import axios from 'axios';

export const getOrderList = () => {
  return axios.get('/api/order/list');
};