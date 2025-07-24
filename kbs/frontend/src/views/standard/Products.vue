<script setup>
import { ref, onBeforeMount, onMounted, computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useStandardMatStore } from '@/stores/standardMatStore';

import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import StandardTable from '@/components/kimbap/table/StandardTable.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';

// 1) Pinia 스토어: 전역 데이터와 액션만
const store = useStandardMatStore();
const { materialList, supplierList } = storeToRefs(store);
const { fetchMaterials, fetchSuppliers, addMaterial, setSearchFilter, selectMaterial } = store;

// 2) 컴포넌트 레벨 UI 설정
const searchColumns = ref([]);
const inputColumns = ref([]);
const cpColumns = ref([]);
const productColumns = ref([]);
const inputFormButtons = ref({});
const rowButtons = ref({});

// 3) 모달 데이터 설정 (공급처)
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

// 4) 라이프사이클에서 UI 설정 및 데이터 로드
onBeforeMount(() => {
    // ─ SearchForm 컬럼
    searchColumns.value = [
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

    // ─ InputForm 컬럼
    inputColumns.value = [
        { key: 'mcode', label: '자재코드', type: 'disabled', defaultValue: '자동생성' },
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
            key: 'deliveryUnit',
            label: '낱개단위',
            type: 'dropdown',
            options: [
                { label: '매', value: '매' },
                { label: '장', value: '장' },
                { label: 'EA', value: 'ea' }
            ]
        },
        { key: 'converQty', label: '환산수량', type: 'number' },
        { key: 'moqty', label: '최소발주단위', type: 'text' },
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
        { key: 'chaRea', label: '변경사유', type: 'text' },
        { key: 'note', label: '비고', type: 'textarea', rows: 1, cols: 20, placeholder: '특이사항을 입력하세요' },
        { key: 'regDt', label: '등록일자', type: 'disabled', defaultValue: new Date().toISOString().slice(0, 10) }
    ];

    // ─ 자재별 공급처 테이블 컬럼
    cpColumns.value = [
        { field: 'cpCd', header: '거래처코드', type: 'inputsearch', width: '250px', placeholder: '거래처 선택', suffixIcon: 'pi pi-search' },
        { field: 'cpName', header: '거래처명', width: '100px', type: 'input' },
        { field: 'unitPrice', header: '단가(원)', width: '100px', type: 'input', inputType: 'number', placeholder: '단가를 입력하세요' },
        { field: 'ltime', header: '리드타임(일)', width: '100px', type: 'input', inputType: 'number', placeholder: '리드타임을 입력하세요' }
    ];

    // ─ 자재 목록 테이블 컬럼
    productColumns.value = [
        { field: 'mcode', header: '자재코드' },
        { field: 'mateName', header: '자재명' },
        { field: 'mateType', header: '유형' },
        { field: 'stoCon', header: '보관조건' },
        { field: 'edate', header: '소비기한' }
    ];

    // ─ 입력폼 버튼
    inputFormButtons.value = {
        save: { show: true, label: '저장', severity: 'success' }
    };
});

onMounted(() => {
    fetchSuppliers();
    fetchMaterials();
});

// 5) 이벤트 핸들러
const handleViewHistory = (rowData) => selectMaterial(rowData);
const handleSearch = (filter) => setSearchFilter(filter);
const handleReset = () => setSearchFilter({});
const handleSaveMaterial = async (formData) => {
    const res = await addMaterial(formData);
    alert(res.success ? '등록 성공' : '등록 실패: ' + res.message);
};
</script>

<template>
    <SearchForm :columns="searchColumns" @search="handleSearch" @reset="handleReset" />

    <div class="flex flex-col md:flex-row gap-4 mt-6">
        <div class="w-full md:basis-[55%]">
            <StandardTable title="자재기준정보 목록" :data="materialList" dataKey="mcode" :columns="productColumns" @view-history="handleViewHistory" :scrollable="true" scrollHeight="230px" height="320px" class="mb-2" />
            <InputTable title="자재별 공급처" :columns="cpColumns" :buttons="rowButtons" dataKey="cpCd" :modalDataSets="modalDataSets" button-position="top" scrollHeight="205px" height="300px" />
        </div>

        <div class="w-full md:basis-[45%]">
            <InputForm title="자재정보" :columns="inputColumns" :buttons="inputFormButtons" @submit="handleSaveMaterial" />
        </div>
    </div>
</template>
