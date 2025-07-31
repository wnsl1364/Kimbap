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
  return axios.get('/api/prod/prodPlan/productAll');
};
// 생산계획 등록
export const postSaveProdPlan = (data) => {
  return axios.post('/api/prod/prodPlan/planSave', data)
}
// 생산계획과 관련 상세 삭제
export const deleteProdutionPlan = async (produPlanCd) => {
  return axios.delete(`/api/prod/prodPlan/${produPlanCd}`)
}
// 조건 검색용 생산요청 조회
export const postProdRequestListByCondition = (searchParams) => {
  return axios.post('/api/prod/request/search', searchParams);
};
// 생산요청상세 목록 조회
export const getProdRequestDetailList = (produReqCd) => {
  return axios.get(`/api/prod/request/${produReqCd}`);
};
// 생산요청 등록
export const postSaveProdReq = (data) => {
  return axios.post('/api/prod/request/requestSave', data)
}
// 생산요청과 관련 상세 삭제
export const deleteProductionReq = async (produReqCd) => {
  return axios.delete(`/api/prod/request/${produReqCd}`)
}