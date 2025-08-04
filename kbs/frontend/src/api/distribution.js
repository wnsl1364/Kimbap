import axios from 'axios';

// 완제품 입출고 조회
export const distributionInOutCheck = () => {
  return axios.get('/api/distribution/distributionInOut');
};
