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

const loadInitialData = async () => {
  try {
    await Promise.all([
      commonStore.fetchCommonCodes('0G'),
      commonStore.fetchCommonCodes('0H')
    ]);

    if (!stockMovementStore.factoryList?.length) {
      await stockMovementStore.fetchLocationData('factory');
    }

    await loadAvailableItems();
    loadPendingMoveRequestPlacements().catch(() => {});
    
  } catch (error) {
    toast.add({
      severity: 'error',
      summary: '초기화 실패',
      detail: '페이지 초기화 중 오류가 발생했습니다.',
      life: 3000
    });
  }
};

const loadAvailableItems = async () => {
  try {
    const factories = stockMovementStore.factoryList || [];
    const allItems = [];
    const failedFactories = [];
    
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
    
    availableItems.value = convertUnitCodes(convertItemTypeCodes(allItems));
    
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

const handleHeaderDataChange = (newData) => {
  if (!newData || Object.keys(newData).length === 0) return;
  
  const protectedFields = ['reqDt', 'requ', 'requName'];
  const currentData = { ...headerFormData.value };
  
  Object.entries(newData).forEach(([key, value]) => {
    currentData[key] = value;
  });
  
  protectedFields.forEach(field => {
    if (headerFormData.value[field] && (newData[field] === '' || newData[field] == null)) {
      currentData[field] = headerFormData.value[field];
    }
  });
  
  headerFormData.value = currentData;
};

const handleItemDataChange = () => {};

const handleSelectionChange = (newSelection) => {
  selectedItems.value = newSelection;
};

const handleAddItem = () => {
  availableItems.value = availableItems.value.map(item => ({
    ...item,
    moveQty: 0
  }));
  
  syncModalQuantityWithTable();
  selectedItemsFromModal.value = [];
  itemSelectModalVisible.value = true;
};

const handleItemSelect = (selectedItemsFromModal) => {
  try {
    if (!selectedItemsFromModal?.length) {
      toast.add({
        severity: 'warn',
        summary: '선택 필요',
        detail: '추가할 자재를 선택해주세요.',
        life: 3000
      });
      syncModalQuantityWithTable();
      return;
    }

    const errorMessages = [];
    selectedItemsFromModal.forEach(item => {
      if (!item.moveQty || item.moveQty <= 0) {
        errorMessages.push(`${item.itemName}: 이동수량 입력 필요`);
      } else if (item.moveQty > item.qty) {
        errorMessages.push(`${item.itemName}: 수량 초과 (재고: ${item.qty})`);
      }
    });

    if (errorMessages.length) {
      toast.add({
        severity: 'warn',
        summary: '입력 확인 필요',
        detail: errorMessages.join(', '),
        life: 4000
      });
      syncModalQuantityWithTable();
      return;
    }

    selectedItemsFromModal.forEach(item => {
      const existingItemIndex = itemTableData.value.findIndex(existing => 
        existing.itemCode === item.itemCode && 
        existing.lotNo === item.lotNo && 
        existing.depaAreaCd === item.wareAreaCd
      );
      
      if (existingItemIndex === -1) {
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
        itemTableData.value[existingItemIndex].moveQty = item.moveQty;
      }
    });

    syncModalQuantityWithTable();
    
    toast.add({
      severity: 'success',
      summary: '자재 처리 완료',
      detail: `${selectedItemsFromModal.length}개 자재가 선택되었습니다.`,
      life: 2000
    });
    
    itemSelectModalVisible.value = false;
    
  } catch (error) {
    syncModalQuantityWithTable();
    
    toast.add({
      severity: 'error',
      summary: '자재 처리 실패',
      detail: '자재 처리 중 오류가 발생했습니다.',
      life: 3000
    });
  }
};

const syncModalQuantityWithTable = () => {
  const currentTableItems = new Set();
  const currentTableQtyMap = new Map();
  
  itemTableData.value.forEach(tableItem => {
    const key = `${tableItem.itemCode}_${tableItem.lotNo}_${tableItem.depaAreaCd}`;
    currentTableItems.add(key);
    currentTableQtyMap.set(key, tableItem.moveQty);
  });
  
  availableItems.value = availableItems.value.map(item => {
    const key = `${item.itemCode}_${item.lotNo}_${item.wareAreaCd}`;
    return {
      ...item,
      moveQty: currentTableItems.has(key) ? currentTableQtyMap.get(key) : 0
    };
  });
};

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
  
  try {
    await loadPendingMoveRequestPlacements();
  } catch (error) {}
  
  currentRowForLocation.value = {
    ...rowData,
    totalQty: rowData.moveQty
  };
  locationSelectModalVisible.value = true;
};

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
  const currentPlacements = itemTableData.value
    .filter(item => item.arrAreaCd && item.itemCode !== currentRowForLocation.value?.itemCode)
    .map(item => ({
      wareAreaCd: item.arrAreaCd,
      itemCode: item.itemCode,
      itemName: item.itemName,
      mcode: item.mcode,
      moveQty: item.moveQty,
      unit: item.unit,
      unitText: item.unitText,
      facName: item.facName,
      source: 'current'
    }));

  const pendingPlacements = pendingMoveRequestPlacements.value
    .filter(item => item.wareAreaCd)
    .map(item => ({
      wareAreaCd: item.wareAreaCd,
      itemCode: item.itemCode,
      itemName: item.itemName,
      mcode: item.mcode,
      moveQty: item.moveQty,
      unit: item.unit,
      unitText: convertSingleUnitText(item.unit),
      facName: item.facName,
      moveReqCd: item.moveReqCd,
      mrdCode: item.mrdCode,
      lotNo: item.lotNo,
      source: 'pending'
    }));

  return [...currentPlacements, ...pendingPlacements];
};

