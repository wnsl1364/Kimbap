<script setup>
import { ref, onBeforeMount, onMounted, computed } from 'vue';
import { storeToRefs } from 'pinia';
import { format } from 'date-fns';
import { useStandardMatStore } from '@/stores/standardMatStore';
import { useCommonStore } from '@/stores/commonStore'
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import StandardTable from '@/components/kimbap/table/StandardTable.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import BasicModal from '@/components/kimbap/modal/basicModal.vue';

// 오늘 날짜 포맷 (등록일자 default 값에 사용)
const today = format(new Date(), 'yyyy-MM-dd');

// 공통코드 가져오기
const common = useCommonStore()
const { commonCodes } = storeToRefs(common)
const convertedMaterialList = computed(() => convertUnitCodes(materialList.value));


// 공통코드 형변환
const convertUnitCodes = (list) => {
  const mateTypeCodes = common.getCodes('0H'); // 자재유형
  const stoConCodes = common.getCodes('0O');   // 보관조건

  return list.map(item => {
    const matchedMateType = mateTypeCodes.find(code => code.dcd === item.mateType);
    const matchedStoCon = stoConCodes.find(code => code.dcd === item.stoCon);

    return {
      ...item,
      mateType: matchedMateType ? matchedMateType.cdInfo : item.mateType,
      stoCon: matchedStoCon ? matchedStoCon.cdInfo : item.stoCon,
    };
  });
};

// Pinia Store 상태 및 함수 바인딩
const store = useStandardMatStore();
const { materialList, supplierList, formData, supplierData, changeHistory } = storeToRefs(store);
const { fetchMaterials, fetchSuppliers, fetchMaterialDetail, saveMaterial } = store;

// UI 상태 정의
const searchColumns = ref([]); // 검색 컬럼
const inputColumns = ref([]); // 입력 폼 컬럼
const cpColumns = ref([]); // 공급처 테이블 컬럼
const mataerialColumns = ref([]); // 자재목록 테이블 컬럼
const inputFormButtons = ref({}); // 자재 등록 버튼
const rowButtons = ref({}); // 공급처 테이블용 버튼

// 이력조회 모달 관련 상태 및 핸들러
const selectedHistoryItems = ref([]);
const historyModalVisible = ref(false); // 모달 표시 여부
const selectedMcode = ref(''); // 선택된 자재코드

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
    if (!selectedMcode.value) {
        console.warn('mcode가 비어있습니다');
        return [];
    }

    // API 호출로 이력 데이터 새로 조회
    await store.fetchChangeHistory(selectedMcode.value);

    // store에서 가져온 changeHistory를 selectedHistoryItems에 복사
    selectedHistoryItems.value = changeHistory.value;

    return changeHistory.value;
};

// 테이블에서 "이력조회" 버튼 클릭 시 실행되는 핸들러
const handleViewHistory = async (rowData) => {
    selectedMcode.value = rowData.mcode;
    await store.fetchChangeHistory(rowData.mcode);

    console.log('[DEBUG] changeHistory:', changeHistory.value);
    historyModalVisible.value = true;
};

// 📦 6. 공급처 선택용 모달 설정
const modalDataSets = computed(() => ({
    cpCd: {
        items: supplierList.value,
        columns: [
            { field: 'cpCd', header: '거래처코드' },
            { field: 'cpName', header: '거래처명' },
            { field: 'tel', header: '전화번호' }
        ],
        displayField: 'cpCd',
        mappingFields: { cpCd: 'cpCd', cpName: 'cpName' }
    }
}));

