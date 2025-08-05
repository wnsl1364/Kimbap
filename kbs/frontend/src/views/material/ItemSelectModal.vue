<script setup>
import { ref, onMounted, watch, computed } from 'vue';
import { useStockMovementStore } from '@/stores/stockMovementStore';
import { useToast } from 'primevue/usetoast';
import Dialog from 'primevue/dialog';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import InputText from 'primevue/inputtext';

// Props 및 Emits
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:visible', 'confirm']);

// Store 및 Toast
const stockMovementStore = useStockMovementStore();
const toast = useToast();

// 반응형 상태
const isVisible = ref(false);
const selectedFactory = ref('');
const itemType = ref('material'); // 'material' or 'product'
const searchKeyword = ref('');
const availableItems = ref([]);
const selectedItems = ref([]);
const isLoading = ref(false);

// 공장 목록
const factoryOptions = ref([]);

// Props 변화 감지
watch(() => props.visible, (newVal) => {
  isVisible.value = newVal;
  if (newVal) {
    initializeModal();
  }
});

// 모달 표시 상태 변화 감지
watch(isVisible, (newVal) => {
  if (!newVal) {
    emit('update:visible', false);
    resetModal();
  }
});

// 공장 선택 변화 감지
watch(selectedFactory, (newFactory) => {
  if (newFactory) {
    fetchAvailableItems();
  }
});

// 품목 유형 변화 감지
watch(itemType, () => {
  if (selectedFactory.value) {
    fetchAvailableItems();
  }
});

// 필터링된 품목 목록
const filteredItems = computed(() => {
  if (!searchKeyword.value) {
    return availableItems.value;
  }
  
  const keyword = searchKeyword.value.toLowerCase();
  return availableItems.value.filter(item => 
    (item.mateName && item.mateName.toLowerCase().includes(keyword)) ||
    (item.prodName && item.prodName.toLowerCase().includes(keyword)) ||
    (item.mcode && item.mcode.toLowerCase().includes(keyword)) ||
    (item.pcode && item.pcode.toLowerCase().includes(keyword)) ||
    (item.lotNo && item.lotNo.toLowerCase().includes(keyword))
  );
});

// 테이블 컬럼 설정
const tableColumns = computed(() => {
  if (itemType.value === 'material') {
    return [
      { field: 'mcode', header: '자재코드' },
      { field: 'mateName', header: '자재명' },
      { field: 'mateType', header: '자재유형' },
      { field: 'lotNo', header: 'LOT번호' },
      { field: 'qty', header: '재고수량', align: 'right' },
      { field: 'unit', header: '단위' },
      { field: 'stoCon', header: '보관조건' },
      { field: 'wareLocation', header: '보관위치' }
    ];
  } else {
    return [
      { field: 'pcode', header: '제품코드' },
      { field: 'prodName', header: '제품명' },
      { field: 'prodType', header: '제품유형' },
      { field: 'lotNo', header: 'LOT번호' },
      { field: 'qty', header: '재고수량', align: 'right' },
      { field: 'unit', header: '단위' },
      { field: 'wareLocation', header: '보관위치' }
    ];
  }
});

// 품목 유형 옵션
const itemTypeOptions = [
  { label: '자재', value: 'material' },
  { label: '제품', value: 'product' }
];

// 컴포넌트 마운트 시 초기화
onMounted(async () => {
  await loadFactoryList();
});

// 공장 목록 로드
const loadFactoryList = async () => {
  try {
    await stockMovementStore.fetchLocationData('factory');
    factoryOptions.value = stockMovementStore.factoryList.map(factory => ({
      label: factory.facName,
      value: factory.fcode
    }));
  } catch (error) {
    console.error('공장 목록 로드 실패:', error);
    toast.add({
      severity: 'error',
      summary: '로드 실패',
      detail: '공장 목록을 불러오는데 실패했습니다.',
      life: 3000
    });
  }
};

// 모달 초기화
const initializeModal = async () => {
  selectedItems.value = [];
  searchKeyword.value = '';
  
  // 기본값 설정
  if (factoryOptions.value.length > 0 && !selectedFactory.value) {
    selectedFactory.value = factoryOptions.value[0].value;
  }
};

