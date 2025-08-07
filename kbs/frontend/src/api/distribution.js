import axios from 'axios';

// 완제품 입출고 조회
export const distributionInOutCheck = (filter) => {
  return axios.post('/api/distribution/distributionInOut', filter);
};

// 출고 지시서 조회
export const getRelOrdList = (filter) => {
  return axios.post('/api/distribution/relOrdList', filter);
};

// 출고지시서 등록 모달관련
export const getRelOrdModal = (filter) => {
  return axios.post('/api/distribution/relOrderModal', filter);
};

// 출고지시서 등록 모달관련
export const getRelOrdSelect = (ordCd) => {
  return axios.get('/api/distribution/relOrderSelect', { params: { ordCd } });
};

// 창고 목록 조회
export const getWareList = (ordCd) => {
  return axios.get('/api/distribution/warehouseList', { params: { ordCd } });
};