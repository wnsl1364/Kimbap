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

// 품목 선택 모달용 데이터
const availableItems = ref([]);
const selectedItemsFromModal = ref([]);

// 컴포넌트 마운트 시 초기화
onMounted(async () => {
  initializeHeaderForm();
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
    // 공통코드 로드
    await Promise.all([
      commonStore.fetchCommonCodes('0G'), // 단위코드
      commonStore.fetchCommonCodes('0H'), // 품목유형코드
    ]);

    // 공장 목록 로드 (품목 선택 모달용)
    if (!stockMovementStore.factoryList || stockMovementStore.factoryList.length === 0) {
      await stockMovementStore.fetchLocationData('factory');
    }

    // 품목 데이터 로드
    await loadAvailableItems();
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
    
    for (const factory of factories) {
      try {
        // 자재 가져오기
        await stockMovementStore.fetchAvailableItems(factory.fcode, 'material');
        const materials = (stockMovementStore.availableMaterials || []).map(item => ({
          ...item,
          id: `${item.mcode}_${item.lotNo}_${item.wareAreaCd}`,
          itemType: item.mateType || 'h1',
          itemCode: item.mcode,
          itemName: item.mateName
        }));
        
        allItems.push(...materials);
      } catch (error) {
        console.error(`공장 ${factory.fcode} 자재 로드 실패:`, error);
      }
      
      // 제품 가져오기 (에러 처리)
      try {
        await stockMovementStore.fetchAvailableItems(factory.fcode, 'product');
        const products = (stockMovementStore.availableProducts || []).map(item => ({
          ...item,
          id: `${item.pcode}_${item.lotNo}_${item.wareAreaCd}`,
          itemType: item.prodType || 'h3',
          itemCode: item.pcode,
          itemName: item.prodName
        }));
        
        allItems.push(...products);
      } catch (error) {
        console.error(`공장 ${factory.fcode} 제품 로드 실패:`, error);
        // 제품 로드 실패해도 계속 진행
      }
    }
    
    // 공통코드 변환 적용
    let processedItems = convertItemTypeCodes(allItems);
    processedItems = convertUnitCodes(processedItems);
    
    availableItems.value = processedItems;
    console.log('품목 데이터 로드 완료:', processedItems.length, '건');
    
  } catch (error) {
    console.error('품목 데이터 로드 실패:', error);
    toast.add({
      severity: 'error',
      summary: '데이터 로드 실패',
      detail: '품목 데이터를 불러오는데 실패했습니다.',
      life: 3000
    });
  }
};

