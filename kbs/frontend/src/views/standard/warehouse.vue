<script setup>
import { ref, onBeforeMount, onMounted, computed, watch } from 'vue';
import { format } from 'date-fns';
import { useCommonStore } from '@/stores/commonStore';
import { useStandardWhStore} from '@/stores/standardWhStore';
import { storeToRefs } from 'pinia';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import StandardTable from '@/components/kimbap/table/StandardTable.vue';
import BasicModal from '@/components/kimbap/modal/basicModal.vue';

// Pinia Store 상태 및 함수 바인딩
const store = useStandardWhStore();
const { warehouseList, factoryList, formData, changeHistory } = storeToRefs(store);
const { fetchWarehouses, saveWarehouse, fetchWarehouseDetail, fetchChangeHistory, fetchFactoryList } = store;

// 오늘 날짜 포맷 (등록일자 default 값에 사용)
const today = format(new Date(), 'yyyy-MM-dd');

// 공통코드 가져오기
const common = useCommonStore();
const { commonCodes } = storeToRefs(common);

// 공통코드 형변환
const convertUnitCodes = (list) => {
    const wareTypeCodes = common.getCodes('0Q'); // 창고 유형
    return list.map((item) => {
        const matchedwareType = wareTypeCodes.find((code) => code.dcd === item.wareType);
        return {
            ...item,
            wareType: matchedwareType ? matchedwareType.cdInfo : item.wareType
        };
    });
};
const convertedwarehouseList = computed(() => convertUnitCodes(warehouseList.value));

// UI 상태 정의
const searchColumns = ref([]); // 검색 컬럼
const inputColumns = ref([]); // 입력 폼 컬럼
const warehouseColumns  = ref([]); // 거래처목록 테이블 컬럼
const inputFormButtons = ref({}); // 거래처 등록 버튼

// 이력조회 모달 관련
const selectedHistoryItems = ref([]);
const historyModalVisible = ref(false); // 모달 표시 여부
const selectedCpcode = ref(''); // 선택된 거래처코드
const changeColumns = [
    { field: 'version', header: '버전' },
    { field: 'fieldName', header: '변경항목' },
    { field: 'oldValue', header: '변경 전 값' },
    { field: 'newValue', header: '변경 후 값' },
    { field: 'changeReason', header: '변경사유' },
    { field: 'regDt', header: '등록일자' }
];

// 함수 내용만 교체
const fetchHistoryItems = async () => {
    if (!selectedCpcode.value) {
        console.warn('cpCode가 비어있습니다');
        return [];
    }

    await fetchChangeHistory(selectedCpcode.value); // 데이터를 불러옴
    selectedHistoryItems.value = changeHistory.value;
    return changeHistory.value;
};

// 테이블에서 "이력조회" 버튼 클릭 시 실행되는 핸들러
const handleViewHistory = async (rowData) => {
    selectedCpcode.value = rowData.wcode;
    await store.fetchChangeHistory(rowData.wcode);

    console.log('[DEBUG] changeHistory:', changeHistory.value);
    historyModalVisible.value = true;
};

// UI 구성 정의
onBeforeMount(() => {
    searchColumns.value = [
        { key: 'wcode', label: '창고코드', type: 'text', placeholder: '창고코드를 입력하세요' },
        { key: 'wareName', label: '창고명', type: 'text', placeholder: '창고명 입력하세요' },
        { key: 'wareType', label: '창고유형', type: 'dropdown', options: [
                { label: '상온 창고', value: 'q1' },
                { label: '냉장 창고', value: 'q2' },
                { label: '냉동 창고', value: 'q3' },
            ] 
        }
    ];
    warehouseColumns .value = [
        { field: 'wcode', header: '창고코드' },
        { field: 'wareName', header: '창고명' },
        { field: 'wareType', header: '유형' },
        { field: 'address', header: '주소' },
    ]
    inputFormButtons.value = {
        save: { show: true, label: '저장', severity: 'success' }
    };
})

