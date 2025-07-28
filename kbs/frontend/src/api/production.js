// api/production.js

import axios from 'axios';

// 공장 목록 조회
export const getFactoryList = () => {
  return axios.get('/api/prod/factory/list');
};

// 생산계획 목록 조회
export const getProdPlanList = () => {
  return axios.get('/api/prod/prodPlan/list');
};

// 조건 검색용 생산계획 조회
export const postProdPlanListByCondition = (searchParams) => {
  return axios.post('/api/prod/prodPlan/search', searchParams);
};

// 생산계획상세 목록 조회
export const getProdPlanDetailList = (produPlanCd) => {
  return axios.get(`/api/prod/prodPlan/${produPlanCd}`);
};
// 제품목록 전체 조회
export const getAllProducts = () => {
  return axios.get('/api/prod/prodPlan/productAll').then(res => res.data)
}