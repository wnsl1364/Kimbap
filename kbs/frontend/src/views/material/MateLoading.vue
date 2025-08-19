<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { useMateLoadingStore } from '@/stores/mateLoadingStore';
import { useCommonStore } from '@/stores/commonStore';
import { useMemberStore } from '@/stores/memberStore'; // 사용자 정보
import { useToast } from 'primevue/usetoast';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import WarehouseAreaSelectModal from '@/views/material/AreaSelectModal.vue';
import { getPendingLoadingPlacements } from '@/api/materials';

// Store 및 Toast
const mateLoadingStore = useMateLoadingStore();
const commonStore = useCommonStore();
const memberStore = useMemberStore(); // 사용자 정보 store
const toast = useToast();

// Store에서 상태 가져오기
const {
    mateLoadingList,
    searchColumns,
    tableColumns,
    filteredMateLoadingList
} = storeToRefs(mateLoadingStore);

// 사용자 정보 가져오기
const { user } = storeToRefs(memberStore);

// 디버깅: 검색 필터 상태 감시 (필요시에만 활성화)

// 로컬 상태
const selectedItems = ref([]);
const warehouseAreaModalVisible = ref(false);
const currentSelectedMaterial = ref(null);
// 대기 중인 적재 계획 정보 (다른 입고건들의 선택된 구역)
const pendingLoadingPlacements = ref([]);

// 공통코드 형변환
const convertUnitCodes = (list) => {
    const unitCodes = commonStore.getCodes('0G');     // 단위코드
    const stoConCodes = commonStore.getCodes('0O');   // 보관조건코드
    
    return list.map(item => {
        const matchedUnit = unitCodes.find(code => code.dcd === item.unit);
        const matchedStoCon = stoConCodes.find(code => code.dcd === item.stoCon);
        const matchedFirstUnit = unitCodes.find(code => code.dcd === item.firstUnit);
        
        return {
            ...item,
            unit: matchedUnit ? matchedUnit.cdInfo : item.unit,
            stoCon: matchedStoCon ? matchedStoCon.cdInfo : item.stoCon,
            firstUnit: matchedFirstUnit ? matchedFirstUnit.cdInfo : item.firstUnit,
        };
    });
};

// 변환된 데이터 computed
const convertedMateLoadingList = computed(() => convertUnitCodes(filteredMateLoadingList.value));

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

// 날짜 필드들 정의
const dateFields = ['inboDt', 'deliDt', 'regDt'];

// 컴포넌트 마운트 시 실행
onMounted(async () => {
    try {
        // 검색 필터 초기화 (전체 목록 표시)
        mateLoadingStore.clearSearchFilter();
        
        // 공통코드 로드
        await Promise.all([
            commonStore.fetchCommonCodes('0G'), // 단위코드
            commonStore.fetchCommonCodes('0O'), // 보관조건코드
        ]);
        
        // 공장 목록 및 자재 적재 대기 목록 조회
        await mateLoadingStore.fetchFactoryList();
        await mateLoadingStore.fetchMateLoadingList();
        
    // 대기 중인 적재 계획 로드
        await loadPendingLoadingPlacements();
    } catch (error) {
        toast.add({
            severity: 'error',
            summary: '초기화 실패',
            detail: '데이터를 불러오는데 실패했습니다.',
            life: 3000
        });
    }
});

// 검색 처리
const handleSearch = async (searchData) => {
    try {
        // Store에 검색 필터 설정 (프론트에서 필터링)
        mateLoadingStore.setSearchFilter(searchData);
        
        toast.add({
            severity: 'success',
            summary: '검색 완료',
            detail: `${filteredMateLoadingList.value.length}건의 자재를 조회했습니다.`,
            life: 3000
        });
    } catch (error) {
        toast.add({
            severity: 'error',
            summary: '검색 실패',
            detail: '검색 중 오류가 발생했습니다.',
            life: 3000
        });
    }
};

// 초기화 처리
const handleReset = async () => {
    try {
        // 검색 필터 완전 초기화
        mateLoadingStore.clearSearchFilter();
        selectedItems.value = [];
        
        toast.add({
            severity: 'info',
            summary: '초기화 완료',
            detail: '검색 조건이 초기화되었습니다.',
            life: 3000
        });
    } catch (error) {
        toast.add({
            severity: 'error',
            summary: '초기화 실패',
            detail: '초기화 중 오류가 발생했습니다.',
            life: 3000
        });
    }
};

