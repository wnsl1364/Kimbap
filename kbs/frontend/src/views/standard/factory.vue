<script setup>
import { ref, onBeforeMount, onMounted, computed } from 'vue';
import { format } from 'date-fns';
import { storeToRefs } from 'pinia';
import { useCommonStore } from '@/stores/commonStore';
import { useStandardFacStore } from '@/stores/standardFacStore';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import StandardTable from '@/components/kimbap/table/StandardTable.vue';
import BasicModal from '@/components/kimbap/modal/basicModal.vue';


// Pinia Store 상태 및 함수 바인딩
const store = useStandardFacStore();
const { factoryList, facMaxList, formData, changeHistory, facMaxData } = storeToRefs(store);
const { fetchFactorys, fetchFacMax, saveFactory, fetchFactoryDetail, fetchChangeHistory } = store;

// 오늘 날짜 포맷 (등록일자 default 값에 사용)
const today = format(new Date(), 'yyyy-MM-dd');

// 공통코드 가져오기
const common = useCommonStore();
const { commonCodes } = storeToRefs(common);

// 공통코드 형변환
const convertUnitCodes = (list) => {
    const opStatusCodes = common.getCodes('0R'); // 가동상태
    return list.map((item) => {
        const matchedopStatus = opStatusCodes.find((code) => code.dcd === item.opStatus);
        return {
            ...item,
            opStatus: matchedopStatus ? matchedopStatus.cdInfo : item.opStatus
        };
    });
};

const convertedfactoryList = computed(() => convertUnitCodes(factoryList.value));

// UI 상태 정의
const searchColumns = ref([]); // 검색 컬럼
const inputColumns = ref([]); // 입력 폼 컬럼
const facMaxColumns = ref([]); // 공장별 최대생산량 테이블 컬럼
const factoryColumns = ref([]); // 공장목록 테이블 컬럼
const inputFormButtons = ref({}); // 공장 등록 버튼
const rowButtons = ref({}); // 공장별 최대생산량 테이블용 버튼


// 이력조회 모달 관련
const selectedHistoryItems = ref([]);
const historyModalVisible = ref(false); // 모달 표시 여부
const selectedfcode = ref(''); // 선택된 공장코드
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
    if (!selectedfcode.value) {
        console.warn('fcode가 비어있습니다');
        return [];
    }

    await fetchChangeHistory(selectedfcode.value); // 데이터를 불러옴
    selectedHistoryItems.value = changeHistory.value;
    return changeHistory.value;
};

// 테이블에서 "이력조회" 버튼 클릭 시 실행되는 핸들러
const handleViewHistory = async (rowData) => {
    selectedfcode.value = rowData.fcode;
    await store.fetchChangeHistory(rowData.fcode);

    console.log('[DEBUG] changeHistory:', changeHistory.value);
    historyModalVisible.value = true;
};

const modalDataSets = computed(() => ({
    pcode: {
        items: facMaxList.value,
        columns: [
            { field: 'pcode', header: '제품코드'},
            { field: 'prodName', header: '제품명'},
        ],
        displayField: 'pcode',
        mappingFields: { pcode: 'pcode', prodName: 'prodName'}
    }
}));

