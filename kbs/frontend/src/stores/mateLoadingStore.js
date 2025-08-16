import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useCommonStore } from '@/stores/commonStore'; // 공통코드 변환용
import { useMemberStore } from '@/stores/memberStore'; // 사용자 정보용
import { 
  getMateLoadingWaitList, 
  getMateLoadingByInboCd, 
  processMateLoadingSingle, 
  processMateLoadingBatch, 
  getMateLoadingFactoryList,
  getWslCodeByArea,
  // 창고 구역 선택 관련 신규 함수들
  getWarehousesByFactory,
  getWarehouseAreasWithStock,
  getWareAreaCode,
  validateAreaAllocation,
  getSameMaterialAreas
} from '@/api/materials';

export const useMateLoadingStore = defineStore('mateLoading', () => {
  // ========== 기존 상태 관리 ==========
  const mateLoadingList = ref([]);           // 적재 대기 목록
  const selectedMateLoadings = ref([]);      // 선택된 자재들 (체크박스)
  const factoryList = ref([]);               // 공장 목록 (드롭다운용)
  const isLoading = ref(false);              // 로딩 상태
  const searchFilter = ref({});              // 검색 필터

  // ========== 창고 구역 선택 관련 신규 상태 ==========
  const warehouseList = ref([]);             // 창고 목록
  const warehouseAreas = ref([]);            // 창고 구역 목록
  const selectedWarehouseArea = ref(null);   // 선택된 창고 구역
  const areaValidationResult = ref({});      // 구역 검증 결과
  const sameMaterialAreas = ref([]);         // 동일 자재 적재 구역들

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
    { field: 'note', header: '비고', type: 'readonly', placeholder: '비고 입력' },
    { field: 'facName', header: '공장', type: 'readonly' },
    { 
      field: 'locationSelect', 
      header: '구역선택',  
      type: 'button', 
      buttonLabel: '구역선택', 
      buttonIcon: 'pi pi-map-marker',
      buttonEvent: 'locationSelect',
      width: '120px',
      minWidth: '120px'
    }
  ]);

  // ========== 기존 Computed ==========
  const hasSelectedItems = computed(() => selectedMateLoadings.value.length > 0);
  const selectedCount = computed(() => selectedMateLoadings.value.length);

  // ========== 기존 검색 기능 ==========
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
      
      // 입고일자 범위 검색 (평탄화된 데이터 형식으로 수정)
      let matchInboDt = true;
      if ((searchData.inboDtStart || searchData.inboDtEnd) && item.inboDt) {
        const itemDate = new Date(item.inboDt);
        
        // 시작일 조건 체크
        if (searchData.inboDtStart) {
          const startDate = new Date(searchData.inboDtStart);
          startDate.setHours(0, 0, 0, 0); // 하루 시작
          if (itemDate < startDate) {
            matchInboDt = false;
          }
        }
        
        // 종료일 조건 체크
        if (searchData.inboDtEnd && matchInboDt) {
          const endDate = new Date(searchData.inboDtEnd);
          endDate.setHours(23, 59, 59, 999); // 하루 끝까지
          if (itemDate > endDate) {
            matchInboDt = false;
          }
        }
      }
      
      return matchMateInboCd && matchMcode && matchMateName && matchFcode && matchStoCon && matchInboDt;
    });
  };

  // 필터링된 목록 computed
  const filteredMateLoadingList = computed(() => {
    // 검색 필터가 실제로 값이 있는지 확인 (빈 문자열이나 null/undefined 제외)
    const hasValidFilter = searchFilter.value && 
      Object.keys(searchFilter.value).length > 0 &&
      Object.values(searchFilter.value).some(value => 
        value !== null && 
        value !== undefined && 
        value !== '' && 
        (Array.isArray(value) ? value.length > 0 : true)
      );
    
    if (!hasValidFilter) {
      return mateLoadingList.value;
    }
    
    return searchMateLoadings(searchFilter.value);
  });

  // ========== 기존 Actions ==========

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
        id: item.mateInboCd, // InputTable의 dataKey용
        wareAreaCd: '' // 창고구역 초기값
      }));
    } catch (error) {
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
    } catch (error) {
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
      
      // 목록에서 해당 항목 제거
      mateLoadingList.value = mateLoadingList.value.filter(
        item => item.mateInboCd !== mateLoadingData.mateInboCd
      );
      
      return response.data;
    } catch (error) {
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
        throw new Error('선택된 자재가 없습니다.');
      }

      // 창고구역이 설정된 자재들만 필터링 (wareAreaCd 또는 placementPlan이 있는 경우)
      const assignedItems = selectedMateLoadings.value.filter(item => 
        (item.wareAreaCd && item.wareAreaCd.trim() !== '') ||
        (item.placementPlan && item.placementPlan.length > 0)
      );
      
      if (assignedItems.length === 0) {
        throw new Error('선택된 자재 중 창고구역이 설정된 자재가 없습니다. 먼저 구역을 선택해주세요.');
      }

      // 창고구역이 설정되지 않은 자재가 있으면 알림 (처리는 계속 진행)
      const unassignedItems = selectedMateLoadings.value.filter(item => 
        (!item.wareAreaCd || item.wareAreaCd.trim() === '') &&
        (!item.placementPlan || item.placementPlan.length === 0)
      );

      try {
        isLoading.value = true;
        
        // 현재 로그인 사용자 정보 가져오기
        const memberStore = useMemberStore();
        const currentUser = memberStore.user?.empCd || 'system';
        
        // 공통코드에서 단위 코드 변환을 위한 함수
        const getOriginalUnitCode = (displayValue) => {
          const commonStore = useCommonStore(); // 함수 내부에서 호출
          const unitCodes = commonStore.getCodes('0G');
          const found = unitCodes.find(code => code.cdInfo === displayValue);
          return found ? found.dcd : displayValue;
        };
        
        // 백엔드로 전송할 데이터 변환
        const processData = assignedItems.map(item => {
          // placementPlan이 있는 경우 - 다중 구역 적재
          if (item.placementPlan && item.placementPlan.length > 0) {
            // 각 구역별로 개별 적재 항목 생성
            return item.placementPlan.map(plan => ({
              mateInboCd: item.mateInboCd,
              mcode: item.mcode,
              mateVerCd: item.mateVerCd,
              qty: plan.allocateQty,
              wareAreaCd: plan.wareAreaCd,
              note: item.note || '',
              // 추가 필요한 필드들
              totalQty: item.totalQty,
              unit: getOriginalUnitCode(item.unit), // 화면 표시값을 원본 코드로 변환 (kg → g2)
              lotNo: item.lotNo,
              inboDt: item.inboDt,
              fcode: item.fcode,
              regi: currentUser, // 현재 로그인 사용자 emp_cd
              mateType: item.mateType // 자재타입 정보 추가
            }));
          } 
          // 기존 방식 - 단일 구역 적재 (wareAreaCd가 직접 설정된 경우)
          else {
            return [{
              mateInboCd: item.mateInboCd,
              mcode: item.mcode,
              mateVerCd: item.mateVerCd,
              qty: item.qty,
              wareAreaCd: item.wareAreaCd,
              note: item.note || '',
              totalQty: item.totalQty,
              unit: getOriginalUnitCode(item.unit), // 화면 표시값을 원본 코드로 변환 (kg → g2)
              lotNo: item.lotNo,
              inboDt: item.inboDt,
              fcode: item.fcode,
              regi: currentUser, // 현재 로그인 사용자 emp_cd
              mateType: item.mateType // 자재타입 정보 추가
            }];
          }
        }).flat(); // 중첩 배열을 평면화
        
        // 창고구역이 설정된 자재들만 처리
        const response = await processMateLoadingBatch(processData);
        
        // 정확한 완전/부분 적재 판단 (메시지 생성용)
        // 각 자재별로 완전/부분 적재 여부 정확히 판단
        const fullyProcessedItems = []; // 완전 적재된 항목들
        const partiallyProcessedItems = []; // 부분 적재된 항목들
        
        assignedItems.forEach(item => {
          const originalItem = mateLoadingList.value.find(original => 
            original.mateInboCd === item.mateInboCd
          );
          
          if (originalItem) {
            // 해당 자재의 총 적재량 계산
            let totalLoadedQty = 0;
            
            if (item.placementPlan && item.placementPlan.length > 0) {
              // 다중 구역 적재의 경우
              totalLoadedQty = item.placementPlan.reduce((sum, plan) => sum + plan.allocateQty, 0);
            } else {
              // 단일 구역 적재의 경우
              totalLoadedQty = item.qty || 0;
            }
            
            // 원본 자재의 남은 수량 계산
            const remainingQty = (originalItem.totalQty || 0) - totalLoadedQty;
            
            if (remainingQty <= 0) {
              // 완전 적재
              fullyProcessedItems.push({
                mateInboCd: item.mateInboCd,
                mateName: originalItem.mateName || originalItem.mname,
                totalLoadedQty: totalLoadedQty
              });
            } else {
              // 부분 적재
              partiallyProcessedItems.push({
                mateInboCd: item.mateInboCd,
                mateName: originalItem.mateName || originalItem.mname,
                loadedQty: totalLoadedQty,
                remainingQty: remainingQty
              });
            }
          }
        });
        
        // 선택된 항목 초기화
        selectedMateLoadings.value = [];
        
        // 정확한 완전/부분 적재 메시지 생성
        const fullyProcessedCount = fullyProcessedItems.length;
        const partiallyProcessedCount = partiallyProcessedItems.length;
        
        let resultMessage = '';
        if (unassignedItems.length > 0) {
          resultMessage = `적재 완료: 완전적재 ${fullyProcessedCount}건, 부분적재 ${partiallyProcessedCount}건 (${unassignedItems.length}건은 구역 미선택으로 제외)`;
        } else {
          if (partiallyProcessedCount > 0) {
            resultMessage = `적재 완료: 완전적재 ${fullyProcessedCount}건, 부분적재 ${partiallyProcessedCount}건`;
          } else {
            resultMessage = `${fullyProcessedCount}건 완전 적재 완료`;
          }
        }
        
        // 상세 정보 로그 출력
        if (fullyProcessedCount > 0) {
        }
        if (partiallyProcessedCount > 0) {
        }
        
        return {
          ...response.data,
          message: resultMessage, // 메시지로 덮어쓰기
          processedCount: assignedItems.length,
          fullyProcessedCount,
          partiallyProcessedCount,
          skippedCount: unassignedItems.length,
          fullyProcessedItems,
          partiallyProcessedItems
        };
        
      } catch (error) {
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
      return response.data.wslCode;
    } catch (error) {
      throw error;
    }
  };

  // ========== 창고 구역 선택 관련 신규 Actions ==========

  /**
   * 특정 공장의 창고 목록 조회
   */
  const fetchWarehousesByFactory = async (fcode) => {
    try {
      isLoading.value = true;
      const response = await getWarehousesByFactory(fcode);
      warehouseList.value = response.data;
      return warehouseList.value;
    } catch (error) {
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * 특정 창고의 구역 정보 조회 (층별)
   */
  const fetchWarehouseAreas = async (wcode, floor) => {
    try {
      isLoading.value = true;
      const response = await getWarehouseAreasWithStock(wcode, floor);
      warehouseAreas.value = response.data;
      return warehouseAreas.value;
    } catch (error) {
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * 창고구역코드 조회
   */
  const fetchWareAreaCode = async (wcode, areaRow, areaCol, areaFloor) => {
    try {
      const response = await getWareAreaCode(wcode, areaRow, areaCol, areaFloor);
      return response.data.wareAreaCd;
    } catch (error) {
      throw error;
    }
  };

  /**
   * 구역 적재 가능 여부 검증
   */
  const validateArea = async (wareAreaCd, mcode, allocateQty) => {
    try {
      const response = await validateAreaAllocation(wareAreaCd, mcode, allocateQty);
      areaValidationResult.value = response.data;
      return response.data;
    } catch (error) {
      throw error;
    }
  };

  /**
   * 동일한 자재가 적재된 다른 구역들 조회
   */
  const fetchSameMaterialAreas = async (mcode, fcode, excludeAreaCd = '') => {
    try {
      const response = await getSameMaterialAreas(mcode, fcode, excludeAreaCd);
      sameMaterialAreas.value = response.data;
      return sameMaterialAreas.value;
    } catch (error) {
      throw error;
    }
  };

  /**
   * 자재의 창고구역 설정 (임시 저장)
   */
  const setMaterialWarehouseArea = (mateInboCd, wareAreaCd) => {
    const material = mateLoadingList.value.find(item => item.mateInboCd === mateInboCd);
    if (material) {
      material.wareAreaCd = wareAreaCd;
    }
  };

  /**
   * 선택된 자재들의 창고구역 설정 상태 확인
   */
  const checkWarehouseAreaAssignment = () => {
    const unassignedItems = selectedMateLoadings.value.filter(item => !item.wareAreaCd || item.wareAreaCd.trim() === '');
    return {
      isAllAssigned: unassignedItems.length === 0,
      unassignedCount: unassignedItems.length,
      unassignedItems: unassignedItems
    };
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
    // 창고 구역 관련 데이터도 초기화
    warehouseList.value = [];
    warehouseAreas.value = [];
    selectedWarehouseArea.value = null;
    areaValidationResult.value = {};
    sameMaterialAreas.value = [];
  };

  return {

    mateLoadingList,
    selectedMateLoadings,
    factoryList,
    isLoading,
    searchFilter,
    
    // 창고 구역 선택 관련 상태
    warehouseList,
    warehouseAreas,
    selectedWarehouseArea,
    areaValidationResult,
    sameMaterialAreas,
    
    // UI 설정
    searchColumns,
    tableColumns,
    
    // Computed
    hasSelectedItems,
    selectedCount,
    filteredMateLoadingList,
    
    // Actions
    fetchMateLoadingList,
    fetchFactoryList,
    processSingleLoading,
    processBatchLoading,
    fetchWslCodeByArea,
    setSelectedMateLoadings,
    setSearchFilter,
    clearSearchFilter,
    searchMateLoadings,
    resetData,

    // 창고 구역 선택 관련 Actions
    fetchWarehousesByFactory,
    fetchWarehouseAreas,
    fetchWareAreaCode,
    validateArea,
    fetchSameMaterialAreas,
    setMaterialWarehouseArea,
    checkWarehouseAreaAssignment
  };
});