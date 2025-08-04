<script setup>
import { ref, onMounted, computed, nextTick, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { useMateLoadingStore } from '@/stores/mateLoadingStore';
import { useCommonStore } from '@/stores/commonStore';
import { useMemberStore } from '@/stores/memberStore'; // 🔥 사용자 정보
import { useToast } from 'primevue/usetoast';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import WarehouseAreaSelectModal from '@/views/material/AreaSelectModal.vue';

// Store 및 Toast
const mateLoadingStore = useMateLoadingStore();
const commonStore = useCommonStore();
const memberStore = useMemberStore(); // 🔥 사용자 정보 store
const toast = useToast();

// Store에서 상태 가져오기
const {
    mateLoadingList,
    selectedMateLoadings,
    factoryList,
    isLoading,
    searchFilter,
    searchColumns,
    tableColumns,
    hasSelectedItems,
    selectedCount,
    filteredMateLoadingList
} = storeToRefs(mateLoadingStore);

// 🔥 사용자 정보 가져오기
const { user } = storeToRefs(memberStore);

// 🔥 디버깅: 검색 필터 상태 감시
watch(searchFilter, (newFilter) => {
    console.log('검색 필터 변경:', newFilter);
    console.log('전체 목록 크기:', mateLoadingList.value.length);
    console.log('필터링된 목록 크기:', filteredMateLoadingList.value.length);
}, { deep: true });

// 로컬 상태
const selectedItems = ref([]);
const warehouseAreaModalVisible = ref(false);
const currentSelectedMaterial = ref(null);

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
        // 🔥 검색 필터 초기화 (전체 목록 표시)
        mateLoadingStore.clearSearchFilter();
        
        // 공통코드 로드
        await Promise.all([
            commonStore.fetchCommonCodes('0G'), // 단위코드
            commonStore.fetchCommonCodes('0O'), // 보관조건코드
        ]);
        
        // 공장 목록 및 자재 적재 대기 목록 조회
        await mateLoadingStore.fetchFactoryList();
        await mateLoadingStore.fetchMateLoadingList();
        
        console.log('MateLoading 컴포넌트 초기화 완료');
    } catch (error) {
        console.error('MateLoading 컴포넌트 초기화 실패:', error);
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
        console.log('검색 조건:', searchData);
        
        // Store에 검색 필터 설정 (프론트에서 필터링)
        mateLoadingStore.setSearchFilter(searchData);
        
        toast.add({
            severity: 'success',
            summary: '검색 완료',
            detail: `${filteredMateLoadingList.value.length}건의 자재를 조회했습니다.`,
            life: 3000
        });
    } catch (error) {
        console.error('검색 실패:', error);
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
        console.error('초기화 실패:', error);
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
    console.log('데이터 변경됨:', newData);
    
    // 🔥 전체 목록을 교체하지 않고, 변경된 항목만 업데이트
    // 구역 선택 후 전체 목록이 손실되는 문제 방지
    console.log('handleDataChange 호출됨 - 전체 목록 교체 방지');
};

// 🔥 체크박스 선택 변경 처리 (InputTable에서 호출)
const handleSelectionChange = (newSelection) => {
    console.log('선택 변경됨:', newSelection);
    selectedItems.value = newSelection;
    
    // 🔥 store에도 즉시 반영
    mateLoadingStore.setSelectedMateLoadings([...newSelection]);
};

//  구역선택 버튼 클릭 처리 (신규)
const handleLocationSelect = (rowData) => {
    console.log('구역선택 클릭:', rowData);
    
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
        // 🔥 모달에서 사용할 총 수량을 사용자가 입력한 적재 수량으로 설정
        totalQty: rowData.qty,
        // 🔥 분할적재 지원: 이미 할당된 수량이 있으면 전달
        existingAllocated: rowData.totalAllocated || 0,
        existingPlacementPlan: rowData.placementPlan || [],
        // 공통코드 원본값으로 변환 (API 호출용)
        stoCon: getOriginalStoConCode(rowData.stoCon),
        unit: getOriginalUnitCode(rowData.unit)
    };
    
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

