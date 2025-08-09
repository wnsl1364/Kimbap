import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { 
  getAllMoveRequestList,
  searchMoveRequestList,
  getMoveRequestById,
  getMoveRequestDetailList,
  approveMoveRequest,
  rejectMoveRequest,
  approveBatchMoveRequest
} from '@/api/materials';

export const useStockMovementListStore = defineStore('stockMovementList', () => {
  
  // ========== 상태 관리 ==========
  const moveRequestList = ref([]);           // 이동요청 목록
  const selectedMoveRequest = ref(null);     // 선택된 이동요청
  const moveRequestDetails = ref([]);        // 이동요청 상세 목록
  const isLoading = ref(false);              // 로딩 상태
  const searchFilter = ref({});              // 검색 필터
  
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
      key: 'requName',
      label: '요청자',
      type: 'text',
      placeholder: '요청자명을 입력하세요'
    },
    { 
      key: 'reqDt', 
      label: '요청일자', 
      type: 'dateRange',
      startPlaceholder: '시작일',
      endPlaceholder: '종료일'
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
    { field: 'moveReqCd', label: '이동요청번호', type: 'input', readonly: true },
    { field: 'reqDt', label: '요청일', type: 'input', readonly: true },
    { field: 'moveTypeText', label: '이동유형', type: 'input', readonly: true },
    { field: 'requName', label: '요청자', type: 'input', readonly: true },
    { field: 'moveRea', label: '이동사유', type: 'input', readonly: true },
    { field: 'note', label: '비고', type: 'input', readonly: true }
  ]);

  // 이동요청 상세 품목 테이블 컬럼 설정
  const moveRequestDetailColumns = ref([
    { field: 'itemCode', header: '자재코드', type: 'readonly' },
    { field: 'itemName', header: '자재명', type: 'readonly' },
    { field: 'lotNo', header: 'LOT번호', type: 'readonly' },
    { field: 'moveQty', header: '수량', type: 'readonly', align: 'right' },
    { field: 'depaLocation', header: '출발위치', type: 'readonly' },
    { field: 'arrLocation', header: '도착위치', type: 'readonly' }
  ]);

  // 승인자 정보 필드 설정
  const approverFields = ref([
    { field: 'approverName', label: '승인자', type: 'input', readonly: true },
    { field: 'rejectionReason', label: '거절사유', type: 'input', placeholder: '거절 시 사유를 입력하세요' }
  ]);

  // ========== Computed ==========
  
  // 필터링된 이동요청 목록
  const filteredMoveRequestList = computed(() => {
    if (!searchFilter.value || Object.keys(searchFilter.value).length === 0) {
      return moveRequestList.value;
    }

    return moveRequestList.value.filter(item => {
      let matches = true;

      // 이동요청번호 검색
      if (searchFilter.value.moveReqCd) {
        matches = matches && item.moveReqCd?.toLowerCase().includes(searchFilter.value.moveReqCd.toLowerCase());
      }

      // 요청자 검색
      if (searchFilter.value.requName) {
        matches = matches && item.requName?.toLowerCase().includes(searchFilter.value.requName.toLowerCase());
      }

      // 요청상태 검색
      if (searchFilter.value.moveStatus) {
        matches = matches && item.moveStatus === searchFilter.value.moveStatus;
      }

      // 요청일자 범위 검색
      if (searchFilter.value.startDate && searchFilter.value.endDate) {
        const itemDate = new Date(item.reqDt);
        const startDate = new Date(searchFilter.value.startDate);
        const endDate = new Date(searchFilter.value.endDate);
        endDate.setHours(23, 59, 59, 999); // 종료일은 해당 날짜의 마지막 시간까지
        matches = matches && itemDate >= startDate && itemDate <= endDate;
      }

      return matches;
    });
  });

  // 선택된 이동요청 정보
  const selectedMoveRequestInfo = computed(() => {
    if (!selectedMoveRequest.value) return {};
    
    return {
      ...selectedMoveRequest.value,
      reqDt: formatDate(selectedMoveRequest.value.reqDt)
    };
  });

  // ========== Actions ==========

  // 이동요청 목록 조회
  const fetchMoveRequestList = async () => {
    isLoading.value = true;
    try {
      const response = await getAllMoveRequestList();
      console.log('이동요청 목록 조회 성공:', response.data);
      moveRequestList.value = convertMoveRequestList(response.data);
      return response.data;
    } catch (error) {
      console.error('이동요청 목록 조회 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  // 이동요청 검색
  const searchMoveRequests = async (searchParam) => {
    isLoading.value = true;
    try {
      const response = await searchMoveRequestList(searchParam);
      console.log('이동요청 검색 성공:', response.data);
      moveRequestList.value = convertMoveRequestList(response.data);
      return response.data;
    } catch (error) {
      console.error('이동요청 검색 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  // 이동요청 단건 조회
  const fetchMoveRequestById = async (moveReqCd) => {
    isLoading.value = true;
    try {
      const response = await getMoveRequestById(moveReqCd);
      console.log('이동요청 단건 조회 성공:', response.data);
      selectedMoveRequest.value = convertSingleMoveRequest(response.data);
      return response.data;
    } catch (error) {
      console.error('이동요청 단건 조회 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  // 이동요청 상세 목록 조회
  const fetchMoveRequestDetails = async (moveReqCd) => {
    isLoading.value = true;
    try {
      const response = await getMoveRequestDetailList(moveReqCd);
      console.log('이동요청 상세 목록 조회 성공:', response.data);
      moveRequestDetails.value = convertMoveRequestDetails(response.data);
      return response.data;
    } catch (error) {
      console.error('이동요청 상세 목록 조회 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  // 이동요청 승인 처리
  const approveMoveRequestById = async (moveReqCd, approver, comment = '') => {
    isLoading.value = true;
    try {
      const response = await approveMoveRequest(moveReqCd, approver, comment);
      console.log('이동요청 승인 처리 성공:', response.data);
      
      // 목록에서 해당 항목의 상태 업데이트
      const index = moveRequestList.value.findIndex(item => item.moveReqCd === moveReqCd);
      if (index !== -1) {
        moveRequestList.value[index].moveStatus = 'd2';
        moveRequestList.value[index].moveStatusText = '승인';
        moveRequestList.value[index].appr = approver;
        moveRequestList.value[index].appDt = new Date().toISOString();
      }
      
      // 선택된 항목도 업데이트
      if (selectedMoveRequest.value && selectedMoveRequest.value.moveReqCd === moveReqCd) {
        selectedMoveRequest.value.moveStatus = 'd2';
        selectedMoveRequest.value.moveStatusText = '승인';
        selectedMoveRequest.value.appr = approver;
        selectedMoveRequest.value.appDt = new Date().toISOString();
      }
      
      return response.data;
    } catch (error) {
      console.error('이동요청 승인 처리 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  // 이동요청 거절 처리
  const rejectMoveRequestById = async (moveReqCd, approver, rejectReason) => {
    isLoading.value = true;
    try {
      const response = await rejectMoveRequest(moveReqCd, approver, rejectReason);
      console.log('이동요청 거절 처리 성공:', response.data);
      
      // 목록에서 해당 항목의 상태 업데이트
      const index = moveRequestList.value.findIndex(item => item.moveReqCd === moveReqCd);
      if (index !== -1) {
        moveRequestList.value[index].moveStatus = 'd3';
        moveRequestList.value[index].moveStatusText = '거절';
        moveRequestList.value[index].appr = approver;
        moveRequestList.value[index].appDt = new Date().toISOString();
        moveRequestList.value[index].retuRea = rejectReason;
      }
      
      // 선택된 항목도 업데이트
      if (selectedMoveRequest.value && selectedMoveRequest.value.moveReqCd === moveReqCd) {
        selectedMoveRequest.value.moveStatus = 'd3';
        selectedMoveRequest.value.moveStatusText = '거절';
        selectedMoveRequest.value.appr = approver;
        selectedMoveRequest.value.appDt = new Date().toISOString();
        selectedMoveRequest.value.retuRea = rejectReason;
      }
      
      return response.data;
    } catch (error) {
      console.error('이동요청 거절 처리 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  // 다중 승인 처리
  const approveBatchMoveRequests = async (moveReqCdList, approver, comment = '') => {
    isLoading.value = true;
    try {
      const response = await approveBatchMoveRequest(moveReqCdList, approver, comment);
      console.log('다중 이동요청 승인 처리 성공:', response.data);
      
      // 목록에서 해당 항목들의 상태 업데이트
      moveReqCdList.forEach(moveReqCd => {
        const index = moveRequestList.value.findIndex(item => item.moveReqCd === moveReqCd);
        if (index !== -1) {
          moveRequestList.value[index].moveStatus = 'd2';
          moveRequestList.value[index].moveStatusText = '승인';
          moveRequestList.value[index].appr = approver;
          moveRequestList.value[index].appDt = new Date().toISOString();
        }
      });
      
      return response.data;
    } catch (error) {
      console.error('다중 이동요청 승인 처리 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  // ========== 유틸리티 함수 ==========

  // 검색 필터 설정
  const setSearchFilter = (filter) => {
    searchFilter.value = { ...filter };
  };

  // 검색 필터 초기화
  const clearSearchFilter = () => {
    searchFilter.value = {};
  };

  // 선택된 이동요청 설정
  const setSelectedMoveRequest = (moveRequest) => {
    selectedMoveRequest.value = moveRequest;
  };

  // 선택된 이동요청 초기화
  const clearSelectedMoveRequest = () => {
    selectedMoveRequest.value = null;
    moveRequestDetails.value = [];
  };

  // 공통코드 변환 함수들
  const convertMoveTypeText = (moveType) => {
    switch(moveType) {
      case 'z1': return '내부이동';
      case 'z2': return '외부이동';
      default: return moveType || '';
    }
  };

  const convertMoveStatusText = (moveStatus) => {
    switch(moveStatus) {
      case 'd1': return '요청';
      case 'd2': return '승인';
      case 'd3': return '거절';
      default: return moveStatus || '';
    }
  };

  const convertItemTypeText = (itemType) => {
    switch(itemType) {
      case 'h1': return '원자재';
      case 'h2': return '부자재';
      case 'h3': return '완제품';
      default: return itemType || '';
    }
  };

  // 날짜 포맷 함수
  const formatDate = (date) => {
    if (!date) return '';
    const d = new Date(date);
    if (isNaN(d.getTime())) return '';
    
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    
    return `${year}-${month}-${day}`;
  };

  // 이동요청 목록 변환
  const convertMoveRequestList = (list) => {
    return list.map(item => ({
      ...item,
      moveTypeText: convertMoveTypeText(item.moveType),
      moveStatusText: convertMoveStatusText(item.moveStatus),
      reqDt: formatDate(item.reqDt)
    }));
  };

  // 단일 이동요청 변환
  const convertSingleMoveRequest = (item) => {
    return {
      ...item,
      moveTypeText: convertMoveTypeText(item.moveType),
      moveStatusText: convertMoveStatusText(item.moveStatus),
      reqDt: formatDate(item.reqDt),
      appDt: formatDate(item.appDt)
    };
  };

  // 이동요청 상세 목록 변환
  const convertMoveRequestDetails = (list) => {
    return list.map(item => ({
      ...item,
      itemTypeText: convertItemTypeText(item.itemType),
      itemCode: item.mcode || item.pcode || '',
      itemName: item.mateName || item.prodName || ''
    }));
  };

  return {
    // 상태
    moveRequestList,
    selectedMoveRequest,
    moveRequestDetails,
    isLoading,
    searchFilter,
    
    // UI 설정
    searchColumns,
    moveRequestColumns,
    moveRequestFields,
    moveRequestDetailColumns,
    approverFields,
    
    // Computed
    filteredMoveRequestList,
    selectedMoveRequestInfo,
    
    // Actions
    fetchMoveRequestList,
    searchMoveRequests,
    fetchMoveRequestById,
    fetchMoveRequestDetails,
    approveMoveRequestById,
    rejectMoveRequestById,
    approveBatchMoveRequests,
    
    // 유틸리티
    setSearchFilter,
    clearSearchFilter,
    setSelectedMoveRequest,
    clearSelectedMoveRequest,
    convertMoveTypeText,
    convertMoveStatusText,
    convertItemTypeText,
    formatDate
  };
});
