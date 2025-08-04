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

export const getPurcOrderDetailList = () => {
  return axios.get('/api/materials/purchaseOrders', { 
    params: { memtype: 'p1' } // 내부직원 권한으로 상세 조회
  });
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

export const getPurcOrderDetailListForApproval = () => {
  return axios.get('/api/materials/purchase-orders/approval-list');
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

export const updatePurchaseOrderStatus = (statusData) => {
  return axios.put('/api/materials/purchase-orders/status', statusData);
}

export const getPendingApprovalOrders = () => {
  const params = {
    purcDStatus: 'c1',
    ...searchParams
  };

  Object.keys(params).forEach(key => 
    (params[key] === null || params[key] === undefined || params[key] === '') && delete params[key]
  );
  return axios.get('/api/materials/purchase-orders/pending-approval', { params });
};

export const getPurchaseOrderStatistics = (dateRange = {}) => {
  const params = {
    startDate: dateRange.startDate,
    endDate: dateRange.endDate
  };
  
  Object.keys(params).forEach(key => 
    (params[key] === null || params[key] === undefined || params[key] === '') && delete params[key]
  );
  
  return axios.get('/api/materials/purchase-orders/statistics', { params });
};

export const sendApprovalNotification = (notificationData) => {
  return axios.post('/api/materials/purchase-orders/notification', notificationData);
};

export const getPurchaseOrderDetailWithHistory = (purcDCd) => {
  return axios.get(`/api/materials/purchase-orders/detail/${purcDCd}`);
};

export const bulkApprovePurchaseOrders = (purcDCdList, approver = 'system') => {
  const requestData = {
    purcDCdList: purcDCdList,
    newStatus: 'c2', // 승인
    approver: approver
  };
  
  return axios.put('/api/materials/purchase-orders/bulk-status', requestData);
};

export const bulkRejectPurchaseOrders = (purcDCdList, reason = '', approver = 'system') => {
  const requestData = {
    purcDCdList: purcDCdList,
    newStatus: 'c6', // 거절
    reason: reason,
    approver: approver
  };
  
  return axios.put('/api/materials/purchase-orders/bulk-status', requestData);
};

export const getPurchaseOrdersForView = (searchParams = {}, userType = 'p1') => {
  const memtypeMap = {
    'internal': 'p1',
    'supplier': 'p3'
  };
  
  const params = {
    ...searchParams,
    memtype: memtypeMap[userType] || 'p1'
  };
  
  // null이나 undefined 값 제거
  Object.keys(params).forEach(key => 
    (params[key] === null || params[key] === undefined || params[key] === '') && delete params[key]
  );
  
  return axios.get('/api/materials/purchase-orders-view', { params });
};

// 자재출고 목록 조회 (공급업체용)
export const getSuppliersMateRel = (searchParams = {}) => {
  const params = {
    purcCd: searchParams.purcCd,              // 발주번호
    purcDStatus: searchParams.purcDStatus,    // 발주상태
    ordDtStart: searchParams.ordDtStart,      // 발주일자 시작
    ordDtEnd: searchParams.ordDtEnd,          // 발주일자 종료
    // 추가 검색 조건들
    mateName: searchParams.mateName,          // 자재명
    mcode: searchParams.mcode,                // 자재코드
    cpCd: searchParams.cpCd                   // 거래처코드
  };
  
  Object.keys(params).forEach(key =>
    (params[key] === null || params[key] === undefined || params[key] === '') && delete params[key]
  );
  
  return axios.get('/api/materials/supplier-mate-relations', { params });
};

// 자재출고 완료 처리 (단건)
export const processMaterialOutboundSingle = (outboundData) => {
  return axios.post('/api/materials/outbound/process-single', outboundData);
};

// 자재출고 완료 처리 (다중)
export const processMaterialOutboundBatch = (outboundDataList) => {
  return axios.post('/api/materials/outbound/process-batch', outboundDataList);
};

// 자재출고 상태 업데이트
export const updateMaterialOutboundStatus = (updateData) => {
  return axios.put('/api/materials/outbound/status', updateData);
};

// 자재출고 이력 조회
export const getMaterialOutboundHistory = (searchParams = {}) => {
  const params = {
    purcCd: searchParams.purcCd,
    mcode: searchParams.mcode,
    startDate: searchParams.startDate,
    endDate: searchParams.endDate
  };
  
  Object.keys(params).forEach(key =>
    (params[key] === null || params[key] === undefined || params[key] === '') && delete params[key]
  );
  
  return axios.get('/api/materials/outbound/history', { params });
};

// 자재출고 통계 조회
export const getMaterialOutboundStatistics = (dateRange = {}) => {
  const params = {
    startDate: dateRange.startDate,
    endDate: dateRange.endDate
  };
  
  Object.keys(params).forEach(key =>
    (params[key] === null || params[key] === undefined || params[key] === '') && delete params[key]
  );
  
  return axios.get('/api/materials/outbound/statistics', { params });
};

// ========== 자재 적재 관련 API 함수들 (materials.js에 추가) ==========

// 자재 적재 대기 목록 전체 조회
export const getMateLoadingWaitList = () => {
  return axios.get('/api/materials/mateLoading/waitList');
};

// 특정 입고번호의 적재 대기 자재 단건 조회
export const getMateLoadingByInboCd = (mateInboCd) => {
  return axios.get(`/api/materials/mateLoading/detail/${mateInboCd}`);
};

// 단건 자재 적재 처리
export const processMateLoadingSingle = (mateLoadingData) => {
  return axios.post('/api/materials/mateLoading/processSingle', mateLoadingData);
};

// 다중 자재 적재 처리 (선택된 여러 자재 한번에 처리)
export const processMateLoadingBatch = (mateLoadingList) => {
  return axios.post('/api/materials/mateLoading/processBatch', mateLoadingList);
};

// 활성화된 공장 목록 조회 (검색조건 드롭다운용)
export const getMateLoadingFactoryList = () => {
  return axios.get('/api/materials/mateLoading/factories');
};

// 창고 구역별 wslcode 조회 (위치선택 시 사용)
export const getWslCodeByArea = (wareAreaCd) => {
  return axios.get('/api/materials/mateLoading/wslcode', {
    params: { wareAreaCd }
  });
};