// 데이터 변경 처리
const handleDataChange = (newData) => {
    // InputTable에서 데이터 변경 시 처리
    // 전체 목록을 교체하지 않고, 변경된 항목만 업데이트
    // 구역 선택 후 전체 목록이 손실되는 문제 방지
};

// 체크박스 선택 변경 처리 (InputTable에서 호출)
const handleSelectionChange = (newSelection) => {
    selectedItems.value = newSelection || [];
    
    // store에도 즉시 반영
    mateLoadingStore.setSelectedMateLoadings([...selectedItems.value]);
};

//  구역선택 버튼 클릭 처리 (신규)
const handleLocationSelect = (rowData) => {
    // 먼저 체크박스로 선택했는지 확인 (InputTable 수정 없이 동작 제어)
    const isSelected = selectedItems.value.some(item => item.mateInboCd === rowData.mateInboCd);
    if (!isSelected) {
        toast.add({
            severity: 'warn',
            summary: '자재 선택 필요',
            detail: '구역을 선택하려면 먼저 자재를 선택하세요.',
            life: 3000
        });
        return; // 선택 안된 경우 실행 중단
    }

    // 적재 수량이 입력되지 않은 경우 경고
    if (!rowData.qty || rowData.qty <= 0) {
        toast.add({
            severity: 'warn',
            summary: '수량 입력 필요',
            detail: '적재할 수량을 먼저 입력해주세요.',
            life: 3000
        });
        return;
    }
    
    // 전체 수량을 초과하는 경우 경고
    if (rowData.qty > rowData.totalQty) {
        toast.add({
            severity: 'warn',
            summary: '수량 초과',
            detail: `적재 수량이 전체 수량(${rowData.totalQty})을 초과할 수 없습니다.`,
            life: 3000
        });
        return;
    }
    
    // 선택된 자재 정보 설정
    currentSelectedMaterial.value = {
        ...rowData,
        // 모달에서 사용할 총 수량을 사용자가 입력한 적재 수량으로 설정
        totalQty: rowData.qty,
        // 분할적재 지원: 이미 할당된 수량이 있으면 전달
        existingAllocated: rowData.totalAllocated || 0,
        existingPlacementPlan: rowData.placementPlan || [],
        // 공통코드 원본값으로 변환 (API 호출용)
        stoCon: getOriginalStoConCode(rowData.stoCon),
        unit: getOriginalUnitCode(rowData.unit),
        // 입고코드 추가 (동일 자재 구분용)
        mateInboCd: rowData.mateInboCd,
        mcode: rowData.mcode
    };
    
    // 다른 입고건들의 구역 선택 정보도 함께 전달
    
    // 모달 열기
    warehouseAreaModalVisible.value = true;
};

// 공통코드 원본값 조회 함수들
const getOriginalStoConCode = (displayValue) => {
    const stoConCodes = commonStore.getCodes('0O');
    const found = stoConCodes.find(code => code.cdInfo === displayValue);
    return found ? found.dcd : displayValue;
};

const getOriginalUnitCode = (displayValue) => {
    const unitCodes = commonStore.getCodes('0G');
    const found = unitCodes.find(code => code.cdInfo === displayValue);
    return found ? found.dcd : displayValue;
};

