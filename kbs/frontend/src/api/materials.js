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

// 🔥 특정 발주번호의 입고대기(c3) 상태 자재 조회
export const getPurchaseOrderDetailsForInbound = (purcCd) => {
  return axios.get(`/api/materials/purchase-orders/${purcCd}/inbound-ready`);
};

export const saveMaterialInbound = (inboundData) => {
  return axios.post('/api/materials/inbound', inboundData);
};

export const updateMaterialInbound = (inboundData) => {
  return axios.put('/api/materials/inbound', inboundData);
};

// 🔥 자재입고 신규 등록 (mate_inbo 테이블에 INSERT)
export const insertMateInbo = (mateInboData) => {
  return axios.post('/api/materials/inbound', mateInboData);
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

export const insertMaterialInbound = (data) => {
  return axios.post('/api/materials/inbound', data);
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

// ========== 창고 구역 선택 관련 API 함수들 ==========

// 특정 공장의 창고 목록 조회 (창고 유형별)

export const getWarehousesByFactory = (fcode) => {
  return axios.get('/api/materials/mateLoading/warehouses', {
    params: { fcode }
  });
};


// 특정 창고의 구역 정보 조회 (층별, 현재 적재 상황 포함)

export const getWarehouseAreasWithStock = (wcode, floor) => {
  return axios.get('/api/materials/mateLoading/warehouse-areas', {
    params: { wcode, floor }
  });
};


//  창고구역코드 조회

export const getWareAreaCode = (wcode, areaRow, areaCol, areaFloor) => {
  return axios.get('/api/materials/mateLoading/warehouse-area-code', {
    params: { wcode, areaRow, areaCol, areaFloor }
  });
};


//  구역 적재 가능 여부 검증

export const validateAreaAllocation = (wareAreaCd, mcode, allocateQty) => {
  return axios.get('/api/materials/mateLoading/validate-area', {
    params: { wareAreaCd, mcode, allocateQty }
  });
};


//  동일한 자재가 적재된 다른 구역들 조회 (분할 적재용)

export const getSameMaterialAreas = (mcode, fcode, excludeAreaCd = '') => {
  const params = { mcode, fcode };
  if (excludeAreaCd) {
    params.excludeAreaCd = excludeAreaCd;
  }
  
  return axios.get('/api/materials/mateLoading/same-material-areas', {
    params
  });
};

// 자재 입출고 내역 조회
export const getMaterialFlowList = (params) => {
  return axios.get('/api/materials/flow', { params });
};

// 이동요청 상태가 d1(요청)인 모든 자재의 배치 정보 조회
// MOVE_REQ 테이블의 MOVE_STATUS = 'd1'인 요청들의
// MOVE_REQ_D 테이블에서 ARR_AREA_CD (도착구역코드) 정보를 조회
export const getPendingMoveRequestPlacements = () => {
  return axios.get('/api/materials/stockMovement/stock-movement/pending-placements');
};
// 입출고내역 하루
export async function getTodayMaterialFlowList() {
  return axios.get('/api/materials/flow/today');
}

// ========== 자재 재고 현황 관련 API 함수들 ==========

/**
 * 🏭 자재 재고 현황 조회 API
 * 
 * 📌 프론트엔드 API 설계 철학:
 * - 백엔드 API와 1:1 매핑
 * - 검색 조건을 객체로 전달하여 유연성 확보
 * - null/undefined 값 자동 제거로 불필요한 파라미터 방지
 * 
 * @param {Object} searchParams 검색 조건 객체
 * @param {string} searchParams.mcode - 자재코드 (정확 매칭)
 * @param {string} searchParams.mateName - 자재명 (부분 검색)
 * @param {string} searchParams.mateType - 자재유형 (h1:원자재, h2:부자재)
 * @param {string} searchParams.facName - 공장명 (부분 검색)
 * @returns {Promise} axios 응답 객체 (data, statistics, alertCount 포함)
 */
export const getMaterialStockStatus = (searchParams = {}) => {
  console.log('🔍 API 호출: 자재 재고 현황 조회', searchParams);
  
  // 검색 파라미터 정리 (null, undefined, 빈 문자열 제거)
  const params = {
    mcode: searchParams.mcode,
    mateName: searchParams.mateName, 
    mateType: searchParams.mateType,
    facName: searchParams.facName
  };
  
  // 🧹 빈 값 제거 (백엔드에 불필요한 파라미터 전송 방지)
  Object.keys(params).forEach(key => 
    (params[key] === null || params[key] === undefined || params[key] === '') && delete params[key]
  );
  
  console.log('📤 전송 파라미터:', params);
  
  return axios.get('/api/materials/stock-status', { params });
};

/**
 * 🏷️ 특정 자재의 LOT별 상세 재고 조회
 * 
 * @param {string} mcode - 자재코드 (필수)
 * @param {string} fcode - 공장코드 (선택)
 * @returns {Promise} LOT별 상세 재고 정보
 */
export const getMaterialLotDetails = (mcode, fcode = null) => {
  console.log(`🏷️ API 호출: LOT별 상세 조회 (${mcode})`);
  
  const params = {};
  if (fcode) params.fcode = fcode;
  
  return axios.get(`/api/materials/stock-status/${mcode}/lots`, { params });
};

/**
 * 📊 재고 현황 엑셀 다운로드
 * 
 * @param {Object} searchParams - 동일한 검색 조건
 * @returns {Promise} 엑셀 파일 다운로드 응답
 */
export const exportStockStatusToExcel = (searchParams = {}) => {
  console.log('📊 API 호출: 재고 현황 엑셀 다운로드');
  
  const params = {
    mcode: searchParams.mcode,
    mateName: searchParams.mateName,
    mateType: searchParams.mateType,
    facName: searchParams.facName
  };
  
  Object.keys(params).forEach(key => 
    (params[key] === null || params[key] === undefined || params[key] === '') && delete params[key]
  );
  
  return axios.get('/api/materials/stock-status/export', { 
    params,
    responseType: 'blob' // 🔥 엑셀 파일 다운로드를 위한 blob 타입
  });
};

/**
 * ⚠️ 재고 부족/과다 알림 조회
 * 
 * @param {string} alertType - 알림 유형 (shortage, overstock, all)
 * @returns {Promise} 알림 대상 자재 목록
 */
export const getStockAlerts = (alertType = 'all') => {
  console.log(`⚠️ API 호출: 재고 알림 조회 (${alertType})`);
  
  return axios.get('/api/materials/stock-alerts', {
    params: { alertType }
  });
};


/**
 * 🔍 LOT별 재고 조회
 * @param {string} mcode - 자재코드
 * @returns {Promise} LOT별 재고 목록
 */
export const getMaterialLotStock = (mcode) => {
  console.log('🔍 LOT별 재고 조회 API 호출:', mcode);
  
  return axios.get(`/api/materials/${mcode}/lots`)
    .then(response => {
      console.log('✅ LOT별 재고 조회 API 응답:', response.data);
      return response;
    })
    .catch(error => {
      console.error('❌ LOT별 재고 조회 API 실패:', error);
      throw error;
    });
};