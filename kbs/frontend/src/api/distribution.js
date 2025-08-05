import axios from 'axios';

// 완제품 입출고 조회
export const distributionInOutCheck = (filter) => {
  return axios.post('/api/distribution/distributionInOut', filter);
};

// 출고 지시서 조회
export const getRelOrdList = (filter) => {
  return axios.post('/api/distribution/relOrdList', filter);
};
