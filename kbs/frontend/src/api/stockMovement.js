import axios from 'axios';

// ========== 이동요청서 등록 관련 ==========

// 이동요청서 등록 (헤더 + 상세 통합)
export const registerMoveRequest = (requestData) => {
  return axios.post('/api/materials/stockMovement/register', requestData);
};

// ========== 이동요청 목록 조회 관련 ==========

// 이동요청 목록 전체 조회
export const getAllMoveRequestList = () => {
  return axios.get('/api/materials/stockMovement/list');
};

// 이동요청 목록 검색 조회
export const searchMoveRequestList = (searchParam) => {
  return axios.post('/api/materials/stockMovement/search', searchParam);
};

// 이동요청 단건 조회
export const getMoveRequestById = (moveReqCd) => {
  return axios.get(`/api/materials/stockMovement/detail/${moveReqCd}`);
};

// 이동요청 상세 목록 조회
export const getMoveRequestDetailList = (moveReqCd) => {
  return axios.get(`/api/materials/stockMovement/details/${moveReqCd}`);
};

// ========== 품목 선택 모달 관련 ==========

// 재고가 있는 자재 목록 조회
export const getAvailableMaterialList = (fcode) => {
  return axios.get('/api/materials/stockMovement/available-materials', {
    params: { fcode }
  });
};

// 재고가 있는 제품 목록 조회
export const getAvailableProductList = (fcode) => {
  return axios.get('/api/materials/stockMovement/available-products', {
    params: { fcode }
  });
};

// 특정 품목의 재고 정보 조회
export const getItemStockInfo = (itemType, itemCode, lotNo) => {
  return axios.get('/api/materials/stockMovement/item-stock', {
    params: { itemType, itemCode, lotNo }
  });
};

// ========== 도착위치 선택 모달 관련 ==========

// 활성화된 공장 목록 조회
export const getActiveFactoryList = () => {
  return axios.get('/api/materials/stockMovement/factories');
};

// 특정 공장의 창고 목록 조회
export const getWarehousesByFactory = (fcode) => {
  return axios.get('/api/materials/stockMovement/warehouses', {
    params: { fcode }
  });
};

// 특정 창고의 구역 목록 조회
export const getAreasByWarehouse = (wcode) => {
  return axios.get('/api/materials/stockMovement/areas', {
    params: { wcode }
  });
};

// ========== 승인/거절 처리 관련 ==========

// 이동요청 승인 처리
export const approveMoveRequest = (moveReqCd, requestData) => {
  return axios.put(`/api/materials/stockMovement/approve/${moveReqCd}`, requestData);
};

// 이동요청 거절 처리
export const rejectMoveRequest = (moveReqCd, requestData) => {
  return axios.put(`/api/materials/stockMovement/reject/${moveReqCd}`, requestData);
};

// 다중 이동요청 승인 처리
export const approveBatchMoveRequest = (requestData) => {
  return axios.put('/api/materials/stockMovement/approve-batch', requestData);
};

// ========== 유효성 검증 관련 ==========

// 현재 재고량 조회
export const getCurrentStock = (wareAreaCd, itemType, itemCode, lotNo) => {
  return axios.get('/api/materials/stockMovement/current-stock', {
    params: { wareAreaCd, itemType, itemCode, lotNo }
  });
};

// 이동 가능 여부 검증
export const validateMoveRequest = (stockMovement) => {
  return axios.post('/api/materials/stockMovement/validate', stockMovement);
};

// ========== 통계/대시보드 관련 ==========

// 이동요청 상태별 건수 조회
export const getMoveRequestStatusCount = () => {
  return axios.get('/api/materials/stockMovement/status-count');
};

// 승인 대기 건수 조회
export const getPendingApprovalCount = () => {
  return axios.get('/api/materials/stockMovement/pending-count');
};