const loadPendingMoveRequestPlacements = async () => {
  try {
    const response = await getPendingMoveRequestPlacements();
    
    pendingMoveRequestPlacements.value = (response.data || []).map(item => ({
      wareAreaCd: item.arrAreaCd,
      itemCode: item.mcode,
      itemName: item.mateName,
      mcode: item.mcode,
      moveQty: item.moveQty,
      facName: item.facName || '',
      moveReqCd: item.moveReqCd,
      lotNo: item.lotNo,
      itemType: item.itemType,
      unit: item.unit,
      depaAreaCd: item.depaAreaCd,
      arrAreaCd: item.arrAreaCd,
      moveStatus: item.moveStatus || 'd1'
    }));
    
  } catch (error) {
    pendingMoveRequestPlacements.value = [];
    
    toast.add({
      severity: 'warn',
      summary: '일부 기능 제한',
      detail: '이동요청 중인 자재 정보를 불러올 수 없어 위치 충돌 검사가 제한됩니다.',
      life: 4000
    });
  }
};

const resetForm = () => {
  initializeHeaderForm();
  itemTableData.value = [];
  selectedItems.value = [];
  selectedItemsFromModal.value = [];
  itemSelectModalVisible.value = false;
  locationSelectModalVisible.value = false;
  currentRowForLocation.value = null;
  
  availableItems.value = availableItems.value.map(item => ({
    ...item,
    moveQty: 0
  }));
  
  loadPendingMoveRequestPlacements().catch(() => {});
  
  toast.add({
    severity: 'info',
    summary: '폼 초기화',
    detail: '새로운 이동요청서를 작성할 수 있습니다.',
    life: 2000
  });
};

const handleRemoveSelectedItems = () => {
  if (!selectedItems.value.length) {
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
  syncModalQuantityWithTable();
};

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

const convertItemTypeCodes = (list) => {
  const itemTypeCodes = commonStore.getCodes('0H');
  return list.map(item => ({
    ...item,
    typeText: itemTypeCodes.find(code => code.dcd === item.itemType)?.cdInfo || getDefaultItemTypeText(item.itemType)
  }));
};

const convertUnitCodes = (list) => {
  const unitCodes = commonStore.getCodes('0G');
  return list.map(item => ({
    ...item,
    unitText: unitCodes.find(code => code.dcd === item.unit)?.cdInfo || item.unit
  }));
};

const getDefaultItemTypeText = (itemType) => {
  const types = { 'h1': '원자재', 'h2': '부자재', 'h3': '완제품' };
  return types[itemType] || itemType || '기타';
};

const convertSingleUnitText = (unit) => {
  const unitCodes = commonStore.getCodes('0G') || [];
  return unitCodes.find(code => code.dcd === unit)?.cdInfo || unit;
};

watch(selectedItems, (newSelection) => {}, { deep: true });

watch(itemTableData, () => {
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
      title="자재 이동 요청 목록"
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
  :dialogWidth="'80vw'"
  :tableHeight="'50vh'"
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
.p-dialog-mask .p-dialog {
  width: 90vw !important;
  max-width: 1200px !important;
}

.p-dialog .p-dialog-content {
  height: calc(70vh - 120px) !important;
  overflow: auto !important;
}

/* 전역 테이블 높이 강제 제거: 개별 컴포넌트에서 scrollHeight로 제어 */
</style>