// UI 구성 정의
onBeforeMount(() => {
    searchColumns.value = [
        { key: 'mcode', label: '자재코드', type: 'text', placeholder: '자재코드를 입력하세요' },
        { key: 'mateName', label: '자재명', type: 'text', placeholder: '자재명을 입력하세요' },
        {
            key: 'mateType',
            label: '자재유형',
            type: 'dropdown',
            options: [
                { label: '원자재', value: 'h1' },
                { label: '부자재', value: 'h2' }
            ]
        },
        {
            key: 'stoCon',
            label: '보관조건',
            type: 'dropdown',
            options: [
                { label: '상온', value: 'o1' },
                { label: '냉장', value: 'o2' },
                { label: '냉동', value: 'o3' }
            ]
        }
    ];

    inputColumns.value = [
        { key: 'mcode', label: '자재코드', type: 'readonly' },
        { key: 'mateName', label: '자재명', type: 'text' },
        {
            key: 'mateType',
            label: '자재유형',
            type: 'dropdown',
            options: [
                { label: '원자재', value: 'h1' },
                { label: '부자재', value: 'h2' }
            ]
        },
        {
            key: 'stoCon',
            label: '보관조건',
            type: 'dropdown',
            options: [
                { label: '상온', value: 'o1' },
                { label: '냉장', value: 'o2' },
                { label: '냉동', value: 'o3' }
            ]
        },
        {
            key: 'unit',
            label: '단위',
            type: 'dropdown',
            options: [
                { label: 'kg', value: 'g2' },
                { label: 'box', value: 'g6' }
            ]
        },
        { key: 'std', label: '규격', type: 'text' },
        {
            key: 'pieceUnit',
            label: '낱개단위',
            type: 'dropdown',
            options: [
                { label: '매', value: '매' },
                { label: '장', value: '장' },
                { label: 'EA', value: 'ea' }
            ],
            disabled: (row) => row.unit !== 'g6'
        },
        {
            key: 'converQty',
            label: '환산수량',
            type: 'number',
            disabled: (row) => row.unit !== 'g6'
        },
        { key: 'moqty', label: '최소발주단위', type: 'number' },
        { key: 'edate', label: '소비기한(일)', type: 'text' },
        { key: 'safeStock', label: '안전재고', type: 'number' },
        { key: 'corigin', label: '원산지', type: 'text' },
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
            disabled: (row) => !row.mcode
        },
        { key: 'note', label: '비고', type: 'textarea', rows: 1, cols: 20 },
        { key: 'regDt', label: '등록일자', type: 'readonly', defaultValue: today }
    ];

    cpColumns.value = [
        { field: 'cpCd', header: '거래처코드', type: 'inputsearch', width: '250px',align: "left" ,placeholder: '거래처 선택', suffixIcon: 'pi pi-search' },
        { field: 'cpName', header: '거래처명', width: '100px', type: 'input' },
        { field: 'unitPrice', header: '단가(원)', width: '100px', type: 'input',align: "right", inputType: 'number', placeholder: '단가를 입력하세요' },
        { field: 'ltime', header: '리드타임(일)', width: '100px', type: 'input', align: "right",inputType: 'number', placeholder: '리드타임을 입력하세요' }
    ];

    mataerialColumns.value = [
        { field: 'mcode', header: '자재코드' },
        { field: 'mateName', header: '자재명' },
        { field: 'mateType', header: '유형' },
        { field: 'stoCon', header: '보관조건' },
        { field: 'edate', header: '소비기한(일)' }
    ];

    inputFormButtons.value = {
        save: { show: true, label: '저장', severity: 'success' }
    };
});

// ⚙️ 8. 데이터 fetch (초기 자재/공급처 목록)
onMounted(async() => {
    await common.fetchCommonCodes('0H')  // 자재유형
    await common.fetchCommonCodes('0O')  // 보관조건
    await fetchSuppliers();
    await fetchMaterials();
});

// 💾 9. 자재 등록 처리
const handleSaveMaterial = async () => {
    const result = await saveMaterial();
    alert(result === '등록 성공' ? '등록 성공' : result);
};

// 📄 10. 자재 단건 조회 처리
const handleSelectMaterial = async (selectedRow) => {
    await fetchMaterialDetail(selectedRow.mcode);
};
const clearForm = () => {
    formData.value = {}; // 또는 필요한 초기화 방식으로
    supplierData.value = [];
};
// 검색
const handleSearch = async (searchData) => {
  await fetchMaterials(); // 최신 데이터 가져오기

  // 조건 키: mcode, mateName, mateType, stoCon
  materialList.value = materialList.value.filter((item) => {
    const matchMcode     = !searchData.mcode     || item.mcode?.toLowerCase().includes(searchData.mcode);
    const matchMateName  = !searchData.mateName  || item.mateName?.includes(searchData.mateName);
    const matchMateType  = !searchData.mateType  || item.mateType === searchData.mateType;
    const matchStoCon    = !searchData.stoCon    || item.stoCon === searchData.stoCon;

    return matchMcode && matchMateName && matchMateType && matchStoCon;
  });
};

const handleReset = async () => {
  await fetchMaterials(); // 전체 목록 다시 조회
};
</script>
<template>
    <SearchForm :columns="searchColumns" @search="handleSearch" @reset="handleReset" />

    <div class="flex flex-col md:flex-row gap-4 mt-6">
        <div class="w-full md:basis-[55%]">
            <StandardTable
                title="자재 기준정보 목록"
                :data="convertedMaterialList"
                dataKey="mcode"
                :columns="mataerialColumns"
                @view-history="handleViewHistory"
                @row-select="handleSelectMaterial"
                @clear-selection="clearForm"
                :scrollable="true"
                scrollHeight="230px"
                height="320px"
                :showRowCount="true"
                class="mb-2"
            />
            <InputTable title="자재별 공급처" v-model:data="supplierData" :columns="cpColumns" :buttons="rowButtons" dataKey="cpCd" :modalDataSets="modalDataSets" button-position="top" scrollHeight="205px" height="300px" />
        </div>

        <div class="w-full md:basis-[45%]">
            <InputForm title="자재정보" :columns="inputColumns" v-model:data="formData" :buttons="inputFormButtons" @submit="handleSaveMaterial" />
        </div>
        <BasicModal v-model:visible="historyModalVisible" :items="changeHistory" :columns="changeColumns" :itemKey="'version'" :fetchItems="fetchHistoryItems" />
    </div>
</template>
