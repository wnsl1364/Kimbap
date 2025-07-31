import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { 
  getMateLoadingWaitList, 
  getMateLoadingByInboCd, 
  processMateLoadingSingle, 
  processMateLoadingBatch, 
  getMateLoadingFactoryList,
  getWslCodeByArea 
} from '@/api/materials';

export const useMateLoadingStore = defineStore('mateLoading', () => {
  // ========== 상태 관리 ==========
  const mateLoadingList = ref([]);           // 적재 대기 목록
  const selectedMateLoadings = ref([]);      // 선택된 자재들 (체크박스)
  const factoryList = ref([]);               // 공장 목록 (드롭다운용)
  const isLoading = ref(false);              // 로딩 상태
  const searchFilter = ref({});              // 검색 필터 (나중에 검색 기능 추가시 사용)

  // ========== 검색 폼 컬럼 정의 ==========
  const searchColumns = ref([
    { 
      key: 'mateInboCd', 
      label: '입고번호', 
      type: 'text', 
      placeholder: '입고번호를 입력하세요' 
    },
    { 
      key: 'mcode', 
      label: '자재코드', 
      type: 'text', 
      placeholder: '자재코드를 입력하세요' 
    },
    { 
      key: 'mateName', 
      label: '자재명', 
      type: 'text', 
      placeholder: '자재명을 입력하세요' 
    },
    { 
      key: 'fcode', 
      label: '공장', 
      type: 'dropdown', 
      placeholder: '공장을 선택하세요',
      options: computed(() => factoryList.value.map(factory => ({
        label: factory.facName,
        value: factory.fcode
      })))
    },
    { 
      key: 'stoCon', 
      label: '보관조건', 
      type: 'dropdown', 
      placeholder: '보관조건을 선택하세요',
      options: [
        { label: '상온', value: 'o1' },
        { label: '냉장', value: 'o2' },
        { label: '냉동', value: 'o3' }
      ]
    },
    { 
      key: 'inboDt', 
      label: '입고일자', 
      type: 'dateRange',
      startPlaceholder: '시작일',
      endPlaceholder: '종료일'
    }
  ]);

  // ========== 테이블 컬럼 정의 ==========
  const tableColumns = ref([
    { field: 'mateInboCd', header: '입고번호', type: 'readonly' },
    { field: 'inboDt', header: '입고일자', type: 'readonly' },
    { field: 'mcode', header: '자재코드', type: 'readonly' },
    { field: 'mateName', header: '자재명', type: 'readonly' },
    { field: 'totalQty', header: '적재대기수량', type: 'readonly', align: 'right' },
    { field: 'qty', header: '적재수량', type: 'input', inputType: 'number', align: 'right', placeholder: '적재수량 입력' },
    { field: 'unit', header: '단위', type: 'readonly' },
    { field: 'stoCon', header: '보관조건', type: 'readonly' },
    { field: 'lotNo', header: 'LOT번호', type: 'readonly' },
    { field: 'note', header: '비고', type: 'input', placeholder: '비고 입력' },
    { field: 'facName', header: '공장', type: 'readonly' },
    { field: 'wareAreaCd', header: '적재구역', type: 'readonly' },
  ]);

  // ========== Computed ==========
  const hasSelectedItems = computed(() => selectedMateLoadings.value.length > 0);
  const selectedCount = computed(() => selectedMateLoadings.value.length);

  // ========== Functions ==========
  
  /**
   * 검색 기능 (프론트에서 필터링)
   */
  const searchMateLoadings = (searchData) => {
    if (!searchData) {
      return mateLoadingList.value;
    }

    return mateLoadingList.value.filter(item => {
      // 입고번호 검색
      const matchMateInboCd = !searchData.mateInboCd || 
        item.mateInboCd?.toLowerCase().includes(searchData.mateInboCd.toLowerCase());
      
      // 자재코드 검색
      const matchMcode = !searchData.mcode || 
        item.mcode?.toLowerCase().includes(searchData.mcode.toLowerCase());
      
      // 자재명 검색
      const matchMateName = !searchData.mateName || 
        item.mateName?.includes(searchData.mateName);
      
      // 공장 검색
      const matchFcode = !searchData.fcode || item.fcode === searchData.fcode;
      
      // 보관조건 검색
      const matchStoCon = !searchData.stoCon || item.stoCon === searchData.stoCon;
      
      // 입고일자 범위 검색
      let matchInboDt = true;
      if (searchData.inboDt?.start && searchData.inboDt?.end && item.inboDt) {
        const itemDate = new Date(item.inboDt);
        const startDate = new Date(searchData.inboDt.start);
        const endDate = new Date(searchData.inboDt.end);
        endDate.setHours(23, 59, 59, 999); // 하루 끝까지
        
        matchInboDt = itemDate >= startDate && itemDate <= endDate;
      }
      
      return matchMateInboCd && matchMcode && matchMateName && matchFcode && matchStoCon && matchInboDt;
    });
  };

  // 필터링된 목록 computed (함수 정의 후에 위치)
  const filteredMateLoadingList = computed(() => {
    if (!searchFilter.value || Object.keys(searchFilter.value).length === 0) {
      return mateLoadingList.value;
    }
    return searchMateLoadings(searchFilter.value);
  });

  // ========== Actions ==========

  /**
   * 자재 적재 대기 목록 조회
   */
  const fetchMateLoadingList = async () => {
    try {
      isLoading.value = true;
      const response = await getMateLoadingWaitList();
      
      // 적재수량 초기값을 적재대기수량으로 설정
      mateLoadingList.value = response.data.map(item => ({
        ...item,
        qty: item.totalQty, // 기본값으로 적재대기수량과 동일하게 설정
        id: item.mateInboCd // InputTable의 dataKey용
      }));
      
      console.log('자재 적재 대기 목록 조회 완료:', mateLoadingList.value.length, '건');
    } catch (error) {
      console.error('자재 적재 대기 목록 조회 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * 공장 목록 조회 (드롭다운용)
   */
  const fetchFactoryList = async () => {
    try {
      const response = await getMateLoadingFactoryList();
      factoryList.value = response.data;
      console.log('공장 목록 조회 완료:', factoryList.value.length, '개');
    } catch (error) {
      console.error('공장 목록 조회 실패:', error);
      throw error;
    }
  };

  /**
   * 단건 자재 적재 처리
   */
  const processSingleLoading = async (mateLoadingData) => {
    try {
      isLoading.value = true;
      const response = await processMateLoadingSingle(mateLoadingData);
      console.log('단건 적재 처리 완료:', response.data);
      
      // 목록에서 해당 항목 제거
      mateLoadingList.value = mateLoadingList.value.filter(
        item => item.mateInboCd !== mateLoadingData.mateInboCd
      );
      
      return response.data;
    } catch (error) {
      console.error('단건 적재 처리 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * 다중 자재 적재 처리 (적재처리 버튼)
   */
  const processBatchLoading = async () => {
    if (selectedMateLoadings.value.length === 0) {
      throw new Error('적재할 자재를 선택해주세요.');
    }

    try {
      isLoading.value = true;
      const response = await processMateLoadingBatch(selectedMateLoadings.value);
      console.log('다중 적재 처리 완료:', response.data);
      
      // 목록에서 처리된 항목들 제거
      const processedIds = selectedMateLoadings.value.map(item => item.mateInboCd);
      mateLoadingList.value = mateLoadingList.value.filter(
        item => !processedIds.includes(item.mateInboCd)
      );
      
      // 선택 초기화
      selectedMateLoadings.value = [];
      
      return response.data;
    } catch (error) {
      console.error('다중 적재 처리 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * 창고 구역별 wslcode 조회
   */
  const fetchWslCodeByArea = async (wareAreaCd) => {
    try {
      const response = await getWslCodeByArea(wareAreaCd);
      console.log('wslcode 조회 완료:', response.data);
      return response.data.wslCode;
    } catch (error) {
      console.error('wslcode 조회 실패:', error);
      throw error;
    }
  };

  /**
   * 선택된 자재 설정
   */
  const setSelectedMateLoadings = (selections) => {
    selectedMateLoadings.value = selections;
  };

  /**
   * 검색 필터 설정
   */
  const setSearchFilter = (filter) => {
    searchFilter.value = filter;
  };

  /**
   * 검색 필터 초기화
   */
  const clearSearchFilter = () => {
    searchFilter.value = {};
  };

  /**
   * 데이터 초기화
   */
  const resetData = () => {
    mateLoadingList.value = [];
    selectedMateLoadings.value = [];
    searchFilter.value = {};
  };

  return {
    // 상태
    mateLoadingList,
    selectedMateLoadings,
    factoryList,
    isLoading,
    searchFilter,
    
    // UI 설정
    searchColumns,
    tableColumns,
    
    // Computed
    hasSelectedItems,
    selectedCount,
    filteredMateLoadingList,  // 필터링된 목록 추가
    
    // Actions
    fetchMateLoadingList,
    fetchFactoryList,
    processSingleLoading,
    processBatchLoading,
    fetchWslCodeByArea,
    setSelectedMateLoadings,
    setSearchFilter,
    clearSearchFilter,  // 추가
    searchMateLoadings,
    resetData
  };
});