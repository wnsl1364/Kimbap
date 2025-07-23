<script setup>
import { ref, onMounted } from 'vue';
import { CustomerService } from '@/service/CustomerService';
import { onBeforeMount } from 'vue';
import { defineProps } from 'vue';
import { getMaterialList } from '@/api/standard';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import StandardTable from '@/components/kimbap/table/StandardTable.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';

const handleViewHistory = (rowData) => {
    console.log('이력조회 클릭됨:', rowData);
    // 모달 열기 + 이력 데이터 세팅 등 처리
};

const purchaseColumns = ref([
  {
    field: 'cpCd',
    header: '거래처',
    type: 'input',
    placeholder: '거래처를 입력하세요'
  },
  {
    field: 'unitPrice',
    header: '단가(원)',
    type: 'input',
    inputType: 'number',
    placeholder: '단가를 입력하세요'
  },
  {
    field: 'ltime',
    header: '리드타임(일)',
    type: 'input',
    inputType: 'number',
    placeholder: '리드타임을 입력하세요'
  },
  {
    field: 'ltime',
    header: '리드타임(일)',
    type: 'input',
    inputType: 'number',
    placeholder: '리드타임을 입력하세요'
  },
]);
// 자재기준정보 테이블용
const productColumns = [
    { field: 'mcode', header: '자재코드' },
    { field: 'mateName', header: '자재명' },
    { field: 'mateType', header: '유형' },
    { field: 'stoCon', header: '보관조건' },
    { field: 'edate', header: '소비기한' }
];
// 자재목록
const products = ref([]);

onMounted(async () => {
    try {
        const res = await getMaterialList();
        console.log('✅ 응답 타입:', typeof res.data);
        console.log('📦 실제 응답 내용:', res.data);
        products.value = res.data;
    } catch (err) {
        console.error('❌ 자재 목록 조회 실패:', err);
    }
});

//
const customers2 = ref(null);
// SearchForm props 정의

const props = defineProps({
    searchColumns: {
        type: Array,
        default: () => [
            {
                key: 'mateName',
                label: '자재명',
                type: 'text',
                placeholder: '이름을 입력하세요'
            },
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
        ]
    },
    inputColumns: {
        type: Array,
        default: () => [
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
        ]
    }
});

function formatCurrency(value) {
    return value.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}
onBeforeMount(() => {
    CustomerService.getCustomersLarge().then((data) => (customers2.value = data));
});

// 검색 이벤트 핸들러
const handleSearch = (searchData) => {
    console.log('테이블 컴포넌트에서 받은 검색 데이터:', searchData);
    // 여기에 검색 로직 구현
};

// 리셋 이벤트 핸들러
const handleReset = () => {
    console.log('검색 조건이 리셋되었습니다');
    // 여기에 리셋 로직 구현
};

const purchaseFormButtons = ref({
  save: { show: true, label: '저장', severity: 'success' },
});

// 
const rowButtons = ref({})

</script>

<template>
    <div>
        <SearchForm :columns="searchColumns" @search="handleSearch" @reset="handleReset" />
    </div>

    <div class="flex flex-col md:flex-row gap-4 mt-6">
        <div class="w-full md:basis-[55%]">
            <StandardTable :title="'자재 기준정보 목록'" :data="products" dataKey="mcode" :columns="productColumns" title="자재기준정보 목록" @view-history="handleViewHistory" :scrollable="true" scrollHeight="300px" height="330px" class="mb-2"/>
            <InputTable :title="'자재 발주 목록'" :columns="purchaseColumns" :buttons="rowButtons" button-position="top" scrollHeight="205px" height="290px" />
        </div>
        <div class="w-full md:basis-[45%]">
            <InputForm :title="'자재정보'" :columns="inputColumns" :buttons="purchaseFormButtons" />
        </div>
    </div>
</template>
