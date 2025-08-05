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
import Card from 'primevue/card';

// Props 및 Emits
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  selectedItem: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['update:visible', 'confirm']);

// Store 및 Toast
const stockMovementStore = useStockMovementStore();
const toast = useToast();

// 반응형 상태
const isVisible = ref(false);
const selectedFactory = ref('');
const selectedWarehouse = ref('');
const selectedArea = ref(null);
const searchKeyword = ref('');
const isLoading = ref(false);

// 선택 옵션들
const factoryOptions = ref([]);
const warehouseOptions = ref([]);
const areaList = ref([]);

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
watch(selectedFactory, async (newFactory) => {
  if (newFactory) {
    selectedWarehouse.value = '';
    selectedArea.value = null;
    await fetchWarehouses();
  }
});

// 창고 선택 변화 감지
watch(selectedWarehouse, async (newWarehouse) => {
  if (newWarehouse) {
    selectedArea.value = null;
    await fetchAreas();
  }
});

// 필터링된 구역 목록
const filteredAreas = computed(() => {
  if (!searchKeyword.value) {
    return areaList.value;
  }
  
  const keyword = searchKeyword.value.toLowerCase();
  return areaList.value.filter(area => 
    (area.areaName && area.areaName.toLowerCase().includes(keyword)) ||
    (area.wareAreaCd && area.wareAreaCd.toLowerCase().includes(keyword)) ||
    (area.displayName && area.displayName.toLowerCase().includes(keyword))
  );
});

