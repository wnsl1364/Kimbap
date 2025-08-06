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

// 제품 적재 관련 =========================================
// 자재 적재 대기 목록 전체 조회
export const getProdLoadingWaitList = () => {
  return axios.get('/api/prod/prodLoading/waitList');
};

// ========== 창고 구역 선택 관련 제품용 API 함수들 ==========

// 제품 구역 적재 가능 여부 검증
export const validateAreaAllocation = (wareAreaCd, pcode, allocateQty) => {
  return axios.get('/api/prod/prodLoading/validate-area', {
    params: { wareAreaCd, pcode, allocateQty }
  });
};

// 동일한 제품이 적재된 다른 구역들 조회 (분할 적재용)
export const getSameProductAreas = (pcode, fcode, excludeAreaCd = '') => {
  const params = { pcode, fcode };
  if (excludeAreaCd) {
    params.excludeAreaCd = excludeAreaCd;
  }
  
  return axios.get('/api/prod/prodLoading/same-product-areas', {
    params
  });
};

// 특정 창고의 구역 정보 조회 (층별, 현재 적재 상황 포함)

export const getWarehouseAreasWithStock = (wcode, floor) => {
  return axios.get('/api/prod/prodLoading/warehouse-areas', {
    params: { wcode, floor }
  });
};

// 특정 입고번호의 적재 대기 자재 단건 조회
export const getProdLoadingByInboCd = (prodInboCd) => {
  return axios.get(`/api/prod/prodLoading/detail/${prodInboCd}`);
};

// 단건 제품 적재 처리
export const processProdLoadingSingle = (prodLoadingData) => {
  return axios.post('/api/prod/prodLoading/processSingle', prodLoadingData);
};

// 다중 제품 적재 처리 (선택된 여러 자재 한번에 처리)
export const processProdLoadingBatch = (prodLoadingList) => {
  return axios.post('/api/prod/prodLoading/processBatch', prodLoadingList);
};