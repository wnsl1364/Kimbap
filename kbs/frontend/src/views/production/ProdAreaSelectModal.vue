<script setup>
import { ref, computed, watch } from 'vue';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import InputNumber from 'primevue/inputnumber';
import { useToast } from 'primevue/usetoast';
import { useCommonStore } from '@/stores/commonStore';
import { getWarehousesByFactory } from '@/api/materials';
import { getWarehouseAreasWithStock } from '@/api/production';

const props = defineProps({
    visible: { type: Boolean, default: false },
    selectedProduct: { type: Object, default: () => ({}) },
    loadingQuantity: { type: Number, default: 0 }
});

const emit = defineEmits(['update:visible', 'confirm']);
const toast = useToast();
const commonStore = useCommonStore();

// 상태
const warehouseTypes = ref([]);
const selectedWarehouseType = ref(null);
const selectedFloor = ref(null);
const warehouseAreas = ref([]);
const selectedAreas = ref([]);
const placementPlan = ref([]);
const modalInputQty = ref(0);

// 계산된 값
const modalVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
});

const remainingQty = computed(() => {
    const totalAllocated = placementPlan.value.reduce((sum, plan) => sum + plan.allocateQty, 0);
    return (modalInputQty.value || 0) - totalAllocated;
});

const warehouseTypeOptions = computed(() => {
    // const stoConMap = { 'o1': 'q1', 'o2': 'q2', 'o3': 'q3' };
    // const allowedType = stoConMap[props.selectedProduct?.stoCon];
    const allowedType = 'q3';
    return warehouseTypes.value
    .filter(warehouse => warehouse.wareType === allowedType)
    .map(warehouse => ({
        label: warehouse.wareName,
        value: warehouse.wcode,
        maxRow: warehouse.maxRow,
        maxCol: warehouse.maxCol,
        maxFloor: warehouse.maxFloor
    }));
});

const floorOptions = computed(() => {
    if (!selectedWarehouseType.value) return [];
    const warehouse = warehouseTypeOptions.value.find(w => w.value === selectedWarehouseType.value);
    if (!warehouse) return [];
    
    return Array.from({ length: warehouse.maxFloor }, (_, i) => ({
        label: `${i + 1}층`,
        value: i + 1
    }));
});

const areaGrid = computed(() => {
    if (!selectedWarehouseType.value || !selectedFloor.value) return [];
    
    const warehouse = warehouseTypeOptions.value.find(w => w.value === selectedWarehouseType.value);
    if (!warehouse) return [];
    
    const grid = [];
    for (let row = 0; row < warehouse.maxRow; row++) {
        const rowData = [];
        const rowLetter = String.fromCharCode(65 + row);
        
        for (let col = 1; col <= warehouse.maxCol; col++) {
            const areaCode = `W-${selectedWarehouseType.value.split('-')[1]}-${rowLetter}${col}-${selectedFloor.value}`;
            const areaInfo = warehouseAreas.value.find(area => area.wareAreaCd === areaCode);
            
            // 🔥 단위별 실제 용량 계산
            const realMaxVolume = getRealCapacity();
            const currentVolume = areaInfo?.currentVolume || 0;
            const availableVolume = realMaxVolume - currentVolume;
            
            rowData.push({
                wareAreaCd: areaCode,
                displayName: `${rowLetter}${col}`,
                maxVolume: areaInfo?.vol || 100, // DB 원본값
                realMaxVolume: realMaxVolume,    // 🔥 단위별 실제 용량
                currentVolume: currentVolume,
                availableVolume: Math.max(0, availableVolume), // 🔥 실제 가용 용량
                currentProduct: areaInfo?.currentProduct || null,
                isAvailable: !areaInfo?.currentProduct || areaInfo?.currentProduct === props.selectedProduct?.pcode,
                isSameProduct: areaInfo?.currentProduct === props.selectedProduct?.pcode
            });
        }
        grid.push(rowData);
    }
    return grid;
});

const isConfirmEnabled = computed(() => {
    return placementPlan.value.length > 0 && 
           placementPlan.value.every(plan => plan.allocateQty > 0);
});

// 공통코드 표시 함수
const getUnitDisplayName = (unitCode) => {
    const unitCodes = commonStore.getCodes('0G') || [];
    const unit = unitCodes.find(code => code.dcd === unitCode);
    return unit ? unit.cdInfo : unitCode;
};

const getStorageConditionDisplayName = (stoConCode) => {
    const stoConCodes = commonStore.getCodes('0O') || [];
    const stoCon = stoConCodes.find(code => code.dcd === stoConCode);
    return stoCon ? stoCon.cdInfo : stoConCode;
};

