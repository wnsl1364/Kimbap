<script setup>
import { ref, onMounted, computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useMateLoadingStore } from '@/stores/mateLoadingStore';
import { useCommonStore } from '@/stores/commonStore';
import { useToast } from 'primevue/usetoast';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';

// Store 및 Toast
const mateLoadingStore = useMateLoadingStore();
const commonStore = useCommonStore();
const toast = useToast();

// Store에서 상태 가져오기
const {
    mateLoadingList,
    selectedMateLoadings,
    factoryList,
    isLoading,
    searchColumns,
    tableColumns,
    tableButtons,
    hasSelectedItems,
    selectedCount,
    filteredMateLoadingList  // 필터링된 목록 추가
} = storeToRefs(mateLoadingStore);

// 선택된 자재들
const selectedItems = ref([]);

// 공통코드 형변환
const convertUnitCodes = (list) => {
    const unitCodes = commonStore.getCodes('0G');     // 단위코드
    const stoConCodes = commonStore.getCodes('0O');   // 보관조건코드
    
    return list.map(item => {
        const matchedUnit = unitCodes.find(code => code.dcd === item.unit);
        const matchedStoCon = stoConCodes.find(code => code.dcd === item.stoCon);
        
        return {
            ...item,
            unit: matchedUnit ? matchedUnit.cdInfo : item.unit,
            stoCon: matchedStoCon ? matchedStoCon.cdInfo : item.stoCon,
        };
    });
};

// 변환된 데이터 computed (필터링된 목록 사용)
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
        // 선택된 자재들을 store에 설정
        mateLoadingStore.setSelectedMateLoadings([...selectedItems.value]);
        
        // 다중 적재 처리 실행
        const result = await mateLoadingStore.processBatchLoading();
        
        // 선택 초기화
        selectedItems.value = [];
        
        toast.add({
            severity: 'success',
            summary: '적재 처리 완료',
            detail: result.message || `${selectedCount.value}건의 자재 적재가 완료되었습니다.`,
            life: 5000
        });
        
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

// 위치선택 버튼 클릭 (나중에 구현)
const handleLocationSelect = (rowData) => {
    console.log('위치선택 클릭:', rowData);
    toast.add({
        severity: 'info',
        summary: '위치선택',
        detail: '위치선택 기능은 추후 구현 예정입니다.',
        life: 3000
    });
};

// 행 클릭 처리 (나중에 구현)
const handleRowClick = (rowData) => {
    console.log('행 클릭:', rowData);
    // TODO: 상세 정보 모달 등 구현
};
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
</template>

<style scoped>
</style>