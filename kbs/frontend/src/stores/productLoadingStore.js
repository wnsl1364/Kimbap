import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { useCommonStore } from '@/stores/commonStore'; // 🔥 공통코드 변환용
import { useMemberStore } from '@/stores/memberStore'; // 🔥 사용자 정보용
import { 
  getWslCodeByArea,
  // 창고 구역 선택 관련 신규 함수들
  getWarehousesByFactory,
  getWarehouseAreasWithStock,
  getWareAreaCode,
} from '@/api/materials';
import {
  validateAreaAllocation,
  getSameProductAreas,
  getProdLoadingWaitList,
  getProdLoadingByInboCd,
  processProdLoadingSingle,
  processProdLoadingBatch,
  getFactoryList
} from '@/api/production';

export const useProductLoadingStore  = defineStore('mateLoading', () => {
  // ========== 기존 상태 관리 ==========
  const mateLoadingList = ref([]);           // 적재 대기 목록
  const prodLoadingList = ref([]);           // 적재 대기 제품 목록
  const selectedMateLoadings = ref([]);      // 선택된 자재들 (체크박스)
  const selectedProdLoadings = ref([]);      // 선택된 제품들 (체크박스)
  const factoryList = ref([]);               // 공장 목록 (드롭다운용)
  const isLoading = ref(false);              // 로딩 상태
  const searchFilter = ref({});              // 검색 필터
  

  // ========== 창고 구역 선택 관련 신규 상태 ==========
  const warehouseList = ref([]);             // 창고 목록
  const warehouseAreas = ref([]);            // 창고 구역 목록
  const selectedWarehouseArea = ref(null);   // 선택된 창고 구역
  const areaValidationResult = ref({});      // 구역 검증 결과
  const sameProductAreas = ref([]);          // 동일 제품 적재 구역들

  const searchColumns = ref([
    { 
      key: 'prodInboCd', 
      label: '입고번호', 
      type: 'text', 
      placeholder: '입고번호를 입력하세요' 
    },
    { 
      key: 'pcode', 
      label: '제품코드', 
      type: 'text', 
      placeholder: '제품코드를 입력하세요' 
    },
    { 
      key: 'prodName', 
      label: '제품명', 
      type: 'text', 
      placeholder: '제품명을 입력하세요' 
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
      key: 'inboDt', 
      label: '입고일자', 
      type: 'dateRange',
      startPlaceholder: '시작일',
      endPlaceholder: '종료일'
    }
  ]);

  const tableColumns = ref([
    { field: 'prodInboCd', header: '입고번호', type: 'readonly' },
    { field: 'inboDt', header: '입고일자', type: 'readonly' },
    { field: 'pcode', header: '제품코드', type: 'readonly' },
    { field: 'prodName', header: '제품명', type: 'readonly' },
    { field: 'totalQty', header: '적재대기수량', type: 'readonly', align: 'right' },
    { field: 'qty', header: '적재수량', type: 'input', inputType: 'number', align: 'right' },
    { field: 'unit', header: '단위', type: 'readonly' },
    { field: 'stoTemp', header: '보관온도', type: 'readonly' },
    { field: 'lotNo', header: 'LOT번호', type: 'readonly' },
    { field: 'note', header: '비고', type: 'readonly' },
    { field: 'facName', header: '공장', type: 'readonly' },
    {
      field: 'locationSelect',
      header: '구역선택',
      type: 'button',
      buttonLabel: '구역선택',
      buttonIcon: 'pi pi-map-marker',
      buttonEvent: 'locationSelect'
    }
  ]);

  // ========== 기존 Computed ==========
  const hasSelectedItems = computed(() => selectedMateLoadings.value.length > 0);
  const selectedCount = computed(() => selectedMateLoadings.value.length);

  // ========== 기존 검색 기능 ==========
  const searchProdLoadings = (searchData) => {
    if (!searchData) {
      return prodLoadingList.value;
    }

    return prodLoadingList.value.filter(item => {
      // 입고번호 검색
      const matchProdInboCd = !searchData.prodInboCd || 
        item.prodInboCd?.toLowerCase().includes(searchData.prodInboCd.toLowerCase());
      
      // 제품코드 검색
      const matchPcode = !searchData.pcode || 
        item.pcode?.toLowerCase().includes(searchData.pcode.toLowerCase());
      
      // 제품명 검색
      const matchProdName = !searchData.prodName || 
        item.prodName?.includes(searchData.prodName);
      
      // 공장 검색
      const matchFcode = !searchData.fcode || item.fcode === searchData.fcode;
      
      // 입고일자 범위 검색
      let matchInboDt = true;
      if (searchData.inboDt?.start && searchData.inboDt?.end && item.inboDt) {
        const itemDate = new Date(item.inboDt);
        const startDate = new Date(searchData.inboDt.start);
        const endDate = new Date(searchData.inboDt.end);
        endDate.setHours(23, 59, 59, 999); // 하루 끝까지
        
        matchInboDt = itemDate >= startDate && itemDate <= endDate;
      }
      
      return matchProdInboCd && matchPcode && matchProdName && matchFcode && matchInboDt;
    });
  };

  // 필터링된 제품 목록 computed
  const filteredProdLoadingList = computed(() => {
    // 🔥 검색 필터가 실제로 값이 있는지 확인 (빈 문자열이나 null/undefined 제외)
    const hasValidFilter = searchFilter.value && 
      Object.keys(searchFilter.value).length > 0 &&
      Object.values(searchFilter.value).some(value => 
        value !== null && 
        value !== undefined && 
        value !== '' && 
        (Array.isArray(value) ? value.length > 0 : true)
      );
    
    if (!hasValidFilter) {
      return prodLoadingList.value;
    }
    return searchProdLoadings(searchFilter.value);
  });

  // ========== 기존 Actions ==========

  /**
   * 제품 적재 대기 목록 조회
   */
  const fetchProdLoadingList = async () => {
    try {
      isLoading.value = true;
      const response = await getProdLoadingWaitList();
      
      // 적재수량 초기값을 적재대기수량으로 설정
      prodLoadingList.value = response.data.map(item => ({
        ...item,
        qty: item.totalQty,     // 기본값으로 적재대기수량과 동일하게 설정
        id: item.prodInboCd,    // InputTable의 dataKey용
        wareAreaCd: ''          // 창고구역 초기값
      }));
      
      console.log('제품 적재 대기 목록 조회 완료:', prodLoadingList.value.length, '건');
    } catch (error) {
      console.error('제품 적재 대기 목록 조회 실패:', error);
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
      const response = await getFactoryList();
      factoryList.value = response.data;
      console.log('공장 목록 조회 완료:', factoryList.value.length, '개');
    } catch (error) {
      console.error('공장 목록 조회 실패:', error);
      throw error;
    }
  };

  /**
   * 단건 제품 적재 처리
   */
  const processSingleLoading = async (prodLoadingData) => {
    try {
      isLoading.value = true;
      const response = await processProdLoadingSingle(prodLoadingData);
      console.log('단건 제품 처리 완료:', response.data);
      
      // 목록에서 해당 항목 제거
      prodLoadingList.value = prodLoadingList.value.filter(
        item => item.prodInboCd !== prodLoadingData.prodInboCd
      );
      
      return response.data;
    } catch (error) {
      console.error('단건 제품 처리 실패:', error);
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  /**
 * 다중 제품 적재 처리 (적재처리 버튼)
 */
const processBatchLoading = async () => {
      if (selectedProdLoadings.value.length === 0) {
        throw new Error('적재할 제품를 선택해주세요.');
      }

      // 창고구역이 설정된 자재들만 필터링 (wareAreaCd 또는 placementPlan이 있는 경우)
      const assignedItems = selectedProdLoadings.value.filter(item => 
        (item.wareAreaCd && item.wareAreaCd.trim() !== '') ||
        (item.placementPlan && item.placementPlan.length > 0)
      );
      
      if (assignedItems.length === 0) {
        throw new Error('선택된 제품 중 창고구역이 설정된 제품이 없습니다. 먼저 구역을 선택해주세요.');
      }

      // 창고구역이 설정되지 않은 제품이 있으면 알림 (처리는 계속 진행)
      const unassignedItems = selectedProdLoadings.value.filter(item => 
        (!item.wareAreaCd || item.wareAreaCd.trim() === '') &&
        (!item.placementPlan || item.placementPlan.length === 0)
      );
      
      if (unassignedItems.length > 0) {
        console.log(`주의: ${unassignedItems.length}개 제품은 창고구역이 설정되지 않아 제외됩니다.`);
      }

      try {
        isLoading.value = true;
        
        // 🔥 현재 로그인 사용자 정보 가져오기
        const memberStore = useMemberStore();
        const currentUser = memberStore.user?.empCd || 'system';
        
        // 🔥 공통코드에서 단위 코드 변환을 위한 함수
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
              prodInboCd: item.prodInboCd,
              pcode: item.pcode,
              prodVerCd: item.prodVerCd,
              qty: plan.allocateQty,
              wareAreaCd: plan.wareAreaCd,
              note: item.note || '',
              // 추가 필요한 필드들
              totalQty: item.totalQty,
              unit: getOriginalUnitCode(item.unit), // 🔥 화면 표시값을 원본 코드로 변환 (kg → g2)
              lotNo: item.lotNo,
              inboDt: item.inboDt,
              fcode: item.fcode,
              regi: currentUser // 현재 로그인 사용자 emp_cd
            }));
          } 
          // 기존 방식 - 단일 구역 적재 (wareAreaCd가 직접 설정된 경우)
          else {
            return [{
              prodInboCd: item.prodInboCd,
              pcode: item.pcode,
              prodVerCd: item.prodVerCd,
              qty: item.qty,
              wareAreaCd: item.wareAreaCd,
              note: item.note || '',
              totalQty: item.totalQty,
              unit: getOriginalUnitCode(item.unit), // 🔥 화면 표시값을 원본 코드로 변환 (kg → g2)
              lotNo: item.lotNo,
              inboDt: item.inboDt,
              fcode: item.fcode,
              regi: currentUser // 현재 로그인 사용자 emp_cd
            }];
          }
        }).flat(); // 중첩 배열을 평면화
        
        console.log('백엔드로 전송할 적재 데이터:', processData);
        
        // 창고구역이 설정된 자재들만 처리
        const response = await processProdLoadingBatch(processData);
        console.log('다중 적재 처리 완료:', response.data);
        
        // 🔥 적재 후 수량 업데이트 로직 개선
        // 각 자재별로 적재된 수량을 계산하고 남은 수량 업데이트
        const processedResults = response.data.results || response.data; // 백엔드 응답 구조에 따라 조정
        
        assignedItems.forEach(item => {
          const originalItem = prodLoadingList.value.find(original => 
            original.prodInboCd === item.prodInboCd
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
            
            console.log(`자재 ${item.prodInboCd}: 원래수량=${originalItem.totalQty}, 적재량=${totalLoadedQty}, 남은수량=${remainingQty}`);
            
            if (remainingQty > 0) {
              // 부분 적재: 남은 수량으로 업데이트
              originalItem.totalQty = remainingQty;
              
              // 적재 계획 정보 초기화 (다시 구역 선택 필요)
              originalItem.placementPlan = null;
              originalItem.totalAllocated = null;
              originalItem.remainingQty = null;
              originalItem.wareAreaCd = null;
              originalItem.selectedAreaInfo = null;
              
              console.log(`부분 적재 완료: ${item.prodInboCd} - 남은 수량 ${remainingQty}으로 업데이트`);
            } else {
              // 완전 적재: 목록에서 제거
              const index = prodLoadingList.value.findIndex(listItem => 
                listItem.prodInboCd === item.prodInboCd
              );
              if (index > -1) {
                prodLoadingList.value.splice(index, 1);
                console.log(`완전 적재 완료: ${item.prodInboCd} - 목록에서 제거`);
              }
            }
          }
        });
        
        // 처리된 항목들을 selectedMateLoadings에서 제거 (완전 적재된 것만)
        const fullyProcessedIds = assignedItems.filter(item => {
          const originalItem = prodLoadingList.value.find(original => 
            original.prodInboCd === item.prodInboCd
          );
          return !originalItem; // 목록에서 제거된 = 완전 적재된 항목
        }).map(item => item.prodInboCd);
        
        selectedProdLoadings.value = selectedProdLoadings.value.filter(item => 
          !fullyProcessedIds.includes(item.prodInboCd)
        );
        
        // 🔥 결과 메시지에 부분/완전 적재 상세 포함
        const fullyProcessedCount = fullyProcessedIds.length;
        const partiallyProcessedCount = assignedItems.length - fullyProcessedCount;
        
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
        
        return {
          ...response.data,
          message: resultMessage,
          processedCount: assignedItems.length,
          fullyProcessedCount,
          partiallyProcessedCount,
          skippedCount: unassignedItems.length
        };
        
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

  // ========== 창고 구역 선택 관련 신규 Actions ==========

  /**
   * 특정 공장의 창고 목록 조회
   */
  const fetchWarehousesByFactory = async (fcode) => {
    try {
      isLoading.value = true;
      const response = await getWarehousesByFactory(fcode);
      warehouseList.value = response.data;
      console.log('공장별 창고 목록 조회 완료:', fcode, '-', warehouseList.value.length, '개');
      return warehouseList.value;
    } catch (error) {
      console.error('공장별 창고 목록 조회 실패:', error);
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
      console.log('창고 구역 정보 조회 완료:', wcode, floor + '층', '-', warehouseAreas.value.length, '개');
      return warehouseAreas.value;
    } catch (error) {
      console.error('창고 구역 정보 조회 실패:', error);
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
      console.log('창고구역코드 조회 완료:', response.data.wareAreaCd);
      return response.data.wareAreaCd;
    } catch (error) {
      console.error('창고구역코드 조회 실패:', error);
      throw error;
    }
  };

  /**
   * 구역 제품 적재 가능 여부 검증
   */
  const validateArea = async (wareAreaCd, pcode, allocateQty) => {
    try {
      const response = await validateAreaAllocation(wareAreaCd, pcode, allocateQty);
      areaValidationResult.value = response.data;
      console.log('구역 검증 완료:', response.data.message);
      return response.data;
    } catch (error) {
      console.error('구역 검증 실패:', error);
      throw error;
    }
  };

  /**
   * 동일한 제품이 적재된 다른 구역들 조회
   */
  const fetchSameProductAreas = async (pcode, fcode, excludeAreaCd = '') => {
    try {
      const response = await getSameProductAreas(pcode, fcode, excludeAreaCd);
      sameProductAreas.value = response.data;
      console.log('동일 제품 적재 구역 조회 완료:', sameProductAreas.value.length, '개');
      return sameProductAreas.value;
    } catch (error) {
      console.error('동일 제품 적재 구역 조회 실패:', error);
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
      console.log('자재 창고구역 설정:', mateInboCd, '->', wareAreaCd);
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
    console.log('검색 필터 초기화됨');
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
    sameProductAreas,
    
    // UI 설정
    searchColumns,
    tableColumns,
    
    // Computed
    hasSelectedItems,
    selectedCount,
    filteredProdLoadingList,        // 필터링된 제품 목록
    
    // Actions
    fetchProdLoadingList,           // 제품 적재 대기 목록 조회
    fetchFactoryList,               // 공장 목록 조회(드롭다운에서 사용)
    processSingleLoading,
    processBatchLoading,
    fetchWslCodeByArea,
    setSelectedMateLoadings,
    setSearchFilter,
    clearSearchFilter,
    searchProdLoadings,             // 제품 적재 대기 목록 필터링
    resetData,

    // 창고 구역 선택 관련 Actions
    fetchWarehousesByFactory,
    fetchWarehouseAreas,
    fetchWareAreaCode,
    validateArea,                    // 제품 구역 적재 가능 여부 검증
    fetchSameProductAreas,           // 동일한 제품이 적재된 다른 구역들 조회
    setMaterialWarehouseArea,
    checkWarehouseAreaAssignment
  };
});