// 구역 테이블 컬럼 설정
const areaTableColumns = [
  { field: 'displayName', header: '구역명' },
  { field: 'areaFloor', header: '층' },
  { field: 'vol', header: '최대용량', align: 'right' },
  { field: 'currentVolume', header: '현재적재량', align: 'right' },
  { field: 'availableVolume', header: '잔여용량', align: 'right' },
  { field: 'statusText', header: '상태' }
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
      value: factory.fcode,
      data: factory
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

// 창고 목록 조회
const fetchWarehouses = async () => {
  if (!selectedFactory.value) return;
  
  try {
    isLoading.value = true;
    await stockMovementStore.fetchLocationData('warehouse', selectedFactory.value);
    
    warehouseOptions.value = stockMovementStore.warehouseList.map(warehouse => ({
      label: `${warehouse.wareName} (${warehouse.wareType})`,
      value: warehouse.wcode,
      data: warehouse
    }));
    
    console.log('창고 목록 조회 완료:', warehouseOptions.value.length, '개');
    
  } catch (error) {
    console.error('창고 목록 조회 실패:', error);
    toast.add({
      severity: 'error',
      summary: '조회 실패',
      detail: '창고 목록 조회 중 오류가 발생했습니다.',
      life: 3000
    });
  } finally {
    isLoading.value = false;
  }
};

// 구역 목록 조회
const fetchAreas = async () => {
  if (!selectedWarehouse.value) return;
  
  try {
    isLoading.value = true;
    await stockMovementStore.fetchLocationData('area', selectedWarehouse.value);
    
    // 구역 데이터 가공
    areaList.value = stockMovementStore.areaList.map(area => {
      const displayName = `${area.areaRow}${area.areaCol}`;
      const availableVolume = (area.vol || 0) - (area.currentVolume || 0);
      const isAvailable = availableVolume > 0;
      
      return {
        ...area,
        id: area.wareAreaCd, // DataTable의 dataKey용
        displayName: displayName,
        areaName: displayName,
        availableVolume: availableVolume,
        statusText: isAvailable ? '사용가능' : '사용불가',
        statusClass: isAvailable ? 'text-green-600' : 'text-red-600'
      };
    });
    
    console.log('구역 목록 조회 완료:', areaList.value.length, '개');
    
  } catch (error) {
    console.error('구역 목록 조회 실패:', error);
    toast.add({
      severity: 'error',
      summary: '조회 실패',
      detail: '구역 목록 조회 중 오류가 발생했습니다.',
      life: 3000
    });
  } finally {
    isLoading.value = false;
  }
};

// 모달 초기화
const initializeModal = async () => {
  selectedArea.value = null;
  searchKeyword.value = '';
  
  // 기본값 설정
  if (factoryOptions.value.length > 0 && !selectedFactory.value) {
    selectedFactory.value = factoryOptions.value[0].value;
  }
};

// 확인 버튼 클릭
const handleConfirm = () => {
  if (!selectedArea.value) {
    toast.add({
      severity: 'warn',
      summary: '선택 필요',
      detail: '도착위치를 선택해주세요.',
      life: 3000
    });
    return;
  }
  
  // 선택된 위치 정보 구성
  const selectedFactoryData = factoryOptions.value.find(f => f.value === selectedFactory.value);
  const selectedWarehouseData = warehouseOptions.value.find(w => w.value === selectedWarehouse.value);
  
  const locationData = {
    fcode: selectedFactory.value,
    facName: selectedFactoryData?.data.facName,
    wcode: selectedWarehouse.value,
    wareName: selectedWarehouseData?.data.wareName,
    wareAreaCd: selectedArea.value.wareAreaCd,
    areaName: selectedArea.value.displayName,
    areaRow: selectedArea.value.areaRow,
    areaCol: selectedArea.value.areaCol,
    areaFloor: selectedArea.value.areaFloor,
    availableVolume: selectedArea.value.availableVolume
  };
  
  emit('confirm', locationData);
  handleClose();
};

// 취소/닫기
const handleClose = () => {
  isVisible.value = false;
};

// 모달 리셋
const resetModal = () => {
  selectedArea.value = null;
  selectedWarehouse.value = '';
  warehouseOptions.value = [];
  areaList.value = [];
  searchKeyword.value = '';
};

// 구역 선택 처리
const handleAreaSelect = (area) => {
  selectedArea.value = area;
};

// 구역 더블클릭 처리 (바로 확인)
const handleAreaDoubleClick = (event) => {
  selectedArea.value = event.data;
  handleConfirm();
};

// 현재 선택된 품목 정보 표시용
const selectedItemInfo = computed(() => {
  if (!props.selectedItem) return null;
  
  return {
    itemName: props.selectedItem.itemName,
    itemCode: props.selectedItem.itemCode,
    lotNo: props.selectedItem.lotNo,
    moveQty: props.selectedItem.moveQty,
    unitText: props.selectedItem.unitText,
    depaLocation: props.selectedItem.depaLocation
  };
});

// 구역 상태에 따른 스타일 클래스
const getAreaStatusClass = (area) => {
  if (area.availableVolume <= 0) {
    return 'bg-red-50 border-red-200';
  } else if (area.availableVolume < 10) {
    return 'bg-yellow-50 border-yellow-200';
  } else {
    return 'bg-green-50 border-green-200';
  }
};

// 선택된 구역 확인
const isAreaSelected = (area) => {
  return selectedArea.value && selectedArea.value.wareAreaCd === area.wareAreaCd;
};
</script>

<template>
  <Dialog
    v-model:visible="isVisible"
    modal
    :closable="true"
    :style="{ width: '90vw', maxWidth: '1400px' }"
    class="p-fluid"
  >
    <template #header>
      <div class="flex align-items-center gap-2">
        <i class="pi pi-map-marker text-primary"></i>
        <span class="font-bold text-xl">도착위치 선택</span>
      </div>
    </template>

    <!-- 선택된 품목 정보 -->
    <Card v-if="selectedItemInfo" class="mb-4">
      <template #title>
        <div class="flex align-items-center gap-2">
          <i class="pi pi-box text-primary"></i>
          <span>선택된 품목 정보</span>
        </div>
      </template>
      <template #content>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
          <div>
            <label class="font-medium text-gray-600">품목명:</label>
            <div class="font-semibold">{{ selectedItemInfo.itemName }}</div>
          </div>
          <div>
            <label class="font-medium text-gray-600">품목코드:</label>
            <div class="font-semibold">{{ selectedItemInfo.itemCode }}</div>
          </div>
          <div>
            <label class="font-medium text-gray-600">LOT번호:</label>
            <div class="font-semibold">{{ selectedItemInfo.lotNo }}</div>
          </div>
          <div>
            <label class="font-medium text-gray-600">이동수량:</label>
            <div class="font-semibold text-blue-600">
              {{ selectedItemInfo.moveQty }} {{ selectedItemInfo.unitText }}
            </div>
          </div>
          <div class="md:col-span-4">
            <label class="font-medium text-gray-600">출발위치:</label>
            <div class="font-semibold text-green-600">{{ selectedItemInfo.depaLocation }}</div>
          </div>
        </div>
      </template>
    </Card>

    <!-- 위치 선택 영역 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4 p-4 bg-gray-50 rounded-lg">
      <!-- 공장 선택 -->
      <div class="flex flex-col gap-2">
        <label class="font-medium text-gray-700">공장 선택 *</label>
        <Dropdown
          v-model="selectedFactory"
          :options="factoryOptions"
          optionLabel="label"
          optionValue="value"
          placeholder="공장을 선택하세요"
          class="w-full"
          :disabled="isLoading"
        />
      </div>

      <!-- 창고 선택 -->
      <div class="flex flex-col gap-2">
        <label class="font-medium text-gray-700">창고 선택 *</label>
        <Dropdown
          v-model="selectedWarehouse"
          :options="warehouseOptions"
          optionLabel="label"
          optionValue="value"
          placeholder="창고를 선택하세요"
          class="w-full"
          :disabled="!selectedFactory || isLoading"
        />
      </div>

      <!-- 구역 검색 -->
      <div class="flex flex-col gap-2">
        <label class="font-medium text-gray-700">구역 검색</label>
        <InputText
          v-model="searchKeyword"
          placeholder="구역명으로 검색"
          class="w-full"
          :disabled="!selectedWarehouse"
        />
      </div>
    </div>

    <!-- 구역 목록 -->
    <div class="mb-4" v-if="selectedWarehouse">
      <div class="flex justify-between items-center mb-3">
        <h3 class="text-lg font-semibold">
          구역 목록 ({{ filteredAreas.length }}개)
        </h3>
        <div class="text-sm text-gray-600" v-if="selectedArea">
          선택된 구역: {{ selectedArea.displayName }}
        </div>
      </div>

      <DataTable
        :value="filteredAreas"
        :selection="selectedArea"
        @update:selection="handleAreaSelect"
        dataKey="wareAreaCd"
        selectionMode="single"
        :scrollable="true"
        scrollHeight="350px"
        :loading="isLoading"
        showGridlines
        responsiveLayout="scroll"
        @rowDblclick="handleAreaDoubleClick"
        :rowClass="(data) => isAreaSelected(data) ? 'selected-row' : ''"
      >
        <!-- 구역 정보 컬럼들 -->
        <Column
          v-for="col in areaTableColumns"
          :key="col.field"
          :field="col.field"
          :header="col.header"
          :headerClass="col.align === 'right' ? 'text-right' : ''"
          :bodyClass="col.align === 'right' ? 'text-right' : ''"
        >
          <template #body="slotProps" v-if="col.field === 'displayName'">
            <div class="flex align-items-center gap-2">
              <i class="pi pi-building text-blue-500"></i>
              <span class="font-semibold">{{ slotProps.data[col.field] }}</span>
            </div>
          </template>
          <template #body="slotProps" v-else-if="col.field === 'vol' || col.field === 'currentVolume' || col.field === 'availableVolume'">
            <span :class="col.field === 'availableVolume' && slotProps.data[col.field] <= 0 ? 'text-red-600 font-semibold' : ''">
              {{ slotProps.data[col.field]?.toLocaleString() }}
            </span>
          </template>
          <template #body="slotProps" v-else-if="col.field === 'statusText'">
            <span :class="slotProps.data.statusClass + ' font-semibold'">
              {{ slotProps.data[col.field] }}
            </span>
          </template>
        </Column>
      </DataTable>

      <!-- 구역 선택 안내 -->
      <div class="mt-3 p-3 bg-blue-50 border-l-4 border-blue-400 text-blue-700">
        <div class="flex">
          <i class="pi pi-info-circle mr-2 mt-1"></i>
          <div>
            <p class="font-medium">구역 선택 안내</p>
            <p class="text-sm mt-1">
              • 구역을 클릭하여 선택하거나 더블클릭으로 바로 확인할 수 있습니다.<br>
              • 잔여용량이 0인 구역은 선택할 수 없습니다.<br>
              • 이동수량: <strong>{{ selectedItemInfo?.moveQty }} {{ selectedItemInfo?.unitText }}</strong>
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- 구역이 없을 때 안내 메시지 -->
    <div v-else-if="selectedWarehouse && !isLoading && filteredAreas.length === 0" 
         class="text-center py-8 text-gray-500">
      <i class="pi pi-inbox text-4xl mb-3"></i>
      <p>선택된 창고에 사용 가능한 구역이 없습니다.</p>
    </div>

    <!-- 버튼 영역 -->
    <template #footer>
      <div class="flex justify-between items-center">
        <!-- 선택 요약 정보 -->
        <div class="text-sm text-gray-600" v-if="selectedArea">
          <i class="pi pi-check-circle text-green-500 mr-1"></i>
          선택된 위치: {{ selectedArea.displayName }} 
          (잔여용량: {{ selectedArea.availableVolume }})
        </div>
        <div v-else></div>

        <!-- 버튼들 -->
        <div class="flex gap-2">
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
            :disabled="!selectedArea || selectedArea.availableVolume <= 0"
          />
        </div>
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

.grid-cols-2 {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.grid-cols-3 {
  grid-template-columns: repeat(3, minmax(0, 1fr));
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

.mt-1 {
  margin-top: 0.25rem;
}

.mt-3 {
  margin-top: 0.75rem;
}

.p-3 {
  padding: 0.75rem;
}

.p-4 {
  padding: 1rem;
}

.py-8 {
  padding-top: 2rem;
  padding-bottom: 2rem;
}

.bg-gray-50 {
  background-color: #f9fafb;
}

.bg-blue-50 {
  background-color: #eff6ff;
}

.rounded-lg {
  border-radius: 0.5rem;
}

.border-l-4 {
  border-left-width: 4px;
}

.border-blue-400 {
  border-color: #60a5fa;
}

.text-blue-700 {
  color: #1d4ed8;
}

.text-green-600 {
  color: #16a34a;
}

.text-red-600 {
  color: #dc2626;
}

.text-gray-500 {
  color: #6b7280;
}

.text-gray-600 {
  color: #4b5563;
}

.text-gray-700 {
  color: #374151;
}

:deep(.selected-row) {
  background-color: #e0f2fe !important;
  border: 2px solid #0288d1;
}

:deep(.selected-row:hover) {
  background-color: #b3e5fc !important;
}

@media (min-width: 768px) {
  .md\:grid-cols-3 {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
  .md\:grid-cols-4 {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
  .md\:col-span-4 {
    grid-column: span 4 / span 4;
  }
}
</style>