// 사용 가능한 품목 조회
const fetchAvailableItems = async () => {
  if (!selectedFactory.value) return;
  
  try {
    isLoading.value = true;
    
    await stockMovementStore.fetchAvailableItems(selectedFactory.value, itemType.value);
    
    if (itemType.value === 'material') {
      availableItems.value = stockMovementStore.availableMaterials.map(item => ({
        ...item,
        wareLocation: `${item.facName} - ${item.wareName} - ${item.areaRow}${item.areaCol}`,
        currentStock: item.qty,
        itemCode: item.mcode,
        itemName: item.mateName
      }));
    } else {
      availableItems.value = stockMovementStore.availableProducts.map(item => ({
        ...item,
        wareLocation: `${item.facName} - ${item.wareName} - ${item.areaRow}${item.areaCol}`,
        currentStock: item.qty,
        itemCode: item.pcode,
        itemName: item.prodName
      }));
    }
    
    console.log('품목 목록 조회 완료:', availableItems.value.length, '건');
    
  } catch (error) {
    console.error('품목 목록 조회 실패:', error);
    toast.add({
      severity: 'error',
      summary: '조회 실패',
      detail: '품목 목록 조회 중 오류가 발생했습니다.',
      life: 3000
    });
  } finally {
    isLoading.value = false;
  }
};

// 확인 버튼 클릭
const handleConfirm = () => {
  if (selectedItems.value.length === 0) {
    toast.add({
      severity: 'warn',
      summary: '선택 필요',
      detail: '이동할 품목을 선택해주세요.',
      life: 3000
    });
    return;
  }
  
  emit('confirm', [...selectedItems.value]);
  handleClose();
};

// 취소/닫기
const handleClose = () => {
  isVisible.value = false;
};

// 모달 리셋
const resetModal = () => {
  selectedItems.value = [];
  availableItems.value = [];
  searchKeyword.value = '';
};

// 행 클릭 처리 (디버깅용)
const handleRowClick = (event) => {
  console.log('행 클릭:', event.data);
};

// 품목 유형별 스타일 클래스
const getItemTypeClass = (type) => {
  switch(type) {
    case 'h1': return 'bg-blue-100 text-blue-800';
    case 'h2': return 'bg-green-100 text-green-800';
    case 'h3': return 'bg-purple-100 text-purple-800';
    default: return 'bg-gray-100 text-gray-800';
  }
};

// 품목 유형 텍스트 변환
const convertItemTypeText = (type) => {
  switch(type) {
    case 'h1': return '원자재';
    case 'h2': return '부자재';
    case 'h3': return '완제품';
    default: return type;
  }
};

// 보관조건 텍스트 변환
const convertStorageConditionText = (condition) => {
  switch(condition) {
    case 'o1': return '상온';
    case 'o2': return '냉장';
    case 'o3': return '냉동';
    default: return condition;
  }
};

// 단위 텍스트 변환
const convertUnitText = (unit) => {
  return stockMovementStore.convertUnitText(unit);
};
</script>

