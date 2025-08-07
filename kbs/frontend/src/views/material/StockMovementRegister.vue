<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { useRouter } from 'vue-router';
import { useStockMovementStore } from '@/stores/stockMovementStore';
import { useMemberStore } from '@/stores/memberStore';
import { useCommonStore } from '@/stores/commonStore';
import { useToast } from 'primevue/usetoast';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import MultipleSelectModal from '@/components/kimbap/modal/multipleselect.vue';
import LocationSelectModal from './LocationSelectModal.vue';
import { getPendingMoveRequestPlacements } from '@/api/materials';

// Store 및 기본 설정
const stockMovementStore = useStockMovementStore();
const memberStore = useMemberStore();
const commonStore = useCommonStore();
const router = useRouter();
const toast = useToast();

// Store에서 상태 가져오기
const { user } = storeToRefs(memberStore);

// 반응형 상태
const headerFormData = ref({});
const itemTableData = ref([]);
const selectedItems = ref([]);
const itemSelectModalVisible = ref(false);
const locationSelectModalVisible = ref(false);
const currentRowForLocation = ref(null);
const isInitialized = ref(false);
const inputFormRef = ref(null);

// 품목 선택 모달용 데이터
const availableItems = ref([]);
const selectedItemsFromModal = ref([]);

// 이동요청 중인 자재 배치 정보 (d1 상태)
const pendingMoveRequestPlacements = ref([]);

// 자재 선택 모달 컬럼 설정
const itemModalColumns = computed(() => [
  { field: 'itemCode', header: '자재번호', type: 'readonly' },
  { field: 'itemName', header: '자재명', type: 'readonly' },
  { field: 'lotNo', header: 'LOT번호', type: 'readonly' },
  { field: 'facName', header: '공장', type: 'readonly' },
  { field: 'areaLocation', header: '구역', type: 'readonly' },
  { field: 'qty', header: '재고수량', type: 'readonly', align: 'right' },
  { field: 'moveQty', header: '이동수량', type: 'input', inputType: 'number', align: 'right', placeholder: '수량 입력', required: true }
]);

// 헤더 폼 필드 설정
const headerFormFields = computed(() => [
  { key: 'reqDt', label: '요청일', type: 'readonly' },
  { key: 'requName', label: '요청자', type: 'readonly' },
  { 
    key: 'moveType', 
    label: '이동유형', 
    type: 'dropdown',
    required: true,
    options: [
      { label: '내부', value: 'z1' },
      { label: '외부', value: 'z2' }
    ]
  },
  { key: 'moveRea', label: '이동사유', type: 'input', required: true, placeholder: '이동사유를 입력하세요' },
  { key: 'note', label: '비고', type: 'input', placeholder: '비고사항을 입력하세요' }
]);

// 자재 테이블 컬럼 설정
const itemTableColumns = computed(() => [
  { field: 'itemCode', header: '자재코드', type: 'readonly' },
  { field: 'itemName', header: '자재명', type: 'readonly' },
  { field: 'lotNo', header: 'LOT번호', type: 'readonly' },
  { field: 'moveQty', header: '수량', type: 'readonly', align: 'right' },
  { field: 'unitText', header: '단위', type: 'readonly' },
  { field: 'depaLocation', header: '출발위치', type: 'readonly' },
  { field: 'arrLocation', header: '도착위치', type: 'readonly', width: '250px' },
  { 
    field: 'locationSelect', 
    header: '위치선택', 
    type: 'button', 
    buttonLabel: '선택',
    buttonIcon: 'pi pi-map-marker',
    buttonEvent: 'locationSelect',
    width: '100px'
  }
]);

// 컴포넌트 마운트 시 초기화
onMounted(async () => {
  if (!isInitialized.value) {
    initializeHeaderForm();
    isInitialized.value = true;
  }
  await loadInitialData();
});

