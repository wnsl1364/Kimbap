<script setup>
import BasicModal from '@/components/kimbap/modal/basicModal.vue';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import StandardTable from '@/components/kimbap/table/StandardTable.vue';
import { useCommonStore } from '@/stores/commonStore';
import { useMemberStore } from '@/stores/memberStore';
import { useStandardFacStore } from '@/stores/standardFacStore';
import { format } from 'date-fns';
import { storeToRefs } from 'pinia';
import { useToast } from 'primevue/usetoast';
import { computed, onBeforeMount, onMounted, ref, onUnmounted } from 'vue';

// Pinia Store 상태 및 함수 바인딩
const store = useStandardFacStore();
const { factoryList, facMaxList, formData, changeHistory, facMaxData } = storeToRefs(store);
const { resetForm, fetchFactorys, fetchFacMax, saveFactory, fetchFactoryDetail, fetchChangeHistory } = store;
const memberStore = useMemberStore();
const { user } = storeToRefs(memberStore);
const toast = useToast();

const isEmployee = computed(() => user.value?.memType === 'p1');
const isManager = computed(() => user.value?.memType === 'p4');
const isAdmin = computed(() => user.value?.memType === 'p5');

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
const selectedFactory = ref({});
const exportColumns = ref([]);

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
    selectedFactory.value = { facName: rowData.facName, fcode: rowData.fcode }; // ✅ 안전하게 저장
    await store.fetchChangeHistory(rowData.fcode);
    historyModalVisible.value = true;
};

const modalDataSets = computed(() => ({
    pcode: {
        items: facMaxList.value,
        columns: [
            { field: 'pcode', header: '제품코드' },
            { field: 'prodName', header: '제품명' },
            { field: 'prodVerCd', header: '제품버전' }
        ],
        displayField: 'pcode',
        mappingFields: { pcode: 'pcode', prodName: 'prodName', prodVerCd: 'prodVerCd' }
    }
}));

// UI 구성 정의
onBeforeMount(() => {
    searchColumns.value = [
        { key: 'fcode', label: '공장코드', type: 'text', placeholder: '공장코드를 입력하세요' },
        { key: 'facName', label: '공장명', type: 'text', placeholder: '공장명 입력하세요' },
        { key: 'regDt', label: '등록일자', type: 'dateRange' }
    ];
    inputColumns.value = [
        { key: 'fcode', label: '공장코드', type: 'readonly' },
        { key: 'facName', label: '공장명', type: 'text' },
        { key: 'address', label: '주소', type: 'text' },
        { key: 'tel', label: '연락처(-포함)', type: 'text' },
        { key: 'mname', label: '담당자명', type: 'text' },
        {
            key: 'opStatus',
            label: '가동상태',
            type: 'radio',
            options: [
                { label: '활성화', value: 'r1' },
                { label: '비활성화', value: 'r2' }
            ]
        },
        { key: 'chaRea', label: '변경사유', type: 'text', disabled: (row) => !row.fcode },
        { key: 'regDt', label: '등록일자', type: 'readonly', defaultValue: today },
        { key: 'note', label: '비고', type: 'textarea', rows: 1, cols: 20 }
    ];
    factoryColumns.value = [
        { field: 'fcode', header: '공장코드' },
        { field: 'facName', header: '공장명' },
        { field: 'address', header: '주소' },
        { field: 'regDt', header: '등록일자' }
    ];
    facMaxColumns.value = [
        { field: 'pcode', header: '제품코드', type: 'inputsearch', width: '200px', align: 'left', placeholder: '제품 선택', suffixIcon: 'pi pi-search' },
        { field: 'prodName', header: '제품명', type: 'input', width: '200px' },
        { field: 'prodVerCd', header: '제품버전', type: 'input', width: '50px' },
        { field: 'mpqty', header: '최대생산량(EA)', type: 'input', width: '150px', align: 'right', inputType: 'number', min: 0 }
    ];
    inputFormButtons.value = {
        save: { show: isAdmin.value || isManager.value, label: '저장', severity: 'success' }
    };
    // 엑셀 다운로드용 컬럼
    exportColumns.value = [
        { field: 'fcode', header: '공장코드' },
        { field: 'facName', header: '공장명' },
        { field: 'address', header: '주소' },
        { field: 'mname', header: '담당자명' },
        { field: 'pcode', header: '제품코드' },
        { field: 'prodName', header: '제품명' },
        { field: 'mpqty', header: '최대생산량' }
    ];
});

const visibleFacMaxColumns = computed(() => facMaxColumns.value.filter((col) => col.field !== 'prodVerCd'));

onMounted(async () => {
    await common.fetchCommonCodes('0R'); // 가동상태
    await fetchFacMax();
    await fetchFactorys();
});

