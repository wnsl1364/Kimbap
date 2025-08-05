import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { 
  registerMoveRequest, 
  getAllMoveRequestList, 
  searchMoveRequestList,
  getMoveRequestById,
  getMoveRequestDetailList,
  getAvailableMaterialList,
  getAvailableProductList,
  getItemStockInfo,
  getActiveFactoryList,
  getWarehousesByFactory,
  getAreasByWarehouse,
  approveMoveRequest,
  rejectMoveRequest,
  approveBatchMoveRequest,
  getCurrentStock,
  validateMoveRequest,
  getMoveRequestStatusCount,
  getPendingApprovalCount
} from '@/api/stockMovement';

export const useStockMovementStore = defineStore('stockMovement', () => {
  
  // ========== 상태 관리 ==========
  const moveRequestList = ref([]);           // 이동요청 목록
  const selectedMoveRequest = ref(null);     // 선택된 이동요청
  const moveRequestDetails = ref([]);        // 이동요청 상세 목록
  const isLoading = ref(false);              // 로딩 상태
  const searchFilter = ref({});              // 검색 필터
  
  // ========== 품목 선택 모달 관련 ==========
  const availableMaterials = ref([]);        // 재고 자재 목록
  const availableProducts = ref([]);         // 재고 제품 목록
  const selectedItems = ref([]);             // 선택된 품목들
  
  // ========== 도착위치 선택 모달 관련 ==========
  const factoryList = ref([]);               // 공장 목록
  const warehouseList = ref([]);             // 창고 목록
  const areaList = ref([]);                  // 구역 목록
  const selectedDestination = ref({});       // 선택된 도착위치
  
  // ========== 통계 관련 ==========
  const statusCount = ref({});               // 상태별 건수
  const pendingCount = ref(0);               // 승인 대기 건수

  // ========== UI 설정 ==========
  
  // 검색 컬럼 설정
  const searchColumns = ref([
    { 
      key: 'moveReqCd', 
      label: '이동요청번호', 
      type: 'text', 
      placeholder: '이동요청번호를 입력하세요' 
    },
    {
      key: 'requ',
      label: '요청자',
      type: 'text',
      placeholder: '요청자명을 입력하세요'
    },
    {
      key: 'moveStatus',
      label: '요청상태',
      type: 'dropdown',
      placeholder: '상태를 선택하세요',
      options: [
        { label: '전체', value: '' },
        { label: '요청', value: 'd1' },
        { label: '승인', value: 'd2' },
        { label: '거절', value: 'd3' }
      ]
    },
    { 
      key: 'reqDt', 
      label: '요청일자', 
      type: 'dateRange',
      startPlaceholder: '시작일',
      endPlaceholder: '종료일'
    }
  ]);

  // 이동요청 목록 테이블 컬럼 설정
  const moveRequestColumns = ref([
    { field: 'moveReqCd', header: '이동요청번호', type: 'clickable' },
    { field: 'moveTypeText', header: '이동유형', type: 'readonly' },
    { field: 'reqDt', header: '요청일', type: 'readonly' },
    { field: 'requName', header: '요청자', type: 'readonly' },
    { field: 'moveStatusText', header: '상태', type: 'readonly' }
  ]);

  // 이동요청 상세 정보 필드 설정
  const moveRequestFields = ref([
    { key: 'moveReqCd', label: '이동요청번호', type: 'readonly' },
    { key: 'moveTypeText', label: '이동유형', type: 'readonly' },
    { key: 'moveRea', label: '이동사유', type: 'readonly' },
    { key: 'reqDt', label: '요청일', type: 'readonly' },
    { key: 'requName', label: '요청자', type: 'readonly' },
    { key: 'note', label: '비고', type: 'readonly' }
  ]);

  // 이동요청 상세 품목 테이블 컬럼 설정
  const moveRequestDetailColumns = ref([
    { field: 'itemCode', header: '품번코드', type: 'readonly' },
    { field: 'itemName', header: '품명', type: 'readonly' },
    { field: 'lotNo', header: 'LOT번호', type: 'readonly' },
    { field: 'moveQty', header: '수량', type: 'readonly', align: 'right' },
    { field: 'depaLocation', header: '출발위치', type: 'readonly' },
    { field: 'arrLocation', header: '도착위치', type: 'readonly' }
  ]);

  // 품목 등록 테이블 컬럼 설정 (이동요청서 등록용)
  const itemRegistrationColumns = ref([
    { field: 'itemType', header: '품목유형', type: 'dropdown', 
      options: [
        { label: '원자재', value: 'h1' },
        { label: '부자재', value: 'h2' },
        { label: '완제품', value: 'h3' }
      ]
    },
    { field: 'itemCode', header: '품목코드', type: 'inputsearch', suffixIcon: 'pi pi-search' },
    { field: 'itemName', header: '품목명', type: 'readonly' },
    { field: 'lotNo', header: 'LOT번호', type: 'readonly' },
    { field: 'currentStock', header: '현재재고', type: 'readonly', align: 'right' },
    { field: 'moveQty', header: '이동수량', type: 'input', inputType: 'number', align: 'right' },
    { field: 'unit', header: '단위', type: 'readonly' },
    { field: 'depaLocation', header: '출발위치', type: 'readonly' },
    { field: 'arrLocation', header: '도착위치', type: 'inputsearch', suffixIcon: 'pi pi-map-marker' }
  ]);

  // ========== Computed ==========
  const hasSelectedItems = computed(() => selectedItems.value.length > 0);
  const selectedItemsCount = computed(() => selectedItems.value.length);
  
  // 필터링된 이동요청 목록
  const filteredMoveRequestList = computed(() => {
    if (!searchFilter.value || Object.keys(searchFilter.value).length === 0) {
      return moveRequestList.value;
    }
    
    return moveRequestList.value.filter(item => {
      // 이동요청번호 검색
      const matchMoveReqCd = !searchFilter.value.moveReqCd || 
        item.moveReqCd?.toLowerCase().includes(searchFilter.value.moveReqCd.toLowerCase());
      
      // 요청자 검색
      const matchRequ = !searchFilter.value.requ || 
        item.requName?.includes(searchFilter.value.requ);
      
      // 상태 검색
      const matchStatus = !searchFilter.value.moveStatus || 
        item.moveStatus === searchFilter.value.moveStatus;
      
      // 요청일자 범위 검색
      let matchReqDt = true;
      if (searchFilter.value.reqDt?.start && searchFilter.value.reqDt?.end && item.reqDt) {
        const itemDate = new Date(item.reqDt);
        const startDate = new Date(searchFilter.value.reqDt.start);
        const endDate = new Date(searchFilter.value.reqDt.end);
        endDate.setHours(23, 59, 59, 999);
        
        matchReqDt = itemDate >= startDate && itemDate <= endDate;
      }
      
      return matchMoveReqCd && matchRequ && matchStatus && matchReqDt;
    });
  });

  // ========== Actions ==========

  // 이동요청서 등록
  const registerNewMoveRequest = async (header, details) => {
    try {
      isLoading.value = true;
      
      const requestData = {
        header: header,
        details: details
      };
      
      const response = await registerMoveRequest(requestData);
      
      console.log('이동요청서 등록 완료:', response.data);
      
      // 목록 새로고침
      await fetchMoveRequestList();
      
      return response.data;
      
    } catch (error) {
      console.error('이동요청서 등록 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  // 이동요청 목록 조회
  const fetchMoveRequestList = async () => {
    try {
      isLoading.value = true;
      const response = await getAllMoveRequestList();
      
      // 공통코드 텍스트 변환
      moveRequestList.value = response.data.map(item => ({
        ...item,
        moveTypeText: convertMoveTypeText(item.moveType),
        moveStatusText: convertMoveStatusText(item.moveStatus)
      }));
      
      console.log('이동요청 목록 조회 완료:', moveRequestList.value.length, '건');
    } catch (error) {
      console.error('이동요청 목록 조회 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  // 이동요청 검색
  const searchMoveRequests = async (searchParam) => {
    try {
      isLoading.value = true;
      const response = await searchMoveRequestList(searchParam);
      
      moveRequestList.value = response.data.map(item => ({
        ...item,
        moveTypeText: convertMoveTypeText(item.moveType),
        moveStatusText: convertMoveStatusText(item.moveStatus)
      }));
      
      console.log('이동요청 검색 완료:', moveRequestList.value.length, '건');
    } catch (error) {
      console.error('이동요청 검색 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  // 이동요청 단건 조회
  const fetchMoveRequestById = async (moveReqCd) => {
    try {
      isLoading.value = true;
      const response = await getMoveRequestById(moveReqCd);
      
      selectedMoveRequest.value = {
        ...response.data,
        moveTypeText: convertMoveTypeText(response.data.moveType),
        moveStatusText: convertMoveStatusText(response.data.moveStatus)
      };
      
      console.log('이동요청 단건 조회 완료:', moveReqCd);
      return selectedMoveRequest.value;
    } catch (error) {
      console.error('이동요청 단건 조회 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  // 이동요청 상세 목록 조회
  const fetchMoveRequestDetails = async (moveReqCd) => {
    try {
      isLoading.value = true;
      const response = await getMoveRequestDetailList(moveReqCd);
      
      moveRequestDetails.value = response.data.map(item => ({
        ...item,
        itemCode: item.mcode || item.pcode,
        unitText: convertUnitText(item.unit)
      }));
      
      console.log('이동요청 상세 조회 완료:', moveReqCd, '-', moveRequestDetails.value.length, '건');
      return moveRequestDetails.value;
    } catch (error) {
      console.error('이동요청 상세 조회 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  // 품목 선택 모달 데이터 조회
  const fetchAvailableItems = async (fcode, itemType) => {
    try {
      isLoading.value = true;
      
      if (itemType === 'material') {
        const response = await getAvailableMaterialList(fcode);
        availableMaterials.value = response.data;
        console.log('재고 자재 목록 조회 완료:', availableMaterials.value.length, '건');
      } else if (itemType === 'product') {
        const response = await getAvailableProductList(fcode);
        availableProducts.value = response.data;
        console.log('재고 제품 목록 조회 완료:', availableProducts.value.length, '건');
      }
    } catch (error) {
      console.error('재고 품목 조회 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  // 도착위치 선택 모달 데이터 조회
  const fetchLocationData = async (type, parentCode) => {
    try {
      isLoading.value = true;
      
      if (type === 'factory') {
        const response = await getActiveFactoryList();
        factoryList.value = response.data;
        console.log('공장 목록 조회 완료:', factoryList.value.length, '개');
      } else if (type === 'warehouse') {
        const response = await getWarehousesByFactory(parentCode);
        warehouseList.value = response.data;
        console.log('창고 목록 조회 완료:', warehouseList.value.length, '개');
      } else if (type === 'area') {
        const response = await getAreasByWarehouse(parentCode);
        areaList.value = response.data;
        console.log('구역 목록 조회 완료:', areaList.value.length, '개');
      }
    } catch (error) {
      console.error('위치 데이터 조회 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  // 이동요청 승인 처리
  const approveMoveRequestById = async (moveReqCd, approver, comment) => {
    try {
      isLoading.value = true;
      const response = await approveMoveRequest(moveReqCd, { approver, comment });
      
      console.log('이동요청 승인 완료:', moveReqCd);
      
      // 목록 새로고침
      await fetchMoveRequestList();
      
      return response.data;
    } catch (error) {
      console.error('이동요청 승인 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  // 이동요청 거절 처리
  const rejectMoveRequestById = async (moveReqCd, approver, rejectReason) => {
    try {
      isLoading.value = true;
      const response = await rejectMoveRequest(moveReqCd, { approver, rejectReason });
      
      console.log('이동요청 거절 완료:', moveReqCd);
      
      // 목록 새로고침
      await fetchMoveRequestList();
      
      return response.data;
    } catch (error) {
      console.error('이동요청 거절 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  // 다중 승인 처리
  const approveBatchMoveRequests = async (moveReqCdList, approver, comment) => {
    try {
      isLoading.value = true;
      const response = await approveBatchMoveRequest({ moveReqCdList, approver, comment });
      
      console.log('다중 이동요청 승인 완료:', moveReqCdList.length, '건');
      
      // 목록 새로고침
      await fetchMoveRequestList();
      
      return response.data;
    } catch (error) {
      console.error('다중 이동요청 승인 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  // 재고량 조회
  const fetchCurrentStock = async (wareAreaCd, itemType, itemCode, lotNo) => {
    try {
      const response = await getCurrentStock(wareAreaCd, itemType, itemCode, lotNo);
      return response.data.currentStock;
    } catch (error) {
      console.error('재고량 조회 실패:', error);
      return 0;
    }
  };

  // 이동 가능 여부 검증
  const validateMoveRequestData = async (stockMovement) => {
    try {
      const response = await validateMoveRequest(stockMovement);
      return response.data;
    } catch (error) {
      console.error('이동 검증 실패:', error);
      return { isValid: false, message: '검증 중 오류 발생' };
    }
  };

  // 통계 데이터 조회
  const fetchStatusCount = async () => {
    try {
      const response = await getMoveRequestStatusCount();
      statusCount.value = response.data;
      console.log('상태별 건수 조회 완료');
    } catch (error) {
      console.error('상태별 건수 조회 실패:', error);
    }
  };

  const fetchPendingCount = async () => {
    try {
      const response = await getPendingApprovalCount();
      pendingCount.value = response.data.pendingCount;
      console.log('승인 대기 건수 조회 완료:', pendingCount.value);
    } catch (error) {
      console.error('승인 대기 건수 조회 실패:', error);
    }
  };

  // ========== 유틸리티 함수 ==========

  // 검색 필터 설정
  const setSearchFilter = (filter) => {
    searchFilter.value = filter;
  };

  // 검색 필터 초기화
  const clearSearchFilter = () => {
    searchFilter.value = {};
  };

  // 선택된 품목 설정
  const setSelectedItems = (items) => {
    selectedItems.value = items;
  };

  // 선택된 도착위치 설정
  const setSelectedDestination = (destination) => {
    selectedDestination.value = destination;
  };

  // 공통코드 변환 함수들
  const convertMoveTypeText = (moveType) => {
    switch(moveType) {
      case 'z1': return '내부';
      case 'z2': return '외부';
      default: return moveType;
    }
  };

  const convertMoveStatusText = (moveStatus) => {
    switch(moveStatus) {
      case 'd1': return '요청';
      case 'd2': return '승인';
      case 'd3': return '거절';
      default: return moveStatus;
    }
  };

  const convertUnitText = (unit) => {
    switch(unit) {
      case 'g1': return 'g';
      case 'g2': return 'kg';
      case 'g3': return 'ml';
      case 'g4': return 'L';
      case 'g5': return 'ea';
      case 'g6': return 'box';
      case 'g7': return 'mm';
      default: return unit;
    }
  };

  // 데이터 초기화
  const resetData = () => {
    moveRequestList.value = [];
    selectedMoveRequest.value = null;
    moveRequestDetails.value = [];
    selectedItems.value = [];
    selectedDestination.value = {};
    searchFilter.value = {};
    availableMaterials.value = [];
    availableProducts.value = [];
    factoryList.value = [];
    warehouseList.value = [];
    areaList.value = [];
    statusCount.value = {};
    pendingCount.value = 0;
  };

  return {
    // 상태
    moveRequestList,
    selectedMoveRequest,
    moveRequestDetails,
    isLoading,
    searchFilter,
    availableMaterials,
    availableProducts,
    selectedItems,
    factoryList,
    warehouseList,
    areaList,
    selectedDestination,
    statusCount,
    pendingCount,
    
    // UI 설정
    searchColumns,
    moveRequestColumns,
    moveRequestFields,
    moveRequestDetailColumns,
    itemRegistrationColumns,
    
    // Computed
    hasSelectedItems,
    selectedItemsCount,
    filteredMoveRequestList,
    
    // Actions
    registerNewMoveRequest,
    fetchMoveRequestList,
    searchMoveRequests,
    fetchMoveRequestById,
    fetchMoveRequestDetails,
    fetchAvailableItems,
    fetchLocationData,
    approveMoveRequestById,
    rejectMoveRequestById,
    approveBatchMoveRequests,
    fetchCurrentStock,
    validateMoveRequestData,
    fetchStatusCount,
    fetchPendingCount,
    
    // 유틸리티
    setSearchFilter,
    clearSearchFilter,
    setSelectedItems,
    setSelectedDestination,
    convertMoveTypeText,
    convertMoveStatusText,
    convertUnitText,
    resetData
  };
});