// 창고 구역 선택 확인 처리 (신규)
const handleWarehouseAreaConfirm = (selectionData) => {
    try {
        // 선택된 적재 계획을 현재 자재에 저장
        if (currentSelectedMaterial.value && selectionData.placementPlan) {
            const material = mateLoadingList.value.find(item => 
                item.mateInboCd === currentSelectedMaterial.value.mateInboCd
            );
            
            if (material) {
                // 적재 계획 정보를 자재 객체에 저장
                material.placementPlan = selectionData.placementPlan;
                material.totalAllocated = selectionData.totalAllocated;
                material.remainingQty = selectionData.remainingQty;
                // 사용자가 모달에서 입력한 수량도 저장
                material.userInputQty = selectionData.userInputQty;
                
                // Store가 기대하는 wareAreaCd 필드도 설정 (첫 번째 구역을 대표로)
                if (selectionData.placementPlan.length > 0) {
                    material.wareAreaCd = selectionData.placementPlan[0].wareAreaCd;
                    material.qty = selectionData.totalAllocated; // 적재 수량을 총 할당량으로 설정
                }
                
                // 선택된 구역 정보 요약
                material.selectedAreaInfo = {
                    totalAreas: selectionData.placementPlan.length,
                    firstArea: selectionData.placementPlan[0].selectedArea.displayName,
                    totalAllocated: selectionData.totalAllocated,
                    areas: selectionData.placementPlan.map(plan => ({
                        area: plan.selectedArea.displayName,
                        qty: plan.allocateQty,
                        wareAreaCd: plan.wareAreaCd
                    }))
                };
                
                // 자동 체크박스 선택 기능 제거 - 구역 정보만 업데이트
                // 이미 선택된 자재가 있는 경우에만 해당 자재의 구역 정보를 업데이트
                const existingSelectedIndex = selectedItems.value.findIndex(item => 
                    item.mateInboCd === material.mateInboCd
                );
                
                // convertedMateLoadingList에서 해당 아이템을 찾기
                const convertedMaterial = convertedMateLoadingList.value.find(item => 
                    item.mateInboCd === material.mateInboCd
                );
                
                if (convertedMaterial) {
                    // 변환된 데이터에 구역 정보를 업데이트
                    convertedMaterial.placementPlan = material.placementPlan;
                    convertedMaterial.totalAllocated = material.totalAllocated;
                    convertedMaterial.remainingQty = material.remainingQty;
                    convertedMaterial.userInputQty = material.userInputQty;
                    convertedMaterial.wareAreaCd = material.wareAreaCd;
                    convertedMaterial.selectedAreaInfo = material.selectedAreaInfo;
                    convertedMaterial.qty = material.qty;
                    
                    if (existingSelectedIndex >= 0) {
                        // 이미 선택되어 있는 경우에만 해당 항목을 업데이트
                        selectedItems.value[existingSelectedIndex] = { ...convertedMaterial };
                        
                        // store에도 즉시 반영 (이미 선택된 자재만)
                        mateLoadingStore.setSelectedMateLoadings([...selectedItems.value]);
                    }
                }
            }
        }
        
        // 모달 닫기
        warehouseAreaModalVisible.value = false;
        currentSelectedMaterial.value = null;
        
        toast.add({
            severity: 'success',
            summary: '구역 선택 완료',
            detail: `${selectionData.placementPlan.length}개 구역에 총 ${selectionData.totalAllocated}개 적재 예정 (입력수량: ${selectionData.userInputQty}개)`,
            life: 3000
        });
        
    } catch (error) {
        toast.add({
            severity: 'error',
            summary: '구역 선택 실패',
            detail: '창고 구역 선택 처리 중 오류가 발생했습니다.',
            life: 3000
        });
    }
};

// 적재처리 버튼 클릭
const handleProcessLoading = async () => {
    // 체크박스가 체크된 자재가 없는 경우 우선 체크
    if (!selectedItems.value || selectedItems.value.length === 0) {
        toast.add({
            severity: 'warn',
            summary: '자재 선택 필요',
            detail: '적재할 자재를 선택해주세요.',
            life: 3000
        });
        return;
    }

    try {
        // 선택된 자재들 중 구역이 설정되지 않은 자재 확인
        const itemsWithoutArea = selectedItems.value.filter(item => 
            !(item.wareAreaCd && item.wareAreaCd.trim() !== '') &&
            !(item.placementPlan && item.placementPlan.length > 0)
        );
        
        if (itemsWithoutArea.length > 0) {
            const itemNames = itemsWithoutArea.map(item => item.mateName || item.mateInboCd).join(', ');
            toast.add({
                severity: 'warn',
                summary: '구역 선택 필요',
                detail: `다음 자재들의 창고구역을 먼저 선택해주세요: ${itemNames}`,
                life: 5000
            });
            return;
        }
        
    // 선택된 자재들의 구역 설정 상태 확인
        const itemsWithArea = selectedItems.value.filter(item => 
            (item.wareAreaCd && item.wareAreaCd.trim() !== '') ||
            (item.placementPlan && item.placementPlan.length > 0)
        );
        
        // 모든 선택된 자재가 구역 설정이 완료된 경우에만 진행
        if (itemsWithArea.length !== selectedItems.value.length) {
            toast.add({
                severity: 'warn',
                summary: '구역 선택 미완료',
                detail: '선택된 모든 자재의 창고구역을 설정해주세요.',
                life: 3000
            });
            return;
        }
        
        // 선택된 자재들을 store에 설정
        mateLoadingStore.setSelectedMateLoadings([...selectedItems.value]);
        
        // 다중 적재 처리 실행
        const result = await mateLoadingStore.processBatchLoading();
        
        //  store에서 이미 생성된 메시지를 그대로 사용
        const message = result.message || '적재 처리가 완료되었습니다.';
        const skippedCount = result.skippedCount || 0;
        const partiallyCount = result.partiallyProcessedCount || 0;
        
        //  적재 처리 후 반드시 목록 새로고침 먼저 실행
        await mateLoadingStore.fetchMateLoadingList();
        
        //  새로고침 후 선택 항목 완전 초기화
        selectedItems.value = [];
        mateLoadingStore.setSelectedMateLoadings([]);
        
        //  결과에 따른 토스트 메시지 (부분/완전 적재 구분)
        if (skippedCount > 0 || partiallyCount > 0) {
            toast.add({
                severity: 'success', 
                summary: '적재 처리 완료',
                detail: message,
                life: 5000
            });
        } else {
            toast.add({
                severity: 'success',
                summary: '적재 처리 완료',
                detail: message,
                life: 3000
            });
        }
        
    } catch (error) {
        toast.add({
            severity: 'error',
            summary: '적재 처리 실패',
            detail: error.message || '적재 처리 중 오류가 발생했습니다.',
            life: 5000
        });
    }
};