// 공장기준정보 등록 처리
const handleSaveFactory = async () => {
    if (!isAdmin.value && !isManager.value) {
        toast.add({
            severity: 'error',
            summary: '등록 실패',
            detail: '등록 권한이 없습니다.',
            life: 3000
        });
        return;
    }
    if (!user.value?.empCd) {
        toast.add({
            severity: 'warn',
            summary: '경고',
            detail: '로그인 정보가 없습니다.',
            life: 3000
        });
        return;
    }
    // 신규 등록이면 regi, 수정이면 modi 설정
    if (!formData.value.pcode) {
        formData.value.regi = user.value.empCd;
    } else {
        formData.value.modi = user.value.empCd;
    }
    const result = await saveFactory();

    if (result === '등록 성공' || result === '수정 성공') {
        toast.add({
            severity: 'success',
            summary: result,
            detail: `공장이 정상적으로 ${result.replace('성공', '')}되었습니다.`,
            life: 3000
        });
    } else {
        toast.add({
            severity: 'error',
            summary: result.includes('예외') ? '예외 발생' : '저장 실패',
            detail: result,
            life: 3000
        });
    }
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
    toast.add({
        severity: 'info',
        summary: '초기화 완료 ✨',
        detail: '검색 조건이 초기화되고 전체 목록을 조회했습니다.',
        life: 3000
    });
};

const handleSearch = async (searchData) => {
    await fetchFactorys();

    factoryList.value = factoryList.value.filter((item) => {
        const matchfcode = !searchData.fcode || item.fcode?.toLowerCase().includes(searchData.fcode.toLowerCase());
        const matchfacName = !searchData.facName || item.facName?.includes(searchData.facName);

        // 🔍 날짜 범위 비교
        let matchregDt = true;
        const startDate = searchData.regDt?.start;
        const endDate = searchData.regDt?.end;

        if (startDate && endDate && item.regDt) {
            const reg = new Date(item.regDt);
            matchregDt = reg >= new Date(startDate) && reg <= new Date(endDate);
        }

        return matchfcode && matchfacName && matchregDt;
    });

    if (factoryList.value.length === 0) {
        toast.add({
            severity: 'info',
            summary: '검색 결과 없음',
            detail: '조건에 해당하는 공장이 없습니다.',
            life: 3000
        });
    } else {
        toast.add({
            severity: 'success',
            summary: '검색 성공',
            detail: `총 ${factoryList.value.length}건의 공장이 검색되었습니다.`,
            life: 3000
        });
    }
};

const mergedExportData = computed(() => {
    // 공장 정보가 있는 경우에만
    return factoryList.value.flatMap((factory) => {
        const facMaxForFactory = facMaxData.value.filter((fm) => fm.fcode === factory.fcode);

        // 공장별 최대생산량이 있으면 각각 매핑, 없으면 공장 단독 1건 반환
        return facMaxForFactory.length > 0
            ? facMaxForFactory.map((fm) => ({
                  ...factory,
                  ...fm // mpqty, pcode, prodName, prodVerCd 등
              }))
            : [
                  {
                      ...factory,
                      pcode: '',
                      prodName: '',
                      prodVerCd: '',
                      mpqty: ''
                  }
              ];
    });
});
onUnmounted(() => {
    resetForm();
});
</script>

<template>
    <SearchForm :columns="searchColumns" @search="handleSearch" @reset="handleReset" :gridColumns="3" />

    <div class="flex flex-col md:flex-row gap-4 mt-6">
        <div class="w-full md:basis-[55%]">
            <StandardTable
                title="공장 목록"
                :data="convertedfactoryList"
                dataKey="fcode"
                :columns="factoryColumns"
                @view-history="handleViewHistory"
                @row-select="handleSelectFactory"
                @clear-selection="clearForm"
                :scrollable="true"
                scrollHeight="230px"
                height="320px"
                :showRowCount="true"
                :showExcelDownload="true"
                :exportData="mergedExportData"
                :exportColumns="exportColumns"
                class="mb-2"
            />
            <InputTable title="공장별최대생산량" v-model:data="facMaxData" :columns="visibleFacMaxColumns" :buttons="rowButtons" dataKey="pcode" :modalDataSets="modalDataSets" button-position="top" scrollHeight="205px" height="300px" />
        </div>
        <div class="w-full md:basis-[45%]">
            <InputForm title="공장정보" :columns="inputColumns" v-model:data="formData" :buttons="inputFormButtons" @submit="handleSaveFactory" />
        </div>
    </div>
    
    <BasicModal
        v-model:visible="historyModalVisible"
        :items="changeHistory"
        :columns="changeColumns"
        :itemKey="'version'"
        :fetchItems="fetchHistoryItems"
        :selectedItem="selectedFactory"
        :titleName="selectedFactory.facName"
        :titleCode="selectedFactory.fcode"
    />
</template>
