// api/materials.js
import axios from 'axios';
import { format, addDays } from 'date-fns';

// 자재입고 관련 API 함수들
export const getMaterialInboundList = () => {
  return axios.get('/api/materials/inbound');
};

export const getMaterialInboundById = (mateInboCd) => {
  return axios.get(`/api/materials/inbound/${mateInboCd}`);
};

export const saveMaterialInbound = (inboundData) => {
  return axios.post('/api/materials/inbound', inboundData);
};

export const updateMaterialInbound = (inboundData) => {
  return axios.put('/api/materials/inbound', inboundData);
};

// ========== 발주 목록 조회 (권한별 + 검색 조건) ==========
export const getPurchaseOrderList = (searchParams = {}, userType = 'p1') => {
  // 권한별 memtype 매핑
  const memtypeMap = {
    'internal': 'p1',   // 내부직원
    'supplier': 'p3'    // 공급업체직원
  };
  
  const params = {
    ...searchParams,
    memtype: memtypeMap[userType] || 'p1'  // 기본값 p1
  };
  
  return axios.get('/api/materials/purchaseOrders', { params });
};

// 발주 목록 검색 (상세 검색)
export const searchPurchaseOrders = (searchData, userType) => {
  const memtypeMap = {
    'internal': 'p1',
    'supplier': 'p3'
  };
  
  const params = {
    purcCd: searchData.purcDCd,           // 발주코드
    mateName: searchData.mateName,        // 자재명
    mcode: searchData.mcode,              // 자재코드 (p3만)
    purcDStatus: searchData.purcDStatus,  // 발주상태
    mateType: searchData.mateType,        // 자재유형 (p1만)
    startDate: searchData.ordDt?.start ? format(searchData.ordDt.start, 'yyyy-MM-dd') : null,
    endDate: searchData.ordDt?.end ? format(addDays(searchData.ordDt.end, 1), 'yyyy-MM-dd') : null,
    exDeliStartDate: searchData.exDeliDt?.start ? format(searchData.exDeliDt.start, 'yyyy-MM-dd') : null,
    exDeliEndDate: searchData.exDeliDt?.end ? format(addDays(searchData.exDeliDt.end, 1), 'yyyy-MM-dd') : null,
    memtype: memtypeMap[userType] || 'p1'
  };
  
  // null이나 undefined 값 제거
  Object.keys(params).forEach(key => 
    (params[key] === null || params[key] === undefined || params[key] === '') && delete params[key]
  );
  
  return axios.get('/api/materials/purchaseOrders', { params });
};

export const getPurcOrderList = () => {
  return axios.get('/api/materials/purchase-orders/list');
};

export const getPurcOrderWithDetails = (purcCd) => {
  return axios.get(`/api/materials/purchase-orders/${purcCd}`);
};

export const savePurchaseOrder = (orderData) => {
  return axios.post('/api/materials/purchase-orders', orderData);
};

export const generatePurchaseCode = () => {
  return axios.post('/api/materials/purchase-orders/generate-code');
};

export const getMaterialsWithSuppliers = (searchParams) => {
  const params = {
    mcode: searchParams?.mcode,
    mateName: searchParams?.mateName,
    cpCd: searchParams?.cpCd,
    cpName: searchParams?.cpName
  };

  Object.keys(params).forEach(key => 
  (params[key] === null || params[key] === undefined || params[key] === '') && delete params[key]
);
  return axios.get('/api/materials/materials-with-suppliers', { params })
}

export const getMaterials = (searchParams) => {
  const params = {
    mateName: searchParams.mateName,
    mateType: searchParams.mateType
  };

  Object.keys(params).forEach(key => 
  (params[key] === null || params[key] === undefined || params[key] === '') && delete params[key]
);
  return axios.get('/api/materials/materials', { params });
};

export const getSuppliers = (searchParams) => {
  const params = {
    cpName: searchParams.cpName,
    cpType: searchParams.cpType
  };

  Object.keys(params).forEach(key => 
  (params[key] === null || params[key] === undefined || params[key] === '') && delete params[key]
);
  return axios.get('/api/materials/suppliers', { params });
};

// 🔥 특정 자재의 공급업체들 조회
export const getSuppliersByMaterial = (mcode, mateVerCd) => {
  return axios.get(`/api/materials/materials/${mcode}/${mateVerCd}/suppliers`);
};

// 🔥 특정 거래처의 자재들 조회  
export const getMaterialsBySupplier = (cpCd) => {
  return axios.get(`/api/materials/suppliers/${cpCd}/materials`);
};

// 자재출고 관련 API 함수들
export const getMaterialOutboundList = () => {
  return axios.get('/api/materials/outbound');
};

export const saveMaterialOutbound = (outboundData) => {
  return axios.post('/api/materials/outbound', outboundData);
};