// 행 클릭 처리
const handleRowClick = (rowData) => {
    // TODO: 상세 정보 모달 등 구현
};

//  selectedItems 변경 감지 (체크박스 선택/해제)
watch(selectedItems, (newSelection) => {
    // store에 즉시 반영
    mateLoadingStore.setSelectedMateLoadings([...newSelection]);
}, { deep: true });

// 대기 중인 적재 계획 로드 함수
const loadPendingLoadingPlacements = async () => {
    try {
        // 현재 화면에서 선택된 자재들의 계획 정보 수집
        const currentPlacements = selectedItems.value
            .filter(item => item.placementPlan && item.placementPlan.length > 0)
            .flatMap(item => 
                item.placementPlan.map(plan => ({
                    wareAreaCd: plan.wareAreaCd,
                    mateInboCd: item.mateInboCd,
                    mcode: item.mcode,
                    mateName: item.mateName,
                    allocateQty: plan.allocateQty,
                    source: 'current'
                }))
            );
        
        pendingLoadingPlacements.value = currentPlacements;
    } catch (error) {
        pendingLoadingPlacements.value = [];
    }
};

//  구역 선택 시 사용할 기존 배치 정보 생성
const getPendingPlacementsForArea = () => {
    // 현재 선택된 자재를 제외한 다른 자재들의 배치 계획
    const otherMaterialPlacements = selectedItems.value
        .filter(item => 
            item.mateInboCd !== currentSelectedMaterial.value?.mateInboCd &&
            item.placementPlan && 
            item.placementPlan.length > 0
        )
        .flatMap(item => 
            item.placementPlan.map(plan => ({
                wareAreaCd: plan.wareAreaCd,
                mateInboCd: item.mateInboCd,
                mcode: item.mcode,
                mateName: item.mateName,
                allocateQty: plan.allocateQty,
                source: 'pending'
            }))
        );
    
    return otherMaterialPlacements;
};
</script>

<template>
    <!-- 검색 폼 -->
    <div class="space-y-4 mb-2">
        <SearchForm 
            :columns="searchColumns"
            title="자재적재 대기 목록"
            @search="handleSearch"
            @reset="handleReset"
            :gridColumns="3"
        />
    </div>

    <!-- 자재 적재 대기 목록 테이블 -->
    <div>
        <InputTable 
            :data="convertedMateLoadingList"
            :columns="tableColumns"
            v-model:selection="selectedItems"
            :dataKey="'mateInboCd'"
            :selectionMode="'multiple'"
            :height="'55vh'"
            :scrollHeight="'45vh'"
            :enableSelection="true"
            :enableRowActions="false"
            :showRowCount="true"
            :dateFields="dateFields"
            :buttons="{ 
              save: { show: true, label: '적재처리', severity: 'success' },
              reset: { show: false, label: '초기화', severity: 'secondary' },
              delete: { show: false, label: '삭제', severity: 'danger' },
              load: { show: false, label: '불러오기', severity: 'info' },
              refund: { show: false, label: '반품요청', severity: 'help' },
              refundReq: { show: false, label: '반품처리', severity: 'info' },
            }"
            @dataChange="handleDataChange"
            @rowClick="handleRowClick"
            @locationSelect="handleLocationSelect"
            @save="handleProcessLoading"
        />
    </div>

    <!-- 창고 구역 선택 모달 (신규) -->
    <WarehouseAreaSelectModal
        v-model:visible="warehouseAreaModalVisible"
        :selectedMaterial="currentSelectedMaterial"
        :loadingQuantity="currentSelectedMaterial?.qty || 0"
        :existingPlacements="getPendingPlacementsForArea()"
        @confirm="handleWarehouseAreaConfirm"
    />
</template>

<style scoped>
</style>