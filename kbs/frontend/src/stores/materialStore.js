import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { format } from 'date-fns';

export const useMaterialStore = defineStore('material', () => {
  const materials = ref([]);
  const formatDate = (date) => {
    if (!date) return null;
    return format(new Date(date), 'yyyy-MM-dd');
  };

  const setMaterials = (newMaterials) => {
    materials.value = newMaterials;
  };
  const searchColumns = ref([]);
  const purchaseColumns = ref([]);
  const purchaseData = ref([]);
//------------------------------------------------------------------------------------------------------------------------
  const inboundData = ref({});
  const inMaterialColumns = ref([]);
  const warehouseColumns = ref([]);
  const inboundFields = ref([]);
  // 선택된 자재들을 저장할 새로운 필드 추가
  const selectedInboundMaterials = ref([]);
  
  // 선택된 자재들을 설정하는 함수
  const setSelectedInboundMaterials = (materials) => {
    selectedInboundMaterials.value = materials;
  };
  
  // 입고 처리된 자재들을 저장할 필드 (히스토리 관리용)
  const processedInboundMaterials = ref([]);
  const purchaseFormButtons = ref([]);
  const materialTableButtons = ref([]);
  
  // 입고 처리된 자재들을 추가하는 함수
  const addProcessedInboundMaterials = (materials) => {
    processedInboundMaterials.value.push(...materials);
  };

//------------------------------------------------------------------------------------------------------------------------
  // 구매/발주 관련 데이터
  const purchaseOrderData = ref([]);
  const purchaseOrderDetailData = ref([]);
  
  // 내부직원용 구매/발주 컬럼 설정 (수정!)
  const internalPurchaseColumns = ref([
    { field: 'purcCd', header: '발주번호', sortable: true },
    { field: 'purcDCd', header: '발주상세번호', sortable: true },
    { field: 'mateName', header: '자재명', sortable: true },        // materialName → mateName
    { field: 'mateType', header: '자재유형', sortable: true },      // materialType → mateType
    { field: 'purcQty', header: '수량', sortable: true },
    { field: 'unit', header: '단위', sortable: true },
    { field: 'exDeliDt', header: '납기예정일', sortable: true },
    { field: 'deliDt', header: '납기일', sortable: true },          // actualDeliDt → deliDt
    { field: 'purcDStatus', header: '발주상태', sortable: true },
    { field: 'ordDt', header: '주문일자', sortable: true },
    { field: 'note', header: '비고', sortable: false }
  ]);

  

  // 공급업체직원용 구매/발주 컬럼 설정 (수정!)
  const supplierPurchaseColumns = ref([
    { field: 'purcCd', header: '발주번호', sortable: true },
    { field: 'purcDCd', header: '발주상세번호', sortable: true },
    { field: 'mcode', header: '자재코드', sortable: true },         // 추가!
    { field: 'mateName', header: '자재명', sortable: true },        // materialName → mateName
    { field: 'purcQty', header: '수량', sortable: true },
    { field: 'unit', header: '단위', sortable: true },
    { field: 'exDeliDt', header: '납기일', sortable: true },
    { field: 'purcDStatus', header: '발주상태', sortable: true },
    { field: 'totalAmount', header: '총금액(원)', sortable: true }, // ordTotalAmount → totalAmount
    { field: 'note', header: '비고', sortable: false }
  ]);

  // 구매/발주 상태 옵션 (실제 DB 값에 맞게 수정)
  const purchaseStatusOptions = ref([
    { label: '주문완료', value: 'c1' },
    { label: '제작중', value: 'c2' },
    { label: '배송완료', value: 'c3' },
    { label: '취소', value: 'c4' }
  ]);

  const materialSearchColumns = ref([
    { key: 'mcode', label: '자재코드', type: 'text', placeholder: '자재코드 검색' },
    { key: 'mateName', label: '자재명', type: 'text', placeholder: '자재명 검색' },
    { key: 'wareName', label: '창고명', type: 'text', placeholder: '창고명 검색' },
    { key: 'mateType', label: '자재유형', type: 'dropdown', options: [], placeholder: '자재유형 검색' }
  ]);

  // 내부직원용 구매/발주 검색 컬럼 설정 (수정!)
  const internalPurchaseSearchColumns = computed(() => [
    { 
      key: 'purcDCd', 
      label: '발주상세번호', 
      type: 'text',
      placeholder: '발주상세번호 검색'
    },
    { 
      key: 'mateName',         // materialName → mateName
      label: '자재명', 
      type: 'text',
      placeholder: '자재명 검색'
    },
    { 
      key: 'mateType',         // materialType → mateType
      label: '자재유형', 
      type: 'dropdown',
      options: [
        { label: '전체', value: '' },
        { label: '원자재', value: 'h1' },
        { label: '부자재', value: 'h2' },
        { label: '완제품', value: 'h3' }
      ],
      placeholder: '자재유형 검색'
    },
    { 
      key: 'purcDStatus', 
      label: '발주상태', 
      type: 'dropdown',
      options: [
        { label: '전체', value: '' },
        ...purchaseStatusOptions.value
      ],
      placeholder: '상태 선택'
    },
    { 
      key: 'ordDt', 
      label: '주문일자', 
      type: 'dateRange',
      startPlaceholder: '시작일',
      endPlaceholder: '종료일'
    }
  ]);

  // 🎯 승인 관련 state 추가
  const approvalOrderHeader = ref({});
  const approvalOrderDetails = ref([]);
  const selectedApprovalItems = ref([]);

  // 🎯 승인 관련 actions
  const setApprovalOrderHeader = (headerData) => {
    approvalOrderHeader.value = headerData;
  };

  const setApprovalOrderDetails = (detailsData) => {
    approvalOrderDetails.value = detailsData;
  };

  const setSelectedApprovalItems = (selectedItems) => {
    selectedApprovalItems.value = selectedItems;
  };

  const clearSelectedApprovalItems = () => {
    selectedApprovalItems.value = [];
  };

  const clearApprovalData = () => {
    approvalOrderHeader.value = {};
    approvalOrderDetails.value = [];
    selectedApprovalItems.value = [];
  };

  // 공급업체직원용 구매/발주 검색 컬럼 설정 (수정!)
  const supplierPurchaseSearchColumns = computed(() => [
    { 
      key: 'mcode', 
      label: '자재코드', 
      type: 'text',
      placeholder: '자재코드 검색'
    },
    { 
      key: 'mateName',         // materialName → mateName
      label: '자재명', 
      type: 'text',
      placeholder: '자재명 검색'
    },
    { 
      key: 'exDeliDt', 
      label: '납기일', 
      type: 'dateRange',
      startPlaceholder: '납기 시작일',
      endPlaceholder: '납기 종료일'
    },
    { 
      key: 'purcDStatus', 
      label: '발주상태', 
      type: 'dropdown',
      options: [
        { label: '전체', value: '' },
        ...purchaseStatusOptions.value
      ],
      placeholder: '상태 선택'
    }
  ]);

  // 구매/발주 데이터 설정 함수
  const setPurchaseOrderData = (data) => {
    purchaseOrderData.value = data;
  };

  const setPurchaseOrderDetailData = (data) => {
    purchaseOrderDetailData.value = data;
  };

  // 구매/발주 상태 업데이트 함수
  const updatePurchaseStatus = (purcDCd, newStatus) => {
    const item = purchaseOrderDetailData.value.find(item => item.purcDCd === purcDCd);
    if (item) {
      item.purcDStatus = newStatus;
    }
  };

  const materialSupplierCombinations = ref([]);
  
  const setMaterialSupplierCombinations = (data) => {
    materialSupplierCombinations.value = data;
    console.log('Store에 자재-공급업체 조합 저장 완료:', data.length, '건');
  };

  const purchaseModalData = computed(() => ({
    // 자재명 중심 모달
    materialName: {
      items: materialSupplierCombinations.value.map(item => ({
        ...item,
        uniqueKey: `${item.mcode}-${item.mateVerCd}-${item.cpCd}` // 추가!
      })),
      columns: [
        { field: 'mcode', header: '자재코드' },
        { field: 'mateName', header: '자재명' },
        { field: 'cpName', header: '공급업체' },
        { field: 'unitPrice', header: '단가(원)' },
        { field: 'unit', header: '단위' },
        { field: 'ltime', header: '리드타임' }
      ],
      displayField: 'mateName',
      mappingFields: {
        materialName: 'mateName',
        buyer: 'cpName',  // 공급업체로 매핑
        mcode: 'mcode',
        mateVerCd: 'mateVerCd',
        cpCd: 'cpCd',
        unit: 'unit',
        price: 'unitPrice',  // 단가 자동 입력
        uniqueKey: 'uniqueKey'
      }
    },
    // 공급업체명 중심 모달
    buyer: {
      items: materialSupplierCombinations.value.map(item => ({
        ...item, 
        uniqueKey: `${item.mcode}-${item.mateVerCd}-${item.cpCd}` // 같은 키!
      })),
      columns: [
        { field: 'cpName', header: '공급업체' },
        { field: 'mateName', header: '자재명' },
        { field: 'unitPrice', header: '단가(원)' },
        { field: 'ltime', header: '리드타임' },
        { field: 'unit', header: '단위' }
      ],
      displayField: 'cpName',
      mappingFields: {
        buyer: 'cpName',
        materialName: 'mateName',
        mcode: 'mcode',
        mateVerCd: 'mateVerCd', 
        cpCd: 'cpCd',
        unit: 'unit',
        price: 'unitPrice',  // 단가 자동 입력
        uniqueKey: 'uniqueKey'
      }
    }
  }));

  // 모달 데이터 설정 (기존 - 호환성 유지)
  const modalDataSets = computed(() => ({
    buyer: {
      items: [
        { id: 1, code: 'B001', name: '삼성전자', category: '대기업', contact: '02-1234-5678' },
        { id: 2, code: 'B002', name: 'LG전자', category: '대기업', contact: '02-9876-5432' },
        { id: 3, code: 'B003', name: '현대자동차', category: '대기업', contact: '02-5555-1234' },
        { id: 4, code: 'B004', name: '소상공인협회', category: '중소기업', contact: '02-7777-8888' },
        { id: 5, code: 'B005', name: '네이버', category: 'IT기업', contact: '031-1111-2222' }
      ],
      columns: [
        { field: 'code', header: '거래처코드' },
        { field: 'name', header: '거래처명' },
        { field: 'category', header: '분류' },
        { field: 'contact', header: '연락처' }
      ],
      displayField: 'name',
      mappingFields: {
        buyerCode: 'code',
        buyerName: 'name',
        buyerCategory: 'category',
        buyerContact: 'contact'
      }
    },
    unit: {
      items: [
        { id: 1, code: 'U001', name: '개', type: '개수', desc: '낱개 단위' },
        { id: 2, code: 'U002', name: 'kg', type: '무게', desc: '킬로그램' },
        { id: 3, code: 'U003', name: 'box', type: '포장', desc: '박스 단위' },
        { id: 4, code: 'U004', name: 'm', type: '길이', desc: '미터' },
        { id: 5, code: 'U005', name: 'L', type: '용량', desc: '리터' },
        { id: 6, code: 'U006', name: 'ton', type: '무게', desc: '톤 단위' }
      ],
      columns: [
        { field: 'code', header: '단위코드' },
        { field: 'name', header: '단위명' },
        { field: 'type', header: '분류' },
        { field: 'desc', header: '설명' }
      ],
      displayField: 'name',
      mappingFields: {
        unitCode: 'code',
        unitName: 'name',
        unitType: 'type',
        unitDesc: 'desc'
      }
    }
  }));

  // 출고 데이터 설정
  const outboundData = ref({
    completedMaterials: [],
    processedAt: null,
    processedBy: '',
    totalProcessedCount: 0
  });

  const selectedOutboundMaterials = ref([]);
  const processedOutboundMaterials = ref([]);
  const outboundStatistics = ref({
    totalRequests: 0,
    completedRequests: 0,
    pendingRequests: 0,
    todayProcessed: 0
  });

  const setOutboundData = (data) => {
    outboundData.value = { ...outboundData.value, ...data };
    console.log('🚚 출고 데이터 설정됨:', outboundData.value);
  };

  const setSelectedOutboundMaterials = (materials) => {
    selectedOutboundMaterials.value = [...materials];
    console.log('📦 선택된 출고 자재 설정됨:', materials.length, '개');
  };

  const addProcessedOutboundMaterials = (materials) => {
    const processed = materials.map(material => ({
      ...material,
      processedAt: new Date(),
      processedBy: outboundData.value.processedBy || '시스템'
    }));
    
    processedOutboundMaterials.value.unshift(...processed);
    outboundStatistics.value.completedRequests += materials.length;
    outboundStatistics.value.todayProcessed += materials.length;
    
    console.log('✅ 처리된 출고 자재 히스토리 추가:', materials.length, '개');
  };

  const updateOutboundStatistics = (stats) => {
    outboundStatistics.value = { ...outboundStatistics.value, ...stats };
    console.log('📊 출고 통계 업데이트됨:', outboundStatistics.value);
  };

  const resetOutboundData = () => {
    outboundData.value = {
      completedMaterials: [],
      processedAt: null,
      processedBy: '',
      totalProcessedCount: 0
    };
    selectedOutboundMaterials.value = [];
    console.log('🔄 출고 데이터 초기화됨');
  };

  const getOutboundProcessStatus = () => {
    return {
      hasSelectedMaterials: selectedOutboundMaterials.value.length > 0,
      totalSelected: selectedOutboundMaterials.value.length,
      isProcessing: outboundData.value.processedAt !== null,
      lastProcessedBy: outboundData.value.processedBy,
      statistics: { ...outboundStatistics.value }
    };
  };

  // ========== 자재 재고 현황 관련 상태 및 액션 ==========
  
  // 📊 재고 현황 데이터 상태
  const stockStatusData = ref([]);              // 재고 현황 목록
  const stockStatistics = ref({});             // 재고 통계 (상태별 개수)
  const stockAlerts = ref([]);                 // 재고 알림 목록 (부족/과다)
  const stockStatusLoading = ref(false);       // 로딩 상태
  const selectedStockItems = ref([]);          // 선택된 재고 항목들
  
  // 🔍 재고 현황 검색 컬럼 설정
  const stockStatusSearchColumns = ref([
    {
      field: 'mcode',
      label: '자재코드',
      type: 'text',
      placeholder: '자재코드를 입력하세요',
      gridColumn: 'col-12 md:col-3'
    },
    {
      field: 'mateName', 
      label: '자재명',
      type: 'text',
      placeholder: '자재명을 입력하세요 (부분검색)',
      gridColumn: 'col-12 md:col-3'
    },
    {
      field: 'mateType',
      label: '자재유형',
      type: 'dropdown',
      options: [
        { label: '전체', value: '' },
        { label: '원자재', value: 'h1' },
        { label: '부자재', value: 'h2' }
      ],
      gridColumn: 'col-12 md:col-3'
    },
    {
      field: 'facName',
      label: '공장명',
      type: 'text', 
      placeholder: '공장명을 입력하세요 (부분검색)',
      gridColumn: 'col-12 md:col-3'
    }
  ]);
  
  // 📋 재고 현황 테이블 컬럼 설정
  const stockStatusDisplayColumns = ref([
    { field: 'materialCode', header: '자재코드', sortable: true, width: '120px' },
    { field: 'materialName', header: '자재명', sortable: true, width: '200px' },
    { field: 'materialTypeText', header: '자재유형', sortable: true, width: '100px' },
    { field: 'factoryName', header: '공장명', sortable: true, width: '150px' },
    { field: 'totalQuantity', header: '현재재고', sortable: true, width: '100px', dataType: 'numeric' },
    { field: 'unitText', header: '단위', sortable: false, width: '80px' },
    { field: 'safeStock', header: '안전재고', sortable: true, width: '100px', dataType: 'numeric' },
    { field: 'stockStatus', header: '재고상태', sortable: true, width: '100px' },
    { field: 'stockDifference', header: '재고차이', sortable: true, width: '100px', dataType: 'numeric' },
    { field: 'stockPercentage', header: '재고비율(%)', sortable: true, width: '120px', dataType: 'numeric' },
    { field: 'lotDetailLink', header: 'LOT정보', sortable: false, width: '120px' },
    { field: 'lastInboundDate', header: '최근입고일', sortable: true, width: '120px', dataType: 'date' }
  ]);
  
  // 🎯 재고 현황 데이터 설정
  const setStockStatusData = (data) => {
    stockStatusData.value = data || [];
    console.log('📊 재고 현황 데이터 설정:', stockStatusData.value.length + '건');
  };
  
  // 📈 재고 통계 설정
  const setStockStatistics = (statistics) => {
    stockStatistics.value = statistics || {};
    console.log('📈 재고 통계 설정:', statistics);
  };
  
  // ⚠️ 재고 알림 설정
  const setStockAlerts = (alerts) => {
    stockAlerts.value = alerts || [];
    console.log('⚠️ 재고 알림 설정:', alerts?.length + '건');
  };
  
  // 🔄 로딩 상태 설정
  const setStockStatusLoading = (loading) => {
    stockStatusLoading.value = loading;
  };
  
  // ✅ 선택된 재고 항목 설정
  const setSelectedStockItems = (items) => {
    selectedStockItems.value = items || [];
  };
  
  // 🧹 재고 현황 데이터 초기화
  const clearStockStatusData = () => {
    stockStatusData.value = [];
    stockStatistics.value = {};
    stockAlerts.value = [];
    selectedStockItems.value = [];
    stockStatusLoading.value = false;
    console.log('🧹 재고 현황 데이터 초기화 완료');
  };
  
  // 🔄 특정 재고 항목 업데이트
  const updateStockItem = (materialCode, factoryCode, updatedItem) => {
    const index = stockStatusData.value.findIndex(
      item => item.materialCode === materialCode && item.factoryCode === factoryCode
    );
    
    if (index !== -1) {
      stockStatusData.value[index] = { ...stockStatusData.value[index], ...updatedItem };
      console.log(`🔄 재고 항목 업데이트: ${materialCode} at ${factoryCode}`);
    }
  };

  return {
    materials,
    setMaterials,
    searchColumns,
    purchaseColumns,
    purchaseData,
    inboundData,
    inMaterialColumns,
    warehouseColumns,
    inboundFields,
    selectedInboundMaterials,
    setSelectedInboundMaterials,
    processedInboundMaterials,
    addProcessedInboundMaterials,
    // 구매/발주 관련 추가
    purchaseOrderData,
    purchaseOrderDetailData,
    internalPurchaseColumns,
    supplierPurchaseColumns,
    internalPurchaseSearchColumns,
    supplierPurchaseSearchColumns,
    purchaseStatusOptions,
    setPurchaseOrderData,
    setPurchaseOrderDetailData,
    updatePurchaseStatus,
    modalDataSets,
    purchaseFormButtons,
    materialTableButtons,
    materialSupplierCombinations,
    setMaterialSupplierCombinations,
    purchaseModalData,
    approvalOrderHeader,
    approvalOrderDetails,
    selectedApprovalItems,
    setApprovalOrderHeader,
    setApprovalOrderDetails,
    setSelectedApprovalItems,
    clearSelectedApprovalItems,
    clearApprovalData,
    outboundData,
    selectedOutboundMaterials,
    processedOutboundMaterials,
    outboundStatistics,
    resetOutboundData,
    getOutboundProcessStatus,
    materialSearchColumns,
    
    // ========== 자재 재고 현황 관련 ==========
    stockStatusData,
    stockStatistics,
    stockAlerts,
    stockStatusLoading,
    selectedStockItems,
    stockStatusSearchColumns,
    stockStatusDisplayColumns,
    setStockStatusData,
    setStockStatistics,
    setStockAlerts,
    setStockStatusLoading,
    setSelectedStockItems,
    clearStockStatusData,
    updateStockItem
  };
});