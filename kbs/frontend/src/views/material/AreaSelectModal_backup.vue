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

// 모달 상태
const modalVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
});

// 데이터 상태
const warehouseTypes = ref([]);
const selectedWarehouseType = ref(null);
const selectedFloor = ref(null);
const warehouseAreas = ref([]);
const selectedAreas = ref([]);
const placementPlan = ref([]);
const modalInputQty = ref(0);

const remainingQty = computed(() => {
    const totalAllocated = placementPlan.value.reduce((sum, plan) => sum + plan.allocateQty, 0);
    return (modalInputQty.value || 0) - totalAllocated;
});

// 창고 및 층 옵션
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

// 확인 버튼 활성화
const isConfirmEnabled = computed(() => {
    return placementPlan.value.length > 0 && 
           placementPlan.value.every(plan => plan.allocateQty > 0);
});

// 구역 그리드 생성
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

// 구역 선택 처리
const selectArea = (area) => {
    if (!area.isAvailable) {
        toast.add({
            severity: 'warn',
            summary: '구역 선택 불가',
            detail: `다른 자재(${area.currentMaterial})가 적재된 구역입니다.`,
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
    if (selectedAreas.value.some(selected => selected.wareAreaCd === area.wareAreaCd)) {
        selectedAreas.value = selectedAreas.value.filter(selected => selected.wareAreaCd !== area.wareAreaCd);
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
        maxAllowedQty: Math.min(remainingQty.value + 0, area.availableVolume)
    });
    
    toast.add({
        severity: 'success',
        summary: '구역 선택됨',
        detail: `${area.displayName} 구역이 선택되었습니다. 수량을 입력해주세요.`,
        life: 2000
    });
};

// 용량 초과 상황 처리 (자동 분할 제안)
const handleOverflowSituation = async (area) => {
    try {
        // 동일 자재가 적재된 다른 구역들 조회
        const response = await getSameMaterialAreas(
            props.selectedMaterial?.mcode, 
            props.selectedMaterial?.fcode, 
            area.wareAreaCd
        );
        
        const sameMaterialAreas = response.data.filter(sameMateArea => 
            sameMateArea.availableVolume > 0
        );
        
        if (sameMaterialAreas.length > 0) {
            const unitDisplayName = getUnitDisplayName(props.selectedMaterial?.unit || 'g5');
            const confirm = window.confirm(
                `선택한 구역 용량: ${area.realAvailableVolume}${unitDisplayName}\n` +
                `필요 용량: ${remainingQty.value}${unitDisplayName}\n\n` +
                `같은 자재가 있는 다른 구역 ${sameMaterialAreas.length}개에 자동 분할 적재하시겠습니까?\n` +
                `(수동 분할을 원하시면 "취소"를 누르세요)`
            );
            
            if (confirm) {
                await executeAutoSplit(area, sameMaterialAreas);
            } else {
                showSplitModalForArea(area);
            }
        } else {
            // 같은 자재가 없으면 수동 선택
            showSplitModalForArea(area);
        }
    } catch (error) {
        console.error('동일 자재 구역 조회 실패:', error);
        // 실패 시 수동 분할로 처리
        showSplitModalForArea(area);
    }
};

// 자동 분할 적재 실행
const executeAutoSplit = async (primaryArea, sameMaterialAreas) => {
    let remainingAmount = remainingQty.value;
    const newPlans = [];
    const newSelectedAreas = [];
    
    // 1순위: 선택한 구역에 최대한 적재
    const primaryQty = Math.min(remainingAmount, primaryArea.realAvailableVolume);
    if (primaryQty > 0) {
        newPlans.push({
            wareAreaCd: primaryArea.wareAreaCd,
            allocateQty: primaryQty,
            selectedArea: primaryArea
        });
        newSelectedAreas.push(primaryArea);
        remainingAmount -= primaryQty;
    }
    
    // 2순위: 같은 자재 있는 구역에 순차 적재
    for (const sameMateArea of sameMaterialAreas) {
        if (remainingAmount <= 0) break;
        
        const available = sameMateArea.availableVolume;
        const qtyToPlace = Math.min(remainingAmount, available);
        
        if (qtyToPlace > 0) {
            // 해당 구역을 areaGrid에서 찾기
            const areaInGrid = findAreaInGrid(sameMateArea.wareAreaCd);
            if (areaInGrid) {
                newPlans.push({
                    wareAreaCd: sameMateArea.wareAreaCd,
                    allocateQty: qtyToPlace,
                    selectedArea: areaInGrid
                });
                newSelectedAreas.push(areaInGrid);
                remainingAmount -= qtyToPlace;
            }
        }
    }
    
    // 계획 적용
    placementPlan.value.push(...newPlans);
    selectedAreas.value.push(...newSelectedAreas);
    updateRemainingQty();
    
    // 결과 알림
    if (remainingAmount > 0) {
        toast.add({
            severity: 'warn',
            summary: '자동 분할 완료',
            detail: `남은 수량: ${remainingAmount}\n추가 구역을 선택해주세요.`,
            life: 5000
        });
    } else {
        toast.add({
            severity: 'success',
            summary: '자동 분할 적재 완료!',
            detail: `${newPlans.length}개 구역에 분배되었습니다.`,
            life: 3000
        });
    }
};

// 그리드에서 구역 찾기 헬퍼 함수
const findAreaInGrid = (wareAreaCd) => {
    for (const row of areaGrid.value) {
        for (const area of row) {
            if (area.wareAreaCd === wareAreaCd) {
                return area;
            }
        }
    }
    return null;
};

// 구역별 수량 입력 처리
const updateAreaQuantity = (planIndex, newQty) => {
    const plan = placementPlan.value[planIndex];
    if (!plan) return;
    
    newQty = Math.max(0, newQty || 0);
    
    if (newQty > plan.selectedArea.availableVolume) {
        toast.add({
            severity: 'warn',
            summary: '수량 초과',
            detail: `${plan.selectedArea.displayName} 구역의 최대 용량을 초과할 수 없습니다.`,
            life: 3000
        });
        newQty = plan.selectedArea.availableVolume;
    }
    
    const otherPlansTotal = placementPlan.value
        .filter((_, index) => index !== planIndex)
        .reduce((sum, p) => sum + p.allocateQty, 0);
    
    if (otherPlansTotal + newQty > modalInputQty.value) {
        const maxAllowed = modalInputQty.value - otherPlansTotal;
        if (maxAllowed <= 0) {
            toast.add({
                severity: 'warn',
                summary: '수량 초과',
                detail: '전체 입력 수량을 초과할 수 없습니다.',
                life: 3000
            });
            newQty = 0;
        } else {
            newQty = maxAllowed;
        }
    }
    
    plan.allocateQty = newQty;
    
    // 모든 구역의 maxAllowedQty 재계산
    const newTotal = placementPlan.value.reduce((sum, p) => sum + p.allocateQty, 0);
    placementPlan.value.forEach(p => {
        const others = newTotal - p.allocateQty;
        p.maxAllowedQty = Math.min(modalInputQty.value - others, p.selectedArea.availableVolume);
    });
};

// 분할 적재 모달 표시
const showSplitModalForArea = (area) => {
    splitModalData.value = {
        area,
        maxQty: Math.min(remainingQty.value, area.realAvailableVolume),
        inputQty: Math.min(remainingQty.value, area.realAvailableVolume)
    };
    showSplitModal.value = true;
};

// 분할 적재 실행
const executeSplitPlacement = async () => {
    const { area, inputQty } = splitModalData.value;
    
    if (inputQty <= 0 || inputQty > area.realAvailableVolume) {
        toast.add({
            severity: 'error',
            summary: '입력 오류',
            detail: '올바른 수량을 입력해주세요.',
            life: 3000
        });
        return;
    }
    
    if (inputQty > remainingQty.value) {
        toast.add({
            severity: 'error',
            summary: '입력 오류',
            detail: '남은 수량을 초과할 수 없습니다.',
            life: 3000
        });
        return;
    }
    
    try {
        // 백엔드 API로 적재 가능 여부 검증
        await validateAreaAllocation(area.wareAreaCd, props.selectedMaterial?.mcode, inputQty);
        
        const newPlan = {
            wareAreaCd: area.wareAreaCd,
            allocateQty: inputQty,
            selectedArea: area
        };
        
        selectedAreas.value.push(area);
        placementPlan.value.push(newPlan);
        updateRemainingQty();
        
        showSplitModal.value = false;
        
        toast.add({
            severity: 'success',
            summary: '분할 적재 등록',
            detail: `${area.displayName}에 ${inputQty} 적재 등록`,
            life: 3000
        });
    } catch (error) {
        console.error('분할 적재 검증 실패:', error);
        toast.add({
            severity: 'error',
            summary: '적재 검증 실패',
            detail: '적재 수량을 다시 확인해주세요.',
            life: 3000
        });
    }
};

// 남은 수량 업데이트
const updateRemainingQty = () => {
    const totalAllocated = placementPlan.value.reduce((sum, plan) => sum + plan.allocateQty, 0);
    // 🔥 모달 내 입력 수량을 기준으로 계산
    remainingQty.value = (modalInputQty.value || 0) - totalAllocated;
};

// 적재 계획 제거
const removePlan = (index) => {
    const removedPlan = placementPlan.value[index];
    placementPlan.value.splice(index, 1);
    selectedAreas.value = selectedAreas.value.filter(area => area.wareAreaCd !== removedPlan.wareAreaCd);
    updateRemainingQty();
    
    toast.add({
        severity: 'info',
        summary: '적재 계획 제거',
        detail: `${removedPlan.selectedArea.displayName} 구역이 제거되었습니다.`,
        life: 3000
    });
};

// 구역 스타일
const getAreaStyle = (area) => {
    const isSelected = selectedAreas.value.some(selected => selected.wareAreaCd === area.wareAreaCd);
    
    if (isSelected) {
        return 'bg-blue-500 text-white border-blue-600';
    }
    // 🔥 다른 자재가 있는 구역 - 빨간색으로 강조하고 클릭 차단
    if (!area.isAvailable) {
        return 'bg-red-200 text-red-900 border-red-400 cursor-not-allowed opacity-75';
    }
    // 🔥 같은 자재가 있는 구역 - 초록색으로 표시
    if (area.isSameMaterial) {
        return 'bg-green-100 text-green-800 border-green-300 hover:bg-green-200';
    }
    // 🔥 용량이 없는 구역 - 회색으로 표시하고 클릭 차단
    if (area.realAvailableVolume <= 0) {
        return 'bg-gray-200 text-gray-600 border-gray-400 cursor-not-allowed opacity-75';
    }
    // 빈 구역 - 일반 스타일
    return 'bg-white hover:bg-blue-50 border-gray-300 hover:border-blue-400';
};

// ===== 단위별 용량 계산 시스템 =====

// 단위명 캐시
const unitDisplayCache = ref(new Map());
const storageConditionCache = ref(new Map());

// 공통코드에서 단위명 가져오기 (캐시 적용)
const getUnitDisplayName = (unitCode) => {
    if (unitDisplayCache.value.has(unitCode)) {
        return unitDisplayCache.value.get(unitCode);
    }
    
    const unitCodes = commonStore.getCodes('0G') || [];
    const unit = unitCodes.find(code => code.dcd === unitCode);
    const result = unit ? unit.cdInfo : unitCode;
    
    unitDisplayCache.value.set(unitCode, result);
    return result; // 변환 실패시 원본 코드 반환
};

// 공통코드에서 보관조건명 가져오기 (캐시 적용)
const getStorageConditionDisplayName = (stoConCode) => {
    if (storageConditionCache.value.has(stoConCode)) {
        return storageConditionCache.value.get(stoConCode);
    }
    
    const stoConCodes = commonStore.getCodes('0O') || [];
    const stoCon = stoConCodes.find(code => code.dcd === stoConCode);
    const result = stoCon ? stoCon.cdInfo : stoConCode;
    
    storageConditionCache.value.set(stoConCode, result);
    return result; // 변환 실패시 원본 코드 반환
};


// 자재 단위별 기준 용량 설정 (공통코드 기반)
const getUnitCapacityStandard = (unitCode) => {
    const standards = {
        'g1': 1000000,     // g(그램)
        'g2': 1000,      // kg(킬로그램): 1000kg
        'g3': 1000000,    // ml(밀리리터)
        'g4': 1000,      // L(리터): 1000L
        'g5': 1000,      // ea(개): 1000개
        'g6': 100,       // box(박스): 100박스
        'g7': 5000000,    // mm(밀리미터)
    };
    
    return standards[unitCode?.toLowerCase()] || 500; // 기본값: 500
};

// 실제 용량 계산 (단위 기반, 구역별 차등 없음)
const getRealCapacity = (area) => {
    const materialUnit = props.selectedMaterial?.unit || 'g5';
    const standardCapacity = getUnitCapacityStandard(materialUnit);
    
    // 모든 구역이 동일한 용량 (차등 제거)
    return standardCapacity;
};

// 용량 표시 (퍼센트 기반)
const getCapacityDisplay = (area) => {
    const current = area.currentVolume || 0;
    const realCapacity = getRealCapacity(area);
    const percentage = Math.round((current / realCapacity) * 100);
    
    return `${percentage}%`;
};


// 사용률 퍼센트 계산 (실제 용량 기준)
const getUsagePercentage = (area) => {
    const current = area.currentVolume || 0;
    const realCapacity = getRealCapacity(area);
    return Math.round((current / realCapacity) * 100);
};

// 용량 상태 색상
const getCapacityColor = (area) => {
    const percentage = getUsagePercentage(area);
    if (percentage === 0) return 'bg-gray-200';
    if (percentage <= 30) return 'bg-green-500';
    if (percentage <= 60) return 'bg-yellow-500';
    if (percentage <= 85) return 'bg-orange-500';
    return 'bg-red-500';
};

// 확인 처리
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
    
    if (remainingQty.value > 0) {
        const confirm = window.confirm(
            `남은 수량이 ${remainingQty.value} 있습니다.\n` +
            `그래도 적재를 진행하시겠습니까?`
        );
        if (!confirm) return;
    }
    
    // 데이터를 emit하고 모달 닫기 (초기화 없이)
    emit('confirm', {
        placementPlan: placementPlan.value,
        // 🔥 모달 내 입력 수량을 기준으로 계산
        totalAllocated: (modalInputQty.value || 0) - remainingQty.value,
        remainingQty: remainingQty.value,
        // 🔥 사용자가 입력한 수량도 전달
        userInputQty: modalInputQty.value
    });
    
    modalVisible.value = false;
};

// 취소 처리
const handleCancel = () => {
    if (placementPlan.value.length > 0) {
        // 적재 계획이 있을 때만 확인 메시지
        const shouldReset = window.confirm('적재 계획을 취소하시겠습니까? 선택한 내용이 모두 삭제됩니다.');
        if (!shouldReset) {
            return; // 취소 취소
        }
        resetModal(); // 데이터 초기화
    }
    modalVisible.value = false;
};

// 모달 초기화
const resetModal = () => {
    selectedWarehouseType.value = null;
    selectedFloor.value = null;
    selectedAreas.value = [];
    placementPlan.value = [];
    remainingQty.value = 0;
    // 🔥 외부에서 받은 적재 수량으로 초기화
    modalInputQty.value = props.loadingQuantity || 0;
};

// 창고 유형 변경 시 층 초기화
watch(selectedWarehouseType, () => {
    selectedFloor.value = null;
    selectedAreas.value = [];
    placementPlan.value = [];
    updateRemainingQty();
});

// 층 변경 시 구역 선택 초기화
watch(selectedFloor, () => {
    selectedAreas.value = [];
    placementPlan.value = [];
    updateRemainingQty();
    // 구역 정보 로드
    loadWarehouseAreas();
});

// 모달이 열릴 때 초기화 (새로운 자재일 때만)
watch(() => props.visible, (newVal, oldVal) => {
    if (newVal && !oldVal) { // 모달이 열릴 때만
        // 이전 자재와 다른 자재인지 확인
        const currentMaterialId = props.selectedMaterial?.mateInboCd || props.selectedMaterial?.mcode;
        const storedMaterialId = sessionStorage.getItem('lastSelectedMaterialId');
        
        if (currentMaterialId !== storedMaterialId) {
            // 다른 자재인 경우에만 초기화
            resetModal();
            sessionStorage.setItem('lastSelectedMaterialId', currentMaterialId);
        }
        
        // 공통코드 로드 (단위 코드, 보관조건 코드)
        console.log('공통코드 로드 시작...');
        Promise.all([
            commonStore.fetchCommonCodes('0G'),
            commonStore.fetchCommonCodes('0O')
        ]).then(() => {
            console.log('공통코드 로드 완료:', {
                '0G': commonStore.getCodes('0G'),
                '0O': commonStore.getCodes('0O')
            });
        }).catch(error => {
            console.error('공통코드 로드 실패:', error);
        });
        
        loadWarehouseTypes();
        // 🔥 외부에서 받은 적재 수량 설정
        modalInputQty.value = props.loadingQuantity || props.selectedMaterial?.totalQty || 0;
        
        // 기본 적재 수량을 외부에서 받은 값으로 설정 (초기화된 경우에만)
        if (remainingQty.value === 0) {
            remainingQty.value = props.loadingQuantity || props.selectedMaterial?.totalQty || 0;
        }
    }
});

// 🔥 외부에서 받은 적재 수량 변경 감시
watch(() => props.loadingQuantity, (newQty) => {
    if (newQty && newQty > 0) {
        modalInputQty.value = newQty;
        const totalAllocated = placementPlan.value.reduce((sum, plan) => sum + plan.allocateQty, 0);
        remainingQty.value = newQty - totalAllocated;
        
        console.log('외부 적재 수량 변경:', { 
            입력수량: newQty, 
            할당된수량: totalAllocated, 
            남은수량: remainingQty.value 
        });
    }
});

// API 호출 함수들
const loadWarehouseTypes = async () => {
    try {
        console.log('창고 조회 시작 - 공장코드:', props.selectedMaterial?.fcode);
        
        const response = await getWarehousesByFactory(props.selectedMaterial?.fcode);
        
        console.log('받은 데이터:', response.data);
        
        warehouseTypes.value = response.data;
        
        console.log('warehouseTypeOptions:', warehouseTypeOptions.value);
        
    } catch (error) {
        console.error('창고 유형 로드 실패:', error);
        toast.add({
            severity: 'error',
            summary: '로드 실패',
            detail: '창고 정보를 불러오는데 실패했습니다.',
            life: 3000
        });
    }
};

const loadWarehouseAreas = async () => {
    if (!selectedWarehouseType.value || !selectedFloor.value) return;
    
    try {
        console.log('창고 구역 조회 시작 - 창고코드:', selectedWarehouseType.value, '층:', selectedFloor.value);
        
        const response = await getWarehouseAreasWithStock(selectedWarehouseType.value, selectedFloor.value);
        
        console.log('창고 구역 데이터:', response.data);
        
        warehouseAreas.value = response.data;
    } catch (error) {
        console.error('창고 구역 로드 실패:', error);
        toast.add({
            severity: 'error',
            summary: '로드 실패',
            detail: '창고 구역 정보를 불러오는데 실패했습니다.',
            life: 3000
        });
    }
};

// 컴포넌트 마운트 시 공통코드 미리 로드
onMounted(() => {
    console.log('AreaSelectModal 마운트됨 - 공통코드 미리 로드');
    Promise.all([
        commonStore.fetchCommonCodes('0G'),
        commonStore.fetchCommonCodes('0O')
    ]).then(() => {
        console.log('onMounted에서 공통코드 로드 완료:', {
            '0G': commonStore.getCodes('0G'),
            '0O': commonStore.getCodes('0O')
        });
    }).catch(error => {
        console.error('onMounted에서 공통코드 로드 실패:', error);
    });
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
            <!-- 왼쪽: 고정 정보 패널 -->
            <div class="w-80 flex-shrink-0 space-y-4 overflow-y-auto">
                <!-- 자재 정보 -->
                <div class="bg-blue-50 p-4 rounded-lg">
                    <h6 class="font-semibold text-blue-800 mb-3">선택된 자재 정보</h6>
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
                            <span>{{ getStorageConditionDisplayName(selectedMaterial?.stoCon || 'o1') }} ({{ selectedMaterial?.stoCon || 'o1' }})</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="font-medium">단위:</span>
                            <span>{{ getUnitDisplayName(selectedMaterial?.unit || 'g5') }} ({{ selectedMaterial?.unit || 'g5' }})</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="font-medium">적재할 수량:</span>
                            <span class="font-bold text-blue-600">{{ modalInputQty }}{{ getUnitDisplayName(selectedMaterial?.unit || 'g5') }}</span>
                        </div>
                        <div class="flex justify-between">
                            <span class="font-medium">전체수량:</span>
                            <span>{{ selectedMaterial?.totalQty }}{{ getUnitDisplayName(selectedMaterial?.unit || 'g5') }}</span>
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
                    <h6 class="font-semibold text-green-800 mb-3">적재 계획 (구역별 수량 입력)</h6>
                    <div class="space-y-3 max-h-48 overflow-y-auto">
                        <div v-for="(plan, index) in placementPlan" :key="index" 
                             class="bg-white p-3 rounded border">
                            <div class="flex justify-between items-start mb-2">
                                <div>
                                    <div class="font-mono text-sm font-semibold">{{ plan.wareAreaCd }}</div>
                                    <div class="text-xs text-gray-600">{{ plan.selectedArea.displayName }}</div>
                                    <div class="text-xs text-blue-600">
                                        최대 {{ plan.selectedArea.realAvailableVolume }}{{ getUnitDisplayName(selectedMaterial?.unit || 'g5') }}
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
                            
                            <!-- 🔥 구역별 수량 입력 -->
                            <div class="flex items-center gap-2">
                                <label class="text-sm font-medium min-w-12">수량:</label>
                                <InputNumber
                                    :modelValue="plan.allocateQty"
                                    @update:modelValue="(newValue) => updateAreaQuantity(index, newValue || 0)"
                                    :min="0"
                                    :max="plan.maxAllowedQty || plan.selectedArea.realAvailableVolume"
                                    :step="1"
                                    class="flex-1"
                                    :suffix="` ${getUnitDisplayName(selectedMaterial?.unit || 'g5')}`"
                                    placeholder="수량 입력"
                                />
                            </div>
                        </div>
                    </div>
                    <div class="mt-3 pt-2 border-t bg-white px-3 py-2 rounded">
                        <div class="flex justify-between text-sm font-semibold">
                            <span>총 적재량:</span>
                            <span class="text-green-700">{{ (selectedMaterial?.totalQty || 0) - remainingQty }}{{ getUnitDisplayName(selectedMaterial?.unit || 'g5') }}</span>
                        </div>
                        <div class="flex justify-between text-sm text-gray-600 mt-1">
                            <span>남은 수량:</span>
                            <span :class="remainingQty > 0 ? 'text-orange-600' : 'text-green-600'">
                                {{ remainingQty }}{{ getUnitDisplayName(selectedMaterial?.unit || 'g5') }}
                            </span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 오른쪽: 창고 선택 영역 -->
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
                            placeholder="창고 유형을 선택하세요"
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
                    <div class="flex justify-between items-center mb-3">
                        <h6 class="font-semibold">구역 선택 ({{ selectedFloor }}층)</h6>
                        
                        <div class="flex items-center gap-3">
                            <!-- 단위 정보 표시 -->
                            <div class="text-sm bg-blue-100 px-3 py-1 rounded-lg">
                                <span class="font-medium">단위:</span> {{ getUnitDisplayName(selectedMaterial?.unit || 'g5') }}
                            </div>
                            
                            <!-- 범례 -->
                            <div class="flex gap-3 text-xs">
                                <div class="flex items-center gap-1">
                                    <div class="w-3 h-3 bg-white border border-gray-300 rounded"></div>
                                    <span>선택 가능</span>
                                </div>
                                <div class="flex items-center gap-1">
                                    <div class="w-3 h-3 bg-green-100 border border-green-300 rounded"></div>
                                    <span>동일 자재</span>
                                </div>
                                <div class="flex items-center gap-1">
                                    <div class="w-3 h-3 bg-red-200 border border-red-400 rounded opacity-75"></div>
                                    <span>다른 자재 (선택불가)</span>
                                </div>
                                <div class="flex items-center gap-1">
                                    <div class="w-3 h-3 bg-gray-200 border border-gray-400 rounded opacity-75"></div>
                                    <span>용량 부족</span>
                                </div>
                                <div class="flex items-center gap-1">
                                    <div class="w-3 h-3 bg-blue-500 border border-blue-600 rounded"></div>
                                    <span>선택됨</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 구역 그리드 (스크롤 가능) -->
                    <div class="flex-1 overflow-auto border rounded-lg p-4 bg-white">
                        <div class="grid gap-2" :style="{ gridTemplateColumns: `repeat(${areaGrid[0]?.length || 1}, 1fr)` }">
                            <template v-for="(row, rowIndex) in areaGrid" :key="rowIndex">
                                <div
                                    v-for="(area, colIndex) in row"
                                    :key="`${rowIndex}-${colIndex}`"
                                    :class="[
                                        'border-2 rounded-lg p-3 transition-all duration-200 min-h-20 min-w-16',
                                        getAreaStyle(area),
                                        // 🔥 클릭 가능 여부에 따라 커서 스타일 적용
                                        area.isAvailable && area.realAvailableVolume > 0 ? 'cursor-pointer' : 'cursor-not-allowed'
                                    ]"
                                    @click="area.isAvailable && area.realAvailableVolume > 0 ? selectArea(area) : null"
                                    :title="`구역: ${area.wareAreaCd}
실제용량: ${getRealCapacity(area)}${getUnitDisplayName(selectedMaterial?.unit || 'g5')}
현재적재: ${area.currentVolume || 0}${getUnitDisplayName(selectedMaterial?.unit || 'g5')} 
가용용량: ${area.realAvailableVolume}${getUnitDisplayName(selectedMaterial?.unit || 'g5')}
${area.currentMaterial ? '기존자재: ' + area.currentMaterial : ''}
${!area.isAvailable ? '[선택불가] 다른 자재가 적재된 구역' : ''}
${area.realAvailableVolume <= 0 ? '[선택불가] 가용 용량 없음' : ''}`"
                                >
                                    <div class="text-center">
                                        <div class="font-bold text-sm">{{ area.displayName }}</div>
                                        <div class="text-xs mt-1">
                                            {{ getCapacityDisplay(area) }}
                                        </div>
                                        <!-- 🔥 자재 상태 표시 개선 -->
                                        <div v-if="area.currentMaterial" class="text-xs mt-1 truncate">
                                            <span v-if="area.currentMaterial === props.selectedMaterial?.mcode" 
                                                  class="text-green-600 font-semibold">동일자재</span>
                                            <span v-else class="text-red-600 font-semibold">다른자재</span>
                                        </div>
                                        <div v-else class="text-xs mt-1 text-gray-500">
                                            빈구역
                                        </div>
                                        <!-- 용량 게이지 바 -->
                                        <div class="w-full bg-gray-200 rounded-full h-1.5 mt-1">
                                            <div 
                                                :class="`h-1.5 rounded-full transition-all duration-300 ${getCapacityColor(area)}`"
                                                :style="{ width: getUsagePercentage(area) + '%' }"
                                            ></div>
                                        </div>
                                        
                                        <!-- 실제 용량 정보 (작은 글씨) -->
                                        <div class="text-xs text-gray-600 mt-1">
                                            {{ area.realAvailableVolume }}/{{ getRealCapacity(area) }}{{ getUnitDisplayName(selectedMaterial?.unit || 'g5') }}
                                        </div>
                                    </div>
                                </div>
                            </template>
                        </div>
                    </div>
                </div>

                <!-- 안내 메시지 -->
                <div v-else-if="selectedWarehouseType && selectedFloor" class="flex-1 flex items-center justify-center text-gray-500">
                    구역 정보를 불러오는 중...
                </div>
                <div v-else class="flex-1 flex items-center justify-center text-gray-500">
                    창고와 층을 먼저 선택해주세요.
                </div>
            </div>
        </div>

        <template #footer>
            <div class="flex justify-end gap-2">
                <Button
                    label="취소"
                    severity="secondary"
                    @click="handleCancel"
                />
                <Button
                    label="확인"
                    severity="success"
                    @click="handleConfirm"
                    :disabled="!isConfirmEnabled"
                />
            </div>
        </template>
    </Dialog>

    <!-- 분할 적재 모달 -->
    <Dialog
        v-model:visible="showSplitModal"
        modal
        header="분할 적재"
        :style="{ width: '400px' }"
        :closable="true"
    >
        <div class="space-y-4">
                <div class="bg-blue-50 p-3 rounded">
                    <div class="text-sm space-y-1">
                        <div><strong>구역:</strong> {{ splitModalData.area?.displayName }}</div>
                        <div><strong>구역코드:</strong> {{ splitModalData.area?.wareAreaCd }}</div>
                        <div><strong>가용 용량:</strong> {{ splitModalData.area?.realAvailableVolume }}{{ getUnitDisplayName(selectedMaterial?.unit || 'g5') }}</div>
                        <div><strong>남은 수량:</strong> {{ remainingQty }}{{ getUnitDisplayName(selectedMaterial?.unit || 'g5') }}</div>
                    </div>
                </div>            <div>
                <label class="block font-semibold mb-2">적재할 수량:</label>
                <InputNumber
                    v-model="splitModalData.inputQty"
                    :max="splitModalData.maxQty"
                    :min="1"
                    class="w-full"
                />
                <div class="text-xs text-gray-500 mt-1">
                    최대 {{ splitModalData.maxQty }}{{ getUnitDisplayName(selectedMaterial?.unit || 'g5') }} 적재 가능
                </div>
            </div>
        </div>
        
        <template #footer>
            <div class="flex gap-2">
                <Button
                    label="취소"
                    severity="secondary"
                    @click="showSplitModal = false"
                    class="flex-1"
                />
                <Button
                    label="적재 등록"
                    severity="primary"
                    @click="executeSplitPlacement"
                    class="flex-1"
                />
            </div>
        </template>
    </Dialog>
</template>

<style scoped>
.grid {
    max-width: 100%;
    overflow-x: auto;
}

/* 스크롤바 스타일링 */
::-webkit-scrollbar {
    width: 6px;
    height: 6px;
}

::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
}

::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
}

/* 그리드 반응형 */
@media (max-width: 1200px) {
    .min-w-16 {
        min-width: 3rem;
    }
    .min-h-20 {
        min-height: 4rem;
    }
}

@media (max-width: 768px) {
    .min-w-16 {
        min-width: 2.5rem;
    }
    .min-h-20 {
        min-height: 3rem;
    }
}
</style>