// 🔥 단위별 기준 용량 설정 (중요!)
const getUnitCapacityStandard = (unitCode) => {
    const standards = {
        'g1': 1000000,     // g(그램)
        'g2': 1000,        // kg(킬로그램): 1000kg
        'g3': 1000000,     // ml(밀리리터)
        'g4': 1000,        // L(리터): 1000L
        'g5': 5000,        // ea(개): 5000개
        'g6': 500,         // box(박스): 500박스
        'g7': 5000000,     // mm(밀리미터)
    };
    
    return standards[unitCode?.toLowerCase()] || 1000; // 기본값: 1000
};

// 🔥 실제 용량 계산 (단위 기반으로 100을 변환)
const getRealCapacity = (area) => {
    const productUnit = props.selectedProduct?.unit || 'g5';
    const standardCapacity = getUnitCapacityStandard(productUnit);
    
    // 기본 100에서 단위별 표준 용량으로 변환
    return standardCapacity;
};

// 🔥 용량 표시 (퍼센트 기반)
const getCapacityDisplay = (area) => {
    const current = area.currentVolume || 0;
    const realCapacity = area.realMaxVolume || getRealCapacity();
    const percentage = Math.round((current / realCapacity) * 100);
    
    return `${percentage}%`;
};

// 🔥 사용률 퍼센트 계산
const getUsagePercentage = (area) => {
    const current = area.currentVolume || 0;
    const realCapacity = area.realMaxVolume || getRealCapacity();
    return Math.round((current / realCapacity) * 100);
};

// 🔥 용량 상태 색상
const getCapacityColor = (area) => {
    const percentage = getUsagePercentage(area);
    if (percentage === 0) return 'bg-gray-200';
    if (percentage <= 30) return 'bg-green-500';
    if (percentage <= 60) return 'bg-yellow-500';
    if (percentage <= 85) return 'bg-orange-500';
    return 'bg-red-500';
};

// 구역 선택
const selectArea = (area) => {
    if (!area.isAvailable) {
        toast.add({
            severity: 'warn',
            summary: '구역 선택 불가',
            detail: `다른 제품이 적재된 구역입니다.`,
            life: 3000
        });
        return;
    }
    
    // 🔥 실제 가용 용량 체크
    if (area.availableVolume <= 0) {
        toast.add({
            severity: 'warn',
            summary: '구역 선택 불가',
            detail: '해당 구역에는 가용 용량이 없습니다.',
            life: 3000
        });
        return;
    }
    
    // 이미 선택된 구역이면 제거
    const selectedIndex = selectedAreas.value.findIndex(selected => selected.wareAreaCd === area.wareAreaCd);
    if (selectedIndex !== -1) {
        selectedAreas.value.splice(selectedIndex, 1);
        placementPlan.value = placementPlan.value.filter(plan => plan.wareAreaCd !== area.wareAreaCd);
        toast.add({
            severity: 'info',
            summary: '구역 선택 해제',
            detail: `${area.displayName} 구역이 선택 해제되었습니다.`,
            life: 2000
        });
        return;
    }
    
    // 새 구역 선택
    selectedAreas.value.push(area);
    placementPlan.value.push({
        wareAreaCd: area.wareAreaCd,
        allocateQty: 0,
        selectedArea: area,
        maxAllowedQty: Math.min(remainingQty.value, area.availableVolume) // 🔥 실제 가용 용량 사용
    });
    
    toast.add({
        severity: 'success',
        summary: '구역 선택됨',
        detail: `${area.displayName} 구역이 선택되었습니다. 수량을 입력해주세요.`,
        life: 2000
    });
};

// 수량 업데이트
const updateAreaQuantity = (planIndex, newQty) => {
    const plan = placementPlan.value[planIndex];
    if (!plan) return;
    
    newQty = Math.max(0, newQty || 0);
    
    // 🔥 실제 가용 용량으로 제한
    newQty = Math.min(newQty, plan.selectedArea.availableVolume);
    
    const otherTotal = placementPlan.value
        .filter((_, index) => index !== planIndex)
        .reduce((sum, p) => sum + p.allocateQty, 0);
    
    if (otherTotal + newQty > modalInputQty.value) {
        newQty = Math.max(0, modalInputQty.value - otherTotal);
    }
    
    plan.allocateQty = newQty;
    
    // 🔥 maxAllowedQty 재계산
    const newTotal = placementPlan.value.reduce((sum, p) => sum + p.allocateQty, 0);
    placementPlan.value.forEach(p => {
        const others = newTotal - p.allocateQty;
        p.maxAllowedQty = Math.min(modalInputQty.value - others, p.selectedArea.availableVolume);
    });
};

