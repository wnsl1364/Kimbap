import axios from 'axios';

export const getMaterialList = () => {
  return axios.get('/api/std/mat/list');
};