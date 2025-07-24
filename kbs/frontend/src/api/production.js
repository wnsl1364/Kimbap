import axios from 'axios';

// 공장 목록 조회
export const getFactoryList = () => {
  return axios.get('/api/prod/factory/list');
};

// 생산계획 목록 조회
export const getProdPlanList = () => {
  return axios.get('/api/prod/prodPlan/list');
};