// 계획 제거
const removePlan = (index) => {
    const removedPlan = placementPlan.value[index];
    placementPlan.value.splice(index, 1);
    selectedAreas.value = selectedAreas.value.filter(area => area.wareAreaCd !== removedPlan.wareAreaCd);
};

// 구역 스타일
const getAreaStyle = (area) => {
    const isSelected = selectedAreas.value.some(selected => selected.wareAreaCd === area.wareAreaCd);
    
    if (isSelected) return 'bg-blue-500 text-white border-blue-600';
    if (!area.isAvailable) return 'bg-red-200 text-red-900 border-red-400 cursor-not-allowed opacity-75';
    if (area.isSameProduct) return 'bg-green-100 text-green-800 border-green-300 hover:bg-green-200';
    if (area.availableVolume <= 0) return 'bg-gray-200 text-gray-600 border-gray-400 cursor-not-allowed opacity-75'; // 🔥 실제 가용 용량 체크
    return 'bg-white hover:bg-blue-50 border-gray-300 hover:border-blue-400';
};

// 확인/취소
const handleConfirm = () => {
    if (placementPlan.value.length === 0) {
        toast.add({
            severity: 'warn',
            summary: '구역 선택 필요',
            detail: '적재할 구역을 선택해주세요.',
            life: 3000
        });
        return;
    }
    
    emit('confirm', {
        placementPlan: placementPlan.value,
        totalAllocated: modalInputQty.value - remainingQty.value,
        remainingQty: remainingQty.value,
        userInputQty: modalInputQty.value
    });
    
    modalVisible.value = false;
};

const handleCancel = () => {
    if (placementPlan.value.length > 0) {
        const shouldReset = window.confirm('적재 계획을 취소하시겠습니까?');
        if (!shouldReset) return;
        resetModal();
    }
    modalVisible.value = false;
};

const resetModal = () => {
    selectedWarehouseType.value = null;
    selectedFloor.value = null;
    selectedAreas.value = [];
    placementPlan.value = [];
    modalInputQty.value = props.loadingQuantity || 0;
};

// API 호출
// 창고 정보 불러오기
const loadWarehouseTypes = async () => {
    try {
        const response = await getWarehousesByFactory(props.selectedProduct?.fcode);
        warehouseTypes.value = response.data;
    } catch (error) {
        console.error('창고 유형 로드 실패:', error);
    }
};

const loadWarehouseAreas = async () => {
    if (!selectedWarehouseType.value || !selectedFloor.value) return;
    
    try {
        const response = await getWarehouseAreasWithStock(selectedWarehouseType.value, selectedFloor.value);
        warehouseAreas.value = response.data;
    } catch (error) {
        console.error('창고 구역 로드 실패:', error);
    }
};

// Watch
watch(selectedWarehouseType, () => {
    selectedFloor.value = null;
    selectedAreas.value = [];
    placementPlan.value = [];
});

watch(selectedFloor, () => {
    selectedAreas.value = [];
    placementPlan.value = [];
    loadWarehouseAreas();
});

watch(() => props.visible, (newVal) => {
    if (newVal) {
        modalInputQty.value = props.loadingQuantity || 0;
        loadWarehouseTypes();
    } else {
        // 🔥 모달이 닫힐 때 초기화
        resetModal();
    }
});

watch(() => props.loadingQuantity, (newQty) => {
    if (newQty > 0) {
        modalInputQty.value = newQty;
    }
});
</script>

