import axios from 'axios';

export const getMaterialList = () => {
  return axios.get('/api/std/mat/list');
};

export const insertMaterial = (data) => {
  return axios.post('/api/std/mat/insert', data);
};

export const getCompanyList = () => {
  return axios.get('/api/std/cp/list');
};

export const getSupplierList = () => {
  return axios.get('/api/std/cp/sup/list');
};

// 자재 단건 조회 API
export function getMaterialDetail(mcode) {
  return axios.get(`/api/std/mat/detail/${mcode}`);
}