// 🔥 창고 구역 선택 확인 처리 (신규)
const handleWarehouseAreaConfirm = (selectionData) => {
    console.log('창고 구역 선택 확인:', selectionData);
    
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
                // 🔥 사용자가 모달에서 입력한 수량도 저장
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
                
                // 🔥 자동으로 체크박스 체크하기 - 수정된 material 객체 사용
                if (material && !selectedItems.value.some(item => item.mateInboCd === material.mateInboCd)) {
                    // 변환된 데이터가 아닌 원본 material 객체를 사용하되, 화면 표시용 정보도 포함
                    const materialForSelection = {
                        ...material,
                        // 화면 표시용 변환된 정보도 포함
                        unit: convertedMateLoadingList.value.find(item => item.mateInboCd === material.mateInboCd)?.unit || material.unit,
                        stoCon: convertedMateLoadingList.value.find(item => item.mateInboCd === material.mateInboCd)?.stoCon || material.stoCon,
                        firstUnit: convertedMateLoadingList.value.find(item => item.mateInboCd === material.mateInboCd)?.firstUnit || material.firstUnit
                    };
                    
                    selectedItems.value.push(materialForSelection);
                    console.log('자동 체크박스 선택 완료:', materialForSelection.mateInboCd);
                    console.log('선택된 자재의 placementPlan:', materialForSelection.placementPlan);
                    console.log('선택된 자재의 wareAreaCd:', materialForSelection.wareAreaCd);
                    
                    // 🔥 store에도 즉시 반영
                    mateLoadingStore.setSelectedMateLoadings([...selectedItems.value]);
                    
                    // 🔥 InputTable의 선택 상태만 업데이트 (전체 데이터는 변경하지 않음)
                    console.log('구역 선택 후 체크박스 상태 업데이트 완료');
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
        console.error('창고 구역 선택 처리 실패:', error);
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
    if (!selectedItems.value || selectedItems.value.length === 0) {
        toast.add({
            severity: 'warn',
            summary: '선택 필요',
            detail: '적재 처리할 자재를 선택해주세요.',
            life: 3000
        });
        return;
    }

    try {
        console.log('적재 처리 시작 - 선택된 자재들:', selectedItems.value);
        
        // 🔥 각 자재의 구역 정보 상세 로깅
        selectedItems.value.forEach((item, index) => {
            console.log(`자재 ${index + 1}: ${item.mateInboCd}`, {
                wareAreaCd: item.wareAreaCd,
                placementPlan: item.placementPlan,
                hasArea: (item.wareAreaCd && item.wareAreaCd.trim() !== '') || (item.placementPlan && item.placementPlan.length > 0)
            });
        });
        
        // 선택된 자재들의 구역 설정 상태 확인
        const itemsWithArea = selectedItems.value.filter(item => 
            (item.wareAreaCd && item.wareAreaCd.trim() !== '') ||
            (item.placementPlan && item.placementPlan.length > 0)
        );
        
        console.log('구역이 설정된 자재들:', itemsWithArea);
        
        if (itemsWithArea.length === 0) {
            toast.add({
                severity: 'warn',
                summary: '구역 선택 필요',
                detail: '선택된 자재 중 창고구역이 설정된 자재가 없습니다. 먼저 구역을 선택해주세요.',
                life: 3000
            });
            return;
        }
        
        // 선택된 자재들을 store에 설정
        mateLoadingStore.setSelectedMateLoadings([...selectedItems.value]);
        
        // 다중 적재 처리 실행
        const result = await mateLoadingStore.processBatchLoading();
        
        // 🔥 결과에 따른 토스트 메시지 (부분/완전 적재 구분)
        if (result.skippedCount > 0 || result.partiallyProcessedCount > 0) {
            toast.add({
                severity: 'warn', 
                summary: '적재 처리 완료',
                detail: result.message,
                life: 5000
            });
        } else {
            toast.add({
                severity: 'success',
                summary: '적재 처리 완료',
                detail: result.message,
                life: 3000
            });
        }
        
        // 처리 완료 후 선택 항목 초기화
        selectedItems.value = [];
        
    } catch (error) {
        console.error('적재 처리 실패:', error);
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
    console.log('행 클릭:', rowData);
    // TODO: 상세 정보 모달 등 구현
};

// 🔥 selectedItems 변경 감지 (체크박스 선택/해제)
watch(selectedItems, (newSelection) => {
    console.log('selectedItems 변경 감지:', newSelection.length, '개 선택됨');
    
    // store에 즉시 반영
    mateLoadingStore.setSelectedMateLoadings([...newSelection]);
    
    // 각 선택된 자재의 구역 정보 로깅
    newSelection.forEach((item, index) => {
        const hasArea = (item.wareAreaCd && item.wareAreaCd.trim() !== '') || 
                       (item.placementPlan && item.placementPlan.length > 0);
        console.log(`선택된 자재 ${index + 1}: ${item.mateInboCd} - 구역설정여부: ${hasArea}`);
    });
}, { deep: true });
</script>

<template>
    <!-- 검색 폼 -->
    <div class="space-y-4 mb-2">
        <SearchForm 
            :columns="searchColumns"
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
            :title="`자재 적재 대기 목록 (총 ${filteredMateLoadingList.length}건)`"
            v-model:selection="selectedItems"
            :dataKey="'mateInboCd'"
            :selectionMode="'multiple'"
            :enableSelection="true"
            :enableRowActions="false"
            :scrollHeight="'500px'"
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

    <!-- 🔥 창고 구역 선택 모달 (신규) -->
    <WarehouseAreaSelectModal
        v-model:visible="warehouseAreaModalVisible"
        :selectedMaterial="currentSelectedMaterial"
        :loadingQuantity="currentSelectedMaterial?.qty || 0"
        @confirm="handleWarehouseAreaConfirm"
    />
</template>

<style scoped>
</style>