// UI 구성 정의
onBeforeMount(() => {
    searchColumns.value = [
        { key: 'fcode', label: '공장코드', type: 'text', placeholder: '공장코드를 입력하세요' },
        { key: 'facName', label: '공장명', type: 'text', placeholder: '공장코드를 입력하세요' },
        {
            key: 'opStatus',
            label: '가동상태',
            type: 'dropdown',
            options: [
                { label: '활성', value: 'r1' },
                { label: '비활성', value: 'r2' }
            ]
        },
        { key: 'regDt', label: '등록일자', type: 'calendar'},
    ];
    inputColumns.value = [
        { key: 'fcode', label: '공장코드', type: 'readonly' },
        { key: 'facName', label: '공장명', type: 'text' },
        { key: 'address', label: '주소', type: 'text' },
        { key: 'tel', label: '연락처(-포함)', type: 'text' },
        { key: 'mname', label: '담당자명', type: 'text' },
        { key: 'opStatus', label: '가동상태', type: 'radio',options: [
                { label: '활성화', value: 'r1' },
                { label: '비활성화', value: 'r2' }
            ] },
        { key: 'chaRea', label: '변경사유', type: 'text', disabled: (row) => !row.fcode },
        { key: 'regDt', label: '등록일자', type: 'readonly', defaultValue: today },
        { key: 'note', label: '비고', type: 'textarea', rows: 1, cols: 20 },
    ];
    factoryColumns.value = [
        { field: 'fcode', header: '공장코드' },
        { field: 'facName', header: '공장명' },
        { field: 'address', header: '주소' },
        { field: 'opStatus', header: '가동상태' },
        { field: 'regDt', header: '등록일자' },
    ];
    facMaxColumns.value = [
        { field: 'pcode', header: '거래처코드', type: 'inputsearch', align: "left" ,placeholder: '제품 선택', suffixIcon: 'pi pi-search' },
        { field: 'prodName', header: '거래처명',  type: 'input' },
        { field: 'mpqty', header: '최대생산량(EA)',  type: 'input',align: "right", inputType: 'number', placeholder: '최대생산량을 입력하세요' },
    ]
    inputFormButtons.value = {
        save: { show: true, label: '저장', severity: 'success' }
    };
})

onMounted(async () => {
    await common.fetchCommonCodes('0R'); // 가동상태
    await fetchFacMax();
    await fetchFactorys();
});

// 공장기준정보 등록 처리
const handleSaveFactory = async () => {
    const result = await saveFactory();
    alert(result === '등록 성공' ? '등록 성공' : result);
};

// 공장 단건 조회 처리
const handleSelectFactory = async (selectedRow) => {
    await fetchFactoryDetail(selectedRow.fcode);
};

const clearForm = () => {
    formData.value = {}; // 또는 필요한 초기화 방식으로
    facMaxData.value = [];
};

const handleReset = async () => {
    await fetchFactorys(); // 전체 목록 다시 조회
};

const handleSearch = async (searchData) => {
    await fetchFactorys(); // 최신 데이터 가져오기

    factoryList.value = factoryList.value.filter((item) => {
        const matchfcode = !searchData.fcode || item.fcode?.toLowerCase().includes(searchData.fcode.toLowerCase());
        const matchfacName = !searchData.facName || item.facName?.includes(searchData.facName);
        const matchopStatus = !searchData.opStatus || item.opStatus?.includes(searchData.opStatus);
        const matchregDt = !searchData.regDt || String(item.regDt) === String(searchData.regDt);

        return matchfcode && matchfacName && matchopStatus && matchregDt;
    });
};
</script>

<template>
    <!-- 검색 영역 -->
    <SearchForm :columns="searchColumns" @search="handleSearch" @reset="handleReset" />

    <!-- 메인 영역 -->
    <div class="flex flex-col md:flex-row gap-4 mt-6">
        <div class="w-full md:basis-[55%]">
            <StandardTable
                title="공장 기준정보 목록"
                :data="convertedfactoryList"
                dataKey="fcode"
                :columns="factoryColumns"
                @view-history="handleViewHistory"
                @row-select="handleSelectFactory"
                @clear-selection="clearForm"
                :scrollable="true"
                scrollHeight="230px"
                height="320px"
                class="mb-2"
            />
            <InputTable title="공장별최대생산량" v-model:data="facMaxData" :columns="facMaxColumns" :buttons="rowButtons" dataKey="pcode" :modalDataSets="modalDataSets" button-position="top" scrollHeight="205px" height="300px" />
        </div>
        <div class="w-full md:basis-[45%]">
            <InputForm title="공장정보" :columns="inputColumns" v-model:data="formData" :buttons="inputFormButtons" @submit="handleSaveFactory" />
        </div>
    </div>
    <BasicModal v-model:visible="historyModalVisible" :items="changeHistory" :columns="changeColumns" :itemKey="'version'" :fetchItems="fetchHistoryItems" />
</template>