// 품목 선택 모달 컬럼 설정
const itemModalColumns = computed(() => [
  { field: 'itemCode', header: '품목코드' },
  { field: 'itemName', header: '품목명' },
  { field: 'typeText', header: '유형' },
  { field: 'lotNo', header: 'LOT번호' },
  { field: 'qty', header: '재고수량' },
  { field: 'unitText', header: '단위' },
  { field: 'facName', header: '공장' },
  { field: 'wareName', header: '창고' }
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

// 품목 테이블 컬럼 설정
const itemTableColumns = computed(() => [
  { field: 'itemTypeText', header: '품목유형', type: 'readonly' },
  { field: 'itemCode', header: '품목코드', type: 'readonly' },
  { field: 'itemName', header: '품목명', type: 'readonly' },
  { field: 'lotNo', header: 'LOT번호', type: 'readonly' },
  { field: 'currentStock', header: '현재재고', type: 'readonly', align: 'right' },
  { field: 'moveQty', header: '이동수량', type: 'input', inputType: 'number', align: 'right', placeholder: '수량 입력' },
  { field: 'unitText', header: '단위', type: 'readonly' },
  { field: 'depaLocation', header: '출발위치', type: 'readonly' },
  { 
    field: 'arrLocation', 
    header: '도착위치', 
    type: 'button', 
    buttonLabel: '위치선택',
    buttonIcon: 'pi pi-map-marker',
    buttonEvent: 'locationSelect',
    width: '120px'
  }
]);

// 헤더 폼 데이터 변경 처리
const handleHeaderDataChange = (newData) => {
  headerFormData.value = { ...newData };
};

// 품목 테이블 데이터 변경 처리
const handleItemDataChange = (newData) => {
  // 전체 목록 교체 방지
};

// 체크박스 선택 변경 처리
const handleSelectionChange = (newSelection) => {
  selectedItems.value = newSelection;
};

// 품목 추가 버튼 클릭
const handleAddItem = () => {
  console.log('품목 추가 버튼 클릭됨');
  try {
    selectedItemsFromModal.value = [];
    itemSelectModalVisible.value = true;
    console.log('모달 상태 변경:', itemSelectModalVisible.value);
  } catch (error) {
    console.error('모달 열기 중 오류:', error);
    toast.add({
      severity: 'error',
      summary: '모달 오류',
      detail: '품목 선택 모달을 여는 중 오류가 발생했습니다.',
      life: 3000
    });
  }
};

// 품목 선택 모달에서 품목 선택 완료
const handleItemSelect = (selectedItemsFromModal) => {
  try {
    selectedItemsFromModal.forEach(item => {
      const existingItem = itemTableData.value.find(existing => 
        existing.itemCode === item.itemCode && existing.lotNo === item.lotNo
      );
      
      if (!existingItem) {
        const newItem = {
          id: Date.now() + Math.random(),
          itemType: item.itemType,
          itemTypeText: item.typeText || getDefaultItemTypeText(item.itemType),
          itemCode: item.itemCode,
          itemName: item.itemName,
          lotNo: item.lotNo,
          currentStock: item.qty,
          moveQty: 0,
          unit: item.unit,
          unitText: item.unitText || convertSingleUnitText(item.unit),
          depaAreaCd: item.wareAreaCd,
          depaLocation: `${item.facName} - ${item.wareName} - ${item.areaRow}${item.areaCol}`,
          arrAreaCd: '',
          arrLocation: '선택해주세요',
          mcode: item.mcode,
          mateVerCd: item.mateVerCd,
          pcode: item.pcode,
          prodVerCd: item.prodVerCd
        };
        
        itemTableData.value.push(newItem);
      }
    });
    
    toast.add({
      severity: 'success',
      summary: '품목 추가 완료',
      detail: `${selectedItemsFromModal.length}개 품목이 추가되었습니다.`,
      life: 3000
    });
  } catch (error) {
    console.error('품목 추가 중 오류:', error);
    toast.add({
      severity: 'error',
      summary: '품목 추가 실패',
      detail: '품목 추가 중 오류가 발생했습니다.',
      life: 3000
    });
  }
};

// 도착위치 선택 버튼 클릭
const handleLocationSelect = (rowData) => {
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
  
  currentRowForLocation.value = {
    ...rowData,
    // 모달에서 사용할 정보 추가
    totalQty: rowData.moveQty
  };
  locationSelectModalVisible.value = true;
};

// 도착위치 선택 완료
const handleLocationSelectConfirm = (locationData) => {
  if (currentRowForLocation.value && locationData) {
    const targetRow = itemTableData.value.find(item => 
      item.id === currentRowForLocation.value.id
    );
    
    if (targetRow) {
      targetRow.arrAreaCd = locationData.wareAreaCd;
      targetRow.arrLocation = `${locationData.facName} - ${locationData.wareName} - ${locationData.areaName}`;
      targetRow.arrFcode = locationData.fcode;
      targetRow.arrWcode = locationData.wcode;
    }
    
    toast.add({
      severity: 'success',
      summary: '도착위치 선택 완료',
      detail: `도착위치가 설정되었습니다.`,
      life: 3000
    });
  }
  
  currentRowForLocation.value = null;
};

// 선택된 품목 제거
const handleRemoveSelectedItems = () => {
  if (selectedItems.value.length === 0) {
    toast.add({
      severity: 'warn',
      summary: '선택 필요',
      detail: '제거할 품목을 선택해주세요.',
      life: 3000
    });
    return;
  }
  
  const selectedIds = selectedItems.value.map(item => item.id);
  itemTableData.value = itemTableData.value.filter(item => !selectedIds.includes(item.id));
  selectedItems.value = [];
  
  toast.add({
    severity: 'success',
    summary: '품목 제거 완료',
    detail: `${selectedIds.length}개 품목이 제거되었습니다.`,
    life: 3000
  });
};

// 저장 처리
const handleSave = async () => {
  try {
    if (!validateForm()) return;
    
    const header = {
      moveType: headerFormData.value.moveType,
      moveRea: headerFormData.value.moveRea,
      note: headerFormData.value.note,
      requ: user.value?.empCd || headerFormData.value.requ
    };
    
    const details = itemTableData.value.map(item => ({
      mcode: item.mcode,
      mateVerCd: item.mateVerCd,
      pcode: item.pcode,
      prodVerCd: item.prodVerCd,
      itemType: item.itemType,
      lotNo: item.lotNo,
      moveQty: item.moveQty,
      unit: item.unit,
      depaAreaCd: item.depaAreaCd,
      arrAreaCd: item.arrAreaCd
    }));
    
    const result = await stockMovementStore.registerNewMoveRequest(header, details);
    
    toast.add({
      severity: 'success',
      summary: '등록 완료',
      detail: `이동요청서 등록완료 (${result.moveReqCd})`,
      life: 5000
    });
    
    router.push('/stock-movement/list');
    
  } catch (error) {
    toast.add({
      severity: 'error',
      summary: '등록 실패',
      detail: '이동요청서 등록 중 오류가 발생했습니다.',
      life: 3000
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
      summary: '품목 선택 필요',
      detail: '이동할 품목을 추가해주세요.',
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
        detail: `${i + 1}번째 품목의 이동수량을 입력해주세요.`,
        life: 3000
      });
      return false;
    }
    
    if (item.moveQty > item.currentStock) {
      toast.add({
        severity: 'warn',
        summary: '수량 초과',
        detail: `${i + 1}번째 품목의 이동수량이 현재재고를 초과합니다.`,
        life: 3000
      });
      return false;
    }
    
    if (!item.arrAreaCd) {
      toast.add({
        severity: 'warn',
        summary: '도착위치 선택 필요',
        detail: `${i + 1}번째 품목의 도착위치를 선택해주세요.`,
        life: 3000
      });
      return false;
    }
  }
  
  return true;
};


// 공통코드 형변환 함수들
const convertItemTypeCodes = (list) => {
  const itemTypeCodes = commonStore.getCodes('0H'); // 품목유형코드
  
  return list.map(item => {
    const matchedType = itemTypeCodes.find(code => code.dcd === item.itemType);
    
    return {
      ...item,
      typeText: matchedType ? matchedType.cdInfo : getDefaultItemTypeText(item.itemType)
    };
  });
};

const convertUnitCodes = (list) => {
  const unitCodes = commonStore.getCodes('0G'); // 단위코드
  
  return list.map(item => {
    const matchedUnit = unitCodes.find(code => code.dcd === item.unit);
    
    return {
      ...item,
      unitText: matchedUnit ? matchedUnit.cdInfo : item.unit
    };
  });
};

// 기본 품목유형 텍스트 (공통코드 로드 전 대체용)
const getDefaultItemTypeText = (itemType) => {
  switch(itemType) {
    case 'h1': return '원자재';
    case 'h2': return '부자재';
    case 'h3': return '완제품';
    default: return itemType || '기타';
  }
};

// 단일 아이템 단위 변환
const convertSingleUnitText = (unit) => {
  const unitCodes = commonStore.getCodes('0G') || [];
  const unitCode = unitCodes.find(code => code.dcd === unit);
  return unitCode ? unitCode.cdInfo : unit;
};

// selectedItems 변경 감지
watch(selectedItems, (newSelection) => {
  // Store 상태 동기화 등 필요 시 처리
}, { deep: true });
</script>

<template>
<div class="space-y-4">

  <!-- 기본정보 입력 폼 -->
  <div class="mb-6">
    <InputForm
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

  <!-- 품목 목록 테이블 -->
  <div>
    <InputTable
      :data="itemTableData"
      :columns="itemTableColumns"
      title="품목 목록"
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

  <!-- 품목 선택 모달 -->
  <MultipleSelectModal
    v-model:visible="itemSelectModalVisible"
    v-model:modelValue="selectedItemsFromModal"
    :items="availableItems"
    :itemKey="'id'"
    :columns="itemModalColumns"
    @update:modelValue="handleItemSelect"
  />

  <!-- 도착위치 선택 모달 -->
  <LocationSelectModal
    v-model:visible="locationSelectModalVisible"
    :selectedItem="currentRowForLocation"
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