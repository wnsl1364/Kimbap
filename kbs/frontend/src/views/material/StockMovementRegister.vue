<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useStockMovementStore } from '@/stores/stockMovementStore';
import { useMemberStore } from '@/stores/memberStore';
import { useToast } from 'primevue/usetoast';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import ItemSelectModal from "./ItemSelectModal.vue"
import LocationSelectModal from './LocationSelectModal.vue';

// Store 및 기본 설정
const stockMovementStore = useStockMovementStore();
const memberStore = useMemberStore();
const router = useRouter();
const toast = useToast();

// 반응형 상태
const headerFormData = ref({});
const itemTableData = ref([]);
const selectedItems = ref([]);
const itemSelectModalVisible = ref(false);
const locationSelectModalVisible = ref(false);
const currentRowForLocation = ref(null);

// 사용자 정보
const { user } = computed(() => memberStore);

// 컴포넌트 마운트 시 초기화
onMounted(async () => {
  initializeHeaderForm();
  await loadInitialData();
});

// 헤더 폼 초기화
const initializeHeaderForm = () => {
  const now = new Date();
  const today = now.toISOString().split('T')[0];
  
  headerFormData.value = {
    moveReqCd: '', // 자동 생성됨
    reqDt: today,
    requ: user.value?.empCd || '',
    requName: user.value?.empName || '시스템',
    moveType: 'z1', // 기본값: 내부
    moveRea: '',
    note: ''
  };
};

// 초기 데이터 로드
const loadInitialData = async () => {
  try {
    // 필요한 초기 데이터 로드 (공장 목록 등)
    console.log('이동요청서 등록 페이지 초기화 완료');
  } catch (error) {
    console.error('초기 데이터 로드 실패:', error);
    toast.add({
      severity: 'error',
      summary: '초기화 실패',
      detail: '페이지 초기화 중 오류가 발생했습니다.',
      life: 3000
    });
  }
};

// 헤더 폼 필드 설정
const headerFormFields = computed(() => [
  { key: 'moveReqCd', label: '이동요청번호', type: 'readonly', placeholder: '자동 생성됩니다' },
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
  itemTableData.value = newData;
};

// 품목 추가 버튼 클릭
const handleAddItem = () => {
  itemSelectModalVisible.value = true;
};

// 품목 선택 모달에서 품목 선택 완료
const handleItemSelect = (selectedItemsFromModal) => {
  try {
    selectedItemsFromModal.forEach(item => {
      // 이미 추가된 품목인지 확인
      const existingItem = itemTableData.value.find(existing => 
        existing.itemCode === item.itemCode && existing.lotNo === item.lotNo
      );
      
      if (!existingItem) {
        const newItem = {
          id: Date.now() + Math.random(), // 고유 ID
          itemType: item.itemType || (item.mcode ? 'h1' : 'h3'),
          itemTypeText: convertItemTypeText(item.itemType || (item.mcode ? 'h1' : 'h3')),
          itemCode: item.mcode || item.pcode,
          itemName: item.mateName || item.prodName,
          lotNo: item.lotNo,
          currentStock: item.currentStock || item.qty,
          moveQty: 0,
          unit: item.unit,
          unitText: stockMovementStore.convertUnitText(item.unit),
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
    console.error('품목 추가 실패:', error);
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
  
  currentRowForLocation.value = rowData;
  locationSelectModalVisible.value = true;
};

// 도착위치 선택 완료
const handleLocationSelectConfirm = (locationData) => {
  if (currentRowForLocation.value && locationData) {
    // 선택된 행의 도착위치 정보 업데이트
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
    // 유효성 검증
    if (!validateForm()) {
      return;
    }
    
    // 헤더 데이터 준비
    const header = {
      ...headerFormData.value,
      requ: user.value?.empCd || headerFormData.value.requ
    };
    
    // 상세 데이터 준비
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
    
    // 등록 처리
    const result = await stockMovementStore.registerNewMoveRequest(header, details);
    
    toast.add({
      severity: 'success',
      summary: '등록 완료',
      detail: `이동요청서가 등록되었습니다. (${result.moveReqCd})`,
      life: 3000
    });
    
    // 목록 페이지로 이동
    router.push('/stock-movement/list');
    
  } catch (error) {
    console.error('이동요청서 등록 실패:', error);
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
  // 헤더 검증
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
  
  // 품목 목록 검증
  if (itemTableData.value.length === 0) {
    toast.add({
      severity: 'warn',
      summary: '품목 선택 필요',
      detail: '이동할 품목을 추가해주세요.',
      life: 3000
    });
    return false;
  }
  
  // 각 품목의 필수 정보 검증
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

// 초기화
const handleReset = () => {
  initializeHeaderForm();
  itemTableData.value = [];
  selectedItems.value = [];
  
  toast.add({
    severity: 'info',
    summary: '초기화 완료',
    detail: '입력 내용이 초기화되었습니다.',
    life: 3000
  });
};

// 목록으로 돌아가기
const handleGoToList = () => {
  router.push('/stock-movement/list');
};

// 유틸리티 함수들
const convertItemTypeText = (itemType) => {
  switch(itemType) {
    case 'h1': return '원자재';
    case 'h2': return '부자재';
    case 'h3': return '완제품';
    default: return itemType;
  }
};
</script>

<template>
  <div class="space-y-4">
    <!-- 페이지 헤더 -->
    <div class="flex justify-between items-center">
      <h1 class="text-2xl font-bold">이동요청서 등록</h1>
      <div class="flex gap-2">
        <Button 
          label="목록" 
          icon="pi pi-list" 
          severity="secondary" 
          @click="handleGoToList"
        />
        <Button 
          label="초기화" 
          icon="pi pi-refresh" 
          severity="secondary" 
          @click="handleReset"
        />
        <Button 
          label="저장" 
          icon="pi pi-save" 
          severity="success" 
          @click="handleSave"
        />
      </div>
    </div>

    <!-- 기본정보 입력 폼 -->
    <div class="mb-6">
      <InputForm
        :columns="headerFormFields"
        :data="headerFormData"
        title="기본정보"
        :buttons="{ 
          save: { show: false },
          reset: { show: false },
          delete: { show: false },
          load: { show: false }
        }"
        @update:data="handleHeaderDataChange"
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
        :enableRowActions="true"
        :scrollHeight="'400px'"
        :showRowCount="true"
        :buttons="{ 
          save: { show: false },
          reset: { show: false },
          delete: { show: false },
          load: { show: false }
        }"
        @dataChange="handleItemDataChange"
        @locationSelect="handleLocationSelect"
      >
        <template #top-buttons>
          <Button 
            label="품목 추가" 
            icon="pi pi-plus" 
            severity="info" 
            @click="handleAddItem"
          />
          <Button 
            v-if="selectedItems.length > 0"
            :label="`${selectedItems.length}개 제거`"
            icon="pi pi-trash" 
            severity="danger" 
            @click="handleRemoveSelectedItems"
          />
        </template>
      </InputTable>
    </div>

    <!-- 품목 선택 모달 -->
    <ItemSelectModal
      v-model:visible="itemSelectModalVisible"
      @confirm="handleItemSelect"
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