onMounted(async () => {
    await common.fetchCommonCodes('0Q'); // 창고 유형
    await fetchFactoryList();
    await fetchWarehouses();
    inputColumns.value = [
        { key: 'wcode', label: '창고코드', type: 'readonly'},
        { key: 'wareName', label: '창고명', type: 'text'},
        { key: 'wareType', label: '창고유형', type: 'dropdown', options: [
                { label: '상온 창고', value: 'q1' },
                { label: '냉장 창고', value: 'q2' },
                { label: '냉동 창고', value: 'q3' },
            ] 
        },
        { key: 'address', label: '주소', type: 'text' },
        { key: 'maxRow', label: '최대 행', type: 'number',disabled: (row) => !!row.wcode },
        { key: 'maxCol', label: '최대 열', type: 'number',disabled: (row) => !!row.wcode },
        { key: 'maxFloor', label: '최대 층', type: 'number' ,disabled: (row) => !!row.wcode},
        { key: 'fcode', label: '공장명', type: 'dropdown',
            options: factoryOptions.value
         },
        {
            key: 'isUsed',
            label: '사용여부',
            type: 'radio',
            options: [
                { label: '활성화', value: 'f1' },
                { label: '비활성화', value: 'f2' }
            ]
        },
        {
            key: 'chaRea',
            label: '변경사유',
            type: 'text',
            disabled: (row) => !row.wcode
        },
        { key: 'regDt', label: '등록일자', type: 'readonly', defaultValue: today },
        { key: 'note', label: '비고', type: 'textarea', rows: 1, cols: 20 }
    ]
     console.log('[DEBUG] warehouseList:', warehouseList.value);
});

// 창고기준정보 등록 처리
const handleSaveWarehouse = async () => {
    const result = await saveWarehouse();
    alert(result === '등록 성공' ? '등록 성공' : result);
};

// 창고 단건 조회 처리
const handleSelectWarehouse = async (selectedRow) => {
    await fetchWarehouseDetail(selectedRow.wcode);
};


// 초기화 
const clearForm = () => {
    formData.value = {}; // 또는 필요한 초기화 방식으로
};

const handleReset = async () => {
    await fetchWarehouses(); // 전체 목록 다시 조회
};

// 검색 
const handleSearch = async (searchData) => {
    await fetchWarehouses(); // 최신 데이터

    warehouseList.value = warehouseList.value.filter((item) => {
        const matchWcode = !searchData.wcode || item.wcode?.toLowerCase().includes(searchData.wcode.toLowerCase());
        const matchName = !searchData.wareName || item.wareName?.includes(searchData.wareName);
        const matchType = !searchData.wareType || item.wareType === searchData.wareType;

        return matchWcode && matchName && matchType;
    });
};

// 공장 옵션 (label: 공장명, value: 공장코드)
const factoryOptions = computed(() =>
  factoryList.value.map(f => ({
    label: f.facName,
    value: f.fcode
  }))
);

// fcode 변경 시 facVerCd 자동 세팅
watch(() => formData.value.fcode, (newFcode) => {
  const selected = factoryList.value.find(f => f.fcode === newFcode);
  if (selected) {
    formData.value.facVerCd = selected.facVerCd;
  }
});

</script>
<template>
    <!-- 검색 영역 -->
    <SearchForm :columns="searchColumns" @search="handleSearch" @reset="handleReset" :gridColumns="3"/>

    <!-- 메인 영역 -->
    <div class="flex flex-col md:flex-row gap-4 mt-6">
        <div class="w-full md:basis-[55%]">
            <StandardTable
                title="창고 기준정보 목록"
                :data="convertedwarehouseList"
                dataKey="wcode"
                :columns="warehouseColumns "
                @view-history="handleViewHistory"
                @row-select="handleSelectWarehouse"
                @clear-selection="clearForm"
                :scrollable="true"
                scrollHeight="530px"
                height="630px"
            />
        </div>
        <div class="w-full md:basis-[45%]">
            <InputForm title="창고정보" :columns="inputColumns" v-model:data="formData" :buttons="inputFormButtons" @submit="handleSaveWarehouse" />
        </div>
    </div>
    <BasicModal v-model:visible="historyModalVisible" :items="changeHistory" :columns="changeColumns" :itemKey="'version'" :fetchItems="fetchHistoryItems" />
</template>