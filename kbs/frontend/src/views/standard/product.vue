<script setup>
import { ref, onBeforeMount, onMounted, computed, watch } from 'vue';
import { format } from 'date-fns';
import { useCommonStore } from '@/stores/commonStore';
import { useStandardProdStore } from '@/stores/standardProdStore';
import { storeToRefs } from 'pinia';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import StandardTable from '@/components/kimbap/table/StandardTable.vue';
import BasicModal from '@/components/kimbap/modal/basicModal.vue';

// Pinia Store 상태 및 함수 바인딩
const store = useStandardProdStore();
const { productList, formData, changeHistory } = storeToRefs(store);
const { fetchProducts, saveProduct, fetchProductDetail, fetchChangeHistory } = store;

// 오늘 날짜 포맷 (등록일자 default 값에 사용)
const today = format(new Date(), 'yyyy-MM-dd');

// 공통코드 가져오기
const common = useCommonStore();
const { commonCodes } = storeToRefs(common);

// 공통코드 형변환
const convertUnitCodes = (list) => {
    const weiCodes = common.getCodes('0N'); // 중량
    const unitCodes = common.getCodes('0G'); // 단위

    return list.map((item) => {
        const matchedWei = weiCodes.find((code) => code.dcd === item.wei);
        const matchedunit = unitCodes.find((code) => code.dcd === item.unit);

        return {
            ...item,
            wei: matchedWei ? matchedWei.cdInfo : item.wei,
            unit: matchedunit ? matchedunit.cdInfo : item.unit
        };
    });
};

//
const convertedproductList = computed(() => convertUnitCodes(productList.value));

// UI 상태 정의
const searchColumns = ref([]); // 검색 컬럼
const inputColumns = ref([]); // 입력 폼 컬럼
const productColumns = ref([]); // 제품목록 테이블 컬럼
const inputFormButtons = ref({}); // 제품 등록 버튼

// 이력조회 모달 관련
const selectedHistoryItems = ref([]);
const historyModalVisible = ref(false); // 모달 표시 여부
const selectedPcode = ref(''); // 선택된 제품코드
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
    if (!selectedPcode.value) {
        console.warn('pcode가 비어있습니다');
        return [];
    }

    await fetchChangeHistory(selectedPcode.value); // 데이터를 불러옴
    selectedHistoryItems.value = changeHistory.value;
    return changeHistory.value;
};

// 테이블에서 "이력조회" 버튼 클릭 시 실행되는 핸들러
const handleViewHistory = async (rowData) => {
    selectedPcode.value = rowData.pcode;
    await store.fetchChangeHistory(rowData.pcode);

    console.log('[DEBUG] changeHistory:', changeHistory.value);
    historyModalVisible.value = true;
};

// UI 구성 정의
onBeforeMount(() => {
    searchColumns.value = [
        { key: 'pcode', label: '제품코드', type: 'text', placeholder: '제품코드를 입력하세요' },
        { key: 'prodName', label: '제품명', type: 'text', placeholder: '제품명을 입력하세요' },
        {
            key: 'wei',
            label: '중량',
            type: 'dropdown',
            options: [
                { label: '230', value: 'n1' },
                { label: '240', value: 'n2' }
            ]
        },
        {
            key: 'unit',
            label: '단위',
            type: 'dropdown',
            options: [
                { label: 'EA', value: 'g5' }
            ]
        },

    ];
    inputColumns.value = [
        { key: 'pcode', label: '제품코드', type: 'readonly' },
        { key: 'prodName', label: '제품명', type: 'text' },
        {
            key: 'wei',
            label: '중량',
            type: 'dropdown',
            options: [
                { label: '230', value: 'n1' },
                { label: '240', value: 'n2' }
            ]
        },
        { key: 'unit', label: '단위', type: 'dropdown', options: [{ label: 'EA', value: 'g5' }] },
        { key: 'edate', label: '소비기한(일)', type: 'number' },
        { key: 'stoTemp', label: '보관온도', type: 'text' },
        { key: 'safeStock', label: '안전재고', type: 'number' },
        { key: 'pacUnit', label: '포장단위', type: 'dropdown', options: [{ label: '40ea,1box', value: 'l1' }] },
        { key: 'primeCost', label: '원가(원)', type: 'number' },
        { key: 'prodUnitPrice', label: '제품단가(원)', type: 'number' },
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
            disabled: (row) => !row.pcode
        },
        { key: 'note', label: '비고', type: 'textarea', rows: 1, cols: 20 },
        { key: 'regDt', label: '등록일자', type: 'readonly', defaultValue: today }
    ];

    productColumns.value = [
        { field: 'pcode', header: '제품코드' },
        { field: 'prodName', header: '제품명' },
        { field: 'wei', header: '중량' },
        { field: 'unit', header: '단위' },
        { field: 'stoTemp', header: '보관온도' }
    ];

    inputFormButtons.value = {
        save: { show: true, label: '저장', severity: 'success' }
    };
});

// dropdown 한개인경우 자동선택
watch(
    formData,
    (val) => {
        inputColumns.value.forEach((col) => {
            if (col.type === 'dropdown' && col.options.length === 1) {
                if (!val[col.key]) {
                    formData.value[col.key] = col.options[0].value;
                }
            }
        });
    },
    { immediate: true }
);

onMounted(async () => {
    await common.fetchCommonCodes('0N'); // 중량
    await common.fetchCommonCodes('0G'); // 단위
    await fetchProducts();
});

// 제품기준정보 등록 처리
const handleSaveProduct = async () => {
    const result = await saveProduct();
    alert(result === '등록 성공' ? '등록 성공' : result);
};

// 📄 10. 자재 단건 조회 처리
const handleSelectProduct = async (selectedRow) => {
    await fetchProductDetail(selectedRow.pcode);
};

const clearForm = () => {
    formData.value = {}; // 또는 필요한 초기화 방식으로
};

const handleReset = async () => {
    await fetchProducts(); // 전체 목록 다시 조회
};

// 검색
const handleSearch = async (searchData) => {
  await fetchProducts(); // 최신 데이터 가져오기

  // 조건 키: mcode, mateName, mateType, stoCon
  productList.value = productList.value.filter((item) => {
    const matchPcode     = !searchData.pcode     || item.pcode?.toLowerCase().includes(searchData.pcode);
    const matchProdName  = !searchData.prodName  || item.prodName?.includes(searchData.prodName);
    const matchWei  = !searchData.wei  || item.wei === searchData.wei;
    const matchUnit    = !searchData.unit    || item.unit === searchData.unit;

    return matchPcode && matchProdName && matchWei && matchUnit;
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
                title="제품 기준정보 목록"
                :data="convertedproductList"
                dataKey="pcode"
                :columns="productColumns"
                @view-history="handleViewHistory"
                @row-select="handleSelectProduct"
                @clear-selection="clearForm"
                :scrollable="true"
                scrollHeight="470px"
                height="560px"
            />
        </div>
        <div class="w-full md:basis-[45%]">
            <InputForm title="제품정보" :columns="inputColumns" v-model:data="formData" :buttons="inputFormButtons" @submit="handleSaveProduct" />
        </div>
    </div>
    <BasicModal v-model:visible="historyModalVisible" :items="changeHistory" :columns="changeColumns" :itemKey="'version'" :fetchItems="fetchHistoryItems" />
</template>