<template>
  <Dialog
    v-model:visible="isVisible"
    modal
    :closable="true"
    :style="{ width: '80vw', maxWidth: '1200px' }"
    class="p-fluid"
  >
    <template #header>
      <div class="flex align-items-center gap-2">
        <i class="pi pi-shopping-cart text-primary"></i>
        <span class="font-bold text-xl">품목 선택</span>
      </div>
    </template>

    <!-- 검색 필터 영역 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-4 p-4 bg-gray-50 rounded-lg">
      <!-- 공장 선택 -->
      <div class="flex flex-col gap-2">
        <label class="font-medium text-gray-700">공장 선택</label>
        <Dropdown
          v-model="selectedFactory"
          :options="factoryOptions"
          optionLabel="label"
          optionValue="value"
          placeholder="공장을 선택하세요"
          class="w-full"
        />
      </div>

      <!-- 품목 유형 선택 -->
      <div class="flex flex-col gap-2">
        <label class="font-medium text-gray-700">품목 유형</label>
        <Dropdown
          v-model="itemType"
          :options="itemTypeOptions"
          optionLabel="label"
          optionValue="value"
          placeholder="유형을 선택하세요"
          class="w-full"
        />
      </div>

      <!-- 검색 키워드 -->
      <div class="flex flex-col gap-2">
        <label class="font-medium text-gray-700">검색</label>
        <InputText
          v-model="searchKeyword"
          placeholder="품목명, 코드, LOT번호 검색"
          class="w-full"
        />
      </div>

      <!-- 조회 버튼 -->
      <div class="flex flex-col gap-2">
        <label class="font-medium text-gray-700">&nbsp;</label>
        <Button
          label="조회"
          icon="pi pi-search"
          @click="fetchAvailableItems"
          :loading="isLoading"
          class="w-full"
        />
      </div>
    </div>

    <!-- 품목 목록 테이블 -->
    <div class="mb-4">
      <div class="flex justify-between items-center mb-3">
        <h3 class="text-lg font-semibold">
          품목 목록 ({{ filteredItems.length }}건)
        </h3>
        <div class="text-sm text-gray-600">
          선택된 품목: {{ selectedItems.length }}개
        </div>
      </div>

      <DataTable
        :value="filteredItems"
        v-model:selection="selectedItems"
        dataKey="id"
        selectionMode="multiple"
        :scrollable="true"
        scrollHeight="400px"
        :loading="isLoading"
        showGridlines
        responsiveLayout="scroll"
        @rowClick="handleRowClick"
      >
        <!-- 선택 체크박스 -->
        <Column selectionMode="multiple" headerStyle="width: 3rem"></Column>

        <!-- 동적 컬럼 생성 -->
        <Column
          v-for="col in tableColumns"
          :key="col.field"
          :field="col.field"
          :header="col.header"
          :headerClass="col.align === 'right' ? 'text-right' : ''"
          :bodyClass="col.align === 'right' ? 'text-right' : ''"
        >
          <template #body="slotProps" v-if="col.field === 'mateType' || col.field === 'prodType'">
            <span :class="getItemTypeClass(slotProps.data[col.field])">
              {{ convertItemTypeText(slotProps.data[col.field]) }}
            </span>
          </template>
          <template #body="slotProps" v-else-if="col.field === 'stoCon'">
            {{ convertStorageConditionText(slotProps.data[col.field]) }}
          </template>
          <template #body="slotProps" v-else-if="col.field === 'unit'">
            {{ convertUnitText(slotProps.data[col.field]) }}
          </template>
          <template #body="slotProps" v-else-if="col.field === 'qty'">
            {{ slotProps.data[col.field]?.toLocaleString() }}
          </template>
        </Column>
      </DataTable>
    </div>

    <!-- 버튼 영역 -->
    <template #footer>
      <div class="flex justify-end gap-2">
        <Button
          label="취소"
          icon="pi pi-times"
          severity="secondary"
          @click="handleClose"
        />
        <Button
          label="확인"
          icon="pi pi-check"
          severity="success"
          @click="handleConfirm"
          :disabled="selectedItems.length === 0"
        />
      </div>
    </template>
  </Dialog>
</template>

<style scoped>
.grid {
  display: grid;
}

.grid-cols-1 {
  grid-template-columns: repeat(1, minmax(0, 1fr));
}

.grid-cols-4 {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.gap-2 {
  gap: 0.5rem;
}

.gap-4 {
  gap: 1rem;
}

.mb-3 {
  margin-bottom: 0.75rem;
}

.mb-4 {
  margin-bottom: 1rem;
}

.p-4 {
  padding: 1rem;
}

.bg-gray-50 {
  background-color: #f9fafb;
}

.rounded-lg {
  border-radius: 0.5rem;
}

.bg-blue-100 { background-color: #dbeafe; }
.text-blue-800 { color: #1e40af; }
.bg-green-100 { background-color: #dcfce7; }
.text-green-800 { color: #166534; }
.bg-purple-100 { background-color: #f3e8ff; }
.text-purple-800 { color: #6b21a8; }
.bg-gray-100 { background-color: #f3f4f6; }
.text-gray-800 { color: #1f2937; }

@media (min-width: 768px) {
  .md\:grid-cols-4 {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}
</style>