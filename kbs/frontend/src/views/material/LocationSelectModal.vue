<script setup>
import { ref, computed, watch } from 'vue';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import InputNumber from 'primevue/inputnumber';
import { useToast } from 'primevue/usetoast';
import { useCommonStore } from '@/stores/commonStore';
import { getWarehousesByFactory, getWarehouseAreasWithStock, getMateLoadingFactoryList } from '@/api/materials';

const props = defineProps({
    visible: { type: Boolean, default: false },
    selectedMaterial: { type: Object, default: () => ({}) },
    loadingQuantity: { type: Number, default: 0 },
    existingPlacements: { type: Array, default: () => [] } // 다른 자재들의 기존 배치 정보
});

const emit = defineEmits(['update:visible', 'confirm']);
const toast = useToast();
const commonStore = useCommonStore();

// 상태
const factories = ref([]);
const selectedFactory = ref(null);
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

const factoryOptions = computed(() => {
    return factories.value.map(factory => ({
        label: factory.facName,
        value: factory.fcode
    }));
});

const warehouseTypeOptions = computed(() => {
    if (!selectedFactory.value) return [];
    
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
            
            // 다른 자재가 이미 선택한 위치인지 확인
            const existingPlacement = props.existingPlacements.find(placement => placement.wareAreaCd === areaCode);
            
            // 🔥 단위별 실제 용량 계산
            const realMaxVolume = getRealCapacity();
            const currentVolume = areaInfo?.currentVolume || 0;
            const availableVolume = realMaxVolume - currentVolume;
            
            // 같은 자재인지 확인 (DB의 현재 자재 vs 선택하려는 자재) - 수량이 0이면 빈구역으로 처리
            const isSameMaterialInDB = areaInfo?.currentMaterial === props.selectedMaterial?.mcode && currentVolume > 0;
            
            // 다른 자재가 이미 이 위치를 선택했는지 확인
            const isDifferentMaterialSelected = existingPlacement && existingPlacement.mcode !== props.selectedMaterial?.mcode;
            
            // 다른 자재가 DB에 적재되어 있는지 확인 - 수량이 0이면 빈구역으로 처리
            const isDifferentMaterialInDB = areaInfo?.currentMaterial && 
                                          areaInfo.currentMaterial !== props.selectedMaterial?.mcode &&
                                          currentVolume > 0;
            
            // 선택 가능 여부 결정
            const isAvailable = !isDifferentMaterialSelected && 
                              !isDifferentMaterialInDB &&
                              (availableVolume > 0 || isSameMaterialInDB);
            
            rowData.push({
                wareAreaCd: areaCode,
                displayName: `${rowLetter}${col}`,
                maxVolume: areaInfo?.vol || 100, // DB 원본값
                realMaxVolume: realMaxVolume,    // 🔥 단위별 실제 용량
                currentVolume: currentVolume,
                availableVolume: Math.max(0, availableVolume), // 🔥 실제 가용 용량
                currentMaterial: areaInfo?.currentMaterial || null,
                existingPlacement: existingPlacement, // 다른 자재의 기존 선택 정보
                isAvailable: isAvailable,
                isSameMaterial: isSameMaterialInDB,
                isDifferentMaterialSelected: isDifferentMaterialSelected,
                isDifferentMaterialInDB: isDifferentMaterialInDB // 🔥 다른 자재가 DB에 적재됨 추가
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
    const materialUnit = props.selectedMaterial?.unit || 'g5';
    const standardCapacity = getUnitCapacityStandard(materialUnit);
    
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
        let message = '구역 선택 불가';
        let detail = '';
        
        if (area.isDifferentMaterialSelected) {
            const placement = area.existingPlacement;
            if (placement.source === 'pending') {
                detail = `다른 자재(${placement.itemName})가 이동요청 중인 구역입니다. (요청번호: ${placement.moveReqCd})`;
            } else {
                detail = `다른 자재(${placement.itemName})가 이미 선택된 구역입니다.`;
            }
        } else if (area.isDifferentMaterialInDB) {
            detail = `다른 자재(${area.currentMaterial})가 적재된 구역입니다.`;
        } else if (area.currentMaterial) {
            detail = `다른 자재가 적재된 구역입니다.`;
        }
        
        toast.add({
            severity: 'warn',
            summary: message,
            detail: detail,
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
    if (area.isDifferentMaterialSelected) {
        // 이동요청 중인 자재와 현재 등록 중인 자재를 구분하여 색상 적용
        const placement = area.existingPlacement;
        if (placement?.source === 'pending') {
            return 'bg-orange-200 text-orange-900 border-orange-400 cursor-not-allowed opacity-75';
        } else {
            return 'bg-yellow-200 text-yellow-900 border-yellow-400 cursor-not-allowed opacity-75';
        }
    }
    // 🔥 다른 자재가 DB에 적재된 구역 체크 추가
    if (area.isDifferentMaterialInDB) return 'bg-red-200 text-red-900 border-red-400 cursor-not-allowed opacity-75';
    if (!area.isAvailable) return 'bg-red-200 text-red-900 border-red-400 cursor-not-allowed opacity-75';
    if (area.isSameMaterial) return 'bg-green-100 text-green-800 border-green-300 hover:bg-green-200';
    if (area.availableVolume <= 0) return 'bg-gray-200 text-gray-800 border-gray-400 cursor-not-allowed opacity-75'; // 🔥 실제 가용 용량 체크
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
    
    // 모든 배치 계획에 수량이 입력되었는지 확인
    const hasInvalidPlan = placementPlan.value.some(plan => !plan.allocateQty || plan.allocateQty <= 0);
    if (hasInvalidPlan) {
        toast.add({
            severity: 'warn',
            summary: '수량 입력 필요',
            detail: '모든 선택된 구역에 수량을 입력해주세요.',
            life: 3000
        });
        return;
    }
    
    // 선택된 공장과 창고 정보도 함께 전달
    const selectedFactoryInfo = factories.value.find(f => f.fcode === selectedFactory.value);
    const selectedWarehouseInfo = warehouseTypes.value.find(w => w.wcode === selectedWarehouseType.value);
    
    const confirmData = {
        fcode: selectedFactory.value,
        facName: selectedFactoryInfo?.facName || '',
        wcode: selectedWarehouseType.value,
        wareName: selectedWarehouseInfo?.wareName || '',
        wareAreaCd: placementPlan.value[0]?.wareAreaCd || '',
        placementPlan: placementPlan.value,
        totalAllocated: modalInputQty.value - remainingQty.value,
        remainingQty: remainingQty.value,
        userInputQty: modalInputQty.value
    };
    
    console.log('LocationSelectModal 확인 데이터:', confirmData);
    
    emit('confirm', confirmData);
    
    // 모달 닫기 전에 데이터 초기화
    resetModal();
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
    selectedFactory.value = null;
    selectedWarehouseType.value = null;
    selectedFloor.value = null;
    selectedAreas.value = [];
    placementPlan.value = [];
    modalInputQty.value = props.loadingQuantity || 0;
};

// API 호출
const loadFactories = async () => {
    try {
        const response = await getMateLoadingFactoryList();
        factories.value = response.data;
        console.log('공장 목록 로드 완료:', factories.value.length, '개');
    } catch (error) {
        console.error('공장 목록 로드 실패:', error);
        toast.add({
            severity: 'error',
            summary: '공장 목록 로드 실패',
            detail: '공장 목록을 불러오는데 실패했습니다.',
            life: 3000
        });
    }
};

const loadWarehouseTypes = async () => {
    if (!selectedFactory.value) return;
    
    try {
        const response = await getWarehousesByFactory(selectedFactory.value);
        warehouseTypes.value = response.data;
        console.log('창고 목록 로드 완료:', warehouseTypes.value.length, '개');
    } catch (error) {
        console.error('창고 유형 로드 실패:', error);
        toast.add({
            severity: 'error',
            summary: '창고 목록 로드 실패',
            detail: '창고 목록을 불러오는데 실패했습니다.',
            life: 3000
        });
    }
};

const loadWarehouseAreas = async () => {
    if (!selectedWarehouseType.value || !selectedFloor.value) return;
    
    try {
        const response = await getWarehouseAreasWithStock(selectedWarehouseType.value, selectedFloor.value);
        warehouseAreas.value = response.data;
        console.log('창고 구역 로드 완료:', warehouseAreas.value.length, '개');
    } catch (error) {
        console.error('창고 구역 로드 실패:', error);
        toast.add({
            severity: 'error',
            summary: '구역 정보 로드 실패',
            detail: '창고 구역 정보를 불러오는데 실패했습니다.',
            life: 3000
        });
    }
};

// Watch
watch(selectedFactory, () => {
    selectedWarehouseType.value = null;
    selectedFloor.value = null;
    selectedAreas.value = [];
    placementPlan.value = [];
    if (selectedFactory.value) {
        loadWarehouseTypes();
    }
});

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
        loadFactories();
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
            root: 'area-modal-root',
            mask: 'area-modal-mask', 
            content: 'area-modal-content'
        }"
        :closable="true"
    >
        <div class="modal-container">
            <!-- 왼쪽: 자재 정보 및 수량 입력 -->
            <div class="info-panel">
                <div style="display: flex; flex-direction: column; gap: 1rem;">
                <!-- 자재 정보 -->
                <div class="bg-blue-50 p-4 rounded-lg">
                    <h6 class="font-bold text-blue-900 mb-3">자재 정보</h6>
                    <div class="space-y-2 text-sm">
                        <div class="flex justify-between">
                            <span class="font-bold text-gray-800">자재코드:</span>
                            <span class="font-medium text-gray-900">{{ selectedMaterial?.mcode }}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="font-bold text-gray-800">자재명:</span>
                            <span class="font-medium text-gray-900">{{ selectedMaterial?.mateName }}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="font-bold text-gray-800">보관조건:</span>
                            <span class="font-medium text-gray-900">{{ getStorageConditionDisplayName(selectedMaterial?.stoCon || 'o1') }}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="font-bold text-gray-800">단위:</span>
                            <span class="font-medium text-gray-900">{{ getUnitDisplayName(selectedMaterial?.unit || 'g5') }}</span>
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

                <!-- 기존 배치 정보 표시 -->
                <div v-if="existingPlacements.length > 0" class="bg-yellow-50 p-4 rounded-lg">
                    <h6 class="font-bold text-yellow-900 mb-3">다른 자재 배치 정보</h6>
                    <div class="space-y-2 max-h-32 overflow-y-auto">
                        <div v-for="placement in existingPlacements" :key="`${placement.wareAreaCd}_${placement.source}`" 
                             class="bg-white p-2 rounded border text-sm"
                             :class="placement.source === 'pending' ? 'border-orange-300' : 'border-blue-300'">
                            <div class="flex justify-between items-start">
                                <span class="font-mono text-xs">{{ placement.wareAreaCd }}</span>
                                <div class="text-right">
                                    <span class="font-semibold">{{ placement.moveQty }}{{ placement.unitText || getUnitDisplayName(placement.unit) }}</span>
                                    <div v-if="placement.source === 'pending'" class="text-xs text-orange-600 font-medium">
                                        요청중 ({{ placement.moveReqCd }})
                                    </div>
                                    <div v-else class="text-xs text-blue-600 font-medium">
                                        등록중
                                    </div>
                                </div>
                            </div>
                            <div :class="placement.source === 'pending' ? 'text-orange-600' : 'text-blue-600'" class="font-medium">
                                {{ placement.itemName }}
                                <span v-if="placement.lotNo" class="text-xs text-gray-800 ml-1">({{ placement.lotNo }})</span>
                            </div>
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
                                    <!-- <div class="text-xs text-gray-600">{{ plan.selectedArea.displayName }}</div>
                                    <div class="text-xs text-blue-600">
                                        최대 {{ plan.selectedArea.availableVolume }}{{ getUnitDisplayName(selectedMaterial?.unit || 'g5') }}
                                    </div> -->
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
            <div class="warehouse-panel">
                <!-- 창고/층 선택 -->
                <div class="bg-gray-50 p-4 rounded-lg mb-4 space-y-3">
                    <div class="flex items-center gap-4">
                        <label class="font-bold text-gray-800 min-w-20">공장:</label>
                        <Dropdown
                            v-model="selectedFactory"
                            :options="factoryOptions"
                            optionLabel="label"
                            optionValue="value"
                            placeholder="공장을 선택하세요"
                            class="flex-1"
                        />
                    </div>

                    <div class="flex items-center gap-4" v-if="selectedFactory">
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
                <div v-if="selectedFloor && areaGrid.length > 0" class="warehouse-panel">
                    <h6 class="font-bold text-gray-900 mb-3">구역 선택 ({{ selectedFloor }}층)</h6>

                    <div class="grid-container">
                        <div class="grid" :style="{ gridTemplateColumns: `repeat(${areaGrid[0]?.length || 1}, 1fr)` }">
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
                                    실제용량: ${area.realMaxVolume}${getUnitDisplayName(selectedMaterial?.unit || 'g5')}
                                    현재적재: ${area.currentVolume}${getUnitDisplayName(selectedMaterial?.unit || 'g5')}
                                    가용용량: ${area.availableVolume}${getUnitDisplayName(selectedMaterial?.unit || 'g5')}
                                    ${area.currentMaterial ? '기존자재: ' + area.currentMaterial : ''}
                                    ${area.existingPlacement ? (area.existingPlacement.source === 'pending' ? '요청중자재: ' : '등록중자재: ') + area.existingPlacement.itemName + ' (' + area.existingPlacement.moveQty + (area.existingPlacement.unitText || getUnitDisplayName(area.existingPlacement.unit)) + ')' + (area.existingPlacement.moveReqCd ? ' [' + area.existingPlacement.moveReqCd + ']' : '') : ''}
                                    ${area.isDifferentMaterialSelected ? (area.existingPlacement?.source === 'pending' ? '[선택불가] 다른 자재가 이동요청 중인 구역' : '[선택불가] 다른 자재가 선택된 구역') : ''}
                                    ${!area.isAvailable && !area.isDifferentMaterialSelected ? '[선택불가] 다른 자재가 적재된 구역' : ''}
                                    ${area.availableVolume <= 0 ? '[선택불가] 가용 용량 없음' : ''}`"
                                                                    >
                                    <div class="text-center">
                                        <div class="font-bold text-sm text-gray-900">{{ area.displayName }}</div>
                                        <div class="text-xs mt-1 font-semibold text-gray-800">
                                            {{ getCapacityDisplay(area) }}
                                        </div>
                                        <div v-if="area.existingPlacement" class="text-xs mt-1">
                                            <span :class="area.existingPlacement.source === 'pending' ? 'text-orange-600' : 'text-yellow-600'" class="font-semibold">
                                                {{ area.existingPlacement.itemName }}
                                                <span v-if="area.existingPlacement.source === 'pending'" class="text-xs">(요청중)</span>
                                                <span v-else class="text-xs">(등록중)</span>
                                            </span>
                                        </div>
                                        <div v-else-if="area.currentMaterial && area.currentVolume > 0" class="text-xs mt-1">
                                            <span v-if="area.isSameMaterial" class="text-green-600 font-semibold">동일자재</span>
                                            <span v-else class="text-red-600 font-semibold">다른자재</span>
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
                                            {{ area.availableVolume }}/{{ area.realMaxVolume }}{{ getUnitDisplayName(selectedMaterial?.unit || 'g5') }}
                                        </div>
                                    </div>
                                </div>
                            </template>
                        </div>
                    </div>
                </div>

                <div v-else style="flex: 1; display: flex; align-items: center; justify-content: center; color: #374151;">
                    공장, 창고, 층을 먼저 선택해주세요.
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
:global(.area-modal-mask) {
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

:global(.area-modal-root) {
    width: 95vw !important;
    max-width: 1600px !important;
    height: 95vh !important;
    max-height: 1000px !important;
    background: white !important;
    border-radius: 8px !important;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2) !important;
    display: flex !important;
    flex-direction: column !important;
    overflow: hidden !important;
}

:global(.area-modal-content) {
    flex: 1 !important;
    overflow: hidden !important;
    padding: 0 !important;
}

/* 내부 레이아웃 */
.modal-container {
    display: flex;
    height: 850px;
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