<script setup>
import { ref, computed, watch } from 'vue';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import InputNumber from 'primevue/inputnumber';
import { useToast } from 'primevue/usetoast';
import { useCommonStore } from '@/stores/commonStore';
import { getWarehousesByFactory, getWarehouseAreasWithStock } from '@/api/materials';

const props = defineProps({
    visible: { type: Boolean, default: false },
    selectedMaterial: { type: Object, default: () => ({}) },
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
    const stoConMap = { 'o1': 'q1', 'o2': 'q2', 'o3': 'q3' };
    const allowedType = stoConMap[props.selectedMaterial?.stoCon];
    
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
            
            rowData.push({
                wareAreaCd: areaCode,
                displayName: `${rowLetter}${col}`,
                maxVolume: areaInfo?.vol || 1000,
                currentVolume: areaInfo?.currentVolume || 0,
                availableVolume: (areaInfo?.vol || 1000) - (areaInfo?.currentVolume || 0),
                currentMaterial: areaInfo?.currentMaterial || null,
                isAvailable: !areaInfo?.currentMaterial || areaInfo?.currentMaterial === props.selectedMaterial?.mcode,
                isSameMaterial: areaInfo?.currentMaterial === props.selectedMaterial?.mcode
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

// 구역 선택
const selectArea = (area) => {
    if (!area.isAvailable) {
        toast.add({
            severity: 'warn',
            summary: '구역 선택 불가',
            detail: `다른 자재가 적재된 구역입니다.`,
            life: 3000
        });
        return;
    }
    
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
        return;
    }
    
    // 새 구역 선택
    selectedAreas.value.push(area);
    placementPlan.value.push({
        wareAreaCd: area.wareAreaCd,
        allocateQty: 0,
        selectedArea: area,
        maxAllowedQty: Math.min(remainingQty.value, area.availableVolume)
    });
};

// 수량 업데이트
const updateAreaQuantity = (planIndex, newQty) => {
    const plan = placementPlan.value[planIndex];
    if (!plan) return;
    
    newQty = Math.max(0, newQty || 0);
    newQty = Math.min(newQty, plan.selectedArea.availableVolume);
    
    const otherTotal = placementPlan.value
        .filter((_, index) => index !== planIndex)
        .reduce((sum, p) => sum + p.allocateQty, 0);
    
    if (otherTotal + newQty > modalInputQty.value) {
        newQty = Math.max(0, modalInputQty.value - otherTotal);
    }
    
    plan.allocateQty = newQty;
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
    if (!area.isAvailable) return 'bg-red-200 text-red-900 border-red-400 cursor-not-allowed';
    if (area.isSameMaterial) return 'bg-green-100 text-green-800 border-green-300 hover:bg-green-200';
    if (area.availableVolume <= 0) return 'bg-gray-200 text-gray-600 border-gray-400 cursor-not-allowed';
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
const loadWarehouseTypes = async () => {
    try {
        const response = await getWarehousesByFactory(props.selectedMaterial?.fcode);
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
        header="창고 구역 선택"
        :style="{ width: '95vw', maxWidth: '1400px', height: '90vh' }"
        :closable="true"
    >
        <div class="flex h-[calc(90vh-120px)] gap-4">
            <!-- 왼쪽: 정보 패널 -->
            <div class="w-80 flex-shrink-0 space-y-4 overflow-y-auto">
                <!-- 자재 정보 -->
                <div class="bg-blue-50 p-4 rounded-lg">
                    <h6 class="font-semibold text-blue-800 mb-3">자재 정보</h6>
                    <div class="space-y-2 text-sm">
                        <div class="flex justify-between">
                            <span class="font-medium">자재코드:</span>
                            <span>{{ selectedMaterial?.mcode }}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="font-medium">자재명:</span>
                            <span>{{ selectedMaterial?.mateName }}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="font-medium">보관조건:</span>
                            <span>{{ getStorageConditionDisplayName(selectedMaterial?.stoCon || 'o1') }}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="font-medium">단위:</span>
                            <span>{{ getUnitDisplayName(selectedMaterial?.unit || 'g5') }}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="font-medium">적재수량:</span>
                            <span class="font-bold text-blue-600">{{ modalInputQty }}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="font-medium">남은수량:</span>
                            <span :class="remainingQty > 0 ? 'text-red-600 font-bold' : 'text-green-600 font-bold'">
                                {{ remainingQty }}
                            </span>
                        </div>
                    </div>
                </div>

                <!-- 적재 계획 -->
                <div v-if="placementPlan.length > 0" class="bg-green-50 p-4 rounded-lg">
                    <h6 class="font-semibold text-green-800 mb-3">적재 계획</h6>
                    <div class="space-y-3 max-h-48 overflow-y-auto">
                        <div v-for="(plan, index) in placementPlan" :key="index" 
                             class="bg-white p-3 rounded border">
                            <div class="flex justify-between items-start mb-2">
                                <div>
                                    <div class="font-mono text-sm font-semibold">{{ plan.wareAreaCd }}</div>
                                    <div class="text-xs text-gray-600">{{ plan.selectedArea.displayName }}</div>
                                    <div class="text-xs text-blue-600">
                                        최대 {{ plan.selectedArea.availableVolume }}
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
                                <label class="text-sm font-medium min-w-12">수량:</label>
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

            <!-- 오른쪽: 창고 선택 -->
            <div class="flex-1 flex flex-col">
                <!-- 창고/층 선택 -->
                <div class="bg-gray-50 p-4 rounded-lg mb-4 space-y-3">
                    <div class="flex items-center gap-4">
                        <label class="font-medium min-w-20">창고:</label>
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
                        <label class="font-medium min-w-20">층:</label>
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
                <div v-if="selectedFloor && areaGrid.length > 0" class="flex-1 flex flex-col">
                    <h6 class="font-semibold mb-3">구역 선택 ({{ selectedFloor }}층)</h6>

                    <div class="flex-1 overflow-auto border rounded-lg p-4 bg-white">
                        <div class="grid gap-2" :style="{ gridTemplateColumns: `repeat(${areaGrid[0]?.length || 1}, 1fr)` }">
                            <template v-for="(row, rowIndex) in areaGrid" :key="rowIndex">
                                <div
                                    v-for="(area, colIndex) in row"
                                    :key="`${rowIndex}-${colIndex}`"
                                    :class="[
                                        'border-2 rounded-lg p-3 transition-all duration-200 min-h-20 min-w-16 cursor-pointer',
                                        getAreaStyle(area)
                                    ]"
                                    @click="selectArea(area)"
                                    :title="`구역: ${area.wareAreaCd}\n가용용량: ${area.availableVolume}`"
                                >
                                    <div class="text-center">
                                        <div class="font-bold text-sm">{{ area.displayName }}</div>
                                        <div class="text-xs mt-1">{{ area.availableVolume }}/{{ area.maxVolume }}</div>
                                        <div v-if="area.currentMaterial" class="text-xs mt-1">
                                            <span v-if="area.isSameMaterial" class="text-green-600">동일자재</span>
                                            <span v-else class="text-red-600">다른자재</span>
                                        </div>
                                        <div v-else class="text-xs mt-1 text-gray-500">빈구역</div>
                                    </div>
                                </div>
                            </template>
                        </div>
                    </div>
                </div>

                <div v-else class="flex-1 flex items-center justify-center text-gray-500">
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
.grid {
    max-width: 100%;
    overflow-x: auto;
}
</style>