<template>
    <Dialog
        v-model:visible="modalVisible"
        modal
        :pt="{
            root: 'prod-area-modal-root',
            mask: 'prod-area-modal-mask', 
            content: 'prod-area-modal-content'
        }"
        :closable="true"
    >
        <div style="display: flex; height: 100%; gap: 1rem;">
            <!-- 왼쪽: 정보 패널 -->
            <div style="width: 320px; flex-shrink: 0; overflow-y: auto;">
                <div style="display: flex; flex-direction: column; gap: 1rem;">
                    <!-- 제품 정보 -->
                    <div class="bg-blue-50 p-4 rounded-lg">
                        <h6 class="font-bold text-blue-900 mb-3">제품 정보</h6>
                        <div class="space-y-2 text-sm">
                            <div class="flex justify-between">
                                <span class="font-bold text-gray-800">제품코드:</span>
                                <span class="font-medium text-gray-900">{{ selectedProduct?.pcode }}</span>
                            </div>
                            <div class="flex justify-between">
                                <span class="font-bold text-gray-800">제품명:</span>
                                <span class="font-medium text-gray-900">{{ selectedProduct?.prodName }}</span>
                            </div>
                            <div class="flex justify-between">
                                <span class="font-bold text-gray-800">보관온도:</span>
                                <span class="font-medium text-gray-900">{{ getStorageConditionDisplayName(selectedProduct?.stoTemp || 'o1') }}</span>
                            </div>
                            <div class="flex justify-between">
                                <span class="font-bold text-gray-800">단위:</span>
                                <span class="font-medium text-gray-900">{{ getUnitDisplayName(selectedProduct?.unit || 'g5') }}</span>
                            </div>
                            <div class="flex justify-between">
                                <span class="font-bold text-gray-800">적재수량:</span>
                                <span class="font-bold text-blue-700">{{ modalInputQty }}</span>
                            </div>
                            <div class="flex justify-between">
                                <span class="font-bold text-gray-800">남은수량:</span>
                                <span :class="remainingQty > 0 ? 'text-red-600 font-bold' : 'text-green-600 font-bold'">
                                    {{ remainingQty }}
                                </span>
                            </div>
                        </div>
                    </div>

                    <!-- 적재 계획 -->
                    <div v-if="placementPlan.length > 0" class="bg-green-50 p-4 rounded-lg">
                        <h6 class="font-bold text-green-900 mb-3">적재 계획</h6>
                        <div class="space-y-3 max-h-48 overflow-y-auto">
                            <div v-for="(plan, index) in placementPlan" :key="index" 
                                 class="bg-white p-3 rounded border">
                                <div class="flex justify-between items-start mb-2">
                                    <div>
                                        <div class="font-mono text-sm font-semibold">{{ plan.wareAreaCd }}</div>
                                        <div class="text-xs text-gray-600">{{ plan.selectedArea.displayName }}</div>
                                        <div class="text-xs text-blue-600">
                                            최대 {{ plan.selectedArea.availableVolume }}{{ getUnitDisplayName(selectedProduct?.unit || 'g5') }}
                                        </div>
                                    </div>
                                    <Button
                                        size="small"
                                        severity="danger"
                                        text
                                        @click="removePlan(index)"
                                        class="p-1 h-6 w-6"
                                    >
                                        ×
                                    </Button>
                                </div>
                                
                                <div class="flex items-center gap-2">
                                    <label class="text-sm font-bold text-gray-800 min-w-12">수량:</label>
                                    <InputNumber
                                        :modelValue="plan.allocateQty"
                                        @update:modelValue="(newValue) => updateAreaQuantity(index, newValue || 0)"
                                        :min="0"
                                        :max="plan.selectedArea.availableVolume"
                                        class="flex-1"
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 오른쪽: 창고 선택 -->
            <div style="flex: 1; display: flex; flex-direction: column; min-height: 0;">
                <!-- 창고/층 선택 -->
                <div class="bg-gray-50 p-4 rounded-lg mb-4 space-y-3">
                    <div class="flex items-center gap-4">
                        <label class="font-bold text-gray-800 min-w-20">창고:</label>
                        <Dropdown
                            v-model="selectedWarehouseType"
                            :options="warehouseTypeOptions"
                            optionLabel="label"
                            optionValue="value"
                            placeholder="창고를 선택하세요"
                            class="flex-1"
                        />
                    </div>

                    <div class="flex items-center gap-4" v-if="selectedWarehouseType">
                        <label class="font-bold text-gray-800 min-w-20">층:</label>
                        <Dropdown
                            v-model="selectedFloor"
                            :options="floorOptions"
                            optionLabel="label"
                            optionValue="value"
                            placeholder="층을 선택하세요"
                            class="flex-1"
                        />
                    </div>
                </div>

                <!-- 구역 그리드 -->
                <div v-if="selectedFloor && areaGrid.length > 0" style="flex: 1; display: flex; flex-direction: column; min-height: 0;">
                    <h6 class="font-bold text-gray-900 mb-3">구역 선택 ({{ selectedFloor }}층)</h6>

                    <div style="flex: 1; overflow: auto; border: 1px solid #e5e7eb; border-radius: 0.5rem; padding: 1rem; background: white;">
                        <div class="grid gap-2" :style="{ gridTemplateColumns: `repeat(${areaGrid[0]?.length || 1}, 1fr)` }">
                            <template v-for="(row, rowIndex) in areaGrid" :key="rowIndex">
                                <div
                                    v-for="(area, colIndex) in row"
                                    :key="`${rowIndex}-${colIndex}`"
                                    :class="[
                                        'border-2 rounded-lg p-3 transition-all duration-200 min-h-20 min-w-16',
                                        getAreaStyle(area),
                                        // 🔥 클릭 가능 여부에 따라 커서 스타일 적용
                                        area.isAvailable && area.availableVolume > 0 ? 'cursor-pointer' : 'cursor-not-allowed'
                                    ]"
                                    @click="area.isAvailable && area.availableVolume > 0 ? selectArea(area) : null"
                                    :title="`구역: ${area.wareAreaCd}
