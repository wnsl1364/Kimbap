import axios from 'axios';
import { format } from 'date-fns';

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

// 자재기준정보  단건 조회 API
export function getMaterialDetail(mcode) {
  return axios.get(`/api/std/mat/detail/${mcode}`);
}
// 자재기준정보 수정 API
export const updateMaterial = (data) => {
  return axios.put('/api/std/mat/update', data);
};
// 변경이력 조회 API
export function getChangeHistory(mcode) {
  return axios.get(`/api/std/mat/change-history/${mcode}`);
}

