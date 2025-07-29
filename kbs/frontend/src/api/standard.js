import axios from 'axios';

// 자재기준정보 

// 자재 기준정보 목록 조회 API
export const getMaterialList = () => {
  return axios.get('/api/std/mat/list');
};
// 자재 기준정보 등록 API
export const insertMaterial = (data) => {
  return axios.post('/api/std/mat/insert', data);
};
// 공급사 목록 조회 API
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

// 제품 기준정보 
// 제품 기준정보 목록 조회 API
export const getProductList = () => {
  return axios.get('/api/std/prod/list');
}
// 제품 기준정보 등록 API
export const insertProduct = (data) => {
  return axios.post('/api/std/prod/insert', data);
}
// 제품 기준정보 단건 조회 API
export function getProductDetail(pcode) {
  return axios.get(`/api/std/prod/detail/${pcode}`);
}
// 제품 기준정보 수정 API
export const updateProdcut = (data) => {
  return axios.put('/api/std/prod/update', data);
}
// 제품 변경이력 조회 API
export function ProdChangeHistory(pcode) {
  return axios.get(`/api/std/prod/change-history/${pcode}`);
}

// 거래처 기준정보
// 거래처 기준정보 목록 조회 API
export const getCompanyList = () => {
  return axios.get('/api/std/cp/list');
}
// 거래처 기준정보 등록 API
export const insertCompany = (data) => {
  return axios.post('/api/std/cp/insert', data);
}
// 거래처 기준정보 단건 조회 API
export function getCompanyDetail(cpCd) {
  return axios.get(`/api/std/cp/detail/${cpCd}`);
}
// 거래처 기준정보 수정 API
export const updateCompany = (data) => {
  return axios.put('/api/std/cp/update', data);
}
// 거래처 변경이력 조회 API
export function CpChangeHistory(cpCd) {
  return axios.get(`/api/std/cp/change-history/${cpCd}`);
}


// 공장 기준정보
// 공장 기준정보 목록 조회 API
export const getFactoryList = () => {
  return axios.get('/api/std/fac/list');
}
// 공장 기준정보 등록 API
export const insertFactory = (data) => {
  return axios.post('/api/std/fac/insert', data);
}
// 공장 기준정보 단건 조회 API
export function getFactoryDetail(fcode) {
  return axios.get(`/api/std/fac/detail/${fcode}`);
}
// 공장 기준정보 수정 API
export const updateFactory = (data) => {
  return axios.put('/api/std/fac/update', data);
}
// 공장 변경이력 조회 API
export function FacChangeHistory(fcode) {
  return axios.get(`/api/std/fac/change-history/${fcode}`);
}