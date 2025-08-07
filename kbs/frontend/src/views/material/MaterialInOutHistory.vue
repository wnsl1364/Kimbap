<script setup>
import { ref, onBeforeMount, onMounted, computed } from 'vue';
import { format } from 'date-fns';
import { storeToRefs } from 'pinia';
import { useCommonStore } from '@/stores/commonStore';
import { usemathistoryListStore } from '@/stores/mathistoryStore';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';

// 🟩 Pinia 상태 및 액션
const store = usemathistoryListStore();
const { mathistoryList } = storeToRefs(store);
const { fetchMatHistorys, fetchTodayMatHistorys } = store;

// 공통코드 가져오기
const common = useCommonStore();
const { commonCodes } = storeToRefs(common);

// 공통코드 형변환
const convertUnitCodes = (list) => {
    const CategoryCodes = common.getCodes('0Y'); // 거래처유형
    const UnitCodes = common.getCodes('0G'); // 단위 
    return list.map((item) => {
        const matchedCategory = CategoryCodes.find((code) => code.dcd === item.movementCategory);
        const matchedUnit = UnitCodes.find((code) => code.dcd === item.unit);
        const formattedRegDt = item.regDt ? format(new Date(item.regDt), 'yyyy-MM-dd') : '';

        return {
            ...item,
            movementCategory : matchedCategory ? matchedCategory.cdInfo : item.movementCategory,
            unit : matchedUnit ? matchedUnit.cdInfo : item.unit,
            regDt: formattedRegDt,
        };
    });
};

const convertedMathistoryList = computed(() => convertUnitCodes(mathistoryList.value));

// UI 상태 정의
const searchColumns = ref([]); // 검색 컬럼
const InputTablecolumns = ref([]); // 목록 컬럼
const inputTableButtons = ref({
    excel: { show: true, label: '엑셀 다운로드', severity: 'success' }
});

// UI 구성 정의
onBeforeMount(() => {
    const today = new Date(); // ← 여기에 추가
    searchColumns.value = [
        {
            key: 'movementType',
            label: '구분',
            type: 'radio',
            options: [
                { label: '전체', value: '' },
                { label: '입고', value: '입고' },
                { label: '출고', value: '출고' }
            ]
        },
        {
            key: 'regDt',
            label: '기간',
            type: 'dateRange',
            default: {
                start: today,
                end: today
            }
        },
        {
            key: 'mateName',
            label: '자재명',
            type: 'text',
            placeholder: '자재명을 입력하세요'
        },
        {
            key: 'wareName',
            label: '창고',
            type: 'text',
            placeholder: '창고명을 입력하세요'
        },
        {
            key: 'lotNo',
            label: 'LOT번호',
            type: 'text',
            placeholder: 'LOT번호를 입력하세요'
        }
    ];
    InputTablecolumns.value = [
        { field: 'regDt', header: '일자', type: 'readonly', width: '120px' },
        { field: 'movementType', header: '구분', type: 'readonly', width: '80px' },       // 입고 / 출고
        { field: 'movementCategory', header: '유형', type: 'readonly', width: '80px' },   // 발주 / 공통코드
        { field: 'mcode', header: '자재코드', type: 'readonly' , width: '120px'},
        { field: 'mateName', header: '자재명', type: 'readonly' , width: '170px'},
        { field: 'qty', header: '수량', type: 'readonly' , width: '120px', align: 'right'},                // 입고 = totalQty / 출고 = relQty
        { field: 'unit', header: '단위', type: 'readonly' , width: '80px'},
        { field: 'wareName', header: '창고', type: 'readonly' , width: '140px'},
        { field: 'lotNo', header: 'LOT번호', type: 'readonly', width: '200px' },
        { field: 'note', header: '비고', type: 'readonly' }
    ];
});

onMounted(async () => {
    await common.fetchCommonCodes('0Y'); // 발주 유형
    await common.fetchCommonCodes('0G'); // 발주 유형
    await fetchTodayMatHistorys(); 
});

const handleSearch = async (searchData) => {
  await fetchMatHistorys(searchData);
};

const handleReset = async () => {
     await fetchTodayMatHistorys(); 
};
</script>
<template>
    <div class="space-y-4">
        <SearchForm :columns="searchColumns" @search="handleSearch" @reset="handleReset" :gridColumns="3" />
    </div>
    <div class="space-y-4 mt-8">
        <!-- 🔽 실제 테이블 -->
        <InputTable :columns="InputTablecolumns" :title="'자재 입출고 조회'" :data="convertedMathistoryList" scrollHeight="360px" height="460px" :enableSelection="false" :buttons="inputTableButtons" :enableRowActions="false" :showRowCount="true" :showExcelDownload="true" />
    </div>
</template>