// 헤더 폼 초기화
const initializeHeaderForm = () => {
  const now = new Date();
  const today = formatDate(now);
  
  headerFormData.value = {
    reqDt: today,
    requ: user.value?.empCd || '',
    requName: user.value?.empName || '',
    moveType: 'z1',
    moveRea: '',
    note: ''
  };
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

// 초기 데이터 로드
const loadInitialData = async () => {
  try {
    await Promise.all([
      commonStore.fetchCommonCodes('0G'), // 단위코드
      commonStore.fetchCommonCodes('0H')  // 품목유형코드
    ]);

    if (!stockMovementStore.factoryList || stockMovementStore.factoryList.length === 0) {
      await stockMovementStore.fetchLocationData('factory');
    }

    await loadAvailableItems();
    
    // 이동요청 중인 자재 배치 정보는 필수가 아니므로 별도로 로드
    loadPendingMoveRequestPlacements().catch(() => {
      // 에러 발생해도 무시 (이미 함수 내부에서 처리됨)
    });
    
  } catch (error) {
    toast.add({
      severity: 'error',
      summary: '초기화 실패',
      detail: '페이지 초기화 중 오류가 발생했습니다.',
      life: 3000
    });
  }
};

// 사용 가능한 품목 데이터 로드
const loadAvailableItems = async () => {
  try {
    const factories = stockMovementStore.factoryList || [];
    const allItems = [];
    let failedFactories = [];
    
    for (const factory of factories) {
      try {
        await stockMovementStore.fetchAvailableItems(factory.fcode, 'material');
        const materials = (stockMovementStore.availableMaterials || []).map(item => ({
          ...item,
          id: `${item.mcode}_${item.lotNo}_${item.wareAreaCd}`,
          itemType: item.mateType || 'h1',
          itemCode: item.mcode,
          itemName: item.mateName,
          mateVerCd: item.mateVerCd || item.mate_ver_cd || '001',
          areaLocation: item.wareAreaCd || `${item.areaRow || ''}${item.areaCol || ''}` || '미지정'
        }));
        
        allItems.push(...materials);
      } catch (error) {
        if (!failedFactories.includes(factory.facName)) {
          failedFactories.push(factory.facName);
        }
      }
    }
    
    let processedItems = convertItemTypeCodes(allItems);
    processedItems = convertUnitCodes(processedItems);
    
    availableItems.value = processedItems;
    
    // 실패한 공장이 있을 때만 알림
    if (failedFactories.length > 0) {
      toast.add({
        severity: 'warn',
        summary: '일부 데이터 로드 실패',
        detail: `${failedFactories.join(', ')} 공장의 데이터를 로드할 수 없습니다.`,
        life: 3000
      });
    }
    
  } catch (error) {
    toast.add({
      severity: 'error',
      summary: '데이터 로드 실패',
      detail: '자재 데이터를 불러오는데 실패했습니다.',
      life: 3000
    });
  }
};

// 헤더 폼 데이터 변경 처리 - 개선된 버전
const handleHeaderDataChange = (newData) => {
  // 완전히 빈 객체인 경우만 무시
  if (!newData || Object.keys(newData).length === 0) {
    return;
  }
  
  // readonly 필드들은 항상 보존
  const protectedFields = ['reqDt', 'requ', 'requName'];
  const currentData = { ...headerFormData.value };
  
  // 모든 필드 업데이트 (빈 값도 허용)
  Object.entries(newData).forEach(([key, value]) => {
    currentData[key] = value;
  });
  
  // readonly 필드 보호 - 기존 값이 있으면 덮어쓰지 않음
  protectedFields.forEach(field => {
    if (headerFormData.value[field] && (newData[field] === '' || newData[field] === null || newData[field] === undefined)) {
      currentData[field] = headerFormData.value[field];
    }
  });
  
  headerFormData.value = currentData;
};

// 품목 테이블 데이터 변경 처리
const handleItemDataChange = (newData) => {
  // 전체 목록 교체 방지
};

// 체크박스 선택 변경 처리
const handleSelectionChange = (newSelection) => {
  selectedItems.value = newSelection;
};

// 자재 추가 버튼 클릭 - 강화된 버전
const handleAddItem = () => {
  // 모달 열기 전에 모든 자재의 수량을 먼저 0으로 초기화
  availableItems.value = availableItems.value.map(item => ({
    ...item,
    moveQty: 0
  }));
  
  // 그 다음 목록에 있는 자재만 수량 복원
  syncModalQuantityWithTable();
  
  // 모달 선택 상태 초기화
  selectedItemsFromModal.value = [];
  
  // 모달 열기
  itemSelectModalVisible.value = true;
};

// 자재 선택 모달에서 자재 선택 완료 - 수정된 버전
const handleItemSelect = (selectedItemsFromModal) => {
  try {
    // 체크된 자재들만 처리
    if (!selectedItemsFromModal || selectedItemsFromModal.length === 0) {
      toast.add({
        severity: 'warn',
        summary: '선택 필요',
        detail: '추가할 자재를 선택해주세요.',
        life: 3000
      });
      // 모달 닫기 전에 목록 기준으로 수량 동기화
      syncModalQuantityWithTable();
      return;
    }

    let hasError = false;
    const errorMessages = [];
    
    selectedItemsFromModal.forEach(item => {
      if (!item.moveQty || item.moveQty <= 0) {
        errorMessages.push(`${item.itemName}: 이동수량 입력 필요`);
        hasError = true;
      } else if (item.moveQty > item.qty) {
        errorMessages.push(`${item.itemName}: 수량 초과 (재고: ${item.qty})`);
        hasError = true;
      }
    });

    if (hasError) {
      toast.add({
        severity: 'warn',
        summary: '입력 확인 필요',
        detail: errorMessages.join(', '),
        life: 4000
      });
      // 에러 발생 시에도 목록 기준으로 수량 동기화
      syncModalQuantityWithTable();
      return;
    }

    // 현재 테이블에 있는 자재들의 키 저장 (기존 목록 보존용)
    const existingTableKeys = new Set(
      itemTableData.value.map(existing => `${existing.itemCode}_${existing.lotNo}_${existing.depaAreaCd}`)
    );
    
    // 새로 선택된 자재들의 키
    const selectedItemKeys = new Set(
      selectedItemsFromModal.map(item => `${item.itemCode}_${item.lotNo}_${item.wareAreaCd}`)
    );
    
    // 체크된 자재들만 처리 (기존 목록은 건드리지 않음)
    selectedItemsFromModal.forEach(item => {
      const existingItemIndex = itemTableData.value.findIndex(existing => 
        existing.itemCode === item.itemCode && 
        existing.lotNo === item.lotNo && 
        existing.depaAreaCd === item.wareAreaCd
      );
      
      if (existingItemIndex === -1) {
        // 새로운 자재 추가 (기존 목록에 없는 경우만)
        const newItem = {
          id: Date.now() + Math.random(),
          itemType: item.itemType,
          itemTypeText: item.typeText || getDefaultItemTypeText(item.itemType),
          itemCode: item.itemCode,
          itemName: item.itemName,
          lotNo: item.lotNo,
          currentStock: item.qty,
          moveQty: item.moveQty,
          unit: item.unit,
          unitText: item.unitText || convertSingleUnitText(item.unit),
          depaAreaCd: item.wareAreaCd,
          depaWareCd: item.wcode || null,
          depaLocation: `${item.facName} / ${item.wareName} / ${item.wareAreaCd}`,
          arrAreaCd: '',
          arrWareCd: null,
          arrLocation: '선택해주세요',
          locationSelect: '',
          mcode: item.mcode,
          mateVerCd: item.mateVerCd,
          pcode: item.pcode,
          prodVerCd: item.prodVerCd,
          fcode: item.fcode,
          facName: item.facName,
          mateName: item.itemName,
          stoCon: item.stoCon
        };
        
        itemTableData.value.push(newItem);
      } else {
        // 기존 자재 수량 업데이트
        itemTableData.value[existingItemIndex].moveQty = item.moveQty;
      }
    });

    // 모달 확인 후 즉시 수량 동기화 (목록에 없는 자재들의 수량을 0으로 초기화)
    syncModalQuantityWithTable();
    
    // 성공 시에만 알림
    toast.add({
      severity: 'success',
      summary: '자재 처리 완료',
      detail: `${selectedItemsFromModal.length}개 자재가 선택되었습니다.`,
      life: 2000
    });
    
    // 모달 닫기
    itemSelectModalVisible.value = false;
    
  } catch (error) {
    // 에러 발생 시에도 목록 기준으로 수량 동기화
    syncModalQuantityWithTable();
    
    toast.add({
      severity: 'error',
      summary: '자재 처리 실패',
      detail: '자재 처리 중 오류가 발생했습니다.',
      life: 3000
    });
  }
};

// 모달의 수량을 목록과 동기화하는 함수 - 강화된 버전
const syncModalQuantityWithTable = () => {
  const currentTableItems = new Set();
  const currentTableQtyMap = new Map();
  
  // 현재 테이블에 있는 자재들의 키와 수량을 저장
  itemTableData.value.forEach(tableItem => {
    const key = `${tableItem.itemCode}_${tableItem.lotNo}_${tableItem.depaAreaCd}`;
    currentTableItems.add(key);
    currentTableQtyMap.set(key, tableItem.moveQty);
  });
  
  // availableItems의 moveQty를 강제로 동기화
  // - 목록에 있는 자재: 목록의 수량 유지
  // - 목록에 없는 자재: 무조건 0으로 설정
  availableItems.value = availableItems.value.map(item => {
    const key = `${item.itemCode}_${item.lotNo}_${item.wareAreaCd}`;
    return {
      ...item,
      moveQty: currentTableItems.has(key) ? currentTableQtyMap.get(key) : 0
    };
  });
};

// 도착위치 선택 버튼 클릭
const handleLocationSelect = async (rowData) => {
  if (!rowData.moveQty || rowData.moveQty <= 0) {
    toast.add({
      severity: 'warn',
      summary: '수량 입력 필요',
      detail: '이동수량을 먼저 입력해주세요.',
      life: 3000
    });
    return;
  }
  
  if (rowData.moveQty > rowData.currentStock) {
    toast.add({
      severity: 'warn',
      summary: '수량 초과',
      detail: `이동수량이 현재재고(${rowData.currentStock})를 초과할 수 없습니다.`,
      life: 3000
    });
    return;
  }
  
  // 위치 선택 모달을 열기 전에 최신 이동요청 정보를 다시 로드 (실패해도 진행)
  try {
    await loadPendingMoveRequestPlacements();
  } catch (error) {
    console.warn('위치 선택 시 이동요청 정보 로드 실패, 계속 진행:', error);
  }
  
  currentRowForLocation.value = {
    ...rowData,
    totalQty: rowData.moveQty
  };
  locationSelectModalVisible.value = true;
};

// 도착위치 선택 완료
const handleLocationSelectConfirm = (locationData) => {
  if (currentRowForLocation.value && locationData) {
    const targetRowIndex = itemTableData.value.findIndex(item => 
      item.id === currentRowForLocation.value.id
    );
    
    if (targetRowIndex !== -1) {
      const firstPlan = locationData.placementPlan && locationData.placementPlan[0];
      
      if (firstPlan) {
        const updatedItem = {
          ...itemTableData.value[targetRowIndex],
          arrAreaCd: firstPlan.wareAreaCd,
          arrLocation: `${locationData.facName} / ${locationData.wareName} / ${firstPlan.wareAreaCd}`,
          arrFcode: locationData.fcode,
          arrWcode: locationData.wcode,
          arrWareCd: locationData.wcode
        };
        
        itemTableData.value.splice(targetRowIndex, 1, updatedItem);
        
        // 성공 알림 제거 (UI에서 변경사항을 바로 확인 가능)
      } else {
        toast.add({
          severity: 'error',
          summary: '도착위치 설정 실패',
          detail: '배치 계획 정보가 없습니다.',
          life: 3000
        });
      }
    } else {
      toast.add({
        severity: 'error',
        summary: '도착위치 설정 실패',
        detail: '대상 행을 찾을 수 없습니다.',
        life: 3000
      });
    }
  }
  
  currentRowForLocation.value = null;
  locationSelectModalVisible.value = false;
};

// 기존 배치 정보 가져오기 (다른 자재들이 이미 선택한 위치들 + 이동요청 중인 자재들)
const getExistingPlacements = () => {
  // 현재 목록에서 다른 자재들의 배치 정보 (현재 세션에서 등록 중)
  const currentPlacements = itemTableData.value
    .filter(item => item.arrAreaCd && item.itemCode !== currentRowForLocation.value?.itemCode)
    .map(item => ({
      wareAreaCd: item.arrAreaCd,
      itemCode: item.itemCode,
      itemName: item.itemName,
      mcode: item.mcode,
      moveQty: item.moveQty,
      facName: item.facName,
      source: 'current' // 현재 등록 중인 자재
    }));

  // 이동요청 중인 자재들의 배치 정보 (DB에 저장된 d1 상태 요청들)
  const pendingPlacements = pendingMoveRequestPlacements.value
    .filter(item => item.wareAreaCd) // 도착구역이 설정된 것들만
    .map(item => ({
      wareAreaCd: item.wareAreaCd,  // arrAreaCd에서 매핑됨
      itemCode: item.itemCode,
      itemName: item.itemName,
      mcode: item.mcode,
      moveQty: item.moveQty,
      facName: item.facName,
      moveReqCd: item.moveReqCd,
      mrdCode: item.mrdCode,
      lotNo: item.lotNo,
      source: 'pending' // 이동요청 중인 자재 (DB의 d1 상태)
    }));

  console.log('현재 등록 중인 배치:', currentPlacements);
  console.log('이동요청 중인 배치:', pendingPlacements);

  return [...currentPlacements, ...pendingPlacements];
};

// 이동요청 중인 자재 배치 정보 로드 (move_status = 'd1')
const loadPendingMoveRequestPlacements = async () => {
  try {
    const response = await getPendingMoveRequestPlacements();
    
    // API 응답 데이터를 적절한 형태로 매핑
    // MyBatis camelCase 변환: move_req_cd -> moveReqCd, mate_name -> mateName 등
    pendingMoveRequestPlacements.value = (response.data || []).map(item => ({
      wareAreaCd: item.arrAreaCd,           // 도착구역코드 -> wareAreaCd로 매핑
      itemCode: item.mcode,                 // 자재마스터코드
      itemName: item.mateName,              // 자재명 (camelCase 변환됨)
      mcode: item.mcode,                    // 자재마스터코드
      moveQty: item.moveQty,                // 이동수량 (camelCase 변환됨)
      facName: item.facName || '',          // 공장명 (camelCase 변환됨)
      moveReqCd: item.moveReqCd,            // 이동요청코드 (camelCase 변환됨)
      lotNo: item.lotNo,                    // LOT번호 (camelCase 변환됨)
      itemType: item.itemType,              // 품목유형 (camelCase 변환됨)
      unit: item.unit,                      // 단위
      depaAreaCd: item.depaAreaCd,          // 출발구역코드 (camelCase 변환됨)
      arrAreaCd: item.arrAreaCd,            // 도착구역코드 (camelCase 변환됨)
      moveStatus: item.moveStatus || 'd1'   // 이동상태 (camelCase 변환됨)
    }));
    
    console.log('이동요청 중인 자재 배치 정보 로드 완료:', pendingMoveRequestPlacements.value.length, '개');
    console.log('로드된 데이터:', pendingMoveRequestPlacements.value);
  } catch (error) {
    console.error('이동요청 중인 자재 배치 정보 로드 실패:', error);
    pendingMoveRequestPlacements.value = [];
    
    // 구체적인 에러 정보 로깅
    if (error.response) {
      console.error('HTTP 상태:', error.response.status);
      console.error('응답 데이터:', error.response.data);
      console.error('요청 URL:', error.config?.url);
    }
    
    toast.add({
      severity: 'warn',
      summary: '일부 기능 제한',
      detail: '이동요청 중인 자재 정보를 불러올 수 없어 위치 충돌 검사가 제한됩니다.',
      life: 4000
    });
  }
};

// 폼 초기화 함수
const resetForm = () => {
  // 헤더 폼 초기화
  initializeHeaderForm();
  
  // 자재 목록 초기화
  itemTableData.value = [];
  selectedItems.value = [];
  
  // 모달 상태 초기화
  selectedItemsFromModal.value = [];
  itemSelectModalVisible.value = false;
  locationSelectModalVisible.value = false;
  currentRowForLocation.value = null;
  
  // availableItems의 수량 초기화
  availableItems.value = availableItems.value.map(item => ({
    ...item,
    moveQty: 0
  }));
  
  // 이동요청 중인 자재 배치 정보 다시 로드 (실패해도 폼 초기화는 계속)
  loadPendingMoveRequestPlacements().catch(() => {
    console.warn('폼 초기화 시 이동요청 정보 로드 실패');
  });
  
  toast.add({
    severity: 'info',
    summary: '폼 초기화',
    detail: '새로운 이동요청서를 작성할 수 있습니다.',
    life: 2000
  });
};

// 선택된 자재 제거 - 강화된 버전
const handleRemoveSelectedItems = () => {
  if (selectedItems.value.length === 0) {
    toast.add({
      severity: 'warn',
      summary: '선택 필요',
      detail: '제거할 자재를 선택해주세요.',
      life: 2000
    });
    return;
  }
  
  const selectedIds = selectedItems.value.map(item => item.id);
  itemTableData.value = itemTableData.value.filter(item => !selectedIds.includes(item.id));
  selectedItems.value = [];
  
  // 목록에서 제거된 자재들의 모달 수량을 즉시 0으로 동기화
  syncModalQuantityWithTable();
};

// 저장 처리
const handleSave = async () => {
  try {
    if (!validateForm()) {
      return;
    }
    
    const header = {
      moveType: headerFormData.value.moveType,
      moveRea: headerFormData.value.moveRea,
      note: headerFormData.value.note || '',
      requ: user.value?.empCd || headerFormData.value.requ
    };
    
    const details = itemTableData.value.map(item => ({
      mcode: item.mcode || null,
      mateVerCd: item.mateVerCd || item.mate_ver_cd || '001',
      pcode: item.pcode || null,
      prodVerCd: item.prodVerCd || null,
      itemType: item.itemType,
      lotNo: item.lotNo,
      moveQty: item.moveQty,
      unit: item.unit,
      depaAreaCd: item.depaAreaCd,
      arrAreaCd: item.arrAreaCd,
      depaWareCd: item.depaWareCd || null,
      arrWareCd: item.arrWcode || null,
      itemCode: item.itemCode,
      itemName: item.itemName
    }));
    
    toast.add({
      severity: 'info',
      summary: '등록 중',
      detail: '이동요청서를 등록하고 있습니다...',
      life: 2000
    });
    
    const result = await stockMovementStore.registerNewMoveRequest(header, details);
    
    toast.add({
      severity: 'success',
      summary: '등록 완료',
      detail: `이동요청서가 성공적으로 등록되었습니다. (${result.moveReqCd || '요청번호 생성됨'})`,
      life: 5000
    });
    
    // 페이지 이동 대신 폼 초기화 후 최신 이동요청 정보 로드
    resetForm();
    
  } catch (error) {
    let errorMessage = '이동요청서 등록 중 오류가 발생했습니다.';
    
    if (error.response) {
      errorMessage = error.response.data?.message || error.response.data?.detail || errorMessage;
    }
    
    toast.add({
      severity: 'error',
      summary: '등록 실패',
      detail: errorMessage,
      life: 5000
    });
  }
};

// 폼 유효성 검증
const validateForm = () => {
  if (!headerFormData.value.moveType) {
    toast.add({
      severity: 'warn',
      summary: '입력 확인',
      detail: '이동유형을 선택해주세요.',
      life: 3000
    });
    return false;
  }
  
  if (!headerFormData.value.moveRea || headerFormData.value.moveRea.trim() === '') {
    toast.add({
      severity: 'warn',
      summary: '입력 확인',
      detail: '이동사유를 입력해주세요.',
      life: 3000
    });
    return false;
  }
  
  if (itemTableData.value.length === 0) {
    toast.add({
      severity: 'warn',
      summary: '자재 선택 필요',
      detail: '이동할 자재를 추가해주세요.',
      life: 3000
    });
    return false;
  }
  
  for (let i = 0; i < itemTableData.value.length; i++) {
    const item = itemTableData.value[i];
    
    if (!item.moveQty || item.moveQty <= 0) {
      toast.add({
        severity: 'warn',
        summary: '수량 입력 필요',
        detail: `${i + 1}번째 자재(${item.itemName})의 이동수량을 입력해주세요.`,
        life: 3000
      });
      return false;
    }
    
    if (item.moveQty > item.currentStock) {
      toast.add({
        severity: 'warn',
        summary: '수량 초과',
        detail: `${i + 1}번째 자재(${item.itemName})의 이동수량이 현재재고를 초과합니다.`,
        life: 3000
      });
      return false;
    }
    
    if (!item.arrAreaCd || item.arrAreaCd.trim() === '') {
      toast.add({
        severity: 'warn',
        summary: '도착위치 선택 필요',
        detail: `${i + 1}번째 자재(${item.itemName})의 도착위치를 선택해주세요.`,
        life: 3000
      });
      return false;
    }
    
    if (!item.itemCode || !item.lotNo) {
      toast.add({
        severity: 'error',
        summary: '데이터 오류',
        detail: `${i + 1}번째 자재의 필수 정보가 누락되었습니다.`,
        life: 3000
      });
      return false;
    }
  }
  
  return true;
};

// 공통코드 형변환 함수들
const convertItemTypeCodes = (list) => {
  const itemTypeCodes = commonStore.getCodes('0H');
  
  return list.map(item => {
    const matchedType = itemTypeCodes.find(code => code.dcd === item.itemType);
    
    return {
      ...item,
      typeText: matchedType ? matchedType.cdInfo : getDefaultItemTypeText(item.itemType)
    };
  });
};

const convertUnitCodes = (list) => {
  const unitCodes = commonStore.getCodes('0G');
  
  return list.map(item => {
    const matchedUnit = unitCodes.find(code => code.dcd === item.unit);
    
    return {
      ...item,
      unitText: matchedUnit ? matchedUnit.cdInfo : item.unit
    };
  });
};

const getDefaultItemTypeText = (itemType) => {
  switch(itemType) {
    case 'h1': return '원자재';
    case 'h2': return '부자재';
    case 'h3': return '완제품';
    default: return itemType || '기타';
  }
};

const convertSingleUnitText = (unit) => {
  const unitCodes = commonStore.getCodes('0G') || [];
  const unitCode = unitCodes.find(code => code.dcd === unit);
  return unitCode ? unitCode.cdInfo : unit;
};

// selectedItems 변경 감지
watch(selectedItems, (newSelection) => {
  // Store 상태 동기화 등 필요 시 처리
}, { deep: true });

// itemTableData 변경 감지하여 모달 수량 자동 동기화
watch(itemTableData, () => {
  // 목록이 변경될 때마다 모달의 수량을 자동으로 동기화
  syncModalQuantityWithTable();
}, { deep: true });
</script>

<template>
<div class="space-y-4">
  <!-- 기본정보 입력 폼 -->
  <div class="mb-6">
    <InputForm
      ref="inputFormRef"
      :columns="headerFormFields"
      :data="headerFormData"
      title="기본정보"
      :buttons="{ 
        save: { show: true, label: '등록', icon: 'pi pi-save' },
        reset: { show: false },
        delete: { show: false },
        load: { show: false }
      }"
      @update:data="handleHeaderDataChange"
      @submit="handleSave"
    />
  </div>

  <!-- 자재 목록 테이블 -->
  <div>
    <InputTable
      :data="itemTableData"
      :columns="itemTableColumns"
      title="자재 목록"
      v-model:selection="selectedItems"
      :dataKey="'id'"
      :selectionMode="'multiple'"
      :enableSelection="true"
      :enableRowActions="false"
      :scrollHeight="'400px'"
      :showRowCount="true"
      :buttons="{ 
        save: { show: false },
        reset: { show: false },
        delete: { show: true, label: '제거', severity: 'danger' },
        load: { show: true, label: '추가', severity: 'info' }
      }"
      @dataChange="handleItemDataChange"
      @locationSelect="handleLocationSelect"
      @delete="handleRemoveSelectedItems"
      @load="handleAddItem"
    />
  </div>

  <!-- 자재 선택 모달 -->
  <MultipleSelectModal
    v-model:visible="itemSelectModalVisible"
    v-model:modelValue="selectedItemsFromModal"
    :items="availableItems"
    :itemKey="'id'"
    :columns="itemModalColumns"
    @update:modelValue="handleItemSelect"
    :style="{ '--modal-width': '90vw', '--modal-height': '80vh' }"
  />

  <!-- 도착위치 선택 모달 -->
  <LocationSelectModal
    v-model:visible="locationSelectModalVisible"
    :selectedMaterial="currentRowForLocation"
    :loadingQuantity="currentRowForLocation?.moveQty || 0"
    :existingPlacements="getExistingPlacements()"
    @confirm="handleLocationSelectConfirm"
  />
</div>
</template>

<style scoped>
.space-y-4 > :not([hidden]) ~ :not([hidden]) {
 margin-top: 1rem;
}

.mb-6 {
 margin-bottom: 1.5rem;
}
</style>

<style>
/* 강제로 모든 다이얼로그 크기 변경 */
.p-dialog-mask .p-dialog {
  width: 90vw !important;
  max-width: 1200px !important;
}

.p-dialog .p-dialog-content {
  height: calc(85vh - 120px) !important;
  overflow: auto !important;
}

.p-datatable-wrapper {
  height: 400px !important;
  overflow: auto !important;
}
</style>