실제용량: ${area.realMaxVolume}${getUnitDisplayName(selectedProduct?.unit || 'g5')}
현재적재: ${area.currentVolume}${getUnitDisplayName(selectedProduct?.unit || 'g5')}
가용용량: ${area.availableVolume}${getUnitDisplayName(selectedProduct?.unit || 'g5')}
${area.currentProduct ? '기존제품: ' + area.currentProduct : ''}
${!area.isAvailable ? '[선택불가] 다른 제품이 적재된 구역' : ''}
${area.availableVolume <= 0 ? '[선택불가] 가용 용량 없음' : ''}`"
                                >
                                    <div class="text-center">
                                        <div class="font-bold text-sm text-gray-900">{{ area.displayName }}</div>
                                        <div class="text-xs mt-1 font-semibold text-gray-800">
                                            {{ getCapacityDisplay(area) }}
                                        </div>
                                        <div v-if="area.currentProduct" class="text-xs mt-1">
                                            <span v-if="area.isSameProduct" class="text-green-600 font-semibold">동일제품</span>
                                            <span v-else class="text-red-600 font-semibold">다른제품</span>
                                        </div>
                                        <div v-else class="text-xs mt-1 text-gray-800">빈구역</div>
                                        
                                        <!-- 🔥 용량 게이지 바 -->
                                        <div class="w-full bg-gray-200 rounded-full h-1.5 mt-1">
                                            <div 
                                                :class="`h-1.5 rounded-full transition-all duration-300 ${getCapacityColor(area)}`"
                                                :style="{ width: getUsagePercentage(area) + '%' }"
                                            ></div>
                                        </div>
                                        
                                        <!-- 🔥 실제 용량 정보 -->
                                        <div class="text-xs text-gray-900 mt-1 font-medium">
                                            {{ area.availableVolume }}/{{ area.realMaxVolume }}{{ getUnitDisplayName(selectedProduct?.unit || 'g5') }}
                                        </div>
                                    </div>
                                </div>
                            </template>
                        </div>
                    </div>
                </div>

                <div v-else style="flex: 1; display: flex; align-items: center; justify-content: center; color: #374151;">
                    창고와 층을 먼저 선택해주세요.
                </div>
            </div>
        </div>

        <template #footer>
            <div class="flex justify-end gap-2">
                <Button label="취소" severity="secondary" @click="handleCancel" />
                <Button label="확인" severity="success" @click="handleConfirm" :disabled="!isConfirmEnabled" />
            </div>
        </template>
    </Dialog>
</template>

<style scoped>
/* PassThrough를 이용한 직접 제어 */
:global(.prod-area-modal-mask) {
    background-color: rgba(0, 0, 0, 0.4) !important;
    position: fixed !important;
    top: 0 !important;
    left: 0 !important;
    width: 100vw !important;
    height: 100vh !important;
    z-index: 1000 !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
}

:global(.prod-area-modal-root) {
    width: 95vw !important;
    max-width: 1600px !important;
    height: 90vh !important;
    max-height: 800px !important;
    background: white !important;
    border-radius: 8px !important;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2) !important;
    display: flex !important;
    flex-direction: column !important;
    overflow: hidden !important;
}

:global(.prod-area-modal-content) {
    flex: 1 !important;
    overflow: hidden !important;
    padding: 0 !important;
}

/* 내부 레이아웃 */
.modal-container {
    display: flex;
    height: 450px;
    gap: 1rem;
    padding: 1.5rem;
}

.info-panel {
    width: 300px;
    flex-shrink: 0;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.warehouse-panel {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
}

.grid-container {
    flex: 1;
    overflow: auto;
    border: 1px solid #e5e7eb;
    border-radius: 0.5rem;
    padding: 1rem;
    background: white;
}

.grid {
    display: grid;
    gap: 0.5rem;
}
</style>